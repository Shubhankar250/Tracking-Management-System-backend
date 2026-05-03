package com.trackingpath.services;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class RangeSet {
	private final int block;
	private final BitSet bs;
	private final long expect;

	public RangeSet(int blockSize, long expectedBytes) {
		this.block = blockSize;
		this.expect = expectedBytes;
		int blocks = (int) ((expectedBytes + block - 1) / block);
		this.bs = new BitSet(Math.max(blocks, 1));
	}

	public void mark(long offset, int len) {
		int start = (int) (offset / block);
		int end = (int) ((offset + len - 1) / block);
		bs.set(start, end + 1);
	}

	public List<long[]> missing() {
		List<long[]> out = new ArrayList<>();
		int blocks = (int) ((expect + block - 1) / block);
		int i = 0;
		while (i < blocks) {
			int nc = bs.nextClearBit(i);
			if (nc >= blocks)
				break;
			int ns = bs.nextSetBit(nc);
			int end = (ns < 0) ? blocks : ns;
			long off = (long) nc * block;
			long len = Math.min((long) (end - nc) * block, expect - off);
			out.add(new long[] { off, len });
			i = end + 1;
		}
		return out;
	}
}
