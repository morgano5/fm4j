package au.id.villar.cloudfs.file;

import java.nio.file.Path;

public interface FileListener {

	void newFile(Path fileInfo);

	void deletedFile(Path fileInfo);

	void updatedFile(Path fileInfo);

}
