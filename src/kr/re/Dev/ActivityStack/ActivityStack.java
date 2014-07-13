package kr.re.Dev.ActivityStack;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;

/**
 * ActivityStack
 * Dev.re.kr
 * @author ice3x2@gmail.com
 */
public class ActivityStack {

	private static int sLastActivityNumber = 0;
	private static ActivityStack mThis;
	private ArrayList<ActivityRef> mActivityAliveList;
	 
	/**
	 * Activity 등록 정보.
	 * @author ice3x2
	 */
	private class ActivityRef {
		WeakReference<Activity> ref = new WeakReference<Activity>(null);
		boolean isResume = false;
		String aliveID = "";
		int taskID = 0;
	
	}

	
	private ActivityStack() {
		mActivityAliveList = new ArrayList<ActivityRef>();
	}
	
// public :
	
	/**
	 * ActivityStack 의 싱글 인스턴스를 가져온다. 
	 * @return ActivityStack 의 인스턴스.
	 */
	public static ActivityStack getInstance() {
		if(mThis == null) {
			mThis = new ActivityStack();
		}
		return mThis;
	}
	
	/**
	 * 현재 프로세스 상에 존재하는 Task 의 ID 값들을 가져온다. 이 것은 Activity 내의 getTaskId 메소드로 얻을 수 있는 값과 일치한다. 
	 * @return TaskID TaskID 들의 리스트.
	 */
	public Integer[] getAliveTaskIDs() {
		cleanGarbageActivities();
		cleanGarbageActivities();
		Set<Integer> taskSet = new HashSet<Integer>();
		for(ActivityRef ref : mActivityAliveList) {
			taskSet.add(ref.taskID);
		}
		 Integer[] aliveTaskIds =  taskSet.toArray(new Integer[0]);
		return aliveTaskIds;
	}
	
	/**
	 * 특정 Activity 가 BaseActivity 로 있는 Task 의 ID 값들을 가져온다.
	 * @param baseActivityClass BaseActivity 가 되는 Activity 의 클래스 정보.
	 * @return TaskID 들의 리스트.
	 */
	public Integer[] getAliveTaskIDByBaseActvity(Class<?> baseActivityClass) {
		cleanGarbageActivities();
		ArrayList<ActivityRef> subList = subList(baseActivityClass);
		Set<Integer> taskSet = new HashSet<Integer>();
		for(ActivityRef ref : subList) {
			taskSet.add(ref.taskID);
		}
		 Integer[] aliveTaskIds =  taskSet.toArray(new Integer[0]);
		return aliveTaskIds;
	}
	
	/**
	 * 특정 Task 상에 있는 Activity 들의 AliveID 값들을 가져온다.<br/>
	 * AliveID 값은 [임의의 식별 번호]:[Class name of Activity] 로 되어 있다. <br/>
	 * 배열 내의 순서는 stack 그대로라고 볼 수 있다.
	 * 모든 Activity가 ActivityStack 를 사용할 경우 0 번 인덱스에 있는 Activity 는 base activity 이고, 가장 마지막에 있는 Activity 는 top Activity 가 된다.    
	 * @param taskID Task 의 id. Activity 의 getTaskId() 메소드를 이용하거나 ActivityManager 를 이용하여 가져온 task 값 입력. 
	 * @return AliveID : 현재 ActivityStack 내에서 관리되는 각 Activity 들의 ID 값. 
	 */
	public String[] getAliveIDsInTask(int taskID) {
		cleanGarbageActivities();
		ArrayList<ActivityRef> subList = subList(taskID);
		String[] aliveIDs = new String[size()];
		for(int i = 0, n = subList.size(); i < n; ++i) {
			aliveIDs[i] =  subList.get(i).aliveID;
		}
		return aliveIDs;
	}
	
	
	/**
	 * 모든 Task 에 존재하는 Activity 들의 AliveID 값들을 가져온다.<br/>
	 * AliveID 값은 [임의의 식별 번호]:[Class name of Activity] 로 되어 있다. <br/>
	 * 배열 내의 순서는 액티비티가 생성되고 종료된 순서와 일치한다. 만약 프로세스 내에 Task 가 하나만 존재한다면, 실제 안드로이드 내부에서 관리되는 Activity stack 과 일치한다.
	 * 가장 먼저 생성된 Activity 는 0 번 인덱스에 위치하게 되고 가장 마지막에 생성된 Activity 는 가장 마지막 인덱스에 위치하게 된다.    
	 * @return AliveID : 현재 ActivityStack 내에서 관리되는 각 Activity 들의 ID 값.
	 */
	public String[] getAliveIDs() {
		cleanGarbageActivities();
		String[] aliveIDs = new String[size()];
		for(int i = 0, n = size(); i < n; ++i) {
			aliveIDs[i] =  mActivityAliveList.get(i).aliveID;
		}
		return aliveIDs;
	}
	
