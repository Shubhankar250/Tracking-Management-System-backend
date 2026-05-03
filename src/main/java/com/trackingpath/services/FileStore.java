package com.trackingpath.services;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.trackingpath.configs.UploadConfig;



public class FileStore {

	public static class Meta {
		public final File file;
		public final FileChannel ch;
		public final RangeSet ranges;
		public volatile long expected;
		public volatile long lastWriteNanos;

		public Meta(File f, FileChannel ch, RangeSet rs) {
			this.file = f;
			this.ch = ch;
			this.ranges = rs;
			this.lastWriteNanos = System.nanoTime();
		}
	}

	private static volatile FileStore INSTANCE;

	public static FileStore getInstance(UploadConfig cfg) {
		if (INSTANCE == null)
			synchronized (FileStore.class) {
				if (INSTANCE == null)
					INSTANCE = new FileStore(cfg);
			}
		return INSTANCE;
	}

	private final UploadConfig cfg;
	private final Map<String, Meta> metas = new ConcurrentHashMap<>();
	private final ThreadPoolExecutor diskPool = new ThreadPoolExecutor(16, 24, 60, TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(10000), new ThreadPoolExecutor.CallerRunsPolicy());

	private FileStore(UploadConfig cfg) {
		this.cfg = cfg;
		new File(cfg.getBaseDir()).mkdirs();
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::evictIdle, 60, 60, TimeUnit.SECONDS);
	}

	private String shardPath(String filename) {
		String sim = "unk";
		String alarm = "unk";

		String[] p = filename.split("_");
		if (p.length >= 2)
			sim = org.apache.commons.lang3.StringUtils.left(p[0], 20);
		if (p.length >= 5) {
			String part = p[4];
			int dot = part.lastIndexOf('.');
			if (dot > 0)
				part = part.substring(0, dot);
			alarm = part;
		}

		java.time.LocalDate d = java.time.LocalDate.now();
		return String.format("%s/%04d/%02d/%02d/%s/%s", cfg.getBaseDir(), d.getYear(), d.getMonthValue(),
				d.getDayOfMonth(), sim, alarm);
	}

	public void begin(String filename, long size) throws Exception {
		Meta m = metas.get(filename);
		if (m != null) {
			m.expected = size;
			return;
		}
		String dir = shardPath(filename);
		new File(dir).mkdirs();
		File f = new File(dir, sanitize(filename));
		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		raf.setLength(size); // pre-allocate
		FileChannel ch = raf.getChannel();
		RangeSet rs = new RangeSet(cfg.getBlockSize(), size);
		metas.put(filename, new Meta(f, ch, rs));
	}

	public void writeChunk(String filename, long offset, byte[] payload) {
		Meta m = metas.get(filename);
		if (m == null) {
			try {
				begin(filename, Math.max(offset + payload.length, 4 * 1024 * 1024));
			} catch (Exception ignore) {
			}
			m = metas.get(filename);
		}
		final Meta mm = m;
		final int len = payload.length;
		final ByteBuffer buf = ByteBuffer.wrap(payload);
		diskPool.submit(() -> {
			try {
				synchronized (mm.ch) {
					mm.ch.position(offset);
					while (buf.hasRemaining()) {
						mm.ch.write(buf);
					}
				}
				mm.ranges.mark(offset, len);
				mm.lastWriteNanos = System.nanoTime();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public List<long[]> missing(String filename) {
		Meta m = metas.get(filename);
		if (m == null)
			return List.of();
		return m.ranges.missing();
	}

	public void complete(String filename, long expected) throws Exception {
		Meta m = metas.get(filename);
		if (m == null)
			return;
		m.expected = expected;
		// best-effort: wait briefly for queue to drain then fsync
		diskPool.submit(() -> {
		});
		m.ch.force(true); // fsync
	}

	private void evictIdle() {
		long now = System.nanoTime();
		long maxNs = cfg.getIdleSeconds() * 1_000_000_000L;
		metas.entrySet().removeIf(e -> {
			Meta m = e.getValue();
			if (now - m.lastWriteNanos > maxNs) {
				try {
					m.ch.close();
				} catch (Exception ignore) {
				}
				return true;
			}
			return false;
		});
	}

	private static String sanitize(String n) {
		return n.replaceAll("[^a-zA-Z0-9._-]", "_");
	}
}
