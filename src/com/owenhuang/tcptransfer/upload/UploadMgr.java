package com.owenhuang.tcptransfer.upload;

import com.owenhuang.tcptransfer.DataStreamUtil;
import com.owenhuang.tcptransfer.TTLog;
import com.owenhuang.tcptransfer.TransferDefine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * TCP上传管理器
 * 需：
 * 1、在AndroidManifest.xml中添加com.owenhuang.tcptransfer.download.DownloadService服务
 * 2、在AndroidManifest.xml中添加android.permission.INTERNET、android.permission.ACCESS_NETWORK_STATE、android.permission.ACCESS_WIFI_STATE权限
 * 3、如果需要输出日志到本地则在添加android.permission.WRITE_EXTERNAL_STORAGE权限
 * @author XlOwen
 *
 */
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
			TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadMgr]bindService: bindResult = " + bindResult);
		}
	}
}
