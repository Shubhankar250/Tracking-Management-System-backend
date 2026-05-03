package com.trackingpath.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Build FULL JT/T 808 & 1078 command frames (HEX, uppercase, no spaces). - XOR
 * checksum (default: BODY ONLY per your vendor), 0x7E framing, 0x7E/0x7D
 * escaping - Terminal ID as BCD[10] or BCD[6] (left-pad) per your vendor - Time
 * BCD [YY,MM,DD,HH,mm,ss] uses DEVICE LOCAL TIME (no forced GMT+8)
 *
 * Supported: 0x9101, 0x9102, 0x9205, 0x9201, 0x9202, 0x9206, 0x9207, 0x9208,
 * 0x9103, 0x9105
 */
public final class Jt808AllCmdHexUtil {

	private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public record CmdFrame(String hex, int serial) {
	}

	private Jt808AllCmdHexUtil() {
	}

	/* =========================== Public API =========================== *
	
	/** 0x8801 — Camera Snapshot */
	public static String cmd8801Hex(
	        String termIdDigits,
	        int channel,
	        int command,
	        int interval,
	        int count,
	        int resolution) {

	    byte[] body = Bodies.x8801(channel, command, interval, count, resolution);

	    return Frames.buildFrameHex6Sim(0x8801, termIdDigits, body, null);
	}
	/**
	 * Convenience: build 0x9205 for one whole day on a single logical channel.
	 *
	 * @param termIdDigits   SIM / terminal id digits (e.g. "862630123456789")
	 * @param logicalChannel logical channel (1..n, or 0=all)
	 * @param dayStart       "yyyy-MM-dd HH:mm:ss" in device local time (IST)
	 * @param dayEnd         "yyyy-MM-dd HH:mm:ss" in device local time (IST)
	 * @param storageType    0=all, 1=main storage, 2=backup
	 */
	


	

	/* =========================== Frames / Codec =========================== */

	public static final class Frames {
		private static final AtomicInteger SEQ = new AtomicInteger(1);

		@SuppressWarnings("unused")
		private static final boolean CHECKSUM_BODY_ONLY = false;

		public static String buildFrameHex6Sim(int msgId, String sim12Digits, byte[] body, Integer serialOrNull) {
			if (body == null)
				body = new byte[0];
			final int serial = (serialOrNull == null) ? nextSerial() : (serialOrNull & 0xFFFF);

			// 12-byte header: 2 msgId + 2 props + 6 BCD SIM + 2 serial
			byte[] simBcd6 = Jt808AllCmdHexUtil.Codec.bcd12(sim12Digits, 6);
			int bodyLen = body.length & 0x03FF;
			int props = bodyLen; // no encrypt/subpack

			byte[] header = new byte[12];
			int i = 0;
			header[i++] = (byte) ((msgId >> 8) & 0xFF);
			header[i++] = (byte) (msgId & 0xFF);
			header[i++] = (byte) ((props >> 8) & 0xFF);
			header[i++] = (byte) (props & 0xFF);
			System.arraycopy(simBcd6, 0, header, i, 6);
			i += 6;
			header[i++] = (byte) ((serial >> 8) & 0xFF);
			header[i] = (byte) (serial & 0xFF);

			byte[] msgNoCs = new byte[header.length + body.length];
			System.arraycopy(header, 0, msgNoCs, 0, header.length);
			System.arraycopy(body, 0, msgNoCs, header.length, body.length);

			// XOR over header + body
			byte cs = Codec.xor(msgNoCs, msgNoCs.length);

			byte[] withCs = new byte[msgNoCs.length + 1];
			System.arraycopy(msgNoCs, 0, withCs, 0, msgNoCs.length);
			withCs[withCs.length - 1] = cs;

			byte[] framed = Jt808AllCmdHexUtil.Codec.escapeAndFrame(withCs);
			return Jt808AllCmdHexUtil.Codec.toHex(framed);
		}


		private static int nextSerial() {
			int v = SEQ.getAndIncrement() & 0xFFFF;
			if (v == 0) {
				SEQ.set(1);
				return 1;
			}
			return v;
		}
	}

	public static final class Codec {

		// BCD[6/12] SIM helpers
		public static byte[] bcd12(String digits, int outBytes) {
			if (digits == null)
				digits = "";
			String s = digits.replaceAll("\\D", "");
			int targetDigits = outBytes * 2;
			if (s.length() > targetDigits)
				s = s.substring(s.length() - targetDigits);
			if (s.length() < targetDigits)
				s = "0".repeat(targetDigits - s.length()) + s;
			byte[] out = new byte[outBytes];
			for (int i = 0; i < outBytes; i++) {
				int hi = s.charAt(2 * i) - '0';
				int lo = s.charAt(2 * i + 1) - '0';
				out[i] = (byte) ((hi << 4) | lo);
			}
			return out;
		}

