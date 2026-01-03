package com.xx.UI.complex.stage;

import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.Util;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

class BDSideBarSkin extends BDSkin<BDSidebar> {
    final Pane frontPane;
    final Pane pane;

    protected BDSideBarSkin(BDSidebar bdSideBar) {
        pane = bdSideBar.orientation == Orientation.VERTICAL ? new VBox() : new HBox();
        frontPane = bdSideBar.orientation == Orientation.VERTICAL ? new VBox() : new HBox();
        super(bdSideBar);
    }

    @Override
    public void initEvent() {
        super.initEvent();
    }

    @Override
    public void initProperty() {
        mapping.addListener(() -> {
            pane.getChildren().clear();
            frontPane.getChildren().clear();
            frontPane.getChildren().addAll(control.fronFrontSideBarItems);
            if (control.orientation == Orientation.VERTICAL) {
                Separator separator = new Separator();
                separator.setOrientation(Orientation.HORIZONTAL);
                separator.getStyleClass().add("bd-side-bar-separator");
                frontPane.getChildren().add(separator);
            }
            frontPane.getChildren().addAll(control.fronSideBarItems);
            pane.getChildren().add(frontPane);
            pane.getChildren().add(Util.getSpring());
            pane.getChildren().addAll(control.afterSideBarItems);
        }, true, (ObservableList<?>) control.fronSideBarItems, control.afterSideBarItems);
    }

    @Override
    public void initUI() {
        control.getStyleClass().add("bd-side-bar");
        pane.getStyleClass().add("bd-side-bar-pane");
        pane.getStyleClass().add(control.orientation == Orientation.VERTICAL ? "vertical":"horizontal");
        getChildren().setAll(pane);
    }
}
