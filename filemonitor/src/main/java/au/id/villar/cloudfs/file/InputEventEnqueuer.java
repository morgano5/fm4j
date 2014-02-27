package au.id.villar.cloudfs.file;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.BlockingQueue;

import static java.nio.file.StandardWatchEventKinds.*;

class InputEventEnqueuer extends Thread {

	private final BlockingQueue<InputEvent> inputEvents;
	private final WatchService watcher;

	InputEventEnqueuer(BlockingQueue<InputEvent> inputEvents, WatchService watcher) {
		super("Input event enqueuer");
		super.setDaemon(true);
		this.inputEvents = inputEvents;
		this.watcher = watcher;
	}

	@Override
	public void run() {
		try {
			putInputEvent(InputEventType.START_LISTENING_FILES, null);
			while(!isInterrupted()) {
				WatchKey key;
				key = watcher.take();
				for(WatchEvent event: key.pollEvents()) {
					WatchEvent.Kind kind = event.kind();
					Object context = event.context();
					Path dirChild = context instanceof Path? (Path)context: null;
					if(kind == ENTRY_CREATE) {
						putInputEvent(InputEventType.NODE_ADDED, new FileChangeEventData(key, dirChild));
					} else if(kind == ENTRY_MODIFY) {
						putInputEvent(InputEventType.NODE_MODIFIED, new FileChangeEventData(key, dirChild));
					} else if(kind == ENTRY_DELETE) {
						putInputEvent(InputEventType.NODE_DELETED, new FileChangeEventData(key, dirChild));
					} else if (kind == OVERFLOW) {
						putInputEvent(InputEventType.OVERFLOW, key);
					}
				}
				if(!key.reset()) {
					putInputEvent(InputEventType.CANCELLED_KEY, key);
				}
			}
		} catch (InterruptedException ignore) {
		} finally {
			putInputEvent(InputEventType.STOP_LISTENING_FILES, null);
		}
	}

	private void putInputEvent(InputEventType eventType, Object data) {
		boolean successful = false;
		while(!successful) {
			try {
				inputEvents.put(new InputEvent(eventType, data));
				successful = true;
			} catch (InterruptedException e) {
				sleepABit();
			}
		}
	}

	private void sleepABit() {
		try {Thread.sleep(1000);} catch (InterruptedException ignore) {}
	}
}