	/**
	 * AliveID 를 가져온다.
	 * @param activity 만약 ActivityStack 에 등록된 Activity가 아니라면 빈 문자열을 반환한다.
	 */
	public String getAliveID(Activity activity) {
		ActivityRef ref = getActivityRef(activity);
		if(ref != null) return ref.aliveID;
		else return "";
	}
	
		
	/**
	 * 특정 Task 상에 있는 Activity 중에 스택의 가장 위에 있는 (예를 들자면 화면에 보여지는) Activity instance 를 가져온다.
	 * @param taskID Task 의 id. Activity 의 getTaskId() 메소드를 이용하거나 ActivityManager 를 이용하여 가져온 task 값 입력.
	 * @return Activity instance
	 */
	public Activity getTopActivityInTask(int taskID) {
		cleanGarbageActivities();
		ArrayList<ActivityRef> subList = subList(taskID);
		int size =  subList.size(); 
		if(size > 0) {
			return subList.get(size - 1).ref.get();
		}
		return null;
	}
	
	/**
	 * 특정 Task 상에 있는 Activity 중에 스택의 가장 아래에 있는 Activity 의 instance 를 가져온다.
	 * @return Activity instance
	 */
	public Activity getBaseActivityInTask(int taskID) {
		cleanGarbageActivities();
		ArrayList<ActivityRef> subList = subList(taskID);
		if(subList.size() > 0) {
			return subList.get(0).ref.get();
		}
		return null;
	
	}
	
	/**
	 *  Activity 를 가져온다.
	 * @param AliveID - getAliveIDsInTask() 또는 getAliveIDs() 을 통하여 가져온 Activity 의 getAliveID 값을 넣는다. 
	 * @return Activity instance
	 */
	public Activity getActivity(String aliveID) {
		cleanGarbageActivities();
		return getActivityRef(aliveID).ref.get();
	}
	
	/**
	 * ActivityStack에 등록된 모든 Activity 들이 현재 살아있는지 체크한다.  	
	 * @return 만약 false 를 리턴한다면 실행되고 있는 (등록된) Activity 는 하나도 없는 것이다.  
	 */
	public boolean isRunning() {
		return size() > 0;
	}
	
	/**
	 * 특정 Task 내의 Activit 가 현재 살아있는지 체크한다.
	 * @param task Task 의 id. Activity 의 getTaskId() 메소드를 이용하거나 ActivityManager 를 이용하여 가져온 task 값 입력.
	 * @return 만약 false 를 리턴한다면 Task 내부에 실행되고 있는 (등록된) Activity 는 하나도 없는 것이다.
	 */
	public boolean isTaskRunning(int task) {
		cleanGarbageActivities();
		return subList(task).size() > 0;
	}
	
	/**
	 * 특정 Task 내의 ActivityStack 의 크기를 가져온다.
	 * @param task Task 의 id. Activity 의 getTaskId() 메소드를 이용하거나 ActivityManager 를 이용하여 가져온 task 값 입력.
	 * @return ActivityStack 의 크기. 0을 반환할 경우 Task가 없거나 실행되고 있는 Activity 가 아직 없는 것이다. 
	 */ 
	public int sizeInTask(int task) {
		cleanGarbageActivities();
		return subList(task).size();
	}
	
	
	/**
	 * ActivityStack 의 크기를 가져온다. 즉, 모든 등록된 Activity 의 개수를 가져오는 것이다. 
	 * @return ActivityStack 의 크기. 0을 반환할 경우 Task가 없거나 실행되고 있는 Activity 가 아직 없는 것이다. 
	 */
	public int size() {
		cleanGarbageActivities();
		return mActivityAliveList.size();
	}
	
