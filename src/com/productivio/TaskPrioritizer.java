package com.productivio;

import java.util.Comparator;

//sort tasks by priority
public class TaskPrioritizer implements Comparator<Task>{
	@Override
	public int compare(Task task1, Task task2){
		if(task1.getPriority()<task2.getPriority()) return 1;
		else if(task1.getPriority()>task2.getPriority()) return -1;
		//same priority - compare alphabetically 
		else return task1.getDescription().compareTo(task2.getDescription());
	}
}
