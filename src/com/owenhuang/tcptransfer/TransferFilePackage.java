package com.owenhuang.tcptransfer;

/**
 * 数据包
 * @author XlOwen
 *
 */
public class TransferFilePackage extends DataPackage {
	
	private int mErrorCode;
	private int mFilePathLen;
	private String mFilePath;
	private long mFileOffset;
	
	public TransferFilePackage() {
		
	}
	
	public void setErrorCode(int errorCode) {
		mErrorCode = errorCode;
	}
	public int getErrorCode() {
		return mErrorCode;
	}
	
	public void setFilePathLen(int filePathLen) {
		mFilePathLen = filePathLen;
	}
	public int getFilePathLen() {
		return mFilePathLen;
	}
	
	public void setFilePath(String filePath) {
		mFilePath = filePath;
	}
	public String getFilePath() {
		return mFilePath;
	}
	
	public void setFileOffset(long fileOffset) {
		mFileOffset = fileOffset;
	}
	public long getFileOffset() {
		return mFileOffset;
	}
}
