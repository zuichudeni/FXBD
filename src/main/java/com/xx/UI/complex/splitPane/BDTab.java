package com.xx.UI.complex.splitPane;

import com.xx.UI.basic.BDButton;
import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.Util;
import javafx.beans.property.*;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Popup;

import static com.xx.UI.complex.splitPane.BDTabItem.BD_TAB_FORMAT;

public class BDTab extends BDControl {
    //    所属的SplitItem
    final SimpleObjectProperty<BDTabItem> splitItem = new SimpleObjectProperty<>();
    //    是否显示
    final SimpleBooleanProperty show = new SimpleBooleanProperty(false);
    //    展示的内容
    private final SimpleObjectProperty<Node> content = new SimpleObjectProperty<>();
    //    是否可关闭
    private final SimpleBooleanProperty closable = new SimpleBooleanProperty(true);
    //    标题
    private final SimpleStringProperty title = new SimpleStringProperty();
    //    图标
    private final SimpleObjectProperty<Node> graphic = new SimpleObjectProperty<>();
    // 缓存拖拽图像
    private WritableImage cachedDragImage;
    private double cachedWidth = -1;
    private double cachedHeight = -1;

    public BDTab() {
        mapping.addDisposeEvent(this::clearCachedDragImage);
    }

    public BDTab(String title) {
        this();
        setTitle(title);
    }

    public BDTab(String title, Node content) {
        this(title);
        setContent(content);
    }

    public BDTab(String title, Node content, Node graphic) {
        this(title, content);
        setGraphic(graphic);
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDTabSkin(this);
    }

    public boolean isClosable() {
        return closable.get();
    }

    public void setClosable(boolean closable) {
        this.closable.set(closable);
    }

    public SimpleBooleanProperty closableProperty() {
        return closable;
    }

    public void close() {
        if (!isClosable()) return;
        BDTabItem item = splitItem.get();
        drag();
//        dispose后无法复原。
        mapping.dispose();
        item.check();
    }

    //    拖拽时，将Tab从SplitItem中暂时移除
    void drag() {
        if (splitItem.get() instanceof BDTabItem item)
            item.removeTab(this);
        show.set(false);
    }

    public Node getContent() {
        return content.get();
    }

    public void setContent(Node content) {
        this.content.set(content);
    }

    public SimpleObjectProperty<Node> contentProperty() {
        return content;
    }

    public Node getGraphic() {
        return graphic.get();
    }

    public void setGraphic(Node graphic) {
        this.graphic.set(graphic);
    }

    public SimpleObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    public boolean isShow() {
        return show.get();
    }

    public ReadOnlyBooleanProperty showProperty() {
        return show;
    }

    public void show() {
        if (splitItem.get() instanceof BDTabItem item)
            item.setShowTab(this);
        if (getContent() != null) getContent().requestFocus();
    }

    public BDTabItem getSplitItem() {
        return splitItem.get();
    }

