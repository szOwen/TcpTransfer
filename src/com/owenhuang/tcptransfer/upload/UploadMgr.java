package com.owenhuang.tcptransfer.upload;

import com.owenhuang.tcptransfer.TTLog;
import com.owenhuang.tcptransfer.TransferDefine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class UploadMgr {
	
	private Context mContext = null;
	
	private UploadService mUploadService = null;
	
	/**
	 * 用来管理单例
	 * @author XlOwen
	 *
	 */
	private static class UploadMgrHolder {
		private static final UploadMgr mInstance = new UploadMgr();
	}

	/**
	 * 获取单例
	 * @return
	 */
	public static final UploadMgr getInstance() {
		return UploadMgrHolder.mInstance;
	}
	
	/**
	 * 初始化
	 * @param context
	 */
	public void init(Context context) {
		if (null != mContext) {
			return;
		}
		
		TTLog.setLogLevel(TTLog.LOG_BOTH);
		mContext = context.getApplicationContext();
		
		bindService();
	}
	
	private UploadMgr() {}
	
	/**
	 * 绑定下载服务
	 */
	private void bindService() {
		if (null == mUploadService) {
			Intent intent = new Intent(mContext, UploadService.class);
			boolean bindResult = mContext.bindService(intent, new ServiceConnection() {				
				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					mUploadService = ((UploadService.ServiceBinder)service).getService();
				}
				
				@Override
				public void onServiceDisconnected(ComponentName name) {
									
				}
			}, Context.BIND_AUTO_CREATE);
			TTLog.d(TransferDefine.UPLOAD_LOG_TAG, "[UploadMgr]bindService: bindResult = " + bindResult);
		}
	}
}
