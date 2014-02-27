package au.id.villar.fsm.poll;

public class LinuxNativeErrorException extends RuntimeException {
	private final int errorCode;

	public LinuxNativeErrorException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
