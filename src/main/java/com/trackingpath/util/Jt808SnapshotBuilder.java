package com.trackingpath.util;

import java.io.ByteArrayOutputStream;

public class Jt808SnapshotBuilder {
	/**

	* 0x8801 - Camera snapshot command

	*

	* Body layout used here:

	* 1  byte  channel

	* 2  bytes command

	* 2  bytes interval

	* 1  byte  saveFlag

	* 1  byte  resolution

	* 1  byte  quality

	* 1  byte  brightness

	* 1  byte  contrast

	* 1  byte  saturation

	* 1  byte chroma

	*/

	public static String build8801(

	String sim,

	int serial,

	int channel,

	int command,

	int interval,

	int saveFlag,

	int resolution,

	int quality,

	int brightness,

	int contrast,

	int saturation,

	int chroma) {



	ByteArrayOutputStream body = new ByteArrayOutputStream();



	writeByte(body, channel);

	writeWord(body, command);

	writeWord(body, interval);

	writeByte(body, saveFlag);

	writeByte(body, resolution);

	writeByte(body, quality);

	writeByte(body, brightness);

	writeByte(body, contrast);

	writeByte(body, saturation);

	writeByte(body, chroma);



	return buildFrame(0x8801, sim, serial, body.toByteArray());

	}



	/**

	* Working snapshot helper based on your known-good hex pattern:

	* channel=1, command=1, interval=0, save=0, resolution=2, quality=2,

	* brightness/contrast/saturation/chroma=128

	*/

	public static String build8801TakePhoto(String sim, int serial, int channel) {

	return build8801(

	sim,

	serial,

	channel,

	1, // command

	0, // interval

	0, // saveFlag

	2, // resolution

	2, // quality

	128, // brightness

	128, // contrast

	128, // saturation

	128 // chroma

	);

	}



	/**

	* 0x8104 - Query terminal parameters

	*/

	public static String build8104(String sim, int serial) {

	return buildFrame(0x8104, sim, serial, new byte[0]);

	}



	/**

	* Generic JT808 2011-style frame builder:

	* [msgId:2][bodyLen:2][simBCD:6][serial:2][body][xor]

	* wrapped with 0x7E ... 0x7E and escaped.

	*/

	public static String buildFrame(int msgId, String sim, int serial, byte[] body) {

	if (body == null) {

	body = new byte[0];

	}



	ByteArrayOutputStream payload = new ByteArrayOutputStream();



	writeWord(payload, msgId);

	writeWord(payload, body.length);

	writeBytes(payload, encodeBcd(sim, 6));

	writeWord(payload, serial);

	writeBytes(payload, body);



	byte[] raw = payload.toByteArray();

	byte checksum = xor(raw);



	ByteArrayOutputStream frame = new ByteArrayOutputStream();

	frame.write(0x7E);

	writeBytes(frame, escape(raw));

	writeBytes(frame, escape(new byte[]{checksum}));

	frame.write(0x7E);



	return bytesToHex(frame.toByteArray());

	}


    // 0x8105 - generic terminal control
    public static String build8105(String sim, int serial, int commandWord) {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        writeWord(body, commandWord);
        return buildFrame(0x8105, sim, serial, body.toByteArray());
    }

    // 0x8105 - restart wrapper
    public static String build8105Restart(String sim, int serial) {
        // change this word if your vendor expects different restart code
        return build8105(sim, serial, 0x0001);
    }

	private static byte[] encodeBcd(String value, int byteLength) {

	if (value == null) {

	throw new IllegalArgumentException("SIM cannot be null");

	}



	String digits = value.replaceAll("\\D", "");

	int required = byteLength * 2;



	if (digits.length() > required) {

	digits = digits.substring(digits.length() - required);

	}

	while (digits.length() < required) {

	digits = "0" + digits;

	}



	byte[] result = new byte[byteLength];

	for (int i = 0; i < byteLength; i++) {

	int hi = Character.digit(digits.charAt(i * 2), 10);

	int lo = Character.digit(digits.charAt(i * 2 + 1), 10);

	if (hi < 0 || lo < 0) {

	throw new IllegalArgumentException("Invalid numeric value for BCD: " + value);

	}

	result[i] = (byte) ((hi << 4) | lo);

	}

	return result;

	}



	private static byte xor(byte[] data) {

	byte v = 0;

	for (byte b : data) {

	v ^= b;

	}

	return v;

	}



	private static byte[] escape(byte[] data) {

	ByteArrayOutputStream out = new ByteArrayOutputStream();

	for (byte b : data) {

	int v = b & 0xFF;

	if (v == 0x7E) {

	out.write(0x7D);

	out.write(0x02);

	} else if (v == 0x7D) {

	out.write(0x7D);

	out.write(0x01);

	} else {

	out.write(v);

	}

	}

	return out.toByteArray();

	}



	private static void writeByte(ByteArrayOutputStream out, int value) {

	out.write(value & 0xFF);

	}



	private static void writeWord(ByteArrayOutputStream out, int value) {

	out.write((value >> 8) & 0xFF);

	out.write(value & 0xFF);

	}



	private static void writeBytes(ByteArrayOutputStream out, byte[] data) {

	out.write(data, 0, data.length);

	}



	public static String bytesToHex(byte[] bytes) {

	StringBuilder sb = new StringBuilder(bytes.length * 2);

	for (byte b : bytes) {

	sb.append(String.format("%02x", b & 0xFF));

	}

	return sb.toString();

	}



	public static void main(String[] args) {

	String sim = "505073769899";



	String snapshot = build8801TakePhoto(sim, 2, 1);

	System.out.println("8801 snapshot: " + snapshot);

	// expected:

	// 7e8801000c5050737698990002010001000000020280808080837e
	



	String queryParams = build8104(sim, 1);

	System.out.println("8104 query:    " + queryParams);

	// expected:

	// 7e810400005050737698990001807e
	 String restart = build8105Restart(sim, 3);
     System.out.println("8105 restart = " + restart);

	}

	}




   


   
   

    