    public ReadOnlyObjectProperty<BDTabItem> splitItemProperty() {
        return splitItem;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    /**
     * 处理拖拽检测事件，创建高质量的拖拽图像
     */
    void handleDragDetected(MouseEvent e) {
        if (!e.getButton().equals(MouseButton.PRIMARY)) return;

        e.setDragDetect(true);

        // 确保控件完全渲染
        applyCss();
        layout();

        // 创建高质量的拖拽图像
        WritableImage dragImage = createHighQualityDragImage();

        // 启动拖拽
        Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
        ClipboardContent cd = new ClipboardContent();

        // 设置拖拽图像（使用高质量图像）
        if (dragImage != null) {
            dragboard.setDragView(dragImage,
                    dragImage.getWidth() / 2,
                    dragImage.getHeight() / 2);
        }

        // 设置拖拽内容
        cd.put(BD_TAB_FORMAT, getUserData() == null ? toString() : getUserData().toString());
        dragboard.setContent(cd);
        drag();
    }

    /**
     * 创建高质量的拖拽图像
     */
    private WritableImage createHighQualityDragImage() {
        double currentWidth = getWidth();
        double currentHeight = getHeight();

        // 检查是否需要重新创建缓存图像
        if (cachedDragImage != null &&
                Math.abs(currentWidth - cachedWidth) < 0.5 &&
                Math.abs(currentHeight - cachedHeight) < 0.5) {
            return cachedDragImage;
        }

        // 确保有有效的尺寸
        if (currentWidth <= 0 || currentHeight <= 0) {
            currentWidth = prefWidth(-1);
            currentHeight = prefHeight(-1);

            if (currentWidth <= 0 || currentHeight <= 0) {
                currentWidth = 100;
                currentHeight = 30;
            }
        }

        // 计算缩放因子：小控件使用更高的缩放，大控件适当缩小
        double scaleFactor = calculateOptimalScaleFactor(currentWidth, currentHeight);

        // 创建图像
        WritableImage image = createScaledSnapshot(scaleFactor);

        // 缓存结果
        cachedDragImage = image;
        cachedWidth = currentWidth;
        cachedHeight = currentHeight;

        return image;
    }

    /**
     * 计算最优的缩放因子
     */
    private double calculateOptimalScaleFactor(double width, double height) {
        double baseSize = Math.max(width, height);

        if (baseSize < 100) {
            return 1; // 小控件，放大2.5倍
        } else if (baseSize < 200) {
            return 1.25; // 中等控件，放大2倍
        } else if (baseSize > 400) {
            return 1; // 很大控件，适当缩小
        } else {
            return 1.5; // 默认放大1.5倍
        }
    }

    /**
     * 创建缩放后的截图
     */
    private WritableImage createScaledSnapshot(double scaleFactor) {
        double originalWidth = Math.max(1, getWidth());
        double originalHeight = Math.max(1, getHeight());

        // 计算目标尺寸
        int targetWidth = (int) Math.max(1, originalWidth * scaleFactor);
        int targetHeight = (int) Math.max(1, originalHeight * scaleFactor);

        // 限制最大尺寸，避免性能问题
        targetWidth = Math.min(targetWidth, 500);
        targetHeight = Math.min(targetHeight, 200);

        // 创建图像
        WritableImage image = new WritableImage(targetWidth, targetHeight);

        try {
            // 配置快照参数
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.WHITE); // 透明背景
            parameters.setDepthBuffer(true); // 启用深度缓冲

            // 如果缩放因子不是1，应用变换
            if (Math.abs(scaleFactor - 1.0) > 0.01) {
                parameters.setTransform(javafx.scene.transform.Transform.scale(
                        scaleFactor, scaleFactor));
            }

            // 截取快照
            snapshot(parameters, image);
            return image;
        } catch (Exception e) {
            return createSimpleSnapshot();
        }
    }

    /**
     * 创建简单截图（后备方案）
     */
    private WritableImage createSimpleSnapshot() {
        double width = Math.max(1, getWidth());
        double height = Math.max(1, getHeight());

        int imageWidth = (int) width;
        int imageHeight = (int) height;

        WritableImage image = new WritableImage(imageWidth, imageHeight);
        snapshot(new SnapshotParameters(), createImageWithShadowUsingImageView(image));

        return image;
    }


