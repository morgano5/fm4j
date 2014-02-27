package au.id.villar.cloudfs.file;

class InputEvent {

	private InputEventType eventType;
	private Object data;

	InputEvent(InputEventType eventType, Object data) {
		this.eventType = eventType;
		this.data = data;
	}

	public InputEventType getEventType() {
		return eventType;
	}

	public Object getData() {
		return data;
	}
}
