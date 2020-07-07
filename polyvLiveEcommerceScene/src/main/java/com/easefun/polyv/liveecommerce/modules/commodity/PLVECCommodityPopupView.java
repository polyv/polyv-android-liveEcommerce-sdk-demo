package com.easefun.polyv.liveecommerce.modules.commodity;

import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.model.commodity.PolyvCommodityVO;
import com.easefun.polyv.livecommon.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品弹层view
 */
public class PLVECCommodityPopupView {
    private PopupWindow popupWindow;
    private ViewGroup emptyCommodityLy;
    private TextView commodityCountTv;
    private PLVECCommodityAdapter commodityAdapter;
    private PolyvCommodityVO commodityVO;

    public void showCommodityLayout(final View v, final PLVECCommodityAdapter.OnViewActionListener listener) {
        if (popupWindow == null) {
            popupWindow = new PopupWindow(v.getContext());

            View.OnClickListener handleHideListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                }
            };
            View view = PLVViewInitUtils.initPopupWindow(v, R.layout.plvec_live_commodity_layout, popupWindow, handleHideListener);
            PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
            view.findViewById(R.id.close_iv).setOnClickListener(handleHideListener);
            emptyCommodityLy = view.findViewById(R.id.empty_commodity_ly);
            commodityCountTv = view.findViewById(R.id.commodity_count_tv);
            commodityCountTv.setText(getCommodityCountMessage(commodityVO));
            final RecyclerView commodityRv = view.findViewById(R.id.commodity_rv);
            commodityRv.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(commodityRv.getContext(), LinearLayoutManager.VERTICAL, false);
            commodityRv.setLayoutManager(linearLayoutManager);
            commodityRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(20), 0));
            commodityAdapter = new PLVECCommodityAdapter();
            commodityAdapter.setOnViewActionListener(new PLVECCommodityAdapter.OnViewActionListener() {
                @Override
                public void onBuyCommodityClick(View view, PolyvCommodityVO.DataBean.ContentsBean contentsBean) {
                    if (listener != null) {
                        listener.onBuyCommodityClick(view, contentsBean);
                    }
                }
            });
            commodityAdapter.setDataList(toViewDataList(commodityVO));
            commodityRv.setAdapter(commodityAdapter);

            if (commodityAdapter.getItemCount() == 0) {
                emptyCommodityLy.setVisibility(commodityVO == null ? View.GONE : View.VISIBLE);
            }
        }
        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
    }

    public void setCommodityVO(PolyvCommodityVO commodityVO) {
        this.commodityVO = commodityVO;
        if (commodityAdapter != null) {
            commodityAdapter.setDataList(toViewDataList(commodityVO));
            commodityAdapter.notifyDataSetChanged();
        }
        if (commodityCountTv != null) {
            commodityCountTv.setText(getCommodityCountMessage(commodityVO));
        }
        if (emptyCommodityLy != null && commodityAdapter != null && commodityAdapter.getItemCount() == 0) {
            emptyCommodityLy.setVisibility(commodityVO == null ? View.GONE : View.VISIBLE);
        }
    }

    private CharSequence getCommodityCountMessage(PolyvCommodityVO commodityVO) {
        CharSequence message = "";
        if (commodityVO != null && commodityVO.getData() != null && commodityVO.getData().getContents() != null) {
            int count = commodityVO.getData().getContents().size();
            SpannableStringBuilder span = new SpannableStringBuilder("共" + count + "件商品");
            span.setSpan(new ForegroundColorSpan(Color.parseColor("#FFA611")), 1, (count + "").length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            message = span;
        }
        return message;
    }

    private List<PLVBaseViewData> toViewDataList(PolyvCommodityVO commodityVO) {
        List<PLVBaseViewData> viewDataList = new ArrayList<>();
        if (commodityVO != null && commodityVO.getData() != null) {
            List<PolyvCommodityVO.DataBean.ContentsBean> contentsBeanList = commodityVO.getData().getContents();
            if (contentsBeanList != null) {
                for (PolyvCommodityVO.DataBean.ContentsBean contentsBean : contentsBeanList) {
                    viewDataList.add(new PLVBaseViewData<>(contentsBean, PLVBaseViewData.ITEMTYPE_UNDEFINED));
                }
            }
        }
        return viewDataList;
    }

    //隐藏
    public void hide() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }
}
