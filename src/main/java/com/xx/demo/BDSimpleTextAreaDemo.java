package com.xx.demo;

import com.dlsc.fxmlkit.fxml.FxmlKit;
import com.xx.UI.complex.textArea.view.BDTextArea;
import com.xx.UI.complex.textArea.view.dataFormat.analyse.Analyse;
import com.xx.UI.complex.textArea.view.dataFormat.analyse.BDSimpleTextInitFactory;
import com.xx.UI.complex.textArea.view.dataFormat.mark.MARK_DIRECTION;
import com.xx.UI.complex.textArea.view.dataFormat.mark.Mark;
import com.xx.UI.complex.textArea.view.dataFormat.mark.MarkNode;
import com.xx.UI.util.Util;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * BDTextArea 完整示例：为所有 testType 样式（UNDEFINED, STYLE_1 ~ STYLE_6）
 * 设置不同的渲染规则，并添加对应文本块。
 */
public class BDSimpleTextAreaDemo extends Application {

    /**
     * 自定义文本样式枚举。
     */
    enum testType implements Analyse.BDTextEnum<testType> {
        UNDEFINED,
        STYLE_1,
        STYLE_2,
        STYLE_3,
        STYLE_4,
        STYLE_5,
        STYLE_6;

        @Override
        public testType undefinedType() {
            return UNDEFINED;
        }
    }

