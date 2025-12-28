package com.xx;

import com.xx.UI.complex.BDTabPane.BDTab;
import com.xx.UI.complex.textArea.view.BDTextArea;
import com.xx.UI.complex.textArea.view.BDTextAreaSearch;
import com.xx.UI.complex.textArea.view.dataFormat.example.java.BDJavaTextInitFactory;
import com.xx.UI.complex.textArea.view.dataFormat.example.json.BDJsonTextInitFactory;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.BDMapping;
import com.xx.UI.util.Util;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Launch extends Application {
    public static BDMapping ROOT_MAPPING = new BDMapping();


    @Override
    public void start(Stage stage) {
        // 创建标题栏
        HeaderBar headerBar = new HeaderBar();
        headerBar.setPrefHeight(50);
        stage.initStyle(StageStyle.EXTENDED);

        // 左侧：返回按钮
        Button backButton = new Button("←");
        headerBar.setLeading(backButton);
        HeaderBar.setAlignment(backButton, Pos.CENTER_LEFT);
        HeaderBar.setMargin(backButton, new Insets(0, 0, 0, 10));

        // 中间：标题（可拖拽）
        Label titleLabel = new Label("我的应用程序");
        headerBar.setCenter(titleLabel);
        HeaderBar.setAlignment(titleLabel, Pos.CENTER);
        HeaderBar.setDragType(titleLabel, HeaderDragType.DRAGGABLE);

        // 右侧：系统按钮
        HBox systemButtons = new HBox(5);

        Button minimizeBtn = new Button("_");
        HeaderBar.setButtonType(minimizeBtn, HeaderButtonType.ICONIFY);

        Button maximizeBtn = new Button("□");
        HeaderBar.setButtonType(maximizeBtn, HeaderButtonType.MAXIMIZE);

        Button closeBtn = new Button("×");
        HeaderBar.setButtonType(closeBtn, HeaderButtonType.CLOSE);

        systemButtons.getChildren().addAll(minimizeBtn, maximizeBtn, closeBtn);
        headerBar.setTrailing(systemButtons);

        Scene scene = new Scene(new VBox(headerBar, initTextArea(Util.getPath("src/main/java/com/xx/Launch.java"))), 800, 600);
        stage.setScene(scene);HeaderBar.setPrefButtonHeight(stage, 0);
        stage.setTitle("HeaderBar 示例");
        stage.show();

        Application.setUserAgentStylesheet(Util.getResourceUrl("/css/cupertino-light.css"));

        new StageTesterWindow(stage).show();

    }

    public BDTab initTab(Path path) {
        BDTab tab = new BDTab(path.getFileName().toString());
        tab.setGraphic(Util.getImageView(25, path.toString().endsWith("java") ? BDIcon.CLASS : BDIcon.JSON));
        BDTextAreaSearch search = new BDTextAreaSearch(initTextArea(path));
        tab.setContent(search);
        tab.getMapping().addChildren(search.getMapping());
        return tab;
    }

    private BDTextArea initTextArea(Path path) {
        BDTextArea area = new BDTextArea();
//        area.insertNode(0, new NodeSegment<>(" ", _ -> {
//            Button pre = new Button("pre");
//            area.getMapping().addEventHandler(pre, ActionEvent.ACTION, _ -> area.caretLeft());
//            return pre;
//        }));
//        area.insertNode(0, new NodeSegment<>(" ", _ -> {
//            Button pre = new Button("pre");
//            area.getMapping().addEventHandler(pre, ActionEvent.ACTION, _ -> area.caretLeft());
//            return pre;
//        }));
//        area.insertNode(0, new NodeSegment<>(" ", _ -> {
//            Button next = new Button("print");
//            area.getMapping().addEventHandler(next, ActionEvent.ACTION, _ -> System.out.printf("总行数：%d,总字符：%d%n", area.getLineNum(), area.getLength()));
//            return next;
//        }));
//        area.insertNode(0, new NodeSegment<>(" ", _ -> Util.getImageView(100, "/img.png")));
//        area.insertText(area.getLength(), "\n");
        try {
            area.insertText(area.getLength(), Files.readString(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.printf("总行数：%d,总字符：%d%n", area.getLineNum(), area.getLength());
//        antrl4 规则渲染
        if (path.toString().endsWith("java"))
            area.setTextInitFactory(new BDJavaTextInitFactory(area));
        else area.setTextInitFactory(new BDJsonTextInitFactory(area));
//        正则表达式 规则渲染
//        area.setTextInitFactory(new BDRegulaInitFactory(area));
        return area;
    }

    @Override
    public void stop() throws Exception {
        ROOT_MAPPING.dispose();
    }
}