	/**
	 * Foreground 상태인가? - 등록된 액티비티중 하나라도 화면에 가장 위에 보여지는 경우. 	
	 * @return false 일 경우 background 상태. 다른 프로세스의 액티비티등에 의해서 일부만 가려져도 false 리턴. resume 상태인 activity 가 있다면 true.
	 */
	public boolean isForeground() {
		Activity activity = getForegroundActivity();
		return activity != null;
	}
	
	/**
	 *  특정 Task 가 Foreground 상태인가? - 등록된 액티비티중 하나라도 화면에 가장 위에 보여지는 경우.
	 *  @param task Task 의 id. Activity 의 getTaskId() 메소드를 이용하거나 ActivityManager 를 이용하여 가져온 task 값 입력.
	 *  @return false 일 경우 background 상태. 다른 프로세스의 액티비티등에 의해서 일부만 가려져도 false 리턴. resume 상태인 activity 가 있다면 true.
	 */
	public boolean isForeground(int taskId) {
		Activity activity = getForegroundActivity();
		return activity != null && activity.getTaskId() == taskId;
	}
	
	/**
	 * Foreground 상태. 즉 화면에 출력되는 Activity 의 instance 를 가져온다.
	 * @return 만약 background 상태라면 null 을 리턴한다. 
	 */
	public Activity getForegroundActivity() {
		cleanGarbageActivities();
		ActivityRef ref = getResumeActivityRef();
		if(ref != null && ref.ref != null) return ref.ref.get();
		return null;
	}
	
	/**
	 * resume 상태. 반드시 Activity 내부의 onResume() 에서 호출해야 한다.
	 * @param activity 대상 activity 
	 * @return 대상 Activity 가 등록되어 있지 않았다면 false. false 를 리턴하는 경우는 setCreateState 메소드를 이용하여 activity 를 등록하지 않았거나 destroy 된 activity 일 경우.
	 */
	public boolean regOnResumeState(Activity activity) {
		cleanGarbageActivities();
		ActivityRef ref = getActivityRef(activity);
		if(ref != null) {
			ref.isResume = true;
			return true;
		}
		return false;
	}

	/**
	 * pause 상태. 반드시 Activity 내부의 onPause() 에서 호출해야 한다.
	 * @param activity 대상 activity 
	 * @return 대상 Activity 가 등록되어 있지 않았다면 false. false 를 리턴하는 경우는 setCreateState 메소드를 이용하여 activity 를 등록하지 않았거나 destroy 된 activity 일 경우.
	 */
	public boolean regOnPauseState(Activity activity) {
		cleanGarbageActivities();
		ActivityRef ref = getActivityRef(activity);
		if(ref != null) {
			ref.isResume = false;
			return true;
		}
		return false; 
	}
	
	/**
	 * Activity 가 생성되어 시작되는 상태. 반드시 Activity 내부의 onCreate() 에서 호출해야한다. 이 메소드를 호출하면 ActivityStack 에 인자값으로 들어간 activity 가 등록된다. 
	 * @param activity 대상 activity 
	 * @return 대상 Activity 가 등록되어 있지 않았다면 false. false 를 리턴하는 경우는 setCreateState 메소드를 이용하여 activity 를 등록하지 않았거나 destroy 된 activity 일 경우.
	 */
	public final void  regOnCreateState(Activity activity) {
		cleanGarbageActivities();
		addActivityRef(activity);
	}
	
	/**
	 * Activity 가 제거되는 상태. 반드시 Activity 내부의 onDestroy() 에서 호출해야한다. 이 메소드를 호출하면 ActivityStack 에 인자값으로 들어간 activity 가 삭제된다. 
	 * @param activity 대상 activity 
	 * @return 대상 Activity 가 등록되어 있지 않았다면 false. false 를 리턴하는 경우는 setCreateState 메소드를 이용하여 activity 를 등록하지 않았거나 destroy 된 activity 일 경우.
	 */
	public final boolean regOnDestroyState(Activity activity) {
 		return removeActivityRef(activity);
		
	}
	
	
	/**
	 * 예:) [{"taskId":1,activityStack:["0:com.test.activity","1:com.test.activity"...,]},
	 * 	    {"taskId":2,activityStack:["10:com.test.activity","11:com.test.activity"...,]},]  
	 */
	public String toString() {
		cleanGarbageActivities();
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("[");
		Integer[] taskIDs  =  getAliveTaskIDs();
		for(Integer taskID : taskIDs) {
			strBuilder.append("{\"taskId\":").append(taskID).append(",\"activityStack\":[");
			ArrayList<ActivityRef> subList = subList(taskID);
			for(ActivityRef ref : subList) {
				strBuilder.append("\"").append(ref.aliveID).append("\",");
			}
			strBuilder.append("]},");
		}
		strBuilder.append("]");
		return strBuilder.toString();
		
		
	}	
	
	
// private : 
	
