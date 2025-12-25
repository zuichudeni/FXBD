package com.xx.UI.complex.splitPane;

import com.xx.UI.basic.BDButton;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.Util;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xx.UI.complex.splitPane.BDTabItem.dragTab;

public class BDTabItemSkin extends BDSkin<BDTabItem> {
    // 常量定义
    private static final PseudoClass BAR_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("hover");
    private static final PseudoClass RECTANGLE_FOCUSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("focused");
    private static final Duration ANIMATION_DURATION = Duration.millis(200);
    private static final int SCROLL_SPEED = 40;
    private static final String CTRL_F4_SHORTCUT = "Ctrl+F4";
    private static final double RECTANGLE_HEIGHT = 5.0;
    private static final double SCROLL_FACTOR = 1.05;

    // UI组件
    private final SplitPane splitPane;
    private final AnchorPane rootPane;
    private final VBox body;
    private final Rectangle rootRec;
    private final ScrollBar bar;
    private final HBox header;
    private final HBox leftBox;
    private final ScrollPane tabContent;
    private final AnchorPane tabContentPane;
    private final Rectangle rectangle;
    private final Rectangle tabsBack;
    private final HBox tabBox;
    private final HBox rightBox;
    private final BDButton foldButton;

    private final StackPane contentPane;

    // 动画组件
    private final Timeline barAnimation;
    private final Timeline recAnimation;
    private final Timeline rootRecSizeAnimation;
    private final Timeline rootRecPositionAnimation;
    private final Timeline splitDivAnimation;
    private ParallelTransition contentTransition;
    private Node currentContent;
    private ParallelTransition rootRecTransition;
    private BDTabDir currentDir;

    protected BDTabItemSkin(BDTabItem bdTabItem) {

        // 初始化UI组件
        splitPane = new SplitPane();
        body = new VBox();
        rootRec = new Rectangle();
        bar = new ScrollBar();
        header = new HBox();
        tabBox = new HBox();
        rightBox = new HBox();
        rootPane = new AnchorPane();
        contentPane = new StackPane();
        tabContent = new ScrollPane();
        tabContentPane = new AnchorPane();
        leftBox = new HBox();
        foldButton = new BDButton();
        rectangle = new Rectangle();
        tabsBack = new Rectangle();

        // 初始化动画
        barAnimation = new Timeline();
        recAnimation = new Timeline();
        splitDivAnimation = new Timeline();

        rootRecSizeAnimation = new Timeline();
        rootRecPositionAnimation = new Timeline();
        super(bdTabItem);
    }

