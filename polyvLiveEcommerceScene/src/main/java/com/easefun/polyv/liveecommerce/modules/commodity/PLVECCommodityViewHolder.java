package com.easefun.polyv.liveecommerce.modules.commodity;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.model.commodity.PolyvCommodityVO;
import com.easefun.polyv.livecommon.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.easefun.polyv.liveecommerce.R;

/**
 * 商品viewHolder
 */
public class PLVECCommodityViewHolder extends PLVBaseViewHolder<PLVBaseViewData, PLVECCommodityAdapter> {
    private PolyvCommodityVO.DataBean.ContentsBean contentsBean;
    private ImageView commodityCoverIv;
    private TextView commodityNameTv;
    private TextView commodityPriceTv;
    private TextView commodityShelfTv;

    public PLVECCommodityViewHolder(View itemView, final PLVECCommodityAdapter adapter) {
        super(itemView, adapter);
        commodityCoverIv = findViewById(R.id.commodity_cover_iv);
        commodityNameTv = findViewById(R.id.commodity_name_tv);
        commodityPriceTv = findViewById(R.id.commodity_price_tv);
        commodityShelfTv = findViewById(R.id.commodity_shelf_tv);
        commodityShelfTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    adapter.callOnBuyCommodityClick(v, contentsBean);
                }
            }
        });
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        contentsBean = (PolyvCommodityVO.DataBean.ContentsBean) data.getData();
        PLVImageLoader.getInstance().loadImage(contentsBean.getCover(), commodityCoverIv);
        commodityNameTv.setText(contentsBean.getName());
        commodityPriceTv.setText("¥" + trimZero(contentsBean.getPrice() + ""));
        if (contentsBean.getStatus() == 1) {
            commodityShelfTv.setText("去购买");
            commodityShelfTv.setSelected(true);
        } else {
            commodityShelfTv.setText("已下架");
            commodityShelfTv.setSelected(false);
        }
    }

    private String trimZero(String s) {
        if (s != null && s.indexOf(".") > 0) {
            // 去掉多余的0
            s = s.replaceAll("0+?$", "");
            // 如最后一位是.则去掉
            s = s.replaceAll("[.]$", "");
        }
        return s;
    }
}
