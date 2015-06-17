package com.owenhuang.tcptransfer;

/**
 * 数据包
 * @author XlOwen
 *
 */
public class DataPackage {
	private static final int PROTOCOL_HEADER_LEN = 6;
	
	private int mProtocolType = -1;
	private int mDataLen = -1;
	private byte[] mHeader = null;
	private byte[] mData = null;
	
	private int mHeaderReceiveLen;
	private int mDataReceiveLen;
	
	public DataPackage(byte[] data, int srcPos, int len) {
		mHeaderReceiveLen = 0;
		mDataReceiveLen = 0;
		
		appendData(data, srcPos, len);
	}
	
	/**
	 * 是否已经接收完全
	 * @return
	 */
	public boolean isReceiveFinish() {
		return mHeaderReceiveLen >= PROTOCOL_HEADER_LEN && mDataReceiveLen >= mDataLen;
	}
	
	/**
	 * 附加数据
	 * @param srcData 源数据
	 * @param srcPos 读取数据起点
	 * @param srcLen 读取数据长度
	 * @return
	 */
	public int appendData(byte[] srcData, int srcPos, int srcLen) {
		int srcOffset = srcPos;
		int srcRestLen = srcLen;
		
		//操作头部
		int headerReadLen = 0;
		if (mHeaderReceiveLen < PROTOCOL_HEADER_LEN) {
			if (null == mHeader) {
				mHeader = new byte[PROTOCOL_HEADER_LEN];
			}
			int restHeaderLen = PROTOCOL_HEADER_LEN - mHeaderReceiveLen;
			if (restHeaderLen >= srcLen) {
				System.arraycopy(srcData, srcOffset, mHeader, mHeaderReceiveLen, srcLen);
				mHeaderReceiveLen += srcLen;
				return srcLen;
			} else {
				System.arraycopy(srcData, srcOffset, mHeader, mHeaderReceiveLen, restHeaderLen);
				mHeaderReceiveLen = PROTOCOL_HEADER_LEN;
				headerReadLen += restHeaderLen;
				srcOffset += restHeaderLen;
				srcRestLen -= restHeaderLen;
			}
		}
		
		//操作数据
		if (null == mData) {
			mProtocolType = DataStreamUtil.readShortFromByteArray(mHeader, 0);
			mDataLen = DataStreamUtil.readIntFromByteArray(mHeader, 2);
			mData = new byte[mDataLen];
		}
		int restDataLen = mDataLen - mDataReceiveLen;
		if (restDataLen >= srcRestLen) {
			System.arraycopy(srcData, srcOffset, mData, mDataReceiveLen, srcRestLen);
			mDataReceiveLen += srcLen;
			return srcLen;
		} else {
			System.arraycopy(srcData, srcOffset, mData, mDataReceiveLen, restDataLen);
			mDataReceiveLen = mDataLen;
			return headerReadLen + restDataLen;
		}
	}
}
