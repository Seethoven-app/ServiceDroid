package com.monstarlab.servicedroid.activity;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.widget.TableLayout;
import android.widget.TextView;

import com.monstarlab.servicedroid.model.Models.BibleStudies;
import com.monstarlab.servicedroid.model.Models.Placements;
import com.monstarlab.servicedroid.model.Models.ReturnVisits;
import com.monstarlab.servicedroid.model.Models.TimeEntries;
import com.monstarlab.servicedroid.util.TimeUtil;
import com.monstarlab.servicedroid.R;

public class StatisticsActivity extends Activity implements OnTouchListener {
	
	private static final String TAG = "StatisticsActivity";
	
	private static final int MENU_MONTH = Menu.FIRST;
	private static final int MENU_YEAR = Menu.FIRST + 1;
	private static final int MENU_EMAIL = Menu.FIRST + 2;
	
	//private TimeUtil mTimeHelper;
	
	//private static String[] CallsProjection = new String[] { Calls._ID, Calls.BIBLE_STUDY };
	private static String[] BibleStudiesProjection = new String[] { BibleStudies._ID };
	private static String[] TimeProjection = new String[] { TimeEntries._ID, TimeEntries.DATE, TimeEntries.LENGTH };
	private static String[] RVProjection = new String[] { ReturnVisits._ID, ReturnVisits.DATE, ReturnVisits.CALL_ID };
	private static String[] PlacementsProjection = new String[] { Placements._ID, Placements.DATE };
	
	private TextView mTimePeriodDisplay;
	private TextView mHoursDisplay;
	private TextView mRvsDisplay;
	private TextView mMagsDisplay;
	private TextView mBrochuresDisplay;
	private TextView mBooksDisplay;
	private TextView mBibleStudiesDisplay;
	
	private int mCurrentMonth = TimeUtil.getCurrentMonth();
	private int mCurrentYear = TimeUtil.getCurrentYear(); // 1 - 12
	//private int mTimeSpan = MENU_MONTH;
	
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	private static final int DIALOG_ROUND_ID = 1;
	
