package b4a.example;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = false;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mostCurrent = this;
		if (processBA == null) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.setActivityPaused(true);
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(this, processBA, wl, false))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.main");
        anywheresoftware.b4a.keywords.Common.ToastMessageShow("This application was developed with B4A trial version and should not be distributed.", true);
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.example.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEventFromUI(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null)
            return;
        if (this != mostCurrent)
			return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        if (mostCurrent != null)
            processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
            main mc = mostCurrent;
			if (mc == null || mc != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
            if (mc != mostCurrent)
                return;
		    processBA.raiseEvent(mc._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        for (int i = 0;i < permissions.length;i++) {
            Object[] o = new Object[] {permissions[i], grantResults[i] == 0};
            processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
        }
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public static anywheresoftware.b4a.objects.Timer _t1 = null;
public static anywheresoftware.b4a.objects.Timer _timerbuttonslidingdown = null;
public static anywheresoftware.b4a.objects.MediaPlayerWrapper _mpbacksound = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imageviewh = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imgh1 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imgh2 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imgh3 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imgh4 = null;
public static byte _h = (byte)0;
public static byte _object_move = (byte)0;
public static byte _score_val = (byte)0;
public static int _topimgh = 0;
public static int _leftimgc = 0;
public static int _leftimgf = 0;
public static int _leftimgff = 0;
public anywheresoftware.b4a.objects.ButtonWrapper _buttonjump = null;
public anywheresoftware.b4a.objects.ButtonWrapper _buttonsliding = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imageviewc = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imageviewsliding = null;
public anywheresoftware.b4a.objects.LabelWrapper _labelscore = null;
public static boolean _jump_fly = false;
public static boolean _fail = false;
public static boolean _finish = false;
public anywheresoftware.b4a.objects.ImageViewWrapper _imageviewf = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imageviewff = null;
public anywheresoftware.b4a.objects.LabelWrapper _labelstart = null;
public anywheresoftware.b4a.objects.ButtonWrapper _buttonstart = null;
public anywheresoftware.b4a.audio.SoundPoolWrapper _soundjump = null;
public static int _loadid = 0;
public static int _speed = 0;
public b4a.example.starter _starter = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 56;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 59;BA.debugLine="Activity.LoadLayout(\"Main\")";
mostCurrent._activity.LoadLayout("Main",mostCurrent.activityBA);
 //BA.debugLineNum = 61;BA.debugLine="ImgH1.Initialize(\"\")";
mostCurrent._imgh1.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 62;BA.debugLine="ImgH2.Initialize(\"\")";
mostCurrent._imgh2.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 63;BA.debugLine="ImgH3.Initialize(\"\")";
mostCurrent._imgh3.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 64;BA.debugLine="ImgH4.Initialize(\"\")";
mostCurrent._imgh4.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 65;BA.debugLine="ImgH1.Bitmap=LoadBitmap(File.DirAssets,\"w1.png\")";
mostCurrent._imgh1.setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"w1.png").getObject()));
 //BA.debugLineNum = 66;BA.debugLine="ImgH2.Bitmap=LoadBitmap(File.DirAssets,\"w2.png\")";
mostCurrent._imgh2.setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"w2.png").getObject()));
 //BA.debugLineNum = 67;BA.debugLine="ImgH3.Bitmap=LoadBitmap(File.DirAssets,\"w3.png\")";
mostCurrent._imgh3.setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"w3.png").getObject()));
 //BA.debugLineNum = 68;BA.debugLine="ImgH4.Bitmap=LoadBitmap(File.DirAssets,\"w4.png\")";
mostCurrent._imgh4.setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"w4.png").getObject()));
 //BA.debugLineNum = 70;BA.debugLine="T1.Initialize(\"T1\",150)";
_t1.Initialize(processBA,"T1",(long) (150));
 //BA.debugLineNum = 71;BA.debugLine="T1.Enabled=True";
_t1.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 73;BA.debugLine="TimerButtonSlidingDown.Initialize(\"TimerButtonSli";
_timerbuttonslidingdown.Initialize(processBA,"TimerButtonSlidingDown",(long) (10));
 //BA.debugLineNum = 74;BA.debugLine="TopImgH=ImageViewH.Top";
_topimgh = mostCurrent._imageviewh.getTop();
 //BA.debugLineNum = 76;BA.debugLine="ImageViewC.Left=100%x+ImageViewC.Width";
mostCurrent._imageviewc.setLeft((int) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)+mostCurrent._imageviewc.getWidth()));
 //BA.debugLineNum = 77;BA.debugLine="LeftImgC=ImageViewC.Left";
_leftimgc = mostCurrent._imageviewc.getLeft();
 //BA.debugLineNum = 79;BA.debugLine="ImageViewF.Left=100%x+ImageViewF.Width";
mostCurrent._imageviewf.setLeft((int) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)+mostCurrent._imageviewf.getWidth()));
 //BA.debugLineNum = 80;BA.debugLine="LeftImgF=ImageViewF.Left";
_leftimgf = mostCurrent._imageviewf.getLeft();
 //BA.debugLineNum = 82;BA.debugLine="ImageViewFF.Left = 100%x + ImageViewFF.Width";
mostCurrent._imageviewff.setLeft((int) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)+mostCurrent._imageviewff.getWidth()));
 //BA.debugLineNum = 83;BA.debugLine="LeftImgFF = ImageViewFF.Left";
