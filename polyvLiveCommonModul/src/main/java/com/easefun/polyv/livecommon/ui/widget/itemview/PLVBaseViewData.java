package com.easefun.polyv.livecommon.ui.widget.itemview;

/**
 * 基础列表item数据
 */
public class PLVBaseViewData<Data> {
    public static final int ITEMTYPE_UNDEFINED = 0;
    private Data data;
    private int itemType;

    public PLVBaseViewData(Data data, int itemType) {
        this.data = data;
        this.itemType = itemType;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }
}
