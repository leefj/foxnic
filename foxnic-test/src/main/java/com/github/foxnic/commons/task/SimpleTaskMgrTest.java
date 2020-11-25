package com.github.foxnic.commons.task;

import com.github.foxnic.commons.concurrent.ThreadUtil;
import com.github.foxnic.commons.concurrent.pool.SimpleTaskManager;

public class SimpleTaskMgrTest {
	
	private static int z=0; 
	
	public static void main(String[] args) {
		Long t=System.currentTimeMillis();
		for (int i = 0; i < 50; i++) {
			SimpleTaskManager.doParallelTask(new Runnable() {
				@Override
				public void run() {
					ThreadUtil.sleep(100,100);
					z++;
					System.out.println("T("+(System.currentTimeMillis()-t)+")"+z);
					ThreadUtil.sleep(1000,500);
					
				}
			});
		}
		
		
	}

}
