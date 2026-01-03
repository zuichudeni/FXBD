package com.xx.UI.complex.stage;

import com.xx.UI.ui.BDSkin;
import javafx.scene.layout.Background;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class BDContentSkin extends BDSkin<BDContent> {

    protected BDContentSkin(BDContent bdContent) {
        super(bdContent);
    }

    @Override
    public void initUI() {
        VBox.setVgrow(control.hBox, Priority.ALWAYS);
        getChildren().setAll(control.vBox);
    }

    @Override
    public void initProperty() {
        super.initProperty();
    }

    @Override
    public void initEvent() {
    }
}
