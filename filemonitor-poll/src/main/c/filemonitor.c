#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include "au_id_villar_fsm_poll_TreeWatcher.h"

static void handle_error(JNIEnv *env, int codeError) {
	char *description = strerror(codeError);
	jclass clazz = (*env)->FindClass(env, "au/id/villar/fsm/poll/LinuxNativeErrorException");
	jmethodID initMethod = (*env)->GetMethodID(env, clazz, "<init>", "(Ljava/lang/String;I)V");
	jstring message = (*env)->NewStringUTF(env, description);
	jobject exceptionObj = (*env)->NewObject(env, clazz, initMethod, message, codeError);
	(*env)->Throw(env, (jthrowable)exceptionObj);
}

JNIEXPORT jobject JNICALL Java_au_id_villar_fsm_poll_TreeWatcher_getInfo
		(JNIEnv *env, jobject object, jstring path, jboolean follow_links) {

	struct stat buffer;

	const char *n_path = (*env)->GetStringUTFChars(env, path, 0);
	int error = follow_links? stat(n_path, &buffer): lstat(n_path, &buffer);
	int err = errno;
	(*env)->ReleaseStringUTFChars(env, path, n_path);
	if(error) {
		handle_error(env, err);
		return NULL;
	}










	jclass clazz = (*env)->FindClass(env, "au/id/villar/fsm/poll/PathInfo");
	jmethodID initMethod = (*env)->GetMethodID(env, clazz, "<init>", "()V");
	jobject node = (*env)->NewObject(env, clazz, initMethod);

	jfieldID containing_device_id = (*env)->GetFieldID(env, clazz, "containingDeviceId", "I");
	jfieldID inode = (*env)->GetFieldID(env, clazz, "inode", "J");
	jfieldID mode = (*env)->GetFieldID(env, clazz, "mode", "I");
	jfieldID hard_links_number = (*env)->GetFieldID(env, clazz, "hardLinksNumber", "I");
	jfieldID user_id = (*env)->GetFieldID(env, clazz, "userId", "I");
	jfieldID group_id = (*env)->GetFieldID(env, clazz, "groupId", "I");
	jfieldID device_id = (*env)->GetFieldID(env, clazz, "deviceId", "I");
	jfieldID size = (*env)->GetFieldID(env, clazz, "size", "J");
	jfieldID block_size = (*env)->GetFieldID(env, clazz, "blockSize", "I");
	jfieldID number_of_blocks = (*env)->GetFieldID(env, clazz, "numberOfBlocks", "I");
	jfieldID last_access = (*env)->GetFieldID(env, clazz, "lastAccess", "J");
	jfieldID last_modification = (*env)->GetFieldID(env, clazz, "lastModification", "J");
	jfieldID last_status_change = (*env)->GetFieldID(env, clazz, "lastStatusChange", "J");
	jfieldID type = (*env)->GetFieldID(env, clazz, "type", "C");
	jfieldID major_containing = (*env)->GetFieldID(env, clazz, "majorContaining", "I");
	jfieldID minor_containing = (*env)->GetFieldID(env, clazz, "minorContaining", "I");
	jfieldID major = (*env)->GetFieldID(env, clazz, "major", "I");
	jfieldID minor = (*env)->GetFieldID(env, clazz, "minor", "I");

	char raw_type = 0;
	if(S_ISREG(buffer.st_mode)) raw_type = 'F';
	if(S_ISDIR(buffer.st_mode)) raw_type = 'D';
	if(S_ISCHR(buffer.st_mode)) raw_type = 'C';
	if(S_ISBLK(buffer.st_mode)) raw_type = 'B';
	if(S_ISFIFO(buffer.st_mode)) raw_type = 'P';
	if(S_ISLNK(buffer.st_mode)) raw_type = 'L';
	if(S_ISSOCK(buffer.st_mode)) raw_type = 'S';

	(*env)->SetIntField(env, node, containing_device_id, buffer.st_dev);
	(*env)->SetLongField(env, node, inode, buffer.st_ino);
	(*env)->SetIntField(env, node, mode, buffer.st_mode);
	(*env)->SetIntField(env, node, hard_links_number, buffer.st_nlink);
	(*env)->SetIntField(env, node, user_id, buffer.st_uid);
	(*env)->SetIntField(env, node, group_id, buffer.st_gid);
	(*env)->SetIntField(env, node, device_id, buffer.st_rdev);
	(*env)->SetLongField(env, node, size, buffer.st_size);
	(*env)->SetIntField(env, node, block_size, buffer.st_blksize);
	(*env)->SetIntField(env, node, number_of_blocks, buffer.st_blocks);
	(*env)->SetLongField(env, node, last_access, buffer.st_atime);
	(*env)->SetLongField(env, node, last_modification, buffer.st_mtime);
	(*env)->SetLongField(env, node, last_status_change, buffer.st_ctime);
	(*env)->SetCharField(env, node, type, raw_type);
	(*env)->SetIntField(env, node, major_containing, major(buffer.st_dev));
	(*env)->SetIntField(env, node, minor_containing, minor(buffer.st_dev));
	(*env)->SetIntField(env, node, major, major(buffer.st_rdev));
	(*env)->SetIntField(env, node, minor, minor(buffer.st_rdev));

	return node;
}
