package au.id.villar.fsm.poll.fail;

import java.util.HashSet;
import java.util.Set;

class LongSet {

	private static enum OpResult {
		NUM_IS_HIGHER,
		ADDED_END,
		ADDED,
		ADDED_START,
		NUM_IS_LOWER
	}

	private static class RangeNode {
		Range range;
		RangeNode next;

		private RangeNode(Range range) {
			this.range = range;
		}
	}

	private RangeNode rangeFirst;

	private class Range {
		long start;
		long end;

		private Range(long number) {
			this.start = number;
			this.end = number;
		}

		boolean contains(long number) {
			return start <= number && end >= number;
		}

		OpResult tryToAdd(long number) {
			if(number < start - 1) {
				return OpResult.NUM_IS_LOWER;
			} else if(start - 1 == number) {
				start--;
				return OpResult.ADDED_START;
			} else if(contains(number)) {
				return OpResult.ADDED;
			} else if(end + 1 == number) {
				end++;
				return OpResult.ADDED_END;
			} else {
				return OpResult.NUM_IS_HIGHER;
			}
		}

		boolean isLowerThan(long number) {
			return end < number;
		}

	}

	boolean contains(long number) {
		RangeNode node = rangeFirst;
		while(node != null && node.range.isLowerThan(number)) {
			node = node.next;
		}
		return node != null && node.range.contains(number);
	}

	void add(long number) {
		if(rangeFirst == null) {
			rangeFirst = new RangeNode(new Range(number));
			return;
		}

		OpResult result = OpResult.NUM_IS_HIGHER;
		Range range = null;
		RangeNode node = rangeFirst;
		RangeNode prevNode = null;

		for(;
				node != null &&
						(result = (range = node.range).tryToAdd(number)) == OpResult.NUM_IS_HIGHER;
				node = node.next) {
			prevNode = node;
		}

		switch (result) {
			case NUM_IS_HIGHER:
				prevNode.next = new RangeNode(new Range(number));
				return;
			case ADDED:
				return;
			case NUM_IS_LOWER:
				RangeNode newNode = new RangeNode(new Range(number));
				newNode.next = node;
				if(prevNode == null) {
					rangeFirst = newNode;
				} else {
					prevNode.next = newNode;
				}
				return;
			case ADDED_START:
				if(prevNode != null) {
					Range prev = prevNode.range;
					if(prev.end + 1 == range.start) {
						prev.end = range.end;
						prevNode.next = node.next;
					}
				}
				return;
			case ADDED_END:
				prevNode = node;
				if (prevNode.next != null) {
					node = node.next;
					Range next = prevNode.range;
					if(next.start - 1 == range.end) {
						range.end = next.end;
						prevNode.next = node.next;
					}
				}
				break;
		}
	}


	public static void main(String[] args) {

		int n = 100000;

		Set<Long> longSet2 = new HashSet<>();
		LongSet longSet1 = new LongSet();

		long t1 = System.currentTimeMillis();

		for(int x = 0; x < n; x++) {
			longSet1.add((long)(Math.random() * n));
		}

		long t2 = System.currentTimeMillis();

		for(int x = 0; x < n; x++) {
			longSet2.add((long)(Math.random() * n));
		}

		long t3 = System.currentTimeMillis();

		for(int x = 0; x < n; x++) {
			if(longSet1.contains((long)(Math.random() * n))) System.out.print("");
		}

		long t4 = System.currentTimeMillis();

		for(int x = 0; x < n; x++) {
			if(longSet2.contains((long)(Math.random() * n))) System.out.print("");
		}

		long t5 = System.currentTimeMillis();

		System.out.format("ADD 1: %12d%nADD 2: %12d%nBOL 1: %12d%nBOL 2: %12d%n",
				t2 - t1, t3 - t2, t4 - t3, t5 - t4);
	}

}
