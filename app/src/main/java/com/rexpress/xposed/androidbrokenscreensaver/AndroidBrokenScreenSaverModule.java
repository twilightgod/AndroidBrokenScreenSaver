package com.rexpress.xposed.androidbrokenscreensaver;

import android.app.Activity;
import android.view.MotionEvent;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.*;

public class AndroidBrokenScreenSaverModule implements IXposedHookLoadPackage {

    public static final String this_package = AndroidBrokenScreenSaverModule.class.getPackage().getName();

    public void XLog(String str)
    {
        XposedBridge.log(this.this_package + " : " + str);
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

        //https://developer.android.com/reference/android/app/Activity.html#dispatchTouchEvent(android.view.MotionEvent)
        try {
            XposedHelpers.findAndHookMethod("android.app.Activity", lpparam.classLoader, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    MotionEvent event = (MotionEvent) param.args[0];
                    //XLog("dispatchTouchEvent hooked: " + event.toString());

                     float X = event.getX();
                     float Y = event.getY();

                     if (Math.abs(Y - 259.8) < 1e-1 || Math.abs(Y - 211.8) < 1e-1) {
                         param.setResult(true);

                         // write log
                         String msg = "Throw away " + event.toString();
                         XLog(msg);

                         Object currentObj = param.thisObject;
                         Activity currentActivity;
                         if (currentObj instanceof Activity) {
                             currentActivity = (Activity) currentObj;
                             Toast.makeText(currentActivity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                         }
                     }
                }
            });

        } catch (Exception E) {
            XLog(E.toString());
        }

    }
}
