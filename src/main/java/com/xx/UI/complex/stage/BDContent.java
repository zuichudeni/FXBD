package com.xx.UI.complex.stage;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.LazyValue;
import javafx.animation.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/*
 * BDStage 的内容区域
 * */
public class BDContent extends BDControl {
    private static final Duration ANIMATION_DURATION = Duration.millis(200);
    static BDSideBarItem DRAG_ITEM;
    final Pane centerPane = new StackPane();
    final SplitPane rightSplitPane = new SplitPane();
    final SplitPane bottomSplitPane = new SplitPane();
    final SplitPane leftSplitPane = new SplitPane();
    final SplitPane horizontalSplitPane = new SplitPane(centerPane);
    final SplitPane verticalSplitPane = new SplitPane(horizontalSplitPane);
    //  背景
    final Rectangle splitBack = new Rectangle();
    //  Tooltip
    final Text text = new Text();
    final Pane tooltip = new StackPane(text);
    final Pane horizontalRootPane = new AnchorPane(verticalSplitPane, splitBack, tooltip);
    final BorderPane borderPane = new BorderPane((horizontalRootPane));
    final LazyValue<BDSidebar> topSideBar = new LazyValue<>(() -> {
        BDSidebar topSidebar = new BDSidebar(this, BDDirection.TOP);
        borderPane.setTop(topSidebar);
        return topSidebar;
    });
    final LazyValue<BDSidebar> bottomSideBar = new LazyValue<>(() -> {
        BDSidebar bottomSidebar = new BDSidebar(this, BDDirection.BOTTOM);
        borderPane.setBottom(bottomSidebar);
        return bottomSidebar;
    });
    final LazyValue<BDSidebar> leftSideBar = new LazyValue<>(() -> {
        BDSidebar leftSidebar = new BDSidebar(this, BDDirection.LEFT);
        borderPane.setLeft(leftSidebar);
        return leftSidebar;
    });
    final LazyValue<BDSidebar> rightSideBar = new LazyValue<>(() -> {
        BDSidebar rightSidebar = new BDSidebar(this, BDDirection.RIGHT);
        borderPane.setRight(rightSidebar);
        return rightSidebar;
    });
    final Timeline splitBackAnimation = new Timeline();
    private final SimpleObjectProperty<Node> content = new SimpleObjectProperty<>();
    // 动画控制属性
    private final BooleanProperty animationEnabled = new SimpleBooleanProperty(true);
    boolean rightPaneShow = false;
    double rightPaneDivider = 0.5;
    boolean bottomPaneShow = false;
    double bottomPaneDivider = 0.5;
    boolean leftPaneShow = false;
    double leftPaneDivider = 0.5;
    boolean leftShow = false;
    double leftDivider = 0.2;
    boolean rightShow = false;
    double rightDivider = 0.8;
    boolean bottomShow = false;
    double bottomDivider = 0.8;
    BDSidebar.BDDragData tempDragData;
    // 添加动画控制器，防止快速点击导致动画冲突
    private Timeline bottomAnimation;
    private Timeline leftAnimation;
    private Timeline rightAnimation;

    public Node getContent() {
        return content.get();
    }

    public void setContent(Node content) {
        this.content.set(content);
    }

    public SimpleObjectProperty<Node> contentProperty() {
        return content;
    }

    // 新增：动画控制属性的getter/setter
    public boolean isAnimationEnabled() {
        return animationEnabled.get();
    }

    public void setAnimationEnabled(boolean enabled) {
        animationEnabled.set(enabled);
    }

    public BooleanProperty animationEnabledProperty() {
        return animationEnabled;
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDContentSkin(this);
    }

    public boolean acceptDragItem(BDSideBarItem item) {
        return true;
    }

    public boolean acceptExitItem(BDSideBarItem item) {
        return true;
    }

    public boolean acceptDragDropped(BDSideBarItem item) {
        return true;
    }

