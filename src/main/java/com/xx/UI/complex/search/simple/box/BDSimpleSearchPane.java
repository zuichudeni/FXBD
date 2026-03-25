package com.xx.UI.complex.search.simple.box;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

import java.util.List;

public class BDSimpleSearchPane<T> extends BDControl {
    protected final BDSimpleSearchBox<T> simpleSearchBox;
    private final SimpleObjectProperty<Node> content = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty showSearchBox = new SimpleBooleanProperty(false);
    public BDSimpleSearchPane(BDSimpleSearchBox<T> searchBox, Node content) {
        this.simpleSearchBox = searchBox;
        mapping.addChildren(searchBox.getMapping());
        setContent(content);
    }

    public Node getContent() {
        return content.get();
    }

    public SimpleObjectProperty<Node> contentProperty() {
        return content;
    }

    public BDSimpleSearchBox<T> getSimpleSearchBox() {
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
