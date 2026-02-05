package com.xx.UI.basic.progressBar;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.Util;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;

public class BDLoadPane extends BDControl {
    private final SimpleBooleanProperty load = new SimpleBooleanProperty();
    private final SimpleObjectProperty<Node> loadNode = new SimpleObjectProperty<>(Util.getImageView(20, BDIcon.LOADER_A));
    private final SimpleStringProperty loadText = new SimpleStringProperty("加载中...");
    private final SimpleBooleanProperty loadAnimatable = new SimpleBooleanProperty(true);
    private static final String CLASS_NAME = "bd-load-pane";
    private final Node node;

    public BDLoadPane(Node node) {
        this.node = node;
        getStyleClass().add(CLASS_NAME);
    }

    public Node getNode() {
        return node;
    }

    public boolean isLoad() {
        return load.get();
    }

    public SimpleBooleanProperty loadProperty() {
        return load;
    }
    public void setLoad(boolean load){
        this.load.set(load);
    }
    public void setLoadNode(Node node){
        this.loadNode.set(node);
    }
    public Node getLoadNode() {
        return loadNode.get();
    }
    public SimpleObjectProperty<Node> loadNodeProperty() {
        return loadNode;
    }
    public void setLoadText(String text){
        this.loadText.set(text);
    }

    public String getLoadText() {
        return loadText.get();
    }

    public SimpleStringProperty loadTextProperty() {
        return loadText;
    }

    public void setLoadAnimatable(boolean loadAnimatable){
        this.loadAnimatable.set(loadAnimatable);
    }
    public boolean getLoadAnimatable() {
        return loadAnimatable.get();
    }

    public SimpleBooleanProperty loadAnimatableProperty() {
        return loadAnimatable;
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDLoadPaneSkin(this);
    }
}
