package com.xx.UI.complex.BDTabPane;

public interface BDTabItemImp {
    boolean canAdd();

    boolean addItem(BDTabDir dir, BDTab... item);

    boolean removeItem(BDTabItem item);

    boolean removeItemFromParent();

    default boolean acceptDrag(BDTab tab) {
        return true;
    }

    void check();
    BDTabItem initItem();
}
