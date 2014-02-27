package au.id.villar.cloudfs.file;

import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

class FileChangeEventData {

	private final WatchKey watchKey;
	private Path changedChild;

	FileChangeEventData(WatchKey watchKey, Path changedChild) {
		this.watchKey = watchKey;
		this.changedChild = changedChild;
	}

	public WatchKey getWatchKey() {
		return watchKey;
	}

	public Path getChangedChild() {
		return changedChild;
	}
}
