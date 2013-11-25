package com.productivio;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database extends SQLiteOpenHelper{
	
	public static final String TABLE_TASK = "tasks";
	public static final String COLUMN_ID = "task_id";
	public static final String COLUMN_TASKDESC = "task_desc";
	public static final String COLUMN_TASKPRIO = "task_prio";
	public static final String COLUMN_TASKSTATUS = "task_status";
	public static final String COLUMN_TASKDATE_START = "task_date_start";
	public static final String COLUMN_TASKDATE_FINISH = "task_finish";
	public static final String COLUMN_TASKPARENT_ID = "task_parent_id";

	private static final String DATABASE_NAME = "productivio.db";
	private static final int DATABASE_VERSION = 2;
	
	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
				+ TABLE_TASK + "( " 
				+ COLUMN_ID		+ " integer primary key autoincrement, " 
				+ COLUMN_TASKDESC	+ " text, "
				+ COLUMN_TASKPRIO	+ " integer, "
				+ COLUMN_TASKSTATUS	+ " text, "
				+ COLUMN_TASKDATE_START	+ " long, "
				+ COLUMN_TASKDATE_FINISH	+ " long, "
				+ COLUMN_TASKPARENT_ID	+ " integer);";
				


	public Database(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(Database.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
		onCreate(db);
	}

}
