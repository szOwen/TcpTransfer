package com.owenhuang.tcptransfer.download;

public class DownloadTask {
	private String mHost;	//地址
	private int mPort;	//端口
	
	public void setHost(String host) {
		mHost = host;
	}
	public String getHost() {
		return mHost;
	}
	
	public void setPort(int port) {
		mPort = port;
	}
	public int getPort() {
		return mPort;
	}
}
