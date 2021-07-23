package knight.rider.kitt.adapter.listener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 作者： mr.Wang
 * 列表滚动的监听事件
 */
public interface OnScrollListener {

    void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState);

    void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy);
}