    @Override
    public void initEvent() {
        KeyCombination close = KeyCombination.keyCombination(CTRL_F4_SHORTCUT);
        mapping.addEventHandler(foldButton, ActionEvent.ACTION, _ -> {
                    List<BDTab>[] invisibleTabs = getInvisibleTabs();
                    List<BDTab> leftInvisibleTabs = invisibleTabs[0];
                    List<BDTab> rightInvisibleTabs = invisibleTabs[1];

                    Popup popup = new Popup();

                    VBox root = new VBox();
                    root.getStyleClass().add("bd-tab-popup");

                    if (!leftInvisibleTabs.isEmpty())
                        leftInvisibleTabs.forEach(e -> {
                            Node node = e.cloneNode(popup);
                            root.getChildren().add(node);
                            node.setOnMouseClicked(_ -> {
                                e.show();
                                popup.hide();
                            });
                        });
                    if (!rightInvisibleTabs.isEmpty()) {
                        if (!leftInvisibleTabs.isEmpty()) {
                            Separator e = new Separator();
                            e.setOrientation(Orientation.HORIZONTAL);
                            root.getChildren().add(e);
                        }
                        rightInvisibleTabs.forEach(e -> {
                            Node node = e.cloneNode(popup);
                            root.getChildren().add(node);
                            node.setOnMouseClicked(_ -> {
                                e.show();
                                popup.hide();
                            });
                        });
                    }

                    popup.getContent().add(root);
                    popup.setAutoHide(true);
                    popup.setHideOnEscape(true);
                    popup.getScene().getStylesheets().addAll(control.getScene().getWindow().getScene().getStylesheets());
                    Bounds bounds = foldButton.localToScreen(foldButton.getLayoutBounds());
                    popup.show(control.getScene().getWindow(), bounds.getMinX() - popup.getWidth() - bounds.getWidth() / 2, bounds.getMaxY());
                })
                .addEventHandler(tabContentPane, ScrollEvent.SCROLL, event1 -> {
                    int delta = (int) (event1.getDeltaY() / 32);
                    double newValue = bar.getValue() - delta * SCROLL_SPEED;
                    newValue = Math.max(bar.getMin(), Math.min(bar.getMax(), newValue));
                    animateScroll(newValue);
                    event1.consume();
                }).addEventFilter(control, KeyEvent.KEY_PRESSED, event -> {
                    if (close.match(event) && control.getShowTab() instanceof BDTab tab)
                        tab.close();
                }).addEventFilter(tabBox, DragEvent.DRAG_ENTERED, event -> {
                    if (dragTab == null || !control.acceptDrag(dragTab) || control.getTabs().isEmpty()) return;
                    event.consume();
                    tabsBack.setHeight(dragTab.getHeight());
                    tabsBack.setWidth(dragTab.getWidth());
                }).addEventFilter(tabBox, DragEvent.DRAG_OVER, event -> {
                    if (dragTab == null || !control.acceptDrag(dragTab) || control.getTabs().isEmpty()) return;
                    event.consume();
                    BDTab tab = Util.searchEventTargetNode(event.getTarget(), BDTab.class);
                    event.acceptTransferModes(TransferMode.MOVE);
                    if (tab != null) {
                        Bounds bounds = tab.localToScene(tab.getLayoutBounds());
                        tabBox.getChildren().remove(tabsBack);
                        int var1 = tabBox.getChildren().indexOf(tab);
                        if (event.getSceneX() <= bounds.getCenterX())
                            tabBox.getChildren().add(var1, tabsBack);
                        else
                            tabBox.getChildren().add(var1 + 1, tabsBack);
                    } else if (!Objects.equals(tabsBack, Util.searchEventTargetNode(event.getTarget(), Rectangle.class))) {
                        tabBox.getChildren().remove(tabsBack);
                        tabBox.getChildren().add(tabsBack);
                    }
                })
                .addEventFilter(tabBox, DragEvent.DRAG_EXITED, event -> {
                    if (dragTab == null || !control.acceptDrag(dragTab) || control.getTabs().isEmpty()) return;
                    event.consume();
                    tabBox.getChildren().remove(tabsBack);
                })
                .addEventFilter(tabBox, DragEvent.DRAG_DROPPED, event -> {
                    if (dragTab == null || !control.acceptDrag(dragTab) || control.getTabs().isEmpty()) return;
                    event.consume();
                    control.addTab(tabBox.getChildren().indexOf(tabsBack), dragTab);
                    dragTab.show();
                })
                .addEventFilter(contentPane, DragEvent.DRAG_ENTERED, event -> {
                    if (dragTab == null || !control.acceptDrag(dragTab) || control.getTabs().isEmpty()) return;
                    event.consume();
                    event.acceptTransferModes(TransferMode.MOVE);
                    Bounds bounds = contentPane.localToScene(contentPane.getLayoutBounds());
                    double left = event.getSceneX() - bounds.getMinX();
                    double top = event.getSceneY() - bounds.getMinY();
                    rootRec.setLayoutX(left);
                    rootRec.setLayoutY(top);
                })
                .addEventFilter(contentPane, DragEvent.DRAG_OVER, event -> {
                    if (dragTab == null || !control.acceptDrag(dragTab) || control.getTabs().isEmpty()) return;
                    event.consume();
                    event.acceptTransferModes(TransferMode.MOVE);
                    BDTabDir dir = getSplitDir(event);
                    animationRootRec(dir, event);
                })
                .addEventFilter(contentPane, DragEvent.DRAG_EXITED, event -> {
                    if (dragTab == null || !control.acceptDrag(dragTab) || control.getTabs().isEmpty()) return;
                    event.consume();
                    animationRootRec(null, event);
                })
                .addEventFilter(contentPane, DragEvent.DRAG_DROPPED, event -> {
                    if (dragTab == null || !control.acceptDrag(dragTab)) return;
                    event.consume();
                    BDTabDir dir = getSplitDir(event);
                    switch (dir) {
                        case TOP, BOTTOM, LEFT, RIGHT -> control.addItem(dir, dragTab);
                        case CENTER ->
                                control.addTab(control.getShowTab() != null ? control.getTabs().indexOf(control.getShowTab()) + 1 : 0, dragTab);
                    }
                    dragTab.show();
                    animationRootRec(null, event);
                }).addEventFilter(rootPane, DragEvent.DRAG_ENTERED, event -> {
                    if (dragTab == null || !control.acceptDrag(dragTab) || !control.getTabs().isEmpty()) return;
                    event.consume();
                    event.acceptTransferModes(TransferMode.MOVE);
                    Bounds bounds = contentPane.localToScene(contentPane.getLayoutBounds());
                    double left = event.getSceneX() - bounds.getMinX();
                    double top = event.getSceneY() - bounds.getMinY();
                    rootRec.setLayoutX(left);
                    rootRec.setLayoutY(top);
                    animationRootRec(rootPane.getWidth(), rootPane.getHeight(), 0, 0);
                }).addEventFilter(rootPane, DragEvent.DRAG_OVER, event -> {
                    if (dragTab == null || !control.acceptDrag(dragTab) || !control.getTabs().isEmpty()) return;
                    event.consume();
                    event.acceptTransferModes(TransferMode.MOVE);
                })
                .addEventFilter(rootPane, DragEvent.DRAG_EXITED, event -> {
                    if (dragTab == null || !control.acceptDrag(dragTab) || !control.getTabs().isEmpty()) return;
                    event.consume();
                    animationRootRec(null, event);
                })
                .addEventFilter(rootPane, DragEvent.DRAG_DROPPED, event -> {
                    if (dragTab == null || !control.acceptDrag(dragTab) || !control.getTabs().isEmpty()) return;
                    event.consume();
                    control.addTab(control.getShowTab() != null ? control.getTabs().indexOf(control.getShowTab()) + 1 : 0, dragTab);
                    dragTab.show();
                    animationRootRec(null, event);
                });
    }

