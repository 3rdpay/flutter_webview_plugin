package com.flutter_webview_plugin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import java.util.ArrayList;
import java.util.List;

public class AssistiveTouchView extends FrameLayout implements View.OnTouchListener {
    private AssistiveTouchView self = this;
    private ImageView imageView;
    private ViewGroup parent;
    private ImageView child1;
    private ImageView child2;
    private ImageView child3;
    private GestureDetector assistiveGestureDetector;
    private GestureDetector parentGestureDetector;
    private AssistiveGestureListener assistiveGestureListener;
    private ParentGestureListener parentGestureListener;
    private OnAssistiveListener assistiveListener;

    private float child1ExpandDegree;
    private float child2ExpandDegree;
    private float child3ExpandDegree;
    private float r = 300;
    private boolean isCollapsed = true;
    private int childPadding = 15;
    private OnChildClickListener listener;
    private Properties properties;
    private List<View> children = new ArrayList<>();
    private int topDiff = 0;

    static class Properties {
        int viewSize;
    }

    public AssistiveTouchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public void setTopDiff(int value) {
        topDiff = value;
    }

    @Override public boolean onTouch(View view, MotionEvent motionEvent) {
        return assistiveGestureDetector.onTouchEvent(motionEvent);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        parent = ((ViewGroup) getParent());
        parent.setClipChildren(false);
    }

