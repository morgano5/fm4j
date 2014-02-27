package au.id.villar.cloudfs.file;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

class OutputEventDispatcher extends Thread {

	private final List<FileListener> listeners;
	private final BlockingQueue<OutputEvent> outputEvents;

	OutputEventDispatcher(BlockingQueue<OutputEvent> outputEvents) {
		super("Output event dispatcher");
		super.setDaemon(true);
		this.outputEvents = outputEvents;
		this.listeners = new ArrayList<>();
	}

	@Override
	public void run() {
		try {
			while(!isInterrupted()) {
				OutputEvent event = outputEvents.take();
				synchronized (listeners) {
					switch(event.getEventType()) {
						case NODE_ADDED:
							System.out.println("ADDED      ---- " + event.getData());// todo
							for(FileListener listener: listeners) listener.newFile(event.getData());
							break;
						case NODE_MODIFIED:
							System.out.println("EDITED     ---- " + event.getData());// todo
							for(FileListener listener: listeners) listener.updatedFile(event.getData());
							break;
						case NODE_DELETED:
							System.out.println("DELETED    ---- " + event.getData());// todo
							for(FileListener listener: listeners) listener.deletedFile(event.getData());
							break;
						case NODE_REGISTERED:
							System.out.println("REGISTERED ---- " + event.getData());// todo
							break;
						case START_LISTENING_FILES:
							System.out.println("START"); // todo
							break;
						case STOP_LISTENING_FILES:
							System.out.println("STOP"); // todo
							break;
						case ERROR:
							System.out.println("ERROR      ---- " + event.getData()); // TODO
							break;
						default:
							throw new RuntimeException("Unexpected event type: " + event.getEventType());
					}
				}
			}
		} catch (InterruptedException ignore) {
		}
	}

	void addListener(FileListener listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}

	void removeListener(FileListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
}
