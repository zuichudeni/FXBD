package com.xx.UI.complex.tree;

import com.xx.UI.complex.search.simple.BDSimpleSearch;
import com.xx.UI.ui.BDUI;
import com.xx.UI.util.BDMapping;
import javafx.scene.control.skin.TreeViewSkin;

public class BDTreeViewSkin<T> extends TreeViewSkin<T> implements BDUI {
    private final BDMapping mapping ;
    private final BDTreeView<T> control;
    private final BDSimpleSearch simpleSearchBox;
    public BDTreeViewSkin(BDTreeView<T> control) {
        super(control);
        this.mapping = control.getMapping();
        this.control = control;
        this.simpleSearchBox = new BDSimpleSearch();
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
        this.mapping.addChildren(simpleSearchBox.getMapping());
    }

    @Override
    public void initUI() {
        BDUI.super.initUI();
        control.getStyleClass().add("bd-tree-view");
    }
}
