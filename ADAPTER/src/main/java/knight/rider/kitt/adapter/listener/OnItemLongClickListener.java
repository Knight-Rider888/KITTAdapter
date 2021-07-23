package knight.rider.kitt.adapter.listener;

import android.view.View;

/**
 * 作者： mr.Wang
 * 列表item的长按点击事件
 */
public interface OnItemLongClickListener<T> {
    void onLongClick(T t, int position, View view);
}