package kr.re.Dev.Test;

import kr.re.Dev.ActivityStack.ActivityStack;
import kr.re.Dev.ActivityStack.StackActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TestActivityA extends StackActivity{
	
	private TextView mTextViewLog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		
		startUpdateLog();
	}
	
	private void initView() {
		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new  android.view.WindowManager.LayoutParams(android.view.WindowManager.LayoutParams.MATCH_PARENT,android.view.WindowManager.LayoutParams.MATCH_PARENT));
		mTextViewLog = new TextView(this);
		
		LinearLayout.LayoutParams textViewLogoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		
		textViewLogoParams.weight = 1.0f;
		mTextViewLog.setLayoutParams(textViewLogoParams);
		mTextViewLog.setText("wait...");
		
		Button button = new Button(this);
		button.setText("Start Activity B");
		
		button.setOnClickListener(mOnClickNew);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(button);
		layout.addView(mTextViewLog);
		setContentView(layout);
	}
	
		
	boolean isForeground = false; 
	private void startUpdateLog() {
		if(!isFinishing()) {
			getWindow().getDecorView().postDelayed(new Runnable() {
				@Override
				public void run() {
					StringBuilder log = new StringBuilder();
					ActivityStack activityStack = ActivityStack.getInstance();
					log.append("AliveID : ").append(activityStack.getAliveID(TestActivityA.this)).append("\n");
					log.append(activityStack.toString()).append("\n");
					
					if(activityStack.getForegroundActivity() != TestActivityA.this) {
						log.append("not ForegroundActivity");
					} 
					if(activityStack.isForeground() && !isForeground) {
						Log.i("test","Foreground");
						isForeground = true;
					} else if(!activityStack.isForeground() && isForeground) {
						Log.i("test","Background");
						isForeground = false;
					}
					
					mTextViewLog.setText(log);
					getWindow().getDecorView().postDelayed(this, 1000);
					
					
				}
			},100);
		}
	}
	private View.OnClickListener mOnClickNew = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getApplicationContext(), TestActivityB.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
		}
	};

}