    private BDTabDir getSplitDir(DragEvent event) {
        Bounds bounds = contentPane.localToScene(contentPane.getLayoutBounds());
        double left = event.getSceneX() - bounds.getMinX();
        double top = event.getSceneY() - bounds.getMinY();
        double right = bounds.getMaxX() - event.getSceneX();
        double bottom = bounds.getMaxY() - event.getSceneY();
        if (left > bounds.getWidth() / 4 && right > bounds.getWidth() / 4 && top > bounds.getHeight() / 4 && bottom > bounds.getHeight() / 4)
            return BDTabDir.CENTER;
        if (left < right && left < bottom && left < top) {
            return BDTabDir.LEFT;
        } else if (right < left && right < bottom && right < top) {
            return BDTabDir.RIGHT;
        } else if (top < left && top < right && top < bottom) {
            return BDTabDir.TOP;
        } else {
            return BDTabDir.BOTTOM;
        }
    }

    @Override
    public void initProperty() {
        BooleanProperty focusWithIn = Util.focusWithIn(control, mapping);
        mapping.binding(bar.visibleProperty(), tabBox.widthProperty().greaterThan(tabContent.widthProperty()))
                .bindProperty(bar.visibleProperty(), foldButton.visibleProperty(), foldButton.managedProperty())
                .addListener(() -> rectangle.pseudoClassStateChanged(RECTANGLE_FOCUSED_PSEUDO_CLASS, focusWithIn.get()), true, focusWithIn)
                .addListener(control.showTab, (_, _, newTab) -> initShowTab(newTab))
                .addListener(control.tabsProperty(), (_, _, _) -> initShowTab(control.getShowTab()))
                .addListener(bar.valueProperty(), (_, _, newValue) -> tabContent.setHvalue(newValue.doubleValue()))
                .addListener(() -> {
                            double tabContentWidth = tabContent.getWidth();
                            double tabBoxWidth = tabBox.getWidth();

                            // 计算滚动条的最大值（可滚动的距离）
                            double max = Math.max(0, tabBoxWidth - tabContentWidth);
                            bar.setMin(0);
                            bar.setMax(max);
                            bar.setValue(Math.min(max, bar.getValue()));
                            bar.setVisibleAmount(Math.min(100, max / SCROLL_FACTOR));

                            // 更新 ScrollPane 的滚动参数
                            tabContent.setHmax(max);
                            tabContent.setHmin(0);
                            tabContent.setHvalue(bar.getValue());
                        }, true,
                        tabContent.widthProperty(),
                        tabBox.widthProperty())
                .addListener(() -> {
                    BDTabItemChild child = control.getChild();
                    if (child == null) {
                        splitPane.getItems().setAll(rootPane);
                        return;
                    }
                    splitPane.getItems().setAll(child.first(), child.second());
                }, true, control.childProperty())
                .addListener(() -> {
                    splitPane.setOrientation(control.getOrientation());
                    tabBox.getChildren().setAll(control.getTabs());
                }, true, (Observable) control.tabsProperty(), control.orientation)
                .addListener(header.hoverProperty(), (_, _, nv) -> bar.pseudoClassStateChanged(BAR_HOVER_PSEUDO_CLASS, nv));

        if (control.dirAnimation) {
            tabBox.setBackground(Background.fill(Color.RED));
            if (control.tempDir == BDTabDir.LEFT || control.tempDir == BDTabDir.TOP)
                splitPane.setDividerPositions(0);
            else splitPane.setDividerPositions(1);
            animationDividers();
        }
    }

