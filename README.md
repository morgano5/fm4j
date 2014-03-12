# fm4j

File monitoring library for Java. The goal of this is provide a library with
file monitoring capabilities to solve some problems I found on existing
solutions. I needed a way to monitor a huge tree of directories for adding,
deleting, changing AND moving files.

The reason I couldn't use Jnotify is that it is based on Linux inofity calls
that depend on kernel memory, which runs out very quickly when trying to
monitor a huge subdirectory tree.

The reason I can't use the new API that comes with Java 7
(java.nio.file.WatchService) is that it doesn't have support to monitor
"file moving" events (just "add", "delete" and "modify"). The reason I need
"file moving" is that I need to synchronize a big subtree against a file
repository in the cloud. If I move a big file to another directory inside the
same tree, I don't want to send "delete" and then "add" with a lot of the file
data just to move it.

## Status

I'm currently working to get an operational version under Linux. If you want
to help you're more than welcome.

## TODO List

### Functionals

* Versions for different Unix flavors and Windows

### Internals

* Analyze the possibility of using iteration instead of recursion in some methods in FileTree.java
* Would it be useful not to modify the "newer" tree and leaving it intact without reusing it?


