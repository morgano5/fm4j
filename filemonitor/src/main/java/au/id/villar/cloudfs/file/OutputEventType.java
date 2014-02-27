package au.id.villar.cloudfs.file;

public enum OutputEventType {

	NODE_ADDED,
	NODE_MODIFIED,
	NODE_DELETED,
	NODE_REGISTERED,

	START_LISTENING_FILES,
	STOP_LISTENING_FILES,

	ERROR
}