    private void animationDividers() {
        splitDivAnimation.stop();
        if (splitPane.getItems().size() > 1) {
            KeyFrame keyFrame = new KeyFrame(ANIMATION_DURATION, new KeyValue(splitPane.getDividers().getFirst().positionProperty(), 0.5));
            splitDivAnimation.getKeyFrames().setAll(keyFrame);
            splitDivAnimation.play();
        }
    }

    private void initShowTab(BDTab newTab) {
        if (!tabBox.getChildren().contains(newTab))
            tabBox.getChildren().setAll(control.getTabs());
        if (newTab != null) {
            tabBox.layout();
            tabBox.requestLayout();
            tabBox.applyCss();

            // 使用动画切换内容
            transitionContent(newTab.getContent());
            animationMove(newTab.getLayoutX(), newTab.getWidth());
            double layoutX = newTab.getLayoutX();
            double width = newTab.getWidth();
            double scrollValue = bar.getValue();
            double visibleWidth = tabContent.getWidth();

            // 如果tab的左边被遮挡
            if (layoutX < scrollValue) {
                animateScroll(layoutX);
            }
            // 如果tab的右边被遮挡
            else if (layoutX + width > scrollValue + visibleWidth) {
                animateScroll(layoutX + width - visibleWidth);
            }
        } else {
            // 清空内容时也使用动画
            transitionContent(null);
            animationMove(0, 0);
        }
    }

    @Override
    public void initUI() {
        // 设置样式类
        setupStyleClasses();

        // 配置UI结构
        rootPane.getChildren().addAll(body, rootRec);
        rootPane.setMinSize(0, 0);
        rootRec.setMouseTransparent(true);

        body.getChildren().addAll(bar, header, contentPane);

        VBox.setVgrow(contentPane, Priority.ALWAYS);
        contentPane.setMinSize(0, 0);
        AnchorPane.setTopAnchor(body, 0.0);
        AnchorPane.setBottomAnchor(body, 0.0);
        AnchorPane.setLeftAnchor(body, 0.0);
        AnchorPane.setRightAnchor(body, 0.0);

        // 配置滚动条
        bar.setOrientation(Orientation.HORIZONTAL);

        header.getChildren().addAll(leftBox, tabContent, rightBox);
        foldButton.setTooltip(new Tooltip("显示隐藏的标签页"));
        foldButton.setGraphic(Util.getImageView(15, BDIcon.FOLD));
        foldButton.setSelectable(false);
        rightBox.getChildren().setAll(foldButton);

        // 配置滚动区域
        tabContent.setContent(tabContentPane);
        tabContent.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tabContent.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        HBox.setHgrow(tabContent, Priority.ALWAYS);
        tabContent.setMinHeight(Region.USE_PREF_SIZE);
        tabContent.setMaxHeight(Region.USE_PREF_SIZE);

        tabContentPane.prefHeightProperty().bind(tabContent.heightProperty());
        tabContentPane.getChildren().addAll(tabBox, rectangle);

        // 设置锚点约束
        AnchorPane.setLeftAnchor(tabBox, 0.0);
        AnchorPane.setRightAnchor(tabBox, 0.0);
        AnchorPane.setTopAnchor(tabBox, 0.0);
        AnchorPane.setBottomAnchor(tabBox, 0.0);
        AnchorPane.setBottomAnchor(rectangle, 0.0);

        // 添加到根节点
        getChildren().setAll(splitPane);

        // 初始化显示的tab
        new Timeline(new KeyFrame(ANIMATION_DURATION,_->initShowTab(control.getShowTab()))).play();

    }

