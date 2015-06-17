package com.owenhuang.tcptransfer.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.owenhuang.tcptransfer.DataStreamUtil;
import com.owenhuang.tcptransfer.StopException;
import com.owenhuang.tcptransfer.TTLog;
import com.owenhuang.tcptransfer.TransferDefine;

public class UploadRunnable implements Runnable {
	
	private Socket mClientSocket = null;
	private OutputStream mOutputStream = null;
	private InputStream mInputStream = null;
	private UploadFileRunnable mUploadFileRunnable = null;
	
	/**
	 * 用于上传文件数据的线程
	 * @author XlOwen
	 *
	 */
	private class UploadFileRunnable implements Runnable {
		private String mFilePath = null;
		private int mFileDataOffset = 0;
		
		public UploadFileRunnable(String filePath, int fileDataOffset) {
			mFilePath = filePath;
			mFileDataOffset = fileDataOffset;
		}
		
		@Override
		public void run() {
			try {
				FileInputStream fileInputStream = new FileInputStream(mFilePath);
				int sendCount = 0;
				int readCount = 0;
				byte[] fileData = new byte[1024];
				while (-1 != (readCount = fileInputStream.read(fileData, mFileDataOffset, 1024))) {
					TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]UploadFileRunnable:run: readCount = " + readCount);
					int offset = 0;
					byte[] sendData = new byte[2 + 4 + 4 + readCount];
					//文件头
					offset = DataStreamUtil.writeShort2ByteArray(TransferDefine.PROTOCOL_TYPE_TRANSFER_FILE, sendData, offset);
					offset = DataStreamUtil.writeInt2ByteArray(4 + readCount, sendData, offset);
					//返回码
					//offset = DataStreamUtil.writeInt2ByteArray(TransferDefine.PROTOCOL_ERROR_CODE_FILE, sendData, offset);
					offset = DataStreamUtil.writeInt2ByteArray(++sendCount, sendData, offset);
					//数据
					System.arraycopy(fileData, 0, sendData, offset, readCount);
					//发送
					send(sendData);
				}
				TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]UploadFileRunnable:run: readCount = " + readCount);
				
				//发送完成标识
				int offset = 0;
				byte[] sendData = new byte[2 + 4 + 4];
				//文件头
				offset = DataStreamUtil.writeShort2ByteArray(TransferDefine.PROTOCOL_TYPE_TRANSFER_FILE, sendData, offset);
				offset = DataStreamUtil.writeInt2ByteArray(4, sendData, offset);
				//返回码
				offset = DataStreamUtil.writeInt2ByteArray(TransferDefine.PROTOCOL_ERROR_CODE_FILE_FINISH, sendData, offset);
				//发送
				send(sendData);
			} catch (StopException e) {
				TTLog.e(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]UploadFileRunnable:run: catch StopException, " + e.getMessage());
			} catch (IOException e) {
				TTLog.e(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]UploadFileRunnable:run: catch IOException, " + e.getMessage());
			}
		}		
	}
	
	public UploadRunnable(Socket clientSocket) {
		mClientSocket = clientSocket;
	}
	
	@Override
	public void run() {		
		try {
			mOutputStream = mClientSocket.getOutputStream();
			mInputStream = mClientSocket.getInputStream();

			//收取数据包
			int readCount = 0;
			byte[] buffer = new byte[265];
			while (-1 != (readCount = mInputStream.read(buffer))) {
				TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]run: readCount = " + readCount);
				processRecvData(buffer);
			}
		} catch (StopException e) {
			TTLog.e(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]run: catch StopException, " + e.getMessage());
		} catch (IOException e) {
			TTLog.e(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]run: catch IOException, " + e.getMessage());
		} finally {
			close();
		}
	}
	
	/**
	 * 处理接收到的数据
	 * @param data
	 */
	private void processRecvData(byte[] data) throws StopException {
		int offset = 0;
		//解释请求头
		int protocolType = DataStreamUtil.readShortFromByteArray(data, offset);
		offset += 2;
		int dataLength = DataStreamUtil.readIntFromByteArray(data, offset);
		offset += 4;
		TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]processRecvData: protocolType = " + protocolType + ", dataLength = " + dataLength);
		
		switch (protocolType) {
		case TransferDefine.PROTOCOL_TYPE_REQUEST_FILE:
			//文件名长度
			int fileNameLength = DataStreamUtil.readIntFromByteArray(data, offset);
			offset += 4;
			TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]processRecvData: fileNameLength = " + fileNameLength);
			//文件名
			String filePath = DataStreamUtil.readStringFromByteArray(data, offset, fileNameLength);
			offset += fileNameLength;
			TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]processRecvData: filePath = " + filePath);
			//文件数据偏移
			int fileDataOffset = DataStreamUtil.readIntFromByteArray(data, offset);
			TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]processRecvData: fileDataOffset = " + fileDataOffset);
			
			//假设本地文件存在，发送确认传输到接收方
			if (null == mUploadFileRunnable) {
				mUploadFileRunnable = new UploadFileRunnable(filePath, fileDataOffset);
				(new Thread(mUploadFileRunnable)).start();
			}
		}
	}
	
	/**
	 * 是否已经连接
	 * @return
	 */
	private boolean isConnect() {
		return null == mClientSocket ? false : mClientSocket.isConnected();
	}
	
	/**
	 * 关闭连接
	 */
	private void close() {
		if(isConnect()){
			try {
				mClientSocket.close();
				mOutputStream.close();
				mInputStream.close();
				mClientSocket = null;
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
