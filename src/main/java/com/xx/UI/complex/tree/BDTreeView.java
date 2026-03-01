package com.xx.UI.complex.tree;

import com.xx.UI.util.BDMapping;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.Stack;

public class BDTreeView<T> extends TreeView<T> {
    private final BDMapping mapping = new BDMapping();
    private final SimpleObjectProperty<BDTreeCellInitFactory<T>> treeCellInitFactory = new SimpleObjectProperty<>();

    public BDTreeView() {
        setCellFactory(param -> new BDTreeCell<>((BDTreeView<T>) param));
    }

    public void sortItem() {
        Stack<TreeItem<T>> stack = new Stack<>();
        if (getRoot() != null && !getRoot().getChildren().isEmpty())
            stack.push(getRoot());
        while (!stack.empty()) {
            TreeItem<T> pop = stack.pop();
            pop.getChildren().sort((o1, o2) -> getTreeCellInitFactory().compare(o1.getValue(), o2.getValue()));
            if (!pop.getChildren().isEmpty())
                pop.getChildren().forEach(stack::push);
        }
    }

    public BDTreeCellInitFactory<T> getTreeCellInitFactory() {
        return treeCellInitFactory.get();
    }

    public void setTreeCellInitFactory(BDTreeCellInitFactory<T> cellInitFactory) {
        this.treeCellInitFactory.set(cellInitFactory);
    }

    public BDMapping getMapping() {
        return mapping;
    }

    public SimpleObjectProperty<BDTreeCellInitFactory<T>> treeCellInitFactoryProperty() {
        return treeCellInitFactory;
    }
}
