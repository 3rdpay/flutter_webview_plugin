package com.flutter_webview_plugin;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WebViewGestureLayout extends FrameLayout {
    private GestureDetector gestureDetector;
//    private Logger logger = new Logger(getClass().getSimpleName());
    //左右滑動多少距離才觸發事件
    private int THRESHOLD_X_MIN_DISTANCE = 400;
    //上下滑動小於多少距離才合法
    private int THRESHOLD_Y_MAX_DISTANCE = 300;
    //滑動速率大於多少才合法
    private int THRESHOLD_MIN_SPEED = 2000;
    private OnWebViewGestureListener listener;

    public WebViewGestureLayout(@NonNull Context context) {
        super(context);
        init(null);
    }

    public WebViewGestureLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
        THRESHOLD_X_MIN_DISTANCE = dp2px(getContext(), 150);
    }

    int dp2px(Context context, float value) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (value * scale + 0.5f);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
        return gestureDetector.onTouchEvent(ev);
    }

    public void setOnWebViewGestureListener(OnWebViewGestureListener listener) {
        this.listener = listener;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float dx = Math.abs(e1.getX() - e2.getX());
            float dy = Math.abs(e1.getY() - e2.getY());
            float speedX = Math.abs(velocityX);
            if (dx > THRESHOLD_X_MIN_DISTANCE && dy < THRESHOLD_Y_MAX_DISTANCE && speedX > THRESHOLD_MIN_SPEED) {
                if (listener != null) {
                    if (velocityX > 0)
                        listener.onBack();
                    else
                        listener.onForward();
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    public interface OnWebViewGestureListener {

        void onBack();

        void onForward();
    }

}