package com.owenhuang.tcptransfer;

/**
 * 数据包
 * @author XlOwen
 *
 */
public class DataPackage {
	private static final int PROTOCOL_HEADER_LEN = 6;
	
	private int mProtocolType = -1;
	private long mDataLen = -1;
	private byte[] mHeader = null;
	
	private int mHeaderReceiveLen;
	private long mDataReceiveLen;
	
	public DataPackage() {
		mHeaderReceiveLen = 0;
		mDataReceiveLen = 0;
	}
	
	/**
	 * 获取协议号
	 * @return
	 */
	public int getProtocolType() {
		return mProtocolType;
	}
	public void setProtocolType(int protocolType) {
		mProtocolType = protocolType;
	}
	
	/**
	 * 获取数据长度
	 * @return
	 */
	public long getDataLen() {
		return mDataLen;
	}
	public void setDataLen(long dataLen) {
		mDataLen = dataLen;
	}
	
	/**
	 * 已获取数据长度
	 * @return
	 */
	public long getDataReceiveLen() {
		return mDataReceiveLen;
	}
	public void setDataReceiveLen(long dataReceiveLen) {
		mDataReceiveLen = dataReceiveLen;
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
	public int appendHeaderData(byte[] srcData, int srcPos, int srcLen) {
		//操作头部
		if (mHeaderReceiveLen < PROTOCOL_HEADER_LEN) {
			if (null == mHeader) {
				mHeader = new byte[PROTOCOL_HEADER_LEN];
			}
			int restHeaderLen = PROTOCOL_HEADER_LEN - mHeaderReceiveLen;
			if (restHeaderLen >= srcLen) {
				System.arraycopy(srcData, srcPos, mHeader, mHeaderReceiveLen, srcLen);
				mHeaderReceiveLen += srcLen;
				return srcLen;
			} else {
				System.arraycopy(srcData, srcPos, mHeader, mHeaderReceiveLen, restHeaderLen);
				mHeaderReceiveLen = PROTOCOL_HEADER_LEN;
				return restHeaderLen;
			}
		} else {
			return 0;
		}
	}
}