    /**
     * 设置样式类
     */
    private void setupStyleClasses() {
        control.getStyleClass().add("bd-tab-item");
        splitPane.getStyleClass().add("bd-tab-item-container");
        body.getStyleClass().add("bd-tab-item-body");
        rootRec.getStyleClass().add("bd-tab-item-root-rec");
        bar.getStyleClass().add("bd-tab-item-bar");
        header.getStyleClass().add("bd-tab-item-header");
        leftBox.getStyleClass().add("bd-tab-item-left");
        tabContent.getStyleClass().add("bd-tab-item-tabcontent");
        tabContentPane.getStyleClass().add("bd-tab-item-content-pane");
        tabBox.getStyleClass().add("bd-tab-item-tabbox");
        rightBox.getStyleClass().add("bd-tab-item-right");
        foldButton.getStyleClass().add("bd-tab-item-fold-button");
        rectangle.getStyleClass().add("bd-tab-item-rectangle");
        tabsBack.getStyleClass().add("bd-tab-item-tabs-back");

        rectangle.setHeight(RECTANGLE_HEIGHT);

        // 设置内容容器的背景和样式
        contentPane.getStyleClass().add("bd-tab-item-content");
    }

    /**
     * 获取tabBox中左边和右边未显示完全的tab列表
     *
     * @return 包含两个列表的数组，第一个是左边未显示的tab列表，第二个是右边未显示的tab列表
     */
    public List<BDTab>[] getInvisibleTabs() {
        List<BDTab> leftInvisibleTabs = new ArrayList<>();
        List<BDTab> rightInvisibleTabs = new ArrayList<>();

        // 获取滚动条的当前值
        double scrollValue = bar.getValue();
        // 获取可见区域的宽度
        double visibleWidth = tabContent.getWidth();
        // 计算可见区域的左右边界（相对于tabBox的坐标系）
        double visibleLeft = scrollValue;
        double visibleRight = scrollValue + visibleWidth;

        // 遍历tabBox中的所有tab
        for (Node node : tabBox.getChildren()) {
            if (node instanceof BDTab tab) {
                // 获取tab在tabBox中的位置和尺寸
                double tabLeft = tab.getLayoutX();
                double tabRight = tabLeft + tab.getWidth();

                // 判断tab是否完全显示
                boolean isFullyVisible = tabLeft >= visibleLeft && tabRight <= visibleRight;

                if (!isFullyVisible) {
                    // 如果tab右边被遮挡
                    if (tabRight > visibleRight) {
                        rightInvisibleTabs.add(tab);
                    }
                    // 如果tab左边被遮挡
                    if (tabLeft < visibleLeft) {
                        leftInvisibleTabs.add(tab);
                    }
                    // 注意：一个tab可能同时左右都被遮挡（如果它非常宽），这种情况我们两边都会添加
                }
            }
        }

        // 返回两个列表
        @SuppressWarnings("unchecked")
        List<BDTab>[] result = new List[2];
        result[0] = leftInvisibleTabs;
        result[1] = rightInvisibleTabs;
        return result;
    }

    private void animationRootRec(BDTabDir dir, DragEvent event) {
        if (Objects.equals(currentDir, dir) && dir != null) return;
        currentDir = dir;
        double width = 0;
        double height = 0;
        double x;
        double y;
        switch (dir) {
            case LEFT:
                width = contentPane.getWidth() / 2;
                height = contentPane.getHeight();
                x = 0;
                y = 0;
                break;
            case RIGHT:
                width = contentPane.getWidth() / 2;
                height = contentPane.getHeight();
                x = contentPane.getWidth() / 2;
                y = 0;
                break;
            case TOP:
                width = contentPane.getWidth();
                height = contentPane.getHeight() / 2;
                x = 0;
                y = 0;
                break;
            case CENTER:
                width = contentPane.getWidth();
                height = contentPane.getHeight();
                x = 0;
                y = 0;
                break;
            case BOTTOM:
                width = contentPane.getWidth();
                height = contentPane.getHeight() / 2;
                x = 0;
                y = contentPane.getHeight() / 2;
                break;
            case null:
                Bounds bounds = contentPane.localToScene(contentPane.getLayoutBounds());
                double left = event.getSceneX() - bounds.getMinX();
                double top = event.getSceneY() - bounds.getMinY();
                x = left;
                y = top;
                break;
        }
        animationRootRec(width, height, x, y + contentPane.localToParent(contentPane.getLayoutBounds()).getMinY());
    }

