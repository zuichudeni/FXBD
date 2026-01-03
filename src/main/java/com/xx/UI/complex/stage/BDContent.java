package com.xx.UI.complex.stage;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.LazyValue;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/*
* BDStage 的内容区域
* */
public class BDContent extends BDControl {
    final StackPane centerPane = new StackPane();
    final SplitPane horizontalSplitPane = new SplitPane(centerPane);
    final SplitPane verticalSplitPane = new SplitPane(horizontalSplitPane);
    final HBox hBox = new HBox(verticalSplitPane);
    final VBox vBox = new VBox(hBox);
    final LazyValue<BDSidebar> topSideBar = new LazyValue<>(() -> { 
        BDSidebar topSidebar = new BDSidebar(this,Orientation.HORIZONTAL);
        vBox.getChildren().addFirst(topSidebar);
        return topSidebar;
    });
    final LazyValue<BDSidebar> leftSideBar = new LazyValue<>(() -> {
        BDSidebar leftSidebar = new BDSidebar(this,Orientation.VERTICAL);
        hBox.getChildren().addFirst(leftSidebar);
        return leftSidebar;
    });
    final LazyValue<BDSidebar> rightSideBar = new LazyValue<>(() -> {
        BDSidebar rightSidebar = new BDSidebar(this,Orientation.VERTICAL);
        hBox.getChildren().addLast(rightSidebar);
        return rightSidebar;
    });
    final LazyValue<BDSidebar> bottomSideBar = new LazyValue<>(() -> {
        BDSidebar bottomSidebar = new BDSidebar(this,Orientation.HORIZONTAL);
        vBox.getChildren().addLast(bottomSidebar);
        return bottomSidebar;
    });
    private final SimpleObjectProperty<Node> content = new SimpleObjectProperty<>();

    public Node getContent() {
        return content.get();
    }

    public SimpleObjectProperty<Node> contentProperty() {
        return content;
    }

    public void setContent(Node content) {
        this.content.set(content);
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDContentSkin(this);
    }
}
