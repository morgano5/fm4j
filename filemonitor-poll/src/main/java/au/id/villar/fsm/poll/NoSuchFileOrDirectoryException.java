package au.id.villar.fsm.poll;

/**
 *
 */
@SuppressWarnings("unused")
public class NoSuchFileOrDirectoryException extends LinuxNativeErrorException {

	public NoSuchFileOrDirectoryException(String message, int errorCode) {
		super(message, errorCode);
	}
}
