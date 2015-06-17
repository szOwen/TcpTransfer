package com.owenhuang.tcptransfer.upload;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.owenhuang.tcptransfer.TTLog;
import com.owenhuang.tcptransfer.TransferDefine;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class UploadService extends Service {
	
	private ThreadPoolExecutor mThreadPool;
	
	/**
	 * 自定义的Binder类
	 * @author XlOwen
	 *
	 */
	public class ServiceBinder extends Binder {
		public UploadService getService() {
			return UploadService.this;
		}
	}
	
	/**
	 * 监听连接类
	 * @author XlOwen
	 *
	 */
	private class AcceptRunnable implements Runnable {
		public static final int RUNNING_FLAG_RUNNING = 0;
		public static final int RUNNING_FLAG_EXIT = 2;
		
		private int mRunningFlag = RUNNING_FLAG_RUNNING;
		
		private ServerSocket mServerSocket;
		
		public AcceptRunnable() {}
		
		@Override
		public void run() {
			TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadService]AcceptRunnable:run: Enter");
			
			try {
				mServerSocket = new ServerSocket(TransferDefine.SOCKET_SERVER_PORT);
				
				while(RUNNING_FLAG_RUNNING == mRunningFlag) {
					try {
						TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadService]AcceptRunnable:run: mServerSocket.accept start");
						Socket clientSocket = mServerSocket.accept();
						TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadService]AcceptRunnable:run: mServerSocket.accept return");
						
						//测试
						UploadRunnable uploadRunnable = new UploadRunnable(clientSocket);
						mThreadPool.submit(uploadRunnable);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		TTLog.d(TransferDefine.TRANSFER_LOG_TAG, "[UploadService]onCreate: Enter");
		
		mThreadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		//开启监听线程
		(new Thread(new AcceptRunnable())).start();
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
