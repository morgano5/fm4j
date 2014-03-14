package au.id.villar.fsm.poll;

/**
 *
 */
@SuppressWarnings("unused")
public class NotADirException extends LinuxNativeErrorException {

	public NotADirException(String message, int errorCode) {
		super(message, errorCode);
	}
}
