package com.xx.UI.util;

import com.xx.UI.ui.BDIcon;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Util {
    public static String getResourceUrl(String path) {
        return Objects.requireNonNull(Util.class.getResource(path)).toExternalForm();
    }

    public static Image getImage(String path) {
        return new Image(getResourceUrl(path));
    }

    public static Image getImage(BDIcon icon) {
        return getImage(icon.getIconPath());
    }

    public static ImageView getImageView(double fillWidth, String path) {
        ImageView imageView = new ImageView(getResourceUrl(path));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(fillWidth);
        return imageView;
    }

    public static ImageView getImageView(double fillWidth, String path, InitImageView initImageView) {
        ImageView imageView = getImageView(fillWidth, path);
        initImageView.initImageView(imageView);
        return imageView;
    }

    public static ImageView getImageView(double fillWidth, BDIcon icon) {
        ImageView imageView = new ImageView(getResourceUrl(icon.getIconPath()));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(fillWidth);
        return imageView;
    }

    public static ImageView getImageView(double fillWidth, BDIcon icon, InitImageView initImageView) {
        ImageView imageView = getImageView(fillWidth, icon);
        initImageView.initImageView(imageView);
        return imageView;
    }

    public static Node getHBoxSpring() {
        Pane stackPane = new Pane();
        HBox.setHgrow(stackPane, Priority.ALWAYS);
        return stackPane;
    }

    public static Node getVBoxSpring() {
        Pane stackPane = new Pane();
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        return stackPane;
    }

    public static Node getSpring() {
        Node hBoxSpring = getHBoxSpring();
        VBox.setVgrow(hBoxSpring, Priority.ALWAYS);
        return hBoxSpring;
    }

    public static TextArea getInputContent(double showLine) {
    return new TextArea() {
        private double lineHeight;
        private final double basePadding = 8.0; // 基础内边距

        @Override
        protected Skin<?> createDefaultSkin() {
            return new TextAreaSkin(this) {
                {
                    TextArea node = (TextArea) getNode();
                    node.applyCss();
                    lineHeight = node.lookup(".text").getLayoutBounds().getHeight();
                    node.getStyleClass().add("transparent-text-area");
                    node.setMinHeight(USE_PREF_SIZE);
                    node.setMaxHeight(USE_PREF_SIZE);
                    // 初始高度计算
                    updateHeight(node);
                    // 监听文本变化
                    node.textProperty().addListener((_, _, _) -> updateHeight(node));
                    // 监听滚动条可见性变化
                    ScrollPane scrollPane = (ScrollPane) node.lookup(".scroll-pane");
                    if (scrollPane != null) {
                        ScrollBar vBar = (ScrollBar) scrollPane.lookup(".scroll-bar:vertical");
                        if (vBar != null) {
                            vBar.visibleProperty().addListener((_, _, _) -> updateHeight(node));
                        }
                    }
                }

                private void updateHeight(TextArea node) {
                    String text = node.getText();
                    int lineCount = Math.max(1, countNewlines(text) + 1);
                    int visibleLines = (int) Math.min(showLine, lineCount);

                    double newHeight = lineHeight * visibleLines + basePadding ;

                    // 如果有垂直滚动条，稍微调整高度以避免布局跳动
                    ScrollPane scrollPane = (ScrollPane) node.lookup(".scroll-pane");
                    if (scrollPane != null) {
                        ScrollBar vBar = (ScrollBar) scrollPane.lookup(".scroll-bar:vertical");
                        if (vBar != null && vBar.isVisible()) {
                            newHeight += 2; // 微小调整
                        }
                    }

                    node.setPrefHeight(newHeight);
                }

            };
        }
    };
}

    public static Button getIconButton() {
        Button button = new Button();
        button.getStyleClass().add("button-bd-icon");
        return button;
    }

    /**
     * 给定一个节点，返回该节点及其父节点的列表
     */

    public static List<Node> getParentList(Node node) {
        Objects.requireNonNull(node, "节点不能为null");
        List<Node> list = new ArrayList<>();
        list.add(node);
        Node parent = node.getParent();
        while (parent != null) {
            list.add(parent);
            parent = parent.getParent();
        }
        return list;
    }

    /**
     * 给定一个节点，返回一个BooleanProperty，当该节点或其该节点的子节点获得焦点时，返回true，否则返回false。只要当前窗口获得焦点时才可以返回true。
     *
     * @param node    节点
     * @param mapping 需要传入一个BDMapping对象，该对象会在节点的sceneProperty发生变化时自动释放原来的监听器
     *
     */
    public static BooleanProperty focusWithIn(Node node, BDMapping mapping) {
        SimpleBooleanProperty focusWithIn = new SimpleBooleanProperty();
        BDMapping temp = new BDMapping();
        BDMapping temp1 = new BDMapping();
        mapping.addChildren(temp);
        temp.addChildren(temp1);
        mapping.addListener(() -> {
            temp.dispose();
            Scene scene = node.getScene();
            if (scene != null) {
                temp.addListener(() -> {
                    temp1.dispose();
                    Window window = scene.getWindow();
                    if (window != null)
                        temp1.binding(focusWithIn, Bindings.createBooleanBinding(() -> {
                            Node owner = scene.getFocusOwner();
                            if (owner == null) return false;
                            return getParentList(owner).contains(node) && window.isFocused();
                        }, scene.focusOwnerProperty(), window.focusedProperty()));
                }, true, scene.windowProperty());
            }
        }, true, node.sceneProperty());
        return focusWithIn;
    }

    public static int countNewlines(String text) {
        if (text == null) return 0;
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == '\n') count++;
        }
        return count;
    }

    public static <T extends Node> T searchEventTargetNode(EventTarget target, Class<T> type) {
        if (target instanceof Node node) {
            while (node != null) {
                if (type.isInstance(node)) return type.cast(node);
                node = node.getParent();
            }
        }
        return null;
    }

    public static <T extends Node> List<T> searchEventTargetNodes(EventTarget target, Class<T> type) {
        List<T> list = new ArrayList<>();
        if (target instanceof Node node) {
            while (node != null) {
                if (type.isInstance(node)) list.add(type.cast(node));
                node = node.getParent();
            }
        }
        return list;
    }

    public interface InitImageView {
        void initImageView(ImageView imageView);
    }

//    返回项目下的文件路径。
    public static Path getPath(String relativePath) {
        Path filePath = Paths.get("").toAbsolutePath().resolve(relativePath);
        if (!Files.exists(filePath)) {
            // 创建父目录
            if (filePath.getParent() != null) {
                try {
                    Files.createDirectories(filePath.getParent());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            // 创建文件
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return filePath;
    }
}
