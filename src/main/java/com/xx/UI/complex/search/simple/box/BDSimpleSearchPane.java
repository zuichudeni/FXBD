package com.xx.UI.complex.search.simple.box;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

public class BDSimpleSearchPane extends BDControl {
    protected final BDSimpleSearchBox simpleSearchBox;
    private final SimpleObjectProperty<Node> content = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty showSearchBox = new SimpleBooleanProperty(false);

    public BDSimpleSearchPane(Node content) {
        this.simpleSearchBox = new BDSimpleSearchBox(this);
        setContent(content);
    }

    public BDSimpleSearchPane(BDSimpleSearchBox searchBox, Node content) {
        this.simpleSearchBox = searchBox;
        setContent(content);
    }

    public Node getContent() {
        return content.get();
    }

    public SimpleObjectProperty<Node> contentProperty() {
        return content;
    }

    public BDSimpleSearchBox getSimpleSearchBox() {
        return simpleSearchBox;
    }

    public void setContent(Node node) {
        this.content.set(node);
    }

    public boolean isShowSearchBox() {
        return showSearchBox.get();
    }

    public void setShowSearchBox(boolean b) {
        this.showSearchBox.set(b);
    }

    public SimpleBooleanProperty showSearchBoxProperty() {
        return showSearchBox;
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDSimpleSearchPaneSkin(this);
    }

    public void refresh() {
    }
}
