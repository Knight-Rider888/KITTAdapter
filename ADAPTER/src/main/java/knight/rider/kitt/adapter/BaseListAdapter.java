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
import android.view.ViewTreeObserver;
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

    // 是否支持上拉加载
    private final boolean mEnableLoadMore;

    // 当前加载状态，默认为加载完成
    private LoadState loadState = LoadState.LOAD_COMPLETE;


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

    // 完成状态是否显示
    private boolean mIsShowComplete = false;

    /**
     * 构造方法，默认支持上拉加载
     *
     * @param context The context to use.  Usually your {@link android.app.Activity} object.
     */
    public BaseListAdapter(Context context) {
        this(context, true);
    }

    /**
     * 构造方法
     *
     * @param context        The context to use.  Usually your {@link android.app.Activity} object.
     * @param enableLoadMore Is support load more ？
     */
    public BaseListAdapter(Context context, boolean enableLoadMore) {
        mData = new ArrayList<>();
        this.mContext = context;
        this.mEnableLoadMore = enableLoadMore;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        wrapParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        matchParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        noParams = new LinearLayout.LayoutParams(0, 0);
    }

    // ******** 获取item的数量 ********
    @Override
    public final int getItemCount() {
        return getHeaderLayoutCount() + mData.size() + getFooterLayoutCount() + getLoadStateViewCount();
    }

    // ******** 获取item的type类型 ********
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


    // ******** 创建Holder ********
    @NonNull
    @Override
    public final RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // 进行判断显示类型，来创建返回不同的View
        if (viewType == TYPE_LOAD_STATE_FOOTER) {

            View view = mInflater.inflate(mStateFooterLayout, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mErrorListener != null && loadState == LoadState.LOAD_ERROR) {
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


    // ******** 绑定显示数据 ********
    @Override
    public final void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {

        if (holder instanceof FootLoadStateViewHolder) {

            FootLoadStateViewHolder footViewHolder = (FootLoadStateViewHolder) holder;
            FrameLayout layout = footViewHolder.getFrameLayout(R.id.kitt_list_footer_layout);
            LottieAnimationView loadingView = footViewHolder.getView(R.id.kitt_list_lv_loading);
            LottieAnimationView errorView = footViewHolder.getView(R.id.kitt_list_lv_error);
            LottieAnimationView emptyView = footViewHolder.getView(R.id.kitt_list_none_lv);
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
                    layout.setLayoutParams(mIsShowComplete ? wrapParams : noParams);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_loading_layout).setVisibility(View.INVISIBLE);
                    footViewHolder.getLinearLayout(R.id.kitt_list_end_layout).setVisibility(View.GONE);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_error_layout).setVisibility(View.GONE);
                    footViewHolder.getRelativeLayout(R.id.kitt_list_no_data_layout).setVisibility(View.GONE);
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
            onBindViewHolders(holder, getItemViewsType(position), mData.get(position - getHeaderLayoutCount()), position - getHeaderLayoutCount());
        }
    }


    // 绑定数据
    public abstract void onBindViewHolders(RecyclerViewHolder holder, int viewType, T t, int position);


    /**
     * 移除所有头布局
     */
    public final void removeAllHeaders() {
        if (mHeaderLayout != null && mHeaderLayout.getChildCount() > 0) {
            mHeaderLayout.removeAllViews();
            notifyItemRangeRemoved(0, 1);
        }
    }

    /**
     * 移除指定头布局
     *
     * @param removeView the view to be removed in the header group
     */
    public final void removeHeader(View removeView) {
        if (mHeaderLayout != null) {
            mHeaderLayout.removeView(removeView);

            if (mHeaderLayout.getChildCount() == 0)
                notifyItemRangeRemoved(0, 1);
        }
    }

    /**
     * 移除指定位置的头布局
     *
     * @param index the position in the header group of the view to remove
     */
    public final void removeHeader(int index) {
        if (mHeaderLayout != null && index >= 0 && mHeaderLayout.getChildCount() > index) {
            mHeaderLayout.removeViewAt(index);

            if (mHeaderLayout.getChildCount() == 0)
                notifyItemRangeRemoved(0, 1);
        }
    }

    /**
     * 添加头布局，默认垂直摆放，顺序为自动排到末尾
     *
     * @param header the view to be added in the header group
     */
    public final void addHeaderView(View header) {
        addHeaderView(header, -1, LinearLayout.VERTICAL);
    }

    /**
     * 添加头布局，顺序为自动排到末尾
     *
     * @param header      the view to be added in the header group
     * @param orientation the orientation of the views
     */
    public final void addHeaderView(View header, int orientation) {
        addHeaderView(header, -1, orientation);
    }


    /**
     * 添加头布局
     * 注意：添加同一个头部视图对象，只进行替换，并不新增
     *
     * @param header      the view to be added in the header group
     * @param index       the position in the header group
     * @param orientation the orientation of the views
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


    /**
     * 移除所有脚布局
     */
    public final void removeAllFooters() {
        if (mFooterLayout != null && mFooterLayout.getChildCount() > 0) {
            mFooterLayout.removeAllViews();
            notifyItemRangeRemoved(getFooterPosition(), 1);
        }
    }

    /**
     * 移除指定脚布局
     *
     * @param removeView the view to be removed in the footer group
     */
    public final void removeFooter(View removeView) {
        if (mFooterLayout != null) {
            mFooterLayout.removeView(removeView);

            if (mFooterLayout.getChildCount() == 0)
                notifyItemRangeRemoved(getFooterPosition(), 1);
        }
    }

    /**
     * 移除指定位置的脚布局
     *
     * @param index the position in the footer group of the view to remove
     */
    public final void removeFooter(int index) {
        if (mFooterLayout != null && index >= 0 && mFooterLayout.getChildCount() > index) {
            mFooterLayout.removeViewAt(index);

            if (mFooterLayout.getChildCount() == 0)
                notifyItemRangeRemoved(getFooterPosition(), 1);
        }
    }

    /**
     * 添加脚布局，默认垂直摆放，顺序为自动排到末尾
     *
     * @param footer the view to be added in the header group
     */
    public final void addFooterView(View footer) {
        addFooterView(footer, -1, LinearLayout.VERTICAL);
    }

    /**
     * 添加脚布局，顺序为自动排到末尾
     *
     * @param footer      the view to be added in the header group
     * @param orientation the orientation of the views
     */
    public final void addFooterView(View footer, int orientation) {
        addFooterView(footer, -1, orientation);
    }

    /**
     * 添加脚布局
     *
     * @param footer      the view to be added in the header group
     * @param index       the position in the header group
     * @param orientation the orientation of the views
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
        return 1;
    }


    // 表格布局 头、脚布局占一行 添加加载更多监听
    @CallSuper
    @Override
    public final void onAttachedToRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;

        mIsSlidingUpward = false;
        mIsSlidingRight = false;

        // 布局管理器
        final RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // 动态更新完成时的脚布局
                        if (loadState == LoadState.LOAD_COMPLETE) {

                            if (manager == null)
                                return;

                            if (!mEnableLoadMore) {
                                mIsShowComplete = false;
                                return;
                            }

                            // 当前屏幕所看到的子项个数
                            int visibleItemCount = manager.getChildCount();

                            if (visibleItemCount == 0) {
                                // 还未构建
                                mIsShowComplete = false;
                                return;
                            }

                            if (visibleItemCount < manager.getItemCount()) {
                                // 超出一屏幕
                                mIsShowComplete = true;
                            } else {
                                // 未超出一屏幕
                                mIsShowComplete = false;
                            }
                        }
                    }
                });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (mScrollListener != null)
                    mScrollListener.onScrollStateChanged(recyclerView, newState);

                // 不支持上拉加载不监听了
                if (!mEnableLoadMore)
                    return;

                // 加载中、加载出错、无数据以及加载到底状态不监听
                if (loadState == LoadState.LOADING || loadState == LoadState.LOAD_END || loadState == LoadState.LOAD_ERROR || loadState == LoadState.LOAD_NO_DATA)
                    return;


                // 变量 最后一个可见的position
                int lastItemPosition = -1;

                if (manager == null)
                    return;

                // 开始滚动（SCROLL_STATE_FLING），正在滚动(SCROLL_STATE_TOUCH_SCROLL), 已经停止（SCROLL_STATE_IDLE）

                // 获取最后一个显示的itemPosition
                if (manager instanceof GridLayoutManager) {
                    //通过LayoutManager找到当前显示的最后的item的position
                    lastItemPosition = ((GridLayoutManager) manager).findLastVisibleItemPosition();
                } else if (manager instanceof LinearLayoutManager) {
                    lastItemPosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                } else if (manager instanceof StaggeredGridLayoutManager) {
                    //因为StaggeredGridLayoutManager的特殊性可能导致最后显示的item存在多个，所以这里取到的是一个数组
                    //得到这个数组后再取到数组中position值最大的那个就是最后显示的position值了
                    int[] lastPositions = new int[((StaggeredGridLayoutManager) manager).getSpanCount()];
                    ((StaggeredGridLayoutManager) manager).findLastVisibleItemPositions(lastPositions);
                    lastItemPosition = findMaxByStaggeredGrid(lastPositions);
                }

                int itemCount = manager.getItemCount();

                // 判断是否滑动到了非加载状态的最后一个item，并且是向上滑动
                if (lastItemPosition >= (itemCount - 1 - getLoadStateViewCount()) && (mIsSlidingUpward || mIsSlidingRight) && mLoadMoreListener != null && loadState != LoadState.LOAD_END && loadState != LoadState.LOADING && loadState != LoadState.LOAD_ERROR && loadState != LoadState.LOAD_NO_DATA) {
                    setLoadState(LoadState.LOADING);
                    //加载更多
                    mLoadMoreListener.onLoadMore();
                }

                //防止第一行到顶部有空白区域
                if (manager instanceof StaggeredGridLayoutManager) {
                    ((StaggeredGridLayoutManager) manager).invalidateSpanAssignments();
                }

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


    private int findMaxByStaggeredGrid(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
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

    /**
     * 添加数据源
     *
     * @param data collection containing elements to be added to this list
     */
    public final void addData(List<T> data) {

        if (data == null)
            return;

        int start = mData.size();

        mData.addAll(data);
        notifyItemRangeInserted(start, data.size());
    }


    /**
     * 清空数据源
     */
    @SuppressLint("NotifyDataSetChanged")
    public final void clearAll() {
        mData.clear();
        notifyDataSetChanged();
    }

    /**
     * 移除指定位置数据源
     *
     * @param position Position of the item that has now been removed
     */
    public final void remove(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount() - position);
    }


    /**
     * 获取数据源的方法,尽量不要使用此方法操作数据
     * 如内部提供的方法不能够满足，再使用此方式获取数据源进行操作
     */
    public final List<T> getAttachData() {
        return mData;
    }


    /**
     * 获取指定位置的实体类，请注意判空处理
     */
    public final T getData(int position) {
        return position < 0 || position + 1 > mData.size() ? null : mData.get(position);
    }

    /**
     * 获取数据源的长度
     */
    public final int getAttachDataSize() {
        return mData.size();
    }

    /**
     * 获取上下文对象
     */
    public final Context getAttachContext() {
        return mContext;
    }


    /**
     * 获取RecyclerView
     */
    public final RecyclerView getAttachRecyclerView() {
        return mRecyclerView;
    }


    /**
     * 获取布局填充墙
     */
    public final LayoutInflater getInflater() {
        return mInflater;
    }


    /**
     * 获取是否支持上拉加载
     */
    public boolean isEnableLoadMore() {
        return mEnableLoadMore;
    }

    /**
     * 设置Item的点击事件
     */
    public final void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.mListener = listener;
    }

    /**
     * 设置Item的长按点击事件
     */
    public final void setOnItemLongClickListener(OnItemLongClickListener<T> listener) {
        this.mLongListener = listener;
    }

    /**
     * 设置加载更多事件
     */
    public final void setOnLoadMoreListener(OnLoadMoreListener mLoadMoreListener) {
        this.mLoadMoreListener = mLoadMoreListener;
    }

    /**
     * 设置滚动监听事件
     */
    public final void setOnScrollListener(OnScrollListener mScrollListener) {
        this.mScrollListener = mScrollListener;
    }

    /**
     * 设置加载错误的监听的事件
     */
    public final void setOnFooterErrorListener(OnFooterErrorListener mErrorListener) {
        this.mErrorListener = mErrorListener;
    }

    /**
     * 设置加载状态
     */
    @SuppressLint("NotifyDataSetChanged")
    public final void setLoadState(LoadState loadState) {
        this.loadState = loadState;
        notifyDataSetChanged();

    }

    /**
     * 设置空数据状态图片
     */
    @SuppressLint("NotifyDataSetChanged")
    public final void setEmptyImg(@DrawableRes int emptyImg) {
        this.mEmptyImg = emptyImg;
        notifyDataSetChanged();
    }

    /**
     * 设置空数据状态是否使用动画
     */
    @SuppressLint("NotifyDataSetChanged")
    public final void setEmptyLottie(boolean useLottie) {
        this.mEmptyLottie = useLottie;
        notifyDataSetChanged();
    }

    /**
     * 设置空数据状态文字描述
     */
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