_leftimgff = mostCurrent._imageviewff.getLeft();
 //BA.debugLineNum = 85;BA.debugLine="ImageViewSliding.Left=ImageViewH.Left";
mostCurrent._imageviewsliding.setLeft(mostCurrent._imageviewh.getLeft());
 //BA.debugLineNum = 86;BA.debugLine="ImageViewSliding.Visible=False";
mostCurrent._imageviewsliding.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 88;BA.debugLine="SoundJump.Initialize(1)";
mostCurrent._soundjump.Initialize((int) (1));
 //BA.debugLineNum = 89;BA.debugLine="loadid=SoundJump.Load(File.DirAssets, \"jump carto";
_loadid = mostCurrent._soundjump.Load(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"jump cartoon ok.wav");
 //BA.debugLineNum = 90;BA.debugLine="SoundJump.Play(loadid,1,1,10,0,0)";
mostCurrent._soundjump.Play(_loadid,(float) (1),(float) (1),(int) (10),(int) (0),(float) (0));
 //BA.debugLineNum = 92;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 98;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 99;BA.debugLine="ExitApplication";
anywheresoftware.b4a.keywords.Common.ExitApplication();
 //BA.debugLineNum = 100;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 94;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 96;BA.debugLine="End Sub";
return "";
}
public static String  _buttonjump_click() throws Exception{
 //BA.debugLineNum = 278;BA.debugLine="Sub ButtonJump_Click";
 //BA.debugLineNum = 279;BA.debugLine="Jump_Fly = True";
_jump_fly = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 280;BA.debugLine="SoundJump.Play(loadid,1,1,50,0,0)";
mostCurrent._soundjump.Play(_loadid,(float) (1),(float) (1),(int) (50),(int) (0),(float) (0));
 //BA.debugLineNum = 281;BA.debugLine="Fly";
_fly();
 //BA.debugLineNum = 283;BA.debugLine="End Sub";
return "";
}
public static String  _buttonsliding_down() throws Exception{
 //BA.debugLineNum = 285;BA.debugLine="Sub ButtonSliding_Down";
 //BA.debugLineNum = 286;BA.debugLine="TimerButtonSlidingDown.Enabled=True";
_timerbuttonslidingdown.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 288;BA.debugLine="End Sub";
return "";
}
public static String  _buttonsliding_up() throws Exception{
 //BA.debugLineNum = 290;BA.debugLine="Sub ButtonSliding_Up";
 //BA.debugLineNum = 291;BA.debugLine="TimerButtonSlidingDown.Enabled = False";
_timerbuttonslidingdown.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 292;BA.debugLine="ImageViewSliding.Visible = False";
mostCurrent._imageviewsliding.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 293;BA.debugLine="ImageViewH.Visible = True";
mostCurrent._imageviewh.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 294;BA.debugLine="End Sub";
return "";
}
public static void  _buttonstart_click() throws Exception{
ResumableSub_ButtonStart_Click rsub = new ResumableSub_ButtonStart_Click(null);
rsub.resume(processBA, null);
}
public static class ResumableSub_ButtonStart_Click extends BA.ResumableSub {
public ResumableSub_ButtonStart_Click(b4a.example.main parent) {
this.parent = parent;
}
b4a.example.main parent;
int _i = 0;
int step2;
int limit2;

@Override
public void resume(BA ba, Object[] result) throws Exception{

    while (true) {
        switch (state) {
            case -1:
return;

case 0:
//C
this.state = 1;
 //BA.debugLineNum = 299;BA.debugLine="ButtonStart.Visible = False";
parent.mostCurrent._buttonstart.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 301;BA.debugLine="For i = 3 To 1 Step -1";
if (true) break;

case 1:
//for
this.state = 4;
step2 = -1;
limit2 = (int) (1);
_i = (int) (3) ;
this.state = 5;
if (true) break;

case 5:
//C
this.state = 4;
if ((step2 > 0 && _i <= limit2) || (step2 < 0 && _i >= limit2)) this.state = 3;
if (true) break;

case 6:
//C
this.state = 5;
_i = ((int)(0 + _i + step2)) ;
if (true) break;

case 3:
//C
this.state = 6;
 //BA.debugLineNum = 302;BA.debugLine="LabelStart.Text = i";
parent.mostCurrent._labelstart.setText(BA.ObjectToCharSequence(_i));
 //BA.debugLineNum = 303;BA.debugLine="Sleep(1000)";
anywheresoftware.b4a.keywords.Common.Sleep(mostCurrent.activityBA,this,(int) (1000));
this.state = 7;
return;
case 7:
//C
this.state = 6;
;
 if (true) break;
if (true) break;

case 4:
//C
this.state = -1;
;
 //BA.debugLineNum = 306;BA.debugLine="LabelStart.Text = \"Go...!\"";
parent.mostCurrent._labelstart.setText(BA.ObjectToCharSequence("Go...!"));
 //BA.debugLineNum = 307;BA.debugLine="Sleep(500)";
anywheresoftware.b4a.keywords.Common.Sleep(mostCurrent.activityBA,this,(int) (500));
this.state = 8;
return;
case 8:
//C
this.state = -1;
;
 //BA.debugLineNum = 308;BA.debugLine="LabelStart.Visible = False";
parent.mostCurrent._labelstart.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 310;BA.debugLine="MPBackSound.Initialize";
parent._mpbacksound.Initialize();
 //BA.debugLineNum = 311;BA.debugLine="MPBackSound.Load(File.DirAssets, \"back sound ok.w";
parent._mpbacksound.Load(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"back sound ok.wav");
 //BA.debugLineNum = 312;BA.debugLine="MPBackSound.Play";
parent._mpbacksound.Play();
 //BA.debugLineNum = 313;BA.debugLine="MPBackSound.Looping = True";
parent._mpbacksound.setLooping(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 315;BA.debugLine="Finish = False";
parent._finish = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 316;BA.debugLine="C_Move";
_c_move();
 //BA.debugLineNum = 317;BA.debugLine="Cek";
_cek();
 //BA.debugLineNum = 318;BA.debugLine="Return";
if (true) return ;
 //BA.debugLineNum = 320;BA.debugLine="End Sub";
if (true) break;

            }
        }
    }
}
public static void  _c_move() throws Exception{
ResumableSub_C_Move rsub = new ResumableSub_C_Move(null);
rsub.resume(processBA, null);
}
public static class ResumableSub_C_Move extends BA.ResumableSub {
public ResumableSub_C_Move(b4a.example.main parent) {
this.parent = parent;
}
b4a.example.main parent;
int _i = 0;
int step8;
int limit8;
int step25;
int limit25;
int step42;
int limit42;

@Override
public void resume(BA ba, Object[] result) throws Exception{

    while (true) {
        switch (state) {
            case -1:
return;

case 0:
//C
this.state = 1;
 //BA.debugLineNum = 182;BA.debugLine="Object_Move = Rnd(1, 4)";
parent._object_move = (byte) (anywheresoftware.b4a.keywords.Common.Rnd((int) (1),(int) (4)));
 //BA.debugLineNum = 184;BA.debugLine="If Score_Val < 20 Then Speed = -8dip";
if (true) break;

case 1:
//if
this.state = 6;
if (parent._score_val<20) { 
this.state = 3;
;}if (true) break;

case 3:
//C
this.state = 6;
parent._speed = (int) (-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (8)));
if (true) break;

case 6:
//C
this.state = 7;
;
 //BA.debugLineNum = 185;BA.debugLine="If Score_Val > 20 And Score_Val < 29 Then Speed =";
if (true) break;

case 7:
//if
this.state = 12;
if (parent._score_val>20 && parent._score_val<29) { 
this.state = 9;
;}if (true) break;

case 9:
//C
this.state = 12;
parent._speed = (int) (-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (9)));
if (true) break;

case 12:
//C
this.state = 13;
;
 //BA.debugLineNum = 186;BA.debugLine="If Score_Val > 30 And Score_Val < 39 Then Speed =";
if (true) break;

case 13:
//if
this.state = 18;
if (parent._score_val>30 && parent._score_val<39) { 
this.state = 15;
;}if (true) break;

case 15:
//C
this.state = 18;
parent._speed = (int) (-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10)));
if (true) break;

case 18:
//C
this.state = 19;
;
 //BA.debugLineNum = 187;BA.debugLine="If Score_Val > 40 And Score_Val < 50 Then Speed =";
if (true) break;

case 19:
//if
this.state = 24;
if (parent._score_val>40 && parent._score_val<50) { 
this.state = 21;
;}if (true) break;

case 21:
//C
this.state = 24;
parent._speed = (int) (-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (11)));
if (true) break;

