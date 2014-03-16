package au.id.villar.fsm.poll;

/**
 * Exception thrown if ther is a reference to an non-existent file or directory.
 */
@SuppressWarnings("unused")
public class NoSuchFileOrDirectoryException extends LinuxNativeErrorException {

	public NoSuchFileOrDirectoryException(String message, int errorCode) {
		super(message, errorCode);
	}
}
