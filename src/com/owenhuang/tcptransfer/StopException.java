package com.owenhuang.tcptransfer;

/**
 * 停止异常
 * @author XlOwen
 *
 */
public class StopException extends Throwable {
	private static final long serialVersionUID = 1L;
	
	private int mStatus;

	public StopException(int status, String message) {
	    super(message);
	    mStatus = status;
	}

	public StopException(int status, String message, Throwable throwable) {
	    super(message, throwable);
	    mStatus = status;
	}
	
	public int getStatus() {
		return mStatus;
	}
}
