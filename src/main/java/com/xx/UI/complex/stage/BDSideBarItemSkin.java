package com.xx.UI.complex.stage;

import com.xx.UI.basic.BDButton;
import com.xx.UI.basic.BDButtonSkin;
import javafx.event.ActionEvent;

public class BDSideBarItemSkin extends BDButtonSkin {

    public BDSideBarItemSkin(BDButton button) {
        super(button);
    }

    @Override
    public void initEvent() {
        super.initEvent();
    }

    @Override
    public void initProperty() {
        super.initProperty();
        mapping.addListener(()->{
             if (control.isSelected() && control instanceof BDSideBarItem item && item.sidebar.get() != null)
                item.sidebar.get().showSideBarItem(item);
             else if (!control.isSelected() && control instanceof BDSideBarItem item && item.sidebar.get() != null)
                item.sidebar.get().closeSideBarItem(item);
        },true,control.selectedProperty());
    }

    @Override
    public void initUI() {
        control.getStyleClass().add("bd-side-bar-item");
    }
}
