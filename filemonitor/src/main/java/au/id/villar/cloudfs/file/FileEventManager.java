package au.id.villar.cloudfs.file;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

class FileEventManager extends Thread {

	private abstract class DirNode {
		WatchKey watchKey;
		String name;
		Path path;
		DirNode parent;
	}

	private class RootAncestorNode extends DirNode {
		DirNode child;
	}

	private class SubDirNode extends DirNode {
		final Map<String, DirNode> children = new HashMap<>();
	}


	private final BlockingQueue<InputEvent> inputEvents;
	private final BlockingQueue<OutputEvent> outputEvents;

	private final WatchService watcher;
	private final Path root;

	private final Map<WatchKey, DirNode> keyToNode;
	private DirNode rootNode;

	public FileEventManager(BlockingQueue<InputEvent> inputEvents,
							BlockingQueue<OutputEvent> outputEvents,
							WatchService watcher, Path root) throws IOException {
		super("File event manager");
		super.setDaemon(true);
		this.inputEvents = inputEvents;
		this.outputEvents = outputEvents;
		this.watcher = watcher;
		this.root = root;
		this.keyToNode = new HashMap<>();

		registerRoot();
	}

	@Override
	public void run() {
		try {

			while(!isInterrupted()) {

				InputEvent event = inputEvents.take();

				switch(event.getEventType()) {
					case START_LISTENING_FILES:
						startListening();
						break;
					case STOP_LISTENING_FILES:
						this.interrupt();
						break;
					case NODE_ADDED:
						nodeAdded((FileChangeEventData)event.getData());
						break;
					case NODE_MODIFIED:
						nodeModified((FileChangeEventData)event.getData());
						break;
					case NODE_DELETED:
						nodeDeleted((FileChangeEventData)event.getData());
						break;
					case OVERFLOW:
						overflow((WatchKey)event.getData());
						break;
					case CANCELLED_KEY:
						cancelledKey((WatchKey)event.getData());
						break;
					default:
						throw new RuntimeException("Unknown event type: " + event.getEventType());
				}
			}
		} catch (InterruptedException ignore) {
		} finally {
			for(WatchKey key: keyToNode.keySet()) {
				key.cancel();
			}
			putOutputEvent(OutputEventType.STOP_LISTENING_FILES, null);
		}
	}

