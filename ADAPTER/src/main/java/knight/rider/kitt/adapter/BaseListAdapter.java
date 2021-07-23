package knight.rider.kitt.adapter;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;

import knight.rider.kitt.adapter.attr.LoadState;
import knight.rider.kitt.adapter.holder.RecyclerViewHolder;
import knight.rider.kitt.adapter.listener.OnFooterErrorListener;
import knight.rider.kitt.adapter.listener.OnItemClickListener;
import knight.rider.kitt.adapter.listener.OnItemLongClickListener;
import knight.rider.kitt.adapter.listener.OnLoadMoreListener;
import knight.rider.kitt.adapter.listener.OnScrollListener;


public abstract class BaseListAdapter<T> extends RecyclerView.Adapter<RecyclerViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    // 数据源
    private final List<T> mData;

    // Attach的RecyclerView
    private RecyclerView mRecyclerView;

    // 上下文对象
    private final Context mContext;

    // 布局填充器
    private final LayoutInflater mInflater;


    // item的点击事件
    private OnItemClickListener mListener;
    // item的长按点击事件
    private OnItemLongClickListener mLongListener;
    // 加载更多的监听
    private OnLoadMoreListener mLoadMoreListener;
    // 滚动的监听
    private OnScrollListener mScrollListener;

    // header footer
    private LinearLayout mHeaderLayout;
    private LinearLayout mFooterLayout;

    // 是否支持加载状态的脚布局
    private final boolean mSupportLoadStateFooter;

    // 当前加载状态，默认为加载完成
    private int loadState = 0;

    // 加载完成
    private final int LOAD_COMPLETE = 0;
    // 正在加载
    private final int LOADING = 1;
    // 加载到底
    private final int LOAD_END = 2;
    // 加载出错
    private final int LOAD_ERROR = 3;
    // 加载无数据
    private final int LOAD_NO_DATA = 4;
    // 去掉脚布局
    private final int LOAD_NO_FOOTER = 5;


    // 头布局
    private final int TYPE_HEADER = Integer.MAX_VALUE - 2;
    // 脚布局
    private final int TYPE_FOOTER = Integer.MAX_VALUE - 1;
    // 加载状态脚布局
    private final int TYPE_LOAD_STATE_FOOTER = Integer.MAX_VALUE;
    // 无数据脚布局图片
    private int mEmptyImg = R.drawable.kitt_list_none;
    // 无数据脚布局是否是Lottie,默认是Lottie动画布局
    private boolean mEmptyLottie = true;
    private String mEmptyText = "暂无数据";

    // error的点击事件
    private OnFooterErrorListener mErrorListener;

    // params
    private LinearLayout.LayoutParams wrapParams;
    private final LinearLayout.LayoutParams matchParams;
    private final LinearLayout.LayoutParams noParams;


    //用来标记是否正在向上滑动
    private boolean mIsSlidingUpward = false;
    //用来标记是否正在向右滑动
    private boolean mIsSlidingRight = false;


    // 脚布局状态Layout
    private int mStateFooterLayout = R.layout.kitt_list_state_footer_vertical;

    // 加载出错可否继续执行加载更多
    private boolean errorEnableLoadMore = false;

    // 默认支持加载状态脚布局
    public BaseListAdapter(Context context) {
        this(context, true);
    }

    public BaseListAdapter(Context context, boolean supportLoadState) {
        mData = new ArrayList<>();
        this.mContext = context;
        this.mSupportLoadStateFooter = supportLoadState;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        wrapParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        matchParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        noParams = new LinearLayout.LayoutParams(0, 0);
    }

    // ******** 获取item的数量 不允许修改 ********
    @Override
    public final int getItemCount() {
        return getHeaderLayoutCount() + mData.size() + getFooterLayoutCount() + getLoadStateViewCount();
    }

    // ******** 获取item的type类型 不允许修改 ********
    @Override
    public final int getItemViewType(int position) {

        if (position == 0 && getHeaderLayoutCount() != 0) {
            return TYPE_HEADER;
        }

        // 真正数据源已获取完毕
        if (position == mData.size() + getHeaderLayoutCount()) {
            return getFooterLayoutCount() == 0 ? TYPE_LOAD_STATE_FOOTER : TYPE_FOOTER;
        }


        // 已经获取完脚布局
        if (position == mData.size() + getHeaderLayoutCount() + getFooterLayoutCount()) {
            return TYPE_LOAD_STATE_FOOTER;
        }

        // 返回数据源对应的真实position
        return getItemViewsType(position - getHeaderLayoutCount());
    }


    // 多布局的ViewType
    public abstract int getItemViewsType(int position);


    // ******** 创建Holder 不允许修改 ********
    @NonNull
    @Override
    public final RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // 进行判断显示类型，来创建返回不同的View
        if (viewType == TYPE_LOAD_STATE_FOOTER) {

            View view = mInflater.inflate(mStateFooterLayout, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mErrorListener != null && loadState == LOAD_ERROR) {
                        mErrorListener.onErrorClick();
                    }
                }
            });
            return new FootLoadStateViewHolder(view);

        } else if (viewType == TYPE_HEADER) {

            ViewParent headerLayoutVp = mHeaderLayout.getParent();
            if (headerLayoutVp instanceof ViewGroup) {
                ((ViewGroup) headerLayoutVp).removeView(mHeaderLayout);
            }
            return new HeaderViewHolder(mHeaderLayout);

        } else if (viewType == TYPE_FOOTER) {

            ViewParent footerLayoutVp = mFooterLayout.getParent();
            if (footerLayoutVp instanceof ViewGroup) {
                ((ViewGroup) footerLayoutVp).removeView(mFooterLayout);
            }
            return new FooterViewHolder(mFooterLayout);

        } else {

            // 用户自定义的Holder
            RecyclerViewHolder viewHolder = onCreateViewHolders(parent, viewType);

            // 如果有需要重写点击事件，可在onBindViewHolders()进行覆盖
            viewHolder.itemView.setOnClickListener(this);
            viewHolder.itemView.setOnLongClickListener(this);
            return viewHolder;

        }
    }

    // 创建多布局的ViewHolder
    public abstract RecyclerViewHolder onCreateViewHolders(ViewGroup parent, int viewType);


    // ******** 绑定显示数据 不允许修改 ********
    @Override
    public final void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {

        if (holder instanceof FootLoadStateViewHolder) {

            FootLoadStateViewHolder footViewHolder = (FootLoadStateViewHolder) holder;
            FrameLayout layout = footViewHolder.getFrameLayout(R.id.kitt_list_footer_layout);
            LottieAnimationView loadingView = footViewHolder.getView(R.id.kitt_list_lv_loading);
            LottieAnimationView errorView = footViewHolder.getView(R.id.kitt_list_lv_error);
            LottieAnimationView emptyView = footViewHolder.getView(R.id.kitt_list_none_lv);
            // 当切换状态时，滚动到底部显示脚布局
            mRecyclerView.smoothScrollToPosition(getDataSize());
            switch (loadState) {
                case LOADING: // 正在加载
                    layout.setLayoutParams(wrapParams);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_loading_layout).setVisibility(View.VISIBLE);
                    footViewHolder.getLinearLayout(R.id.kitt_list_end_layout).setVisibility(View.GONE);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_error_layout).setVisibility(View.GONE);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_no_data_layout).setVisibility(View.GONE);
                    loadingView.playAnimation();
                    emptyView.pauseAnimation();
                    errorView.pauseAnimation();
                    break;
                case LOAD_COMPLETE: // 加载完成
                case LOAD_NO_FOOTER:// 无脚布局
                    layout.setLayoutParams(noParams);
                    loadingView.pauseAnimation();
                    emptyView.pauseAnimation();
                    errorView.pauseAnimation();
                    break;
                case LOAD_END: // 加载到底
                    layout.setLayoutParams(wrapParams);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_loading_layout).setVisibility(View.GONE);
                    footViewHolder.getLinearLayout(R.id.kitt_list_end_layout).setVisibility(View.VISIBLE);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_error_layout).setVisibility(View.GONE);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_no_data_layout).setVisibility(View.GONE);
                    loadingView.pauseAnimation();
                    emptyView.pauseAnimation();
                    errorView.pauseAnimation();
                    break;
                case LOAD_ERROR: // 加载出错
                    layout.setLayoutParams(wrapParams);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_loading_layout).setVisibility(View.GONE);
                    footViewHolder.getLinearLayout(R.id.kitt_list_end_layout).setVisibility(View.GONE);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_error_layout).setVisibility(View.VISIBLE);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_no_data_layout).setVisibility(View.GONE);
                    errorView.playAnimation();
                    loadingView.pauseAnimation();
                    emptyView.pauseAnimation();
                    break;
                case LOAD_NO_DATA:
                    layout.setLayoutParams(matchParams);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_loading_layout).setVisibility(View.GONE);
                    footViewHolder.getLinearLayout(R.id.kitt_list_end_layout).setVisibility(View.GONE);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_error_layout).setVisibility(View.GONE);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_no_data_layout).setVisibility(View.VISIBLE);
                    emptyView.setVisibility(mEmptyLottie ? View.VISIBLE : View.GONE);
                    footViewHolder.getImageView(R.id.kitt_list_none_img).setVisibility(!mEmptyLottie ? View.VISIBLE : View.GONE);
                    footViewHolder.getTextView(R.id.kitt_list_none_tv).setText(mEmptyText);
                    if (!mEmptyLottie)
                        footViewHolder.getImageView(R.id.kitt_list_none_img).setImageResource(mEmptyImg);
                    else
                        emptyView.playAnimation();
                    loadingView.pauseAnimation();
                    errorView.playAnimation();
                    break;
            }
        } else if (holder instanceof FooterViewHolder) {

        } else if (holder instanceof HeaderViewHolder) {

        } else {
            // 需要子类去实现 具体操作
            onBindViewHolders(holder, mData.get(position - getHeaderLayoutCount()), position - getHeaderLayoutCount());
        }
    }


    // 绑定数据
    public abstract void onBindViewHolders(RecyclerViewHolder holder, T t, int position);

    // 移除所有头布局
    public final void removeAllHeaders() {
        if (mHeaderLayout != null && mHeaderLayout.getChildCount() > 0) {
            mHeaderLayout.removeAllViews();
            notifyItemRangeRemoved(0, 1);
        }
    }

    public final void removeHeader(View removeView) {
        if (mHeaderLayout != null) {
            mHeaderLayout.removeView(removeView);

            if (mHeaderLayout.getChildCount() == 0)
                notifyItemRangeRemoved(0, 1);
        }
    }

    public final void removeHeader(int index) {
        if (mHeaderLayout != null && index >= 0 && mHeaderLayout.getChildCount() > index) {
            mHeaderLayout.removeViewAt(index);

            if (mHeaderLayout.getChildCount() == 0)
                notifyItemRangeRemoved(0, 1);
        }
    }

    public final void addHeaderView(View header) {
        addHeaderView(header, -1, LinearLayout.VERTICAL);
    }

    public final void addHeaderView(View header, int orientation) {
        addHeaderView(header, -1, orientation);
    }


    /**
     * 添加头布局
     * 注意：添加同一个头部视图对象，只进行替换，并不新增
     *
     * @param header      头布局
     * @param index       头布局添加的具体位置
     * @param orientation 横向 还是 纵向
     */
    public final void addHeaderView(View header, final int index, int orientation) {

        if (mHeaderLayout == null) {
            mHeaderLayout = new LinearLayout(header.getContext());
            if (orientation == LinearLayout.VERTICAL) {
                mHeaderLayout.setOrientation(LinearLayout.VERTICAL);
                mHeaderLayout.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else {
                mHeaderLayout.setOrientation(LinearLayout.HORIZONTAL);
                mHeaderLayout.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
            }
        }

        final int childCount = mHeaderLayout.getChildCount();
        int mIndex = index;

        // 传入index不符合规范自动插入到头布局最后的位置
        if (index < 0 || index > childCount) {
            mIndex = childCount;
        }

        for (int i = 0; i < childCount; i++) {
            View childAt = mHeaderLayout.getChildAt(i);

            if (childAt == header) {
                mHeaderLayout.removeView(childAt);
                mIndex = i;
                break;
            }
        }

        mHeaderLayout.addView(header, mIndex);

        if (childCount == 0 && mHeaderLayout.getChildCount() == 1)
            // 通知适配器刷新第一条数据
            notifyItemInserted(0);
    }


    // 移除所有脚布局
    public final void removeAllFooters() {
        if (mFooterLayout != null && mFooterLayout.getChildCount() > 0) {
            mFooterLayout.removeAllViews();
            notifyItemRangeRemoved(getFooterPosition(), 1);
        }
    }

    public final void removeFooter(View removeView) {
        if (mFooterLayout != null) {
            mFooterLayout.removeView(removeView);

            if (mFooterLayout.getChildCount() == 0)
                notifyItemRangeRemoved(getFooterPosition(), 1);
        }
    }

    public final void removeFooter(int index) {
        if (mFooterLayout != null && index >= 0 && mFooterLayout.getChildCount() > index) {
            mFooterLayout.removeViewAt(index);

            if (mFooterLayout.getChildCount() == 0)
                notifyItemRangeRemoved(getFooterPosition(), 1);
        }
    }


    public final void addFooterView(View footer) {
        addFooterView(footer, -1, LinearLayout.VERTICAL);
    }

    public final void addFooterView(View footer, int orientation) {
        addFooterView(footer, -1, orientation);
    }

    /**
     * 添加脚布局
     *
     * @param footer      脚布局
     * @param index       脚布局添加的具体位置
     * @param orientation 横向 还是 纵向
     */
    public final void addFooterView(View footer, int index, int orientation) {

        if (mFooterLayout == null) {
            mFooterLayout = new LinearLayout(footer.getContext());
            if (orientation == LinearLayout.VERTICAL) {
                mFooterLayout.setOrientation(LinearLayout.VERTICAL);
                mFooterLayout.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else {
                mFooterLayout.setOrientation(LinearLayout.HORIZONTAL);
                mFooterLayout.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
            }
        }
        final int childCount = mFooterLayout.getChildCount();

        if (index < 0 || index > childCount) {
            index = childCount;
        }

        for (int i = 0; i < childCount; i++) {
            View childAt = mFooterLayout.getChildAt(i);

            if (childAt == footer) {
                mFooterLayout.removeView(childAt);
                index = i;
                break;
            }
        }

        mFooterLayout.addView(footer, index);


        if (childCount == 0 && mFooterLayout.getChildCount() == 1)
            // 通知适配器刷新脚布局数据
            notifyItemInserted(getFooterPosition());
    }


    // 获取脚布局的刷新位置
    private int getFooterPosition() {
        int position = getItemCount() - getLoadStateViewCount() - 1;
        return Math.max(position, 0);
    }

    // 获取用户自定义头布局数量
    private int getHeaderLayoutCount() {
        if (mHeaderLayout == null || mHeaderLayout.getChildCount() == 0) {
            return 0;
        }
        return 1;
    }

    // 获取用户自定义脚布局数量
    private int getFooterLayoutCount() {
        if (mFooterLayout == null || mFooterLayout.getChildCount() == 0) {
            return 0;
        }
        return 1;
    }

    // 获取加载状态的脚布局数量
    private int getLoadStateViewCount() {
        return mSupportLoadStateFooter ? 1 : 0;
    }


    // 表格布局 头、脚布局占一行 添加加载更多监听
    @CallSuper
    @Override
    public final void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;

        mIsSlidingUpward = false;
        mIsSlidingRight = false;

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // 变量 最后一个可见的position
                int lastItemPosition = -1;
                // 布局管理器
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();

                if (manager == null)
                    return;

                // 开始滚动（SCROLL_STATE_FLING），正在滚动(SCROLL_STATE_TOUCH_SCROLL), 已经停止（SCROLL_STATE_IDLE）

                // 当不滑动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的itemPosition

                    if (manager instanceof GridLayoutManager) {
                        //通过LayoutManager找到当前显示的最后的item的position
                        lastItemPosition = ((GridLayoutManager) manager).findLastCompletelyVisibleItemPosition();
                    } else if (manager instanceof LinearLayoutManager) {
                        lastItemPosition = ((LinearLayoutManager) manager).findLastCompletelyVisibleItemPosition();
                    } else if (manager instanceof StaggeredGridLayoutManager) {
                        //因为StaggeredGridLayoutManager的特殊性可能导致最后显示的item存在多个，所以这里取到的是一个数组
                        //得到这个数组后再取到数组中position值最大的那个就是最后显示的position值了
                        int[] lastPositions = new int[((StaggeredGridLayoutManager) manager).getSpanCount()];
                        ((StaggeredGridLayoutManager) manager).findLastCompletelyVisibleItemPositions(lastPositions);
                        lastItemPosition = findMax(lastPositions);
                    }

                    int itemCount = manager.getItemCount();

                    // 判断是否滑动到了最后一个item，并且是向上滑动
                    if (lastItemPosition == (itemCount - 1) && (mIsSlidingUpward || mIsSlidingRight) && mLoadMoreListener != null && (errorEnableLoadMore || loadState != LOAD_ERROR) && loadState != LOAD_END && loadState != LOADING) {
                        //加载更多
                        mLoadMoreListener.onLoadMore();
                    }
                }

                //防止第一行到顶部有空白区域
                if (manager instanceof StaggeredGridLayoutManager) {
                    ((StaggeredGridLayoutManager) manager).invalidateSpanAssignments();
                }

                if (mScrollListener != null)
                    mScrollListener.onScrollStateChanged(recyclerView, newState);
            }

            private int findMax(int[] lastPositions) {
                int max = lastPositions[0];
                for (int value : lastPositions) {
                    if (value > max) {
                        max = value;
                    }
                }
                return max;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 大于0表示正在向上滑动，小于等于0表示停止或向下滑动
                mIsSlidingUpward = dy > 0;
                // 大于0表示正在向右滑动，小于等于0表示停止或向左滑动
                mIsSlidingRight = dx > 0;

                if (mScrollListener != null)
                    mScrollListener.onScrolled(recyclerView, dx, dy);
            }
        });

        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();

        if (manager instanceof LinearLayoutManager) {
            if (((LinearLayoutManager) manager).getOrientation() == RecyclerView.HORIZONTAL) {
                mStateFooterLayout = R.layout.kitt_list_state_footer_horizontal;
                wrapParams = new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            }
        }

        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            final GridLayoutManager.SpanSizeLookup defSpanSizeLookup = gridManager.getSpanSizeLookup();
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {

                    int type = getItemViewType(position);

                    //  头脚布局 占满一行
                    if (type == TYPE_HEADER || type == TYPE_FOOTER || type == TYPE_LOAD_STATE_FOOTER) {
                        return gridManager.getSpanCount();
                    }

                    // 其他占据本身1个位置
                    return 1;
                }

            });
        }
    }


    // 流式布局 头、脚布局占一行
    @Override
    public final void onViewAttachedToWindow(@NonNull RecyclerViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        int type = holder.getItemViewType();
        if (type == TYPE_HEADER || type == TYPE_FOOTER || type == TYPE_LOAD_STATE_FOOTER) {
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            if (params instanceof StaggeredGridLayoutManager.LayoutParams
                    && holder.getLayoutPosition() == mData.size()) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) params;
                layoutParams.setFullSpan(true);
            }
        }
    }

    @Override
    public void onClick(View v) {

        int position = mRecyclerView.getChildAdapterPosition(v);

        T t = mData.get(position - getHeaderLayoutCount());
        if (mListener != null) {
            mListener.onClick(t, position - getHeaderLayoutCount(), v);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        int position = mRecyclerView.getChildAdapterPosition(view);
        T t = mData.get(position - getHeaderLayoutCount());
        if (mLongListener != null) {
            mLongListener.onLongClick(t, position - getHeaderLayoutCount(), view);
        }
        return mLongListener != null;
    }

    // 添加数据源
    public final void addData(List<T> data) {

        if (data == null)
            return;

        int start = mData.size();

        mData.addAll(data);
        notifyItemRangeInserted(start, data.size());
    }


    // 清空数据源
    @SuppressLint("NotifyDataSetChanged")
    public final void clearAll() {
        mData.clear();
        notifyDataSetChanged();
    }


    // 对外提供获取数据源的方法
    // 如果要更改数据源尽量通过addData() clearAll()方法
    public final List<T> getAttachData() {
        return mData;
    }

    // 对外提供获取指定位置的数据
    public final T getData(int position) {
        return mData.get(position);
    }

    // 对外提供获取数据的长度
    public final int getDataSize() {
        return mData.size();
    }

    // 对外提供获取context的方法
    public final Context getAttachContext() {
        return mContext;
    }

    // 对外提供获取布局填充器的方法
    public final LayoutInflater getInflater() {
        return mInflater;
    }

    // 对外提供获取布是否支持状态脚布局
    public final boolean isSupportLoadStateFooter() {
        return mSupportLoadStateFooter;
    }

    // 设置Item的点击事件
    public final void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.mListener = listener;
    }

    // 设置Item的点击事件
    public final void setOnItemLongClickListener(OnItemLongClickListener<T> listener) {
        this.mLongListener = listener;
    }

    // 设置加载更多事件
    public final void setOnLoadMoreListener(OnLoadMoreListener mLoadMoreListener) {
        this.mLoadMoreListener = mLoadMoreListener;
    }

    // 设置滚动监听事件
    public final void setOnScrollListener(OnScrollListener mScrollListener) {
        this.mScrollListener = mScrollListener;
    }

    // 设置加载错误的监听器的方法
    public final void setOnFooterErrorListener(OnFooterErrorListener mErrorListener) {
        this.mErrorListener = mErrorListener;
    }

    // 设置加载状态
    @SuppressLint("NotifyDataSetChanged")
    public final void setLoadState(LoadState loadState) {
        if (mSupportLoadStateFooter) {
            this.loadState = loadState.getState();
            notifyDataSetChanged();
        }
    }

    // 设置加载出错状态可否继续执行加载更多
    public final void setLoadMoreEnableByLoadError(boolean enable) {
        this.errorEnableLoadMore = enable;
    }

    // 设置空数据图片
    @SuppressLint("NotifyDataSetChanged")
    public final void setEmptyImg(@DrawableRes int emptyImg) {
        this.mEmptyImg = emptyImg;
        notifyDataSetChanged();
    }

    // 设置空数据是否使用动画
    @SuppressLint("NotifyDataSetChanged")
    public final void setEmptyLottie(boolean useLottie) {
        this.mEmptyLottie = useLottie;
        notifyDataSetChanged();
    }

    // 设置空数据文件
    @SuppressLint("NotifyDataSetChanged")
    public final void setEmptyText(String text) {
        this.mEmptyText = TextUtils.isEmpty(text) ? "" : text;
        notifyDataSetChanged();
    }

    private static class HeaderViewHolder extends RecyclerViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class FooterViewHolder extends RecyclerViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class FootLoadStateViewHolder extends RecyclerViewHolder {

        public FootLoadStateViewHolder(View itemView) {
            super(itemView);
        }
    }
}
