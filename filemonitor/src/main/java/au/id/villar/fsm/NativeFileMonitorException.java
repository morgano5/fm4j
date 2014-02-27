package au.id.villar.fsm;

public class NativeFileMonitorException extends FileMonitorException {

	private int code;

	public NativeFileMonitorException(String message, int code) {
		super(message);
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
