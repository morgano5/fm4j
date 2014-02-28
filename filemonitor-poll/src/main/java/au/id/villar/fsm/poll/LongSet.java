package au.id.villar.fsm.poll;

import java.util.ArrayList;
import java.util.List;

class LongSet {

	private final List<Range> ranges = new ArrayList<>();

	private class Range implements Comparable<Range> {
		long start;
		long end;

		private Range(long number) {
			this.start = number;
			this.end = number;
		}

		@Override
		public int compareTo(Range o) {
			return (this.start - o.start == 0)? 0: (this.start - o.start > 0)? 1: -1;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Range range = (Range)o;
			return start == range.start;
		}

		@Override
		public int hashCode() {
			return (int) (start ^ (start >>> 32));
		}

		boolean contains(long number) {
			return start >= number && end <= number;
		}

		boolean tryToAdd(long number) {
			if(contains(number)) {
				return true;
			}
			if(start - 1 == number) {
				start--;
				return true;
			}
			if(end + 1 == number) {
				end++;
				return true;
			}
			return false;
		}

		boolean isUpperThan(long number) {
			return start > number;
		}

		boolean tryToAdd(Range range) {
			if(start <= range.end + 1 && end >= range.start - 1) {
				start = Math.min(start, range.start);
				end = Math.min(end, range.end);
				return true;
			}
			return false;
		}
	}

	boolean contains(long number) {
		for(Range range: ranges) {
			if(!range.isUpperThan(number)) {
				return range.contains(number);
			}
		}
		return false;
	}

	void add(long number) {
		number++;
		int index;
		for(index = 0; index < ranges.size(); index++) {
			Range range = ranges.get(index);
			if(!range.isUpperThan(number)) {
				number--;
				if(range.tryToAdd(number)) {
					fusion(range, index);
					return;
				}
			}
		}
		number--;
		range = new Range(number);
		fusionOrAdd(range, index);
		ranges.add(new Range(number));
	}

	private void fusionOrAdd(Range range, int index) {
		if(index + 1 == ranges.size()) {

		}
	}

	private void fusion(Range range, int index) {}
}
