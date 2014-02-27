package au.id.villar.fsm;

public enum ChangeType {

	NODE_ADDED(0x1),
	NODE_DELETED(0x2),
	NODE_MODIFIED(0x4),
	NODE_MOVED(0x8);

	public static final ChangeType[] ALL_CHANGES = ChangeType.values();

	private int flag;

	ChangeType(int flag) {
		this.flag = flag;
	}

	public int getFlag() {
		return flag;
	}

}
