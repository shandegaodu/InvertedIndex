package littleferry.share;

//: littleferry/share/LongPair.java

/**
 * @author chn
 * LongPair: class for two long numbers
 */
public class LongPair implements Comparable<LongPair> {
	
	private long l1;
	private long l2;
	
	public LongPair() {
		l1 = l2 = 0;
	}
	
	public LongPair(long l1, long l2) {
		this.l1 = l1;
		this.l2 = l2;
	}
	
	/**
	 * @return the first long number
	 */
	public long getFirst() {
		return l1;
	}

	/**
	 * @param l1 the first long number to set
	 */
	public void setFirst(long l1) {
		this.l1 = l1;
	}

	/**
	 * @return the second long number
	 */
	public long getSecond() {
		return l2;
	}

	/**
	 * @param l2 the second long number to set
	 */
	public void setSecond(long l2) {
		this.l2 = l2;
	}

	@Override
	public int compareTo(LongPair o) {
		if (l1 < o.l1) return 1;
		else if (l1 > o.l1) return -1;
		else {
			if (l2 < o.l2) return -1;
			else if (l2 > o.l2) return 1;
			else return 0;
		}
	}
	
}
///:~