    /**
     * 使用ImageView和效果API添加阴影
     */
    private WritableImage createImageWithShadowUsingImageView(WritableImage original) {
        // 创建ImageView来显示原始图像
        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(original);

        // 设置阴影效果
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();

        // 阴影参数配置
        shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.3)); // 半透明黑色阴影
        shadow.setRadius(3); // 阴影模糊半径，值越大阴影越模糊
        shadow.setOffsetX(1); // 水平偏移量
        shadow.setOffsetY(1); // 垂直偏移量
        shadow.setSpread(0.05); // 阴影扩展范围

        // 应用效果到ImageView
        imageView.setEffect(shadow);

        // 设置背景透明
        javafx.scene.layout.Pane container = new javafx.scene.layout.Pane(imageView);
        container.setStyle("-fx-background-color: transparent;");

        // 由于阴影会超出原图边界，需要增加容器大小来容纳阴影
        double shadowMargin = shadow.getRadius() + Math.max(Math.abs(shadow.getOffsetX()), Math.abs(shadow.getOffsetY()));
        int containerWidth = (int) (original.getWidth() + shadowMargin * 2);
        int containerHeight = (int) (original.getHeight() + shadowMargin * 2);

        // 调整ImageView在容器中的位置，确保图像居中
        imageView.setLayoutX(shadowMargin);
        imageView.setLayoutY(shadowMargin);

        // 创建快照参数
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(javafx.scene.paint.Color.TRANSPARENT);
        params.setDepthBuffer(true);

        // 渲染带阴影的图像
        return container.snapshot(params, new WritableImage(containerWidth, containerHeight));
    }

    /**
     * 清理缓存的拖拽图像
     */
    void clearCachedDragImage() {
        cachedDragImage = null;
        cachedWidth = -1;
        cachedHeight = -1;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    private final PseudoClass TAB_SHOW = PseudoClass.getPseudoClass("show");

//    单纯的生成本标签tab的简单UI拷贝。
    Node cloneNode(Popup popup) {
        HBox hBox = new HBox();
        hBox.getStyleClass().add("bd-tab-clone");
        double height = 20;
        if (getGraphic() != null) {
            Node graphicNode = getGraphic();

            // 确保节点已应用样式和布局
            graphicNode.applyCss();

            Bounds layoutBounds = graphicNode.getLayoutBounds();

            // 确保有有效的尺寸
            if (layoutBounds.getWidth() > 0 && layoutBounds.getHeight() > 0) {
                // 获取节点的实际渲染区域
                double width = layoutBounds.getWidth();
                height = layoutBounds.getHeight();

                // 创建合适尺寸的图像
                int imageWidth = (int) Math.max(1, width);
                int imageHeight = (int) Math.max(1, height);

                WritableImage image = new WritableImage(imageWidth, imageHeight);
                SnapshotParameters parameters = new SnapshotParameters();
                parameters.setFill(Color.TRANSPARENT);
                parameters.setDepthBuffer(true);

                // 对graphic节点本身进行快照
                graphicNode.snapshot(parameters, image);
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(width); // 限制图标最大宽度
                hBox.getChildren().add(imageView);

            } else {
                try {
                    Node graphicCopy = copyNode(getGraphic());
                    hBox.getChildren().add(graphicCopy);
                } catch (Exception e) {
                    System.err.println("Failed to copy graphic: " + e.getMessage());
                }
            }
        }

        Text text = new Text(getTitle());
        text.getStyleClass().add("tab-title");

        BDButton closeButton = new BDButton();
        closeButton.setSelectable(false);
        closeButton.getStyleClass().add("circle");
        closeButton.setPadding(new Insets(0, 0, 0, 0));
        closeButton.setGraphic(Util.getImageView(20, BDIcon.CLOSE_SMALL));
        hBox.getChildren().addAll(text, Util.getHBoxSpring(), closeButton);
        closeButton.setOnAction(_ -> {
            close();
            popup.hide();
        });
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(5);
        rectangle.setHeight(height);
        rectangle.getStyleClass().add("bd-tab-clone-rectangle");
        rectangle.pseudoClassStateChanged(TAB_SHOW, isShow());
        hBox.getChildren().addFirst(rectangle);
        return hBox;
    }

    /**
     * 复制节点的辅助方法
     */
    private Node copyNode(Node original) {
        if (original == null) return null;

        // 创建一个新的同类型节点
        // 对于ImageView，创建新的ImageView
        if (original instanceof ImageView originalView) {
            ImageView copyView = new ImageView(originalView.getImage());
            copyView.setFitWidth(originalView.getFitWidth());
            copyView.setFitHeight(originalView.getFitHeight());
            copyView.setPreserveRatio(originalView.isPreserveRatio());
            return copyView;
        }

        // 对于其他节点，尝试使用快照
        Bounds bounds = original.getLayoutBounds();
        if (bounds.getWidth() > 0 && bounds.getHeight() > 0) {
            WritableImage image = new WritableImage(
                    (int) Math.max(1, bounds.getWidth()),
                    (int) Math.max(1, bounds.getHeight())
            );
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            original.snapshot(params, image);
            return new ImageView(image);
        }
        return null;
    }
}
