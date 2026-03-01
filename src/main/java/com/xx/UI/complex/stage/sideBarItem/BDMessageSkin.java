package com.xx.UI.complex.stage.sideBarItem;

import atlantafx.base.controls.Notification;
import com.xx.UI.basic.button.BDButton;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.Util;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Duration;

import java.text.SimpleDateFormat;

public class BDMessageSkin<T> extends BDSkin<BDMessage<T>> {
    private final HBox header;
    private final Node icon;
    private final Text title;
    private final BDButton delete;
    private final Text date;
    private final VBox root;
    private final PseudoClass uncheck;

    protected BDMessageSkin(BDMessage<T> message) {
        this.header = new HBox();
        this.icon = Util.getImageView(25, switch (message.getType()) {
            case QUESTION -> BDIcon.QUESTION_DIALOG;
            case INFORMATION -> BDIcon.INFORMATION_DIALOG;
            case WARNING -> BDIcon.WARNING_DIALOG;
            case ERROR -> BDIcon.ERROR_DIALOG;
            case SUCCESS -> BDIcon.SUCCESS_DIALOG;
            case NONE -> BDIcon.QUESTION_MARK;
        });
        this.title = new Text(message.getTitle());
        this.delete = new BDButton();
        this.date = new Text();
        this.root = new VBox();
        this.uncheck = PseudoClass.getPseudoClass("uncheck");
        super(message);
    }

    @Override
    public void initEvent() {
        super.initEvent();
        Tooltip dateTooltip = new Tooltip();
        dateTooltip.setShowDelay(Duration.millis(500));
        dateTooltip.setText(new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(control.getDate()));
        mapping.addEventHandler(delete, ActionEvent.ACTION, _ -> {
            if (control.messageItem != null)
                control.messageItem.removeMessage(control);
        }).addEventHandler(date, MouseEvent.MOUSE_ENTERED, _ -> {
            Bounds bounds = date.localToScene(date.getLayoutBounds());
            Window window = date.getScene().getWindow();
            dateTooltip.show(date, bounds.getMaxX() + window.getX() , bounds.getMaxY() + window.getY() + 10);
        }).addEventHandler(date, MouseEvent.MOUSE_EXITED, _ -> dateTooltip.hide());
    }

    @Override
    public void initProperty() {
        super.initProperty();
        mapping.addListener(() -> root.pseudoClassStateChanged(uncheck, control.getUncheck()), true, control.uncheckProperty());
    }

    @Override
    public void initUI() {
        super.initUI();
        delete.setSelectable(false);
        delete.setDefaultGraphic(Util.getImageView(25, BDIcon.DELETE));
        delete.setTooltip(new Tooltip("删除"));
        date.setText(new SimpleDateFormat("HH:mm").format(control.getDate()));
        header.getChildren().addAll(icon, title, Util.getHBoxSpring(), delete, date);
        Node init = control.getMessageInit().Init(control);
        root.getChildren().addAll(header, init);

        header.getStyleClass().add("bd-message-header");
        icon.getStyleClass().add("bd-message-icon");
        title.getStyleClass().add("bd-message-title");
        delete.getStyleClass().add("bd-message-delete");
        date.getStyleClass().add("bd-message-date");
        init.getStyleClass().add("bd-message-content");
        root.getStyleClass().addAll("bd-message-root", control.getType().getStyle());
        getChildren().setAll(root);
    }
}
