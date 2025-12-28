package com.xx.UI.complex.stage;

import com.xx.UI.util.Util;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * BDStage构建器
 * 用于构建具有自定义标题栏的JavaFX窗口
 */
public class BDStageBuilder {
    private final VBox root = new VBox();
    private final Stage stage = new Stage();

    /**
     * 构造函数，初始化Stage和Scene
     * @param headerBar 标题栏组件
     */
    public BDStageBuilder(HeaderBar headerBar) {
        initializeStage(headerBar);
    }

    /**
     * 初始化Stage和Scene
     * @param headerBar 标题栏组件
     */
    private void initializeStage(HeaderBar headerBar) {
        stage.initStyle(StageStyle.EXTENDED);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        root.getChildren().addFirst(headerBar);
        HeaderBar.setPrefButtonHeight(stage, 0);
    }

    /**
     * 设置窗口内容
     * @param content 内容节点
     * @return 当前构建器实例，支持链式调用
     */
    public BDStageBuilder buildContent(Node content) {
        VBox.setVgrow(content, Priority.ALWAYS);
        root.getChildren().add(content);
        return this;
    }

    /**
     * 设置窗口样式
     * @param path 样式文件路径
     * @return 当前构建器实例，支持链式调用
     */
    public BDStageBuilder buildStyle(String path) {
        Application.setUserAgentStylesheet(Util.getResourceUrl(path));
        return this;
    }

    /**
     * 构建并返回Stage
     * @return 配置好的Stage实例
     */
    public Stage build() {
        return stage;
    }
}
