package com.beef.dataorigin.web.datacommittask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.beef.dataorigin.context.data.MDBTable;
import com.beef.dataorigin.context.data.MMetaDataUISetting;
import com.beef.dataorigin.setting.msg.DOServiceMsg;
import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.beef.dataorigin.web.dao.DODataDao;
import com.beef.dataorigin.web.data.DODataModificationCommitTask;
import com.beef.dataorigin.web.data.DODataModificationCommitTaskBundle;
import com.beef.dataorigin.web.datacommittask.dao.DODataModificationCommitTaskSchedulerDao;
import com.beef.dataorigin.web.util.DOServiceMsgUtil;
import com.beef.dataorigin.web.util.DOServiceUtil;
import com.beef.dataorigin.web.util.DOSqlParamUtil;
import com.salama.service.clouddata.util.dao.QueryDataDao;
import com.salama.service.clouddata.util.dao.UpdateDataDao;
import com.salama.util.db.JDBCUtil;

/**
 * only use single thread to execute task
 * @author XingGu Liu
 *
 */
public class DODataModificationCommitTaskScheduler {
	private final static Logger logger = Logger.getLogger(DODataModificationCommitTaskScheduler.class);
	
	/**
	 * That number of running task is 1 will simplify the process of finding tasks.
	 * If number of running task is bigger than 1, then finding tasks should assure there only 1 task of same table in pool. 
	 */
	private final static int MAX_RUNNING_THREAD = 1;
	
	private final static int WAIT_SHUTDOWN_TIMEOUT_MS = 5000;
	
	private final static int TASK_FINDER_DELAY_MS = 5000;
	private final static int TASK_FINDER_PERIOD_MS = 60000;
	
	private final static int MAX_TASK_IN_QUEUE = 32;
	
	private final static int TASK_EXECUTOR_MIN_DELAY_MS = 180000;
	
	private final static int MAX_COMMIT_TASK_IN_ONE_LOOP = 1000; 
	
	private Timer _taskFinderTimer = null;
	private ScheduledExecutorService _taskPool = null;
	
	/**
	 * key: table_name (it means there is only 1 running task of the same table)
	 */
	private ConcurrentHashMap<String, DODataModificationCommitTaskBundle> _scheduleTaskBundleMap = 
			new ConcurrentHashMap<String, DODataModificationCommitTaskBundle>();
	
	public DODataModificationCommitTaskScheduler() {
		logger.info("DODataModificationCommitTaskScheduler()" 
				+ " MAX_RUNNING_THREAD:" + MAX_RUNNING_THREAD
				+ " TASK_FINDER_DELAY_MS:" + TASK_FINDER_DELAY_MS
				+ " TASK_FINDER_PERIOD_MS:" + TASK_FINDER_PERIOD_MS
				);

		_taskPool = Executors.newScheduledThreadPool(MAX_RUNNING_THREAD);

		_taskFinderTimer = new Timer();
		_taskFinderTimer.schedule(
				_bundleTaskFinder, 
				TASK_FINDER_DELAY_MS, TASK_FINDER_DELAY_MS);
	}
	
