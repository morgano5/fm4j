package au.id.villar.fsm.poll;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

class FileTree {


	private class QueueNode {
		Node node;
		Path path;
		QueueNode(Node node, Path path) { this.node = node; this.path = path; }
	}

	private class ByNameComparator implements Comparator<Node> {
		@Override
		public int compare(Node o1, Node o2) {
			return o1.name.compareTo(o2.name);
		}
	}

	private ByNameComparator byNameComparator = new ByNameComparator();




	private final Path rootDir;
	private final boolean followLinks;
	private final Pattern[] ignorePatterns;

	private DirNode rootNode;
	private Map<Long, Node> inodeToNode;


	FileTree(Path rootDir, boolean followLinks, Pattern ... ignorePatterns) {
		this.rootDir = rootDir;
		this.followLinks = followLinks;
		this.ignorePatterns = ignorePatterns;
		createTree();
	}

	List<FileEvent> updateAndGetChanges(FileTree newer) {
		List<FileEvent> fileEvents = new ArrayList<>();
		compareDirs(rootNode, newer.rootNode, inodeToNode, newer.inodeToNode, fileEvents);
		return fileEvents;
	}


	private Path nodeToPath(Node node) {
		Deque<String> pathParts = new ArrayDeque<>();
		while(node != null) {
			if(!"".equals(node.name)) {
				pathParts.push(node.name);
			}
			node = node.parent;
		}
		String part;
		Path path = rootDir;
		while ((part = pathParts.poll()) != null) {
			path = path.resolve(part);
		}
		return path;
	}

	private void compareDirs(DirNode oldNode, DirNode newNode, Map<Long, Node> oldInodes, Map<Long, Node> newInodes,
							 List<FileEvent> fileEvents) {

		Iterator<Node> oldNodes = new ArrayList<>(oldNode.children).iterator();
		Iterator<Node> newNodes = new ArrayList<>(newNode.children).iterator();


		Node oldChild = next(oldNodes);
		Node newChild = next(newNodes);

		while(oldChild != null && newChild != null) {
			int nameComp = oldChild.name.compareTo(newChild.name);
			if(nameComp == 0) {
				if(oldChild instanceof DirNode && newChild instanceof DirNode) {
					compareDirs((DirNode)oldChild, (DirNode)newChild, oldInodes, newInodes, fileEvents);
				} else if(oldChild instanceof DirNode || newChild instanceof DirNode) {
					handleMovedFromDirOrDeleted(newInodes, oldInodes, oldChild, oldNode, fileEvents);
					handleMovedToDirOrAdded(oldInodes, newChild, oldNode, fileEvents);
				} else {
					if(oldChild.inode == newChild.inode) {
						if(oldChild.lastUpdated != newChild.lastUpdated) {
							fileEvents.add(new FileEvent(EventType.FILE_MODIFIED, nodeToPath(newChild), null));
							oldChild.lastUpdated = newChild.lastUpdated;
						}
					} else {
						handleMovedFromDirOrDeleted(newInodes, oldInodes, oldChild, oldNode, fileEvents);
						handleMovedToDirOrAdded(oldInodes, newChild, oldNode, fileEvents);
					}
				}
				oldChild = next(oldNodes);
				newChild = next(newNodes);
			} else if(nameComp > 0) {
				handleMovedToDirOrAdded(oldInodes, newChild, oldNode, fileEvents);
				newChild = next(newNodes);
			} else {
				handleMovedFromDirOrDeleted(newInodes, oldInodes, oldChild, oldNode, fileEvents);
				oldChild = next(oldNodes);
			}
		}
		while(oldChild != null) {
			handleMovedFromDirOrDeleted(newInodes, oldInodes, oldChild, oldNode, fileEvents);
			oldChild = next(oldNodes);
		}
		while(newChild != null) {
			handleMovedToDirOrAdded(oldInodes, newChild, oldNode, fileEvents);
			newChild = next(newNodes);
		}
	}

	private <T> T next(Iterator<T> iterator) {
		return iterator.hasNext()? iterator.next(): null;
	}

