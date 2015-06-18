package com.owenhuang.tcptransfer.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

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
		private static final int FILE_BLOCK_SIZE = 8192;
		private String mFilePath = null;
		private long mFileDataOffset = 0;
		
		public UploadFileRunnable(String filePath, long fileDataOffset) {
			mFilePath = filePath;
			mFileDataOffset = fileDataOffset;
		}
		
		@Override
		public void run() {
			try {
				int offset = 0;
				int fileNameLen = DataStreamUtil.writeString2ByteArray(mFilePath, null, offset);
				byte[] transferInfoData = new byte[2 + 8 + 4 + 4 + fileNameLen + 8];	//协议类型+数据长度+错误码+文件名长度+文件名+文件偏移
				//协议类型
				offset = DataStreamUtil.writeShort2ByteArray(TransferDefine.PROTOCOL_TYPE_TRANSFER_FILE, transferInfoData, offset);
				//数据长度
				int dataLen = 8 + fileNameLen + 8;
				File uploadFile = new File(mFilePath);
				dataLen += uploadFile.length();
				TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]UploadFileRunnable:run: uploadFile.length() = " + uploadFile.length());	
				offset = DataStreamUtil.writeLong2ByteArray(dataLen, transferInfoData, offset);
				//错误码
				offset = DataStreamUtil.writeInt2ByteArray(TransferDefine.PROTOCOL_ERROR_CODE_FILE, transferInfoData, offset);
				//文件名长度
				offset = DataStreamUtil.writeInt2ByteArray(fileNameLen, transferInfoData, offset);
				//文件名
				offset = DataStreamUtil.writeString2ByteArray(mFilePath, transferInfoData, offset);
				//文件偏移
				offset = DataStreamUtil.writeLong2ByteArray(mFileDataOffset, transferInfoData, offset);
				//发送
				send(transferInfoData);				
				
				//传输文件数据
				FileInputStream fileInputStream = new FileInputStream(uploadFile);
				int readCount = 0;
				byte[] fileData = new byte[FILE_BLOCK_SIZE];
				fileInputStream.skip(mFileDataOffset);
				while (-1 != (readCount = fileInputStream.read(fileData, 0, FILE_BLOCK_SIZE))) {
					TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]UploadFileRunnable:run: readCount = " + readCount);					
					//发送
					send(fileData);
				}
				TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]UploadFileRunnable:run: readCount = " + readCount);
				
				//发送完成标识
				offset = 0;
				byte[] finishSendData = new byte[2 + 4 + 4];
				//文件头
				offset = DataStreamUtil.writeShort2ByteArray(TransferDefine.PROTOCOL_TYPE_TRANSFER_FILE, finishSendData, offset);
				offset = DataStreamUtil.writeInt2ByteArray(4, finishSendData, offset);
				//返回码
				offset = DataStreamUtil.writeInt2ByteArray(TransferDefine.PROTOCOL_ERROR_CODE_FILE_FINISH, finishSendData, offset);
				//发送
				send(finishSendData);
			} catch (StopException e) {
				TTLog.e(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]UploadFileRunnable:run: catch StopException, " + e.getMessage());
			} catch (IOException e) {
				TTLog.e(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]UploadFileRunnable:run: catch IOException, " + e.getMessage());
			} catch (Exception e) {
				TTLog.e(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]UploadFileRunnable:run: catch Exception, " + e.getMessage());
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
			byte[] buffer = new byte[256];
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
		long dataLength = DataStreamUtil.readLongFromByteArray(data, offset);
		offset += 8;
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
		TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadRunnable]close: Enter");
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
			//mOutputStream.flush();
		} catch (IOException e) {
			throw new StopException(TransferDefine.STATUS_SOCKET_SEND_FAILED, e.toString(), e);
		}
	}
}
