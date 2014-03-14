package au.id.villar.fsm.poll;

/**
 *
 */
@SuppressWarnings("unused")
public class NotALinkException extends LinuxNativeErrorException {

	public NotALinkException(String message, int errorCode) {
		super(message, errorCode);
	}
}
