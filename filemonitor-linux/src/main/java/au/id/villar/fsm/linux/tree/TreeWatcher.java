package au.id.villar.fsm.linux.tree;

//import au.id.villar.fsm.ChangeListener;
//import au.id.villar.fsm.FileMonitorException;

import au.id.villar.fsm.linux.InotifyChangeListener;
import au.id.villar.fsm.linux.InotifyMessage;
import au.id.villar.fsm.linux.LinuxNativeAdapter;
import au.id.villar.fsm.linux.LinuxNativeErrorException;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class TreeWatcher implements Closeable {

	private static final InotifyMessage[] BASIC_EVENTS = {
			InotifyMessage.CREATE,
			InotifyMessage.DELETE,
			InotifyMessage.MODIFY,
			InotifyMessage.ATTRIB,
			InotifyMessage.MOVED_FROM,
			InotifyMessage.MOVED_TO,
			InotifyMessage.DONT_FOLLOW

	};

	private static final InotifyMessage[] ROOT_EVENTS = {
			InotifyMessage.DELETE_SELF,
			InotifyMessage.MOVE_SELF,
	};

	private static final InotifyMessage[] EXTENDED_EVENTS = {
			InotifyMessage.ACCESS,
			InotifyMessage.OPEN,
			InotifyMessage.CLOSE_NOWRITE,
			InotifyMessage.CLOSE_WRITE
	};


	private final LinuxNativeAdapter nativeAdapter;

	private final Node root;
	private final Node first;

	private final List<ChangeListener> listeners = new ArrayList<>();
	private final Path rootPath;

	private final Map<Integer, MovedNode> movedNodes = new HashMap<>();


	public TreeWatcher(Path dir, boolean withExtended) {
		dir = dir.toAbsolutePath().normalize();
		rootPath = dir;
		nativeAdapter = new LinuxNativeAdapter();
		try {
			nativeAdapter.addListener(listener);
			int wd = nativeAdapter.register(dir, sumMessages(BASIC_EVENTS, ROOT_EVENTS, EXTENDED_EVENTS));
			root = new Node("", null, wd);
			first = root;
			try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
				for(Path child: stream) {
					if(Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
						registerDir(dir, root, child.getName(child.getNameCount() - 1).toString(), false);
					}
				}
			} catch (IOException e) {
				e.printStackTrace(); // TODO better handling
			}
		} catch (RuntimeException e) {
			nativeAdapter.close();
			throw e;
		}
	}

	@Override
	public void close() throws IOException {
		nativeAdapter.close();
	}

	public void addListener(ChangeListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ChangeListener listener) {
		listeners.remove(listener);
	}







	private InotifyMessage[] sumMessages(InotifyMessage[] ... messageArrays) {
		Set<InotifyMessage> sum = new HashSet<>();
		for(InotifyMessage[] array: messageArrays) {
			for(InotifyMessage message: array) {
				sum.add(message);
			}
		}
		return sum.toArray(new InotifyMessage[sum.size()]);
	}

	private InotifyChangeListener listener = new InotifyChangeListener() {

		@Override
		public void processEvent(String name, int wd, int mask, int cookie) {

			if(wd == -1 && InotifyMessage.Q_OVERFLOW.isInMask(mask)) {
				// TODO
			}

			// TODO implement

			System.out.format("                EVENT -- %d -- %s -- %s (link %d)%n", wd, name, InotifyMessage.containedEvents(mask), cookie);


			if(InotifyMessage.CREATE.isInMask(mask)) {
				Node parent = searchNode(wd);
				Path parentPath = getPath(parent);
				for(ChangeListener listener: listeners) {
					listener.fileAdded(parentPath.resolve(name));
				}
				if(InotifyMessage.ISDIR.isInMask(mask)) {
					registerDir(parentPath, parent, name, true);
				}
			}

			if(InotifyMessage.DELETE.isInMask(mask)) {
				Node parent = searchNode(wd);
				Path parentPath = getPath(parent);
				for(ChangeListener listener: listeners) {
					listener.fileDeleted(parentPath.resolve(name));
				}
			}

			if(InotifyMessage.IGNORED.isInMask(mask)) {
				Node node = searchNode(wd);
				removeNode(node);
			}


			if(InotifyMessage.MODIFY.isInMask(mask)) {
				Node parent = searchNode(wd);
				Path parentPath = getPath(parent);
				for(ChangeListener listener: listeners) {
					listener.fileModified(parentPath.resolve(name));
				}
			}


			if(InotifyMessage.MOVED_FROM.isInMask(mask)) {
				// TODO threads to handle a node moved to a unwatched dir
				Node parent = searchNode(wd);
				Path parentPath = getPath(parent);
				MovedNode movedNode = new MovedNode();
				movedNode.originalPath = parentPath.resolve(name);
				movedNode.timestamp = System.currentTimeMillis();
				if(InotifyMessage.ISDIR.isInMask(mask)) {
					movedNode.node = searchInParent(name, parent);
				}
				movedNodes.put(cookie, movedNode);
			}

			if(InotifyMessage.MOVED_TO.isInMask(mask)) {
				// TODO threads to handle a node moved to a unwatched dir
				Node parent = searchNode(wd);
				Path parentPath = getPath(parent);
				Path source = null;
				Path dest = parentPath.resolve(name);
				MovedNode movedNode = movedNodes.remove(cookie);
				if(movedNode != null) {
					source = movedNode.originalPath;
					if(movedNode.node != null) {
						Node node = movedNode.node;
						if(node.parent != null) {
							node.parent.children.remove(node);
						}
						node.parent = parent;
						parent.children.add(node);
						node.name = name;
					}
				}
				for(ChangeListener listener: listeners) {
					if(source != null) {
						listener.fileMoved(source, dest);
					} else {
						listener.fileAdded(dest);
					}
				}

				if(source == null && InotifyMessage.ISDIR.isInMask(mask)) {

					try(DirectoryStream<Path> stream = Files.newDirectoryStream(dest)) {
						for(Path child: stream) {
							for(ChangeListener listener: listeners) {
								listener.fileAdded(child);
							}
							if(Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
								registerDir(dest, parent, child.getName(child.getNameCount() - 1).toString(), true);
							}
						}
					} catch (IOException e) {
						e.printStackTrace(); // TODO better handling
					}
				}


			}

		}

		@Override
		public void processFatalException(LinuxNativeErrorException e) {
			// TODO implement

			e.printStackTrace();
		}

		@Override
		public void processClose() {
			// TODO implement

			System.out.println("CLOSE");
		}
	};








	private class MovedNode {
		Node node;
		long timestamp;
		Path originalPath;
	}







	private class Node implements Comparable<Node> {

		int wd;
		String name;
		Node parent;
		Node prev;
		Node next;
		final List<Node> children = new ArrayList<>();


		private Node(String name) {
			this.name = name;
			this.wd = -1;
		}

		private Node(String name, Node parent, int wd) {
			this.name = name;
			this.parent = parent;
			this.wd = wd;
		}

		@Override
		public boolean equals(Object o) {
			return this == o || (o != null && getClass() == o.getClass() && (wd == ((Node)o).wd));
		}

		@Override
		public int hashCode() {
			return wd;
		}

		@Override
		public int compareTo(Node o) {
			return wd - o.wd;
		}
	}

	private void registerDir(Path parentDir, Node parentNode, String name, boolean reportAdded) {

		Path pathNode = parentDir.resolve(name);

		int wd = nativeAdapter.register(pathNode, sumMessages(BASIC_EVENTS, EXTENDED_EVENTS));

		Node node = new Node(name, parentNode, wd);
		node.parent.children.add(node);

		// add to list
		Node prevNode = null;
		Node stepNode = first;
		while(stepNode != null && stepNode.compareTo(node) < 0) {
			prevNode = stepNode;
			stepNode = stepNode.next;
		}
		if(stepNode == null) {
			prevNode.next = node;
			node.prev = prevNode;
		} else {
			node.prev = stepNode.prev;
			node.next = stepNode;
			if(stepNode.prev != null) {
				stepNode.prev.next = node;
			}
			stepNode.prev = node;
		}

		try(DirectoryStream<Path> stream = Files.newDirectoryStream(pathNode)) {
			for(Path element: stream) {
				if(reportAdded) {
					for(ChangeListener listener: listeners) {
						listener.fileAdded(element);
					}
				}
				if(Files.isDirectory(element, LinkOption.NOFOLLOW_LINKS)) {
					registerDir(pathNode, node, element.getName(element.getNameCount() - 1).toString(), reportAdded);
				}
			}
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}

	}

	private Node searchNode(int wd) {
		Node stepNode = first;
		while(stepNode != null && stepNode.wd != wd) {
			stepNode = stepNode.next;
		}
		return stepNode;
	}

	private Path getPath(Node node) {
		List<String> parts = new ArrayList<>();
		while(node != root) {
			parts.add(node.name);
			node = node.parent;
		}
		Collections.reverse(parts);
		if(parts.size() > 0) {
			String first = parts.remove(0);
			return rootPath.resolve(Paths.get(first, parts.toArray(new String[parts.size()])));
		} else {
			return rootPath;
		}
	}

	private void removeNode(Node node) {

		if(node.prev != null) {
			node.prev.next = node.next;
		}
		if(node.next != null) {
			node.next.prev = node.prev;
		}
		if(node.parent != null) {
			node.parent.children.remove(node);
		}
		for(Node child: node.children) {
			child.parent = null;
		}

//		nativeAdapter.unregister(node.wd);

	}


	private Node searchInParent(String name, Node parent) {
		for(Node child: parent.children) {
			if(child.name.equals(name)) {
				return child;
			}
		}
		return null;
	}

}
