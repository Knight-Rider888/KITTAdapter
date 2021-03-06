package knight.rider.kitt.adapter.impl;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import knight.rider.kitt.adapter.attr.DividerStyle;

/**
 * 作者： mr.Wang
 * RecyclerView.ItemDecoration抽象类实现
 */
public class ItemDecorationImpl extends RecyclerView.ItemDecoration {

    private final DividerStyle mDividerStyle;

    private final int mDividerWidth;
    private final Paint mPaint;

    // 线的间距
    private int mMarginStart, mMarginEnd;

    /**
     * 构造方法，分割线颜色浅灰，宽度1px
     *
     * @param dividerStyle the divider style.
     */
    public ItemDecorationImpl(DividerStyle dividerStyle) {
        this(dividerStyle, Color.parseColor("#F2F2F2"), 1);
    }

    /**
     * 构造方法
     *
     * @param dividerStyle the divider style.
     * @param color        the divider color.
     * @param divWidth     the divider width.
     */
    public ItemDecorationImpl(DividerStyle dividerStyle, @ColorInt int color, int divWidth) {
        this.mDividerStyle = dividerStyle;
        mDividerWidth = divWidth;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 构造方法
     *
     * @param dividerStyle the divider style.
     * @param color        the divider color.
     * @param divWidth     the divider width.
     * @param marginStart  当是线性管理器时：垂直方向 marginStart 为线的左间距  水平方向 marginStart 为线的顶部间距  暂不支持grid管理器
     * @param marginEnd    当是线性管理器时：垂直方向 marginEnd 为线的右间距  水平方向 marginEnd 为线的底部间距 暂不支持grid管理器
     */
    public ItemDecorationImpl(DividerStyle dividerStyle, @ColorInt int color, int divWidth, int marginStart, int marginEnd) {
        this.mDividerStyle = dividerStyle;
        mDividerWidth = divWidth;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        mMarginStart = marginStart;
        mMarginEnd = marginEnd;
    }


    @Override
    public final void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        switch (mDividerStyle) {
            case HORIZONTAL:
                // 横向布局分割线
                drawHorizontal(c, parent);
                break;
            case VERTICAL:
                // 纵向布局分割线
                drawVertical(c, parent);
                break;
            case GRID:
                // 表格格局分割线
                drawGrid(c, parent);
                break;
            case GRID_NO_OUTER:
                // 表格格局分割线
                drawGridNoOuter(c, parent);
                break;

        }
    }


