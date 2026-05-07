package com.xx.UI.complex.tree;

import com.xx.UI.complex.search.simple.box.BDSimpleSearchBox;
import com.xx.UI.complex.search.simple.box.BDSimpleSearchPane;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.xx.UI.complex.tree.BDTreeCellInitFactory.toMap;

public class BDTreeViewBySearch<T> extends BDSimpleSearchPane<T> {

    public BDTreeViewBySearch(BDTreeView<T> treeView) {
        super(new BDSimpleSearchBox<>() {
            @Override
            public LinkedHashMap<T, String> getSearchSource() {
                return toMap(treeView.getRoot(), treeView.getTreeCellInitFactory());
            }
        }, treeView);
        BDSimpleSearchBox<T> box = getSimpleSearchBox();
        treeView.searchBox.set(box);
        box.setChangeEvent(new BDSimpleSearchBox.SearchEvent() {
            @Override
            public void onSearchStart() {

            }

            @Override
            public void onSearchEnd() {
                BDSimpleSearchBox.SimpleSearchResult<T> searchResult = box.getSearchResult();
                if (searchResult != null)
                    BDTreeCellInitFactory.selectAndShow(treeView, searchResult.t());
            }

            @Override
            public void onPrevious(int ov, int nv) {
                BDSimpleSearchBox.SimpleSearchResult<T> searchResult = box.getSearchResult();
                if (searchResult != null)
                    BDTreeCellInitFactory.selectAndShow(treeView, searchResult.t());
            }

            @Override
            public void onNext(int ov, int nv) {
                BDSimpleSearchBox.SimpleSearchResult<T> searchResult = box.getSearchResult();
                if (searchResult != null)
                    BDTreeCellInitFactory.selectAndShow(treeView, searchResult.t());
            }
        });
        treeView.onRefresh = box::refreshSearch;
    }
}
