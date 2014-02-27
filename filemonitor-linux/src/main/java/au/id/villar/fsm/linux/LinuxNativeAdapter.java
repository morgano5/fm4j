package au.id.villar.fsm.linux;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a successfully opened Inotify Linux fd. It encapsulates necessary native calls to Inotify
 * ( see inotify(7) in man pages ), specifically: inotify_init(2), inotify_add_watch(2), inotify_rm_watch(2)
 * and close(2). Every time a new object of this class is successfully created, a new inotify fd is associated to it.
 */
public class LinuxNativeAdapter implements Closeable {

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

			Path tempFile = Files.createTempFile(LinuxNativeAdapter.class.getCanonicalName() + "_", ".so");
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


	private class ListeningTask extends Thread {

		private ListeningTask() {
			setDaemon(false);
			setName("Inotify listener for FD " + fd);
		}

		@Override
		public void run() {
			try {
				LinuxNativeAdapter.this.listenForEvents(fd);
			} catch(LinuxNativeErrorException e) {
				fdIsAlive = false;
				for(InotifyChangeListener listener: listeners) {
					listener.processFatalException(e);
				}
			}
		}

	}



	private volatile int fd = -1;
	private volatile boolean fdIsAlive;
	private volatile boolean active;
	private final List<InotifyChangeListener> listeners = new ArrayList<>();

	private Thread listeningTask;

	/**
	 * Creates a new Linux Inotify adapter.
	 */
	public LinuxNativeAdapter() {
		fd = nativeInit();
		active = true;
		fdIsAlive = true;
		listeningTask = new ListeningTask();
		listeningTask.start();
	}

	public int getFd() {
		return fd;
	}

	public void addListener(InotifyChangeListener listener) {
		synchronized (listeners) {
			if(!fdIsAlive) {
				return;
			}
			listeners.add(listener);
		}
	}

	public void removeListener(InotifyChangeListener listener) {
		synchronized (listeners) {
			if(!fdIsAlive) {
				return;
			}
			listeners.remove(listener);
		}
	}

	public int register(Path path, InotifyMessage... messages) {
		if(!fdIsAlive) {
			return -1;
		}
		path = path.toAbsolutePath().normalize();
		return addWatch(fd, path.toString(), InotifyMessage.calculateMask(messages));
	}

	public void unregister(int wd) {
		if(!fdIsAlive) {
			return;
		}
		removeWatch(fd, wd);
	}

	@Override
	public void close() {
		fdIsAlive = false;
		active = false;
		try {
			listeningTask.join();
		} catch (NullPointerException | InterruptedException ignore) {}
		for(InotifyChangeListener listener: listeners) {
			listener.processClose();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	private native int nativeInit();

	private native int addWatch(int fd, String path, int flags);

	private native void removeWatch(int fd, int wd);

	private native void listenForEvents(int fd);

	// DO NOT REFACTORIZE -- this method is called in the native code
	@SuppressWarnings("unused")
	private int callbackProcessEvent(String name, int wd, int mask, int cookie) {
		for(InotifyChangeListener listener: listeners) {

			listener.processEvent(name, wd, mask, cookie);
		}
		return callbackStatusCheck();
	}

	// DO NOT REFACTORIZE -- this method is called in the native code
	private int callbackStatusCheck() {
		return active? 0: -1;
	}

}
