package au.id.villar.cloudfs.file;

import java.nio.file.Path;

class OutputEvent {

	private OutputEventType eventType;
	private Path path;

	OutputEvent(OutputEventType eventType, Path path) {
		this.eventType = eventType;
		this.path = path;
	}

	public OutputEventType getEventType() {
		return eventType;
	}

	public Path getData() {
		return path;
	}
}