		public static void putWord(ByteArrayOutputStream b, int v) {
			if ((v & ~0xFFFF) != 0)
				throw new IllegalArgumentException("word out of range: " + v);
			b.write((v >> 8) & 0xFF);
			b.write(v & 0xFF);
		}

		public static void putDword(ByteArrayOutputStream b, long v32) {
			int v = (int) (v32 & 0xFFFFFFFFL);
			b.write((v >> 24) & 0xFF);
			b.write((v >> 16) & 0xFF);
			b.write((v >> 8) & 0xFF);
			b.write(v & 0xFF);
		}

		public static byte xor(byte[] a, int len) {
			byte x = 0;
			for (int i = 0; i < len; i++)
				x ^= a[i];
			return x;
		}

		public static byte[] escapeAndFrame(byte[] raw) {
			ByteArrayOutputStream out = new ByteArrayOutputStream(raw.length + 8);
			out.write(0x7E);
			for (byte b : raw) {
				if (b == 0x7E) {
					out.write(0x7D);
					out.write(0x02);
				} else if (b == 0x7D) {
					out.write(0x7D);
					out.write(0x01);
				} else {
					out.write(b);
				}
			}
			out.write(0x7E);
			return out.toByteArray();
		}

		public static String toHex(byte[] a) {
			StringBuilder sb = new StringBuilder(a.length * 2);
			for (byte x : a) {
				sb.append(String.format("%02X", x));
			}
			return sb.toString();
		}
		
	}

	/*
	 * =========================== Bodies (per-command) ===========================
	 */

	public static final class Bodies {

		// 0x8801 — Camera Snapshot
		public static byte[] x8801(
		        int channel,
		        int command,
		        int interval,
		        int count,
		        int resolution) {

		    ByteArrayOutputStream b = new ByteArrayOutputStream();

		    b.write(channel & 0xFF);       // camera channel
		    b.write(command & 0xFF);       // 0 = shoot
		    Codec.putWord(b, interval);    // interval ms
		    Codec.putWord(b, count);       // number of photos
		    b.write(resolution & 0xFF);    // resolution type

		    return b.toByteArray();
		}

	}

	/* Start Dashcam Remote access*/


	public static String build8300(String sim, int serial, String text) {

	    byte[] simBytes = encodeBcd(sim, 6);

	    byte[] textBytes = text.getBytes();

	    // ✅ +1 for flag byte
	    int bodyLen = 1 + textBytes.length;

	    ByteArrayOutputStream body = new ByteArrayOutputStream();

	    // Message ID 0x8300
	    body.write(0x83);
	    body.write(0x00);

	    // Body length
	    body.write((bodyLen >> 8) & 0xFF);
	    body.write(bodyLen & 0xFF);

	    // SIM (BCD 6 bytes)
	    body.write(simBytes, 0, simBytes.length);

	    // Serial number
	    body.write((serial >> 8) & 0xFF);
	    body.write(serial & 0xFF);

	    // ✅ FLAG BYTE (VERY IMPORTANT)
	    body.write(0x00);

	    // Text content
	    body.write(textBytes, 0, textBytes.length);

	    byte[] payload = body.toByteArray();

	    byte checksum = 0;
	    for (byte b : payload) {
	        checksum ^= b;
	    }

	    ByteArrayOutputStream frame = new ByteArrayOutputStream();
	    frame.write(0x7E);
	    frame.write(payload, 0, payload.length);
	    frame.write(checksum);
	    frame.write(0x7E);

	    return bytesToHex(frame.toByteArray());
	}
    private static byte[] encodeBcd(String number, int len) {
        byte[] bcd = new byte[len];
        for (int i = 0; i < len; i++) {
            int idx = i * 2;
            int high = idx < number.length() ? number.charAt(idx) - '0' : 0;
            int low = (idx + 1) < number.length() ? number.charAt(idx + 1) - '0' : 0;
            bcd[i] = (byte) ((high << 4) | low);
        }
        return bcd;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
    
	/* End  Dashcam Remote access */
	/*
	 * =========================== Convenience helpers ===========================
	 */

	public static void main(String[] args) {
		// Quick sanity test for one command
		String hex9101 = Jt808AllCmdHexUtil.build8300("505073769899", 1,"#FRPSET:vms.trackingpath.com,7001,coointech2024");
		System.out.println("hex9101:" + hex9101);
	}

	

}
