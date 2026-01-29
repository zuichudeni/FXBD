package com.xx.UI.complex.stage;

import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.Util;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xx.UI.complex.stage.BDContent.DRAG_ITEM;

public class BDContentSkin extends BDSkin<BDContent> {

    private BDSidebar tempSidebar;

    protected BDContentSkin(BDContent bdContent) {
        super(bdContent);
    }

    @Override
    public void initUI() {
        getChildren().setAll(control.borderPane);
        HBox.setHgrow(control.verticalSplitPane, Priority.ALWAYS);
        AnchorPane.setTopAnchor(control.verticalSplitPane, .0);
        AnchorPane.setRightAnchor(control.verticalSplitPane, .0);
        AnchorPane.setBottomAnchor(control.verticalSplitPane, .0);
        AnchorPane.setLeftAnchor(control.verticalSplitPane, .0);
        control.verticalSplitPane.setOrientation(Orientation.VERTICAL);
        control.leftSplitPane.setOrientation(Orientation.VERTICAL);
        control.rightSplitPane.setOrientation(Orientation.VERTICAL);
        control.borderPane.getStyleClass().add("bd-content-pane");
        control.verticalSplitPane.getStyleClass().add("bd-content-vertical-pane");
        control.horizontalSplitPane.getStyleClass().add("bd-content-horizontal-pane");
        control.leftSplitPane.getStyleClass().add("bd-content-left-pane");
        control.rightSplitPane.getStyleClass().add("bd-content-right-pane");
        control.bottomSplitPane.getStyleClass().add("bd-content-bottom-pane");
        control.centerPane.getStyleClass().add("bd-content-center-pane");
        control.splitBack.getStyleClass().add("bd-split-back");
        control.splitBack.setMouseTransparent(true);
        control.tooltip.setMouseTransparent(true);
        control.tooltip.getStyleClass().add("bd-content-tooltip");
        control.text.getStyleClass().add("bd-content-tooltip-text");
        control.setMinSize(0, 0);
        control.horizontalRootPane.setMinSize(0, 0);
        control.hideToolTip();
    }

    @Override
    public void initProperty() {
        mapping.addListener(() -> {
            control.centerPane.getChildren().clear();
            if (control.getContent() != null)
                control.centerPane.getChildren().add(control.getContent());
        }, true, control.contentProperty());
    }

