package com.xx.UI.complex.stage;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Node;

/*
* BDContent 的侧边栏
* */
class BDSidebar extends BDControl {
    final SimpleListProperty<Node> fronFrontSideBarItems = new SimpleListProperty<>(FXCollections.observableArrayList());
    final SimpleListProperty<Node> fronSideBarItems = new SimpleListProperty<>(FXCollections.observableArrayList());
    final SimpleListProperty<Node> afterSideBarItems = new SimpleListProperty<>(FXCollections.observableArrayList());
    final Orientation orientation;
    final BDContent content;
    final boolean addFrontSideNode(Node node, BDInSequence inSequence){
        if (node instanceof BDSideBarItem item)
            item.sidebar.set(this);
        return switch (inSequence) {
            case FRONT -> {
                fronFrontSideBarItems.add(node);
                yield true;
            }
            case AFTER -> {
                fronSideBarItems.add(node);
                yield true;
            }
            case null -> false;
        };
    }
    final boolean addAfterSideNode(Node node){
        if (node instanceof BDSideBarItem item)
            item.sidebar.set(this);
        return afterSideBarItems.add(node);
    }
    boolean removeSideItem(Node node){
        if (node instanceof BDSideBarItem item)
            item.sidebar.set(null);
        return fronSideBarItems.remove(node) || afterSideBarItems.remove(node) || fronFrontSideBarItems.remove(node);
    }
    BDSidebar(BDContent content,Orientation orientation) {
        this.content = content;
        this.orientation = orientation;
    }
    void showSideBarItem(BDSideBarItem sideBarItem){
        if (sideBarItem.isWindowOpen()){
            sideBarItem.stage.get().show();
        }
        else {
            fronFrontSideBarItems.forEach(node -> {
                if (node instanceof BDSideBarItem item && !item.isWindowOpen() && item.isSelected())
                    item.setSelected(false);
            });
            fronSideBarItems.forEach(node -> {
                if (node instanceof BDSideBarItem item && !item.isWindowOpen() && item.isSelected())
                    item.setSelected(false);
            });
            afterSideBarItems.forEach(node -> {
                if (node instanceof BDSideBarItem item && !item.isWindowOpen() && item.isSelected())
                    item.setSelected(false);
            });

        }
    }
    void closeSideBarItem(BDSideBarItem sideBarItem){
    }
    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDSideBarSkin(this);
    }
}
