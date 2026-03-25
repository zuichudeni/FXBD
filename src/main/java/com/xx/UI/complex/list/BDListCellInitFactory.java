package com.xx.UI.complex.list;

import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

public interface BDListCellInitFactory<T> {
    static <T> void selectAndShow(ListView<T> listView, T item) {
        if (!listView.getItems().contains(item)) return;
        int index = listView.getItems().indexOf(item);
        int[] range = getVisibleRowRange(listView);
        listView.getSelectionModel().clearAndSelect(index);
        if (range[0] != -1 && index >= range[0] && index < range[1]) return;
        if (range[0] == range[1]) {
            index -= 1;
        } else if (index < range[0])
            index -= 2;
        else if (index > range[1])
            index -= (range[1] - range[0]) - 3;
        else if (index == range[1])
            index -= (range[1] - range[0]) - 1;
        listView.scrollTo(index);

    }

    static int[] getVisibleRowRange(ListView<?> listView) {
        VirtualFlow<?> virtualFlow = (VirtualFlow<?>) listView.lookup(".virtual-flow");
        IndexedCell<?> firstVisibleCell = virtualFlow.getFirstVisibleCell();
        IndexedCell<?> lastVisibleCell = virtualFlow.getLastVisibleCell();
        return new int[]{firstVisibleCell.getIndex(), lastVisibleCell.getIndex()};
    }

    //      初始化图标。
    Node initGraphic(T t);

    //      初始化text信息。
    default String getInfo(T t) {
        return t.toString();
    }

    //    渲染
    void rendering(T t, Node graphic, Text text, Region pane);
}
