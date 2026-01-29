package com.xx.demo;

import com.dlsc.fxmlkit.fxml.FxmlKit;
import com.xx.UI.basic.button.BDButton;
import com.xx.UI.basic.progressBar.BDProgressContent;
import com.xx.UI.basic.progressBar.BDTask;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.BDMapping;
import com.xx.UI.util.Util;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.TimeoutException;

public class BDProgressDemo extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FxmlKit.enableDevelopmentMode();
        FxmlKit.setApplicationUserAgentStylesheet(Util.getResourceUrl("/css/cupertino-light.css"));
        stage.setTitle("BDProgress Demo");

        BDMapping mapping = new BDMapping();

        VBox root = new VBox(20);

        BDProgressContent progressContent = new BDProgressContent();
        progressContent.setPausable(true);
        progressContent.setPrefWidth(500);

        HBox buttonGroup = new HBox();

        BDButton insertTask = new BDButton("插入任务",Util.getImageView(20, BDIcon.ADD_DARK));
        insertTask.setSelectable(false);
        mapping.addEventHandler(insertTask, ActionEvent.ACTION,_-> progressContent.setTask(getSimpleTask()));

        BDButton cleanTask = new BDButton("移除任务",Util.getImageView(20,BDIcon.CLEAR_CASH_DARK));
        cleanTask.setSelectable(false);
        mapping.addEventHandler(cleanTask, ActionEvent.ACTION,_-> progressContent.setTask(null));

        buttonGroup.getChildren().addAll(insertTask,Util.getHBoxSpring(),cleanTask);
        root.getChildren().addAll(progressContent,Util.getVBoxSpring(),buttonGroup);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 600, 200);
        stage.setScene(scene);
        stage.show();
        stage.setAlwaysOnTop(true);
    }

    private static BDTask<String> getSimpleTask() {
        BDTask<String> task = new BDTask<>() {
            @Override
            protected String work() throws InterruptedException, TimeoutException {
                for (int i = 0; i < 101; i++) {
                    /*
                    * 必须调用此函数！！！只有这样，暂停、超时等功能才生效。
                    * */
                    checkState();
                    Thread.sleep(100);
                    updateProgress(i, 100);
                    updateMessage("正在加载(%d/%d)".formatted(i, 100));
                    updateTitle("更新测试");
                }
                return "done";
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                updateMessage("更新成功");
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                updateMessage("取消任务");
            }

            @Override
            protected void failed() {
                super.failed();
                updateMessage("任务执行失败");
            }
        };
        new Thread(task).start();
        return task;
    }
}
