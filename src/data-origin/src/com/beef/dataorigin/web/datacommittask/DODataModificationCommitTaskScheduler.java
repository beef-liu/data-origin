package com.beef.dataorigin.web.datacommittask;

import java.sql.Connection;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.beef.dataorigin.web.data.DODataModificationCommitTaskBundle;
import com.beef.dataorigin.web.datacommittask.dao.DODataModificationCommitTaskSchedulerDao;
import com.beef.dataorigin.web.util.DOServiceUtil;

/**
 * only use single thread to execute task
 * @author XingGu Liu
 *
 */
public class DODataModificationCommitTaskScheduler {
	private final static Logger logger = Logger.getLogger(DODataModificationCommitTaskScheduler.class);
	
	private final static int MAX_RUNNING_THREAD = 4;
	private final static int WAIT_SHUTDOWN_TIMEOUT_MS = 5000;
	
	private final static int TASK_FINDER_DELAY_MS = 5000;
	private final static int TASK_FINDER_PERIOD_MS = 60000;
	
	private final static int MAX_TASK_IN_QUEUE = 32;
	
	private final static int TASK_EXECUTOR_MIN_DELAY_MS = 180000;
	
	private ScheduledExecutorService _taskScheduler = null;
	
	private ConcurrentHashMap<String, DODataModificationCommitTaskBundle> _scheduleTaskBundleMap = 
			new ConcurrentHashMap<String, DODataModificationCommitTaskBundle>();
	
	public DODataModificationCommitTaskScheduler() {
		logger.info("DODataModificationCommitTaskScheduler()" 
				+ " MAX_RUNNING_THREAD:" + MAX_RUNNING_THREAD
				+ " TASK_FINDER_DELAY_MS:" + TASK_FINDER_DELAY_MS
				+ " TASK_FINDER_PERIOD_MS:" + TASK_FINDER_PERIOD_MS
				);

		//in scheduled thread pool, there's a special one that is called taskFinder to refresh committing task
		//other threads are BundleTaskExecutor that are created by the taskFinder 
		_taskScheduler = Executors.newScheduledThreadPool(MAX_RUNNING_THREAD);
	
		_taskScheduler.scheduleAtFixedRate(
				_bundleTaskFinder, 
				TASK_FINDER_DELAY_MS, TASK_FINDER_PERIOD_MS, TimeUnit.MILLISECONDS);
	}
	
	public void stopSchedule() throws InterruptedException {
		logger.info("stopSchedule() ------------");
		_taskScheduler.shutdown();

		boolean isAllCompleted = _taskScheduler.awaitTermination(WAIT_SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		logger.info("stopSchedule() isAllCompleted:" + isAllCompleted + " waiting_timeout(ms):" + WAIT_SHUTDOWN_TIMEOUT_MS);
	}
	
	protected Runnable _bundleTaskFinder = new Runnable() {
		
		@Override
		public void run() {
			Connection conn = null;
			try {
				int queryTaskCount = MAX_TASK_IN_QUEUE - _scheduleTaskBundleMap.size();
				if(queryTaskCount <= 0) {
					logger.info("_bundleTaskFinder task waiting queue arrived max count:" + MAX_TASK_IN_QUEUE);
					return;
				}
				
				logger.info("_bundleTaskFinder start finding -----");
				
				conn = DOServiceUtil.getOnEditingDBConnection();
				
				List<DODataModificationCommitTaskBundle> bundleList = 
						DODataModificationCommitTaskSchedulerDao.findTaskBundleWaitToExecute(
								conn, queryTaskCount);
				logger.info("_bundleTaskFinder found:" + bundleList.size());
				
				DODataModificationCommitTaskBundle taskBundle;
				long taskDelay;
				String taskBundleKey;
				for(int i = 0; i < bundleList.size(); i++) {
					taskBundle = bundleList.get(i);
					
					taskBundleKey = taskBundleKey(taskBundle.getTable_name(), taskBundle.getSchedule_commit_time()); 
					_scheduleTaskBundleMap.put(
							taskBundleKey, 
							taskBundle);
					
					//add to thread pool
					BundleTaskExecutor taskExecutor = new BundleTaskExecutor(
							taskBundle.getTable_name(), taskBundle.getSchedule_commit_time());
					
					taskDelay = taskBundle.getSchedule_commit_time() - System.currentTimeMillis();
					if(taskDelay <= 0) {
						taskDelay = TASK_EXECUTOR_MIN_DELAY_MS;
					}
					
					_taskScheduler.schedule(taskExecutor, taskDelay, TimeUnit.MILLISECONDS);
					logger.info("_bundleTaskFinder task added into queue. " + "task_key:" + taskBundleKey + " delay:" + taskDelay + " ms ");
				}
				
			} catch(Throwable e) {
				logger.error(null, e);
			} finally {
				try {
					conn.close();
				} catch(Throwable e) {
					logger.error(null, e);
				}
			}
		}
	};
	
	protected final static String taskBundleKey(String table_name, long schedule_commit_time) {
		return  table_name + "." + schedule_commit_time;
	}

	protected class BundleTaskExecutor implements Callable<Long> {
		private String _table_name;
		private long _schedule_commit_time = 0;

		public BundleTaskExecutor(String table_name, long schedule_commit_time) {
			_table_name = table_name;
			_schedule_commit_time = schedule_commit_time;
		} 
		
		@Override
		public Long call() throws Exception {
			
			
			long finishTime = System.currentTimeMillis();
			return finishTime;
		}
		
	}
	
}