package com.xx.UI.complex.tree;

import com.xx.UI.ui.BDUI;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.text.Text;

public class BDTreeViewSkin<T> extends TreeViewSkin<T> implements BDUI {
    public BDTreeViewSkin(TreeView control) {
        super(control);
        initUI();
        initEvent();
        initProperty();
    }

    @Override
    public void initEvent() {
        BDUI.super.initEvent();
    }

    @Override
    public void initProperty() {
        BDUI.super.initProperty();
    }

    @Override
    public void initUI() {
        BDUI.super.initUI();
    }
}
