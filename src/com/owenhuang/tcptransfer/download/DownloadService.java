package com.owenhuang.tcptransfer.download;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.owenhuang.tcptransfer.TTLog;
import com.owenhuang.tcptransfer.TransferDefine;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class DownloadService extends Service {
	
	private ThreadPoolExecutor mThreadPool;
	
	/**
	 * 自定义的Binder类
	 * @author XlOwen
	 *
	 */
	public class ServiceBinder extends Binder {
		public DownloadService getService() {
			return DownloadService.this;
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[DownloadService]onCreate: Enter");

		mThreadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		
		//测试
		DownloadTask downloadTask = new DownloadTask();
		downloadTask.setHost(TransferDefine.SOCKET_SERVER_HOST);
		downloadTask.setPort(TransferDefine.SOCKET_SERVER_PORT);
		DownloadRunnable downloadRunnable = new DownloadRunnable(downloadTask);
		mThreadPool.submit(downloadRunnable);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new ServiceBinder();
	}

}
