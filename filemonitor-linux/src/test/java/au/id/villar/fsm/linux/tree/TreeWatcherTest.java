package au.id.villar.fsm.linux.tree;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class TreeWatcherTest {

//	private static final String TESTING_DIR = /**/"/home/villarr/Desktop/testingDir"/*/"/home/rafael/Desktop/testingDir"/**/;
private static final String TESTING_DIR = "/home/villarr";

	public static void main(String[] args) throws Exception {
		TreeWatcher watcher = new TreeWatcher(Paths.get(TESTING_DIR), true);

		watcher.addListener(new ChangeListener() {
			@Override
			public void fileAdded(Path path) {
				System.out.println("[ ADD  ] " + path);
			}

			@Override
			public void fileDeleted(Path path) {
				System.out.println("[DELETE] " + path);
			}

			@Override
			public void fileModified(Path path) {
				System.out.println("[MODIFY] " + path);
			}

			@Override
			public void fileMoved(Path oldPath, Path newPath) {
				System.out.println("[MOVED FROM] " + oldPath);
				System.out.println("[MOVED TO  ] " + newPath);
			}

			@Override
			public void exception(Exception e) {
				System.out.println("[EXCEPTION] " + e);
			}

		});


		Scanner scanner = new Scanner(System.in);
		scanner.next(".*");
		System.out.println("[DONE]");

		watcher.close();
	}

}
