package com.xx.UI.complex.stage.sideBarItem;

import com.xx.UI.complex.stage.BDDialog;
import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;

import java.util.Date;

public class BDMessage<T> extends BDControl {
    BDMessageItem messageItem;
    private final BDDialog.BD_DIALOG_TYPE type;
    private final String title;
    private final Date date;
    private final T content;
    private final BDMessageInit<T> messageInit;
    private final SimpleBooleanProperty uncheck = new SimpleBooleanProperty(true);

    public BDMessage(BDDialog.BD_DIALOG_TYPE type, String title, Date date, T content) {
        this.type = type;
        this.title = title;
        this.date = date;
        this.content = content;
        this.messageInit = new BDMessageInit<>() {
            @Override
            public Node Init(BDMessage<T> message) {
                return BDMessageInit.super.Init(message);
            }
        };
    }

    public BDMessage(BDDialog.BD_DIALOG_TYPE type, String title, Date date, T content, BDMessageInit<T> messageInit) {
        this.type = type;
        this.title = title;
        this.date = date;
        this.content = content;
        this.messageInit = messageInit;
    }

    public T getContent() {
        return content;
    }

    public Date getDate() {
        return date;
    }

    public BDMessageInit<T> getMessageInit() {
        return messageInit;
    }

    public String getTitle() {
        return title;
    }

    public BDDialog.BD_DIALOG_TYPE getType() {
        return type;
    }

    public boolean getUncheck() {
        return uncheck.get();
    }

    public SimpleBooleanProperty uncheckProperty() {
        return uncheck;
    }
    public void setUncheck(boolean uncheck){
        this.uncheck.set(uncheck);
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDMessageSkin<>(this);
    }
}
