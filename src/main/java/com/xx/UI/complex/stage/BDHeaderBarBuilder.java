package com.xx.UI.complex.stage;

import com.xx.UI.basic.button.BDButton;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.Util;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

/**
 * BDHeaderBar构建器
 * 用于构建具有左右中三个区域的标题栏
 */
public class BDHeaderBarBuilder {
    final HBox centerBox = new HBox();
    private final HeaderBar headerBar = new HeaderBar();
    private final HBox leadingBox = new HBox();
    private final HBox trailingBox = new HBox();
    Text title;
    ImageView icon;
    BDButton maximizeButton;
    BDButton minimizeButton;
    BDButton closeButton;

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
        HeaderBar.setDragType(leadingBox, HeaderDragType.DRAGGABLE_SUBTREE);
    }

    /**
     * 向左侧区域添加节点
     *
     * @param node 要添加的节点
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addLeading(Node node) {
        leadingBox.getChildren().add(node);
        return this;
    }

    /**
     * 添加图标到左侧区域
     *
     * @param imageView 图标ImageView
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addIcon(ImageView imageView) {
        imageView.getStyleClass().add("bd-header-bar-icon");
        this.icon = imageView;
        return this;
    }

    /**
     * 向中间区域添加节点
     *
     * @param node 要添加的节点
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addCenter(Node node) {
        centerBox.getChildren().add(node);
        return this;
    }

    /**
     * 在左侧区域添加标题
     *
     * @param title 标题文本
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addTitle(String title) {
        this.title = new Text(title);
        this.title.setText(title);
        this.title.getStyleClass().add("bd-header-bar-title");
        return this;
    }

    /**
     * 向右侧区域添加节点
     *
     * @param node 要添加的节点
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addTrailing(Node node) {
        trailingBox.getChildren().add(node);
        return this;
    }

    /**
     * 添加最大化按钮
     *
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addMaximizeButton() {
        this.maximizeButton = createToolButton(
                "bd-stage-maximize-button",
                BDIcon.MAXIMIZE_DARK,
                BDIcon.MAXIMIZE_INACTIVE_DARK
        );
        HeaderBar.setButtonType(maximizeButton, HeaderButtonType.MAXIMIZE);
        return this;
    }

    /**
     * 添加最小化按钮
     *
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addMinimizeButton() {
        this.minimizeButton = createToolButton(
                "bd-stage-minimize-button",
                BDIcon.MINIMIZE_DARK,
                BDIcon.MINIMIZE_INACTIVE_DARK
        );
        HeaderBar.setButtonType(minimizeButton, HeaderButtonType.ICONIFY);
        return this;
    }

    /**
     * 添加关闭按钮
     *
     * @return 当前构建器实例，支持链式调用
     */
    public BDHeaderBarBuilder addCloseButton() {
        this.closeButton = createToolButton(
                "bd-stage-close-button",
                BDIcon.CLOSE_DARK,
                BDIcon.CLOSE_INACTIVE_DARK
        );
        HeaderBar.setButtonType(closeButton, HeaderButtonType.CLOSE);
        return this;
    }

    public BDHeaderBarBuilder setBackFill(Paint paint) {
        headerBar.setBackground(Background.fill(paint));
        return this;
    }

    /**
     * 创建工具按钮的通用方法
     *
     * @param styleClass  样式类名
     * @param defaultIcon 默认图标
     * @param pressIcon   按下时的图标
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
     *
     * @return 配置好的HeaderBar实例
     */
    HeaderBar build() {
        HBox iconTitle = new HBox();
        if (icon != null)
            iconTitle.getChildren().add(icon);
        if (title != null)
            iconTitle.getChildren().add(title);
        iconTitle.getStyleClass().add("bd-header-bar-icon-title");
        leadingBox.getChildren().add(iconTitle);

        HBox buttonBox = new HBox();
        if (minimizeButton != null)
            buttonBox.getChildren().add(minimizeButton);
        if (maximizeButton != null)
            buttonBox.getChildren().add(maximizeButton);
        if (closeButton != null)
            buttonBox.getChildren().add(closeButton);
        buttonBox.getStyleClass().add("bd-header-bar-button-box");
        trailingBox.getChildren().add(buttonBox);
        return headerBar;
    }
}