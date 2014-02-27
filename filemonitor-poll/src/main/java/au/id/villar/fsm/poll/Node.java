package au.id.villar.fsm.poll;

@SuppressWarnings("unused")
public class Node {

	private static int S_IFMT    = 0x170000;   // bit mask for the file type bit fields
	private static int S_IFSOCK  = 0x140000;   // socket
	private static int S_IFLNK   = 0x120000;   // symbolic link
	private static int S_IFREG   = 0x100000;   // regular file
	private static int S_IFBLK   = 0x060000;   // block device
	private static int S_IFDIR   = 0x040000;   // directory
	private static int S_IFCHR   = 0x020000;   // character device
	private static int S_IFIFO   = 0x010000;   // FIFO
	private static int S_ISUID   = 0x004000;   // set-user-ID bit
	private static int S_ISGID   = 0x002000;   // set-group-ID bit (see below)
	private static int S_ISVTX   = 0x001000;   // sticky bit (see below)
	private static int S_IRWXU   = 0x000700;   // mask for file owner permissions
	private static int S_IRUSR   = 0x000400;   // owner has read permission
	private static int S_IWUSR   = 0x000200;   // owner has write permission
	private static int S_IXUSR   = 0x000100;   // owner has execute permission
	private static int S_IRWXG   = 0x000070;   // mask for group permissions
	private static int S_IRGRP   = 0x000040;   // group has read permission
	private static int S_IWGRP   = 0x000020;   // group has write permission
	private static int S_IXGRP   = 0x000010;   // group has execute permission
	private static int S_IRWXO   = 0x000007;   // mask for permissions for others (not in group)
	private static int S_IROTH   = 0x000004;   // others have read permission
	private static int S_IWOTH   = 0x000002;   // others have write permission
	private static int S_IXOTH   = 0x000001;   // others have execute permission

//	The  set-group-ID  bit  (S_ISGID) has several special uses.  For a directory it indicates that BSD semantics is to be used for that directory: files
//	created there inherit their group ID from the directory, not from the effective group ID of the creating process, and directories created there will
//	also  get  the  S_ISGID  bit  set.   For  a  file that does not have the group execution bit (S_IXGRP) set, the set-group-ID bit indicates mandatory
//	file/record locking.
//
//	The sticky bit (S_ISVTX) on a directory means that a file in that directory can be renamed or deleted only by the owner of the file, by the owner of
//	the directory, and by a privileged process.




	// IMPORTANT: these fields are modified by the native code, their names shouldn't be changed
	private int containingDeviceId;
	private long inode;
	private int mode;
	private int hardLinksNumber;
	private int userId;
	private int groupId;
	private int deviceId;
	private long size;
	private int blockSize;
	private int numberOfBlocks;
	private long lastAccess;
	private long lastModification;
	private long lastStatusChange;
	private char type;
	private int majorContaining;
	private int minorContaining;
	private int major;
	private int minor;

	public int getContainingDeviceId() {
		return containingDeviceId;
	}

	public long getInode() {
		return inode;
	}

	public int getMode() {
		return mode;
	}

	public int getHardLinksNumber() {
		return hardLinksNumber;
	}

	public int getUserId() {
		return userId;
	}

	public int getGroupId() {
		return groupId;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public long getSize() {
		return size;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public int getNumberOfBlocks() {
		return numberOfBlocks;
	}

	public long getLastAccess() {
		return lastAccess;
	}

	public long getLastModification() {
		return lastModification;
	}

	public long getLastStatusChange() {
		return lastStatusChange;
	}

	@Override
	public String toString() {
		return "Node{" +
				"containingDeviceId=" + containingDeviceId +
				", inode=" + inode +
				", mode=" + mode +
				", hardLinksNumber=" + hardLinksNumber +
				", userId=" + userId +
				", groupId=" + groupId +
				", deviceId=" + Integer.toHexString(deviceId) +
				", size=" + size +
				", blockSize=" + blockSize +
				", numberOfBlocks=" + numberOfBlocks +
				", lastAccess=" + lastAccess +
				", lastModification=" + lastModification +
				", lastStatusChange=" + lastStatusChange +
				", type=" + type +
				", majorContaining=" + majorContaining +
				", minorContaining=" + minorContaining +
				", major=" + major +
				", minor=" + minor +
				'}';
	}
}
