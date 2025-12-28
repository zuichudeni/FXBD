package com.xx.demo;

import com.xx.UI.complex.BDTabPane.BDTab;
import com.xx.UI.complex.BDTabPane.BDTabItem;
import com.xx.UI.complex.BDTabPane.BDTabPane;
import com.xx.UI.complex.textArea.content.segment.NodeSegment;
import com.xx.UI.complex.textArea.view.BDTextArea;
import com.xx.UI.complex.textArea.view.BDTextAreaSearch;
import com.xx.UI.complex.textArea.view.dataFormat.example.java.BDJavaTextInitFactory;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.Util;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;

public class BDTextAreaDemo extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("BDTextArea Demo");
        BDTabItem root = new BDTabItem();

//        普通 BDTextArea
        BDTab BDTextAreaTab = new BDTab("BDTextArea", initTextArea(), Util.getImageView(20, BDIcon.JAVA));
        root.addTab(BDTextAreaTab);
        BDTabPane tabPane = new BDTabPane(root);

//        带搜索的 BDTextArea
        BDTab searchableTextAreaTab = new BDTab("Searchable BDTextArea", new BDTextAreaSearch(initTextArea()), Util.getImageView(20, BDIcon.JAVA));
        root.addTab(searchableTextAreaTab);

        Scene scene = new Scene(tabPane, 800, 600);
        stage.setScene(scene);
        Application.setUserAgentStylesheet(Util.getResourceUrl("/css/cupertino-light.css"));
        stage.show();
    }

    public BDTextArea initTextArea() {
        BDTextArea area = new BDTextArea();
//        添加 Node
        area.appendNode(new NodeSegment<>(" ", _ -> {
            Button pre = new Button("pre");
            area.getMapping().addEventHandler(pre, ActionEvent.ACTION, _ -> area.caretLeft());
            return pre;
        }));
        area.appendNode(new NodeSegment<>(" ", _ -> {
            Button next = new Button("next");
            area.getMapping().addEventHandler(next, ActionEvent.ACTION, _ -> area.caretRight());
            return next;
        }));
        area.insertNode(0, new NodeSegment<>(" ", _ -> {
            Button next = new Button("print");
            area.getMapping().addEventHandler(next, ActionEvent.ACTION, _ -> System.out.printf("总行数：%d,总字符：%d%n", area.getLineNum(), area.getLength()));
            return next;
        }));
        area.insertNode(0, new NodeSegment<>(" ", _ -> Util.getImageView(100, "/icon.jpg")));
        area.appendText("\n");
        try {
            area.appendText(Files.readString(Util.getPath("src/main/java/com/xx/UI/complex/textArea/view/BDTextCell.java")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        设置 TextInitFactory, 用于解析文本并渲染。
//        ANTLR4规则渲染。
        area.setTextInitFactory(new BDJavaTextInitFactory(area));
        //        正则表达式 规则渲染
//        area.setTextInitFactory(new BDRegulaInitFactory(area));
        return area;
    }
}
