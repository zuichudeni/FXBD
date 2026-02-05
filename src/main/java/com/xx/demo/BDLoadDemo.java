package com.xx.demo;

import com.dlsc.fxmlkit.fxml.FxmlKit;
import com.xx.UI.basic.button.BDButton;
import com.xx.UI.basic.progressBar.BDLoadPane;
import com.xx.UI.basic.progressBar.BDProgressContent;
import com.xx.UI.basic.progressBar.BDTask;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.BDMapping;
import com.xx.UI.util.Util;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static com.xx.demo.BDProgressDemo.getSimpleTask;

public class BDLoadDemo extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FxmlKit.enableDevelopmentMode();
        FxmlKit.setApplicationUserAgentStylesheet(Util.getResourceUrl("/css/cupertino-light.css"));

        BDMapping mapping = new BDMapping();
        VBox progress = new VBox(10);
        BDLoadPane loadPane = new BDLoadPane(progress);
        BDProgressContent progressContent = new BDProgressContent();
        progressContent.setPausable(true);
        HBox buttonGroup = new HBox();
        BDButton insertTask = new BDButton("插入任务", Util.getImageView(20, BDIcon.ADD_DARK));
        insertTask.setLoadGraphic(Util.getImageView(20, BDIcon.LOADER_A));
        insertTask.setSelectable(false);

        BDMapping tempMapping = new BDMapping();
        mapping.bindProperty(loadPane.loadProperty(),insertTask.loadProperty())
                .addEventHandler(insertTask, ActionEvent.ACTION, _ -> {
            BDTask<String> task = getSimpleTask();
            insertTask.setLoad(true);
            tempMapping.addEventHandler(task, WorkerStateEvent.WORKER_STATE_SUCCEEDED, _ ->
                    tempMapping.dispose()).addDisposeEvent(() -> {
                if (!task.getState().equals(Worker.State.SUCCEEDED)) task.cancel();
                insertTask.setLoad(false);
            });
            task.setOnSucceeded(_ -> insertTask.setLoad(false));
            progressContent.setTask(task);
        });
        BDButton cleanTask = new BDButton("移除任务", Util.getImageView(20, BDIcon.CLEAR_CASH_DARK));
        cleanTask.setSelectable(false);
        mapping.addEventHandler(cleanTask, ActionEvent.ACTION, _ -> {
            progressContent.setTask(null);
            tempMapping.dispose();
        });
        buttonGroup.getChildren().addAll(insertTask, Util.getHBoxSpring(), cleanTask);
        progress.getChildren().addAll(progressContent, Util.getVBoxSpring(), buttonGroup);
        progress.setPadding(new Insets(20));



        Scene scene = new Scene(loadPane, 600, 400);
        stage.setScene(scene);
        stage.show();
        stage.setAlwaysOnTop(true);
    }
}
