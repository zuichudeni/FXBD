package com.xx.UI.complex.splitPane;

import com.xx.UI.basic.BDButton;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.Util;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;

import static com.xx.UI.complex.splitPane.BDTabItem.dragTab;
import static com.xx.UI.complex.splitPane.BDTabItem.tempRunnable;

public class BDTabSkin extends BDSkin<BDTab> {
    private final HBox root;
    private final Text title;
    private final BDButton closeButton;
    private final PseudoClass CLOSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("show");
    BDTabItem tempItem;

    protected BDTabSkin(BDTab bdTab) {
        this.root = new HBox();
        this.title = new Text();
        this.closeButton = new BDButton();
        super(bdTab);
    }

    @Override
    public void initEvent() {
        mapping.addEventHandler(root, MouseEvent.MOUSE_PRESSED, _ -> control.show())
                .addEventHandler(closeButton, MouseEvent.MOUSE_CLICKED, e -> {
                    if (e.isAltDown()) {
                        List<BDTab> tabs = control.splitItem.get().getTabs().stream().toList();
                        tabs.forEach(tab -> {
                            if (tab != control) tab.close();
                        });
                    } else control.close();
                }).addEventFilter(control, MouseDragEvent.DRAG_DETECTED, event -> {
                    dragTab = control;
                    control.startDragAndDrop(TransferMode.MOVE);
                    control.startFullDrag();
                    tempItem = control.splitItem.get();
                    control.handleDragDetected(event);
                }).addEventFilter(control, DragEvent.DRAG_DONE, event -> {
                    if (tempItem != null)
                        tempItem.check();
                    if (dragTab.splitItem.get() == null) {
                        Stage stage = new Stage();
                        BDTabPane splitPane = new BDTabPane(new BDTabItem(dragTab));
                        splitPane.getMapping()
                                .addListener(splitPane.tabsCount, (_, _, nv) -> {
                                    if (nv.intValue() == 0) {
                                        if (dragTab != null)
                                            tempRunnable = () -> {
                                                if (splitPane.tabsCount.get() == 0) stage.close();
                                            };
                                        else stage.close();
                                    }
                                }).addListener(stage.showingProperty(), (_, _, nv) -> {
                                    if (!nv)
                                        splitPane.getMapping().dispose();
                                });
                        stage.setScene(new Scene(splitPane));
                        stage.show();
                        dragTab.show();
                    }
                    dragTab = null;
                    if (tempRunnable != null)
                        tempRunnable.run();
                    tempRunnable = null;
                    tempItem = null;
                });
    }

    @Override
    public void initProperty() {
        mapping.bindProperty(title.textProperty(), control.titleProperty())
                .binding(closeButton.disableProperty(), control.closableProperty().not())
                .addListener(() -> closeButton.pseudoClassStateChanged(CLOSED_PSEUDO_CLASS,
                                control.isShow() || control.isHover()), true,
                        control.showProperty(), control.hoverProperty())
                .addListener(() -> {
                    if (control.getGraphic() == null)
                        root.getChildren().setAll(title, closeButton);
                    else
                        root.getChildren().setAll(control.getGraphic(), title, closeButton);
                }, true, control.graphicProperty());
    }

    @Override
    public void initUI() {
        control.getStyleClass().add("bd-tab");
        root.getChildren().addAll(title, closeButton);
        root.setSpacing(5);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(5, 10, 5, 10));
        getChildren().setAll(root);
        title.getStyleClass().add("bd-tab-title");

        Tooltip tooltip = new Tooltip();
        tooltip.setText("关闭。按 Alt 并点击以关闭其他。(Ctrl+F4)");
        closeButton.setTooltip(tooltip);

        closeButton.setSelectable(false);
        closeButton.getStyleClass().add("bd-tab-close-button");
        closeButton.getStyleClass().add("circle");
        closeButton.setPadding(new Insets(0, 0, 0, 0));
        closeButton.setGraphic(Util.getImageView(20, BDIcon.CLOSE_SMALL));
    }

}