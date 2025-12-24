package com.xx;

import com.xx.UI.complex.splitPane.BDTabItem;
import com.xx.UI.complex.splitPane.BDTabPane;
import com.xx.UI.complex.splitPane.BDTab;
import com.xx.UI.complex.textArea.view.BDTextArea;
import com.xx.UI.complex.textArea.view.BDTextAreaSearch;
import com.xx.UI.complex.textArea.view.dataFormat.example.java.BDJavaTextInitFactory;
import com.xx.UI.complex.textArea.view.dataFormat.example.json.BDJsonTextInitFactory;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.BDMapping;
import com.xx.UI.util.Util;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Launch extends Application {
    public static BDMapping ROOT_MAPPING = new BDMapping();

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Text Editor");

        BDTabItem splitItem = new BDTabItem();
        File file = new File(Util.getPath("src\\main\\java\\com\\xx\\UI\\ui").toUri());
        for (File child : Objects.requireNonNull(file.listFiles())) {
            if (child.isFile())
                splitItem.addTab(initTab(child.toPath()));
        }
        splitItem.addTab(initTab(Util.getPath("src\\main\\resources\\business.json")));

        Scene scene = new Scene(new BDTabPane(splitItem), 800, 600);

        stage.setScene(scene);
        Application.setUserAgentStylesheet(Util.getResourceUrl("/css/cupertino-light.css"));
        stage.show();
    }

    public BDTab initTab(Path path) {
        BDTab tab = new BDTab(path.getFileName().toString());
        tab.setGraphic(Util.getImageView(25, path.toString().endsWith("java")?BDIcon.CLASS:BDIcon.JSON));
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
