package au.id.villar.fsm.poll;

/**
 * Exception thrown if a reference is tried to be read as a symbolic link and it is not.
 */
@SuppressWarnings("unused")
public class NotALinkException extends LinuxNativeErrorException {

	public NotALinkException(String message, int errorCode) {
		super(message, errorCode);
	}
}
