package au.id.villar.fsm.poll;

import java.nio.file.Path;

public class FileEvent {

	private final EventType type;
	private final Path path;
	private final Path oldPath;

	public FileEvent(EventType type, Path path, Path oldPath) {
		this.type = type;
		this.path = path;
		this.oldPath = oldPath;
	}

	public EventType getType() {
		return type;
	}

	public Path getPath() {
		return path;
	}

	public Path getOldPath() {
		return oldPath;
	}

}
