package com.didi.carmate.dreambox.core.render.view.list;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.didi.carmate.dreambox.core.base.DBConstants;
import com.didi.carmate.dreambox.core.render.view.DBRootView;

import java.util.ArrayList;
import java.util.List;

/**
 * author: chenjing
 * date: 2020/7/1
 */
public class DBListAdapter extends RecyclerView.Adapter<DBListViewHolder> {

    private static final int TYPE_REFRESH_HEADER = 10000;
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_FOOTER_VIEW = 10001;
    private static final int HEADER_INIT_INDEX = 10002;
    private List<Integer> mHeaderTypes = new ArrayList<>();

    private DBListInnerAdapter mInnerAdapter;
    private IRefreshIndicator mRefreshArea;
    private String mOrientation;
    private boolean mHasHeader;
    private boolean mHasFooter;
    private int mParentWidth;
    private int mParentHeight;

    private ArrayList<DBRootView> mHeaderViews = new ArrayList<>();
    // 同时只支持一个footerView或loadingView，如果开启了loading则不应该再添加footerView，否则分页加载时会闪现footerView
    private ArrayList<DBRootView> mFooterViews = new ArrayList<>();

    private IAdapterCallback mAdapterCallback;

    public DBListAdapter(DBListInnerAdapter innerAdapter, @NonNull IAdapterCallback adapterCallback, String orientation,
                         boolean hasHeader, boolean hasFooter) {
        mInnerAdapter = innerAdapter;
        mAdapterCallback = adapterCallback;
        mOrientation = orientation;
        mHasHeader = hasHeader;
        mHasFooter = hasFooter;
    }

    void setRefreshHeader(IRefreshIndicator refreshArea) {
        mRefreshArea = refreshArea;
    }

    DBListInnerAdapter getInnerAdapter() {
        return mInnerAdapter;
    }

    public void addHeaderView(DBRootView view) {
        if (view == null) {
            throw new RuntimeException("header is null");
        }

        mHeaderTypes.add(HEADER_INIT_INDEX + mHeaderViews.size());
        mHeaderViews.add(view);
    }

    public void addFooterView(DBRootView view) {
        if (view == null) {
            throw new RuntimeException("footer is null");
        }

        removeFooterView();
        mFooterViews.add(view);
    }

    /**
     * 根据header的ViewType判断是哪个header
     */
    private DBRootView getHeaderViewByType(int itemType) {
        return mHeaderViews.get(itemType - HEADER_INIT_INDEX);
    }

    /**
     * 判断一个type是否为HeaderType
     */
    private boolean isHeaderType(int itemViewType) {
        return mHeaderViews.size() > 0 && mHeaderTypes.contains(itemViewType);
    }

    /**
     * 返回第一个FootView,可能是LoadingView，也可能是FooterView
     */
    private View getFooterView() {
        return getFooterViewsCount() > 0 ? mFooterViews.get(0) : null;
    }

    void removeFooterView() {
        if (getFooterViewsCount() > 0) {
            View footerView = getFooterView();
            mFooterViews.remove(footerView);
            this.notifyDataSetChanged();
        }
    }

    int getHeaderViewsCount() {
        return mHeaderViews.size();
    }

    int getFooterViewsCount() {
        return mFooterViews.size();
    }

    private boolean isHeader(int position) {
        return position >= 1 && position < mHeaderViews.size() + 1;
    }

    private boolean isRefreshHeader(int position) {
        return position == 0;
    }

    /**
     * 可能是footer view，也可能是loading view
     */
    private boolean isFooter(int position) {
        int lastPosition = getItemCount() - getFooterViewsCount();
        return getFooterViewsCount() > 0 && position >= lastPosition;
    }