    final void changeSplitBack(BDSidebar.BDDragData dragData, DragEvent event) {
        dragHove(dragData, event.getSceneX(), event.getSceneY());
        if (dragData == null) {
            stopSplitBackAnimation();
            return;
        }
        if (tempDragData != null && tempDragData.getDirection().equals(dragData.getDirection()) && tempDragData.getInSequence().equals(dragData.getInSequence()))
            return;
        tempDragData = dragData;
        splitBackAnimation.stop();
        double layoutX = 0;
        double layoutY = 0;
        double height = 0;
        double width = 0;
        switch (dragData.getDirection()) {
            case LEFT -> {
                layoutX = 0;
                layoutY = dragData.getInSequence().equals(BDInSequence.FRONT) ? 0 : horizontalRootPane.getHeight() * leftPaneDivider;
                width = horizontalRootPane.getWidth() * leftDivider;
                height = dragData.getInSequence().equals(BDInSequence.FRONT) ? horizontalRootPane.getHeight() * leftPaneDivider : horizontalRootPane.getHeight() * (1 - leftPaneDivider);
            }
            case RIGHT -> {
                layoutX = horizontalRootPane.getWidth() * rightDivider;
                layoutY = dragData.getInSequence().equals(BDInSequence.FRONT) ? 0 : horizontalRootPane.getHeight() * rightPaneDivider;
                width = horizontalRootPane.getWidth() * (1 - rightDivider);
                height = dragData.getInSequence().equals(BDInSequence.FRONT) ? horizontalRootPane.getHeight() * rightPaneDivider : horizontalRootPane.getHeight() * (1 - rightPaneDivider);

            }
            case TOP -> throw new IllegalArgumentException("DragData 不能为 TOP");
            case BOTTOM -> {
                layoutX = dragData.getInSequence().equals(BDInSequence.FRONT) ? 0 : horizontalRootPane.getWidth() * bottomPaneDivider;
                layoutY = horizontalRootPane.getHeight() * bottomDivider;
                width = dragData.getInSequence().equals(BDInSequence.FRONT) ? horizontalRootPane.getWidth() * bottomPaneDivider : horizontalRootPane.getWidth() * (1 - bottomPaneDivider);
                height = horizontalRootPane.getHeight() * (1 - bottomDivider);
            }
        }
        KeyFrame o = new KeyFrame(ANIMATION_DURATION, new KeyValue(splitBack.opacityProperty(), 1, Interpolator.LINEAR));
        KeyFrame x = new KeyFrame(ANIMATION_DURATION, new KeyValue(splitBack.layoutXProperty(), layoutX, Interpolator.LINEAR));
        KeyFrame y = new KeyFrame(ANIMATION_DURATION, new KeyValue(splitBack.layoutYProperty(), layoutY, Interpolator.LINEAR));
        KeyFrame h = new KeyFrame(ANIMATION_DURATION, new KeyValue(splitBack.heightProperty(), height, Interpolator.LINEAR));
        KeyFrame w = new KeyFrame(ANIMATION_DURATION, new KeyValue(splitBack.widthProperty(), width, Interpolator.LINEAR));
        splitBackAnimation.getKeyFrames().setAll(o, x, y, h, w);
        splitBackAnimation.play();

    }

