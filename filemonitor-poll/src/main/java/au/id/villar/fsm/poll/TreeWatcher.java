package au.id.villar.fsm.poll;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class TreeWatcher {

	/**
	 * Specifies if the native library was successfully loaded
	 */
	public static final boolean LOADED;

	/**
	 * If the native library wasn't loaded, then this is the Exception thrown, otherwise it is null.
	 */
	public static final IOException LOAD_EXCEPTION;

	private static final String SHARED_NATIVE_LIB_RESOURCE = "filemonitor.so";

	// native library loading stuff
	static {

		boolean loaded = true;
		IOException exception = null;

		try {
			int size;
			byte[] buffer = new byte[2048];

			Path tempFile = Files.createTempFile(TreeWatcher.class.getCanonicalName() + "_", ".so");
			try(InputStream input = ClassLoader.getSystemResourceAsStream(SHARED_NATIVE_LIB_RESOURCE)) {
				try(OutputStream output = Files.newOutputStream(tempFile)) {
					while((size = input.read(buffer)) != -1) {
						output.write(buffer, 0, size);
					}
				}
			}
			System.load(tempFile.toString());
		} catch (IOException e) {
			loaded = false;
			exception = e;
		} finally {
			LOADED = loaded;
			LOAD_EXCEPTION = exception;
		}

	}


	/* Utility classes: file info is stored in a form of a tree, that reflects a sort of  snapshot of the
	*/

	// Contains
	private class Node {
		Node parent;
		long inode;
		long lastUpdated;
		String name;
	}

	private class DirNode extends Node {
		List<Node> children = new ArrayList<>();
	}

	private class LinkNode extends Node {
		String link;
	}

	class QueueNode { Node node; Path path;
		QueueNode(Node node, Path path) { this.node = node; this.path = path; }
	}




	private final Path rootDir;
	private final boolean followLinks;
	private final Pattern[] ignorePatterns;
	private final List<TreeListener> listeners = new ArrayList<>();

	private Node rootNode;

	public TreeWatcher(Path rootDir, boolean followLinks) {
		this.rootDir = rootDir;
		this.followLinks = followLinks;
		this.ignorePatterns = new Pattern[0];
		createTree();
	}

	public TreeWatcher(Path rootDir, boolean followLinks, String ... ignorePatterns) {
		this.rootDir = rootDir;
		this.followLinks = followLinks;
		this.ignorePatterns = new Pattern[ignorePatterns.length];
		int index = 0;
		for(String pattern: ignorePatterns) {
			this.ignorePatterns[index++] = Pattern.compile(pattern);
		}
		createTree();
	}

	public TreeWatcher(Path rootDir, boolean followLinks, Pattern ... ignorePatterns) {
		this.rootDir = rootDir;
		this.followLinks = followLinks;
		this.ignorePatterns = ignorePatterns;
		createTree();
	}


	public void start() {

	}

	public void stop() {

	}

	public void addListener(TreeListener listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}

	public boolean removeListener(TreeListener listener) {
		synchronized(listeners) {
			return listeners.remove(listener);
		}
	}

	private void createTree() {

		Node node;
		QueueNode queueNode;
		Queue<QueueNode> nodes = new LinkedList<>();

		rootNode = createNode(rootDir);
		nodes.add(new QueueNode(rootNode, rootDir));

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
								nodes.add(new QueueNode(newNode, newPath));
							}
						}
					}
				} catch(NoSuchFileOrDirectoryException | NotADirException ignore) {
				}
			}
		}
	}

	private Node createNode(Path path) {

		Node node;
		PathInfo info;
		String strPath = path.toString();
		String name = path.getName(path.getNameCount() - 1).toString();

		try {
			info = getInfo(strPath, followLinks);
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
		node.lastUpdated = info.getLastModification();
		node.name = name;
		return node;
	}



	private native PathInfo getInfo(String path, boolean followLinks);

	private native String readlink(String path, int size);

	private native List<String> readDir(String path);

	private String readLink(String path) {
		return readlink(path, -1);
	}


	private class MonitorThread extends Thread {

		private MonitorThread() {
			super("File monitor ");
			super.setDaemon(true);
		}

		@Override
		public void run() {
			while(!this.isInterrupted()) {

				// todo
			}
		}

	}





	public static void main(String[] args) throws InterruptedException {

//		TreeWatcher watcher = new TreeWatcher(Paths.get("/home/villarr/Desktop/testingDir"));

//		GregorianCalendar g = new GregorianCalendar();
//		g.setTimeInMillis(new TreeWatcher(null).getInfo("/home/rafael/Desktop/testingDir").getLastModification() * 1000);
//		System.out.format(">>> %04d-%02d-%02d %02d:%02d:%02d%n", g.get(Calendar.YEAR), g.get(Calendar.MONTH), g.get(Calendar.DAY_OF_MONTH),
//				g.get(Calendar.HOUR_OF_DAY), g.get(Calendar.MINUTE), g.get(Calendar.SECOND));

//		System.out.println(">>> " + new TreeWatcher(null).getInfo(/*"/home/rafael/Desktop/testingDir"*/ "/dev/sda1"));
//		System.out.println(">>> " + new TreeWatcher().readLink("/home/villarr/.wine/dosdevices/c:") + " <<<");


		long start = System.currentTimeMillis();
		System.out.println("Working...");
		TreeWatcher watcher = new TreeWatcher(Paths.get(/**/"/home/villarr"/*/"../../.."/**/), true, "/home/villarr/\\..*");
		watcher.start();

		System.gc();
		Thread.sleep(2000);

		System.out.format("MAX:   %12d%nTOTAL: %12d%nFREE:  %12d%nUSED:  %12d%n%nMilliseconds: %d%n",
				Runtime.getRuntime().maxMemory(),
				Runtime.getRuntime().totalMemory(),
				Runtime.getRuntime().freeMemory(),
				Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(),
				System.currentTimeMillis() - start);

		Thread.sleep(1000);

//		TreeWatcher watcher = new TreeWatcher(Paths.get(/*"/home/villarr"*/"../../.."));
//		System.out.println(watcher.readLink("/home/rafael/Desktop/m/link"));

	}



}
