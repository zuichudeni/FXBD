package com.xx.UI.complex.stage;

import com.xx.UI.ui.BDIcon;
import com.xx.UI.ui.BDUI;
import com.xx.UI.util.BDMapping;
import com.xx.UI.util.Util;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class BDDialog implements BDUI {
    private final BDStageBuilder stageBuilder = new BDStageBuilder();
    private final Text headerText = new Text();
    private final SimpleObjectProperty<DIALOG_HEADER_DISPLAY> headerDisplay = new SimpleObjectProperty<>(DIALOG_HEADER_DISPLAY.TEXT_GRAPHIC);
    private final SimpleObjectProperty<Node> headerGraphic = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Node> content = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Node> expandContent = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty expand = new SimpleBooleanProperty(false);
    private final Text expandText = new Text();
    private final SimpleObjectProperty<expandAction> expandAction = new SimpleObjectProperty<>((text, expand) -> {
        if (expand) text.setText("隐藏详细信息");
        else text.setText("显示详细信息");
    });
    private final VBox root = new VBox();
    private final HBox header = new HBox();
    private final Separator separator = new Separator();
    private final VBox contentRoot = new VBox();
    private final HBox actionBar = new HBox();
    private final HBox pre = new HBox();
    private final HBox center = new HBox();
    private final HBox after = new HBox();
    private final BDMapping mapping = new BDMapping();
    private final SimpleObjectProperty<BD_DIALOG_TYPE> dialogType = new SimpleObjectProperty<>(BD_DIALOG_TYPE.INFORMATION);
    private boolean headerGraphicInit = false;

    public BDDialog() {
        initUI();
        initEvent();
        initProperty();
    }

    public BDDialog setHeader(BDHeaderBarBuilder headerBarBuilder) {
        stageBuilder.setHeaderBar(headerBarBuilder);
        return this;
    }

    public DIALOG_HEADER_DISPLAY getHeaderDisplay() {
        return headerDisplay.get();
    }

    public BDDialog setHeaderDisplay(DIALOG_HEADER_DISPLAY headerDisplay) {
        this.headerDisplay.set(headerDisplay);
        return this;
    }

    public SimpleObjectProperty<DIALOG_HEADER_DISPLAY> headerDisplayProperty() {
        return headerDisplay;
    }

    public Node getHeaderGraphic() {
        return headerGraphic.get();
    }

    public BDDialog setHeaderGraphic(Node headerGraphic) {
        this.headerGraphic.set(headerGraphic);
        this.headerGraphicInit = true;
        return this;
    }

    public SimpleObjectProperty<Node> headerGraphicProperty() {
        return headerGraphic;
    }

    public Node getContent() {
        return content.get();
    }

    public BDDialog setContent(Node content) {
        this.content.set(content);
        return this;
    }

    public SimpleObjectProperty<Node> contentProperty() {
        return content;
    }

    public Node getExpandContent() {
        return expandContent.get();
    }

    public BDDialog setExpandContent(Node expandContent) {
        this.expandContent.set(expandContent);
        return this;
    }

    public SimpleObjectProperty<Node> expandContentProperty() {
        return expandContent;
    }

    public boolean isExpand() {
        return expand.get();
    }

    public BDDialog setExpand(boolean expand) {
        this.expand.set(expand);
        return this;
    }

    public BDDialog setExpand(expandAction expandAction) {
        this.expandAction.set(expandAction);
        return this;
    }

    public SimpleBooleanProperty expandProperty() {
        return expand;
    }

    public expandAction getExpandAction() {
        return expandAction.get();
    }

    public SimpleObjectProperty<expandAction> expandActionProperty() {
        return expandAction;
    }

    public StringProperty headerTextProperty() {
        return headerText.textProperty();
    }

    public String getHeaderText() {
        return headerText.getText();
    }

    public BDDialog setHeaderText(String headerText) {
        this.headerText.setText(headerText);
        return this;
    }

    public BD_DIALOG_TYPE getDialogType() {
        return dialogType.get();
    }

    public SimpleObjectProperty<BD_DIALOG_TYPE> dialogTypeProperty() {
        return dialogType;
    }

    public BDDialog setDialogType(BD_DIALOG_TYPE dialogType) {
        this.dialogType.set(dialogType);
        return this;
    }

    public BDDialog addPreActionNode(Node... node) {
        pre.getChildren().addAll(node);
        return this;
    }

    public ObservableList<Node> preActionChildren() {
        return pre.getChildren();
    }

    public BDDialog addCenterActionNode(Node... node) {
        center.getChildren().addAll(node);
        return this;
    }

    public ObservableList<Node> centerActionChildren() {
        return center.getChildren();
    }

    public BDDialog addAfterActionNode(Node... node) {
        after.getChildren().addAll(node);
        return this;
    }

    public ObservableList<Node> afterActionChildren() {
        return after.getChildren();
    }

    @Override
    public void initUI() {
        root.getChildren().addAll(header, contentRoot, actionBar);
        root.getStyleClass().add("bd-dialog-root");
        header.getStyleClass().add("bd-dialog-header");
        headerText.getStyleClass().add("bd-dialog-header-text");
        VBox.setVgrow(contentRoot, Priority.ALWAYS);
        separator.setPrefHeight(1);
        separator.getStyleClass().add("bd-dialog-separator");
        separator.setOrientation(Orientation.HORIZONTAL);
        contentRoot.getStyleClass().add("bd-dialog-content-root");
        actionBar.getChildren().addAll(pre, Util.getHBoxSpring(), center, Util.getHBoxSpring(), after);
        pre.getChildren().add(expandText);
        actionBar.getStyleClass().add("bd-dialog-action-bar");
        pre.getStyleClass().add("bd-dialog-action-bar-pre");
        center.getStyleClass().add("bd-dialog-action-bar-center");
        after.getStyleClass().add("bd-dialog-action-bar-after");
        expandText.getStyleClass().add("bd-dialog-expand-text");
    }

    @Override
    public void initProperty() {
        mapping.addListener(() -> {
                    if (headerText.getText() != null && !headerText.getText().isEmpty()) {
                        if (headerGraphic.get() != null) {
                            if (headerDisplay.get().equals(DIALOG_HEADER_DISPLAY.TEXT_GRAPHIC))
                                header.getChildren().setAll(headerText, Util.getHBoxSpring(), headerGraphic.get());
                            else header.getChildren().setAll(headerGraphic.get(), Util.getHBoxSpring(), headerText);
                        } else header.getChildren().setAll(headerText);
                    } else if (headerGraphic.get() != null) header.getChildren().setAll(headerGraphic.get());
                    else header.getChildren().clear();
                    refreshRoot();
                }, true, headerText.textProperty(), headerGraphic, headerDisplay)
                .addListener(() -> {
                    expandText.setVisible(expandContent.get() != null);
                    expandText.setManaged(expandContent.get() != null);
                    if (expand.get()) {
                        if (content.get() != null) {
                            if (expandContent.get() != null)
                                contentRoot.getChildren().setAll(content.get(), expandContent.get());
                            else contentRoot.getChildren().setAll(content.get());
                        } else if (expandContent.get() != null)
                            contentRoot.getChildren().setAll(expandContent.get());
                        else
                            contentRoot.getChildren().clear();
                    } else {
                        if (content.get() != null)
                            contentRoot.getChildren().setAll(content.get());
                        else contentRoot.getChildren().clear();
                    }
                    refreshRoot();
                }, true, content, expandContent, expand)
                .addListener(() -> expandAction.get().action(expandText, expand.get()), true, expand, expandAction)
                .addListener(() -> {
                    stageBuilder.addStyleClass(dialogType.get().style);
                    if (!headerGraphicInit) {
                        if (dialogType.get() != BD_DIALOG_TYPE.NONE)
                            headerGraphic.set(Util.getImageView(35, switch (dialogType.get()) {
                                case QUESTION -> BDIcon.QUESTION_DIALOG;
                                case INFORMATION -> BDIcon.INFORMATION_DIALOG;
                                case WARNING -> BDIcon.WARNING_DIALOG;
                                case ERROR -> BDIcon.ERROR_DIALOG;
                                case SUCCESS -> BDIcon.SUCCESS_DIALOG;
                                case NONE -> BDIcon.EMPTY;
                            }));
                        else headerGraphic.set(null);
                    }
                }, true, dialogType);
    }

    private void refreshRoot() {
        if (!header.getChildren().isEmpty()) {
            if (!contentRoot.getChildren().isEmpty())
                root.getChildren().setAll(header, separator, contentRoot, actionBar);
            else root.getChildren().setAll(header, actionBar);
        } else if (!contentRoot.getChildren().isEmpty())
            root.getChildren().setAll(contentRoot, actionBar);
        else
            root.getChildren().setAll(actionBar);
    }

    public BDDialog setSize(double width, double height) {
        stageBuilder.setSize(width, height);
        return this;
    }

    public Stage build() {
        stageBuilder.setContent(root);
        stageBuilder.addStyleClass("dialog");
        return stageBuilder.build();
    }

    public BDDialog addBDDialogStyleClass(String s) {
        stageBuilder.addStyleClass(s);
        return this;
    }

    @Override
    public void initEvent() {
        mapping.addEventHandler(expandText, MouseEvent.MOUSE_CLICKED, _ -> {
            expand.set(!expand.get());
        });
    }

    public BDMapping getMapping() {
        return mapping;
    }

    public enum BD_DIALOG_TYPE {
        QUESTION("question"),
        INFORMATION("information"),
        WARNING("warning"),
        ERROR("error"),
        SUCCESS("success"),
        NONE("none");
        private final String style;

        BD_DIALOG_TYPE(String style) {
            this.style = style;
        }

        public String getStyle() {
            return style;
        }
    }

    public enum DIALOG_HEADER_DISPLAY {
        TEXT_GRAPHIC, GRAPHIC_TEXT
    }

    public interface expandAction {
        void action(Text text, boolean expand);
    }
}
