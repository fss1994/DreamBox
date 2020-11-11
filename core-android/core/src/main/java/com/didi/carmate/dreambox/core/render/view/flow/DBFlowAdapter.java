package com.didi.carmate.dreambox.core.render.view.flow;

import android.view.View;
import android.view.ViewGroup;

import com.didi.carmate.dreambox.core.base.DBContext;
import com.didi.carmate.dreambox.core.render.view.DBRootView;
import com.didi.carmate.dreambox.core.render.view.list.IAdapterCallback;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * author: chenjing
 * date: 2020/7/14
 */
public class DBFlowAdapter<E> extends DBFlowLayout.FlowAdapter<E> {
    private DBContext mDBContext;
    private IAdapterCallback mAdapterCallback;
    private List<JsonObject> mDataList;

    public DBFlowAdapter(DBContext dbContext, ViewGroup viewGroup, List<JsonObject> dataList, IAdapterCallback adapterCallback) {
        super(dbContext.getContext(), viewGroup);
        mDBContext = dbContext;
        mAdapterCallback = adapterCallback;
        mDataList = (dataList == null) ? new ArrayList<JsonObject>() : dataList;
    }

    public void setData(List<JsonObject> listData) {
        mDataList = listData;
        notifyDataSetChanged();
    }

    @Override
    View getView(int index) {
        DBRootView rootView = new DBRootView(mDBContext);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.setLayoutParams(lp);

        if (null != mAdapterCallback) {
            mAdapterCallback.onBindItemView(rootView, mDataList.get(index));
        }
        return rootView;
    }

    @Override
    int getCount() {
        return mDataList.size();
    }

    public void setDataList(List<JsonObject> dataList) {
        if (null != dataList) {
            this.mDataList = dataList;
        } else {
            this.mDataList = new ArrayList<>();
        }
    }
}
