package knight.rider.kitt.adapter.attr;

/**
 * 作者： mr.Wang
 * 加载状态
 */
public enum LoadState {
    
    LOAD_COMPLETE("加载完成"), LOADING("加载中"), LOAD_END("加载到底"), LOAD_ERROR("加载出错了"), LOAD_NO_DATA("无数据");

    LoadState(String state) {

    }

}
