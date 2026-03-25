package com.xx.UI.complex.search;

import com.xx.UI.ui.BDSkin;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class BDSearchPaneSkin extends BDSkin<BDSearchPane> {
    private StackPane contentPane;
    private VBox root;

    protected BDSearchPaneSkin(BDSearchPane bdSearchPane) {
        super(bdSearchPane);
    }

    @Override
    public void initEvent() {
        KeyCombination search = KeyCombination.keyCombination("Ctrl+F");
        KeyCombination replace = KeyCombination.keyCombination("Ctrl+R");
        KeyCombination close = KeyCombination.keyCombination("Esc");
        mapping.addEventFilter(control, KeyEvent.KEY_PRESSED, event -> {
                    if (search.match(event)) {
                        control.setShowSearchBox(true);
                        control.searchBox.setRetract(true);
                        control.searchBox.requestFocus();
                        control.searchBox.applyCss();
                        String selectedResource = control.bdSearchResource.getSelectedResource();
                        if (selectedResource!= null && !selectedResource.isEmpty())
                            control.searchBox.setSearchText(selectedResource);
                    }
                    if (replace.match(event)){
                        control.setShowSearchBox(true);
                        control.searchBox.setRetract(false);
                        control.searchBox.requestFocus();
                        String selectedResource = control.bdSearchResource.getSelectedResource();
                        if (selectedResource!= null && !selectedResource.isEmpty())
                            control.searchBox.setSearchText(selectedResource);
                    }
                    if (close.match(event)){
                        control.setShowSearchBox(false);
                        control.searchBox.setRetract(true);
                    }
                });
    }

    @Override
    public void initProperty() {
        mapping.addChildren(control.searchBox.getMapping())
                .addListener(() -> {
                    if (control.isShowSearchBox())
                        root.getChildren().addFirst(control.searchBox);
                    else root.getChildren().remove(control.searchBox);
                    control.searchBox.clearSearch();
                }, true, control.showSearchBoxProperty())
                .addListener(() -> {
                    if (control.getContent() != null)
                        contentPane.getChildren().setAll(control.getContent());
                    else contentPane.getChildren().clear();
                }, true, control.contentProperty())
                .addListener(control::refresh,true,
                        control.searchBox.searchTextProperty(),
                        control.searchBox.searchCaseProperty(),
                        control.searchBox.searchRegexProperty(),
                        control.searchSelectProperty(),
                        control.showSearchBoxProperty())
                .addListener(control.searchBox::refresh,true,control.searchBox.searchBlockCountProperty())
                .addListener(()-> control.bdSearchResource.updateResult(control.searchBox.getSearchBlockIndex(),control.searchBox.getSearchBlocks(),control.searchBox.getSearchResults()),
                        true,
                        control.searchBox.refresh);
    }

    @Override
    public void initUI() {
        contentPane = new StackPane();
        root = new VBox(contentPane);
        getChildren().add(root);
        VBox.setVgrow(contentPane, Priority.ALWAYS);
    }
}