case 24:
//C
this.state = 25;
;
 //BA.debugLineNum = 189;BA.debugLine="Select Object_Move";
if (true) break;

case 25:
//select
this.state = 65;
switch (BA.switchObjectToInt(parent._object_move,(byte) (1),(byte) (2),(byte) (3))) {
case 0: {
this.state = 27;
if (true) break;
}
case 1: {
this.state = 40;
if (true) break;
}
case 2: {
this.state = 53;
if (true) break;
}
}
if (true) break;

case 27:
//C
this.state = 28;
 //BA.debugLineNum = 191;BA.debugLine="For i = ImageViewC.Left To 0dip - ImageViewC.Wi";
if (true) break;

case 28:
//for
this.state = 38;
step8 = parent._speed;
limit8 = (int) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0))-parent.mostCurrent._imageviewc.getWidth());
_i = parent.mostCurrent._imageviewc.getLeft() ;
this.state = 66;
if (true) break;

case 66:
//C
this.state = 38;
if ((step8 > 0 && _i <= limit8) || (step8 < 0 && _i >= limit8)) this.state = 30;
if (true) break;

case 67:
//C
this.state = 66;
_i = ((int)(0 + _i + step8)) ;
if (true) break;

case 30:
//C
this.state = 31;
 //BA.debugLineNum = 192;BA.debugLine="ImageViewC.Left = i";
parent.mostCurrent._imageviewc.setLeft(_i);
 //BA.debugLineNum = 193;BA.debugLine="Sleep(3)";
anywheresoftware.b4a.keywords.Common.Sleep(mostCurrent.activityBA,this,(int) (3));
this.state = 68;
return;
case 68:
//C
this.state = 31;
;
 //BA.debugLineNum = 195;BA.debugLine="If Fail = True Then";
if (true) break;

case 31:
//if
this.state = 34;
if (parent._fail==anywheresoftware.b4a.keywords.Common.True) { 
this.state = 33;
}if (true) break;

case 33:
//C
this.state = 34;
 //BA.debugLineNum = 196;BA.debugLine="Msgbox(\"Your Score is : \" & Score_Val, \"Game";