    int dp2px(Context context, float value) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (value * scale + 0.5f);
    }

    private void initAttr(AttributeSet attrs) {
        properties = new Properties();
        properties.viewSize = dp2px(getContext(), 55);
    }

    private ImageView buildImageView(int padding) {
        ImageView imageView = new ImageView(getContext());
        LayoutParams lp = new LayoutParams(properties.viewSize, properties.viewSize);
        imageView.setLayoutParams(lp);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setPadding(padding, padding, padding, padding);
        return imageView;
    }

    private void init() {
        imageView = buildImageView(0);
        imageView.setImageResource(R.drawable.ic_assistive);
        imageView.setAlpha(0.7f);

        child1 = buildImageView(childPadding);
        child2 = buildImageView(childPadding);
        child3 = buildImageView(childPadding);

        child1.setImageResource(R.drawable.ic_qq);
        child2.setImageResource(R.drawable.ic_customer_service);
        child3.setImageResource(R.drawable.ic_wechat);

        addView(child1);
        addView(child2);
        addView(child3);
        addView(imageView);

        child1.setAlpha(0f);
        child2.setAlpha(0f);
        child3.setAlpha(0f);

        setOnTouchListener(this);
        setClickable(true);
        setLongClickable(true);
        setClipChildren(false);

        assistiveGestureDetector = new GestureDetector(getContext(), assistiveGestureListener = new AssistiveGestureListener());
        parentGestureDetector = new GestureDetector(getContext(), parentGestureListener = new ParentGestureListener());

        children.add(child1);
        children.add(child2);
        children.add(child3);
    }

    void setChildEnabled(boolean qq, boolean customService, boolean weChat) {
        child1.setImageResource(qq ? R.drawable.ic_qq : R.drawable.ic_qq_disable);
        child2.setImageResource(customService ? R.drawable.ic_customer_service : R.drawable.ic_customer_service_disable);
        child3.setImageResource(weChat ? R.drawable.ic_wechat : R.drawable.ic_wechat_disable);
    }

    private void triggeEvent(View view, int index) {
        if (listener != null)
            listener.onClick(view, index);
    }

    private PointF getPoint(float x, float y, float r, float degree) {
        PointF point = PointSpace.getPoint(r, degree);
        point.offset(x, y);
        return point;
    }

    public void toggleChildCollapsed() {
        setCollapsed(!isCollapsed);
    }

    public void setCollapsed(boolean collapsed) {
        if (collapsed) {
            collapseChild(child1ExpandDegree, child1);
            collapseChild(child2ExpandDegree, child2);
            collapseChild(child3ExpandDegree, child3);
            syncCollapse();
        } else {
            expandChild(child1ExpandDegree, child1);
            expandChild(child2ExpandDegree, child2);
            expandChild(child3ExpandDegree, child3);
            syncExpand();
        }
    }

    private void syncExpand() {
        isCollapsed = false;
        assistiveGestureListener.canScroll = isCollapsed;
        parent.setClickable(true);
        parent.setFocusable(true);
        parent.setOnTouchListener((view, motionEvent) -> parentGestureDetector.onTouchEvent(motionEvent));

    }

    private void syncCollapse() {
        isCollapsed = true;
        assistiveGestureListener.canScroll = isCollapsed;
        parent.setClickable(false);
        parent.setFocusable(false);
    }

    private void expandChild(float degree, View child) {
        PointF point = getPoint(0, 0, r, degree);
        PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat("x", 0, point.x);
        PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat("y", 0, point.y);
        PropertyValuesHolder holderAlpha = PropertyValuesHolder.ofFloat("alpha", child.getAlpha(), 1);
        PropertyValuesHolder holderRotation = PropertyValuesHolder.ofFloat("rotation", child.getRotation(), 360f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(child, holderX, holderY, holderAlpha, holderRotation);
        animator.setDuration(250);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
            }
        });
    }

    private void collapseChild(float degree, View child) {
        PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat("x", child.getX(), 0);
        PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat("y", child.getY(), 0);
        PropertyValuesHolder holderAlpha = PropertyValuesHolder.ofFloat("alpha", child.getAlpha(), 0);
        PropertyValuesHolder holderRotation = PropertyValuesHolder.ofFloat("rotation", child.getRotation(), 0f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(child, holderX, holderY, holderAlpha, holderRotation);
        animator.setDuration(250);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
            }
        });
    }

    private boolean isPointInView(View view, float x, float y) {
        float l = view.getX();
        float t = view.getY();
        float r = l + view.getWidth();
        float b = t + view.getHeight();
        RectF rectView = new RectF(l, t, r, b);
        rectView.offset(getX(), getY());
        return rectView.contains(x, y);
    }

    private View findClickedView(float x, float y, List<View> views) {
        for (View view : views) {
            if (isPointInView(view, x, y))
                return view;
        }
        return null;
    }

    public void setOnChildClickListener(OnChildClickListener l) {
        this.listener = l;
    }

    public void setOnAssistiveListener(OnAssistiveListener l) {
        this.assistiveListener = l;
    }

    class AssistiveGestureListener extends GestureDetector.SimpleOnGestureListener {
        private float preScrollX;
        private float preScrollY;
        private long backTime = 800;
        private long animDuration = 300;
        private Handler handler = new Handler();
        private boolean floatingLeft = true;
        private boolean floatingTop = true;
        private boolean canExpandChild = true;
        private boolean canScroll = true;

        AssistiveGestureListener() {
            updateChildDegrees();
        }

        private boolean isTouchSelf(float x, float y) {
            return x > getLeft() && x < getRight() && y > getTop() && y < getBottom();
        }

        private boolean canSetX(float x) {
            return x >= parent.getLeft() && x + getWidth() <= parent.getRight();
        }

        private boolean canSetY(float y) {
            return y >= (parent.getTop() - topDiff) && (y + topDiff) + getHeight() <= parent.getBottom();
        }

        private @Size(2) float[] operateBackPoint(float x, float y) {
            float resultX;
            float resultY;
            float centerX = x + self.getWidth() / 2f;
            float centerY = y + self.getHeight() / 2f;
            float centerParentX = parent.getWidth() / 2f;
            float centerParentY = parent.getHeight() / 2f;
            floatingLeft = centerX < centerParentX;
            floatingTop = centerY < centerParentY;
            resultX = floatingLeft ? 0 : parent.getRight() - self.getWidth();
            resultY = y;
            return new float[]{resultX, resultY};
        }

        @Override public boolean onSingleTapUp(MotionEvent e) {
//            Log.d("aaa", "點點點放開");
            if (canExpandChild) {
                toggleChildCollapsed();
//                Log.d("aaa", "點點點放開二世");
            }
            return true;
        }

        @Override public void onLongPress(MotionEvent e) {
//            Log.d("aaa", "長長長");
            super.onLongPress(e);
            if (assistiveListener != null) {
//                Log.d("aaa", "長長長二世");
                assistiveListener.onLongClick(AssistiveTouchView.this);
            }
        }

        @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            Log.d("aaa", "滑滑滑");
            if (!canScroll)
                return false;
//            Log.d("aaa", "滑滑滑二世");
            distanceX = preScrollX == 0 ? 0 : e2.getRawX() - preScrollX;
            distanceY = preScrollY == 0 ? 0 : e2.getRawY() - preScrollY;
            float newX = getX() + distanceX;
            float newY = getY() + distanceY;
            if (canSetX(newX)) {
                setX(newX);
            }
//            Log.d("aaaa", "可以滑y嗎:" + canSetY(newY) + ", preScrollY = " + preScrollY + ", rawY = " + e2.getRawY());
            if (canSetY(newY)) {
//                Log.d("aaaa", "設定y:" + newY);
                setY(newY);
            }
            preScrollX = e2.getRawX();
            preScrollY = e2.getRawY();
            handler.removeCallbacks(timeBack);
            handler.postDelayed(timeBack, backTime);
            canExpandChild = false;
            return true;
        }

        @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            Log.d("aaa", "慣慣慣");
            handler.removeCallbacks(timeBack);
            back();
            updateChildDegrees();
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override public boolean onDown(MotionEvent e) {
//            Log.d("aaa", "點點點");
            preScrollX = 0;
            preScrollY = 0;
            return true;
        }

        private void back() {
            float[] newXY = operateBackPoint(getX(), getY());
            PropertyValuesHolder xHolder = PropertyValuesHolder.ofFloat("x", getX(), newXY[0]);
            PropertyValuesHolder yHolder = PropertyValuesHolder.ofFloat("y", getY(), newXY[1]);
            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(self, xHolder, yHolder);
            animator.setDuration(animDuration);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.start();
            animator.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    canExpandChild = true;
                }

                @Override public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    canExpandChild = true;
                }
            });
        }

        private void updateChildDegrees() {
            float offset = 8;
            if (floatingLeft && floatingTop) {
                //左上
                child1ExpandDegree = 90 + offset;
                child2ExpandDegree = 135;
                child3ExpandDegree = 180 - offset;
            } else if (floatingLeft && !floatingTop) {
                //左下
                child1ExpandDegree = 0 + offset;
                child2ExpandDegree = 45;
                child3ExpandDegree = 90 - offset;
            } else if (!floatingLeft && floatingTop) {
                //右上
                child1ExpandDegree = 270 - offset;
                child2ExpandDegree = 225;
                child3ExpandDegree = 180 + offset;
            } else if (!floatingLeft && !floatingTop) {
                //右下
                child1ExpandDegree = 360 - offset;
                child2ExpandDegree = 315;
                child3ExpandDegree = 270 + offset;
            }
        }

        Runnable timeBack = () -> {
            back();
            updateChildDegrees();
        };
    }

    class ParentGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }

        @Override public boolean onSingleTapUp(MotionEvent e) {
            View clickedView = findClickedView(e.getX(), e.getY(), children);
            if (clickedView == null) {
                setCollapsed(true);
            } else {
                if (listener != null)
                    listener.onClick(clickedView, children.indexOf(clickedView));
            }
            return true;
        }
    }

    public interface OnChildClickListener {
        void onClick(View v, int index);
    }

    public interface OnAssistiveListener {

        void onLongClick(View v);
    }

    public enum PointSpace {
        SPACE1,
        SPACE2,
        SPACE3,
        SPACE4,
        X_PLUS,
        X_LESS,
        Y_PLUS,
        Y_LESS;

        /**
         * 取得角度所在的象限範圍
         *
         * @param degree 角度
         * @return 象限範圍
         */
        public static PointSpace getSpace(float degree) {
            degree = parseRealDegree(degree);
            if (degree == 0)
                return PointSpace.Y_LESS;
            else if (degree == 90)
                return PointSpace.X_PLUS;
            else if (degree == 180)
                return PointSpace.Y_PLUS;
            else if (degree == 270)
                return PointSpace.X_LESS;
            else if (degree > 0 && degree < 90)
                return PointSpace.SPACE4;
            else if (degree > 90 && degree < 180)
                return PointSpace.SPACE1;
            else if (degree > 180 && degree < 270)
                return PointSpace.SPACE2;
            else if (degree > 270 && degree < 360)
                return PointSpace.SPACE3;
            throw new Error("Invalid degree: " + degree);
        }

        /**
         * 輸入角度並轉換為真實角度
         *
         * @param degree 輸入的角度
         * @return 轉換後的角度
         * <pre>
         * ex:
         *  0 -> 0
         *  361 -> 1
         *  -1 -> 359
         */
        public static float parseRealDegree(float degree) {
            if (degree < 0) {
                while (degree < 0)
                    degree += 360;
            } else {
                degree %= 360;
            }
            return degree;
        }

        public static PointF getPoint(float r, float degree) {
            PointSpace space = getSpace(degree);
            float radian = (float) Math.toRadians(degree);
            PointF p = new PointF();
            switch (space) {
                case SPACE1:
                case SPACE2:
                case SPACE3:
                case SPACE4:
                    p.x = (float) +(r * Math.sin(radian));
                    p.y = (float) -(r * Math.cos(radian));
                    break;
                case X_PLUS:
                    p.x = r;
                    p.y = 0;
                    break;
                case X_LESS:
                    p.x = -r;
                    p.y = 0;
                    break;
                case Y_PLUS:
                    p.x = 0;
                    p.y = r;
                    break;
                case Y_LESS:
                    p.x = 0;
                    p.y = -r;
                    break;
            }
            return p;
        }
    }
}
