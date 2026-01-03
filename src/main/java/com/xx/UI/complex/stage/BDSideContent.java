package com.xx.UI.complex.stage;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.beans.property.*;
import javafx.scene.Node;

/*
* BDContent 的侧边内容栏
* */
public class BDSideContent extends BDControl {
    private final SimpleStringProperty title = new SimpleStringProperty(); // 侧边内容栏的标题
    private final SimpleListProperty<Node> fronNodeItems = new SimpleListProperty<>(); // 侧边内容栏的前置节点列表
    private final SimpleListProperty<Node> afterNodeItems = new SimpleListProperty<>(); // 侧边内容栏的后置节点列表
    private final SimpleObjectProperty<Node> content = new SimpleObjectProperty<>();// 侧边内容栏的内容
    private final BooleanProperty animated = new SimpleBooleanProperty(true); // 侧边内容栏后侧节点是否使用动画
    public boolean addFronNodeItem(Node node) {
        return fronNodeItems.add(node);
    }
    public boolean addAfterNodeItem(Node node) {
        return afterNodeItems.add(node);
    }
    public ReadOnlyListProperty<Node> preNodeItemsProperty() {
        return fronNodeItems;
    }
    public ReadOnlyListProperty<Node> afterNodeItemsProperty() {
        return afterNodeItems;
    }
    public ObjectProperty<Node> contentProperty() {
        return content;
    }
    public StringProperty titleProperty() {
        return title;
    }
    public void setTitle(String title) {
        this.title.set(title);
    }
    public String getTitle() {
        return title.get();
    }
    public BooleanProperty animatedProperty() {
        return animated;
    }
    public boolean isAnimated() {
        return animated.get();
    }
    public void setAnimated(boolean animated) {
        this.animated.set(animated);
    }
    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDSideContentSkin(this);
    }
}