    @Override
    public void initEvent() {
        mapping.addEventFilter(control.borderPane, DragEvent.DRAG_OVER, event -> {
                    if (DRAG_ITEM == null || !control.acceptDragItem(DRAG_ITEM)) return;
                    event.consume();
                    event.acceptTransferModes(TransferMode.MOVE);
                    if (Util.searchEventTargetNode(event.getTarget(), BDSidebar.class) instanceof BDSidebar sidebar
                            && (sidebar.direction.equals(BDDirection.LEFT) || sidebar.direction.equals(BDDirection.RIGHT))) {
                        control.changeSplitBack(calculate(event, sidebar), event);
                    } else {
                        Bounds bounds = control.horizontalSplitPane.localToScene(control.horizontalSplitPane.getLayoutBounds());
                        control.refreshDivider();
                        if (!control.leftSideBar.isNone() && event.getSceneX() <= bounds.getMinX() + bounds.getWidth() * control.leftDivider)
                            control.changeSplitBack(calculate(event, control.leftSideBar.get()), event);
                        else if (!control.rightSideBar.isNone() && event.getSceneX() >= bounds.getMinX() + bounds.getWidth() * control.rightDivider)
                            control.changeSplitBack(calculate(event, control.rightSideBar.get()), event);
                        else {
                            control.hideAllItemBack();
                            control.dragHove(null, event.getSceneX(), event.getSceneY());
                        }
                    }
                })
                .addEventFilter(control.borderPane, DragEvent.DRAG_EXITED, _ -> {
                    if (DRAG_ITEM == null || !control.acceptExitItem(DRAG_ITEM)) return;
                    control.hideAllItemBack();
                    if (tempSidebar != null)
                        tempSidebar.removeItemNode(tempSidebar.itemBack);
                })
                .addEventFilter(control.borderPane, DragEvent.DRAG_DROPPED, event -> {
                    if (DRAG_ITEM == null || !control.acceptDragDropped(DRAG_ITEM)) return;
                    BDSidebar.BDDragData bdDragData = null;
                    if (Util.searchEventTargetNode(event.getTarget(), BDSidebar.class) instanceof BDSidebar sidebar && (sidebar.direction.equals(BDDirection.LEFT) || sidebar.direction.equals(BDDirection.RIGHT))) {
                        bdDragData = calculate(event, sidebar);
                    } else {
                        Bounds bounds = control.horizontalSplitPane.localToScene(control.horizontalSplitPane.getLayoutBounds());
                        control.refreshDivider();
                        if (!control.leftSideBar.isNone() && event.getSceneX() <= bounds.getMinX() + bounds.getWidth() * control.leftDivider)
                            bdDragData = calculate(event, control.leftSideBar.get());
                        else if (!control.rightSideBar.isNone() && event.getSceneX() >= bounds.getMinX() + bounds.getWidth() * control.rightDivider)
                            bdDragData = calculate(event, control.rightSideBar.get());
                    }
                    BDSidebar oldSidebar = DRAG_ITEM.tempSidebar == null ? DRAG_ITEM.sidebar.get() : DRAG_ITEM.tempSidebar;
                    if (bdDragData != null) {
                        BDSidebar newSideBar = null;
                        if (bdDragData.getDirection().equals(BDDirection.LEFT))
                            newSideBar = control.leftSideBar.get();
                        else if (bdDragData.getDirection().equals(BDDirection.RIGHT))
                            newSideBar = control.rightSideBar.get();
                        else if (bdDragData.getDirection().equals(BDDirection.BOTTOM)) {
                            if (bdDragData.getInSequence().equals(BDInSequence.FRONT))
                                newSideBar = control.leftSideBar.get();
                            else newSideBar = control.rightSideBar.get();
                        }
                        if (newSideBar == null)
                            throw new IllegalArgumentException("DRAG_ITEM的DIRECTION不能为：" + bdDragData.getDirection());
                        if (!bdDragData.getDirection().equals(DRAG_ITEM.getDirection())
                                || !DRAG_ITEM.getInSequence().equals(bdDragData.getInSequence()))
                            oldSidebar.cleanBDSideBarItem(DRAG_ITEM);

                        DRAG_ITEM.tempSidebar = newSideBar;
                        DRAG_ITEM.setDirection(bdDragData.getDirection());
                        DRAG_ITEM.setInSequence(bdDragData.getInSequence());
                        DRAG_ITEM.tempIndex = bdDragData.getItemBackIndex();
                    } else DRAG_ITEM.tempSidebar = null;
                    control.hideAllItemBack();
                })
                .addEventFilter(control.borderPane, KeyEvent.KEY_PRESSED, event -> {
                    Map<KeyCombination, List<BDSideBarItem>> map = new HashMap<>();
                    control.leftSideBar.applyIfNotNone(bar -> bar.getItems().forEach(item -> {
                        if (item.getShortcutKey() != null) {
                            KeyCombination key = KeyCombination.keyCombination(item.getShortcutKey());
                            if (map.containsKey(key))
                                map.get(key).add(item);
                            else map.put(key, new ArrayList<>(List.of(item)));
                        }
                    }));
                    control.rightSideBar.applyIfNotNone(bar -> bar.getItems().forEach(item -> {
                        if (item.getShortcutKey() != null) {
                            KeyCombination key = KeyCombination.keyCombination(item.getShortcutKey());
                            if (map.containsKey(key))
                                map.get(key).add(item);
                            else map.put(key, new ArrayList<>(List.of(item)));
                        }
                    }));
                    map.forEach((k, v) -> {
                        if (k.match(event)) {
                            v.forEach(item -> item.setSelected(!item.isSelected()));
                        }
                    });
                });
    }

    private BDSidebar.BDDragData calculate(DragEvent event, BDSidebar sidebar) {
        if (tempSidebar != sidebar) {
            if (tempSidebar != null)
                tempSidebar.removeItemNode(tempSidebar.itemBack);
            tempSidebar = sidebar;
        }
        BDSidebar.BDDragData bdDragData = sidebar.calculateDragData(event.getSceneY());
        if (bdDragData == null) sidebar.hideItemBack();
        else {
            bdDragData.setItemBackIndex(sidebar.addItemNode(bdDragData.getDirection(), bdDragData.getInSequence(), bdDragData.getIndex(), sidebar.itemBack));
            sidebar.itemBack.setHeight(DRAG_ITEM.cachedHeight);
            sidebar.itemBack.setWidth(DRAG_ITEM.cachedWidth);
        }
        return bdDragData;
    }
}
