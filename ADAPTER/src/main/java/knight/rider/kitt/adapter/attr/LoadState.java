package knight.rider.kitt.adapter.attr;

/**
 * 作者： mr.Wang
 * 加载状态
 */
public enum LoadState {

    // 加载完成、加载中、加载到底、加载出错了、无数据
    LOAD_COMPLETE(0), LOADING(1), LOAD_END(2), LOAD_ERROR(3), LOAD_NO_DATA(4);

    private final int mState;

    LoadState(int state) {
        this.mState = state;
    }

    public final int getState() {
        return mState;
    }
}