    // 停止指定方向的所有动画
    private boolean stopAnimation(BDDirection direction) {
        return switch (direction) {
            case BOTTOM -> {
                if (bottomAnimation != null) {
                    Animation.Status status = bottomAnimation.getStatus();
                    if (bottomAnimation.getOnFinished() != null)
                        bottomAnimation.getOnFinished().handle(new ActionEvent());
                    bottomAnimation.stop();
                    bottomAnimation = null;
                    yield status == Animation.Status.RUNNING;
                }
                yield false;
            }
            case LEFT -> {
                if (leftAnimation != null) {
                    Animation.Status status = leftAnimation.getStatus();
                    if (leftAnimation.getOnFinished() != null)
                        leftAnimation.getOnFinished().handle(new ActionEvent());
                    leftAnimation.stop();
                    leftAnimation = null;
                    yield status == Animation.Status.RUNNING;
                }
                yield false;
            }
            case RIGHT -> {
                if (rightAnimation != null) {
                    Animation.Status status = rightAnimation.getStatus();
                    if (rightAnimation.getOnFinished() != null)
                        rightAnimation.getOnFinished().handle(new ActionEvent());
                    rightAnimation.stop();
                    rightAnimation = null;
                    yield status == Animation.Status.RUNNING;
                }
                yield false;
            }
            case null -> false;
            case TOP -> false;
        };
    }

    // 辅助方法：直接设置分隔条位置而不使用动画
    private void setDividerPositionWithoutAnimation(SplitPane.Divider divider, double position) {
        if (divider != null) {
            divider.setPosition(position);
        }
    }

