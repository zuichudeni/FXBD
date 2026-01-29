package com.xx.UI.complex.stage;

import com.xx.UI.basic.button.BDButton;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.BDMapping;
import com.xx.UI.util.Util;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * BDStage构建器
 * 用于构建具有自定义标题栏的JavaFX窗口
 */
public class BDStageBuilder {
    private final BDMapping mapping = new BDMapping();
    private final VBox root = new VBox();
    double x = Double.MIN_VALUE;
    double y = Double.MIN_VALUE;
    double w = Double.MIN_VALUE;
    double h = Double.MIN_VALUE;

    /**
     * 构造函数，初始化Stage和Scene
     */
    public BDStageBuilder() {
        mapping.addEventHandler(stage, WindowEvent.WINDOW_SHOWN, _ -> {
            if (x != Double.MIN_VALUE)
                stage.setX(x);
            if (y != Double.MIN_VALUE)
                stage.setY(y);
            if (w != Double.MIN_VALUE)
                stage.setWidth(w);
            if (h != Double.MIN_VALUE)
                stage.setHeight(h);
        });
    }

    /**
     * 初始化Stage和Scene
     *
     * @param headerBarBuilder 标题栏构建器
     */
    public BDStageBuilder setHeaderBar(BDHeaderBarBuilder headerBarBuilder) {
        stage.initStyle(StageStyle.EXTENDED);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        root.getChildren().addFirst(headerBarBuilder.build());
        HeaderBar.setPrefButtonHeight(stage, 0);
        if (headerBarBuilder.title instanceof Text title)
            mapping.bindBidirectional(stage.titleProperty(), title.textProperty());
        if (headerBarBuilder.maximizeButton instanceof BDButton maximizeButton)
            mapping.addListener(() -> {
                if (stage.isMaximized()) {
                    maximizeButton.setDefaultGraphic(Util.getImageView(15, BDIcon.RESTORE_DARK));
                    maximizeButton.setPressGraphic(Util.getImageView(15, BDIcon.RESTORE_INACTIVE_DARK));
                } else {
                    maximizeButton.setDefaultGraphic(Util.getImageView(15, BDIcon.MAXIMIZE_DARK));
                    maximizeButton.setPressGraphic(Util.getImageView(15, BDIcon.MAXIMIZE_INACTIVE_DARK));
                }
            }, true, stage.maximizedProperty());
        return this;
    }    private final Stage stage = new Stage() {
        @Override
        public void close() {
            mapping.dispose();
            super.close();
        }

        @Override
        public void hide() {
            x = stage.getX();
            y = stage.getY();
            w = stage.getWidth();
            h = stage.getHeight();
            super.hide();
        }

    };

    /**
     * 设置窗口内容
     *
     * @param content 内容节点
     * @return 当前构建器实例，支持链式调用
     */
    public BDStageBuilder setContent(Node content) {
        VBox.setVgrow(content, Priority.ALWAYS);
        root.getChildren().add(content);
        return this;
    }

    /**
     * 设置窗口样式
     *
     * @param path 样式文件路径
     * @return 当前构建器实例，支持链式调用
     */
    public BDStageBuilder setStyle(String path) {
        Application.setUserAgentStylesheet(path);
        return this;
    }

    /**
     * 构建并返回Stage
     *
     * @return 配置好的Stage实例
     */
    public Stage build() {
        return stage;
    }

    public BDMapping getMapping() {
        return mapping;
    }


}
