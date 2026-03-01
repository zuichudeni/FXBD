package com.xx.UI.complex.tree;

import com.xx.UI.ui.BDUI;
import com.xx.UI.ui.BDVirtualUI;
import com.xx.UI.util.BDMapping;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Objects;

public class BDTreeCell<T> extends TreeCell<T> implements BDVirtualUI, BDUI {
    private final BDTreeView<T> treeView;
    private final Text text = new Text();
    private final HBox hBox = new HBox();
    private final Rectangle line = new Rectangle();
    private final Pane root = new AnchorPane(hBox,line);
    private final BDMapping mapping = new BDMapping();

    public BDTreeCell(BDTreeView<T> treeView) {
        this.treeView = treeView;
        treeView.getMapping().addChildren(mapping);
        initUI();
        initEvent();
        initProperty();
    }

    @Override
    public void initUI() {
        text.getStyleClass().add("bd-tree-cell-text");
        hBox.getStyleClass().add("bd-tree-hbox");
        root.getStyleClass().add("bd-tree-root");
        getStyleClass().add("bd-tree-cell");
        line.getStyleClass().add("bd-tree-cell-line");
        line.setWidth(2);
        AnchorPane.setLeftAnchor(line,.0);
        AnchorPane.setTopAnchor(line,.0);
        AnchorPane.setBottomAnchor(line,.0);
        line.setMouseTransparent(true);
        AnchorPane.setLeftAnchor(hBox,.0);
        AnchorPane.setTopAnchor(hBox,.0);
        AnchorPane.setBottomAnchor(hBox,.0);
        setPadding(Insets.EMPTY);
        setBorder(Border.EMPTY);
        setFont(Font.font(20));
    }

    @Override
    public void initProperty() {
        mapping.addListener(()->{
            line.setHeight(getHeight() - 2);
        },true,heightProperty(),emptyProperty());
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) disposeVirtual();
        else {
            initVirtualUI();
            initVirtualEvent();
            initVirtualProperty();
        }
    }

    @Override
    public void initVirtualUI() {
        Node graphic = treeView.getTreeCellInitFactory().initGraphic(getItem());
        String info = treeView.getTreeCellInitFactory().getInfo(getItem());
        text.setText(info);
        hBox.getChildren().setAll(graphic,text);
        treeView.getTreeCellInitFactory().rendering(getItem(), graphic, text, hBox);
//        检查text的内容是否被更改
        if (!Objects.equals(info, text.getText()))
            throw new UnsupportedOperationException("文本信息发生了变化：oldText=%s,newText=%s".formatted(info, text.getText()));
        if (!hBox.getChildren().contains(graphic))
            throw new UnsupportedOperationException("不能删除graphic");
        if (!hBox.getChildren().contains(text))
            throw new UnsupportedOperationException("不能删除text");
        setGraphic(root);
    }

    @Override
    public void disposeVirtual() {
        setGraphic(null);
    }
}