    final void showSidebar(BDSideBarItem item) {
        BDInSequence inSequence = item.getInSequence();
        Node contentNode = item.getSideContent();
        BDDirection direction = item.getDirection();

        // 停止当前方向的动画
        stopAnimation(direction);

        switch (direction) {
            case BOTTOM -> {
                if (verticalSplitPane.getItems().contains(bottomSplitPane)) {
                    if (!bottomSplitPane.getItems().contains(contentNode)) {
                        if (inSequence.equals(BDInSequence.FRONT))
                            bottomSplitPane.getItems().addFirst(contentNode);
                        else
                            bottomSplitPane.getItems().addLast(contentNode);
                    }

                    if (!bottomSplitPane.getDividers().isEmpty()) {
                        bottomSplitPane.getDividers().getFirst().setPosition(inSequence.equals(BDInSequence.FRONT) ? 0 : 1);

                        if (isAnimationEnabled() && item.tempAnimation) {
                            bottomAnimation = new Timeline(
                                    new KeyFrame(ANIMATION_DURATION,
                                            new KeyValue(bottomSplitPane.getDividers().getFirst().positionProperty(), bottomPaneDivider))
                            );
                            bottomAnimation.play();
                        } else {
                            setDividerPositionWithoutAnimation(bottomSplitPane.getDividers().getFirst(), bottomPaneDivider);
                        }
                        bottomPaneShow = true;
                    }
                } else {
                    bottomSplitPane.getItems().clear();
                    bottomSplitPane.getItems().add(contentNode);
                    verticalSplitPane.getItems().addLast(bottomSplitPane);

                    verticalSplitPane.getDividers().getLast().setPosition(1);

                    if (isAnimationEnabled() && item.tempAnimation) {
                        bottomAnimation = new Timeline(
                                new KeyFrame(ANIMATION_DURATION,
                                        new KeyValue(verticalSplitPane.getDividers().getLast().positionProperty(), bottomDivider))
                        );
                        bottomAnimation.play();
                    } else {
                        setDividerPositionWithoutAnimation(verticalSplitPane.getDividers().getLast(), bottomDivider);
                    }
                    bottomShow = true;
                }
            }
            case LEFT -> {
                if (horizontalSplitPane.getItems().contains(leftSplitPane)) {
                    if (!leftSplitPane.getItems().contains(contentNode)) {
                        if (inSequence.equals(BDInSequence.FRONT))
                            leftSplitPane.getItems().addFirst(contentNode);
                        else
                            leftSplitPane.getItems().addLast(contentNode);
                    }

                    if (!leftSplitPane.getDividers().isEmpty()) {
                        leftSplitPane.getDividers().getFirst().setPosition(inSequence.equals(BDInSequence.FRONT) ? 0 : 1);

                        if (isAnimationEnabled() && item.tempAnimation) {
                            leftAnimation = new Timeline(
                                    new KeyFrame(ANIMATION_DURATION,
                                            new KeyValue(leftSplitPane.getDividers().getFirst().positionProperty(), leftPaneDivider))
                            );
                            leftAnimation.play();
                        } else {
                            setDividerPositionWithoutAnimation(leftSplitPane.getDividers().getFirst(), leftPaneDivider);
                        }
                        leftPaneShow = true;
                    }
                } else {
                    // 保存水平SplitPane的分隔条位置
                    double[] savedPositions = horizontalSplitPane.getDividerPositions();

                    leftSplitPane.getItems().clear();
                    leftSplitPane.getItems().add(contentNode);
                    horizontalSplitPane.getItems().addFirst(leftSplitPane);

                    double[] positions = new double[horizontalSplitPane.getDividers().size() + 1];
                    positions[0] = 0;
                    System.arraycopy(savedPositions, 0, positions, 1, savedPositions.length);
                    horizontalSplitPane.setDividerPositions(positions);

                    if (isAnimationEnabled() && item.tempAnimation) {
                        leftAnimation = new Timeline(
                                new KeyFrame(ANIMATION_DURATION,
                                        new KeyValue(horizontalSplitPane.getDividers().getFirst().positionProperty(), leftDivider))
                        );
                        leftAnimation.play();
                    } else {
                        setDividerPositionWithoutAnimation(horizontalSplitPane.getDividers().getFirst(), leftDivider);
                    }
                    leftShow = true;
                }
            }
            case RIGHT -> {
                if (horizontalSplitPane.getItems().contains(rightSplitPane)) {
                    if (!rightSplitPane.getItems().contains(contentNode)) {
                        if (inSequence.equals(BDInSequence.FRONT)) rightSplitPane.getItems().addFirst(contentNode);
                        else rightSplitPane.getItems().addLast(contentNode);
                    }

                    if (!rightSplitPane.getDividers().isEmpty()) {
                        rightSplitPane.getDividers().getFirst().setPosition(inSequence.equals(BDInSequence.FRONT) ? 0 : 1);

                        if (isAnimationEnabled() && item.tempAnimation) {
                            rightAnimation = new Timeline(
                                    new KeyFrame(ANIMATION_DURATION,
                                            new KeyValue(rightSplitPane.getDividers().getFirst().positionProperty(), rightPaneDivider))
                            );
                            rightAnimation.play();
                        } else {
                            setDividerPositionWithoutAnimation(rightSplitPane.getDividers().getFirst(), rightPaneDivider);
                        }
                        rightPaneShow = true;
                    }
                } else {
                    rightSplitPane.getItems().clear();
                    rightSplitPane.getItems().add(contentNode);
                    horizontalSplitPane.getItems().addLast(rightSplitPane);
                    horizontalSplitPane.getDividers().getLast().setPosition(1);

                    if (isAnimationEnabled() && item.tempAnimation) {
                        rightAnimation = new Timeline(
                                new KeyFrame(ANIMATION_DURATION,
                                        new KeyValue(horizontalSplitPane.getDividers().getLast().positionProperty(), rightDivider))
                        );
                        rightAnimation.play();
                    } else {
                        setDividerPositionWithoutAnimation(horizontalSplitPane.getDividers().getLast(), rightDivider);
                    }
                    rightShow = true;
                }
            }
        }
    }

