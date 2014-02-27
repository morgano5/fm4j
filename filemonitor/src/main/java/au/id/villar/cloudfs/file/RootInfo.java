package au.id.villar.cloudfs.file;

import java.nio.file.Path;

/**
 * Programmer: Rafael Villar Villar
 * Date: 12/12/13
 * Time: 9:37 AM
 */
public class RootInfo {

	private String id;
	private Path path;

	public RootInfo(String id, Path path) {
		this.id = id;
		this.path = path;
	}

	public String getId() {
		return id;
	}

	public Path getPath() {
		return path;
	}

}
