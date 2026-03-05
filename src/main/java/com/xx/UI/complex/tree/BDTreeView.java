package com.xx.UI.complex.tree;

import com.xx.UI.util.BDMapping;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.*;

public class BDTreeView<T> extends TreeView<T> {
    final SimpleListProperty<TreeItem<T>> selectBroItem = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final BDMapping mapping = new BDMapping();
    private final SimpleObjectProperty<BDTreeCellInitFactory<T>> treeCellInitFactory = new SimpleObjectProperty<>();
    Runnable selectedItemDispose;

    public BDTreeView() {
        setCellFactory(param -> new BDTreeCell<>((BDTreeView<T>) param));
        mapping.addListener(() -> {
            if (selectedItemDispose != null) selectedItemDispose.run();
            ListChangeListener<TreeItem<T>> listener = change -> {
                selectBroItem.clear();
                Set<TreeItem<T>> items = new HashSet<>();
                change.getList().forEach(item -> {
                    if (item.getParent() instanceof TreeItem<T> parent)
                        items.addAll(parent.getChildren());
                });
                selectBroItem.setAll(items);
            };
            MultipleSelectionModel<TreeItem<T>> model = getSelectionModel();
            model.getSelectedItems().addListener(listener);
            selectedItemDispose = ()-> model.getSelectedItems().removeListener(listener);
        }, true, selectionModelProperty());
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

    @Override
    protected Skin<?> createDefaultSkin() {
        return new BDTreeViewSkin<>(this);
    }
}