    @Override
    public final void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int itemPosition = parent.getChildAdapterPosition(view);
        RecyclerView.Adapter mAdapter = parent.getAdapter();
        if (mAdapter != null) {
            switch (mDividerStyle) {
                case HORIZONTAL:
                    /**
                     * 横向布局分割线
                     *     如果是第一个Item，则不需要分割线
                     */
                    if (itemPosition != 0) {
                        outRect.set(mDividerWidth, 0, 0, 0);
                    }
                    break;
                case VERTICAL:
                    /**
                     * 纵向布局分割线
                     *     如果是第一个Item，则不需要分割线
                     */
                    if (itemPosition != 0) {
                        outRect.set(0, mDividerWidth, 0, 0);
                    }
                    break;
                case GRID:
                case GRID_NO_OUTER:
                    /**
                     * 表格格局分割线
                     *      1：当是第一个Item的时候，四周全部需要分割线
                     *      2：当是第一行Item的时候，需要额外添加顶部的分割线
                     *      3：当是第一列Item的时候，需要额外添加左侧的分割线
                     *      4：默认情况全部添加底部和右侧的分割线
                     */
                    RecyclerView.LayoutManager mLayoutManager = parent.getLayoutManager();
                    if (mLayoutManager instanceof GridLayoutManager) {
                        GridLayoutManager mGridLayoutManager = (GridLayoutManager) mLayoutManager;
                        int mSpanCount = mGridLayoutManager.getSpanCount();
                        if (itemPosition == 0) {//1
                            outRect.set(mDividerWidth, mDividerWidth, mDividerWidth, mDividerWidth);
                        } else if ((itemPosition + 1) <= mSpanCount) {//2
                            outRect.set(0, mDividerWidth, mDividerWidth, mDividerWidth);
                        } else if (((itemPosition + mSpanCount) % mSpanCount) == 0) {//3
                            outRect.set(mDividerWidth, 0, mDividerWidth, mDividerWidth);
                        } else {//4
                            outRect.set(0, 0, mDividerWidth, mDividerWidth);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 绘制横向列表分割线
     *
     * @param c      绘制容器
     * @param parent RecyclerView
     */
    private void drawHorizontal(Canvas c, RecyclerView parent) {
        int mChildCount = parent.getChildCount();
        for (int i = 0; i < mChildCount; i++) {
            View mChild = parent.getChildAt(i);
            drawLeft(c, mChild, parent);
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
            drawTop(c, mChild, parent);
        }
    }


    /**
     * 绘制表格类型分割线
     *
     * @param c      绘制容器
     * @param parent RecyclerView
     */
    private void drawGrid(Canvas c, RecyclerView parent) {
        int mChildCount = parent.getChildCount();
        for (int i = 0; i < mChildCount; i++) {
            View mChild = parent.getChildAt(i);
            RecyclerView.LayoutManager mLayoutManager = parent.getLayoutManager();
            if (mLayoutManager instanceof GridLayoutManager) {
                GridLayoutManager mGridLayoutManager = (GridLayoutManager) mLayoutManager;
                int mSpanCount = mGridLayoutManager.getSpanCount();
                if (i == 0) {
                    drawTop(c, mChild, parent);
                    drawLeft(c, mChild, parent);
                }
                if ((i + 1) <= mSpanCount) {
                    drawTop(c, mChild, parent);
                }
                if (((i + mSpanCount) % mSpanCount) == 0) {
                    drawLeft(c, mChild, parent);
                }
                drawRight(c, mChild, parent);
                drawBottom(c, mChild, parent);
            }
        }
    }


    /**
     * 绘制表格类型分割线
     *
     * @param c      绘制容器
     * @param parent RecyclerView
     */
    private void drawGridNoOuter(Canvas c, RecyclerView parent) {
        int mChildCount = parent.getChildCount();
        for (int i = 0; i < mChildCount; i++) {
            View mChild = parent.getChildAt(i);
            RecyclerView.LayoutManager mLayoutManager = parent.getLayoutManager();
            if (mLayoutManager instanceof GridLayoutManager) {
                GridLayoutManager mGridLayoutManager = (GridLayoutManager) mLayoutManager;
                int mSpanCount = mGridLayoutManager.getSpanCount();

                if (((i + mSpanCount) % mSpanCount) == 0 && i % mSpanCount != 0) {
                    drawLeft(c, mChild, parent);
                }

                if ((i + 1) % mSpanCount != 0) {
                    drawRight(c, mChild, parent);
                }

                if ((mChildCount % mSpanCount == 0 ? mChildCount - mSpanCount : mChildCount - mChildCount % mSpanCount) > i) {
                    drawBottom(c, mChild, parent);
                }
            }
        }
    }

    /**
     * 绘制右边分割线
     *
     * @param c            绘制容器
     * @param mChild       对应ItemView
     * @param recyclerView RecyclerView
     */
    private void drawLeft(Canvas c, View mChild, RecyclerView recyclerView) {
        RecyclerView.LayoutParams mChildLayoutParams = (RecyclerView.LayoutParams) mChild.getLayoutParams();
        int left = mChild.getLeft() - mDividerWidth - mChildLayoutParams.leftMargin;
        int top = mChild.getTop() - mChildLayoutParams.topMargin + (isGridLayoutManager(recyclerView) ? 0 : mMarginStart);
        int right = mChild.getLeft() - mChildLayoutParams.leftMargin;
        int bottom;
        if (isGridLayoutManager(recyclerView)) {
            bottom = mChild.getBottom() + mChildLayoutParams.bottomMargin + mDividerWidth;
        } else {
            bottom = mChild.getBottom() + mChildLayoutParams.bottomMargin - mMarginEnd;
        }
        c.drawRect(left, top, right, bottom, mPaint);
    }

    /**
     * 绘制顶部分割线
     *
     * @param c            绘制容器
     * @param mChild       对应ItemView
     * @param recyclerView RecyclerView
     */
    private void drawTop(Canvas c, View mChild, RecyclerView recyclerView) {
        RecyclerView.LayoutParams mChildLayoutParams = (RecyclerView.LayoutParams) mChild.getLayoutParams();
        int left;
        int top = mChild.getTop() - mChildLayoutParams.topMargin - mDividerWidth;
        int right = mChild.getRight() + mChildLayoutParams.rightMargin - (isGridLayoutManager(recyclerView) ? 0 : mMarginEnd);
        int bottom = mChild.getTop() - mChildLayoutParams.topMargin;
        if (isGridLayoutManager(recyclerView)) {
            left = mChild.getLeft() - mChildLayoutParams.leftMargin - mDividerWidth;
        } else {
            left = mChild.getLeft() - mChildLayoutParams.leftMargin + mMarginStart;
        }
        c.drawRect(left, top, right, bottom, mPaint);
    }

    /**
     * 绘制右边分割线
     *
     * @param c            绘制容器
     * @param mChild       对应ItemView
     * @param recyclerView RecyclerView
     */
    private void drawRight(Canvas c, View mChild, RecyclerView recyclerView) {
        RecyclerView.LayoutParams mChildLayoutParams = (RecyclerView.LayoutParams) mChild.getLayoutParams();
        int left = mChild.getRight() + mChildLayoutParams.rightMargin;
        int top;
        int right = left + mDividerWidth;
        int bottom = mChild.getBottom() + mChildLayoutParams.bottomMargin + mDividerWidth;
        if (isGridLayoutManager(recyclerView)) {
            top = mChild.getTop() - mChildLayoutParams.topMargin - mDividerWidth;
        } else {
            top = mChild.getTop() - mChildLayoutParams.topMargin;
        }
        c.drawRect(left, top, right, bottom, mPaint);
    }

    /**
     * 绘制底部分割线
     *
     * @param c            绘制容器
     * @param mChild       对应ItemView
     * @param recyclerView RecyclerView
     */
    private void drawBottom(Canvas c, View mChild, RecyclerView recyclerView) {
        RecyclerView.LayoutParams mChildLayoutParams = (RecyclerView.LayoutParams) mChild.getLayoutParams();
        int left = mChild.getLeft() - mChildLayoutParams.leftMargin - (isGridLayoutManager(recyclerView) ? mDividerWidth : 0);
        int top = mChild.getBottom() + mChildLayoutParams.bottomMargin;
        int bottom = top + mDividerWidth;
        int right;
        if (isGridLayoutManager(recyclerView)) {
            right = mChild.getRight() + mChildLayoutParams.rightMargin + mDividerWidth;
        } else {
            right = mChild.getRight() + mChildLayoutParams.rightMargin;
        }
        c.drawRect(left, top, right, bottom, mPaint);
    }

    /**
     * 判断RecyclerView所加载LayoutManager是否为GridLayoutManager
     *
     * @param recyclerView RecyclerView
     * @return 是GridLayoutManager返回true，否则返回false
     */
    private boolean isGridLayoutManager(RecyclerView recyclerView) {
        RecyclerView.LayoutManager mLayoutManager = recyclerView.getLayoutManager();
        return (mLayoutManager instanceof GridLayoutManager);
    }
}