anywheresoftware.b4a.keywords.Common.Msgbox(BA.ObjectToCharSequence("Your Score is : "+BA.NumberToString(parent._score_val)),BA.ObjectToCharSequence("Game Over"),mostCurrent.activityBA);
 //BA.debugLineNum = 197;BA.debugLine="Fail = False";
parent._fail = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 198;BA.debugLine="Refresh";
_refresh();
 //BA.debugLineNum = 199;BA.debugLine="Return";
if (true) return ;
 if (true) break;
;
 //BA.debugLineNum = 202;BA.debugLine="If Finish = True Then";

case 34:
//if
this.state = 37;
if (parent._finish==anywheresoftware.b4a.keywords.Common.True) { 
this.state = 36;
}if (true) break;

case 36:
//C
this.state = 37;
 //BA.debugLineNum = 203;BA.debugLine="Refresh";
_refresh();
 //BA.debugLineNum = 204;BA.debugLine="Return";
if (true) return ;
 if (true) break;

case 37:
//C
this.state = 67;
;
 if (true) break;
if (true) break;

case 38:
//C
this.state = 65;
;
 //BA.debugLineNum = 207;BA.debugLine="ImageViewC.Left = LeftImgC";
parent.mostCurrent._imageviewc.setLeft(parent._leftimgc);
 //BA.debugLineNum = 208;BA.debugLine="Score";
_score();
 if (true) break;

case 40:
//C
this.state = 41;
 //BA.debugLineNum = 210;BA.debugLine="For i = ImageViewF.Left To 0dip - ImageViewF.Wi";
if (true) break;

case 41:
//for
this.state = 51;
step25 = parent._speed;
limit25 = (int) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0))-parent.mostCurrent._imageviewf.getWidth());
_i = parent.mostCurrent._imageviewf.getLeft() ;
this.state = 69;
if (true) break;

case 69:
//C
this.state = 51;
if ((step25 > 0 && _i <= limit25) || (step25 < 0 && _i >= limit25)) this.state = 43;
if (true) break;

case 70:
//C
this.state = 69;
_i = ((int)(0 + _i + step25)) ;
if (true) break;

case 43:
//C
this.state = 44;
 //BA.debugLineNum = 211;BA.debugLine="ImageViewF.Left = i";
parent.mostCurrent._imageviewf.setLeft(_i);
 //BA.debugLineNum = 212;BA.debugLine="Sleep(3)";
anywheresoftware.b4a.keywords.Common.Sleep(mostCurrent.activityBA,this,(int) (3));
this.state = 71;
return;
case 71:
//C
this.state = 44;
;
 //BA.debugLineNum = 214;BA.debugLine="If Fail = True Then";
if (true) break;

case 44:
//if
this.state = 47;
if (parent._fail==anywheresoftware.b4a.keywords.Common.True) { 
this.state = 46;
}if (true) break;

case 46:
//C
this.state = 47;
 //BA.debugLineNum = 215;BA.debugLine="Msgbox(\"Your Score is : \" & Score_Val, \"Game";
anywheresoftware.b4a.keywords.Common.Msgbox(BA.ObjectToCharSequence("Your Score is : "+BA.NumberToString(parent._score_val)),BA.ObjectToCharSequence("Game Over"),mostCurrent.activityBA);
 //BA.debugLineNum = 216;BA.debugLine="Fail = False";
parent._fail = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 217;BA.debugLine="Refresh";
_refresh();
 //BA.debugLineNum = 218;BA.debugLine="Return";
if (true) return ;
 if (true) break;
;
 //BA.debugLineNum = 221;BA.debugLine="If Finish = True Then";

case 47:
//if
this.state = 50;
if (parent._finish==anywheresoftware.b4a.keywords.Common.True) { 
this.state = 49;
}if (true) break;

case 49:
//C
this.state = 50;
 //BA.debugLineNum = 222;BA.debugLine="Refresh";
_refresh();
 //BA.debugLineNum = 223;BA.debugLine="Return";
if (true) return ;
 if (true) break;

case 50:
//C
this.state = 70;
;
 if (true) break;
if (true) break;

case 51:
//C
this.state = 65;
;
 //BA.debugLineNum = 226;BA.debugLine="ImageViewF.Left = LeftImgF";
parent.mostCurrent._imageviewf.setLeft(parent._leftimgf);
 //BA.debugLineNum = 227;BA.debugLine="Score";
_score();
 if (true) break;

case 53:
//C
this.state = 54;
 //BA.debugLineNum = 229;BA.debugLine="For i = ImageViewFF.Left To 0dip - ImageViewFF.";
if (true) break;

case 54:
//for
this.state = 64;
step42 = parent._speed;
limit42 = (int) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0))-parent.mostCurrent._imageviewff.getWidth());
_i = parent.mostCurrent._imageviewff.getLeft() ;
this.state = 72;
if (true) break;

case 72:
//C
this.state = 64;
if ((step42 > 0 && _i <= limit42) || (step42 < 0 && _i >= limit42)) this.state = 56;
if (true) break;

case 73:
//C
this.state = 72;
_i = ((int)(0 + _i + step42)) ;
if (true) break;

case 56:
//C
this.state = 57;
 //BA.debugLineNum = 230;BA.debugLine="ImageViewFF.Left = i";
parent.mostCurrent._imageviewff.setLeft(_i);
 //BA.debugLineNum = 231;BA.debugLine="Sleep(3)";
anywheresoftware.b4a.keywords.Common.Sleep(mostCurrent.activityBA,this,(int) (3));
this.state = 74;
return;
case 74:
//C
this.state = 57;
;
 //BA.debugLineNum = 233;BA.debugLine="If Fail = True Then";
