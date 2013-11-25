package com.productivio;


import java.util.*;

public class Task {
	//timestamp?
	private Long dateStarted;
	private Long dateCompleted;
	private String description;
	private int priority;
	private int timeSpent;
	private int timeNeeded;
	private TaskDatasource datasource;
	private STATUS status;
	//does index get used? Yes if with db
	private int index;
	private ArrayList<Task> subtasks;
	private int superTask;
	private boolean commitNeeded;
	public enum STATUS{DUE, DONE, DELETED};
	
	//new task from scratch, index may not be useful here, can get index when commiting
	public Task(Long dateCreated, String description, int priority, int index){
		dateStarted = dateCreated;
		this.description = description;
		this.priority = priority;
		status = STATUS.DUE;
		this.index = index;
		subtasks = new ArrayList<Task>();
		//add to database
		//datasource = dts;
		
	}
	//new task built from db
	public Task(int index){
		this.index = index;
		//this.revertFromDb();
	}
	//constructor must play with fields directly, otherwise it commits too quickly
	public Task(String description){
		this.description = description;
		this.status = STATUS.DUE;
		subtasks = new ArrayList<Task>();
		index = -1;
		//this.dateStarted = Time
		//need to set index and priority
	}
	
	public int getIndex(){
		return index;
	}
	
	public String getDescription(){
		return description;
	}
	
	public Long getStartDate(){
		return dateStarted;
	}
	
	public Long getCompletionDate(){
		return dateCompleted;
	}
	
	public STATUS getStatus(){
		return status;
	}
	public int getPriority(){
		return priority;
	}
	public int getSuperTask(){
		return superTask;
	}
	public ArrayList<Task> getSubTasks(){
		return subtasks;
	}
	public int getTimeSpent(){
		return timeSpent;
	}
	public int getTimeNeeded(){
		return timeNeeded;
	}
	public boolean getCommitNeeded(){
		return commitNeeded;
	}
	
	public String setStatus(STATUS st){
		String error = "No error";
		if(status==STATUS.DUE){
			if(st==STATUS.DONE){
				//to be able to mark a due task as done
				//no subtasks must be due
				int DueSubTasks = 0;
				for(Task subtask:subtasks){
					if(subtask.getStatus()==STATUS.DUE) DueSubTasks++;
					//exit if possible
				}
				//number of subtasks is still 0, mark this task as done
				if(DueSubTasks==0){
					this.status = st;
				}
				//else return error and mark nothing
				else error = "Some subtasks are due";
			}
			else if(st==STATUS.DELETED){
				//To delete a due task, 
				//all due subtasks are marked as deleted
				for(Task subtask:subtasks){
					if(subtask.getStatus()==STATUS.DUE) subtask.setStatus(STATUS.DELETED);
				}
				this.status = st;
			}
		}
		else if(status==STATUS.DONE){
			if(st==STATUS.DUE){
				//When a done task is marked as due
				//no subtasks are marked
				this.status = st;
			}
			else if(st==STATUS.DELETED){
				//When a done task is marked as deleted
				//no subtasks are marked
				this.status = st;
			}
		}
		//deleted
		else {
			if(st==STATUS.DUE){
				//When a deleted task is marked as due
				//all deleted subtasks are marked due
				for(Task subtask:subtasks){
					if(subtask.getStatus()==STATUS.DELETED) subtask.setStatus(STATUS.DELETED);
				}
				this.status = st;
			}
			else if(st==STATUS.DONE){
				//when a deleted task is marked as done
				//no subtasks are marked (means completed tasks stay completed
				this.status = st;
			}
		}
		return error;
	}
	
	public void setDescription(String desc){
		this.description = desc;
	}
	public void setIndex(int id){
		index = id;
	}
	
	public void setPriority(int priority){
		this.priority = priority;
	}
	public void setStartDate(Long date){
		dateStarted = date;
	}
	
	public void setCompletionDate(Long date){
		dateCompleted = date;
	}
	public void setSuperTask(int par){
		superTask = par;
	}

	public void setTimeSpent(int ts){
		timeSpent = ts;
	}
	public void setTimeNeeded(int tn){
		timeNeeded = tn;
	}
	public void setCommitNeeded(boolean commit){
		commitNeeded = commit;
	}
	public void commitToDb(){
		if(index==-1) TaskDatasource.insertTask(this);
		else TaskDatasource.updateTask(this);
		commitNeeded = false;
	}
	//does this get used?
	public void revertFromDb(){
		//use index value to query database and build object
	}
	//new subtask from scratch
	public void createSubTask(Long dateCreated, String description, int priority, int index){
		//create new task object
		Task newTask = new Task(dateCreated,description,priority,index);
		newTask.commitToDb();
	}
	//recursive task population function
	public void getSubTasksFromDB(){
		subtasks = TaskDatasource.getChildren(index,true);
			//tell all children to try and fetch subtasks
		//for some reason this did not work
		//fixed bug where each task pointed to itself, resulting in infinite db calls
	
		//recursion too slow, and buggy
		/*for(Task task: subtasks){
			task.getSubTasksFromDB();
		}*/
	}
	//new subtask from db, don't think is needed
	public void createSubTask(int index){
		Task newTask = new Task(index);
		newTask.revertFromDb();
		subtasks.add(newTask);
	}
	//doesn't get used?
	public void setSubTaskStatus(int index, STATUS status){
		for(int x =0;x<subtasks.size();x++){
			Task subtask = subtasks.get(x);
			if(subtask.getIndex()==index){
				subtask.setStatus(status);
			}
		}
	}

}