	private ActivityRef createActivityRef(Activity activity) {
		ActivityRef activityRef = new ActivityRef();
		activityRef.ref = new WeakReference<Activity>(activity);
		activityRef.aliveID =  makeAliveID(activity);
		activityRef.taskID =  activity.getTaskId();
		return activityRef;
	}
	
	
	
	private String makeAliveID(Activity activity) {
		return sLastActivityNumber++ + ":" + activity.getClass().getName();
	}
	
	
	private void addActivityRef(Activity activity) {
		ActivityRef activityRef = createActivityRef(activity);
		mActivityAliveList.add(activityRef);
	}
	
	private boolean removeActivityRef(Activity activity) {
		Iterator<ActivityRef> iter =  mActivityAliveList.iterator();
		while(iter.hasNext()) {
			ActivityRef iterItem = iter.next();
			Activity itemActivity = iterItem.ref.get();
			if(iterItem.ref.get() != null && activity == itemActivity) {
				iter.remove();
				return true;
			}
		}
		return false;
	}
	
	private ActivityRef getActivityRef(Activity activity) {
		Iterator<ActivityRef> iter =  mActivityAliveList.iterator();
		while(iter.hasNext()) {
			ActivityRef iterItem = iter.next();
			Activity activityItem =  iterItem.ref.get();
			if(activityItem != null && activityItem == activity) return iterItem;
		}
		return null;
	}
	
	private ActivityRef getActivityRef(String aliveID) {
		Iterator<ActivityRef> iter =  mActivityAliveList.iterator();
		while(iter.hasNext()) {
			ActivityRef iterItem = iter.next();
			if(iterItem.aliveID.equals(aliveID)) return iterItem;
		}
		return null;
	}
	
	private ActivityRef getResumeActivityRef() {
		Iterator<ActivityRef> iter =  mActivityAliveList.iterator();
		while(iter.hasNext()) {
			ActivityRef iterItem = iter.next();
			Activity activityItem =  iterItem.ref.get();
			if(activityItem != null && iterItem.isResume) return iterItem;
		}
		return null;
	}
	
	
	private ArrayList<ActivityRef> subList(int taskID) {
		ArrayList<ActivityRef> subList = new ArrayList<ActivityRef>();
		Iterator<ActivityRef> iter =  mActivityAliveList.iterator();
		while(iter.hasNext()) {
			ActivityRef iterItem = iter.next();
			if(iterItem.taskID == taskID) subList.add(iterItem);
		}
		return subList;
	}
	
	
	private ArrayList<ActivityRef> subList(Class<?> activityClass) {
		ArrayList<ActivityRef> subList = new ArrayList<ActivityRef>();
		Iterator<ActivityRef> iter =  mActivityAliveList.iterator();
		while(iter.hasNext()) {
			ActivityRef iterItem = iter.next();
			if(iterItem.ref.get() != null && iterItem.ref.get().getClass().equals(activityClass)) subList.add(iterItem);
		}
		return subList;
	}
	
	@SuppressLint("NewApi")
	private void cleanGarbageActivities() {
		Iterator<ActivityRef> iter =  mActivityAliveList.iterator();
		while(iter.hasNext()) {
			ActivityRef iterItem = iter.next();
			if(iterItem.ref.get() == null || 
					(android.os.Build.VERSION.SDK_INT >= 17 && iterItem.ref.get().isDestroyed()))    {
				iter.remove();
				Log.i("remove", "destroyed : " + iterItem.aliveID);
			}
		}
	}
	
	
	
	

	
	
	
}