if (true) break;

case 57:
//if
this.state = 60;
if (parent._fail==anywheresoftware.b4a.keywords.Common.True) { 
this.state = 59;
}if (true) break;

case 59:
//C
this.state = 60;
 //BA.debugLineNum = 234;BA.debugLine="Msgbox(\"Your Score is : \" & Score_Val, \"Game";
anywheresoftware.b4a.keywords.Common.Msgbox(BA.ObjectToCharSequence("Your Score is : "+BA.NumberToString(parent._score_val)),BA.ObjectToCharSequence("Game Over"),mostCurrent.activityBA);
 //BA.debugLineNum = 235;BA.debugLine="Fail = False";
parent._fail = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 236;BA.debugLine="Refresh";
_refresh();
 //BA.debugLineNum = 237;BA.debugLine="Return";
if (true) return ;
 if (true) break;
;
 //BA.debugLineNum = 240;BA.debugLine="If Finish = True Then";

case 60:
//if
this.state = 63;
if (parent._finish==anywheresoftware.b4a.keywords.Common.True) { 
this.state = 62;
}if (true) break;

case 62:
//C
this.state = 63;
 //BA.debugLineNum = 241;BA.debugLine="Refresh";
_refresh();
 //BA.debugLineNum = 242;BA.debugLine="Return";
if (true) return ;
 if (true) break;

case 63:
//C
this.state = 73;
;
 if (true) break;
if (true) break;

case 64:
//C
this.state = 65;
;
 //BA.debugLineNum = 245;BA.debugLine="ImageViewFF.Left = LeftImgFF";
parent.mostCurrent._imageviewff.setLeft(parent._leftimgff);
 //BA.debugLineNum = 246;BA.debugLine="Score";
_score();
 if (true) break;

case 65:
//C
this.state = -1;
;
 //BA.debugLineNum = 248;BA.debugLine="C_Move";
_c_move();
 //BA.debugLineNum = 249;BA.debugLine="End Sub";
