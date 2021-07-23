package knight.rider.kitt.adapter.attr;

/**
 * 作者： mr.Wang
 * 分割线样式
 */
public enum DividerStyle {

    // 网格，无外边框网格,纵向，横向
    GRID(0), GRID_NO_OUTER(1), VERTICAL(2), HORIZONTAL(3);
    private final int mStyle;

    DividerStyle(int style) {
        this.mStyle = style;
    }

    public final int getStyle() {
        return mStyle;
    }
}
