package au.id.villar.fsm.poll;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

class FileTree {


	private class QueueNode {
		Node node;
		Path path;
		QueueNode(Node node, Path path) { this.node = node; this.path = path; }
	}

	private class ByNameComparator implements Comparator<Node> {
		@Override
		public int compare(Node o1, Node o2) {
			return o1.name.compareTo(o2.name);
		}
	}

	private ByNameComparator byNameComparator = new ByNameComparator();




	private final Path rootDir;
	private final boolean followLinks;
	private final Pattern[] ignorePatterns;

	private Node rootNode;
	private Map<Long, Node> inodeToNode;


	FileTree(Path rootDir, boolean followLinks, Pattern ... ignorePatterns) {
		this.rootDir = rootDir;
		this.followLinks = followLinks;
		this.ignorePatterns = ignorePatterns;
		createTree();
	}

	public List<FileEvent> getChanges(FileTree newer) {


	}

	private void createTree() {

		Node node;
		QueueNode queueNode;
		Deque<QueueNode> nodes = new ArrayDeque<>();

		rootNode = createNode(rootDir);
		inodeToNode = new HashMap<>();
		inodeToNode.put(rootNode.inode, rootNode);
		nodes.push(new QueueNode(rootNode, rootDir));

		while((queueNode = nodes.poll()) != null) {

			node = queueNode.node;
			Path nodePath = queueNode.path;

			if(node instanceof DirNode) {
				DirNode dirNode = (DirNode)node;
				try {
					for(String name: readDir(nodePath.toString())) {
						Path newPath = nodePath.resolve(name);
						boolean ignorePath = false;
						for(Pattern ignorePattern: ignorePatterns) {
							if(ignorePattern.matcher(newPath.toString()).matches()) {
								ignorePath = true;
								break;
							}
						}
						if(!ignorePath) {
							Node newNode = createNode(newPath);
							if(newNode != null) {
								dirNode.children.add(newNode);
								newNode.parent = dirNode;
								nodes.push(new QueueNode(newNode, newPath));
								inodeToNode.put(newNode.inode, newNode);
							}
						}
					}
					Collections.sort(dirNode.children, byNameComparator);
				} catch(NoSuchFileOrDirectoryException | NotADirException ignore) {
				}
			}
		}
	}

	private Node createNode(Path path) {

//System.out.println(">> " + path); // TODO delete

		Node node;
		PathInfo info;
		String strPath = path.toString();
		String name = path.getName(path.getNameCount() - 1).toString();

		try {
			info = getInfo(strPath);
		} catch (NoSuchFileOrDirectoryException | NotADirException e) {
			return null;
		}

		if(info.isFile()) {
			node = new Node();
		} else if(info.isDirectory()) {
			node = new DirNode();
		} else if(info.isLink()) {
			LinkNode linkNode = new LinkNode();
			try {
				linkNode.link = readLink(strPath);
			} catch (NoSuchFileOrDirectoryException | NotADirException e) {
				return null;
			}
			node = linkNode;
		} else {
			// node type not supported
			return null;
		}
		node.inode = info.getInode();
		node.lastUpdated = info.getLastStatusChange();
		node.name = name;
		return node;
	}


	private native PathInfo getInfo(String path, boolean followLinks);

	private native String readlink(String path, int size);

	private native List<String> readDir(String path);

	private String readLink(String path) {
		return readlink(path, -1);
	}

	private PathInfo getInfo(String path) {
		return getInfo(path, followLinks);
	}



}
