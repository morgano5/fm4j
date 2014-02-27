#include <errno.h>
#include <string.h>
#include <sys/inotify.h>
#include <sys/select.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>
#include "au_id_villar_fsm_linux_LinuxNativeAdapter.h"

#define EVENT_BUFFER_SIZE 2048
#define TIMEOUT_SECONDS 1

static void reset_select_params(int fd, struct timeval *tv, fd_set *rfds) {
	FD_ZERO(rfds);
	FD_SET(fd, rfds);
	tv->tv_sec = TIMEOUT_SECONDS;
	tv->tv_usec = 0;
}

static void handle_error(JNIEnv *env, int codeError) {
	char *description = strerror(codeError);
	jclass clazz = (*env)->FindClass(env, "au/id/villar/fsm/linux/LinuxNativeErrorException");
	jmethodID initMethod = (*env)->GetMethodID(env, clazz, "<init>", "(Ljava/lang/String;I)V");
	jstring message = (*env)->NewStringUTF(env, description);
	jobject exceptionObj = (*env)->NewObject(env, clazz, initMethod, message, codeError, description);
	(*env)->Throw(env, (jthrowable)exceptionObj);
}


JNIEXPORT jint JNICALL Java_au_id_villar_fsm_linux_LinuxNativeAdapter_nativeInit(JNIEnv *env,
		jobject object) {

	int fd = inotify_init();

	if(fd == -1) {
		handle_error(env, errno);
	}

	return fd;
}

JNIEXPORT jint JNICALL Java_au_id_villar_fsm_linux_LinuxNativeAdapter_addWatch(JNIEnv *env,
		jobject object, jint fd, jstring path, jint flags) {

	const char *n_path = (*env)->GetStringUTFChars(env, path, 0);
	int wd = inotify_add_watch(fd, n_path, flags);
	(*env)->ReleaseStringUTFChars(env, path, n_path);

	if(wd == -1) {
		handle_error(env, errno);
	}

	return wd;
}

JNIEXPORT void JNICALL Java_au_id_villar_fsm_linux_LinuxNativeAdapter_removeWatch(JNIEnv *env,
		jobject object, jint fd, jint wd) {

	int error = inotify_rm_watch(fd, wd);
	if(error) {
		handle_error(env, errno);
	}
}

JNIEXPORT void JNICALL Java_au_id_villar_fsm_linux_LinuxNativeAdapter_listenForEvents(JNIEnv *env,
		jobject object, jint fd) {

	char buffer[EVENT_BUFFER_SIZE];
	struct inotify_event *event;

	jclass clazz = (*env)->GetObjectClass(env, object);
	jmethodID callback = (*env)->GetMethodID(env, clazz, "callbackProcessEvent", "(Ljava/lang/String;III)I");
	jmethodID checkCallback = (*env)->GetMethodID(env, clazz, "callbackStatusCheck", "()I");

	int is_empty;
	char emptyChars[] = {0};
	jstring emptyNode = (*env)->NewStringUTF(env, emptyChars);
	jstring node;

	fd_set rfds;
	struct timeval tv;
	int sel_errorcode;
	int sel_nfds = fd + 1;

	reset_select_params(fd, &tv, &rfds);

	int finished = 0;

	// loop waiting for events
	while(!finished) {

		// wait for events to come or timeout, whatever comes first
		sel_errorcode = select(sel_nfds, &rfds, NULL, NULL, &tv);
		if (sel_errorcode == -1) {
			handle_error(env, errno);
			close(fd);
			return;
		}

		finished = (*env)->CallIntMethod(env, object, checkCallback);

		if (!sel_errorcode) {
			// timeout, ask if the loop must finish and start over again
			reset_select_params(fd, &tv, &rfds);
			continue;
		}

		// Event(s) available, read them
		int len = read(fd, buffer, EVENT_BUFFER_SIZE);
		if(len == -1) {
			handle_error(env, errno);
			close(fd);
			return;
		}

		// Read and report incoming events
		event = (void *)buffer;
		while(((void *)event) < ((void *)buffer) + len) {

			if (event->len) {
				node = (*env)->NewStringUTF(env, event->name);
				is_empty = 0;
			} else {
				node = emptyNode;
				is_empty = -1;
			}

			finished = (*env)->CallIntMethod(env, object, callback, node, event->wd,
					event->mask, event->cookie);
			if(!is_empty) {
				(*env)->DeleteLocalRef(env, node);
			}

			event = ((void *)event) + sizeof(struct inotify_event) + event->len;
		}

	}

	int error = close(fd);
	if(error) {
		handle_error(env, errno);
	}

}
