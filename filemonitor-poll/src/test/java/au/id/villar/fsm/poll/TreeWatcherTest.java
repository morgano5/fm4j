package au.id.villar.fsm.poll;

import java.nio.file.Paths;

public class TreeWatcherTest {

	public static void main(String[] args) throws InterruptedException {

//		TreeWatcher watcher = new TreeWatcher(Paths.get("/home/villarr/Desktop/testingDir"));

//		GregorianCalendar g = new GregorianCalendar();
//		g.setTimeInMillis(new TreeWatcher(null).getInfo("/home/rafael/Desktop/testingDir").getLastModification() * 1000);
//		System.out.format(">>> %04d-%02d-%02d %02d:%02d:%02d%n", g.get(Calendar.YEAR), g.get(Calendar.MONTH), g.get(Calendar.DAY_OF_MONTH),
//				g.get(Calendar.HOUR_OF_DAY), g.get(Calendar.MINUTE), g.get(Calendar.SECOND));

//		System.out.println(">>> " + new TreeWatcher(null).getInfo(/*"/home/rafael/Desktop/testingDir"*/ "/dev/sda1"));
//		System.out.println(">>> " + new TreeWatcher().readLink("/home/villarr/.wine/dosdevices/c:") + " <<<");


		long start = System.currentTimeMillis();
		System.out.println("Working...");
		TreeWatcher watcher = new TreeWatcher(Paths.get(/**/"/home/rafael"/*/"../../.."/**/), true, "/home/rafael/\\..*");
		watcher.addListener(new TreeListener() {
			@Override
			public void fileChanged(FileEvent event) {
				if (event.getType() == EventType.FILE_MOVED) {
					System.out.format(">> %s -- %s   -->   %s%n", event.getType().toString(), event.getOldPath(), event.getPath());
				} else {
					System.out.format(">> %s -- %s%n", event.getType().toString(), event.getPath());
				}

			}
		});
		System.out.println("Started");
		watcher.start();

		while(true) { Thread.sleep(1000); }

//		System.gc();
//		Thread.sleep(2000);
//
//		System.out.format("MAX:   %12d%nTOTAL: %12d%nFREE:  %12d%nUSED:  %12d%n%nMilliseconds: %d%n",
//				Runtime.getRuntime().maxMemory(),
//				Runtime.getRuntime().totalMemory(),
//				Runtime.getRuntime().freeMemory(),
//				Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(),
//				System.currentTimeMillis() - start);
//
//		Thread.sleep(1000);

//		TreeWatcher watcher = new TreeWatcher(Paths.get(/*"/home/villarr"*/"../../.."));
//		System.out.println(watcher.readLink("/home/rafael/Desktop/m/link"));

	}
}
