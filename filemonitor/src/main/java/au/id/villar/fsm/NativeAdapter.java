package au.id.villar.fsm;

import java.io.Closeable;
import java.nio.file.Path;

public interface NativeAdapter extends Closeable {

	void init();

	void registerDir(Path path, ChangeListener listener, boolean recursive, ChangeType... changes);

	void unregisterDir(Path dir);

	void close();
}
