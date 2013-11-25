package com.productivio;

import java.util.ArrayList;


import android.view.ViewGroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public final class TaskAdapter extends ArrayAdapter<Task> implements RemoveListener, DropListener {

        private ArrayList<Task> tasks;
        private LayoutInflater layoutInflater;
        private int[] ids;
        private int[] layouts;
        
        //i think textViewResourceID can be anything
        public TaskAdapter(Context context, int textViewResourceId, ArrayList<Task> tasks, LayoutInflater li) {
                super(context, textViewResourceId,tasks);
                this.tasks = tasks;
                layoutInflater = li;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;
        	
                if (convertView == null) {
                    
                    convertView = layoutInflater.inflate(R.layout.task, null);
                 // Creates a ViewHolder and store references to the two children views
                    // we want to bind data to.
                    holder = new ViewHolder();
                    holder.heading = (TextView) convertView.findViewById(R.id.heading);
                    holder.content = (TextView) convertView.findViewById(R.id.content);
                    convertView.setTag(holder);

                }
                Task task = tasks.get(position);
                if (task != null) {
                	holder = (ViewHolder) convertView.getTag();
                        //TextView heading = (TextView) convertView.findViewById(R.id.heading);
                        //TextView content = (TextView) convertView.findViewById(R.id.content);
                        if (holder.heading != null) {
                              holder.heading.setText(task.getDescription());                            }
                        if(holder.content != null){
                              holder.content.setText("Index: " + task.getIndex() +  "\tStatus: " + task.getStatus().toString());
                        }
                }
                return convertView;
        }
        public int getCount(){
        	return tasks.size();
        }
        public void rebuildPriority(){
        	for(int x = 0;x<tasks.size();x++){
        		Task temp  = tasks.get(x);
        		if(temp.getPriority()!=x){
        			temp.setPriority(x);
        			temp.setCommitNeeded(true);
        		}
        	}
        	quickCommit();
        }
        public void quickCommit(){

        	//doing this in a new thread
        	
        	
        	new Thread(new Runnable() {
			    public void run() {
			    	//Db shit is this thread's problem
			    	TaskDatasource.open();
			    	//for each task
			    	
			    	for(Task task:tasks) {
			    		//commit to db, those which needed
		        		if(task.getCommitNeeded()) {
		        			TaskDatasource.updateTask(task,true);
		        			task.setCommitNeeded(false);
		        		}
		        	}
			    	//need to close db inside thread, or the handle closes before all queries
			    	TaskDatasource.close();
			    }
			  }).start();
        	
        	
        }
        public Task getTask(int position) {
            return tasks.get(position);
        }

    	public void onRemove(int which) {
    		if (which < 0 || which > tasks.size()) return;
    		Task task = tasks.get(which);
    		TaskDatasource.deleteTask(task);
    		tasks.remove(which);
    		rebuildPriority();
    		//set task status here
    		//and commit to db
    	}
    	public void onDrop(int from, int to) {
    		if(from!=to){
    			Task temp= tasks.get(from);
    			tasks.remove(temp);
    			tasks.add(to,temp);
    			rebuildPriority();
    			}
    	}

		@Override
		public Task getItem(int index) {
			return tasks.get(index+1);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
	  static class ViewHolder {
		        TextView heading;
		        TextView content;
	    }




}
