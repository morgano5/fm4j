package au.id.villar.fsm.poll;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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



	private class Node {
		Node parent;
		long inode;
		long lastUpdated;
		String name;
	}

	private class DirNode extends Node {
		List<Node> children = new ArrayList<>();
	}



	private final Path rootDir;
	private final DirNode rootNode;

	public TreeWatcher(Path rootDir, boolean followLinks) {
		this.rootDir = rootDir;
		rootNode = new DirNode();
		registerDir(rootDir, rootNode, followLinks);
	}

	private void registerDir(Path path, DirNode parent, boolean followLinks) {
		try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
			for(Path child: dirStream) {
				Node childNode;
System.out.println(child);
				PathInfo info = getInfo(child.toString(), followLinks);
				if(info.isDirectory()) {
					DirNode dirNode = new DirNode();
					registerDir(child, dirNode, followLinks);
					childNode = dirNode;
				} else {
					childNode = new Node();
				}
				childNode.parent = parent;
				childNode.inode = info.getInode();
				childNode.lastUpdated = info.getLastModification() * 1000;
				childNode.name = child.getName(child.getNameCount() - 1).toString();
				parent.children.add(childNode);
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		Collections.sort(parent.children, new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return o1.name.compareTo(o2.name);
			}
		});
	}

	private native PathInfo getInfo(String path, boolean followLinks);







	public static void main(String[] args) throws InterruptedException {

//		TreeWatcher watcher = new TreeWatcher(Paths.get("/home/villarr/Desktop/testingDir"));

//		GregorianCalendar g = new GregorianCalendar();
//		g.setTimeInMillis(new TreeWatcher(null).getInfo("/home/rafael/Desktop/testingDir").getLastModification() * 1000);
//		System.out.format(">>> %04d-%02d-%02d %02d:%02d:%02d%n", g.get(Calendar.YEAR), g.get(Calendar.MONTH), g.get(Calendar.DAY_OF_MONTH),
//				g.get(Calendar.HOUR_OF_DAY), g.get(Calendar.MINUTE), g.get(Calendar.SECOND));

//		System.out.println(">>> " + new TreeWatcher(null).getInfo(/*"/home/rafael/Desktop/testingDir"*/ "/dev/sda1"));

		TreeWatcher watcher = new TreeWatcher(Paths.get("/home/rafael"), true);
		System.out.format("MAX:   %12d%nTOTAL: %12d%nFREE:  %12d%nUSED:  %12d%n",
				Runtime.getRuntime().maxMemory(),
				Runtime.getRuntime().totalMemory(),
				Runtime.getRuntime().freeMemory(),
				Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());

		Thread.sleep(1000);
	}



}
