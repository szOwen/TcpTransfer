package com.owenhuang.tcptransfer;

/**
 * 传输常量定义
 * 0~199为传输中的状态
 * 200~299为传输完成的状态
 * 300~599为传输错误的状态
 * @author XlOwen
 *
 */
public class TransferDefine {
	
	public static final String DOWNLOAD_LOG_TAG = "DownloadLogTag";
	public static final String UPLOAD_LOG_TAG = "UploadLogTag";
	
	/**
	 * 服务器的监听端口号
	 */
	public static final int SOCKET_SERVER_PORT = 53467;
	
	/**
	 * Socket连接失败
	 */
	public static final int STATUS_SOCKET_CONNECT_FAILED = 300;
	/**
	 * Socket获取IO失败
	 */
	public static final int STATUS_SOCKET_GET_IO_FAILED = 301;
	/**
	 * Socket关闭失败
	 */
	public static final int STATUS_SOCKET_CLOSE_FAILED = 302;
	/**
	 * Socket没有连接
	 */
	public static final int STATUS_SOCKET_NOT_CONNECT = 303;
	/**
	 * Socket发送数据失败
	 */
	public static final int STATUS_SOCKET_SEND_FAILED = 304;
}
