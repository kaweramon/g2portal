package com.g2soft.g2portal.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.g2soft.g2portal.database.AppsBean;

public class ConnectToDBFutureTask {
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private AppsBean appsBean;
	private G2AppsManager g2AppsManager;
	
	public Future<Boolean> isConnect() {
		return executor.submit(() -> {
			Thread.sleep(5000);
			if (this.appsBean == null) {
				this.appsBean = new AppsBean();
			}
			
			if (g2AppsManager == null) {
				g2AppsManager = new G2AppsManager();
			}
			
			return appsBean.connectToDB();
		});
	}
}
