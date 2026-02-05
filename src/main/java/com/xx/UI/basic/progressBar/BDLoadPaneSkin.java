package com.xx.UI.basic.progressBar;

import com.xx.UI.ui.BDSkin;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.sql.RowId;

public class BDLoadPaneSkin extends BDSkin<BDLoadPane> {
    private final Text text;
    private final VBox vbox;
    private final StackPane root;
    private RotateTransition rotateTransition;

    protected BDLoadPaneSkin(BDLoadPane bdLoadPane) {
        this.text = new Text();
        this.vbox = new VBox();
        this.root = new StackPane();
        super(bdLoadPane);
    }

    @Override
    public void initUI() {
        root.getStyleClass().add("bd-load-root");
        vbox.getStyleClass().add("bd-load-vbox");
        text.getStyleClass().add("bd-load-text");
        root.getChildren().addAll(control.getNode());
        getChildren().setAll(root);
    }

    @Override
    public void initProperty() {
        mapping.bindProperty(text.textProperty(), control.loadTextProperty())
                .addListener(() -> {
                    if (control.getLoadNode() != null)
                        vbox.getChildren().setAll(control.getLoadNode(), text);
                    else vbox.getChildren().setAll(text);
                }, true, control.loadNodeProperty())
                .addListener(() -> {
                    if (control.getLoadAnimatable() && control.isLoad()) startLoad();
                    else stopLoad();
                }, true, control.loadProperty(), control.loadAnimatableProperty());
    }

    private void startLoad() {
        root.getChildren().add(vbox);
        vbox.setVisible(true);
        vbox.setManaged(true);
        if (rotateTransition != null) rotateTransition.stop();
        if (control.getLoadNode() != null && control.getLoadAnimatable()) {
             rotateTransition = new RotateTransition(Duration.millis(900), control.getLoadNode());
            rotateTransition.setFromAngle(0);
            rotateTransition.setToAngle(-360);
            rotateTransition.setCycleCount(-1);
            rotateTransition.setAutoReverse(false);
            rotateTransition.setInterpolator(Interpolator.LINEAR);

            rotateTransition.play();
        }
    }

    private void stopLoad() {
        root.getChildren().remove(vbox);
        vbox.setVisible(false);
        vbox.setManaged(false);
        if (rotateTransition != null) {
            rotateTransition.stop();
            rotateTransition = null;
        }
    }

    @Override
    public void initEvent() {
        super.initEvent();
    }
}
