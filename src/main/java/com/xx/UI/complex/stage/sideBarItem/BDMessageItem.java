package com.xx.UI.complex.stage.sideBarItem;

import com.xx.UI.complex.stage.BDDirection;
import com.xx.UI.complex.stage.BDInSequence;
import com.xx.UI.complex.stage.BDSideBarItem;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.Util;
import javafx.beans.Observable;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.ImageView;

public class BDMessageItem extends BDSideBarItem {
    final SimpleListProperty<BDMessage<?>> messages = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final BDMessageSideContent sideContent;
    private final SimpleStringProperty promptText = new SimpleStringProperty("建议、事件，\n以及错误将出现在这里");

    public BDMessageItem(String name, ImageView defaultIcon, ImageView selectIcon, BDDirection direction, BDInSequence inSequence) {
        super(name, defaultIcon, selectIcon, direction, inSequence, sideContent = new BDMessageSideContent(name));
        init();
    }

    public BDMessageItem(String name, String shortcutKey, ImageView defaultIcon, ImageView selectIcon, BDDirection direction, BDInSequence inSequence) {
        super(name, shortcutKey, defaultIcon, selectIcon, direction, inSequence, sideContent = new BDMessageSideContent(name));
        init();
    }

    public BDMessageItem(BDDirection direction, BDInSequence inSequence) {
        this("通知", Util.getImageView(30, BDIcon.NOTIFICATIONS), Util.getImageView(30, BDIcon.NOTIFICATIONS), direction, inSequence);
    }

    private void init() {
        sideContent.setMessageItem(this);
        getMapping().addListener(selectedProperty(), _ -> {
            if (!isSelected())
                messages.forEach(message -> message.setUncheck(false));
            initBadge();
        }).addListener((Observable) messages, _ -> this.initBadge());
    }
    private void initBadge(){
        setBadge((int) messages.stream().filter(BDMessage::getUncheck).count());
    }
    public void pushMessage(BDMessage<?>... message) {
        messages.addAll(message);
        for (BDMessage<?> bdMessage : message)
            bdMessage.messageItem = this;
    }

    public void removeMessage(BDMessage<?> message) {
        messages.remove(message);
    }

    public void removeMessage(int index) {
        messages.remove(index);
    }

    public ObservableList<BDMessage<?>> getMessages() {
        return messages.get();
    }

    public SimpleListProperty<BDMessage<?>> messagesProperty() {
        return messages;
    }

    public String getPromptText() {
        return promptText.get();
    }

    public void setPromptText(String promptText) {
        this.promptText.set(promptText);
    }

    public SimpleStringProperty promptTextProperty() {
        return promptText;
    }
}