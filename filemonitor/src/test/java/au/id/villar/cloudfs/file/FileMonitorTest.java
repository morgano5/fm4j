package au.id.villar.cloudfs.file;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Programmer: Rafael Villar Villar
 * Date: 13/12/13
 * Time: 3:01 PM
 */
public class FileMonitorTest {

	@Test
	public void basicTest() throws IOException {


	}


	private File createTempDir() throws IOException {
		File temp = File.createTempFile("au_id_villar_cloudfs_file_testing_", "");
		if(temp.delete() && temp.mkdir()) {
			return temp;
		} else {
			return null;
		}
	}
}
