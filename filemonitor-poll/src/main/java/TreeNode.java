import java.util.HashMap;
import java.util.Map;

public class TreeNode<T extends IntKeyObject> {

	private TreeNode<T> left;
	private TreeNode<T> right;
	private T value;
	private int deep;
	private int id;

	public TreeNode(T value) {
		this.value = value;
		this.id = value.getId();
	}


	private void recalculateDeep() {
		int deepLeft = left != null? left.deep + 1: 0;
		int deepRight = right != null? right.deep + 1: 0;
		deep = Math.max(deepLeft, deepRight);
	}

	private void recalculateDeepAfterRotation() {
		left.recalculateDeep();
		right.recalculateDeep();
		recalculateDeep();
	}






	private static <T extends IntKeyObject> TreeNode<T> addNode(TreeNode<T> node, T value) {
		int id = value.getId();
		if(node.id == id) {
			node.value = value;
			return node;
		}
		if(node.id < id) {
			if(node.right == null) {
				node.right = new TreeNode<>(value);
				if(node.deep == 0) {
					node.deep = 1;
				}
			} else {
				node.right = addNode(node.right, value);
				int deep = node.right.deep + 1;
				if(node.deep < deep) {
					node.deep = deep;
				}
			}
		} else {
			if(node.left == null) {
				node.left = new TreeNode<>(value);
				if(node.deep == 0) {
					node.deep = 1;
				}
			} else {
				node.left = addNode(node.left, value);
				int deep = node.left.deep + 1;
				if(node.deep < deep) {
					node.deep = deep;
				}
			}
		}
		node = balance(node);
		return node;
	}




	public static <T extends IntKeyObject> T getNode(TreeNode<T> root, int id) {
		while (root != null) {
			if(root.id == id) {
				return root.value;
			} else if(root.id < id) {
				root = root.right;
			} else {
				root = root.left;
			}
		}
		return  null;
	}




	private static <T extends IntKeyObject> TreeNode<T> balance(TreeNode<T> node) {
		int deepLeft = node.left != null? node.left.deep + 1: 0;
		int deepRight = node.right != null? node.right.deep + 1: 0;
		if(Math.abs(deepLeft - deepRight) <= 1) {
			return node;
		}
		if(deepLeft > deepRight) {
			if(node.left.left != null) {
				return balanceRight1(node);
			} else {
				return balanceRight2(node);
			}
		} else {
			if(node.right.right != null) {
				return balanceLeft1(node);
			} else {
				return balanceLeft2(node);
			}
		}
	}

	private static <T extends IntKeyObject> TreeNode<T> balanceRight1(TreeNode<T> node) {
		TreeNode<T> root = node.left;
		TreeNode<T> leftRight = root.right;
		root.right = node;
		node.left = leftRight;
		node.recalculateDeep();
		root.recalculateDeep();
		return root;
	}

	private static <T extends IntKeyObject> TreeNode<T> balanceRight2(TreeNode<T> node) {
		TreeNode<T> root = node.left.right;
		TreeNode<T> newRootLeft = root.left;
		TreeNode<T> newRootRight = root.right;
		node.left.right = newRootLeft;
		root.left = node.left;
		root.right = node;
		node.left = newRootRight;
		root.recalculateDeepAfterRotation();
		return root;
	}

	private static <T extends IntKeyObject> TreeNode<T> balanceLeft1(TreeNode<T> node) {
		TreeNode<T> root = node.right;
		TreeNode<T> rightLeft = root.left;
		root.left = node;
		node.right = rightLeft;
		node.recalculateDeep();
		root.recalculateDeep();
		return root;
	}

	private static <T extends IntKeyObject> TreeNode<T> balanceLeft2(TreeNode<T> node) {
		TreeNode<T> root = node.right.left;
		TreeNode<T> newRootRight = root.right;
		TreeNode<T> newRootLeft = root.left;
		node.right.left = newRootRight;
		root.right = node.right;
		root.left = node;
		node.right = newRootLeft;
		root.recalculateDeepAfterRotation();
		return root;
	}









	private static class MyObj implements IntKeyObject {

		private int id;

		private MyObj(int id) {
			this.id = id;
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public String toString() {
			return "{" + id + '}';
		}
	}







	private static final int NUM = 10000000;

	private static int createRandomIndex() {
		return ((int)(Math.random() * NUM));
	}


	private static MyObj createRandomObject() {
		return new MyObj(createRandomIndex() + 1);
	}

	public static void main(String[] args) {

		TreeNode<MyObj> root = new TreeNode<>(createRandomObject());
		Map<Integer, MyObj> map = new HashMap<>();

		System.out.println("--- 1");
		long timestamp1 = System.currentTimeMillis();

		for(int x = 0; x < NUM; x++) {
			root = addNode(root, createRandomObject());
		}

		System.out.println("--- 2");
		long timestamp2 = System.currentTimeMillis();

		for(int x = 0; x < NUM; x++) {
			MyObj obj = createRandomObject();
			map.put(obj.getId(), obj);
		}

		System.out.println("--- 3");
		long timestamp3 = System.currentTimeMillis();

		for(int x = 0; x < NUM; x++) {
			MyObj obj = getNode(root, createRandomIndex());
		}

		System.out.println("--- 4");
		long timestamp4 = System.currentTimeMillis();

		for(int x = 0; x < NUM; x++) {
			MyObj obj = map.get(createRandomIndex());
		}

		System.out.println("--- 5");
		long timestamp5 = System.currentTimeMillis();

		System.out.format("Time 1: %11d%nTime 2: %11d%nTime 3: %11d%nTime 4: %11d%n",
				timestamp2 - timestamp1,
				timestamp3 - timestamp2,
				timestamp4 - timestamp3,
				timestamp5 - timestamp4
		);
	}

}
