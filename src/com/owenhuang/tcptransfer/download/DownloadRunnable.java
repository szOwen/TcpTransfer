package com.owenhuang.tcptransfer.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import android.os.Environment;

import com.owenhuang.tcptransfer.DataPackage;
import com.owenhuang.tcptransfer.DataStreamUtil;
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
	private DataPackage mLastDataPackage = null;
	
	public DownloadRunnable(DownloadTask downloadTask) {
		mDownloadTask = downloadTask;
	}
	
	/**
     * Executes the download in a separate thread
     */
	@Override
	public void run() {	
		TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[DownloadRunnable]run: Enter");
		
		try {
			//连接
			connect(mDownloadTask.getHost(), mDownloadTask.getPort(), SOCKET_CONNECT_TIMEOUT, SOCKET_READ_TIMEOUT);
			
			//请求文件
			File pictureDirFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			String filePath = pictureDirFile.getPath() + "/BabyImpre/BI28.jpg";
			requestFile(filePath, 0);
			
			//收取数据包
			int readCount = 0;
			byte[] buffer = new byte[256];
			while (-1 != (readCount = mInputStream.read(buffer))) {
				TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[DownloadRunnable]run: readCount = " + readCount);
				processRecvData(buffer, readCount);
			}
		} catch (StopException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//断连
			close();			
		}
	}
	
	/**
	 * 请求文件
	 * @param fileName
	 * @param Offset
	 */
	private void requestFile(String fileName, int fileDataOffset) throws StopException {
		TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[DownloadRunnable]requestFile: fileName = " + fileName + ", fileDataOffset = " + fileDataOffset);
		
		int fileNameLength = DataStreamUtil.writeString2ByteArray(fileName, null, 0);
		byte[] data = new byte[2 + 4 + 4 + fileNameLength + 4];
		int offset = 0;
		//请求头
		offset = DataStreamUtil.writeShort2ByteArray(TransferDefine.PROTOCOL_TYPE_REQUEST_FILE, data, offset);
		offset = DataStreamUtil.writeInt2ByteArray(4 + fileNameLength + 4, data, offset);
		//文件名长度
		offset = DataStreamUtil.writeInt2ByteArray(fileNameLength, data, offset);
		//文件名
		offset = DataStreamUtil.writeString2ByteArray(fileName, data, offset);
		//文件数据偏移
		offset = DataStreamUtil.writeInt2ByteArray(fileDataOffset, data, offset);
		send(data);		
	}
	
	/**
	 * 处理接收到的数据
	 * @param data
	 */
	private void processRecvData(byte[] data, int readCount) throws StopException {
		int appendCount = 0;
		//判断是不有数据包没接完
		if (null != mLastDataPackage && false == mLastDataPackage.isReceiveFinish()) {
			appendCount = mLastDataPackage.appendData(data, 0, readCount);
			if (mLastDataPackage.isReceiveFinish()) {
				processDataPackage(mLastDataPackage);
				
				
			}
		}
		
		/*int offset = 0;
		int protocolType = DataStreamUtil.readShortFromByteArray(data, offset);
		offset += 2;
		int dataLength = DataStreamUtil.readIntFromByteArray(data, offset);
		offset += 4;
		TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[DownloadRunnable]processRecvData: protocolType = " + protocolType + ", dataLength = " + dataLength);
		
		switch (protocolType) {
		case TransferDefine.PROTOCOL_TYPE_TRANSFER_FILE:
			int rtn = DataStreamUtil.readIntFromByteArray(data, offset);
			offset += 4;
			TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[DownloadRunnable]processRecvData: rtn = " + rtn);
		}*/
	}
	
	private void processDataPackage(DataPackage dataPackage) {
		
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
			//mSocket.setSoTimeout(readTimeout);
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
	private void close() {
		if(isConnect()){
			try {
				mSocket.close();
				mOutputStream.close();
				mInputStream.close();
				mSocket = null;
			} catch (IOException e) {
				
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
