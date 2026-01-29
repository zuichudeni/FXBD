package com.xx.UI.complex.stage;

import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.Util;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

class BDSideBarSkin extends BDSkin<BDSidebar> {
    final Pane fronFrontPane;
    final Pane frontRoot;
    final Pane frontPane;
    final Pane pane;
    final Node spring;
    final Pane afterPane;

    protected BDSideBarSkin(BDSidebar bdSideBar) {
        if (bdSideBar.direction == BDDirection.LEFT || bdSideBar.direction == BDDirection.RIGHT) {
            fronFrontPane = new VBox();
            frontRoot = new VBox();
            frontPane = new VBox();
            pane = new VBox();
            spring = Util.getVBoxSpring();
            afterPane = new VBox();
        } else {
            fronFrontPane = new HBox();
            frontRoot = new HBox();
            frontPane = new HBox();
            pane = new HBox();
            spring = Util.getHBoxSpring();
            afterPane = new HBox();
        }
        super(bdSideBar);
        control.fronFrontPane = fronFrontPane;
        control.frontPane = frontPane;
        control.afterPane = afterPane;
    }

    @Override
    public void initEvent() {
        super.initEvent();
    }

    @Override
    public void initProperty() {
        mapping
                .addListener(() -> {
                            fronFrontPane.getChildren().setAll(control.fronFrontSideBarItems);
                            if (control.separator != null && !frontPane.getChildren().isEmpty() && !fronFrontPane.getChildren().isEmpty() && frontRoot.getChildren().size() < 3)
                                frontRoot.getChildren().add(1, control.separator);
                            else if ((fronFrontPane.getChildren().isEmpty() || frontPane.getChildren().isEmpty()) && control.separator != null)
                                frontRoot.getChildren().remove(control.separator);
                        }, true,
                        (ObservableList<?>) control.fronFrontSideBarItems)
                .addListener(() -> {
                            frontPane.getChildren().setAll(control.fronSideBarItems);
                            if (control.separator != null && !frontPane.getChildren().isEmpty() && !fronFrontPane.getChildren().isEmpty() && frontRoot.getChildren().size() < 3)
                                frontRoot.getChildren().add(1, control.separator);
                            else if ((fronFrontPane.getChildren().isEmpty() || frontPane.getChildren().isEmpty()) && control.separator != null)
                                frontRoot.getChildren().remove(control.separator);
                        }, true,
                        (ObservableList<?>) control.fronSideBarItems)
                .addListener(() -> afterPane.getChildren().setAll(control.afterSideBarItems), true, (ObservableList<?>) control.afterSideBarItems);
    }

    @Override
    public void initUI() {
        frontRoot.getChildren().addAll(fronFrontPane, frontPane);
        pane.getChildren().addAll(frontRoot, spring, afterPane);
        pane.getStyleClass().add("bd-side-bar-pane");
        pane.getStyleClass().add(control.direction.name().toLowerCase());
        frontRoot.getStyleClass().add("front-root");
        frontPane.getStyleClass().add("front-pane");
        fronFrontPane.getStyleClass().add("front-pane");
        afterPane.getStyleClass().add("after-pane");
        if (control.separator != null) {
            control.separator.setOrientation(Orientation.HORIZONTAL);
            control.separator.getStyleClass().add("bd-side-bar-separator");
        }
        pane.getStyleClass().add((control.direction == BDDirection.LEFT || control.direction == BDDirection.RIGHT) ? "vertical" : "horizontal");
        control.itemBack.getStyleClass().add("item-back");
        getChildren().setAll(pane);
        control.setMinSize(0, 0);
        frontRoot.setMinSize(0, 0);
        afterPane.setMinSize(0, 0);
        pane.setMinSize(0, 0);
    }
}
