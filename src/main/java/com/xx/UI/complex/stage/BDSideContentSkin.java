package com.xx.UI.complex.stage;

import com.xx.UI.basic.button.BDButton;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.Util;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class BDSideContentSkin extends BDSkin<BDSideContent> {
    private final AnchorPane header;
    private final HBox prePane;
    private final Text title;
    private final HBox postPane;
    private final PseudoClass SHOW_CLASS;
    private final StackPane content;
    private final VBox root;
    private final BDButton hide;
    private BooleanProperty isFocusOn;

    protected BDSideContentSkin(BDSideContent bdSideContent) {
        this.header = bdSideContent.headerPane;
        this.prePane = bdSideContent.leadingPane;
        this.title = new Text();
        this.postPane = bdSideContent.trailingPane;
        this.SHOW_CLASS = PseudoClass.getPseudoClass("show");
        this.content = bdSideContent.contentPane;
        this.root = bdSideContent.rootPane;
        hide = new BDButton();
        super(bdSideContent);
    }

    @Override
    public void initEvent() {
        mapping.addEventHandler(hide, ActionEvent.ACTION, _ -> control.item.setSelected(false))
                .addEventFilter(root, MouseEvent.MOUSE_ENTERED, _ -> control.setShow(true))
                .addEventFilter(root, MouseEvent.MOUSE_EXITED, _ -> control.setShow(false));
    }

    @Override
    public void initProperty() {
        mapping.bindProperty(title.textProperty(), control.titleProperty())
                .addListener(() -> {
                            if (isFocusOn.get())
                                postPane.pseudoClassStateChanged(SHOW_CLASS, true);
                            else {
                                postPane.pseudoClassStateChanged(SHOW_CLASS,control.showProperty().get() || control.dock.isVisible());
                            }
                        }, true,
                        control.showProperty(), isFocusOn,control.dock.visibleProperty())
                .addListener(() -> {
                    content.getChildren().clear();
                    if (control.getContent() != null)
                        content.getChildren().add(control.getContent());
                }, true, control.contentProperty())
                .addListener(() -> {
                    prePane.getChildren().add(title);
                    if (!control.preNodeItemsProperty().isEmpty())
                        prePane.getChildren().addAll(control.preNodeItemsProperty().get());
                    if (!control.afterNodeItemsProperty().isEmpty())
                        postPane.getChildren().addAll(control.afterNodeItemsProperty().get());
                }, true, (ObservableList<?>) control.preNodeItemsProperty(), control.afterNodeItemsProperty());
    }

    @Override
    public void initUI() {
        isFocusOn = Util.focusWithIn(control, mapping);
        control.addAfterNodeItem(control.dock);
        control.dock.setDefaultGraphic(Util.getImageView(15, BDIcon.OPEN_IN_TOOL_WINDOW));
        control.dock.setSelectable(false);
        control.dock.getStyleClass().addAll("icon", "dock");
        header.getChildren().addAll(prePane, postPane);
        control.addAfterNodeItem(hide);
        hide.setDefaultGraphic(Util.getImageView(15, BDIcon.HIDE));
        hide.setSelectable(false);
        hide.getStyleClass().addAll("icon", "hide");
        hide.setTooltip(new Tooltip("隐藏"));
        AnchorPane.setLeftAnchor(prePane, .0);
        AnchorPane.setTopAnchor(prePane, .0);
        AnchorPane.setBottomAnchor(prePane, .0);
        AnchorPane.setRightAnchor(postPane, .0);
        AnchorPane.setTopAnchor(postPane, .0);
        AnchorPane.setBottomAnchor(postPane, .0);
        header.getStyleClass().add("bd-side-content-header");
        prePane.getStyleClass().add("bd-side-content-header-pre");
        title.getStyleClass().add("bd-side-content-title");
        postPane.getStyleClass().add("bd-side-content-header-post");
        content.getStyleClass().add("bd-side-content-content");
        VBox.setVgrow(content, Priority.ALWAYS);
        root.getChildren().setAll(header, content);
        root.getStyleClass().add("bd-side-content");
        root.setMinSize(0,0);
        getChildren().add(root);
    }
}
