package com.xx.UI.complex.list;

import com.xx.UI.complex.search.simple.box.BDSimpleSearchBox;
import com.xx.UI.util.BDMapping;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;

public class BDListView<T> extends ListView<T> {
    final SimpleObjectProperty<BDSimpleSearchBox<T>> searchBox = new SimpleObjectProperty<>();
    private final BDMapping mapping = new BDMapping();
    private final SimpleObjectProperty<BDListCellInitFactory<T>> listCellInitFactory = new SimpleObjectProperty<>();


    public BDListView(BDListCellInitFactory<T> initFactory) {
        setCellFactory(param -> new BDListCell<>((BDListView<T>) param));
        setListCellInitFactory(initFactory);
    }

    public BDMapping getMapping() {
        return mapping;
    }

    public BDListCellInitFactory<T> getListCellInitFactory() {
        return listCellInitFactory.get();
    }

    public void setListCellInitFactory(BDListCellInitFactory<T> initFactory) {
        this.listCellInitFactory.set(initFactory);
        refresh();
    }

    public SimpleObjectProperty<BDListCellInitFactory<T>> listCellInitFactoryProperty() {
        return listCellInitFactory;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new BDListViewSkin<>(this);
    }
}
