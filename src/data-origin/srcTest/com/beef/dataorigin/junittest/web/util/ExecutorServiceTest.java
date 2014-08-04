package com.beef.dataorigin.junittest.web.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.Test;

public class ExecutorServiceTest {

	public static void main(String[] args) {
		test1();
		
		//test2();
	}
	
	protected static void test1() {
		try {
			ScheduledExecutorService taskScheduler = Executors.newScheduledThreadPool(2);

			Runnable periodTask = new Runnable() {
				@Override
				public void run() {
					log("thread scheduler --------------");
				}
			};
			
			taskScheduler.scheduleAtFixedRate(
					periodTask, 
					1000, 1000, TimeUnit.MILLISECONDS);

			for(int i = 0; i < 20; i++) {
				final int threadNum = i;
				
				taskScheduler.schedule(new Runnable() {
					@Override
					public void run() {
						try {
							log("thread num:" + threadNum);
							Thread.sleep(1000);
							log("thread num:" + threadNum + "--------");
						} catch(Throwable e) {
							e.printStackTrace();
						}
					}
				}, 5000, TimeUnit.MILLISECONDS);
			}
			
			//Thread.sleep(20000);
			//taskScheduler.shutdown();
			
			log("Task Scheduler starting shutdown ---------");
			boolean allShutdown = taskScheduler.awaitTermination(20000, TimeUnit.MILLISECONDS);
			log("Task Scheduler allShutdown: " + allShutdown);
			
			if(allShutdown) {
				//Error will occurred when invoke schedule after shutdown invoked 
				taskScheduler.scheduleAtFixedRate(
						periodTask, 
						1000, 1000, TimeUnit.MILLISECONDS);
			}
			
			Thread.sleep(5000);
			taskScheduler.shutdown();
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static ConcurrentHashMap<String, TestCallableStatus> _taskStatusMap = new ConcurrentHashMap<String, ExecutorServiceTest.TestCallableStatus>();
	protected static void test2() {
		try {
			
			final List<ScheduledFuture<TestCallableResult>> futureList = new ArrayList<ScheduledFuture<TestCallableResult>>();
			
			ScheduledExecutorService taskScheduler = Executors.newScheduledThreadPool(1);
			
			Random rand = new Random(System.currentTimeMillis());
			long delay;
			for(int i = 0; i < 10; i++) {
				final int threadNum = i;
				
				delay = rand.nextInt(10000) + 500;
				//delay = 1000;
				
				TestCallableStatus taskStatus = new TestCallableStatus();
				taskStatus.taskNum = threadNum;
				taskStatus.isStarted = false;
				_taskStatusMap.put(String.valueOf(threadNum), taskStatus);
				
				ScheduledFuture<TestCallableResult> future = taskScheduler.schedule(
						new TestCallable(threadNum, " delay:" + delay + " threadNum" + threadNum), 
						delay, TimeUnit.MILLISECONDS
						);
				
				futureList.add(future);
			}
			
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					log("---------------------------------------");
					Iterator<TestCallableStatus> taskStatusIter = _taskStatusMap.values().iterator();
					TestCallableStatus taskStatus;
					while(taskStatusIter.hasNext()) {
						taskStatus = taskStatusIter.next();
						
						if(taskStatus.isStarted) {
							log("taskStatus[" + taskStatus.taskNum + "] started -----");
						}
					}
					
					ScheduledFuture<TestCallableResult> future;
					for(int i = 0; i < futureList.size(); i++) {
						future = futureList.get(i);
						if(future.isCancelled() || future.isDone()) {
							log("future[" + i + "] " 
									+ " isDone:" + future.isDone() 
									+ " isCancelled:" + future.isCancelled());
						}
					}
					
					log("                                        ");
				}
			}, 1, 333);
			
			
			boolean waitShutdownSuccess = taskScheduler.awaitTermination(1000, TimeUnit.MILLISECONDS);
			log("waitShutdownSuccess_1:" + waitShutdownSuccess);
			
			taskScheduler.shutdown();

			waitShutdownSuccess = taskScheduler.awaitTermination(20000, TimeUnit.MILLISECONDS);
			log("waitShutdownSuccess_2:" + waitShutdownSuccess);
			
			//Thread.sleep(20000);
			timer.cancel();
			log("Task Scheduler testing end ---------");
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static class TestCallableStatus {
		public int taskNum;
		public boolean isStarted = false;
	}
	
	private static class TestCallableResult {
		public int num;
		public String desc;
		
		public TestCallableResult() {
			
		}
		
		public TestCallableResult(int num, String desc) {
			this.num = num;
			this.desc = desc;
		}
	}
	
	private static class TestCallable implements Callable<TestCallableResult> {
		private int _num;
		private String _desc;
		
		public TestCallable(int num, String desc) {
			_num = num;
			_desc = desc;
		}

		@Override
		public TestCallableResult call() throws Exception {
			log("TestCallable call() " + _desc + " -----");
			
			_taskStatusMap.get(String.valueOf(_num)).isStarted = true;
			
			Thread.sleep(1500);
			
			return new TestCallableResult(_num, _desc);
		}
		
	}
	
	private final static SimpleDateFormat DateFormatHmsSSS = new SimpleDateFormat("HH:mm:ss.SSS");
	private static void log(String msg) {
		System.out.println(DateFormatHmsSSS.format(new Date()) + " " + msg);
	}
}