    final void closeSidebar(BDSideBarItem item) {
        Node contentNode = item.getSideContent();
        BDDirection direction = item.getDirection();

        // 停止当前方向的动画
        boolean stopAnimation = !stopAnimation(direction);

        switch (direction) {
            case BOTTOM -> {
                if (!bottomSplitPane.getItems().contains(contentNode)) return;

                boolean hasMultipleItems = bottomSplitPane.getItems().size() > 1;

                if (hasMultipleItems) {
                    if (stopAnimation) {
                        bottomPaneDivider = bottomSplitPane.getDividers().getFirst().getPosition();
                        bottomPaneShow = false;
                    }

                    if (isAnimationEnabled() && item.tempAnimation) {
                        bottomAnimation = new Timeline(
                                new KeyFrame(ANIMATION_DURATION,
                                        new KeyValue(bottomSplitPane.getDividers().getFirst().positionProperty(), item.getInSequence().equals(BDInSequence.FRONT) ? 0 : 1)));
                        bottomAnimation.setOnFinished(_ -> bottomSplitPane.getItems().remove(contentNode));
                        bottomAnimation.play();
                    } else {
                        setDividerPositionWithoutAnimation(bottomSplitPane.getDividers().getFirst(),
                                item.getInSequence().equals(BDInSequence.FRONT) ? 0 : 1);
                        bottomSplitPane.getItems().remove(contentNode);
                    }
                } else {
                    // 保存垂直SplitPane的分隔条位置（除了最后一个）
                    double[] savedPositions = new double[verticalSplitPane.getDividers().size() - 1];
                    for (int i = 0; i < verticalSplitPane.getDividers().size() - 1; i++) {
                        savedPositions[i] = verticalSplitPane.getDividers().get(i).getPosition();
                    }

                    if (!verticalSplitPane.getDividers().isEmpty()) {
                        int lastDividerIndex = verticalSplitPane.getDividers().size() - 1;
                        bottomDivider = verticalSplitPane.getDividers().get(lastDividerIndex).getPosition();
                        bottomShow = false;

                        if (isAnimationEnabled() && item.tempAnimation) {
                            bottomAnimation = new Timeline(
                                    new KeyFrame(ANIMATION_DURATION,
                                            new KeyValue(verticalSplitPane.getDividers().get(lastDividerIndex).positionProperty(), 1))
                            );
                            bottomAnimation.setOnFinished(_ -> {
                                bottomSplitPane.getItems().remove(contentNode);
                                verticalSplitPane.getItems().remove(bottomSplitPane);

                                // 恢复其他分隔条的位置
                                for (int i = 0; i < Math.min(verticalSplitPane.getDividers().size(), savedPositions.length); i++) {
                                    verticalSplitPane.getDividers().get(i).setPosition(savedPositions[i]);
                                }
                            });
                            bottomAnimation.play();
                        } else {
                            setDividerPositionWithoutAnimation(verticalSplitPane.getDividers().get(lastDividerIndex), 1);
                            bottomSplitPane.getItems().remove(contentNode);
                            verticalSplitPane.getItems().remove(bottomSplitPane);

                            // 恢复其他分隔条的位置
                            for (int i = 0; i < Math.min(verticalSplitPane.getDividers().size(), savedPositions.length); i++) {
                                verticalSplitPane.getDividers().get(i).setPosition(savedPositions[i]);
                            }
                        }
                    } else {
                        bottomSplitPane.getItems().remove(contentNode);
                        verticalSplitPane.getItems().remove(bottomSplitPane);
                    }
                }
            }
            case LEFT -> {
                if (!leftSplitPane.getItems().contains(contentNode)) return;
                boolean hasMultipleItems = leftSplitPane.getItems().size() > 1;

                if (hasMultipleItems) {
                    if (stopAnimation) {
                        leftPaneDivider = leftSplitPane.getDividers().getFirst().getPosition();
                        leftPaneShow = false;
                    }

                    if (isAnimationEnabled() && item.tempAnimation) {
                        leftAnimation = new Timeline(
                                new KeyFrame(ANIMATION_DURATION,
                                        new KeyValue(leftSplitPane.getDividers().getFirst().positionProperty(), item.getInSequence().equals(BDInSequence.FRONT) ? 0 : 1)));
                        leftAnimation.setOnFinished(_ -> leftSplitPane.getItems().remove(contentNode));
                        leftAnimation.play();
                    } else {
                        setDividerPositionWithoutAnimation(leftSplitPane.getDividers().getFirst(),
                                item.getInSequence().equals(BDInSequence.FRONT) ? 0 : 1);
                        leftSplitPane.getItems().remove(contentNode);
                    }
                } else {
                    // 保存水平SplitPane的分隔条位置（除了第一个）
                    double[] savedPositions = new double[horizontalSplitPane.getDividers().size() - 1];
                    for (int i = 1; i < horizontalSplitPane.getDividers().size(); i++) {
                        savedPositions[i - 1] = horizontalSplitPane.getDividers().get(i).getPosition();
                    }

                    if (!horizontalSplitPane.getDividers().isEmpty()) {
                        if (stopAnimation) {
                            leftDivider = horizontalSplitPane.getDividers().getFirst().getPosition();
                            leftShow = false;
                        }

                        if (isAnimationEnabled() && item.tempAnimation) {
                            leftAnimation = new Timeline(
                                    new KeyFrame(ANIMATION_DURATION,
                                            new KeyValue(horizontalSplitPane.getDividers().getFirst().positionProperty(), 0))
                            );
                            leftAnimation.setOnFinished(_ -> {
                                leftSplitPane.getItems().remove(contentNode);
                                horizontalSplitPane.getItems().remove(leftSplitPane);

                                // 恢复其他分隔条的位置
                                for (int i = 0; i < Math.min(horizontalSplitPane.getDividers().size(), savedPositions.length); i++) {
                                    horizontalSplitPane.getDividers().get(i).setPosition(savedPositions[i]);
                                }
                            });
                            leftAnimation.play();
                        } else {
                            setDividerPositionWithoutAnimation(horizontalSplitPane.getDividers().getFirst(), 0);
                            leftSplitPane.getItems().remove(contentNode);
                            horizontalSplitPane.getItems().remove(leftSplitPane);

                            // 恢复其他分隔条的位置
                            for (int i = 0; i < Math.min(horizontalSplitPane.getDividers().size(), savedPositions.length); i++) {
                                horizontalSplitPane.getDividers().get(i).setPosition(savedPositions[i]);
                            }
                        }
                    } else {
                        leftSplitPane.getItems().remove(contentNode);
                        horizontalSplitPane.getItems().remove(leftSplitPane);
                    }
                }
            }
            case RIGHT -> {
                if (!rightSplitPane.getItems().contains(contentNode)) return;

                boolean hasMultipleItems = rightSplitPane.getItems().size() > 1;

                if (hasMultipleItems) {
                    if (stopAnimation) {
                        rightPaneDivider = rightSplitPane.getDividers().getFirst().getPosition();
                        rightPaneShow = false;
                    }

                    if (isAnimationEnabled() && item.tempAnimation) {
                        rightAnimation = new Timeline(
                                new KeyFrame(ANIMATION_DURATION,
                                        new KeyValue(rightSplitPane.getDividers().getFirst().positionProperty(), item.getInSequence().equals(BDInSequence.FRONT) ? 0 : 1)));
                        rightAnimation.setOnFinished(_ -> rightSplitPane.getItems().remove(contentNode));
                        rightAnimation.play();
                    } else {
                        setDividerPositionWithoutAnimation(rightSplitPane.getDividers().getFirst(),
                                item.getInSequence().equals(BDInSequence.FRONT) ? 0 : 1);
                        rightSplitPane.getItems().remove(contentNode);
                    }
                } else {
                    // 保存水平SplitPane的分隔条位置（除了最后一个）
                    double[] savedPositions = new double[horizontalSplitPane.getDividers().size() - 1];
                    for (int i = 0; i < horizontalSplitPane.getDividers().size() - 1; i++) {
                        savedPositions[i] = horizontalSplitPane.getDividers().get(i).getPosition();
                    }

                    if (!horizontalSplitPane.getDividers().isEmpty()) {
                        int lastDividerIndex = horizontalSplitPane.getDividers().size() - 1;
                        rightDivider = horizontalSplitPane.getDividers().get(lastDividerIndex).getPosition();
                        rightShow = false;

                        if (isAnimationEnabled() && item.tempAnimation) {
                            rightAnimation = new Timeline(
                                    new KeyFrame(ANIMATION_DURATION,
                                            new KeyValue(horizontalSplitPane.getDividers().get(lastDividerIndex).positionProperty(), 1))
                            );
                            rightAnimation.setOnFinished(_ -> {
                                rightSplitPane.getItems().remove(contentNode);
                                horizontalSplitPane.getItems().remove(rightSplitPane);

                                // 恢复其他分隔条的位置
                                for (int i = 0; i < Math.min(horizontalSplitPane.getDividers().size(), savedPositions.length); i++) {
                                    horizontalSplitPane.getDividers().get(i).setPosition(savedPositions[i]);
                                }
                            });
                            rightAnimation.play();
                        } else {
                            setDividerPositionWithoutAnimation(horizontalSplitPane.getDividers().get(lastDividerIndex), 1);
                            rightSplitPane.getItems().remove(contentNode);
                            horizontalSplitPane.getItems().remove(rightSplitPane);

                            // 恢复其他分隔条的位置
                            for (int i = 0; i < Math.min(horizontalSplitPane.getDividers().size(), savedPositions.length); i++) {
                                horizontalSplitPane.getDividers().get(i).setPosition(savedPositions[i]);
                            }
                        }
                    } else {
                        rightSplitPane.getItems().remove(contentNode);
                        horizontalSplitPane.getItems().remove(rightSplitPane);
                    }
                }
            }
        }
    }

