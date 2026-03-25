package com.xx.UI.complex.list;

import com.xx.UI.complex.search.simple.box.BDSimpleSearchBox;
import com.xx.UI.complex.search.simple.box.BDSimpleSearchPane;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BDListViewBySearch<T> extends BDSimpleSearchPane<T> {
    public BDListViewBySearch(BDListView<T> listView) {
        super(new BDSimpleSearchBox<>() {
            @Override
            public LinkedHashMap<T, String> getSearchSource() {
                LinkedHashMap<T,String> map = new LinkedHashMap<>();
                listView.getItems().forEach(item->{
                    map.put(item,listView.getListCellInitFactory().getInfo(item));
                });
                return map;
            }
        }, listView);
        BDSimpleSearchBox<T> box = getSimpleSearchBox();
        listView.searchBox.set(box);
        box.setChangeEvent(new BDSimpleSearchBox.SearchEvent() {
            @Override
            public void onSearchStart() {

            }

            @Override
            public void onSearchEnd() {
                BDSimpleSearchBox.SimpleSearchResult<T> searchResult = box.getSearchResult();
                if (searchResult != null)
                    BDListCellInitFactory.selectAndShow(listView, searchResult.t());

            }

            @Override
            public void onPrevious(int ov, int nv) {
                BDSimpleSearchBox.SimpleSearchResult<T> searchResult = box.getSearchResult();
                if (searchResult != null)
                    BDListCellInitFactory.selectAndShow(listView, searchResult.t());
            }

            @Override
            public void onNext(int ov, int nv) {
                BDSimpleSearchBox.SimpleSearchResult<T> searchResult = box.getSearchResult();
                if (searchResult != null)
                    BDListCellInitFactory.selectAndShow(listView, searchResult.t());

            }
        });
    }
}
