package com.xx.demo;

import com.xx.UI.complex.BDTabPane.BDTab;
import com.xx.UI.complex.BDTabPane.BDTabItem;
import com.xx.UI.complex.BDTabPane.BDTabPane;
import com.xx.UI.complex.textArea.view.BDTextArea;
import com.xx.UI.complex.textArea.view.BDTextAreaSearch;
import com.xx.UI.complex.textArea.view.dataFormat.example.java.BDJavaTextInitFactory;
import com.xx.UI.complex.textArea.view.dataFormat.example.json.BDJsonTextInitFactory;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.Util;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class BDTabPaneDemo extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("BDTabPane Demo");

//         root item
        BDTabItem splitItem = new BDTabItem();
//         root item 添加BDTab
        File file = new File(Util.getPath("src\\main\\java\\com\\xx\\UI\\ui").toUri());
        for (File child : Objects.requireNonNull(file.listFiles())) {
            if (child.isFile()) splitItem.addTab(initTab(child.toPath()));
        }
        splitItem.addTab(initTab(Util.getPath("src\\main\\resources\\business.json")));
//        BDTabPane 设置root
        BDTabPane tabPane = new BDTabPane(splitItem);

        Scene scene = new Scene(tabPane, 800, 600);
        stage.setScene(scene);
        Application.setUserAgentStylesheet(Util.getResourceUrl("/css/cupertino-light.css"));
        stage.show();
    }

    public BDTab initTab(Path path) {
        BDTab tab = new BDTab(path.getFileName().toString());
//        tab 设置图标
        tab.setGraphic(Util.getImageView(25, path.toString().endsWith("java") ? BDIcon.CLASS : BDIcon.JSON));
//        tab 添加显示内容
        BDTextAreaSearch search = new BDTextAreaSearch(initTextArea(path));
        tab.setContent(search);
//        当tab关闭时，触发search的dispose。
        tab.getMapping().addChildren(search.getMapping());
        return tab;
    }

    private BDTextArea initTextArea(Path path) {
        BDTextArea area = new BDTextArea();
        try {
            area.insertText(area.getLength(), Files.readString(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        antrl4 规则渲染
        if (path.toString().endsWith("java"))
            area.setTextInitFactory(new BDJavaTextInitFactory(area));
        else area.setTextInitFactory(new BDJsonTextInitFactory(area));
        return area;
    }
}