if (true) break;

            }
        }
    }
}
public static void  _cek() throws Exception{
ResumableSub_Cek rsub = new ResumableSub_Cek(null);
rsub.resume(processBA, null);
}
public static class ResumableSub_Cek extends BA.ResumableSub {
public ResumableSub_Cek(b4a.example.main parent) {
this.parent = parent;
}
b4a.example.main parent;
int _i = 0;
int step9;
int limit9;

@Override
public void resume(BA ba, Object[] result) throws Exception{

    while (true) {
        switch (state) {
            case -1:
return;

case 0:
//C
this.state = 1;
 //BA.debugLineNum = 119;BA.debugLine="If Jump_Fly = False Then";
if (true) break;

case 1:
//if
this.state = 32;
if (parent._jump_fly==anywheresoftware.b4a.keywords.Common.False) { 
this.state = 3;
}else {
this.state = 12;
}if (true) break;

case 3:
//C
this.state = 4;
 //BA.debugLineNum = 120;BA.debugLine="If ImageViewC.Left <= ImageViewH.Left + ImageVie";
if (true) break;

case 4:
//if
this.state = 7;
if (parent.mostCurrent._imageviewc.getLeft()<=parent.mostCurrent._imageviewh.getLeft()+parent.mostCurrent._imageviewh.getWidth()) { 
this.state = 6;
}if (true) break;

case 6:
//C
this.state = 7;
 //BA.debugLineNum = 121;BA.debugLine="Fail = True";
parent._fail = anywheresoftware.b4a.keywords.Common.True;
 if (true) break;
;
 //BA.debugLineNum = 124;BA.debugLine="If ImageViewFF.Left <= ImageViewH.Left + ImageVi";

case 7:
//if
this.state = 10;
if (parent.mostCurrent._imageviewff.getLeft()<=parent.mostCurrent._imageviewh.getLeft()+parent.mostCurrent._imageviewh.getWidth()) { 
this.state = 9;
}if (true) break;

case 9:
//C
this.state = 10;
 //BA.debugLineNum = 125;BA.debugLine="Fail = True";
parent._fail = anywheresoftware.b4a.keywords.Common.True;
 if (true) break;

case 10:
//C
this.state = 32;
;
 if (true) break;

case 12:
//C
this.state = 13;
 //BA.debugLineNum = 128;BA.debugLine="For i = ImageViewH.Left To ImageViewH.Width Step";
if (true) break;

case 13:
//for
this.state = 31;
step9 = 1;
limit9 = parent.mostCurrent._imageviewh.getWidth();
_i = parent.mostCurrent._imageviewh.getLeft() ;
this.state = 40;
if (true) break;

case 40:
//C
this.state = 31;
if ((step9 > 0 && _i <= limit9) || (step9 < 0 && _i >= limit9)) this.state = 15;
if (true) break;

case 41:
//C
this.state = 40;
_i = ((int)(0 + _i + step9)) ;
if (true) break;

case 15:
//C
this.state = 16;
 //BA.debugLineNum = 129;BA.debugLine="If ImageViewC.Left = i Then";
if (true) break;

case 16:
//if
this.state = 23;
if (parent.mostCurrent._imageviewc.getLeft()==_i) { 
this.state = 18;
}if (true) break;

case 18:
//C
this.state = 19;
 //BA.debugLineNum = 130;BA.debugLine="If ImageViewH.Top + ImageViewH.Height >= Image";
if (true) break;

case 19:
//if
this.state = 22;
if (parent.mostCurrent._imageviewh.getTop()+parent.mostCurrent._imageviewh.getHeight()>=parent.mostCurrent._imageviewc.getTop()) { 
this.state = 21;
}if (true) break;

case 21:
//C
this.state = 22;
 //BA.debugLineNum = 131;BA.debugLine="Fail = True";
parent._fail = anywheresoftware.b4a.keywords.Common.True;
 if (true) break;

case 22:
//C
this.state = 23;
;
 if (true) break;
;
 //BA.debugLineNum = 135;BA.debugLine="If ImageViewFF.Left = i Then";

case 23:
//if
this.state = 30;
if (parent.mostCurrent._imageviewff.getLeft()==_i) { 
this.state = 25;
}if (true) break;

case 25:
//C
this.state = 26;
 //BA.debugLineNum = 136;BA.debugLine="If ImageViewH.Top + ImageViewH.Height >= Image";
if (true) break;

case 26:
//if
this.state = 29;
if (parent.mostCurrent._imageviewh.getTop()+parent.mostCurrent._imageviewh.getHeight()>=parent.mostCurrent._imageviewff.getTop()) { 
this.state = 28;
}if (true) break;

case 28:
//C
this.state = 29;
 //BA.debugLineNum = 137;BA.debugLine="Fail = True";
parent._fail = anywheresoftware.b4a.keywords.Common.True;
 if (true) break;

case 29:
//C
this.state = 30;
;
 if (true) break;

case 30:
//C
this.state = 41;
;
 if (true) break;
if (true) break;

case 31:
//C
this.state = 32;
;
 if (true) break;
;
 //BA.debugLineNum = 143;BA.debugLine="If ImageViewH.Visible = True And ImageViewSliding";

case 32:
//if
this.state = 39;
if (parent.mostCurrent._imageviewh.getVisible()==anywheresoftware.b4a.keywords.Common.True && parent.mostCurrent._imageviewsliding.getVisible()==anywheresoftware.b4a.keywords.Common.False) { 
this.state = 34;
}if (true) break;

case 34:
//C
this.state = 35;
 //BA.debugLineNum = 144;BA.debugLine="If ImageViewF.Left <= ImageViewH.Left + ImageVie";
if (true) break;

case 35:
//if
this.state = 38;
if (parent.mostCurrent._imageviewf.getLeft()<=parent.mostCurrent._imageviewh.getLeft()+parent.mostCurrent._imageviewh.getWidth()) { 
this.state = 37;
}if (true) break;

case 37:
//C
this.state = 38;
 //BA.debugLineNum = 145;BA.debugLine="Fail = True";
parent._fail = anywheresoftware.b4a.keywords.Common.True;
 if (true) break;

case 38:
//C
this.state = 39;
;
 if (true) break;

case 39:
//C
this.state = -1;
;
 //BA.debugLineNum = 148;BA.debugLine="Sleep(1)";
anywheresoftware.b4a.keywords.Common.Sleep(mostCurrent.activityBA,this,(int) (1));
this.state = 42;
return;
case 42:
//C
this.state = -1;
;
 //BA.debugLineNum = 149;BA.debugLine="Cek";
_cek();
 //BA.debugLineNum = 150;BA.debugLine="End Sub";
if (true) break;

            }
        }
    }
}
public static void  _fly() throws Exception{
ResumableSub_Fly rsub = new ResumableSub_Fly(null);
rsub.resume(processBA, null);
}
public static class ResumableSub_Fly extends BA.ResumableSub {
public ResumableSub_Fly(b4a.example.main parent) {
this.parent = parent;
}
b4a.example.main parent;
int _i = 0;
int step4;
int limit4;
int step11;
int limit11;

@Override
public void resume(BA ba, Object[] result) throws Exception{

    while (true) {
        switch (state) {
            case -1:
return;

case 0:
//C
this.state = 1;
 //BA.debugLineNum = 158;BA.debugLine="ButtonJump.Enabled = False";
parent.mostCurrent._buttonjump.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 159;BA.debugLine="ImageViewH.Bitmap = ImgH2.Bitmap";
parent.mostCurrent._imageviewh.setBitmap(parent.mostCurrent._imgh2.getBitmap());
 //BA.debugLineNum = 160;BA.debugLine="T1.Enabled = False";
parent._t1.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 162;BA.debugLine="For i = ImageViewH.Top To 10dip Step -8dip";
if (true) break;

case 1:
//for
this.state = 4;
step4 = (int) (-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (8)));
limit4 = anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10));
_i = parent.mostCurrent._imageviewh.getTop() ;
this.state = 9;
if (true) break;

case 9:
//C
this.state = 4;
if ((step4 > 0 && _i <= limit4) || (step4 < 0 && _i >= limit4)) this.state = 3;
if (true) break;

case 10:
//C
this.state = 9;
_i = ((int)(0 + _i + step4)) ;
if (true) break;

case 3:
//C
this.state = 10;
 //BA.debugLineNum = 163;BA.debugLine="ImageViewH.Top = i";
parent.mostCurrent._imageviewh.setTop(_i);
 //BA.debugLineNum = 164;BA.debugLine="Sleep(3)";
anywheresoftware.b4a.keywords.Common.Sleep(mostCurrent.activityBA,this,(int) (3));
this.state = 11;
return;
case 11:
//C
this.state = 10;
;
 if (true) break;
if (true) break;

case 4:
//C
this.state = 5;
;
 //BA.debugLineNum = 167;BA.debugLine="ImageViewH.Top = ImageViewH.Top";
parent.mostCurrent._imageviewh.setTop(parent.mostCurrent._imageviewh.getTop());
 //BA.debugLineNum = 168;BA.debugLine="Sleep(10)";
anywheresoftware.b4a.keywords.Common.Sleep(mostCurrent.activityBA,this,(int) (10));
this.state = 12;
return;
case 12:
//C
this.state = 5;
;
 //BA.debugLineNum = 169;BA.debugLine="T1.Enabled = True";
parent._t1.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 171;BA.debugLine="For i = 10dip To TopImgH Step 5dip";
if (true) break;

case 5:
//for
this.state = 8;
step11 = anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (5));
limit11 = parent._topimgh;
_i = anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10)) ;
this.state = 13;
if (true) break;

