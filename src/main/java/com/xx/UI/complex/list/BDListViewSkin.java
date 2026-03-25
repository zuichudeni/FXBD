package com.xx.UI.complex.list;

import com.xx.UI.ui.BDUI;
import javafx.scene.control.skin.ListViewSkin;

public class BDListViewSkin<T> extends ListViewSkin<T> implements BDUI {
    private final BDListView<T> control;

    public BDListViewSkin(BDListView<T> listView) {
        super(listView);
        this.control = listView;
        initUI();
        initEvent();
        initProperty();
    }

    @Override
    public void initUI() {
        control.getStyleClass().add("bd-list-view");
    }
}
