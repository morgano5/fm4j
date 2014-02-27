package au.id.villar.cloudfs.file;

import java.nio.file.Path;
import java.nio.file.WatchKey;

class RootMissingMonitorDirInfo extends FileInfo {

	private final WatchKey relatedKey;

	RootMissingMonitorDirInfo(RootInfo rootInfo, Path path, WatchKey relatedKey) {
		super(rootInfo, path);
		this.relatedKey = relatedKey;
	}

	public WatchKey getRelatedKey() {
		return relatedKey;
	}
}