case 13:
//C
this.state = 8;
if ((step11 > 0 && _i <= limit11) || (step11 < 0 && _i >= limit11)) this.state = 7;
if (true) break;

case 14:
//C
this.state = 13;
_i = ((int)(0 + _i + step11)) ;
if (true) break;

case 7:
//C
this.state = 14;
 //BA.debugLineNum = 172;BA.debugLine="ImageViewH.Top = i";
parent.mostCurrent._imageviewh.setTop(_i);
 //BA.debugLineNum = 173;BA.debugLine="Sleep(3)";
anywheresoftware.b4a.keywords.Common.Sleep(mostCurrent.activityBA,this,(int) (3));
this.state = 15;
return;
case 15:
//C
this.state = 14;
;
 if (true) break;
if (true) break;

case 8:
//C
this.state = -1;
;
 //BA.debugLineNum = 176;BA.debugLine="T1.Enabled = True";
parent._t1.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 177;BA.debugLine="ButtonJump.Enabled = True";
parent.mostCurrent._buttonjump.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 178;BA.debugLine="Jump_Fly = False";
parent._jump_fly = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 179;BA.debugLine="End Sub";
if (true) break;

            }
        }
    }
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 23;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 27;BA.debugLine="Private ImageViewH As ImageView";
mostCurrent._imageviewh = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 28;BA.debugLine="Dim ImgH1, ImgH2, ImgH3, ImgH4 As ImageView";
mostCurrent._imgh1 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._imgh2 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._imgh3 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._imgh4 = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 30;BA.debugLine="Dim H As Byte = 0";
_h = (byte) (0);
 //BA.debugLineNum = 31;BA.debugLine="Dim Object_Move As Byte";
_object_move = (byte)0;
 //BA.debugLineNum = 32;BA.debugLine="Dim Score_Val As Byte";
_score_val = (byte)0;
 //BA.debugLineNum = 33;BA.debugLine="Dim TopImgH, LeftImgC, LeftImgF, LeftImgFF As Int";
_topimgh = 0;
_leftimgc = 0;
_leftimgf = 0;
_leftimgff = 0;
 //BA.debugLineNum = 35;BA.debugLine="Private ButtonJump As Button";
mostCurrent._buttonjump = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 36;BA.debugLine="Private ButtonSliding As Button";
mostCurrent._buttonsliding = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 37;BA.debugLine="Private ImageViewC As ImageView";
mostCurrent._imageviewc = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 38;BA.debugLine="Private ImageViewSliding As ImageView";
mostCurrent._imageviewsliding = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 39;BA.debugLine="Private LabelScore As Label";
mostCurrent._labelscore = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 41;BA.debugLine="Dim Jump_Fly As Boolean";
_jump_fly = false;
 //BA.debugLineNum = 42;BA.debugLine="Dim Fail As Boolean = False";
_fail = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 43;BA.debugLine="Dim Finish As Boolean = False";
_finish = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 44;BA.debugLine="Private ImageViewF As ImageView";
mostCurrent._imageviewf = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 45;BA.debugLine="Private ImageViewFF As ImageView";
mostCurrent._imageviewff = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 48;BA.debugLine="Private LabelStart As Label";
mostCurrent._labelstart = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 49;BA.debugLine="Private ButtonStart As Button";
mostCurrent._buttonstart = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 51;BA.debugLine="Dim SoundJump As SoundPool";
mostCurrent._soundjump = new anywheresoftware.b4a.audio.SoundPoolWrapper();
 //BA.debugLineNum = 52;BA.debugLine="Dim loadid As Int";
_loadid = 0;
 //BA.debugLineNum = 53;BA.debugLine="Dim Speed As Int = -8dip";
_speed = (int) (-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (8)));
 //BA.debugLineNum = 54;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
starter._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 15;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 18;BA.debugLine="Dim T1,TimerButtonSlidingDown As Timer";
_t1 = new anywheresoftware.b4a.objects.Timer();
_timerbuttonslidingdown = new anywheresoftware.b4a.objects.Timer();
 //BA.debugLineNum = 19;BA.debugLine="Dim MPBackSound As MediaPlayer";
_mpbacksound = new anywheresoftware.b4a.objects.MediaPlayerWrapper();
 //BA.debugLineNum = 21;BA.debugLine="End Sub";
