package knight.rider.kitt.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import knight.rider.kitt.adapter.holder.RecyclerViewHolder;


/**
 * 作者： mr.Wang
 * 单布局列表的简单适配器
 */
public abstract class SimpleListAdapter<T> extends BaseListAdapter<T> {

    // item的布局资源
    private final int mLayoutResId;

    // 初始化无需数据源
    public SimpleListAdapter(Context context, int layoutResId, boolean supportFooter) {
        super(context, supportFooter);
        mLayoutResId = layoutResId;
    }

    // 初始化无需数据源,支持脚布局
    public SimpleListAdapter(Context context, int layoutResId) {
        super(context, true);
        mLayoutResId = layoutResId;
    }

    // 初始化需数据源
    public SimpleListAdapter(Context context, int layoutResId, boolean supportFooter, List<T> list) {
        this(context, layoutResId, supportFooter);
        addData(list);
    }

    // 初始化需数据源，支持脚布局
    public SimpleListAdapter(Context context, int layoutResId, List<T> list) {
        this(context, layoutResId, true);
        addData(list);
    }

    @Override
    public final int getItemViewsType(int position) {
        return 1;
    }

    @Override
    public final RecyclerViewHolder onCreateViewHolders(ViewGroup parent, int viewType) {
        View itemView = getInflater().inflate(mLayoutResId, parent, false);
        // 设置item的点击事件
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
        return new RecyclerViewHolder(itemView);
    }
}