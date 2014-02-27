package au.id.villar.cloudfs.file;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.concurrent.LinkedBlockingQueue;

public class FileEventManagerTest {

	public static void main(String[] args) throws IOException {

		Path path = Paths.get("/home/rafael/Desktop/test");

		try(WatchService watcher = path.getFileSystem().newWatchService()) {
			FileEventManager manager = new FileEventManager(new LinkedBlockingQueue<InputEvent>(), new LinkedBlockingQueue<OutputEvent>(), watcher, path);
		}

	}

}
