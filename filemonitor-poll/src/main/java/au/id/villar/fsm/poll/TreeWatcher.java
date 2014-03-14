package au.id.villar.fsm.poll;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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





	private final Path rootDir;
	private final boolean followLinks;
	private final Pattern[] ignorePatterns;
	private final List<TreeListener> listeners = new ArrayList<>();
	private final FileTree tree;

	private MonitorThread monitorThread;

	/**
	 *
	 * @param rootDir
	 * @param followLinks
	 */
	public TreeWatcher(Path rootDir, boolean followLinks) {
		this.rootDir = rootDir;
		this.followLinks = followLinks;
		this.ignorePatterns = new Pattern[0];
		tree = new FileTree(rootDir, followLinks, ignorePatterns);
	}

	/**
	 *
	 * @param rootDir
	 * @param followLinks
	 * @param ignorePatterns
	 */
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

	/**
	 *
	 * @param rootDir
	 * @param followLinks
	 * @param ignorePatterns
	 */
	public TreeWatcher(Path rootDir, boolean followLinks, Pattern ... ignorePatterns) {
		this.rootDir = rootDir;
		this.followLinks = followLinks;
		this.ignorePatterns = ignorePatterns;
		tree = new FileTree(rootDir, followLinks, ignorePatterns);
	}

	/**
	 *
	 */
	public synchronized void start() {
		if(monitorThread == null || !monitorThread.isAlive() || monitorThread.isInterrupted()) {
			monitorThread = new MonitorThread();
			monitorThread.start();
		}
	}

	/**
	 *
	 */
	public synchronized void stop() {
		if(monitorThread != null && monitorThread.isAlive() && !monitorThread.isInterrupted()) {
			monitorThread.interrupt();
			monitorThread = null;
		}
	}

	/**
	 *
	 * @param listener
	 */
	public void addListener(TreeListener listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}

	/**
	 *
	 * @param listener
	 * @return
	 */
	public boolean removeListener(TreeListener listener) {
		synchronized(listeners) {
			return listeners.remove(listener);
		}
	}

	private class MonitorThread extends Thread {

		private MonitorThread() {
			super("File monitor " + rootDir);
			super.setDaemon(true);
		}

		@Override
		public void run() {
			try {
				while(!this.isInterrupted()) {
					Thread.sleep(1000); // TODO make this configurable
					FileTree newTree = new FileTree(rootDir, followLinks, ignorePatterns);
					List<FileEvent> changes =  tree.updateAndGetChanges(newTree);
					for (TreeListener listener: listeners) {
						for(FileEvent event: changes) {
							listener.fileChanged(event);
						}
					}
				}
			} catch(InterruptedException ignore) {}
		}

	}

}
