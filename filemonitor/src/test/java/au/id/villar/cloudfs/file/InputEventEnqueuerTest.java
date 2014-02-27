package au.id.villar.cloudfs.file;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class InputEventEnqueuerTest {

	private static final String TEST_DIR_NAME = "test";

	@Before
	public void cleanUp() throws IOException {
		deleteTestingDir();
		Files.createDirectory(getTestDir());
	}

//	@Test
	public void basicTest() throws IOException, InterruptedException {
		Path testDir = getTestDir();
		BlockingQueue<InputEvent> inputEvents = new LinkedBlockingQueue<>(10);
		try(WatchService watcher = testDir.getFileSystem().newWatchService()) {
			InputEventEnqueuer enqueuer = new InputEventEnqueuer(inputEvents, watcher);
			enqueuer.start();
			Thread.sleep(100);
			testDir.register(watcher, ENTRY_DELETE, ENTRY_MODIFY, ENTRY_CREATE);
			Thread.sleep(100);
			Path file = testDir.resolve("one");
			Files.createFile(file);
			Thread.sleep(100);
			Files.write(file, "Hola mundo\n".getBytes());
			Thread.sleep(100);
			Files.delete(file);
			Thread.sleep(100);
			enqueuer.interrupt();
			enqueuer.join();
		}
		assertTrue(inputEvents.size() >= 7);
		assertEquals(InputEventType.START_LISTENING_FILES, inputEvents.take().getEventType());
		assertEquals(InputEventType.NODE_ADDED, inputEvents.take().getEventType());
		while (inputEvents.size() >= 3) {
			assertEquals(InputEventType.NODE_MODIFIED, inputEvents.take().getEventType());
		}
		assertEquals(InputEventType.NODE_DELETED, inputEvents.take().getEventType());
		assertEquals(InputEventType.STOP_LISTENING_FILES, inputEvents.take().getEventType());

	}

	@After
	public void deleteTestingDir() throws IOException {
		Path testDir = getTestDir();
		if(Files.isDirectory(testDir)) {
			Files.walkFileTree(testDir, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

			});
			Files.delete(testDir);
		} else {
			Files.deleteIfExists(testDir);
		}
	}

	private Path getTestDir() {
		Path currentDir = Paths.get(".").toAbsolutePath().normalize();
		return currentDir.resolve(TEST_DIR_NAME);
	}
}
