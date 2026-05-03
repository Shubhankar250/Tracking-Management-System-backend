package com.trackingpath.dtos;

import lombok.Data;

@Data
public class OnPublishMessage {
	public String type = "on_publish";
	public String app;
	public String stream;
	public String channel; // "CH1"
	public String profile; // "sub" by default (no profile in streamId)
	public String hls; // computed link

	public OnPublishMessage(String app, String stream, String channel, String profile, String hls) {
		this.app = app;
		this.stream = stream;
		this.channel = channel;
		this.profile = profile;
		this.hls = hls;
	}

	
}
