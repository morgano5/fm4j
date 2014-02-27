import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

public class Test {

	public static void main(String[] args) throws JNotifyException, InterruptedException {
		Test test = new Test();
		int watchID1 = test.registerPath("/home/rafael/Desktop/testingDir");
		int watchID2 = test.registerPath("/home/rafael/Desktop/testingDir/testingFolded");

		Thread.sleep(1000000);

		boolean res1 = JNotify.removeWatch(watchID1);
		boolean res2 = JNotify.removeWatch(watchID2);
		if (!res1) {
			// invalid watch ID specified.
		}
		if (!res2) {
			// invalid watch ID specified.
		}
	}

	public int registerPath(String path) throws JNotifyException, InterruptedException {

		int mask = JNotify.FILE_CREATED    |
				JNotify.FILE_DELETED    |
				JNotify.FILE_MODIFIED |
				JNotify.FILE_RENAMED;

		return JNotify.addWatch(path, mask, false, new Listener());
	}

	class Listener implements JNotifyListener {
		public void fileRenamed(int wd, String rootPath, String oldName,
								String newName) {
			print("renamed " + rootPath + " : " + oldName + " -> " + newName);
		}
		public void fileModified(int wd, String rootPath, String name) {
			print("modified " + rootPath + " : " + name);
		}
		public void fileDeleted(int wd, String rootPath, String name) {
			print("deleted " + rootPath + " : " + name);
		}
		public void fileCreated(int wd, String rootPath, String name) {
			print("created " + rootPath + " : " + name);
		}
		void print(String msg) {
			System.out.println(msg);
		}
	}

}

class A { private int getmax() {return 1;} } class B extends A { private Integer getmax() { return null;} }