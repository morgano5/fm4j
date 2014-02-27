package au.id.villar.fsm.linux;

import java.util.ArrayList;
import java.util.List;

/*

OPEN --> ACCESS --> CLOSE_NOWRITE
OPEN --> MODIFY --> CLOSE_WRITE

[ MOVED_FROM ] --> MOVED_TO
MOVED_FROM --> [ MOVED_TO ]

[ DELETED ] --> IGNORED
UNMOUNT --> IGNORED

ONESHOT --> IGNORED

*/
public enum InotifyMessage {

	/** File was accessed. (read)  */
	ACCESS         (0x00000001),

	/** File was modified. (write) */
	MODIFY         (0x00000002),

	/** Metadata changed.  */
	ATTRIB         (0x00000004),

	/** Writtable file was closed.  */
	CLOSE_WRITE    (0x00000008),

	/** Unwrittable file closed.  */
	CLOSE_NOWRITE  (0x00000010),

	/** Close.  */
	CLOSE          (0x00000018),

	/** File was opened.  */
	OPEN           (0x00000020),

	/** File was moved from X.  */
	MOVED_FROM     (0x00000040),

	/** File was moved to Y.  */
	MOVED_TO       (0x00000080),

	/** Moves.  */
	MOVE           (0x000000C0),

	/** Subfile was created.  */
	CREATE         (0x00000100),

	/** Subfile was deleted.  */
	DELETE         (0x00000200),

	/** Self was deleted.  */
	DELETE_SELF    (0x00000400),

	/** Self was moved.  */
	MOVE_SELF      (0x00000800),

	/** Backing fs was unmounted */
	UNMOUNT        (0x00002000),

	/** Event queued overflowed */
	Q_OVERFLOW     (0x00004000),

	/** File was ignored (watch was removed, either explicitly or because the node was deleted) */
	IGNORED        (0x00008000),

	/** only watch the path if it is a directory (used in "register") */
	ONLYDIR        (0x01000000),

	/** don't follow a sym link (used in "register") */
	DONT_FOLLOW    (0x02000000),

	/** exclude events on unlinked objects (used in "register") */
	EXCL_UNLINK    (0x04000000),

	/** add to the mask of an already existing watch (used in "register") */
	MASK_ADD       (0x20000000),

	/** event occurred against dir */
	ISDIR          (0x40000000),

	/** only send event once (used in "register") */
	ONESHOT        (0x80000000);

	public static final InotifyMessage[] ALL_MESSAGES = InotifyMessage.values();

	public static List<InotifyMessage> containedEvents(int mask) {
		List<InotifyMessage> messages = new ArrayList<>();

		for(InotifyMessage message: InotifyMessage.ALL_MESSAGES) {
			if(message.isInMask(mask)) {
				messages.add(message);
			}
		}
		return messages;
	}

	public static int calculateMask(InotifyMessage... messages) {
		int mask = 0;
		for(InotifyMessage eventType: messages) {
			mask |= eventType.getMask();
		}
		return mask;
	}

	private int mask;

	InotifyMessage(int mask) {
		this.mask = mask;
	}

	public int getMask() {
		return mask;
	}

	public boolean isInMask(int mask) {
		return (this.mask & mask) != 0;
	}
}
