package com.trackingpath.services;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class StreamContext {
	  public final String streamId;
	  public final String zlmHost;
	  public final int zlmPort;
		public final DatagramSocket udp;
	  public final RtpPsPacker.State rtp = new RtpPsPacker.State();

	  public StreamContext(String streamId, String zlmHost, int zlmPort, DatagramSocket udp) {
	    this.streamId = streamId;
	    this.zlmHost = zlmHost;
	    this.zlmPort = zlmPort;
	    this.udp = udp;
	  }

	  public void send(ByteBuffer pkt) throws Exception {
	    byte[] b = new byte[pkt.remaining()];
	    pkt.get(b);
	    DatagramPacket dp = new DatagramPacket(b, b.length, InetAddress.getByName(zlmHost), zlmPort);
	    udp.send(dp);
	  }
	}

