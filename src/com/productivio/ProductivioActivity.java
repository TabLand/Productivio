package com.productivio;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;

import android.view.MotionEvent;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import android.widget.AdapterView;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ProductivioActivity extends ListActivity {
	private ArrayList<Task> subtasks;
	private TaskAdapter adapter;
	// list item index (can be used in priorities
	private int index;
	// task parent index
	private int parent;
	// whether a child activity exists
	private boolean childActivity, commitNeeded;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.taskview);
		//build list of task names
		final Task task = TaskManager.getTask();
		//initialise static database class 
		TaskDatasource.initialise(getBaseContext());

		String headerText=null;
		//this means master task exists
		subtasks = new ArrayList<Task>();

		if(task!=null){
			//set parent index to this tasks index
			this.parent = task.getIndex();
			//whenever activity is started, synchronise
			//don't need to do any more recursive calls, all data was fetched when app started

			TaskDatasource.openReadOnly();
			task.getSubTasksFromDB();
			TaskDatasource.close();

			subtasks = task.getSubTasks();
			headerText = task.getDescription();
			//Collections.sort(subtasks,new TaskPrioritizer());
		}
		else{
			//get all subtasks of todo list
			TaskDatasource.openReadOnly();

			subtasks = TaskDatasource.getChildren(0,true);
			//recursion too slow and buggy

			/*for(Task subtask:subtasks){
						subtask.getSubTasksFromDB();
					}*/
			//close inside thread, otherwise db handle closes before queries are executed
			TaskDatasource.close();

			headerText = "To Do list";
			parent = 0;
		}







		LayoutInflater inflater = LayoutInflater.from(getBaseContext());

		adapter = new TaskAdapter(this, R.layout.task, subtasks,inflater);


		//set hold event listener
		//setLongClick(lv);

		//ViewGroup header = (ViewGroup)inflater.inflate(R.layout.tasks_header, lv, false);
		//lv.addHeaderView(header, null, false);

		setListAdapter(adapter);

		ListView lv = getListView();


		if (lv instanceof TaskView) {
			((TaskView) lv).setDropListener(dropListener);
			((TaskView) lv).setRemoveListener(removeListener);
			((TaskView) lv).setDragListener(dragListener);
		}
		setEvents(lv);
		changeHeaderText(headerText);	
	}

	private DropListener dropListener = new DropListener() {
		public void onDrop(int from, int to) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof TaskAdapter) {
				((TaskAdapter) adapter).onDrop(from, to);
				getListView().invalidateViews();
			}
		}
	};

	private RemoveListener removeListener = new RemoveListener() {
		public void onRemove(int which) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof TaskAdapter) {
				((TaskAdapter) adapter).onRemove(which);
				getListView().invalidateViews();
			}
		}
	};
	private DragListener dragListener = new DragListener() {

		public void onDrag(int x, int y, ListView listView) {
			// TODO Auto-generated method stub
		}

		public void onStartDrag(View itemView) {

			// set background to black
			// itemView is task

			/*
			 * TextView heading = (TextView)itemView.findViewById(R.id.heading);
			 * TextView content = (TextView)itemView.findViewById(R.id.content);
			 * 
			 * if (heading != null || content != null){
			 * heading.setBackgroundColor(0xff000000);
			 * content.setBackgroundColor(0xff000000);
			 * heading.setTextColor(0xff000000);
			 * content.setTextColor(0xff000000); }
			 * getListView().invalidateViews();
			 */
		}

		public void onStopDrag(View itemView) {
			/*
			 * TextView heading = (TextView)itemView.findViewById(R.id.heading);
			 * TextView content = (TextView)itemView.findViewById(R.id.content);
			 * 
			 * if(heading!=null ||content !=null){
			 * heading.setBackgroundColor(0xffc75c34);
			 * content.setBackgroundColor(0xffc75c34);
			 * heading.setTextColor(0xffffffff);
			 * content.setTextColor(0xffffffff);
			 * 
			 * } getListView().invalidateViews();
			 */
		}

		@Override
		public void onRightSwipe(final int item, final View childAt) {
			// super.dialog("text","changeTask");
			/*
			 * TextView heading = (TextView)childAt.findViewById(R.id.heading);
			 * TextView content = (TextView)childAt.findViewById(R.id.content);
			 * 
			 * if(heading!=null ||content !=null){
			 * heading.setBackgroundColor(0xff444444);
			 * content.setBackgroundColor(0xff444444);
			 * heading.setTextColor(0xffffffff);
			 * content.setTextColor(0xffffffff); }*
			 * getListView().invalidateViews();
			 */
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					index = item;
					// item is not the index, its the priority
					String name = subtasks.get(index).getDescription();
					dialog(name, "changeTask", childAt);
				}
			});
		}

		@Override
		public void onLeftSwipe(int item) {
			// TODO Auto-generated method stub

		}

		// public void onLeftSwipe(int item) {
		// TODO Auto-generated method stub

		// }

	};

	@Override
	public void onDestroy() {
		// if child activity exists, do not destroy
		if (childActivity)
			super.onStop();
		// else
		else {
			super.onDestroy();
			finish();
		}
	}

	@Override
	public void onRestart() {
		// if child activity no longer exists
		super.onRestart();
		childActivity = false;

	}

	@Override
	public void onStop() {
		// do nothing
		super.onStop();
		// if there isn't a child we are waiting for, might as well save memory
		// ruins flow of program
		// else onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		childActivity = false;
		// do nothing
	}

	@Override
	public void onPause() {
		super.onPause();
		// do nothing
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		enterSublist(v, position, id);
	}

	private void changeTaskName(String name, View item) {
		Task task = subtasks.get(index);
		task.setDescription(name);
		// only commit when we know user changes something
		task.commitToDb();
		adapter.notifyDataSetChanged();
		dragListener.onStopDrag(item);
	}

	private void removeSubTask() {
		subtasks.remove(index);
		// need to remove this index from db
		// in actual use- better to just mark status
		adapter.notifyDataSetChanged();
	}

	private void addNewSubTask(Task task) {
		task.setPriority(subtasks.size());
		this.subtasks.add(task);
		task.setSuperTask(parent);
		task.commitToDb();
		adapter.notifyDataSetChanged();
	}

	private void setEvents(ListView lv) {
		final Button button = (Button) findViewById(R.id.button_add_task);
		button.setOnClickListener((android.view.View.OnClickListener) clickListener);
		this.setLongClick(lv);
		// might as well set vibrations
		lv.setHapticFeedbackEnabled(true);
	}

	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {

			runOnUiThread(new Runnable() {
				public void run() {
					dialog("", "addTask", v);
				}
			});
		}
	};
	private OnTouchListener touchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent mEv) {
			// if(view.getId()==R.layout.task){
			Log.d("touch", String.valueOf(mEv.getX()));
			Log.d("touch", String.valueOf(mEv.getY()));

			return true;
			// }
			// else return false;
		}
	};

	private void setLongClick(ListView lv) {
		// lv.setOnTouchListener(touchListener);
		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> av, View view,
					int pos, long id) {
				index = pos - 1;
				Task item = (Task) getListAdapter().getItem(index);
				// v.findViewById(R.id.heading).setBackgroundColor(0xcccccc00);
				// Toast.makeText(this, item + " selected",
				// Toast.LENGTH_LONG).show();
				// dialog(item.getDescription(),"changeTask");
				TaskView.setScrollMode(false);
				view.performHapticFeedback(1);
				return false;
			}
		});
	};

	private void enterSublist(View v, int position, long id) {
		// TODO Auto-generated method stub
		// start new activity
		childActivity = true;
		Intent sublist = new Intent(getBaseContext(), ProductivioActivity.class);
		sublist.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		TaskManager.setTask((Task) getListAdapter().getItem(position - 1));
		startActivity(sublist);
		// change header text

		// and change main task array (handled in oncreate + task manager);
	}

	private void changeHeaderText(String challenge) {
		TextView myTextView = (TextView) findViewById(R.id.textView1);
		myTextView.setText(challenge);
	}

	private void dialog(String taskName, final String action, final View item) {
		String title = null;
		if (action == "changeTask")
			title = "Change Task Name";
		else if (action == "addTask")
			title = "Add Task";

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(title);
		alert.setMessage(title + ":");

		// Set an EditText view to get user input

		final EditText input = new EditText(this);
		input.setText(taskName);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				final String value = input.getText().toString();

				// send value back
				runOnUiThread(new Runnable() {
					public void run() {
						if (action == "changeTask")
							changeTaskName(value, item);
						else if (action == "addTask")
							addNewSubTask(new Task(value));
					};
				});
			}
		});
		alert.show();
	}

}