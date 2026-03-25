package com.xx.UI.complex.search;

import com.xx.UI.complex.textArea.content.BDTextAreaContent;
import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.beans.property.*;
import javafx.scene.Node;

public abstract class BDSearchPane extends BDControl {
    protected final BDSearchBox searchBox;
    final BDSearchResource bdSearchResource;
    private final SimpleObjectProperty<Node> content = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty showSearchBox = new SimpleBooleanProperty(false);

    public BDSearchPane(Node content) {
        this.searchBox = new BDSearchBox(this);
        setContent(content);
        bdSearchResource = initialResource(this);
        mapping.bindBidirectional(searchBox.searchBlockIndex, bdSearchResource.resultIndexProperty());
    }
    public BDSearchPane(BDSearchBox searchBox, Node content) {
        this.searchBox = searchBox;
        setContent(content);
        bdSearchResource = initialResource(this);
        mapping.bindBidirectional(searchBox.searchBlockIndex, bdSearchResource.resultIndexProperty());
    }

    public Node getContent() {
        return content.get();
    }

    public void setContent(Node content) {
        this.content.set(content);
    }

    public ReadOnlyObjectProperty<Node> contentProperty() {
        return content;
    }

    public boolean isShowSearchBox() {
        return showSearchBox.get();
    }

    public void setShowSearchBox(boolean showSearchBox) {
        this.showSearchBox.set(showSearchBox);
    }

    public ReadOnlyBooleanProperty showSearchBoxProperty() {
        return showSearchBox;
    }

    public boolean isSearchSelect() {
        return searchBox.isSearchSelected();
    }

    public void setSearchSelect(boolean searchSelect) {
        searchBox.setSearchSelected(searchSelect);
    }

    public BooleanProperty searchSelectProperty() {
        return searchBox.searchSelectedProperty();
    }

    public void setContentText(String text) {
        searchBox.triggerSearchImmediate(text);
    }

    public void setContentText(String text, long delayMillis) {
        searchBox.triggerSearch(text, delayMillis);
    }

    public void refresh() {
        if (isShowSearchBox())

            if (isShowSearchBox()) {
                if (searchBox.isSearchSelected()) {
                    searchBox.triggerSearchImmediate(bdSearchResource.getSelectedResource());
                }
                else searchBox.triggerSearchImmediate(bdSearchResource.getResource());
            }
        else searchBox.clearSearch();
    }
    protected abstract void replace(BDTextAreaContent.Point start, BDTextAreaContent.Point end, String text);
    /*
    * @param originallyText 原文本
    * @param laterText 被替换后的文本
    * @param replaceText 替换文本
    * */
    protected abstract ReplaceAll replaceAll();

    protected abstract BDSearchResource initialResource(BDSearchPane pane);

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDSearchPaneSkin(this);
    }
}
