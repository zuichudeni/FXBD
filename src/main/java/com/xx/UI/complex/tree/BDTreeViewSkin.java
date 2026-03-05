package com.xx.UI.complex.tree;

import com.xx.UI.ui.BDUI;
import com.xx.UI.util.BDMapping;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.text.Text;

public class BDTreeViewSkin<T> extends TreeViewSkin<T> implements BDUI {
    private final BDMapping mapping ;
    private final BDTreeView<T> control;
    public BDTreeViewSkin(BDTreeView<T> control) {
        super(control);
        this.mapping = control.getMapping();
        this.control = control;
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
        control.getStyleClass().add("bd-tree-view");
    }
}
