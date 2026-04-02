package com.xx.demo;

import com.dlsc.fxmlkit.fxml.FxmlKit;
import com.xx.UI.complex.list.BDListCellInitFactory;
import com.xx.UI.complex.list.BDListView;
import com.xx.UI.complex.list.BDListViewBySearch;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.Util;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BDListViewDemo extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FxmlKit.enableDevelopmentMode();
        FxmlKit.setApplicationUserAgentStylesheet(Util.getResourceUrl("/css/cupertino-light.css"));

        BDListView<File> listView = new BDListView<>(new BDListCellInitFactory<>() {
            @Override
            public Node initGraphic(File file) {
                if (file == null) return Util.getImageView(25,BDIcon.UNKNOWN);
                return Util.getImageView(25, BDIcon.getIconForFile(file));
            }

            @Override
            public String getInfo(File file) {
                if (file == null) return "null";
                return file.getName();
            }

            @Override
            public void rendering(File file, Node graphic, Text text, Region pane) {
                if (file != null && file.isDirectory()) text.setFill(Color.BLUE);
                else text.setFill(Color.BLACK);
            }
        });
        listView.getItems().addAll(getFile(Path.of("./").toFile()));
        Scene scene = new Scene(new BDListViewBySearch<>(listView), 800, 600);
        stage.setTitle("BDListView 测试");
        stage.setScene(scene);
        stage.show();
        stage.setAlwaysOnTop(true);


    }

    private List<File> getFile(File root) {
        List<File> list = new ArrayList<>();
        if (root != null) {
            list.add(root);
            if (root.isDirectory()) {
                File[] files = root.listFiles();
                if (files != null)
                    for (File file : Objects.requireNonNull(files))
                        list.addAll(getFile(file));
            }
        }
        return list;
    }

}
