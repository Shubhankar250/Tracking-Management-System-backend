package com.trackingpath.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "upload")
public class UploadConfig {
	private int tcpPort = 6900;
	private String baseDir = "/data/adas";
	private int blockSize = 32768;
	private int idleSeconds = 600;

	public int getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public int getIdleSeconds() {
		return idleSeconds;
	}

	public void setIdleSeconds(int idleSeconds) {
		this.idleSeconds = idleSeconds;
	}
}