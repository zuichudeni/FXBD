package com.xx.UI.complex.search.simple.box;

import com.xx.UI.ui.BDSkin;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class BDSimpleSearchPaneSkin extends BDSkin<BDSimpleSearchPane> {
    private StackPane contentPane;
    private VBox root;

    protected BDSimpleSearchPaneSkin(BDSimpleSearchPane bdSimpleSearchPane) {
        super(bdSimpleSearchPane);
    }

    @Override
    public void initUI() {
        contentPane = new StackPane();
        root = new VBox(contentPane);
        getChildren().add(root);
        VBox.setVgrow(contentPane, Priority.ALWAYS);
    }

    @Override
    public void initProperty() {
        mapping.addChildren(control.simpleSearchBox.getMapping())
                .addListener(() -> {
                    if (control.isShowSearchBox())
                        root.getChildren().addFirst(control.simpleSearchBox);
                    else root.getChildren().remove(control.simpleSearchBox);
                    control.simpleSearchBox.clearSearch();
                }, true, control.showSearchBoxProperty())
                .addListener(() -> {
                    if (control.getContent() != null)
                        contentPane.getChildren().setAll(control.getContent());
                    else contentPane.getChildren().clear();
                }, true, control.contentProperty());
    }

    @Override
    public void initEvent() {
        KeyCombination search = KeyCombination.keyCombination("Ctrl+F");
        KeyCombination replace = KeyCombination.keyCombination("Ctrl+R");
        KeyCombination close = KeyCombination.keyCombination("Esc");
        mapping.addEventFilter(control, KeyEvent.KEY_PRESSED, event -> {
                    if (search.match(event)) {
                        control.setShowSearchBox(true);
                        control.simpleSearchBox.requestFocus();
                        control.simpleSearchBox.applyCss();
                    }
                    if (replace.match(event)){
                        control.setShowSearchBox(true);
                        control.simpleSearchBox.requestFocus();
                    }
                    if (close.match(event)){
                        control.setShowSearchBox(false);
                    }
                });
    }
}
