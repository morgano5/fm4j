package au.id.villar.fsm.linux.tree;

import java.nio.file.Path;

public interface ChangeListener {

	void fileAdded(Path path);

	void fileDeleted(Path path);

	void fileModified(Path path);

	void fileMoved(Path oldPath, Path newPath);

	void exception(Exception e);
}
