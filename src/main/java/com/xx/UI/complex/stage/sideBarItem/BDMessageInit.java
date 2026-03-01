package com.xx.UI.complex.stage.sideBarItem;

import javafx.scene.Node;
import javafx.scene.text.Text;

public interface BDMessageInit<T> {
    default Node Init(BDMessage<T> message) {
        Text text = new Text(message.getContent().toString());
        text.wrappingWidthProperty().bind(message.widthProperty().subtract(20));
        return text;
    }
}
