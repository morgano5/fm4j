package au.id.villar.cloudfs.file;

import java.nio.file.Path;

/**
 * Programmer: Rafael Villar Villar
 * Date: 11/12/13
 * Time: 1:50 PM
 */
public class FileInfo {

	private final RootInfo rootInfo;

	private final Path path;

	public FileInfo(RootInfo rootInfo, Path path) {
		this.rootInfo = rootInfo;
		this.path = path;
	}

	public RootInfo getRootInfo() {
		return rootInfo;
	}

	public Path getPath() {
		return path;
	}
}