    void refreshDivider() {
        if (leftPaneShow)
            leftPaneDivider = leftSplitPane.getDividers().getFirst().getPosition();
        if (rightPaneShow)
            rightPaneDivider = rightSplitPane.getDividers().getFirst().getPosition();
        if (bottomPaneShow)
            bottomPaneDivider = bottomSplitPane.getDividers().getFirst().getPosition();
        if (leftShow)
            leftDivider = horizontalSplitPane.getDividers().getFirst().getPosition();
        if (rightShow)
            rightDivider = horizontalSplitPane.getDividers().getLast().getPosition();
        if (bottomShow)
            bottomDivider = verticalSplitPane.getDividers().getFirst().getPosition();
    }

    void hideAllItemBack() {
        topSideBar.applyIfNotNone(BDSidebar::hideItemBack);
        rightSideBar.applyIfNotNone(BDSidebar::hideItemBack);
        bottomSideBar.applyIfNotNone(BDSidebar::hideItemBack);
        leftSideBar.applyIfNotNone(BDSidebar::hideItemBack);
        stopSplitBackAnimation();
        hideToolTip();
    }

    void stopSplitBackAnimation() {
        splitBackAnimation.stop();
        KeyFrame o = new KeyFrame(ANIMATION_DURATION, new KeyValue(splitBack.opacityProperty(), 0, Interpolator.LINEAR));
        KeyFrame x = new KeyFrame(ANIMATION_DURATION, new KeyValue(splitBack.layoutXProperty(), horizontalSplitPane.getWidth() / 2, Interpolator.LINEAR));
        KeyFrame y = new KeyFrame(ANIMATION_DURATION, new KeyValue(splitBack.layoutYProperty(), horizontalSplitPane.getHeight() / 2, Interpolator.LINEAR));
        KeyFrame h = new KeyFrame(ANIMATION_DURATION, new KeyValue(splitBack.heightProperty(), 0, Interpolator.LINEAR));
        KeyFrame w = new KeyFrame(ANIMATION_DURATION, new KeyValue(splitBack.widthProperty(), 0, Interpolator.LINEAR));
        splitBackAnimation.getKeyFrames().setAll(o, x, y, h, w);
        splitBackAnimation.play();
        tempDragData = null;
    }

