package lk.lankalens.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class SafeSwipeRefreshLayout extends SwipeRefreshLayout {

    public SafeSwipeRefreshLayout(@NonNull Context context) {
        super(context);
    }

    public SafeSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev == null) return false;
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_MOVE && ev.getPointerId(0) < 0) {
            return false;
        }
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
            // Catches "Got ACTION_MOVE event but don't have an active pointer id" or pointer index out of range
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev == null) return false;
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_MOVE && ev.getPointerId(0) < 0) {
            return false;
        }
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
            // Catches "Got ACTION_MOVE event but don't have an active pointer id" or pointer index out of range
            return false;
        }
    }
}
