package com.productivio;



import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TaskDatasource {
	// Database fields
		private static SQLiteDatabase database;
		private static Database dbHelper;
		private static String[] allColumns = { Database.COLUMN_ID, 
										Database.COLUMN_TASKDESC,
										Database.COLUMN_TASKPRIO,
										Database.COLUMN_TASKSTATUS,
										Database.COLUMN_TASKDATE_START,
										Database.COLUMN_TASKDATE_FINISH,
										Database.COLUMN_TASKPARENT_ID };
		//static constructor initialisation
		private static boolean initialised;
		
		public  TaskDatasource(Context context) {
			dbHelper = new Database(context);
		}
		
		public static void initialise(Context context){
			if(!initialised) dbHelper = new Database(context);
			initialised = true;
		}

		public static void open() throws SQLException {
			database = dbHelper.getWritableDatabase();
		}
		public static void openReadOnly() throws SQLException {
			database = dbHelper.getReadableDatabase();
		}

		public static void close() {
			dbHelper.close();
		}

		public static ContentValues taskToCValues(Task task){
			ContentValues values = new ContentValues();
			if(task.getIndex()==-1)values.putNull(Database.COLUMN_ID); 
			else values.put(Database.COLUMN_ID, task.getIndex());
			values.put(Database.COLUMN_TASKDESC, task.getDescription());
			values.put(Database.COLUMN_TASKPRIO, task.getPriority());
			values.put(Database.COLUMN_TASKSTATUS, task.getStatus().toString());
			if(task.getStartDate()==null) values.putNull(Database.COLUMN_TASKDATE_START);
			else values.put(Database.COLUMN_TASKDATE_START, task.getStartDate());
			if(task.getCompletionDate()==null) values.putNull(Database.COLUMN_TASKDATE_FINISH);
			else values.put(Database.COLUMN_TASKDATE_FINISH, task.getCompletionDate());
			values.put(Database.COLUMN_TASKPARENT_ID, task.getSuperTask());
			//insert into db

			return values;
		}

		public static int insertTask(Task task){
			ContentValues newTask = taskToCValues(task);
			open();
			int insertId = (int)database.insert(Database.TABLE_TASK, Database.COLUMN_TASKPARENT_ID,newTask);
			task.setIndex(insertId);
			close();
			return insertId;
		}
		
		public static void updateTask(Task task){
			open();
			database.update(Database.TABLE_TASK,taskToCValues(task),Database.COLUMN_ID+"=" +task.getIndex(),null);
			close();
		}
		public static void updateTask(Task task, boolean speedHack){
			if(!speedHack) open();
			database.update(Database.TABLE_TASK,taskToCValues(task),Database.COLUMN_ID+"=" +task.getIndex(),null);
			if(!speedHack) close();
		}

		public static void deleteTask(Task task) {
			long id = task.getIndex();
			open();
			System.out.println("Task deleted with id: " + id);
			database.delete(Database.TABLE_TASK, Database.COLUMN_ID
					+ " = " + id, null);
			//always think of the children
			close();
		}
		
		public static void deleteAllTasks(){
			open();
			System.out.println("All tasks to be deleted");
			database.delete(Database.TABLE_TASK, null, null);
			close();
			
			
		}
		//might not get used
		public static ArrayList<Task> getAllTasks() {
			ArrayList<Task> tasks = new ArrayList<Task>();
			openReadOnly();
			Cursor cursor = database.query(Database.TABLE_TASK,
					allColumns, null, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Task task = cursorToTask(cursor);
				tasks.add(task);
				cursor.moveToNext();
			}
			// Make sure to close the cursor
			cursor.close();
			close();
			return tasks;
		}
		//very important
		public static ArrayList<Task> getChildren(int index, boolean conPerfHack){
			ArrayList<Task> children = new ArrayList<Task>();
			//connection performance hack
			//if boolean is set to true, a recursive get children call is being done
			//don't waste time opening and closing connections
			if(!conPerfHack) openReadOnly();
			Cursor cursor = database.query(Database.TABLE_TASK,
					allColumns, Database.COLUMN_TASKPARENT_ID +"="+index, null, null, null, Database.COLUMN_TASKPRIO +" ASC");
			boolean success = cursor.moveToFirst();
			
			while (success) {
				Task task = cursorToTask(cursor);
				children.add(task);
				 success = cursor.moveToNext();
				 if(success) Log.d("cursor","working");
				 else Log.d("cursor","not working");
			}
			// Make sure to close the cursor
			cursor.close();
			if(!conPerfHack)  close();
			return children;
		}

		private static Task cursorToTask(Cursor cursor) {
			// has to be built recursively
			Task task = new Task(cursor.getLong(4),cursor.getString(1),(int)cursor.getLong(2),(int)cursor.getLong(0));
			task.setStatus(Task.STATUS.valueOf(cursor.getString(3)));
			task.setCompletionDate(cursor.getLong(5));
			//parenthood??			
			task.setSuperTask((int)cursor.getLong(6));
			
			return task;
		}
		
}


