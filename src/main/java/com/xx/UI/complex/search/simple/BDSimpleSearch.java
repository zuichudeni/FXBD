package com.xx.UI.complex.search.simple;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;

import java.util.List;

public class BDSimpleSearch extends BDControl {
    private final SimpleStringProperty search = new SimpleStringProperty();
    private final SimpleListProperty<BDSimpleSearchUpdate> searchUpdates = new SimpleListProperty<>(FXCollections.observableArrayList());

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDSimpleSearchSkin(this);
    }

    public String getSearch() {
        return search.get();
    }

    public void setSearch(String search) {
        this.search.set(search);
    }

    public SimpleStringProperty searchProperty() {
        return search;
    }

    public boolean addSearchUpdate(BDSimpleSearchUpdate simpleSearchUpdate) {
        return searchUpdates.add(simpleSearchUpdate);
    }

    public ReadOnlyListProperty<BDSimpleSearchUpdate> searchUpdatesProperty() {
        return searchUpdates;
    }

    public List<BDSimpleSearchUpdate> getSearchUpdates() {
        return searchUpdates.get();
    }

    public interface BDSimpleSearchUpdate {
        boolean searchUpdate(String search);
    }
}
