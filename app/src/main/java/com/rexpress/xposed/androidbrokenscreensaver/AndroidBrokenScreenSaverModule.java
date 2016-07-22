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
                    int pointerIndex = event.getActionIndex();
                    int pointerId = event.getPointerId(pointerIndex);

                    float X;
                    float Y;

                    // debug
                    for (int i = 0; i < event.getPointerCount(); ++i) {
                        X = event.getX(i);
                        Y = event.getY(i);

                        if (IsBadTouch(X, Y)) {
                            String msg = "[debugtouch] " + event.toString();
                            XLog(msg);
                            break;
                        }
                    }
                    MotionEvent.ob
                    switch (event.getActionMasked()) {
                        // Stop bad touch from begining...
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_POINTER_DOWN:
                            // Get the triggering touch
                            X = event.getX(pointerIndex);
                            Y = event.getY(pointerIndex);

                            // Skip this event, since it's triggered by a bad touch
                            if (IsBadTouch(X, Y)) {
                                param.setResult(true);
                            }
                            break;

                        // We can safely skip move event since down events are been taken care of
                        case MotionEvent.ACTION_MOVE:
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_POINTER_UP:
                            break;

                        default:
                            break;
                    }

                    if (param.getResult() != null && (Boolean)param.getResult()) {
                        // write log
                        String msg = "Throw away " + event.toString();
                        XLog(msg);
                    }


                         /*
                         Object currentObj = param.thisObject;
                         Activity currentActivity;
                         if (currentObj instanceof Activity) {
                             currentActivity = (Activity) currentObj;
                             //Toast.makeText(currentActivity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                         }*/
                }
            });

        } catch (Exception E) {
            XLog(E.toString());
        }

    }

    private boolean IsBadTouch(float X, float Y) {
        return Math.abs(Y - 259.8) < 1e-1 || Math.abs(Y - 211.8) < 1e-1 || Math.abs(Y - 70) < 1e-1;
    }
}
