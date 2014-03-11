#include <dirent.h>
#include <errno.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include "au_id_villar_fsm_poll_FileTree.h"

static void handle_error(JNIEnv *env, int error_code) {

	char *classname;

	switch(error_code) {
	case EACCES:  classname = "au/id/villar/fsm/poll/PermissionDeniedException"; break;
	case ENOENT:  classname = "au/id/villar/fsm/poll/NoSuchFileOrDirectoryException"; break;
	case ENOTDIR: classname = "au/id/villar/fsm/poll/NotADirException"; break;
	case EINVAL:  classname = "au/id/villar/fsm/poll/NotALinkException"; break;
	default: classname = "au/id/villar/fsm/poll/LinuxNativeErrorException";
	}

	char *description = strerror(error_code);
	jclass clazz = (*env)->FindClass(env, classname);
	jmethodID initMethod = (*env)->GetMethodID(env, clazz, "<init>", "(Ljava/lang/String;I)V");
	jstring message = (*env)->NewStringUTF(env, description);
	jobject exceptionObj = (*env)->NewObject(env, clazz, initMethod, message, error_code);
	(*env)->Throw(env, (jthrowable)exceptionObj);
}

JNIEXPORT jobject JNICALL Java_au_id_villar_fsm_poll_FileTree_readDir
		(JNIEnv *env, jobject object, jstring path) {

	jclass clazz = (*env)->FindClass(env, "java/util/ArrayList");
	jmethodID initMethod = (*env)->GetMethodID(env, clazz, "<init>", "(I)V");
	jmethodID addMethod = (*env)->GetMethodID(env, clazz, "add", "(Ljava/lang/Object;)Z");
	jobject list = (*env)->NewObject(env, clazz, initMethod, 10);

	struct dirent *entry;

	const char *n_path = (*env)->GetStringUTFChars(env, path, 0);

	DIR *dir = opendir(n_path);
	if(dir == NULL) {
		handle_error(env, errno);
		return NULL;
	}

	(*env)->ReleaseStringUTFChars(env, path, n_path);

	while((errno = 0, entry = readdir(dir)) != NULL) {
		if(!strcmp(entry->d_name, ".") || !strcmp(entry->d_name, "..")) {
			continue;
		}
		jstring name = (*env)->NewStringUTF(env, entry->d_name);
		(*env)->CallBooleanMethod(env, list, addMethod, name);
	}
	if(errno != 0) {
		handle_error(env, errno);
		return NULL;
	}

	closedir(dir);
	return list;
}

JNIEXPORT jstring JNICALL Java_au_id_villar_fsm_poll_FileTree_readlink
		(JNIEnv *env, jobject object, jstring path, jint link_size) {

	int buf_increment = 256;
	char *buffer = NULL;
	ssize_t result = -1;
	int done = 0;
	jstring link = NULL;

	if(link_size <= 0) {
		link_size = buf_increment;
	}

	const char *n_path = (*env)->GetStringUTFChars(env, path, 0);

	while(!done) {

		buffer = malloc(link_size + 1);
		if(buffer == NULL) {
			handle_error(env, errno);
			return NULL;
		}

		result = readlink(n_path, buffer, link_size);
		done = 1;

		if(result > 0) {
			buffer[result] = 0;
			link = (*env)->NewStringUTF(env, buffer);
		} else if(result == -1) {
			if(errno == ENAMETOOLONG) {
				errno = 0;
				done = 0;
				link_size += buf_increment;
			} else {
				handle_error(env, errno);
			}
		}

		free(buffer);
	}

	(*env)->ReleaseStringUTFChars(env, path, n_path);

	return link;
}


JNIEXPORT jobject JNICALL Java_au_id_villar_fsm_poll_FileTree_getInfo
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
