package au.id.villar.fsm.linux;

import java.util.List;

public interface InotifyChangeListener {

	void processEvent(String name, int wd, int mask, int cookie);

	void processFatalException(LinuxNativeErrorException e);

	void processClose();
}
