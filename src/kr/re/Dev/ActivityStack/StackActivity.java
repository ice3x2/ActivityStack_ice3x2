package kr.re.Dev.ActivityStack;

import android.app.Activity;
import android.os.Bundle;

/**
 * 
 * @author ice3x2
 *
 */


public class StackActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityStack.getInstance().regOnCreateState(this);
	}
	
	
	@Override
	protected void onDestroy() {
		ActivityStack.getInstance().regOnDestroyState(this);
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		ActivityStack.getInstance().regOnResumeState(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		ActivityStack.getInstance().regOnPauseState(this);
	
	}
	
	
}
