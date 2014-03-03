package au.id.villar.fsm.poll;

@SuppressWarnings("unused")
public class PermissionDeniedException extends LinuxNativeErrorException {

	public PermissionDeniedException(String message, int errorCode) {
		super(message, errorCode);
	}

}