    @Override
    public void start(Stage stage) {
        // 启用开发模式并加载样式表
        FxmlKit.enableDevelopmentMode();
        FxmlKit.setApplicationUserAgentStylesheet(Util.getResourceUrl("/css/cupertino-light.css"));

        // 创建 BDTextArea 和初始化工厂
        BDTextArea textArea = new BDTextArea();
        BDSimpleTextInitFactory<testType> factory = new BDSimpleTextInitFactory<>(textArea, testType.UNDEFINED);

        // ========== 添加各个样式的文本块 ==========
        factory.append("【UNDEFINED】默认样式（无特殊渲染）\n", testType.UNDEFINED);
        factory.append("【STYLE_1】红色文字 + 黄色虚线边框 + 左侧标记\n", testType.STYLE_1);
        factory.append("【STYLE_2】绿色文字 + 蓝色实线边框 + 右侧标记\n", testType.STYLE_2);
        factory.append("【STYLE_3】蓝色文字 + 灰色背景 \n", testType.STYLE_3);
        factory.append("【STYLE_4】紫色加粗文字 + 圆点边框 \n", testType.STYLE_4);
        factory.append("【STYLE_5】橙色文字 + 淡黄色背景 + 虚线边框 + 左侧标记（不同内容）\n", testType.STYLE_5);
        factory.insert(20,"【STYLE_6】棕色文字 + 双线边框 + 右侧标记（带颜色）\n", testType.STYLE_6);

        // ========== 为各样式设置渲染规则 ==========

        // STYLE_1：红色文字，黄色虚线边框，左侧标记（内容：“样式1”）
        factory.pushTextRenderingRuler(testType.STYLE_1, (text, textPane, dataBlock) -> {
            text.setFill(Color.RED);
            textPane.setBorder(new Border(new BorderStroke(
                    Color.YELLOW,
                    BorderStrokeStyle.DASHED,
                    new CornerRadii(5),
                    new BorderWidths(1)
            )));
            if (dataBlock.getMark() == null) {
                MarkNode node = new MarkNode();
                node.setText("样式1");
                node.setTextFill(Color.RED);
                dataBlock.setMark(new Mark(dataBlock, node, MARK_DIRECTION.LEFT));
            }
        });

        // STYLE_2：绿色文字，蓝色实线边框，右侧标记（内容：“样式2”）
        factory.pushTextRenderingRuler(testType.STYLE_2, (text, textPane, dataBlock) -> {
            text.setFill(Color.GREEN);
            textPane.setBorder(new Border(new BorderStroke(
                    Color.BLUE,
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(5),
                    new BorderWidths(2)
            )));
            if (dataBlock.getMark() == null) {
                MarkNode node = new MarkNode();
                node.setText("样式2");
                node.setTextFill(Color.GREEN);
                dataBlock.setMark(new Mark(dataBlock, node, MARK_DIRECTION.RIGHT));
            }
        });

        // STYLE_3：蓝色文字，灰色背景，上方标记（内容：“↑样式3”）
        factory.pushTextRenderingRuler(testType.STYLE_3, (text, textPane, dataBlock) -> {
            text.setFill(Color.BLUE);
            textPane.setBackground(new Background(new BackgroundFill(
                    Color.LIGHTGRAY, CornerRadii.EMPTY, null
            )));
            if (dataBlock.getMark() == null) {
                MarkNode node = new MarkNode();
                node.setText("样式3");
                node.setTextFill(Color.BLUE);
                dataBlock.setMark(new Mark(dataBlock, node, MARK_DIRECTION.LEFT));
            }
        });

        // STYLE_4：紫色加粗文字，圆点边框，下方标记（内容：“样式4↓”）
        factory.pushTextRenderingRuler(testType.STYLE_4, (text, textPane, dataBlock) -> {
            text.setFill(Color.PURPLE);
            text.setFont(Font.font(text.getFont().getFamily(), FontWeight.BOLD, text.getFont().getSize()));
            textPane.setBorder(new Border(new BorderStroke(
                    Color.MAGENTA,
                    BorderStrokeStyle.DOTTED,
                    new CornerRadii(5),
                    new BorderWidths(2)
            )));
            if (dataBlock.getMark() == null) {
                MarkNode node = new MarkNode();
                node.setText("样式4");
                node.setTextFill(Color.PURPLE);
                dataBlock.setMark(new Mark(dataBlock, node, MARK_DIRECTION.LEFT));
            }
        });

        // STYLE_5：橙色文字，淡黄色背景，虚线边框，左侧标记（内容：“★样式5”）
        factory.pushTextRenderingRuler(testType.STYLE_5, (text, textPane, dataBlock) -> {
            text.setFill(Color.ORANGE);
            textPane.setBackground(new Background(new BackgroundFill(
                    Color.LIGHTYELLOW, CornerRadii.EMPTY, null
            )));
            textPane.setBorder(new Border(new BorderStroke(
                    Color.ORANGERED,
                    BorderStrokeStyle.DASHED,
                    new CornerRadii(5),
                    new BorderWidths(1)
            )));
            if (dataBlock.getMark() == null) {
                MarkNode node = new MarkNode();
                node.setText("★样式5");
                node.setTextFill(Color.ORANGE);
                dataBlock.setMark(new Mark(dataBlock, node, MARK_DIRECTION.LEFT));
            }
        });

        // STYLE_6：棕色文字，双线边框，右侧标记（内容：“样式6★”，蓝色文字）
        factory.pushTextRenderingRuler(testType.STYLE_6, (text, textPane, dataBlock) -> {
            text.setFill(Color.BROWN);
            // 双线边框需要使用多个 BorderStroke 组合？这里用 BorderStrokeStyle 的 DOUBLE 线型（需要确认是否存在）
            // 如果 DOUBLE 不可用，可以使用两个边框叠加或者用粗边框模拟。这里使用 BorderStrokeStyle.SOLID 并加粗宽度模拟双线效果。
            textPane.setBorder(new Border(new BorderStroke(
                    Color.DARKGOLDENROD,
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(5),
                    new BorderWidths(3) // 粗边框模拟双线
            )));
            if (dataBlock.getMark() == null) {
                MarkNode node = new MarkNode();
                node.setText("样式6★");
                node.setTextFill(Color.BLUE); // 标记文字为蓝色，与文字颜色区分
                dataBlock.setMark(new Mark(dataBlock, node, MARK_DIRECTION.RIGHT));
            }
        });

        // 可选：为 UNDEFINED 设置一个简单规则，或不设置（使用默认）
        // 这里不设置，展示原始样式

        // 应用工厂
        textArea.setTextInitFactory(factory);

        // 布局
        StackPane stackPane = new StackPane(textArea);
        stackPane.setStyle("-fx-padding: 20; -fx-background-color: #f0f0f0;");
        Scene scene = new Scene(stackPane, 800, 600);
        stage.setTitle("BDTextArea 多样式测试");
        stage.setScene(scene);
        stage.show();
    }

}