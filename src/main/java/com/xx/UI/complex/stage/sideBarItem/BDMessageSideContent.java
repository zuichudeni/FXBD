package com.xx.UI.complex.stage.sideBarItem;

import com.xx.UI.complex.stage.BDSideContent;
import com.xx.UI.util.Util;
import javafx.beans.Observable;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

class BDMessageSideContent extends BDSideContent {


    private final Text clean;
    private final Text promptText = new Text();
    private final VBox content;
    private final VBox root;

    public BDMessageSideContent(String title) {
        setTitle(title);
        var text = new Text("时间线");
        clean = new Text("全部清除");
        content = new VBox();
        var scrollPane = new ScrollPane(content);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mapping.bindProperty(content.prefWidthProperty(), scrollPane.widthProperty());
        HBox header = new HBox(text, Util.getHBoxSpring(), clean);
        root = new VBox(header, scrollPane);
        root.getStyleClass().add("bd-message-item-root");
        header.getStyleClass().add("bd-message-item-header");
        scrollPane.getStyleClass().add("bd-message-item-scrollpane");
        content.getStyleClass().add("bd-message-item-content");
        text.getStyleClass().add("bd-message-item-title");
        clean.getStyleClass().add("bd-message-item-clean");
        promptText.getStyleClass().add("bd-message-item-prompt-text");
        setContent(new StackPane(root, promptText));
    }

    void setMessageItem(BDMessageItem messageItem) {
        mapping.addListener(() -> {
                    content.getChildren().clear();

                    if (messageItem.messages.isEmpty()){
                        root.setVisible(false);
                        promptText.setVisible(true);
                    }
                    else {
                        content.getChildren().addAll(messageItem.messages.stream().sorted(((o1, o2) -> o2.getDate().compareTo(o1.getDate()))).toList());
                        root.setVisible(true);
                        promptText.setVisible(false);
                    }
                }, true, (Observable) messageItem.messages)
                .addEventHandler(clean, MouseEvent.MOUSE_CLICKED, _ -> {
                    messageItem.messages.clear();
                    messageItem.setBadge(0);
                })
                .bindBidirectional(promptText.textProperty(),messageItem.promptTextProperty());
    }
}
