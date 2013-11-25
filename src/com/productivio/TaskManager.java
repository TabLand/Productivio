package com.productivio;

import java.util.ArrayList;

import android.content.Context;

public class TaskManager {
	private ArrayList<Task> tasks;
	private TaskDatasource datasource;
	private ArrayList<Task> taskTree;
	//task sharing between activities
	private static Task parentSingleton;
	//this class includes two task lists
	//one linear
	//one based on a traversable tree structure
	public TaskManager(Context context){
		tasks = new ArrayList<Task>();
		//try and load data from database?
		datasource = new TaskDatasource(context);
		datasource.open();
		tasks = datasource.getAllTasks();
	}
	public void createTree(){
		for(Task task:tasks){
		}
	}
	//functions for filtering lists go here, 
	//e.g by status
	//date will probably be better done on the db
	
	//task sharing b/w activities
	public static void setTask(Task task){
		parentSingleton = task;
	}
	//task sharing between activities
	public static Task getTask(){
		return parentSingleton;
	}
}
