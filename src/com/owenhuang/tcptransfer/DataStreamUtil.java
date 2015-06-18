package com.owenhuang.tcptransfer;

public class DataStreamUtil {
	
	/**
	 * 写Short到Byte数组中
	 * @param value
	 * @param byteBuffer
	 * @param offset
	 * @return
	 */
	static public int writeShort2ByteArray(short value, byte[] byteBuffer, int offset)
	{
		byteBuffer[offset+1] = (byte) ((value >>> 8) & 0xff);
		byteBuffer[offset] = (byte) (value & 0xff);
		return offset+2;
	}		
	static public int readShortFromByteArray(byte[] byteBuffer, int offset)
	{
		int firstByte = 0x000000FF & byteBuffer[offset+1];
        int secondByte = 0x000000FF & byteBuffer[offset];
        return (firstByte << 8 | secondByte);
	}
	
	/**
	 * 写Int到Byte数组中
	 * @param value
	 * @param byteBuffer
	 * @param offset
	 * @return
	 */
	static public int writeInt2ByteArray(int value, byte[] byteBuffer, int offset)
	{
		byteBuffer[offset+3] = (byte) ((value >>> 24) & 0xff);
		byteBuffer[offset+2] = (byte) ((value >> 16) & 0xff);
		byteBuffer[offset+1] = (byte) ((value >> 8) & 0xff);
		byteBuffer[offset] = (byte) (value & 0xff);
		return offset+4;
	}
	static public int readIntFromByteArray(byte[] byteBuffer, int offset)
	{
        int firstByte = 0x000000FF & byteBuffer[offset + 3];
        int secondByte = 0x000000FF & byteBuffer[offset + 2];
        int thirdByte = 0x000000FF & byteBuffer[offset + 1];
        int fourthByte = 0x000000FF & byteBuffer[offset];
        return (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte);
	}
	
	/**
	 * 写Long到Byte数组中
	 * @param value
	 * @param byteBuffer
	 * @param offset
	 * @return
	 */
	static public int writeLong2ByteArray(long value, byte[] byteBuffer, int offset)
	{
		int byteOffset = 0;
		for (int i = 0; i < 8; i++) {
			byteOffset = (7 - i) * 8;
			byteBuffer[offset + (7-i)] = (byte) ((value >>> byteOffset) & 0xff);
        }
		return offset + 8;
	}
	static public long readLongFromByteArray(byte[] byteBuffer, int offset)
	{
		
		long value = 0L, tmpValue;
		for(int i=0; i<8 ; i++)
		{
			tmpValue = 0xFFL & byteBuffer[offset+(7-i)];
			value = value | (tmpValue << (7-i)*8);
		}
		
		return value;
	}
	
	/**
	 * 写String到Byte数组中
	 * @param value
	 * @param byteBuffer
	 * @param offset
	 * @return
	 */
	static public int writeString2ByteArray(String value, byte[] byteBuffer, int offset)
	{
		try {
			byte[] byteValue = value.getBytes("unicode");
			
			if (null == byteBuffer) {
				return byteValue.length;
			}
			
			System.arraycopy(byteValue, 0, byteBuffer, offset, + byteValue.length);
			return offset + byteValue.length;
		} catch (Exception e) {
			TTLog.e(TransferDefine.TRANSFER_LOG_TAG, "[DataTransformUtil]writeString2ByteArray: Exception msg: " + e.getMessage());
			return 0;
		}
	}
	static public String readStringFromByteArray(byte[] byteBuffer, int offset, int length)/* throws IOException*/
	{
		if(byteBuffer.length < offset + length)
		{
			TTLog.e(TransferDefine.TRANSFER_LOG_TAG, "[DataTransformUtil]readStringFromByteArray: byteBuffer.length < offset + length");
			return null;
		}
		try {
			// 根据长度在读取字符串
			String strValue = new String(byteBuffer, offset, length, "unicode");
			return strValue;
		} catch (Exception e) {
			TTLog.e(TransferDefine.TRANSFER_LOG_TAG, "[DataTransformUtil]readStringFromByteArray: Exception msg: " + e.getMessage());
			return null;
		}
	}
}
