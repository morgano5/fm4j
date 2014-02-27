package au.id.villar.fsm.poll;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Queue;

public class TreeWatcher {

	/**
	 * Specifies if the native library was successfully loaded
	 */
	public static final boolean LOADED;

	/**
	 * If the native library wasn't loaded, then this is the Exception thrown, otherwise it is null.
	 */
	public static final IOException LOAD_EXCEPTION;

	private static final String SHARED_NATIVE_LIB_RESOURCE = "filemonitor.so";

	static {

		boolean loaded = true;
		IOException exception = null;

		try {
			int size;
			byte[] buffer = new byte[2048];

			Path tempFile = Files.createTempFile(TreeWatcher.class.getCanonicalName() + "_", ".so");
			try(InputStream input = ClassLoader.getSystemResourceAsStream(SHARED_NATIVE_LIB_RESOURCE)) {
				try(OutputStream output = Files.newOutputStream(tempFile)) {
					while((size = input.read(buffer)) != -1) {
						output.write(buffer, 0, size);
					}
				}
			}
			System.load(tempFile.toString());
		} catch (IOException e) {
			loaded = false;
			exception = e;
		} finally {
			LOADED = loaded;
			LOAD_EXCEPTION = exception;
		}

	}




	private final Path rootDir;

	public TreeWatcher(Path rootDir) {
		this.rootDir = rootDir;
		Queue<Path> queue = new LinkedList<>();
	}


	private void registerDir() {}

	private native Node getInfo(String path);


	public static void main(String[] args) {

//		TreeWatcher watcher = new TreeWatcher(Paths.get("/home/villarr/Desktop/testingDir"));

//		GregorianCalendar g = new GregorianCalendar();
//		g.setTimeInMillis(new TreeWatcher(null).getInfo("/home/rafael/Desktop/testingDir").getLastModification() * 1000);
//		System.out.format(">>> %04d-%02d-%02d %02d:%02d:%02d%n", g.get(Calendar.YEAR), g.get(Calendar.MONTH), g.get(Calendar.DAY_OF_MONTH),
//				g.get(Calendar.HOUR_OF_DAY), g.get(Calendar.MINUTE), g.get(Calendar.SECOND));

		System.out.println(">>> " + new TreeWatcher(null).getInfo(/*"/home/rafael/Desktop/testingDir"*/ "/dev/sda1"));

	}



}
