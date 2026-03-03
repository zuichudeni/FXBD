package com.xx.demo;

import com.dlsc.fxmlkit.fxml.FxmlKit;
import com.xx.UI.complex.tree.BDTreeCellInitFactory;
import com.xx.UI.complex.tree.BDTreeView;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.Util;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class BDTreeViewDemo extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FxmlKit.enableDevelopmentMode();
        FxmlKit.setApplicationUserAgentStylesheet(Util.getResourceUrl("/css/cupertino-light.css"));

        BDTreeView<File> treeView = new BDTreeView<>();
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeView.setTreeCellInitFactory(new BDTreeCellInitFactory<>() {
            @Override
            public Node initGraphic(File file) {
                return Util.getImageView(25, BDIcon.getIconForFile(file));
            }

            @Override
            public String getInfo(File file) {
                return file.getName();
            }

            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isDirectory())
                    return o1.getName().compareTo(o2.getName());
                if (o1.isDirectory()) return 0;
                if (o2.isDirectory()) return 1;
                return o1.getName().compareTo(o2.getName());
            }

            @Override
            public void rendering(File file, Node graphic, Text text, Region pane) {
                if (file.isDirectory()) text.setFill(Color.BLUE);
                else text.setFill(Color.BLACK);
            }
        });
        treeView.setRoot(getFile(Path.of("D:\\").toFile()));
        treeView.sortItem();
        StackPane stackPane = new StackPane(treeView);
        Scene scene = new Scene(stackPane, 800, 600);
        stage.setTitle("BDTreeView 测试");
        stage.setScene(scene);
        stage.show();
        stage.setAlwaysOnTop(true);
    }

    private TreeItem<File> getFile(File root) {
        var item = new TreeItem<>(root);
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null)
                for (File file : Objects.requireNonNull(files))
                    item.getChildren().add(getFile(file));
        }
        return item;
    }

}
