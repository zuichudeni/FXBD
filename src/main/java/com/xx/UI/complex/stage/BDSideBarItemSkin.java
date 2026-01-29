package com.xx.UI.complex.stage;

import com.xx.UI.basic.button.BDButton;
import com.xx.UI.basic.button.BDButtonSkin;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public class BDSideBarItemSkin extends BDButtonSkin {

    public BDSideBarItemSkin(BDButton button) {
        super(button);
    }

    @Override
    public void initEvent() {
        super.initEvent();
        mapping.addEventFilter(control, MouseDragEvent.DRAG_DETECTED, event -> {
                    BDContent.DRAG_ITEM = (BDSideBarItem) control;
                    control.startDragAndDrop(TransferMode.MOVE);
                    control.startFullDrag();
                    ((BDSideBarItem) control).handleDragDetected(event);
                })
                .addEventFilter(control, DragEvent.DRAG_DONE, _ -> {
                    ((BDSideBarItem) control).dragEnd();
                    BDContent.DRAG_ITEM = null;
                })
                .addEventFilter(control, MouseEvent.MOUSE_ENTERED,_->{
                    ((BDSideBarItem) control).sidebar.get().content.hoverToolTipShow((BDSideBarItem) control);
                })
                .addEventFilter(control,MouseEvent.MOUSE_EXITED,_->{
                    ((BDSideBarItem) control).sidebar.get().content.hideToolTip();
                })
        ;
    }

    @Override
    public void initProperty() {
        super.initProperty();
        mapping.addListener(() -> {
            if (control.isSelected() && control instanceof BDSideBarItem item && item.sidebar.get() != null)
                item.sidebar.get().showSideBarItem(item);
            else if (!control.isSelected() && control instanceof BDSideBarItem item && item.sidebar.get() != null)
                item.sidebar.get().closeSideBarItem(item);
        }, true, control.selectedProperty());
    }

    @Override
    public void initUI() {

        control.getStyleClass().add("bd-side-bar-item");
    }
}
