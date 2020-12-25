package com.github.foxnic.dao.sql.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import com.github.foxnic.commons.concurrent.task.SimpleTaskManager;

 


/**
 * 这个类只是方便在开发过程中修改SQL立即生效，没有其他意义
 * @author fangjieli
 * */
class SQLMonitor {

//	public static void main(String[] args) {
//		SQLMonitor sm=new SQLMonitor(null);
//		sm.addWatch(new File("D:\\devtoosl\\git\\local\\tity-module-system\\system-service\\target\\classes\\org\\tity\\system\\deploy"));
//		sm.addWatch(new File("D:\\log\\1\\aa\\deploy"));
//		sm.start();
//	}
	
	private WatchService watcher=null;
	private TQLCache cache=null;
	public   SQLMonitor(TQLCache cache) {
		this.cache=cache;
		try {
			watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int delayTaskId=-1;
	
	private ArrayList<String> modifiedFilePath=null;
	private ArrayList<String> modifiedPkgs=null;
	
	public void start()
	{
		while (true) {
			// 获取下一个文件改动事件
			WatchKey key=null;
			try {
				key = watcher.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (WatchEvent<?> event : key.pollEvents()) {
				Object ctx=event.context();
				Path paths  = (Path)ctx;
				String filename=paths.getFileName().toFile().getName();
				if(!filename.toLowerCase().endsWith(".tql") && !filename.toLowerCase().endsWith(".sql")) {
					continue;
				}
				
				if(modifiedFilePath==null) {
					modifiedFilePath=new ArrayList<>();
				}
				if(modifiedPkgs==null) {
					modifiedPkgs=new ArrayList<>();
				}
				
				//定位最后修改的文件
				for (int i = 0; i < watchingDirPath.size(); i++) {
					String  dirPath = watchingDirPath.get(i);
					
					File file=new File(dirPath+"/"+filename);
					if(!file.exists()) {
						continue;
					}
					if(!file.isFile()) {
						continue;
					}

					if(!modifiedFilePath.contains(file.getAbsolutePath()))
					{
						modifiedFilePath.add(file.getAbsolutePath());
						modifiedPkgs.add(watchingPkgs.get(i));
					}
				}
					
//				if(sqlRefreshFuture!=null) 
//				{
//					sqlRefreshFuture.cancel(false);
//				}
//				sqlRefreshFuture=schedulePool.scheduleWithFixedDelay(sqlRefreshTask, 500, 500, TimeUnit.MILLISECONDS);
				
				SimpleTaskManager tm=new SimpleTaskManager(1);
				delayTaskId=tm.doDelayTask(new Runnable() {
					@Override
					public void run() {
						handleModifiedSQLFiles();
					}
				}, 500);
				
				
				
			}
			// 重设WatchKey
			boolean valid = key.reset();
			// 如果重设失败，退出监听
			if (!valid) {
				break;
			}
		}
	}
	
	
	
	private synchronized void handleModifiedSQLFiles() {
		
		if(modifiedFilePath==null)
		{
			return;
		}
		
		for (int i = 0; i < modifiedFilePath.size(); i++) {
			String path=modifiedFilePath.get(i);
			String pkg=modifiedPkgs.get(i);

			File file=new File(path);
 
			if(!file.exists()) {
				continue;
			}
			if(!file.isFile()) {
				continue;
			}
			String cnt=null;
			try {
//				cnt = FileUtil.readString(file);
				cnt = FileUtils.readFileToString(file, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			if(cnt==null) {
				continue;
			}
			
			try {
				cache.parse(pkg,file, cnt,true);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("SQL Parse Error : "+e.getLocalizedMessage());
			}
		}
		modifiedFilePath=null;
		modifiedPkgs=null;
	}
 
	private ArrayList<String> watchingDirPath = new ArrayList<String>();
	private ArrayList<String> watchingPkgs = new ArrayList<String>();
	
	public void addWatch(String pkg,File file) {
		
		if(watchingDirPath.contains(file.getAbsolutePath())) {
			return;
		}
		//
		watchingDirPath.add(file.getAbsolutePath());
		watchingPkgs.add(pkg);
		try {
			Path path=Paths.get(file.getAbsolutePath());
			path.register(watcher
			        , StandardWatchEventKinds.ENTRY_CREATE
			        , StandardWatchEventKinds.ENTRY_MODIFY
			        , StandardWatchEventKinds.ENTRY_DELETE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

}