    private void animationRootRec(double width, double height, double x, double y) {
        if (rootRecTransition != null) {
            rootRecTransition.stop();
            rootRecTransition = null;
        }
        KeyFrame size = new KeyFrame(ANIMATION_DURATION, new KeyValue(rootRec.widthProperty(), width, Interpolator.LINEAR), new KeyValue(rootRec.heightProperty(), height, Interpolator.LINEAR));
        KeyFrame position = new KeyFrame(ANIMATION_DURATION, new KeyValue(rootRec.layoutXProperty(), x, Interpolator.LINEAR), new KeyValue(rootRec.layoutYProperty(), y, Interpolator.LINEAR));
        rootRecSizeAnimation.getKeyFrames().setAll(size);
        rootRecPositionAnimation.getKeyFrames().setAll(position);
        rootRecTransition = new ParallelTransition(rootRecSizeAnimation, rootRecPositionAnimation);
        rootRecTransition.play();
    }

    /**
     * 执行滚动动画
     */
    private void animateScroll(double scrollValue) {
        Property<Number> property = bar.valueProperty();
        barAnimation.stop();
        KeyFrame keyFrame = new KeyFrame(ANIMATION_DURATION,
                new KeyValue(property, scrollValue, Interpolator.LINEAR));
        barAnimation.getKeyFrames().setAll(keyFrame);
        barAnimation.play();
    }

    /**
     * 执行矩形移动动画
     */
    private void animationMove(double layoutX, double width) {
        recAnimation.stop();
        KeyFrame keyFrame = new KeyFrame(ANIMATION_DURATION,
                new KeyValue(rectangle.layoutXProperty(), layoutX, Interpolator.LINEAR),
                new KeyValue(rectangle.widthProperty(), width, Interpolator.LINEAR));
        recAnimation.getKeyFrames().setAll(keyFrame);
        recAnimation.play();
    }

    /**
     * 切换内容的动画效果
     */
    private void transitionContent(Node newContent) {
        // 停止当前正在进行的动画
        if (contentTransition != null) {
            contentTransition.stop();
            contentTransition = null;
        }

        // 如果新内容和旧内容相同，不做动画
        if (currentContent == newContent) {
            return;
        }

        Node oldContent = currentContent;
        currentContent = newContent;

        // 根据不同的场景创建动画
        if (newContent == null) {
            contentTransition = new ParallelTransition(createOpacityTimeline(oldContent, 1.0, 0.0));
            contentTransition.setOnFinished(e -> contentPane.getChildren().clear());
            contentTransition.play();
        } else if (oldContent == null) {
            newContent.setOpacity(0);
            contentPane.getChildren().setAll(newContent);

            contentTransition = new ParallelTransition(createOpacityTimeline(newContent, 0.0, 1.0));
            contentTransition.play();
        } else {
            newContent.setOpacity(0);
            contentPane.getChildren().setAll(oldContent, newContent);

            Timeline oldFadeOut = createOpacityTimeline(oldContent, 1.0, 0.0);
            Timeline newFadeIn = createOpacityTimeline(newContent, 0.0, 1.0);

            contentTransition = new ParallelTransition(oldFadeOut, newFadeIn);
            contentTransition.setOnFinished(e -> contentPane.getChildren().remove(oldContent));
            contentTransition.play();
        }
    }

    /**
     * 创建透明度变化时间线
     */
    private Timeline createOpacityTimeline(Node node, double startValue, double endValue) {
        return new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(node.opacityProperty(), startValue, Interpolator.LINEAR)
                ),
                new KeyFrame(ANIMATION_DURATION,
                        new KeyValue(node.opacityProperty(), endValue, Interpolator.LINEAR)
                )
        );
    }

}