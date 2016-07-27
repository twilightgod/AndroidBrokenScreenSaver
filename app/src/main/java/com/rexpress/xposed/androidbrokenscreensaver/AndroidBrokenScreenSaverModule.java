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
                    int action = event.getActionMasked();
                    int pointerIndex = event.getActionIndex();
                    int pointerId = event.getPointerId(pointerIndex);


                    float X;
                    float Y;

                    int toolType;

                    int j;

                    // Get triggering touch
                    X = event.getX(pointerIndex);
                    Y = event.getY(pointerIndex);
                    toolType = event.getToolType(pointerIndex);

                    // Triggered by bad touch
                    if (IsBadTouch(X, Y, toolType) && (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL)) {
                        //XLog("Triggered by bad touch, ignore. " + event.toString());
                        param.setResult(true);
                        return;
                    }

                    // Multi-touch, triggered by normal touch
                    if (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
                        boolean[] bad = new boolean[event.getPointerCount()];
                        int badCount = 0;

                        for (int i = 0; i < event.getPointerCount(); ++i) {
                            X = event.getX(i);
                            Y = event.getY(i);
                            toolType = event.getToolType(i);
                            if (IsBadTouch(X, Y, toolType)) {
                                bad[i] = true;
                                badCount++;
                            }
                            else {
                                bad[i] = false;
                            }
                        }

                        // No bad touch, skip
                        if (badCount == 0) {
                            return;
                        }

                        // Filter out bad touches
                        else {
                            int goodCount = event.getPointerCount() - badCount;

                            //getPointerProperties(int pointerIndex, MotionEvent.PointerProperties outPointerProperties)
                            //Populates a MotionEvent.PointerProperties object with pointer properties for the specified pointer index.
                            MotionEvent.PointerProperties[] pros = new MotionEvent.PointerProperties[goodCount];
                            j = 0;
                            for (int i = 0; i < event.getPointerCount(); ++i) {
                                if (!bad[i]) {
                                    pros[j] = new MotionEvent.PointerProperties();
                                    event.getPointerProperties(i, pros[j]);
                                    j++;
                                }
                            }

                            MotionEvent.PointerCoords[] coods = new MotionEvent.PointerCoords[goodCount];
                            j = 0;
                            for (int i = 0; i < event.getPointerCount(); ++i) {
                                if (!bad[i]) {
                                    coods[j] = new MotionEvent.PointerCoords();
                                    event.getPointerCoords(i, coods[j]);
                                    j++;
                                }
                            }

                            int newAction = action;

                            // Only one touch and it's good, translate to single touch
                            if (goodCount == 1) {
                                newAction = action == MotionEvent.ACTION_POINTER_DOWN ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_UP;
                            }
                            // The main touch is good, action type does't change
                            else {

                            }


                            MotionEvent newEvent = MotionEvent.obtain(
                                    event.getDownTime(),
                                    event.getEventTime(),
                                    newAction,
                                    goodCount,
                                    pros,
                                    coods,
                                    event.getMetaState(),
                                    event.getButtonState(),
                                    event.getXPrecision(),
                                    event.getYPrecision(),
                                    event.getDeviceId(),
                                    event.getEdgeFlags(),
                                    event.getSource(),
                                    event.getSource()
                            );

                            XLog("Filter up/down.");
                            XLog(event.toString());
                            XLog(newEvent.toString());
                            param.args[0] = newEvent;
                            return;
                        }
                    }

                    // Move
                    if (action == MotionEvent.ACTION_MOVE) {
                        boolean[] bad = new boolean[event.getPointerCount()];
                        int badCount = 0;

                        for (int i = 0; i < event.getPointerCount(); ++i) {
                            X = event.getX(i);
                            Y = event.getY(i);
                            toolType = event.getToolType(i);
                            if (IsBadTouch(X, Y, toolType)) {
                                bad[i] = true;
                                badCount++;
                            }
                            else {
                                bad[i] = false;
                            }
                        }

                        // No bad touch, skip
                        if (badCount == 0) {
                            return;
                        }
                        // Only bad touch
                        else if (badCount == event.getPointerCount()) {
                            //XLog("Move only contains bad touch, ignore. " + event.toString());
                            param.setResult(true);
                            return;
                        }
                        // Filter out bad touches
                        else {
                            int goodCount = event.getPointerCount() - badCount;

                            //getPointerProperties(int pointerIndex, MotionEvent.PointerProperties outPointerProperties)
                            //Populates a MotionEvent.PointerProperties object with pointer properties for the specified pointer index.
                            MotionEvent.PointerProperties[] pros = new MotionEvent.PointerProperties[goodCount];
                            j = 0;
                            for (int i = 0; i < event.getPointerCount(); ++i) {
                                if (!bad[i]) {
                                    pros[j] = new MotionEvent.PointerProperties();
                                    event.getPointerProperties(i, pros[j]);
                                    j++;
                                }
                            }

                            MotionEvent.PointerCoords[] coods = new MotionEvent.PointerCoords[goodCount];
                            j = 0;
                            for (int i = 0; i < event.getPointerCount(); ++i) {
                                if (!bad[i]) {
                                    coods[j] = new MotionEvent.PointerCoords();
                                    event.getPointerCoords(i, coods[j]);
                                    j++;
                                }
                            }

                            MotionEvent newEvent = MotionEvent.obtain(
                                    event.getDownTime(),
                                    event.getEventTime(),
                                    action,
                                    goodCount,
                                    pros,
                                    coods,
                                    event.getMetaState(),
                                    event.getButtonState(),
                                    event.getXPrecision(),
                                    event.getYPrecision(),
                                    event.getDeviceId(),
                                    event.getEdgeFlags(),
                                    event.getSource(),
                                    event.getSource()
                            );

                            XLog("Filter move.");
                            XLog(event.toString());
                            XLog(newEvent.toString());
                            param.args[0] = newEvent;
                            return;
                        }
                    }

                    XLog("Normal touch: " + event.toString());
                }
            });

        } catch (Exception E) {
            XLog(E.toString());
        }

    }

    private boolean IsBadTouch(float X, float Y, int toolType) {
        return toolType == MotionEvent.TOOL_TYPE_UNKNOWN || Math.abs(Y - 259.8) < 1 || Math.abs(Y - 127.9) < 1 || Math.abs(Y - 211.8) < 1 || Math.abs(Y - 70) < 1 || Math.abs(Y - 69) < 1 || Math.abs(Y - 68) < 1 || Math.abs(Y - 162.9) < 1 || Math.abs(Y - 163.9) < 1;
    }
}
