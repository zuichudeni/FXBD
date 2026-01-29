package com.xx.UI.complex.stage;

import com.xx.UI.basic.button.BDButton;
import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/*
 * BDContent 的侧边内容栏
 * */
public class BDSideContent extends BDControl {
    final VBox rootPane = new VBox();
    final AnchorPane headerPane = new AnchorPane();
    final StackPane contentPane = new StackPane();
    final HBox leadingPane = new HBox();
    final HBox trailingPane = new HBox();
    final BDButton dock = new BDButton();
    final BDSideContentSkin skin;
    private final SimpleStringProperty title = new SimpleStringProperty(); // 侧边内容栏的标题
    private final SimpleListProperty<Node> fronNodeItems = new SimpleListProperty<>(FXCollections.observableArrayList()); // 侧边内容栏的前置节点列表
    private final SimpleListProperty<Node> afterNodeItems = new SimpleListProperty<>(FXCollections.observableArrayList()); // 侧边内容栏的后置节点列表
    private final SimpleObjectProperty<Node> content = new SimpleObjectProperty<>();// 侧边内容栏的内容
    private final BooleanProperty show = new SimpleBooleanProperty(true); // 侧边内容栏后侧节点是否使用动画
    BDSideBarItem item;

    public BDSideContent() {
        skin = new BDSideContentSkin(this);
    }

    void setItem(BDSideBarItem item) {
        this.item = item;
        mapping.bindProperty(item.windowOpenProperty(), dock.visibleProperty(), dock.managedProperty())
                .addEventHandler(dock, ActionEvent.ACTION, _ -> item.windowClose());
    }

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

    public Node getContent() {
        return content.get();
    }

    public void setContent(Node content) {
        this.content.set(content);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public BooleanProperty showProperty() {
        return show;
    }

    public boolean getShow() {
        return show.get();
    }

    public void setShow(boolean show) {
        this.show.set(show);
    }

    void windowAction() {
        rootPane.getChildren().clear();
        headerPane.getChildren().clear();
    }

    void sideBarContentAction() {
        headerPane.getChildren().setAll(leadingPane, trailingPane);
        rootPane.getChildren().setAll(headerPane, contentPane);
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return skin;
    }
}
