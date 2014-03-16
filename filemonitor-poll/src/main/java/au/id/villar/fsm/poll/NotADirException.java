package au.id.villar.fsm.poll;

/**
 * Exception thrown if the reference is not a directory. This could happen, for instance, if a normal file is tried to
 * be open as a directory.
 */
@SuppressWarnings("unused")
public class NotADirException extends LinuxNativeErrorException {

	public NotADirException(String message, int errorCode) {
		super(message, errorCode);
	}
}
