/*
 * Copyright (C) 2010 Eric Harlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.productivio;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;

public class TaskView extends ListView {
	//initially called drag mode
	//better understood as scrollMode
	static boolean scrollMode = true;
	
	int startPosition;
	int endPosition;
	int dragPointOffset;		//Used to adjust drag view location
	int initialX;
	boolean dragging,deleteAlert,editAlert;
	ImageView dragView;
	GestureDetector gestureDetector;
	Bitmap bitmap, bitmapRed;
	Canvas canvasRed;
	
	DropListener dropListener;
	RemoveListener removeListener;
	DragListener dragListener;
	
	public TaskView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setDropListener(DropListener l) {
		dropListener = l;
	}

	public void setRemoveListener(RemoveListener l) {
		removeListener = l;
	}
	
	public void setDragListener(DragListener l) {
		dragListener = l;
	}
	public static void setScrollMode(boolean scroll){
		scrollMode = scroll;
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();
		
		
		
		
		if (action == MotionEvent.ACTION_DOWN) {
			initialX = x;
			//scrollMode should be controlled outside of this class
			//scrollMode = true;
		}
		//this is similar to quiting if returned...
		if(scrollMode)  super.onTouchEvent(ev);
		int xOffset  = x - initialX;
		
		//----------------------------debug-----------------------------
		/*Log.d("y",String.valueOf(y));
		Log.d("new index",String.valueOf(pointToPosition(x,y)));
		Log.d("old index",String.valueOf(startPosition));
		Log.d("delete alert",String.valueOf(deleteAlert));
		Log.d("x",String.valueOf(xOffset));
		Log.d("width",String.valueOf(this.getWidth()/2));*/
		
		switch (action) {
		//case for starting touch action
			case MotionEvent.ACTION_DOWN:
				startPosition = pointToPosition(x,y);
				if (startPosition != INVALID_POSITION) {
					int mItemPosition = startPosition - getFirstVisiblePosition();
                    dragPointOffset = y - getChildAt(mItemPosition).getTop();
                    dragPointOffset -= ((int)ev.getRawY()) - y;
                    
					if(!scrollMode) {
						dragging = true;
						startDrag(mItemPosition,y);
						drag(xOffset,y);// replace 0 with x if desired}
					}
				}	
				break;
				
				//case for moving touch pointer
			case MotionEvent.ACTION_MOVE:
				//if currently not in scroll mode, and dragging hasn't started
				if(!dragging && !scrollMode){
					dragging = true;
					//start it..
					startPosition = pointToPosition(x,y);
					int mItemPosition = startPosition - getFirstVisiblePosition();
                    dragPointOffset = y - getChildAt(mItemPosition).getTop();
                    dragPointOffset -= ((int)ev.getRawY()) - y;
                    startDrag(mItemPosition,y);
                    editAlert= false;
				}
				if(!scrollMode) {
					drag(xOffset,y);// replace 0 with x if desired

                    //remove task alert!
					boolean leftSwipe = xOffset< -this.getWidth()*0.4;
					boolean rightSwipe = xOffset > this.getWidth()*0.4;
					boolean onSameItem = startPosition == pointToPosition(x,y); 
					if(leftSwipe){
						if(onSameItem && !deleteAlert){
							if(!deleteAlert){
								deleteAlert=true;
								
								editAlert=false;
								this.performHapticFeedback(1);
							}
                    	}
                    }
					else if(rightSwipe && onSameItem){
						//push dialog here

						if(!editAlert) {
							//send vibration
							this.performHapticFeedback(1);
							editAlert =true;
							//because in getChildAt a child is 0-n on visible group, not whole list
							int itemPosition = startPosition - getFirstVisiblePosition();
							//this sets view back to orange
							stopDrag(itemPosition,true);
							//bug - rightswipe is called here on touch move and touch off
							dragListener.onRightSwipe(startPosition, getChildAt(itemPosition));
							dropListener.onDrop(startPosition, startPosition);
						}
					}
                    else{ 
                    	if(deleteAlert){
                    		//orange
                    		dragView.setImageBitmap(bitmap);
                    		deleteAlert=false;
                    		editAlert=false;
                    		
                    	}
                    }
				}
				break;
		//	case MotionEvent.ACTION_CANCEL: 
		//		scrollMode = true;
		//	case MotionEvent.ACTION_UP:
		//		scrollMode = true;
				
				//case when user stops touching screen
			default:
				if(!scrollMode){
					endPosition = pointToPosition(x,y);
					
					stopDrag(startPosition - getFirstVisiblePosition(),true);
					boolean leftSwipe = xOffset< -this.getWidth()*0.4;
					boolean rightSwipe = xOffset > this.getWidth()*0.4;
					boolean onSameItem = startPosition == pointToPosition(x,y);
					
					if (dropListener != null && startPosition != INVALID_POSITION && endPosition != INVALID_POSITION) 
						dropListener.onDrop(startPosition, endPosition);
					if(leftSwipe && startPosition == endPosition) {
						removeListener.onRemove(startPosition);
					}
					if(rightSwipe && editAlert){
						//bug onRightSwipe is called twice
						//dragListener.onRightSwipe(startPosition,getChildAt(startPosition));
					}
				}
				scrollMode = true;
				dragging = false;
				deleteAlert = false;
				editAlert = false;
				break;
		}
		if(scrollMode) return true;
		else return true;
	}	
	
	// move the drag view
	private void drag(int x, int y) {
		if (dragView != null) {
			WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) dragView.getLayoutParams();
			layoutParams.x = x;
			layoutParams.y = y - dragPointOffset;
			WindowManager mWindowManager = (WindowManager) getContext()
					.getSystemService(Context.WINDOW_SERVICE);
			mWindowManager.updateViewLayout(dragView, layoutParams);
			if (dragListener != null)
				dragListener.onDrag(x, y, this);// change null to "this" when ready to use
		}
	}

	// enable the drag view for dragging
	private void startDrag(int itemIndex, int y) {
		//why is stopDrag being called - to destroy last window
		stopDrag(itemIndex,false);
		
		//grab child
		final View item = getChildAt(itemIndex);
		if (item == null) return;
		item.setDrawingCacheEnabled(true);
		
		
        // Create a copy of the drawing cache so that it does not get recycled
        // by the framework when the list tries to clean up memory
        	bitmap = Bitmap.createBitmap(item.getDrawingCache());
        	Bitmap bitmap2 = bitmap.copy(Config.ARGB_8888, true);
        	bitmapRed = bitmap.copy(Config.ARGB_8888, true);
        	
        	bitmap2.eraseColor(0xaaff0000);
        	
        	//colour me red .... *shivers*
        	
        	canvasRed = new Canvas(bitmapRed);
        	Paint paint = new Paint();
        	paint.setFilterBitmap(false);
        	
        	//this isn't needed
        	paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        	canvasRed.drawBitmap(bitmapRed, 0, 0, paint);
        	//paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        	//alpha blending paint bucket XD
        	paint.setXfermode(null);

        //Create new window
        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
        //and place it where ever its supposed to be
        mWindowParams.gravity = Gravity.TOP;
        mWindowParams.x = 0;
        mWindowParams.y = y - dragPointOffset;
        //create said new window
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;
        
        Context context = getContext();
        ImageView v = new ImageView(context);
        v.setImageBitmap(bitmap);
        v.invalidate();

        WindowManager mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(v, mWindowParams);
        dragView = v;
        //this sets original to black
        //makes the screen flicker if done earlier
        Runnable hideTask = new Runnable(){
			@Override
			public void run() {
				if (dragListener != null)
        			dragListener.onStartDrag(item);
			}
        };
        this.postDelayed(hideTask, 50);
	}

	// destroy drag view
	private void stopDrag(int itemIndex,boolean updateView) {
		if (dragView != null) {
			//this thing causes flicker
			if (dragListener != null && updateView)
				dragListener.onStopDrag(getChildAt(itemIndex));
			//drag window and controls are deleted
            dragView.setVisibility(GONE);
            WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(dragView);
            //image is deleted
            dragView.setImageDrawable(null);
            dragView = null;
            
        }
	}

//	private GestureDetector createFlingDetector() {
//		return new GestureDetector(getContext(), new SimpleOnGestureListener() {
//            @Override
//            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//                    float velocityY) {         	
//                if (dragView != null) {              	
//                	int deltaX = (int)Math.abs(e1.getX()-e2.getX());
//                	int deltaY = (int)Math.abs(e1.getY() - e2.getY());
//               
//                	if (deltaX > dragView.getWidth()/2 && deltaY < dragView.getHeight()) {
//                		mRemoveListener.onRemove(mStartPosition);
//                	}
//                	
//                	stopDrag(mStartPosition - getFirstVisiblePosition());
//
//                    return true;
//                }
//                return false;
//            }
//        });
//	}
}