return "";
}
public static void  _refresh() throws Exception{
ResumableSub_Refresh rsub = new ResumableSub_Refresh(null);
rsub.resume(processBA, null);
}
public static class ResumableSub_Refresh extends BA.ResumableSub {
public ResumableSub_Refresh(b4a.example.main parent) {
this.parent = parent;
}
b4a.example.main parent;

@Override
public void resume(BA ba, Object[] result) throws Exception{

    while (true) {
        switch (state) {
            case -1:
return;

case 0:
//C
this.state = -1;
 //BA.debugLineNum = 266;BA.debugLine="Score_Val = 0";
parent._score_val = (byte) (0);
 //BA.debugLineNum = 267;BA.debugLine="LabelScore.Text = \"Score = \" & Score_Val";
parent.mostCurrent._labelscore.setText(BA.ObjectToCharSequence("Score = "+BA.NumberToString(parent._score_val)));
 //BA.debugLineNum = 268;BA.debugLine="ImageViewC.Left = LeftImgC";
parent.mostCurrent._imageviewc.setLeft(parent._leftimgc);
 //BA.debugLineNum = 269;BA.debugLine="ImageViewF.Left = LeftImgF";
parent.mostCurrent._imageviewf.setLeft(parent._leftimgf);
 //BA.debugLineNum = 270;BA.debugLine="ImageViewFF.Left = LeftImgFF";
parent.mostCurrent._imageviewff.setLeft(parent._leftimgff);
 //BA.debugLineNum = 271;BA.debugLine="Sleep(1000)";
anywheresoftware.b4a.keywords.Common.Sleep(mostCurrent.activityBA,this,(int) (1000));
this.state = 1;
return;
case 1:
//C
this.state = -1;
;
 //BA.debugLineNum = 272;BA.debugLine="LabelStart.Visible = True";
parent.mostCurrent._labelstart.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 273;BA.debugLine="ButtonStart.Visible = True";
parent.mostCurrent._buttonstart.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 274;BA.debugLine="MPBackSound.Stop";
parent._mpbacksound.Stop();
 //BA.debugLineNum = 275;BA.debugLine="Return";
if (true) return ;
 //BA.debugLineNum = 276;BA.debugLine="End Sub";
if (true) break;

            }
        }
    }
}
public static String  _score() throws Exception{
 //BA.debugLineNum = 251;BA.debugLine="Sub Score 'to calculate the score";
 //BA.debugLineNum = 252;BA.debugLine="If Fail = False Then";
if (_fail==anywheresoftware.b4a.keywords.Common.False) { 
 //BA.debugLineNum = 253;BA.debugLine="Score_Val = Score_Val + 1";
_score_val = (byte) (_score_val+1);
 };
 //BA.debugLineNum = 255;BA.debugLine="LabelScore.Text = \"Score = \" & Score_Val";
mostCurrent._labelscore.setText(BA.ObjectToCharSequence("Score = "+BA.NumberToString(_score_val)));
 //BA.debugLineNum = 257;BA.debugLine="If Score_Val = 50 Then";
if (_score_val==50) { 
 //BA.debugLineNum = 258;BA.debugLine="Msgbox(\"Finish\", \"\")";
anywheresoftware.b4a.keywords.Common.Msgbox(BA.ObjectToCharSequence("Finish"),BA.ObjectToCharSequence(""),mostCurrent.activityBA);
 //BA.debugLineNum = 259;BA.debugLine="Finish = True";
_finish = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 260;BA.debugLine="Refresh";
_refresh();
 };
 //BA.debugLineNum = 262;BA.debugLine="Return";
if (true) return "";
 //BA.debugLineNum = 263;BA.debugLine="End Sub";
return "";
}
public static String  _t1_tick() throws Exception{
 //BA.debugLineNum = 102;BA.debugLine="Sub T1_Tick 'to manpulate blue object to make them";
 //BA.debugLineNum = 103;BA.debugLine="H = H + 1";
_h = (byte) (_h+1);
 //BA.debugLineNum = 104;BA.debugLine="Select H";
switch (BA.switchObjectToInt(_h,(byte) (1),(byte) (2),(byte) (3),(byte) (4))) {
case 0: {
 //BA.debugLineNum = 106;BA.debugLine="ImageViewH.Bitmap = ImgH1.Bitmap";
mostCurrent._imageviewh.setBitmap(mostCurrent._imgh1.getBitmap());
 break; }
case 1: {
 //BA.debugLineNum = 108;BA.debugLine="ImageViewH.Bitmap = ImgH2.Bitmap";
mostCurrent._imageviewh.setBitmap(mostCurrent._imgh2.getBitmap());
 break; }
case 2: {
 //BA.debugLineNum = 110;BA.debugLine="ImageViewH.Bitmap = ImgH3.Bitmap";
mostCurrent._imageviewh.setBitmap(mostCurrent._imgh3.getBitmap());
 break; }
case 3: {
 //BA.debugLineNum = 112;BA.debugLine="ImageViewH.Bitmap = ImgH2.Bitmap";
mostCurrent._imageviewh.setBitmap(mostCurrent._imgh2.getBitmap());
 //BA.debugLineNum = 113;BA.debugLine="H = 0";
_h = (byte) (0);
 break; }
}
;
 //BA.debugLineNum = 115;BA.debugLine="End Sub";
return "";
}
public static String  _timerbuttonslidingdown_tick() throws Exception{
 //BA.debugLineNum = 152;BA.debugLine="Sub TimerButtonSlidingDown_Tick";
 //BA.debugLineNum = 153;BA.debugLine="ImageViewH.Visible = False";
mostCurrent._imageviewh.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 154;BA.debugLine="ImageViewSliding.Visible = True";
mostCurrent._imageviewsliding.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 155;BA.debugLine="End Sub";
return "";
}
}
