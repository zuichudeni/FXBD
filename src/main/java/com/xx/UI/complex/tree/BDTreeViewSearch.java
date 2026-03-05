package com.xx.UI.complex.tree;

import com.xx.UI.complex.search.BDSearchBox;
import com.xx.UI.complex.search.BDSearchPane;
import com.xx.UI.complex.search.BDSearchResource;
import com.xx.UI.complex.search.ReplaceAll;
import com.xx.UI.complex.textArea.content.BDTextAreaContent;
import javafx.beans.property.IntegerProperty;
import javafx.scene.Node;

import java.util.List;
import java.util.Map;

public class BDTreeViewSearch extends BDSearchPane {
    public BDTreeViewSearch(BDSearchBox searchBox, Node content) {
        super(searchBox, content);
    }

    @Override
    protected void replace(BDTextAreaContent.Point start, BDTextAreaContent.Point end, String text) {

    }

    @Override
    protected ReplaceAll replaceAll() {
        return null;
    }

    @Override
    protected BDSearchResource initialResource(BDSearchPane pane) {
        return new BDSearchResource(pane) {
            @Override
            protected String getResource() {
                return "";
            }

            @Override
            protected String getSelectedResource() {
                return "";
            }

            @Override
            protected int getSelectedStartParagraph() {
                return 0;
            }

            @Override
            protected int getSelectedOffset() {
                return 0;
            }

            @Override
            protected IntegerProperty resultIndexProperty() {
                return null;
            }

            @Override
            protected void updateResult(int searchBlockIndex, List<BDSearchBox.SearchBlock> searchBlocks, Map<Integer, List<BDSearchBox.SearchResult>> resultMap) {

            }
        };
    }
}
