package au.id.villar.cloudfs.file;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Programmer: Rafael Villar Villar
 * Date: 11/12/13
 * Time: 1:46 PM
 */
public class FileMonitor {

	private InputEventEnqueuer fileListener;
	private FileEventManager fileEventManager;
	private OutputEventDispatcher outputEventDispatcher;

	private WatchService watcher;

	private boolean working;

	private final Path root;

	public FileMonitor(Path root) {
		this.root = root.toAbsolutePath().normalize();
	}

// TODO put this in the appropriate class
//	public void registerRoot(RootInfo root) {
//		root = normalizeRootInfo(root);
//		fileListener.registerRoot(root);
//	}
//
//	public void unregisterRoot(RootInfo root) {
//		root = normalizeRootInfo(root);
//		fileListener.unregisterRoot(root);
//	}
//
//	private RootInfo normalizeRootInfo(RootInfo rootInfo) {
//		Path rootPath = rootInfo.getPath().toAbsolutePath().normalize();
//		return new RootInfo(rootInfo.getId(), rootPath);
//	}

	public void addListener(FileListener listener) {
		outputEventDispatcher.addListener(listener);
	}

	public void removeListener(FileListener listener) {
		outputEventDispatcher.removeListener(listener);
	}

	public synchronized void start() throws IOException {
		if(working) {
			return;
		}
		BlockingQueue<InputEvent> inputEvents = new LinkedBlockingQueue<>();
		BlockingQueue<OutputEvent> outputEvents = new LinkedBlockingQueue<>();

		this.watcher = root.getFileSystem().newWatchService();

		try {
			this.fileListener = new InputEventEnqueuer(inputEvents, watcher);
			this.fileEventManager = new FileEventManager(inputEvents, outputEvents, watcher, root);
			this.outputEventDispatcher = new OutputEventDispatcher(outputEvents);
		} catch (IOException e) {
			//this.fileListener;
			//this.fileEventManager;
			//this.outputEventDispatcher;
			watcher.close();
			throw e;
		}

		this.outputEventDispatcher.start();
		this.fileEventManager.start();
		this.fileListener.start();

		this.working = true;
	}

	public synchronized void stop() throws IOException {
		if(!working) {
			return;
		}
		this.fileListener.interrupt();
		this.fileEventManager.interrupt();
		this.outputEventDispatcher.interrupt();

		boolean finished = false;
		while(!finished) {
			try {
				this.fileListener.join();
				this.fileEventManager.join();
				this.outputEventDispatcher.join();
				finished = true;
			} catch (InterruptedException e) {
				finished = false;
			}
		}
		watcher.close();
		working = false;
	}

}