    private final PseudoClass TOOL_TIP_SHOW = PseudoClass.getPseudoClass("show");
    void hoverToolTipShow(BDSideBarItem item) {
        tooltip.pseudoClassStateChanged(TOOL_TIP_SHOW,true);
        text.setText(item.getName() + (item.getShortcutKey() == null ? "" :" " + item.getShortcutKey()));
        tooltip.layout();
        tooltip.applyCss();
        Bounds itemBounds = item.localToScene(item.getLayoutBounds());
        Bounds rootBounds = horizontalRootPane.localToScene(horizontalRootPane.getLayoutBounds());
        double tooltipWidth = tooltip.getWidth();
        double tooltipHeight = tooltip.getHeight();
        double layoutY = itemBounds.getMinY() - rootBounds.getMinY() - (tooltipHeight - itemBounds.getHeight()) / 2;
        double layoutX;
        if (item.getDirection().equals(BDDirection.LEFT))
            layoutX = 10;
        else if (item.getDirection().equals(BDDirection.RIGHT))
            layoutX = rootBounds.getWidth() - tooltipWidth - 10;
        else if (item.getInSequence().equals(BDInSequence.FRONT))
            layoutX = 10;
        else layoutX = rootBounds.getWidth() - tooltipWidth - 10;
        tooltip.setLayoutX(Math.min(layoutX, horizontalSplitPane.getWidth() - tooltipWidth));
        tooltip.setLayoutY(layoutY);
    }

