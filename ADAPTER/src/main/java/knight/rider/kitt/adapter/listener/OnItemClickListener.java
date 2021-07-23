package knight.rider.kitt.adapter.listener;

import android.view.View;

/**
 * 作者： mr.Wang
 * 列表item的点击事件
 */
public interface OnItemClickListener<T> {
    void onClick(T t, int position, View view);
}