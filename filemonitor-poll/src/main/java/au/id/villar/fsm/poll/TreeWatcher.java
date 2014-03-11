package au.id.villar.fsm.poll;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
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





	private final Path rootDir;
	private final boolean followLinks;
	private final Pattern[] ignorePatterns;
	private final List<TreeListener> listeners = new ArrayList<>();

	private final FileTree tree;

	public TreeWatcher(Path rootDir, boolean followLinks) {
		this.rootDir = rootDir;
		this.followLinks = followLinks;
		this.ignorePatterns = new Pattern[0];
		tree = new FileTree(rootDir, followLinks, ignorePatterns);
	}

	public TreeWatcher(Path rootDir, boolean followLinks, String ... ignorePatterns) {
		this.rootDir = rootDir;
		this.followLinks = followLinks;
		this.ignorePatterns = new Pattern[ignorePatterns.length];
		int index = 0;
		for(String pattern: ignorePatterns) {
			this.ignorePatterns[index++] = Pattern.compile(pattern);
		}
		tree = new FileTree(rootDir, followLinks, this.ignorePatterns);
	}

	public TreeWatcher(Path rootDir, boolean followLinks, Pattern ... ignorePatterns) {
		this.rootDir = rootDir;
		this.followLinks = followLinks;
		this.ignorePatterns = ignorePatterns;
		tree = new FileTree(rootDir, followLinks, ignorePatterns);
	}


	public void start() {
new MonitorThread().start();
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



	private void monitorTree() {



	}



	private class MonitorThread extends Thread {

		private MonitorThread() {
			super("File monitor ");
			super.setDaemon(true);
		}

		@Override
		public void run() {
			while(!this.isInterrupted()) {
				try {
					Thread.sleep(5000);
					FileTree newTree = new FileTree(rootDir, followLinks, ignorePatterns);
					List<FileEvent> changes =  tree.getChanges(newTree);
					for (TreeListener listener: listeners) {
						for(FileEvent event: changes) {
							listener.fileChanged(event);
						}
					}

				} catch(InterruptedException e) {}
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
		TreeWatcher watcher = new TreeWatcher(Paths.get(/**/"/home/rafael/Desktop/testingDir"/*/"../../.."/**/), true, "/home/rafael/Desktop/testingDir/\\..*");
		watcher.addListener(new TreeListener() {
			@Override
			public void fileChanged(FileEvent event) {
				if(event.getType() == EventType.FILE_MOVED) {
					System.out.format(">> %s -- %s   -->   %s%n", event.getType().toString(), event.getOldPath(), event.getPath());
				} else {
					System.out.format(">> %s -- %s%n", event.getType().toString(), event.getPath());
				}

			}
		});
		watcher.start();

while(true) { Thread.sleep(1000); }

//		System.gc();
//		Thread.sleep(2000);
//
//		System.out.format("MAX:   %12d%nTOTAL: %12d%nFREE:  %12d%nUSED:  %12d%n%nMilliseconds: %d%n",
//				Runtime.getRuntime().maxMemory(),
//				Runtime.getRuntime().totalMemory(),
//				Runtime.getRuntime().freeMemory(),
//				Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(),
//				System.currentTimeMillis() - start);
//
//		Thread.sleep(1000);

//		TreeWatcher watcher = new TreeWatcher(Paths.get(/*"/home/villarr"*/"../../.."));
//		System.out.println(watcher.readLink("/home/rafael/Desktop/m/link"));

	}



}