    void dragHove(BDSidebar.BDDragData dragData, double scenex, double sceney) {
        Bounds rootBounds = horizontalRootPane.localToScene(horizontalRootPane.getLayoutBounds());
        sceney -= rootBounds.getMinY();
        scenex -= rootBounds.getMinX();
        if (dragData == null) {
            tooltip.setManaged(true);
            tooltip.setVisible(true);
            text.setText("窗口独立显示");
            tooltip.layout();
            tooltip.applyCss();
            tooltip.setLayoutX(scenex - tooltip.getWidth() / 2);
            tooltip.setLayoutY(sceney + DRAG_ITEM.getHeight() / 2 + 20);
            return;
        }
        String s = "移至";
        if (dragData.getDirection().equals(BDDirection.LEFT))
            s += " 左侧" + (dragData.inSequence.equals(BDInSequence.FRONT) ? " 顶部" : " 底部");
        else if (dragData.getDirection().equals(BDDirection.RIGHT))
            s += " 右侧" + (dragData.inSequence.equals(BDInSequence.FRONT) ? " 顶部" : " 底部");
        else s += (dragData.inSequence.equals(BDInSequence.FRONT) ? " 左侧" : " 右侧") + " 底部";
        tooltip.setVisible(true);
        tooltip.setManaged(true);
        text.setText(s);
        tooltip.layout();
        tooltip.applyCss();
        double layoutx;
        if (dragData.getDirection().equals(BDDirection.LEFT)) {
            layoutx = scenex + DRAG_ITEM.getWidth() / 2 + 10;
        } else if (dragData.getDirection().equals(BDDirection.RIGHT)) {
            layoutx = scenex - DRAG_ITEM.getWidth() / 2 - tooltip.getWidth() - 10;
        } else if (dragData.getInSequence().equals(BDInSequence.FRONT))
            layoutx = scenex + DRAG_ITEM.getWidth() / 2 + 10;
        else layoutx = scenex - DRAG_ITEM.getWidth() / 2 - tooltip.getWidth() - 10;
        tooltip.setLayoutY(Math.min(sceney - tooltip.getHeight() / 2,horizontalSplitPane.getHeight() - tooltip.getHeight()));
        tooltip.setLayoutX(Math.min(layoutx, horizontalSplitPane.getWidth() - tooltip.getWidth()));
    }

    void hideToolTip() {
        tooltip.pseudoClassStateChanged(TOOL_TIP_SHOW,false);
    }

}