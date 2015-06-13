package com.owenhuang.tcptransfer.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import com.owenhuang.tcptransfer.DataTransformUtil;
import com.owenhuang.tcptransfer.StopException;
import com.owenhuang.tcptransfer.TTLog;
import com.owenhuang.tcptransfer.TransferDefine;

public class DownloadRunnable implements Runnable {
	
	private static final int SOCKET_CONNECT_TIMEOUT = 10000;
	private static final int SOCKET_READ_TIMEOUT = 10000;
	
	private DownloadTask mDownloadTask;
	private Socket mSocket;
	private OutputStream mOutputStream;
	private InputStream mInputStream;	
	
	public DownloadRunnable(DownloadTask downloadTask) {
		mDownloadTask = downloadTask;
	}
	
	/**
     * Executes the download in a separate thread
     */
	@Override
	public void run() {	
		TTLog.d(TransferDefine.DOWNLOAD_LOG_TAG, "[DownloadRunnable]run: Enter");
		
		try {
			//连接
			connect(mDownloadTask.getHost(), mDownloadTask.getPort(), SOCKET_CONNECT_TIMEOUT, SOCKET_READ_TIMEOUT);
			
			//测试
			String str = "This is 测试的例子";
			send(DataTransformUtil.String2Bytearray(str));

			//断连
			close();
		} catch (StopException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
	}
	
	/**
	 * 是否已经连接
	 * @return
	 */
	private boolean isConnect() {
		return null == mSocket ? false : mSocket.isConnected();
	}
	
	/**
	 * 连接
	 * @param host
	 * @param port
	 * @param connectTimeout
	 * @param readTimeout
	 * @throws StopException
	 * @throws RetryException
	 */
	private void connect(String host, int port, int connectTimeout, int readTimeout) throws StopException {		
		mSocket = new Socket();
		SocketAddress socketAddress = new InetSocketAddress(host, port);
		
		try {
			mSocket.setKeepAlive(true);
			mSocket.setSoTimeout(readTimeout);
			mSocket.connect(socketAddress, connectTimeout);
			mOutputStream = mSocket.getOutputStream();
			mInputStream = mSocket.getInputStream();
		} catch (SocketException e) {
			mSocket = null;
			throw new StopException(TransferDefine.STATUS_SOCKET_CONNECT_FAILED, e.toString(), e);
		} catch (IOException e) {
			mSocket = null;
			throw new StopException(TransferDefine.STATUS_SOCKET_GET_IO_FAILED, e.toString(), e);
		}
	}
	
	/**
	 * 关闭连接
	 */
	private void close() throws StopException {
		if(isConnect()){
			try {
				mSocket.close();
				mOutputStream.close();
				mInputStream.close();
				mSocket = null;
			} catch (IOException e) {
				throw new StopException(TransferDefine.STATUS_SOCKET_CLOSE_FAILED, e.toString(), e);
			}
		}
	}
	
	/**
	 * 发送数据
	 * @param data
	 * @throws StopException
	 */
	private void send(byte[] data) throws StopException {
		if (!isConnect()) {
			throw new StopException(TransferDefine.STATUS_SOCKET_NOT_CONNECT, "send failed, socket is not connect!");
		}
		
		try {
			mOutputStream.write(data);
			mOutputStream.flush();
		} catch (IOException e) {
			throw new StopException(TransferDefine.STATUS_SOCKET_SEND_FAILED, e.toString(), e);
		}
	}
}