    @NonNull
    @Override
    public DBListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_REFRESH_HEADER) {
            return new DBListViewHolder(mRefreshArea.getHeaderView());
        } else if (isHeaderType(viewType)) {
            DBRootView rootView = getHeaderViewByType(viewType);
            ViewGroup.LayoutParams lp;
            if (mOrientation.equals(DBConstants.LIST_ORIENTATION_H)) {
                if (mParentHeight == 0) {
                    mParentHeight = ((DBListView) parent).getLayoutManager().getHeight();
                }
                lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mParentHeight);
            } else {
                if (mParentWidth == 0) {
                    mParentWidth = ((DBListView) parent).getLayoutManager().getWidth();
                }
                lp = new ViewGroup.LayoutParams(mParentWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            rootView.setLayoutParams(lp);
            return new DBListViewHolder(rootView);
        } else if (viewType == TYPE_FOOTER_VIEW) {
            DBRootView rootView = mFooterViews.get(0);
            ViewGroup.LayoutParams lp;
            if (mOrientation.equals(DBConstants.LIST_ORIENTATION_H)) {
                if (mParentHeight == 0) {
                    mParentHeight = ((DBListView) parent).getLayoutManager().getHeight();
                }
                lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mParentHeight);
            } else {
                if (mParentWidth == 0) {
                    mParentWidth = ((DBListView) parent).getLayoutManager().getWidth();
                }
                lp = new ViewGroup.LayoutParams(mParentWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            rootView.setLayoutParams(lp);
            return new DBListViewHolder(rootView);
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull final DBListViewHolder holder, int position) {
        if (isRefreshHeader(position)) {
            return;
        }
        if (isHeader(position)) {
            mAdapterCallback.onBindHeaderView(holder.getListRootView());
            return;
        }
        if (mHasFooter && isFooter(position)) {
            holder.getListRootView().removeAllViews();// 无法复用，因为LoadingView需要实时根据加进来 TODO 改进
            mAdapterCallback.onBindFooterView(holder.getListRootView());
            return;
        }
        final int adjPosition = position - (getHeaderViewsCount() + 1);
        if (mInnerAdapter != null) {
            if (adjPosition < mInnerAdapter.getItemCount()) {
                mInnerAdapter.onBindViewHolder(holder, adjPosition);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final DBListViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            if (isHeader(position) || isRefreshHeader(position)) {
                return;
            }
            final int adjPosition = position - (getHeaderViewsCount() + 1);
            int adapterCount;
            if (mInnerAdapter != null) {
                adapterCount = mInnerAdapter.getItemCount();
                if (adjPosition < adapterCount) {
                    mInnerAdapter.onBindViewHolder(holder, adjPosition, payloads);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mInnerAdapter != null) {
            return getHeaderViewsCount() + getFooterViewsCount() + mInnerAdapter.getItemCount() + 1;
        } else {
            return getHeaderViewsCount() + getFooterViewsCount() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        int adjPosition = position - (getHeaderViewsCount() + 1);
        if (isRefreshHeader(position)) {
            return TYPE_REFRESH_HEADER;
        }
        if (isHeader(position)) {
            position = position - 1;
            return mHeaderTypes.get(position);
        }
        if (isFooter(position)) {
            return TYPE_FOOTER_VIEW;
        }
        int adapterCount;
        if (mInnerAdapter != null) {
            adapterCount = mInnerAdapter.getItemCount();
            if (adjPosition < adapterCount) {
                return mInnerAdapter.getItemViewType(adjPosition);
            }
        }
        return TYPE_NORMAL;
    }

    @Override
    public long getItemId(int position) {
        if (mInnerAdapter != null && position >= getHeaderViewsCount()) {
            int adjPosition = position - getHeaderViewsCount();
            //判断是否setHasStableIds(true);
            if (hasStableIds()) {
                adjPosition--;
            }
            int adapterCount = mInnerAdapter.getItemCount();
            if (adjPosition < adapterCount) {
                return mInnerAdapter.getItemId(adjPosition);
            }
        }
        return -1;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mInnerAdapter.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        mInnerAdapter.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull DBListViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        mInnerAdapter.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull DBListViewHolder holder) {
        mInnerAdapter.onViewDetachedFromWindow(holder);
    }
}
