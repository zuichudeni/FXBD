package com.xx.UI.complex.tree;

import com.xx.UI.complex.search.simple.box.BDSimpleSearchBox;
import com.xx.UI.complex.search.simple.box.BDSimpleSearchPane;

import java.util.HashMap;
import java.util.Map;

import static com.xx.UI.complex.tree.BDTreeCellInitFactory.toMap;

public class BDTreeViewBySearch<T> extends BDSimpleSearchPane<T> {

    public BDTreeViewBySearch(BDTreeView<T> treeView) {
        super(new BDSimpleSearchBox<T>() {
            @Override
            public Map<T, String> getSearchSource() {
                return toMap(treeView.getRoot(), treeView.getTreeCellInitFactory(), new HashMap<>());
            }
        }, treeView);
        BDSimpleSearchBox<T> box = getSimpleSearchBox();
        treeView.searchBox = box;
        treeView.getMapping().addListener(() -> treeView.searchRefresh.set(!treeView.searchRefresh.get()), true, box.searchResultIndexProperty(), box.searchBlockCountProperty());
    }
}
