package com.owenhuang.tcptransfer.download;

import com.owenhuang.tcptransfer.TTLog;
import com.owenhuang.tcptransfer.TransferDefine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class DownloadMgr {
	
	private Context mContext = null;
	
	private DownloadService mDownloadService = null;
	
	/**
	 * 用来管理单例
	 * @author XlOwen
	 *
	 */
	private static class DownloadMgrHolder {
		private static final DownloadMgr mInstance = new DownloadMgr();
	}

	/**
	 * 获取单例
	 * @return
	 */
	public static final DownloadMgr getInstance() {
		return DownloadMgrHolder.mInstance;
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
	
	private DownloadMgr() {}
	
	/**
	 * 绑定下载服务
	 */
	private void bindService() {
		if (null == mDownloadService) {
			Intent intent = new Intent(mContext, DownloadService.class);
			boolean bindResult = mContext.bindService(intent, new ServiceConnection() {				
				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					mDownloadService = ((DownloadService.ServiceBinder)service).getService();
				}
				
				@Override
				public void onServiceDisconnected(ComponentName name) {
									
				}
			}, Context.BIND_AUTO_CREATE);
			TTLog.d(TransferDefine.DOWNLOAD_LOG_TAG, "[DownloadMgr]bindService: bindResult = " + bindResult);
		}
	}
}
