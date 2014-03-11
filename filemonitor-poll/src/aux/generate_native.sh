#!/bin/bash

function exit_error () {
	echo "ERROR: $1"
	exit -1
}

function makedir () {
	[ -d "$1" ] || {
		mkdir "$1" || exit_error "couldn't create dir $1"
	}
}

export BASE_DIR="$1"

# --- some validations
[ -d "$BASE_DIR" ] || exit_error "$BASE_DIR not found or not a directory"
which javac > /dev/null || exit_error "javac not found"
which javah > /dev/null || exit_error "javah not found"
which gcc > /dev/null || exit_error "gcc not found"

# --- making sure basic dir structure exists
cd $BASE_DIR
makedir target
cd target
makedir classes
makedir c_headers

# --- compiling class with native methods
javac -sourcepath ../src/main/java -d classes \
	../src/main/java/au/id/villar/fsm/poll/FileTree.java ||
	exit_error "javac returned $?"

# --- generating header file
javah -cp classes -d c_headers \
	au.id.villar.fsm.poll.FileTree ||
	exit_error "javah returned $?"

# --- compiling shared library
JAVAH_PATH=`which javah`
JAVAH_DIR=`dirname $JAVAH_PATH`
INCLUDE_DIR=`dirname $JAVAH_DIR`/include
gcc -Ic_headers -I"$INCLUDE_DIR" -I"$INCLUDE_DIR/linux"  -shared -fPIC \
	../src/main/c/filemonitor.c \
	-o classes/filemonitor.so