    private GestureDetector gestureDetector;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);
        
       // mTimeHelper = new TimeUtil(this);
        mTimePeriodDisplay = (TextView)findViewById(R.id.stats_timeperiod);
        mHoursDisplay = (TextView)findViewById(R.id.hours);
        mRvsDisplay = (TextView)findViewById(R.id.rvs);
        mMagsDisplay = (TextView)findViewById(R.id.magazines);
        mBrochuresDisplay = (TextView)findViewById(R.id.brochures);
        mBooksDisplay = (TextView)findViewById(R.id.books);
        mBibleStudiesDisplay = (TextView)findViewById(R.id.bible_studies);
        
        // Gesture detection
	    gestureDetector = new GestureDetector(new MyGestureDetector());
	    TableLayout table = (TableLayout) findViewById(R.id.statstable);
	    table.setOnTouchListener(this);

    }
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		fillData();
	}



	protected void fillData() {
		mTimePeriodDisplay.setText("" + mCurrentMonth + "/" + mCurrentYear);
		mHoursDisplay.setText(getHours());
		mRvsDisplay.setText(getRVs());
		mMagsDisplay.setText(getMagazines());
		mBrochuresDisplay.setText(getBrochures());
		mBooksDisplay.setText(getBooks());
		mBibleStudiesDisplay.setText(getBibleStudies());
	}
	
	protected String getBibleStudies() {
		//all Bible Studies started before next month, and not ended before this month
		String where = BibleStudies.DATE_START + "<? and (" + BibleStudies.DATE_END+" isnull or "+BibleStudies.DATE_END + ">?)";
		
		String[] args = getTimePeriodArgs(mCurrentYear, mCurrentMonth);
		String thisMonth = args[0];
		String nextMonth = args[1];
		
		String[] whereArgs = new String[] { nextMonth, thisMonth };
		Cursor c = getContentResolver().query(BibleStudies.CONTENT_URI, BibleStudiesProjection, where, whereArgs, null);
		int sum = 0;
		if(c != null) {
			c.moveToFirst();
			sum = c.getCount();
			c.close();
			c = null;
		}
		return "" + sum;
	}



	protected String getBooks() {
		Cursor c = getContentResolver().query(Placements.BOOKS_CONTENT_URI, PlacementsProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
		int sum = 0;
		if(c != null) {
			sum = c.getCount();
			c.close();
			c = null;
		}
		return ""+sum;
	}



	protected String getBrochures() {
		Cursor c = getContentResolver().query(Placements.BROCHURES_CONTENT_URI, PlacementsProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
		int sum = 0;
		if(c != null) {
			sum = c.getCount();
			c.close();
			c = null;
		}
		return ""+sum;
	}



	protected String getMagazines() {
		Cursor c = getContentResolver().query(Placements.MAGAZINES_CONTENT_URI, PlacementsProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
		int sum = 0;
		if(c != null) {
			sum = c.getCount();
			c.close();
			c = null;
		}
		return ""+sum;
	}



	protected int getHoursSum() {
		
		Cursor c = getContentResolver().query(TimeEntries.CONTENT_URI, TimeProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
		int sum = 0;
		if(c != null) {
			c.moveToFirst();
			while(!c.isAfterLast()) {
				sum += c.getInt(2);
				c.moveToNext(); 
			}
			c.close();
			c = null;
		}
		return sum;
	}
	
	protected String getHours() {
		return TimeUtil.toTimeString(getHoursSum());
	}
	
	protected String getRVs() {
		Cursor c = getContentResolver().query(ReturnVisits.CONTENT_URI, RVProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), null);
		String numOfRVs = "0";
		if(c != null) {
			c.moveToFirst();
			numOfRVs = "" + c.getCount();
			c.close();
			c = null;
		}
		
		return numOfRVs;
	}
	
	protected void setTimePeriod() {
		
	}
	
	protected void moveBackwardOneMonth() {
		mCurrentMonth--;
		if(mCurrentMonth <= 0) {
			mCurrentMonth = 12;
			mCurrentYear--;
		}
	}
	
	protected void moveForwardOneMonth() {
		mCurrentMonth++;
		if(mCurrentMonth > 12) {
			mCurrentMonth = 1;
			mCurrentYear++;
		}
	}
	
	protected String getTimePeriodWhere(String dateField) {
		return "("+dateField + " between ? and ?)"; // "dateField between YYYY-MM-01 and date('YYYY-MM-01','+1 month','-1 day');"
	}
	
	protected String[] getTimePeriodArgs(int year, int month) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, 1);
		String[] args = new String[2];

		//beginning of month
		Date startOfMonth = cal.getTime();
		args[0] = TimeUtil.getSQLTextFromDate(startOfMonth);
		
		
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.DATE, -1);
		Date endOfMonth = cal.getTime();
		
		//end of month
		args[1] = TimeUtil.getSQLTextFromDate(endOfMonth);
		
		return args;
	}
	
	/*protected void setTimeSpan(int span) {
		mTimeSpan = span;
	}*/
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		//menu.add(0, MENU_MONTH, 1, R.string.monthly).setIcon(android.R.drawable.ic_menu_month);
		//menu.add(0, MENU_YEAR, 1, R.string.service_year).setIcon(android.R.drawable.ic_menu_my_calendar);
		menu.add(0, MENU_EMAIL, 1, R.string.send).setIcon(android.R.drawable.ic_menu_send);
		return result;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		switch(item.getItemId()) {
		
		/*case MENU_MONTH:
			setTimeSpan(MENU_MONTH);
			break;
		case MENU_YEAR:
			setTimeSpan(MENU_YEAR);
			break;*/
		
		case MENU_EMAIL:
			setupSendEmail();
			break;
			
		}
		
		return super.onOptionsItemSelected(item);
	}



	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
	    switch(id) {
	    case DIALOG_ROUND_ID:
	    	dialog = makeRoundTimeDialog();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
	protected Dialog makeRoundTimeDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(getHours());
		builder.setMessage(getString(R.string.round_time_prompt))
	       .setCancelable(true)
	       .setPositiveButton(getString(R.string.round_time_prompt_round), new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   roundUpTime();
	        	   sendEmail();
	           }
	       })
	       .setNegativeButton(getString(R.string.round_time_prompt_carry), new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                carryOverTime();
	                sendEmail();
	           }
	       });
		return builder.create();
	}
	
	protected void roundUpTime() {
		//add another time entry of 60 - mins, on last day of month
		int minutes = TimeUtil.getMins(getHoursSum());
		Calendar cal = Calendar.getInstance();
		cal.set(mCurrentYear, mCurrentMonth, 1);
		cal.add(Calendar.DATE, -1);
		
		Date lastDayOfMonth = cal.getTime();
		
		ContentValues values = new ContentValues();
		values.put(TimeEntries.LENGTH, TimeUtil.toTimeInt(0, 60 - minutes));
		values.put(TimeEntries.DATE, TimeUtil.getSQLTextFromDate(lastDayOfMonth));
		
		getContentResolver().insert(TimeEntries.CONTENT_URI, values);
		
	}

	protected void carryOverTime() {
		//place time entry of `mins` in next month
		int minutes = TimeUtil.getMins(getHoursSum());
		Calendar cal = Calendar.getInstance();
		cal.set(mCurrentYear, mCurrentMonth - 1, 1);
		cal.add(Calendar.MONTH, 1);
		
		Date nextMonth = cal.getTime();
		
		ContentValues values = new ContentValues();
		values.put(TimeEntries.LENGTH, TimeUtil.toTimeInt(0, minutes));
		values.put(TimeEntries.DATE, TimeUtil.getSQLTextFromDate(nextMonth));
		
		getContentResolver().insert(TimeEntries.CONTENT_URI, values);

		//somehow remove those mins from this month
		// Collect all time entries, and peel off minutes until we get to 0 extra mins, and save the Entries back
		int minutesLeft = minutes;
		Cursor cursor = getContentResolver().query(TimeEntries.CONTENT_URI, TimeProjection, getTimePeriodWhere(ReturnVisits.DATE), getTimePeriodArgs(mCurrentYear, mCurrentMonth), TimeEntries.LENGTH +" desc");
		if(cursor.getCount() > 0) {
			cursor.moveToFirst();
			int curTime = 0;
			while(!cursor.isAfterLast() && minutesLeft > 0) {
				curTime = cursor.getInt(2) / TimeUtil.MIN;
				if(curTime >= minutesLeft) {
					curTime -= minutesLeft;
					minutesLeft = 0;
				} else {
					minutesLeft -= curTime;
					curTime = 0;
				}
				
				//update the time entry if curTime > 0, else remove the entry
				Uri curUri = ContentUris.withAppendedId(TimeEntries.CONTENT_URI, cursor.getInt(0));
				if(curTime > 0) {
					ContentValues upValues = new ContentValues();
					upValues.put(TimeEntries.LENGTH, TimeUtil.toTimeInt(0, curTime));
					getContentResolver().update(curUri, upValues, null, null);
				} else {
					getContentResolver().delete(curUri, null, null);
				}
				
			
				cursor.moveToNext();
			}
		}
		cursor.close();
		cursor = null;
	}

	protected void setupSendEmail() {
		
		if(shouldRoundTime()) {
			//offer to round or carry over
			showDialog(DIALOG_ROUND_ID);
		} else {
			sendEmail();
		}

	}
	
	protected void sendEmail() {
		Intent i = new Intent(Intent.ACTION_SEND, Uri.parse("content://com.android.email.provider"));
		//i.setType("text/plain"); //use this line for testing in the emulator  
		i.setType("message/rfc822"); //for device
		i.putExtra(Intent.EXTRA_EMAIL, new String[] {});  
		i.putExtra(Intent.EXTRA_SUBJECT, "Service Time for " + mCurrentMonth + "/" + mCurrentYear);  
		i.putExtra(Intent.EXTRA_TEXT, getStatsTextForTimePeriod());
		startActivity(Intent.createChooser(i, "Send by..."));
	}
	
	protected boolean shouldRoundTime() {
		int seconds = getHoursSum();
		int hours = TimeUtil.getHours(seconds);
		int minutes = TimeUtil.getMins(seconds);
		
		//make sure Hours greater than 1. don't want to bother someone if they're submitting under an hour
		//since the Society points out that infirm publishers can still report as small as 15 minutes.
		//asking them to round or carry over every time woudl be discouraging...
		if(minutes > 0 && hours > 1) {
			return true;
		}
		
		return false;
	}



	protected String getStatsTextForTimePeriod() {
		StringBuilder sb = new StringBuilder();
		
		//TODO - use strings.xml to allow for internationalization
		sb.append("Here is my Service Record for " + mCurrentMonth + "/" + mCurrentYear + "\n\n");
		sb.append("Hours: " + getHours() + "\n");
		sb.append("Magazines: " + getMagazines() + "\n");
		sb.append("Brochures: " + getBrochures() + "\n");
		sb.append("Books: " + getBooks() + "\n");
		sb.append("Return Visits: " + getRVs() + "\n");
		sb.append("Bible Studies: " + getBibleStudies() + "\n");
		
		
		
		return sb.toString();
	}

	
	class MyGestureDetector extends SimpleOnGestureListener {
	    @Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	        try {
	            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
	                return false;
	            // right to left swipe
	            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	            	//left
	            	moveForwardOneMonth();
	            	fillData();
	            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	                //right
	            	moveBackwardOneMonth();
	            	fillData();
	            }
	        } catch (Exception e) {
	            // nothing
	        }
	        return false;
	    }
	    
	    @Override
	    public boolean onDown(MotionEvent event) {
	    	return true;
	    }
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return false;
    }
	
}
