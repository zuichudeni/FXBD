package com.xx.UI.complex.stage;

import com.xx.UI.basic.BDButton;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.Util;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.HeaderButtonType;
import javafx.scene.layout.HeaderDragType;
import javafx.scene.text.Text;

/**
 * BDHeaderBar构建器
 * 用于构建具有左右中三个区域的标题栏
 */
public class BDHeaderBarBuilder {
    private final HeaderBar headerBar = new HeaderBar();
    private final HBox leadingBox = new HBox();
    private final HBox centerBox = new HBox();
    private final HBox trailingBox = new HBox();

    public BDHeaderBarBuilder() {
        initializeHeaderBar();
    }

    /**
     * 初始化标题栏及其子组件
     */
    private void initializeHeaderBar() {
        headerBar.getStyleClass().add("bd-header-bar");
        leadingBox.getStyleClass().add("bd-header-bar-leading");
        centerBox.getStyleClass().add("bd-header-bar-center");
        trailingBox.getStyleClass().add("bd-header-bar-trailing");

        headerBar.setLeading(leadingBox);
        HeaderBar.setAlignment(leadingBox, Pos.CENTER_LEFT);

        headerBar.setCenter(centerBox);
        HeaderBar.setAlignment(centerBox, Pos.CENTER);

        headerBar.setTrailing(trailingBox);
        HeaderBar.setAlignment(trailingBox, Pos.CENTER_RIGHT);

        HeaderBar.setDragType(centerBox, HeaderDragType.DRAGGABLE);
    }

    /**
     * 向左侧区域添加节点
     * @param node 要添加的节点
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addLeading(Node node) {
        leadingBox.getChildren().add(node);
        return this;
    }

    /**
     * 添加图标到左侧区域
     * @param imageView 图标ImageView
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addIcon(ImageView imageView) {
        HBox.setMargin(imageView, new Insets(0, 0, 0, 10));
        return addLeading(imageView);
    }

    /**
     * 向中间区域添加节点
     * @param node 要添加的节点
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addCenter(Node node) {
        centerBox.getChildren().add(node);
        return this;
    }

    /**
     * 在中间区域添加标题
     * @param title 标题文本
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addTitleInCenter(String title) {
        Text node = new Text(title);
        node.getStyleClass().add("bd-header-bar-title");
        return addCenter(node);
    }

    /**
     * 在左侧区域添加标题
     * @param title 标题文本
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addTitleInLeft(String title) {
        Text node = new Text(title);
        node.getStyleClass().add("bd-header-bar-title");
        return addLeading(node);
    }

    /**
     * 向右侧区域添加节点
     * @param node 要添加的节点
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addTrailing(Node node) {
        trailingBox.getChildren().add(node);
        return this;
    }

    /**
     * 添加最大化按钮
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addMaximizeButton() {
        BDButton button = createToolButton(
            "bd-stage-maximize-button",
            BDIcon.MAXIMIZE_DARK,
            BDIcon.MAXIMIZE_INACTIVE_DARK
        );
        HeaderBar.setButtonType(button, HeaderButtonType.MAXIMIZE);
        return addTrailing(button);
    }

    /**
     * 添加最小化按钮
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addMinimizeButton() {
        BDButton button = createToolButton(
            "bd-stage-minimize-button",
            BDIcon.MINIMIZE_DARK,
            BDIcon.MINIMIZE_INACTIVE_DARK
        );
        HeaderBar.setButtonType(button, HeaderButtonType.ICONIFY);
        return addTrailing(button);
    }

    /**
     * 添加关闭按钮
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addCloseButton() {
        BDButton button = createToolButton(
            "bd-stage-close-button",
            BDIcon.CLOSE_DARK,
            BDIcon.CLOSE_INACTIVE_DARK
        );
        HeaderBar.setButtonType(button, HeaderButtonType.CLOSE);
        return addTrailing(button);
    }

    /**
     * 创建工具按钮的通用方法
     * @param styleClass 样式类名
     * @param defaultIcon 默认图标
     * @param pressIcon 按下时的图标
     * @return 配置好的按钮
     */
    private BDButton createToolButton(String styleClass, BDIcon defaultIcon, BDIcon pressIcon) {
        BDButton button = new BDButton();
        button.getStyleClass().addAll("bd-stage-tool-button", styleClass);
        button.setDefaultGraphic(Util.getImageView(15, defaultIcon));
        button.setPressGraphic(Util.getImageView(15, pressIcon));
        button.setSelectable(false);
        return button;
    }

    /**
     * 构建并返回标题栏
     * @return 配置好的HeaderBar实例
     */
    public HeaderBar build() {
        return headerBar;
    }
}