	private void registerRoot() throws IOException {
		RootAncestorNode ancestorNode;

		ancestorNode = new RootAncestorNode();
		ancestorNode.path = root.getRoot();
		ancestorNode.watchKey = ancestorNode.path.register(watcher, ENTRY_DELETE, ENTRY_CREATE);
		rootNode = ancestorNode;

		Path parents = root.subpath(0, root.getNameCount() - 1);
		Path last = root.subpath(root.getNameCount() - 1, root.getNameCount());

		DirNode currentNode = rootNode;

		for(Path part: parents) {
			currentNode = addNodeToTree(currentNode, part.toString(), RootAncestorNode.class);
		}

		currentNode = addNodeToTree(currentNode, last.toString(), SubDirNode.class);
		if(!Files.isDirectory(currentNode.path)) {
			throw new IOException(currentNode.path + " is not a directory");
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends DirNode> T addNodeToTree(DirNode parent, String name, Class<T> childClass) throws IOException {

		DirNode node;
		WatchEvent.Kind<?>[] eventsToRegister;


		if(childClass == RootAncestorNode.class) {
			node = new RootAncestorNode();
			eventsToRegister = new WatchEvent.Kind[] {ENTRY_DELETE, ENTRY_CREATE};
		} else if(childClass == SubDirNode.class) {
			node = new SubDirNode();
			eventsToRegister = new WatchEvent.Kind[] {ENTRY_DELETE, ENTRY_CREATE, ENTRY_MODIFY};
		} else {
			throw new RuntimeException("Unknown node type: " + childClass.getName());
		}

		node.path = parent.path.resolve(name);
		node.watchKey = node.path.register(watcher, eventsToRegister);
		node.name = name;

		node.parent = parent;
		if(parent instanceof RootAncestorNode) {
			((RootAncestorNode)parent).child = node;
		} else if(parent instanceof SubDirNode) {
			((SubDirNode)parent).children.put(name, node);
		}
		keyToNode.put(node.watchKey, node);

		return (T)node;
	}


	private void addAll(SubDirNode parent, final boolean nodeAddedEvent) throws IOException {

		Path path = parent.path;

		try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
			for(Path subPath: dirStream) {
				if(Files.isDirectory(subPath)) {
					SubDirNode subDir = addNodeToTree(parent, subPath.subpath(subPath.getNameCount() - 1, subPath.getNameCount()).toString(), SubDirNode.class);
					addAll(subDir, nodeAddedEvent);
				}
				if(nodeAddedEvent) {
					putOutputEvent(OutputEventType.NODE_ADDED, subPath);
				} else {
					putOutputEvent(OutputEventType.NODE_REGISTERED, subPath);
				}
			}
		}
	}

	private void startListening() {

		DirNode dir = rootNode;

		putOutputEvent(OutputEventType.START_LISTENING_FILES, null);

		while(dir instanceof RootAncestorNode) {
			dir = ((RootAncestorNode)dir).child;
		}

		try {
			addAll((SubDirNode)dir, false);
		} catch (IOException e) {
			putOutputEvent(OutputEventType.ERROR, null);
		}
	}

	private void nodeAdded(FileChangeEventData fileChangeEventData) {

		WatchKey key = fileChangeEventData.getWatchKey();
		Path child = fileChangeEventData.getChangedChild();
		DirNode node = keyToNode.get(key);
		Path path = node.path.resolve(child);

		if(node instanceof RootAncestorNode) {
			// TODO
		} else if (node instanceof SubDirNode) {
			putOutputEvent(OutputEventType.NODE_ADDED, path);
			if(Files.isDirectory(path)) {
				try {
					SubDirNode childNode = addNodeToTree(node, child.toString(), SubDirNode.class);
					addAll(childNode, true);
				} catch (IOException e) {
					putOutputEvent(OutputEventType.ERROR, path);
				}
			}
		} else {
			putOutputEvent(OutputEventType.ERROR, null);
		}

	}

	private void nodeModified(FileChangeEventData fileChangeEventData) {

		WatchKey key = fileChangeEventData.getWatchKey();
		Path child = fileChangeEventData.getChangedChild();
		SubDirNode node = (SubDirNode)keyToNode.get(key);
		Path path = node.path.resolve(child);

		if(Files.isDirectory(path)) {
			// TODO
		}
		putOutputEvent(OutputEventType.NODE_MODIFIED, path);
	}

	private void nodeDeleted(FileChangeEventData fileChangeEventData) {

		WatchKey key = fileChangeEventData.getWatchKey();
		Path child = fileChangeEventData.getChangedChild();
		DirNode node = keyToNode.get(key);
		Path path = node.path.resolve(child);

		if(node instanceof RootAncestorNode) {
			// TODO
		} else if (node instanceof SubDirNode) {
			putOutputEvent(OutputEventType.NODE_DELETED, path);
			if(Files.isDirectory(path)) {
				// TODO
			}
		} else {
			putOutputEvent(OutputEventType.ERROR, null);
		}
	}





	private void rootMissingNodeEvent(WatchKey key, RootMissingMonitorDirInfo dirInfo, Path path, Path child) {
		RootInfo rootInfo = dirInfo.getRootInfo();
		Path rootPath = rootInfo.getPath().getName(path.getNameCount());
		if(rootPath.equals(child)) {
			removeWatchKey(key);
			WatchKey relatedKey = dirInfo.getRelatedKey();
			if(relatedKey != null) {
				removeWatchKey(relatedKey);
			}
			registerMissingRoot();
		}
	}

	private void removeWatchKey(WatchKey key) {
		key.cancel();
		keyToNode.remove(key);
	}

	private void overflow(WatchKey key) {
		// TODO
		throw new RuntimeException("NOT YET IMPLEMENTED");
	}

	private void cancelledKey(WatchKey key) {
		keyToNode.remove(key);
	}

//	private void registerAll(final Path start, final boolean nodeAddedEvent) {
//
//		try {
//			Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
//
//				@Override
//				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//					register(dir);
//					return nodeAdded(dir);
//				}
//
//				@Override
//				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//					return nodeAdded(file);
//				}
//
//				private FileVisitResult nodeAdded(Path file) {
//					if(nodeAddedEvent) {
//						putOutputEvent(InputEventType.NODE_ADDED, file);
//					} else {
//						putOutputEvent(InputEventType.NODE_REGISTERED, file);
//					}
//					return FileVisitResult.CONTINUE;
//				}
//
//			});
//		} catch (IOException ignore) {
//			// IOException is already handled
//		}
//
//	}

	private void registerMissingRoot() {
//		rootMissing = true;
		// TODO
		throw new RuntimeException("NOT YET IMPLEMENTED");
	}

//	private void register(Path dir) {
//		try {
//			WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
//			keyToNode.put(key, dir);
//		} catch (IOException e) {
//			// TODO decide what to do if an exception happens here, not clear in which circumstances this could happen
//			throw new RuntimeException(e);
//		}
//	}

	private void putOutputEvent(OutputEventType type, Path path) {
		path = path != null? root.relativize(path): null;
		boolean success = false;
		while (!success) {
			try {
				outputEvents.put(new OutputEvent(type, path));
				success = true;
			} catch (InterruptedException e) {
				sleepABit();
			}
		}
	}
	private void sleepABit() {
		try {Thread.sleep(1000);} catch (InterruptedException ignore) {}
	}

}
