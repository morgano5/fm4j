package au.id.villar.fsm.linux;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

public class LinuxNativeAdapterTest {

	@Test
	public void basicTest() throws InterruptedException, IOException {

		Path testingDir;

		class Event {
			String name;
			int wd;
			List<InotifyMessage> events;
			int cookie;
		}

		class Result {
			LinuxNativeErrorException exception = null;
			boolean closed = false;
			List<Event> events = new ArrayList<>();
		}

		final Result result = new Result();

		InotifyChangeListener listener = new InotifyChangeListener() {

			@Override
			public void processEvent(String name, int wd, int mask, int cookie) {
				Event event = new Event();
				event.name = name;
				event.wd = wd;
				event.events = InotifyMessage.containedEvents(mask);
				event.cookie = cookie;
				result.events.add(event);
			}

			@Override
			public void processFatalException(LinuxNativeErrorException e) {
				result.exception = e;
			}

			@Override
			public void processClose() {
				result.closed = true;
			}

		};

		try {
			testingDir = Files.createTempDirectory(LinuxNativeAdapterTest.class.getCanonicalName() + "_inotify");
			Files.createFile(testingDir.resolve("test1"));
			Files.createFile(testingDir.resolve("test2"));
			Files.createFile(testingDir.resolve("test3"));
			Files.createFile(testingDir.resolve("test4"));
		} catch (IOException e) {
			assumeTrue("Testing files couldn't be created", false);
			return;
		}


		int wd;
		try(LinuxNativeAdapter adapter = new LinuxNativeAdapter()) {
			assertTrue(adapter.getFd() > 0);
			wd = adapter.register(testingDir, InotifyMessage.CREATE,
						InotifyMessage.DELETE,
						InotifyMessage.MOVED_FROM,
						InotifyMessage.MOVED_TO,
						InotifyMessage.MODIFY);

			Thread.sleep(100);
			Files.delete(testingDir.resolve("test4"));
			Thread.sleep(2000);
			adapter.addListener(listener);
			Thread.sleep(100);
			Files.delete(testingDir.resolve("test3"));
			Thread.sleep(100);
			Files.createFile(testingDir.resolve("test4"));
			Thread.sleep(100);
			Files.move(testingDir.resolve("test1"), testingDir.resolve("test8"));
			Thread.sleep(100);
			adapter.removeListener(listener);
			Thread.sleep(100);
			Files.delete(testingDir.resolve("test2"));
			Thread.sleep(2000);
			adapter.addListener(listener);
			Thread.sleep(100);
			adapter.unregister(wd);
			Thread.sleep(100);
			Files.delete(testingDir.resolve("test4"));
			Files.delete(testingDir.resolve("test8"));
			Files.delete(testingDir);
		}

		assertEquals("there must be 5 events", 5, result.events.size());

		assertEquals(wd, result.events.get(0).wd);
		assertEquals(wd, result.events.get(1).wd);
		assertEquals(wd, result.events.get(2).wd);
		assertEquals(wd, result.events.get(3).wd);
		assertEquals(wd, result.events.get(4).wd);

		assertEquals(0, result.events.get(0).cookie);
		assertEquals(0, result.events.get(1).cookie);
		assertEquals(result.events.get(3).cookie, result.events.get(2).cookie);
		assertEquals(result.events.get(2).cookie, result.events.get(3).cookie);
		assertEquals(0, result.events.get(4).cookie);

		assertEquals("test3", result.events.get(0).name);
		assertEquals("test4", result.events.get(1).name);
		assertEquals("test1", result.events.get(2).name);
		assertEquals("test8", result.events.get(3).name);
		assertEquals("", result.events.get(4).name);

		assertTrue("Inotify fd must be closed", result.closed);
		assertNull("No exception must have been thrown", result.exception);

	}

	public static void main(String[] args) throws InterruptedException {
		try (LinuxNativeAdapter adapter = new LinuxNativeAdapter()) {
			adapter.addListener(new InotifyChangeListener() {

				@Override
				public void processEvent(String name, int wd, int mask, int cookie) {
					System.out.println("EVENT -- [" + name + "] -- " + wd + " -- "
							+ InotifyMessage.containedEvents(mask) + " -- " + cookie);
				}

				@Override
				public void processFatalException(LinuxNativeErrorException e) {
					System.out.println("ERROR -- " + e);
				}

				@Override
				public void processClose() {
					System.out.println("CLOSE");
				}

			});
			adapter.register(Paths.get("/home/rafael/Desktop/testingDir/testingRoot"), InotifyMessage.MOVE_SELF);
			Thread.sleep(60000);
		}
	}
}
