package com.trackingpath.services;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RtpPsPacker {

	  public static class State {
	    public int seq = ThreadLocalRandom.current().nextInt(0, 65535);
	    public int ssrc = ThreadLocalRandom.current().nextInt();
	    public long lastTs = 0;
	  }

	  public static List<ByteBuffer> packetize(byte[] ps, int mtu, int payloadType, long ts90k, State st) {
	    List<ByteBuffer> list = new ArrayList<>();
	    int off = 0;
	    while (off < ps.length) {
	      int chunk = Math.min(mtu, ps.length - off);
	      boolean marker = (off + chunk) >= ps.length;
	      ByteBuffer pkt = ByteBuffer.allocate(12 + chunk);
	      pkt.put((byte) 0x80);
	      int mpt = (marker ? 0x80 : 0x00) | (payloadType & 0x7F);
	      pkt.put((byte) mpt);
	      st.seq = (st.seq + 1) & 0xFFFF;
	      pkt.putShort((short) st.seq);
	      pkt.putInt((int) ts90k);
	      pkt.putInt(st.ssrc);
	      pkt.put(ps, off, chunk);
	      pkt.flip();
	      list.add(pkt);
	      off += chunk;
	    }
	    st.lastTs = ts90k;
	    return list;
	  }
	}