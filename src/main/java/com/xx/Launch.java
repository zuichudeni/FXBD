package com.xx;

import com.xx.UI.util.BDMapping;
import com.xx.UI.util.Util;
import javafx.application.Application;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Launch extends Application {
    public static BDMapping ROOT_MAPPING = new BDMapping();
    private Service<Double> service;


    @Override
    public void start(Stage stage) {
        service = new Service<Double>() {
            @Override
            protected Task<Double> createTask() {
                return new Task<>() {
                    @Override
                    protected Double call() throws InterruptedException {
                        for (int i = 0; i < 10; i++) {
                            Thread.sleep(100);
                            updateProgress(i,10);
                        }
                        return 0.0;
                    }
                };
            }
        };
        ProgressBar bar = new ProgressBar();
        service.progressProperty().addListener((_, _, nv) -> {
            bar.setProgress(nv.doubleValue());
        });

        Task<Double> task = new Task<Double>() {
            @Override
            protected Double call() throws Exception {
                return 0.0;
            }
        };


        service.start();
        Scene scene = new Scene(new StackPane(bar));
        stage.setScene(scene);
        stage.show();
        Application.setUserAgentStylesheet(Util.getResourceUrl("/css/cupertino-light.css"));
    }

    @Override
    public void stop() throws Exception {
        ROOT_MAPPING.dispose();
    }
}
