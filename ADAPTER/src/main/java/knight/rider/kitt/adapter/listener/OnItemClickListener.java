package knight.rider.kitt.adapter.listener;

import android.view.View;

/**
 * 作者： mr.Wang
 * 列表item的点击事件
 */
public interface OnItemClickListener<T> {
    // 传递当前点击的对象（List对应位置的数据）与位置
    void onClick(T t, int position, View view);
}