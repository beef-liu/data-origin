package com.beef.dataorigin.web.datacommittask;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
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
import com.beef.dataorigin.web.datacommittask.DODataModificationCommitTaskCallback.DataUpdateType;
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

	private final static String[] DATA_COMMIT_TASK_BUNDLE_PRIMARY_KEY_NAMES = new String[]{"table_name", "schedule_commit_time"};
	
	/**
	 * That number of running task is 1 will simplify the process of finding tasks.
	 * If number of running task is bigger than 1, then finding tasks should assure there only 1 task of same table in pool. 
	 */
	private final static int MAX_RUNNING_THREAD = 1;
	
	private final static int WAIT_SHUTDOWN_TIMEOUT_MS = 5000;
	
	private final static int TASK_FINDER_DELAY_MS = 5000;
	private final static int TASK_FINDER_PERIOD_MS = 60000;
	private final static long TASK_FINDER_AHEAD_MS = 300000;
	
	private final static int MAX_TASK_IN_QUEUE = 32;
	
	private final static int TASK_EXECUTOR_MIN_DELAY_MS = 60000;
	
	private final static int MAX_COMMIT_TASK_IN_ONE_LOOP = 1000; 
	
	private Timer _taskFinderTimer = null;
	private ScheduledExecutorService _taskPool = null;
	
	private Object _lockForAddTaskToSchedule = new Object();
	
	private DODataModificationCommitTaskCallback _taskCallback = null;
	
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
		_taskFinderTimer.scheduleAtFixedRate(
				_bundleTaskFinder, 
				TASK_FINDER_DELAY_MS, TASK_FINDER_PERIOD_MS);
	}
	
	public void setTaskCallback(DODataModificationCommitTaskCallback taskCallback) {
		_taskCallback = taskCallback;
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
	
	public boolean isAllowModifyDataCommitTaskBundleScheduleTime(
			String table_name, long schedule_commit_time) {
		
		if(schedule_commit_time <= (System.currentTimeMillis() + TASK_FINDER_AHEAD_MS)) {
			return false;
		} else {
			DODataModificationCommitTaskBundle taskBundle = _scheduleTaskBundleMap.get(taskBundleMapKey(table_name, schedule_commit_time));
			if(taskBundle != null && taskBundle.getSchedule_commit_time() == schedule_commit_time) {
				return false;
			} else {
				return true;
			}
		}
	}

	public int modifyDataCommitTaskBundleScheduleTime(Connection conn, 
			String table_name, long schedule_commit_time, long newSchedule_commit_time
			) throws IllegalArgumentException, SQLException, InstantiationException, 
			InvocationTargetException, IllegalAccessException, IntrospectionException, ParseException {
		//update schedule_time of task which has not been committed
		int updCntOfTask = DODataModificationCommitTaskSchedulerDao.updateDataCommitTaskScheduleTime(
				conn, table_name, schedule_commit_time, newSchedule_commit_time);
		logger.info("modifyDataCommitTaskBundleScheduleTime() updated task:" + updCntOfTask);
		
		/*
		//insert new task bundle, old task bundle still be running, just will not have tasks to execute
		DODataModificationCommitTaskBundle taskBundle = new DODataModificationCommitTaskBundle();
		taskBundle.setTable_name(table_name);
		taskBundle.setSchedule_commit_time(schedule_commit_time);
		
		taskBundle = (DODataModificationCommitTaskBundle) DODataDao.searchDataByPK(
				conn, "DODataModificationCommitTaskBundle", taskBundle);
		
		DODataModificationCommitTaskBundle newTaskBundle = new DODataModificationCommitTaskBundle();
		newTaskBundle.setTable_name(table_name);
		newTaskBundle.setSchedule_commit_time(newSchedule_commit_time);
		newTaskBundle.setCommit_finish_time(0);
		newTaskBundle.setCommit_start_time(0);
		newTaskBundle.setData_row_count_of_did_commit(0);
		newTaskBundle.setData_row_count_of_total(updCntOfTask);
		newTaskBundle.setTask_bundle_status(0);
		newTaskBundle.setUpdate_time(System.currentTimeMillis());
		
		int updCntOfTaskBundle = UpdateDataDao.insertData(conn, "DODataModificationCommitTaskBundle", newTaskBundle);
		
		//add to schedule 
		addTaskToSchedule(taskBundle);
		logger.info("modifyDataCommitTaskBundleScheduleTime() table_name:" + table_name 
				+ " old schedule time:" + schedule_commit_time + " new schedule time:" + newSchedule_commit_time);
		*/
		
		int updCntOfTaskBundle = DODataModificationCommitTaskSchedulerDao.updateDataCommitTaskBundleScheduleTime(
				conn, table_name, schedule_commit_time, newSchedule_commit_time);
		
		return updCntOfTaskBundle;
	}
	
	protected TimerTask _bundleTaskFinder = new TimerTask() {
		
		@Override
		public void run() {
			findTaskBundleAndLoadIntoMap();
		}
	};
	
	protected void findTaskBundleAndLoadIntoMap() {
		try {
			Connection conn = null;
			List<DODataModificationCommitTaskBundle> bundleList = null;
			try {
				int queryTaskCount = MAX_TASK_IN_QUEUE - _scheduleTaskBundleMap.size();
				if(queryTaskCount <= 0) {
					logger.info("_bundleTaskFinder task waiting queue arrived max count:" + MAX_TASK_IN_QUEUE);
					return;
				}
				
				//logger.info("_bundleTaskFinder start finding -----");
				
				conn = DOServiceUtil.getOnEditingDBConnection();
				
				//find taskBundle and add them into task pool, order by schedule time
				bundleList = DODataModificationCommitTaskSchedulerDao.findTaskBundleWaitToExecute(
								conn, queryTaskCount, System.currentTimeMillis());
				logger.info("_bundleTaskFinder found:" + bundleList.size());
				
			} finally {
				try {
					conn.close();
				} catch(Throwable e) {
					logger.error(null, e);
				}
			}
			
			DODataModificationCommitTaskBundle taskBundle;
			for(int i = 0; i < bundleList.size(); i++) {
				taskBundle = bundleList.get(i);
			
				addTaskToSchedule(taskBundle);
			}
		} catch(Throwable e) {
			logger.error(null, e);
		}
		
	}
	
	protected void addTaskToSchedule(DODataModificationCommitTaskBundle taskBundle) {
		synchronized (_lockForAddTaskToSchedule) {
			String taskBundleMapKey = taskBundleMapKey(taskBundle.getTable_name(), taskBundle.getSchedule_commit_time());
			if(_scheduleTaskBundleMap.containsKey(taskBundleMapKey)) {
				//assure only 1 running task for the same table
				return;
			}
			
			_scheduleTaskBundleMap.put(
					taskBundleMapKey, 
					taskBundle);
			logger.info("_bundleTaskFinder added task bundle:" + taskBundle.getTable_name() + " " + taskBundle.getSchedule_commit_time());
			
			//add to thread pool
			BundleTaskExecutor taskExecutor = new BundleTaskExecutor(taskBundle, _taskCallback);
			
			long taskDelay = taskBundle.getSchedule_commit_time() - System.currentTimeMillis();
			if(taskDelay <= 0) {
				taskDelay = TASK_EXECUTOR_MIN_DELAY_MS;
			}
			
			_taskPool.schedule(taskExecutor, taskDelay, TimeUnit.MILLISECONDS);
			logger.info("_bundleTaskFinder task added into queue. " + "taskBundleMapKey:" + taskBundleMapKey + " delay:" + taskDelay + " ms ");
		}
	}
	
	protected final static String taskBundleMapKey(String table_name, long schedule_commit_time) {
		//return  table_name + "." + schedule_commit_time;
		return table_name;
	}

	protected class BundleTaskExecutor implements Callable<Long> {
		private DODataModificationCommitTaskBundle _taskBundle;
		private DODataModificationCommitTaskCallback _taskCallback;
		
		public BundleTaskExecutor(DODataModificationCommitTaskBundle taskBundle, DODataModificationCommitTaskCallback taskCallback) {
			_taskBundle = taskBundle;
			_taskCallback = taskCallback;
		} 
		
		@Override
		public Long call() throws Exception {
			logger.info("BundleTaskExecutor() table_name:" + _taskBundle.getTable_name() 
					+ " schedule_commit_time:" + _taskBundle.getSchedule_commit_time());
			long startTime = System.currentTimeMillis();

			MDBTable mDBTable = DataOriginWebContext.getDataOriginContext().getMDBTable(_taskBundle.getTable_name());
			MMetaDataUISetting metaUISetting = DataOriginWebContext.getDataOriginContext().getMMetaDataUISetting(_taskBundle.getTable_name());
			String dataClassName = metaUISetting.getDataClassName();
			Class<?> dataClass = DataOriginWebContext.getDataOriginContext().findClass(dataClassName);
			
			//find DODataModificationCommitTask for assigned table_name, schedule_commit_time
			Connection connOfProd = null;
			Connection connOfEdit = null;
			
			boolean autoCommitOfProd = false;
			boolean autoCommitOfEdit = false;
			int updCnt = 0;
			try {
				connOfEdit = DOServiceUtil.getOnEditingDBConnection();
				connOfProd = DOServiceUtil.getProductionDBConnection();
				
				autoCommitOfProd = connOfProd.getAutoCommit();
				autoCommitOfEdit = connOfEdit.getAutoCommit();
				
				connOfProd.setAutoCommit(true);
				connOfEdit.setAutoCommit(true);
				
				//update status to started
				_taskBundle.setCommit_start_time(System.currentTimeMillis());
				_taskBundle.setTask_bundle_status(DODataModificationCommitTaskSchedulerDao.TASK_BUNDLE_STATUS_STARTED);
				UpdateDataDao.updateData(connOfEdit, "DODataModificationCommitTaskBundle", _taskBundle, DATA_COMMIT_TASK_BUNDLE_PRIMARY_KEY_NAMES);

				//commit data to production DB
				List<DODataModificationCommitTask> taskList;
				DODataModificationCommitTask dataCommitTask;
				int i;
				while(true) {
					taskList = DODataModificationCommitTaskSchedulerDao.findTaskToExecute(
							connOfEdit, MAX_COMMIT_TASK_IN_ONE_LOOP, 
							_taskBundle.getTable_name(), _taskBundle.getSchedule_commit_time());
					logger.info("BundleTaskExecutor() found task to execute:" + taskList.size());
					if(taskList.size() == 0) {
						break;
					}
					
					for(i = 0; i < taskList.size(); i++) {
						dataCommitTask = taskList.get(i);

						updCnt += commitOneData(connOfProd, connOfEdit, mDBTable, dataClass, dataCommitTask);
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
					//update status to finished
					long curTime = System.currentTimeMillis();
					_taskBundle.setCommit_finish_time(curTime);
					_taskBundle.setTask_bundle_status(DODataModificationCommitTaskSchedulerDao.TASK_BUNDLE_STATUS_FINISHED);
					_taskBundle.setData_row_count_of_did_commit(updCnt);
					_taskBundle.setUpdate_time(curTime);
					
					UpdateDataDao.updateData(connOfEdit, "DODataModificationCommitTaskBundle", _taskBundle, DATA_COMMIT_TASK_BUNDLE_PRIMARY_KEY_NAMES);

					//remove from map
					_scheduleTaskBundleMap.remove(taskBundleMapKey(_taskBundle.getTable_name(), _taskBundle.getSchedule_commit_time()));
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
			}
			
			long finishTime = System.currentTimeMillis();
			long elapsedTime = finishTime - startTime;
			logger.info("BundleTaskExecutor() table_name:" + _taskBundle.getTable_name() 
					+ " schedule_commit_time:" + _taskBundle.getSchedule_commit_time() 
					+ " updated count:" + updCnt + " elapsed(ms):" + elapsedTime
					);
			
			return finishTime;
		}
		
		protected int commitOneData(Connection connOfProd, Connection connOfEdit,
				MDBTable mDBTable, Class<?> dataClass,
				DODataModificationCommitTask dataCommitTask) {
			int updCnt = 0;
			Object data = null;
			
			try {
				DataUpdateType updateType;
				if(dataCommitTask.getMod_type() == DODataModificationCommitTaskSchedulerDao.TASK_MOD_TYPE_DELETE) {
					updateType = DataUpdateType.Delete;
				} else if(dataCommitTask.getMod_type() == DODataModificationCommitTaskSchedulerDao.TASK_MOD_TYPE_UPDATE) {
					updateType = DataUpdateType.Update;
				} else {
					updateType = DataUpdateType.Insert;
				}
				
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
						updateType = DataUpdateType.Delete;
						
						StringBuilder sql = new StringBuilder();
						sql.append("delete from " + DOSqlParamUtil.quoteSqlIdentifier(dataCommitTask.getTable_name()));
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
						try {
							//insert
							updateType = DataUpdateType.Insert;
							
							updCnt = UpdateDataDao.insertData(connOfProd, mDBTable.getTableName(), data);
						} catch(SQLException sqle) {
							if(sqle.getClass().getSimpleName().equalsIgnoreCase("MySQLIntegrityConstraintViolationException")) {
								//duplicated key, then update
								updateType = DataUpdateType.Update;
								
								updCnt = UpdateDataDao.updateData(connOfProd, 
										mDBTable.getTableName(), data, mDBTable.getPrimaryKeys());
							} else {
								throw sqle;
							}
						}
					}
					
					if(_taskCallback != null) {
						if(updCnt > 0) {
							_taskCallback.didSuccessOfDataCommit(mDBTable, data, updateType);
						} else {
							_taskCallback.didFailOfDataCommit(mDBTable, data, updateType);
						}
					}
				} catch(Throwable e) {
					logger.error(null, e);
					dataCommitTask.setCommit_status(DODataModificationCommitTaskSchedulerDao.TASK_COMMIT_STATUS_FAIL);
					String errMsg = DOServiceMsgUtil.getStackTrace(e);
					if(errMsg.length() > 6000) {
						errMsg = errMsg.substring(0, 6000);
					}
					dataCommitTask.setError_msg(errMsg);
					
					if(_taskCallback != null) {
						_taskCallback.didFailOfDataCommit(mDBTable, data, updateType);
					}
				}
				
				return updCnt;
			} finally {
				try {
					if(updCnt == 0) {
						dataCommitTask.setCommit_status(DODataModificationCommitTaskSchedulerDao.TASK_COMMIT_STATUS_FAIL);
						dataCommitTask.setError_msg("Committing data to ProductionDB failed: updated count is 0");
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