	public void stopSchedule() throws InterruptedException {
		logger.info("stopSchedule() ------------");

		//stop timer
		_taskFinderTimer.cancel();

		//shutdown task pool
		_taskPool.shutdown();
		boolean isAllCompleted = _taskPool.awaitTermination(WAIT_SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		logger.info("stopSchedule() isAllCompleted:" + isAllCompleted + " waiting_timeout(ms):" + WAIT_SHUTDOWN_TIMEOUT_MS);
	}

	protected TimerTask _bundleTaskFinder = new TimerTask() {
		
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
				
				//find taskBundle and add them into task pool, order by schedule time
				List<DODataModificationCommitTaskBundle> bundleList = 
						DODataModificationCommitTaskSchedulerDao.findTaskBundleWaitToExecute(
								conn, queryTaskCount);
				logger.info("_bundleTaskFinder found:" + bundleList.size());
				
				DODataModificationCommitTaskBundle taskBundle;
				long taskDelay;
				String taskBundleMapKey;
				for(int i = 0; i < bundleList.size(); i++) {
					taskBundle = bundleList.get(i);
					taskBundleMapKey = taskBundleMapKey(taskBundle.getTable_name(), taskBundle.getSchedule_commit_time());
					if(_scheduleTaskBundleMap.containsKey(taskBundleMapKey)) {
						//assure only 1 running task for the same table
						continue;
					}
					
					_scheduleTaskBundleMap.put(
							taskBundleMapKey, 
							taskBundle);
					
					//add to thread pool
					BundleTaskExecutor taskExecutor = new BundleTaskExecutor(
							taskBundle.getTable_name(), taskBundle.getSchedule_commit_time());
					
					taskDelay = taskBundle.getSchedule_commit_time() - System.currentTimeMillis();
					if(taskDelay <= 0) {
						taskDelay = TASK_EXECUTOR_MIN_DELAY_MS;
					}
					
					_taskPool.schedule(taskExecutor, taskDelay, TimeUnit.MILLISECONDS);
					logger.info("_bundleTaskFinder task added into queue. " + "taskBundleMapKey:" + taskBundleMapKey + " delay:" + taskDelay + " ms ");
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
	
	protected final static String taskBundleMapKey(String table_name, long schedule_commit_time) {
		//return  table_name + "." + schedule_commit_time;
		return table_name;
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
			logger.info("BundleTaskExecutor() table_name:" + _table_name + " schedule_commit_time:" + _schedule_commit_time);
			long startTime = System.currentTimeMillis();

			//find DODataModificationCommitTask for assigned table_name, schedule_commit_time
			Connection connOfProd = null;
			Connection connOfEdit = null;
			
			boolean autoCommitOfProd = false;
			boolean autoCommitOfEdit = false;
			try {
				connOfProd = DOServiceUtil.getOnEditingDBConnection();
				connOfEdit = DOServiceUtil.getProductionDBConnection();
				
				autoCommitOfProd = connOfProd.getAutoCommit();
				autoCommitOfEdit = connOfEdit.getAutoCommit();
				
				connOfProd.setAutoCommit(true);
				connOfEdit.setAutoCommit(true);
				
				//find tasks until all did
				MDBTable mDBTable = DataOriginWebContext.getDataOriginContext().getMDBTable(_table_name);
				MMetaDataUISetting metaUISetting = DataOriginWebContext.getDataOriginContext().getMMetaDataUISetting(_table_name);
				String dataClassName = metaUISetting.getDataClassName();
				Class<?> dataClass = DataOriginWebContext.getDataOriginContext().findClass(dataClassName);
				
				List<DODataModificationCommitTask> taskList;
				DODataModificationCommitTask dataCommitTask;
				int i;
				while(true) {
					taskList = DODataModificationCommitTaskSchedulerDao.findTaskToExecute(
							connOfEdit, MAX_COMMIT_TASK_IN_ONE_LOOP, 
							_table_name, _schedule_commit_time);
					logger.info("BundleTaskExecutor() found commit task:" + taskList.size());
					if(taskList.size() == 0) {
						break;
					}
					
					for(i = 0; i < taskList.size(); i++) {
						dataCommitTask = taskList.get(i);

						commitOneData(connOfProd, connOfEdit, mDBTable, dataClass, dataCommitTask);
					}
				}
			} catch(Throwable e) {
				logger.error(null, e);
			} finally {
				try {
					connOfProd.setAutoCommit(autoCommitOfProd);
				} catch(Throwable e) {
					logger.error(null, e);
				}
				try {
					connOfProd.close();
				} catch(Throwable e) {
					logger.error(null, e);
				}
				try {
					connOfEdit.setAutoCommit(autoCommitOfEdit);
				} catch(Throwable e) {
					logger.error(null, e);
				}
				try {
					connOfEdit.close();
				} catch(Throwable e) {
					logger.error(null, e);
				}
				
				//remove from map
				try {
					_scheduleTaskBundleMap.remove(taskBundleMapKey(_table_name, _schedule_commit_time));
				} catch(Throwable e) {
					logger.error(null, e);
				}
			}
			
			long finishTime = System.currentTimeMillis();
			long elapsedTime = finishTime - startTime;
			logger.info("BundleTaskExecutor() table_name:" + _table_name + " schedule_commit_time:" + _schedule_commit_time + " elapsed(ms):" + elapsedTime);
			
			return finishTime;
		}
		
		protected int commitOneData(Connection connOfProd, Connection connOfEdit,
				MDBTable mDBTable, Class<?> dataClass,
				DODataModificationCommitTask dataCommitTask) {
			int updCnt = 0;
			Object data = null;
			
			try {
				try {
					if(dataCommitTask.getMod_type() != DODataModificationCommitTaskSchedulerDao.TASK_MOD_TYPE_DELETE) {
						//insert or update
						//find data in OnEditingDB
						StringBuilder sql = new StringBuilder();
						sql.append("select * from " + DOSqlParamUtil.quoteSqlIdentifier(dataCommitTask.getTable_name()));
						sql.append(" where " + dataCommitTask.getSql_primary_key());
						
						PreparedStatement stmtOfEdit = null;
						try {
							stmtOfEdit = connOfEdit.prepareStatement(sql.toString());
							ResultSet rs = stmtOfEdit.executeQuery();
							boolean isIgnorePropertiesNotExist = true;
							if(rs.next()) {
								data = JDBCUtil.ResultSetToData(rs, dataClass, isIgnorePropertiesNotExist);
							} else {
								dataCommitTask.setCommit_status(DODataModificationCommitTaskSchedulerDao.TASK_COMMIT_STATUS_FAIL);
								dataCommitTask.setError_msg("Data to update or insert into ProductionDB does not exist in OnEditingDB");
								return updCnt;
							}
						} finally {
							try {
								stmtOfEdit.close();
							} catch(Throwable e) {
								logger.error(null, e);
							}
						}
					}
					
					if(dataCommitTask.getMod_type() == DODataModificationCommitTaskSchedulerDao.TASK_MOD_TYPE_DELETE) {
						//delete
						StringBuilder sql = new StringBuilder();
						sql.append("delete * from " + DOSqlParamUtil.quoteSqlIdentifier(dataCommitTask.getTable_name()));
						sql.append(" where " + dataCommitTask.getSql_primary_key());
						
						PreparedStatement stmtOfProd = null;
						try {
							stmtOfProd = connOfProd.prepareStatement(sql.toString());
							updCnt = stmtOfProd.executeUpdate();
						} finally {
							try {
								stmtOfProd.close();
							} catch(Throwable e) {
								logger.error(null, e);
							}
						}
					} else {
						//insert or update
						if(dataCommitTask.getMod_type() == 0) {
							//update
							updCnt = UpdateDataDao.updateData(connOfProd, 
									_table_name, data, mDBTable.getPrimaryKeys());
						} else {
							//insert
							updCnt = UpdateDataDao.insertData(connOfProd, _table_name, data);
						}
					}
				} catch(Throwable e) {
					logger.error(null, e);
					dataCommitTask.setCommit_status(DODataModificationCommitTaskSchedulerDao.TASK_COMMIT_STATUS_FAIL);
					dataCommitTask.setError_msg(DOServiceMsgUtil.getStackTrace(e).substring(0, 6000));
				}
				
				return updCnt;
			} finally {
				try {
					if(updCnt == 0) {
						dataCommitTask.setCommit_status(DODataModificationCommitTaskSchedulerDao.TASK_COMMIT_STATUS_FAIL);
						dataCommitTask.setError_msg("Committing data to ProductionDB failed: updated count is 0");
						return updCnt;
					}
					
					if(dataCommitTask.getCommit_status() != DODataModificationCommitTaskSchedulerDao.TASK_COMMIT_STATUS_FAIL) {
						dataCommitTask.setCommit_status(DODataModificationCommitTaskSchedulerDao.TASK_COMMIT_STATUS_SUCCESS);
						dataCommitTask.setError_msg(null);
						dataCommitTask.setCommit_time(System.currentTimeMillis());
					}
					
					int taskStatusUpdCnt = DODataModificationCommitTaskSchedulerDao.updateDataCommitTaskStatus(connOfEdit, dataCommitTask);
					if(taskStatusUpdCnt == 0) {
						logger.error("updateDataCommitTaskStatus() failed");
					}
				} catch(Throwable e) {
					logger.error("updateDataCommitTaskStatus() failed", e);
				}
			}
		}
		
	}
	
}