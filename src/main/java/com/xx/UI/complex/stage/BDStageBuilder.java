package com.xx.UI.complex.stage;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class BDStageBuilder {
    private final VBox root = new VBox();
    private final Scene scene = new Scene(root);
    private final Stage stage = new Stage();
    {
        stage.initStyle(StageStyle.EXTENDED);
        stage.setScene(scene);
    }
    public BDStageBuilder buildHeaderBar(HeaderBar headerBar){
        root.getChildren().add(0, headerBar);
        return this;
    }
    public BDStageBuilder buildContent(Node content){
        VBox.setVgrow(content, Priority.ALWAYS);
        root.getChildren().add(content);
        return this;
    }
    public Stage build() {
        return stage;
    }
}
