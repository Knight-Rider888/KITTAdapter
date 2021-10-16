package knight.rider.kitt.adapter.impl;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 作者： mr.Wang
 * RecyclerView.ItemDecoration抽象类实现
 */
public class ItemVerticalDecorationImpl extends RecyclerView.ItemDecoration {

    private final int mDividerHeight;
    private final Paint mPaint;

    // 线的间距
    private final int mMarginLeft;
    private final int mMarginRight;

    /**
     * 构造方法，分割线颜色浅灰，宽度1px
     */
    public ItemVerticalDecorationImpl() {
        this(Color.parseColor("#F2F2F2"), 1);
    }

    /**
     * 构造方法
     *
     * @param color     the divider color.
     * @param divHeight the divider height.
     */
    public ItemVerticalDecorationImpl(@ColorInt int color, int divHeight) {
        this(color, divHeight, 0, 0);
    }

    /**
     * 构造方法
     *
     * @param color       the divider color.
     * @param divHeight   the divider height.
     * @param marginLeft  当是线性管理器时：marginStart 为线的左间距
     * @param marginRight 当是线性管理器时：marginEnd 为线的右间距
     */
    public ItemVerticalDecorationImpl(@ColorInt int color, int divHeight, int marginLeft, int marginRight) {
        mDividerHeight = divHeight;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        this.mMarginLeft = marginLeft;
        this.mMarginRight = marginRight;
    }


    @Override
    public final void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        // 纵向布局分割线
        drawVertical(c, parent);
    }


    @Override
    public final void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int itemPosition = parent.getChildAdapterPosition(view);
        RecyclerView.Adapter mAdapter = parent.getAdapter();
        if (mAdapter != null) {
            if (itemPosition != 0) {
                outRect.set(0, mDividerHeight, 0, 0);
            }
        }
    }


    /**
     * 绘制纵向列表分割线
     *
     * @param c      绘制容器
     * @param parent RecyclerView
     */
    private void drawVertical(Canvas c, RecyclerView parent) {
        int mChildCount = parent.getChildCount();
        for (int i = 0; i < mChildCount; i++) {
            View mChild = parent.getChildAt(i);
            drawTop(c, mChild);
        }
    }


    /**
     * 绘制顶部分割线
     *
     * @param c      绘制容器
     * @param mChild 对应ItemView
     */
    private void drawTop(Canvas c, View mChild) {
        RecyclerView.LayoutParams mChildLayoutParams = (RecyclerView.LayoutParams) mChild.getLayoutParams();
        int left = mChild.getLeft() - mChildLayoutParams.leftMargin + mMarginLeft;
        int top = mChild.getTop() - mChildLayoutParams.topMargin - mDividerHeight;
        int right = mChild.getRight() + mChildLayoutParams.rightMargin - mMarginRight;
        int bottom = mChild.getTop() - mChildLayoutParams.topMargin;

        if (left > right)
            left = right;

        c.drawRect(left, top, right, bottom, mPaint);
    }

}
