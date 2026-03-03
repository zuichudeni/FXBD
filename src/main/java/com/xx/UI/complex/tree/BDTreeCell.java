package com.xx.UI.complex.tree;

import com.xx.UI.ui.BDUI;
import com.xx.UI.ui.BDVirtualUI;
import com.xx.UI.util.BDMapping;
import javafx.beans.Observable;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xx.UI.complex.tree.BDTreeCellInitFactory.getDeep;

public class BDTreeCell<T> extends TreeCell<T> implements BDVirtualUI, BDUI {
    private final BDTreeView<T> treeView;
    private final Text text = new Text();
    private final HBox hBox = new HBox();
    private final Pane linePane = new AnchorPane();
    private final Pane root = new AnchorPane(hBox, linePane);
    private final BDMapping mapping = new BDMapping();
    private final double translateX = -24;
    private final double disclosureNodeWidth = 20;
    private final ImageView imageView = new ImageView();
    private final Label disclosureNode = new Label();
    private final PseudoClass focus = PseudoClass.getPseudoClass("focus");

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
        AnchorPane.setLeftAnchor(hBox, .0);
        AnchorPane.setTopAnchor(hBox, .0);
        AnchorPane.setBottomAnchor(hBox, .0);
        setPadding(Insets.EMPTY);
        setBorder(Border.EMPTY);
        setFont(Font.font(20));
        setContentDisplay(null);
        imageView.setFitWidth(disclosureNodeWidth);
        imageView.setPreserveRatio(true);
        disclosureNode.setPadding(Insets.EMPTY);
        disclosureNode.setGraphic(imageView);
        disclosureNode.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        disclosureNode.setAlignment(Pos.CENTER);
        AnchorPane.setLeftAnchor(linePane, .0);
        AnchorPane.setTopAnchor(linePane, .0);
        AnchorPane.setBottomAnchor(linePane, .0);
        AnchorPane.setTopAnchor(disclosureNode, .0);
        AnchorPane.setBottomAnchor(disclosureNode, .0);
        AnchorPane.setLeftAnchor(disclosureNode, translateX);
        linePane.setMouseTransparent(true);

    }

    @Override
    public void initProperty() {
        mapping.addListener(() -> {
                    disposeVirtual();
                    if (!isEmpty()) {
                        initVirtualUI();
                        initVirtualEvent();
                        initVirtualProperty();
                    }
                }, true, (Observable) treeView.selectBroItem, treeItemProperty());
    }

    @Override
    public void initEvent() {
        mapping.addEventHandler(disclosureNode, MouseEvent.MOUSE_CLICKED,_->{
            TreeItem<T> treeItem = getTreeItem();
            if (treeItem != null)
                treeItem.setExpanded(!treeItem.isExpanded());
        });
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        disposeVirtual();
        if (!isEmpty()) {
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
        hBox.getChildren().setAll(graphic, text);
        treeView.getTreeCellInitFactory().rendering(getItem(), graphic, text, hBox);
//        检查text的内容是否被更改
        if (!Objects.equals(info, text.getText()))
            throw new UnsupportedOperationException("文本信息发生了变化：oldText=%s,newText=%s".formatted(info, text.getText()));
        if (!hBox.getChildren().contains(graphic))
            throw new UnsupportedOperationException("不能删除graphic");
        if (!hBox.getChildren().contains(text))
            throw new UnsupportedOperationException("不能删除text");
        setGraphic(root);
        refreshLine();
        refreshDisclosureNode();
        applyCss();
        layout();
    }
    private void refreshFocusLine(){
            if (!linePane.getChildren().isEmpty()) {
                TreeItem<T> treeItem = getTreeItem();
                List<Node> focusItem = new ArrayList<>();
                if (treeView.selectBroItem.contains(treeItem)) {
                    if (!treeItem.isExpanded())
                        focusItem.add(linePane.getChildren().getFirst());
                    else focusItem.add(linePane.getChildren().get(1));
                } else {
                    treeView.selectBroItem.forEach(broItem -> {
                        int deep = getDeep(broItem, treeItem);
                        if (deep != -1) {
                            if (!treeItem.isExpanded())
                                focusItem.add(linePane.getChildren().get(deep));
                            else focusItem.add(linePane.getChildren().get(deep + 1));
                        } else if (Objects.equals(broItem.getParent(),treeItem))
                            focusItem.add(linePane.getChildren().getFirst());
                    });
                }
                linePane.getChildren().forEach(node -> node.pseudoClassStateChanged(focus, focusItem.contains(node)));
            }
        }
    private void refreshDisclosureNode() {
        root.getChildren().remove(disclosureNode);
        if (getTreeItem() == null) return;
        if (!getTreeItem().isLeaf()) {
            root.getChildren().add(disclosureNode);
            imageView.setImage(treeView.getTreeCellInitFactory().getDisclosureNodeImage(getTreeItem().isExpanded()));
        }
    }

    private void refreshLine() {
        linePane.getChildren().clear();
        TreeItem<T> treeItem = getTreeItem();
        if (treeItem == null) return;
        Stack<TreeItem<T>> stack = new Stack<>();
        stack.push(treeItem);
        AtomicInteger index = new AtomicInteger(1);
        if (!treeItem.getChildren().isEmpty() && treeItem.isExpanded())
            linePane.getChildren().add(getLine(0));
        while (!stack.empty()) {
            TreeItem<T> pop = stack.pop();
            if (pop.getParent() instanceof TreeItem<T> parent) {
                stack.push(parent);
                Pane line = getLine(index.getAndIncrement());
                linePane.getChildren().add(line);
            }
        }
        refreshFocusLine();
    }

    private Pane getLine(int level) {
        Pane line = new Pane();
        line.getStyleClass().add("bd-tree-cell-line");
        AnchorPane.setLeftAnchor(line, .0);
        AnchorPane.setTopAnchor(line, level == 0 ? root.getHeight() / 1.5 : .0);
        AnchorPane.setBottomAnchor(line, .0);
        line.setTranslateX(translateX * (level + 1) + disclosureNodeWidth / 2 + line.getPrefWidth() / 2);
        return line;
    }

    @Override
    public void disposeVirtual() {
        setGraphic(null);
        setDisclosureNode(null);
    }
}
