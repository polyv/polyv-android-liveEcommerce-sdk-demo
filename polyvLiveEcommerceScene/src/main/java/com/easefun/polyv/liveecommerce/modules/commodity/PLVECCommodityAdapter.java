package com.easefun.polyv.liveecommerce.modules.commodity;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.cloudclass.model.commodity.PolyvCommodityVO;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.easefun.polyv.liveecommerce.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品列表adapter
 */
public class PLVECCommodityAdapter extends PLVBaseAdapter<PLVBaseViewData, PLVBaseViewHolder<PLVBaseViewData, PLVECCommodityAdapter>> {
    private List<PLVBaseViewData> dataList;

    public PLVECCommodityAdapter() {
        dataList = new ArrayList<>();
    }

    @Override
    public List<PLVBaseViewData> getDataList() {
        return dataList;
    }

    public void setDataList(List<PLVBaseViewData> dataList) {
        this.dataList = dataList;
    }

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    private OnViewActionListener onViewActionListener;

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public interface OnViewActionListener {
        void onBuyCommodityClick(View view, PolyvCommodityVO.DataBean.ContentsBean contentsBean);
    }

    public void callOnBuyCommodityClick(View view, PolyvCommodityVO.DataBean.ContentsBean contentsBean) {
        if (onViewActionListener != null) {
            onViewActionListener.onBuyCommodityClick(view, contentsBean);
        }
    }
    // </editor-fold>

    @NonNull
    @Override
    public PLVBaseViewHolder<PLVBaseViewData, PLVECCommodityAdapter> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PLVECCommodityViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plvec_live_commodity_list_item, parent, false), this);
    }

    @Override
    public void onBindViewHolder(@NonNull PLVBaseViewHolder<PLVBaseViewData, PLVECCommodityAdapter> holder, int position) {
        holder.processData(dataList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