	private void handleMovedToDirOrAdded(Map<Long, Node> oldInodes, Node newChild, DirNode oldDir,
										 List<FileEvent> fileEvents) {
		Node node;
		if((node = oldInodes.get(newChild.inode)) != null) {

			// node was moved to current dir
			Path oldPath = nodeToPath(node);
			DirNode parent = node.parent;
			parent.children.remove(node);
			node.parent = oldDir;
			oldDir.children.add(node);
			node.lastUpdated = newChild.lastUpdated;
			node.name  = newChild.name;
			Collections.sort(oldDir.children, byNameComparator);
			Path newPath = nodeToPath(node);
			fileEvents.add(new FileEvent(EventType.FILE_MOVED, newPath, oldPath));

		} else {

			// node is new
			node = newChild;
			oldDir.children.add(node);
			node.parent = oldDir;
			Collections.sort(oldDir.children, byNameComparator);
			oldInodes.put(node.inode, node);
			Path path = nodeToPath(node);
			fileEvents.add(new FileEvent(EventType.FILE_ADDED, path, null));
			if(node instanceof DirNode) {
				for(Node child: new ArrayList<>(((DirNode)node).children)) {
					handleMovedToDirOrAdded(oldInodes, child, (DirNode)node, fileEvents);
				}
			}

		}
	}

	private void handleMovedFromDirOrDeleted(Map<Long, Node> newInodes, Map<Long, Node> oldInodes, Node oldChild,
											 DirNode oldDir, List<FileEvent> fileEvents) {

		if(!newInodes.containsKey(oldChild.inode)) {

			// node was deleted
			fileEvents.add(new FileEvent(EventType.FILE_DELETED, nodeToPath(oldChild), null));
			oldDir.children.remove(oldChild);
			oldInodes.remove(oldChild.inode);
			if(oldChild instanceof DirNode) {
				for(Node child: new ArrayList<>(((DirNode)oldChild).children)) {
					handleMovedFromDirOrDeleted(newInodes, oldInodes, child, (DirNode)oldChild, fileEvents);
				}
			}

		} /* else {

			// node was moved from current dir
			do nothing, this case will be or was already processed by "node was moved to current dir"
			in handleMovedToDirOrAdded()

		} */
	}


	private void createTree() {

		Node node;
		QueueNode queueNode;
		Deque<QueueNode> nodes = new ArrayDeque<>();

		rootNode = (DirNode)createNode(rootDir);
		rootNode.name = "";
		inodeToNode = new HashMap<>();
		inodeToNode.put(rootNode.inode, rootNode);
		nodes.push(new QueueNode(rootNode, rootDir));

		while((queueNode = nodes.poll()) != null) {

			node = queueNode.node;
			Path nodePath = queueNode.path;

			if(node instanceof DirNode) {
				DirNode dirNode = (DirNode)node;
				try {
					for(String name: readDir(nodePath.toString())) {
						Path newPath = nodePath.resolve(name);
						boolean ignorePath = false;
						for(Pattern ignorePattern: ignorePatterns) {
							if(ignorePattern.matcher(newPath.toString()).matches()) {
								ignorePath = true;
								break;
							}
						}
						if(!ignorePath) {
							Node newNode = createNode(newPath);
							if(newNode != null) {
								dirNode.children.add(newNode);
								newNode.parent = dirNode;
								nodes.push(new QueueNode(newNode, newPath));
								inodeToNode.put(newNode.inode, newNode);
							}
						}
					}
					Collections.sort(dirNode.children, byNameComparator);
				} catch(NoSuchFileOrDirectoryException | NotADirException ignore) {
				}
			}
		}
	}

	private Node createNode(Path path) {

		Node node;
		PathInfo info;
		String strPath = path.toString();
		String name = path.getNameCount() > 0? path.getName(path.getNameCount() - 1).toString(): "";

		try {
			info = getInfo(strPath);
		} catch (NoSuchFileOrDirectoryException | NotADirException e) {
			return null;
		}

		if(info.isFile()) {
			node = new Node();
		} else if(info.isDirectory()) {
			node = new DirNode();
		} else if(info.isLink()) {
			LinkNode linkNode = new LinkNode();
			try {
				linkNode.link = readLink(strPath);
			} catch (NoSuchFileOrDirectoryException | NotADirException e) {
				return null;
			}
			node = linkNode;
		} else {
			// node type not supported
			return null;
		}
		node.inode = info.getInode();
		node.lastUpdated = info.getLastStatusChange();
		node.name = name;
		return node;
	}


	private native PathInfo getInfo(String path, boolean followLinks);

	private native String readlink(String path, int size);

	private native List<String> readDir(String path);

	private String readLink(String path) {
		return readlink(path, -1);
	}

	private PathInfo getInfo(String path) {
		return getInfo(path, followLinks);
	}



}
