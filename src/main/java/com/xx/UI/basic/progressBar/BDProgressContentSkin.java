package com.xx.UI.basic.progressBar;

import com.xx.UI.basic.button.BDButton;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.BDMapping;
import com.xx.UI.util.Util;
import javafx.concurrent.Worker;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BDProgressContentSkin extends BDSkin<BDProgressContent> {
    private final VBox root;
    private final Text title;
    private final HBox barContent;
    private final ProgressBar bar;
    private final BDButton pause;
    private final BDButton close;
    private final Text state;
    private final Text message;
    private final PseudoClass RUNNING;
    private final PseudoClass PAUSE;
    private final PseudoClass FAILED;
    private final PseudoClass SUCCEEDED;
    private List<PseudoClass> allPseudoClasses;

    protected BDProgressContentSkin(BDProgressContent bdProgressContent) {
        root = new VBox();
        title = new Text();
        barContent = new HBox();
        bar = new ProgressBar();
        pause = new BDButton();
        close = new BDButton();
        state = new Text();
        message = new Text();
        RUNNING = PseudoClass.getPseudoClass("running");
        PAUSE = PseudoClass.getPseudoClass("pause");
        FAILED = PseudoClass.getPseudoClass("failed");
        SUCCEEDED = PseudoClass.getPseudoClass("succeeded");
        super(bdProgressContent);
    }

    @Override
    public void initUI() {
        allPseudoClasses = List.of(RUNNING, PAUSE, FAILED, SUCCEEDED);
        root.getChildren().addAll(title, barContent, new VBox(state, message));
        barContent.getChildren().addAll(bar, pause, close);
        // 关键修复：确保 bar 能填充剩余空间
        bar.setMaxWidth(Double.MAX_VALUE);  // 1. 允许 bar 无限扩展
        HBox.setHgrow(bar, Priority.ALWAYS); // 2. 设置增长优先级
        barContent.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(bar, Priority.ALWAYS);
        VBox.setVgrow(barContent, Priority.ALWAYS);
        root.getStyleClass().add("bd-progress-root");
        title.getStyleClass().add("bd-progress-title");
        barContent.getStyleClass().add("bd-progress-bar-content");
        bar.getStyleClass().add("bd-progress-bar");
        pause.getStyleClass().add("bd-progress-pause");
        close.getStyleClass().add("bd-progress-close");
        state.getStyleClass().add("bd-progress-state");
        message.getStyleClass().add("bd-progress-message");
        getChildren().setAll(root);

        pause.getStyleClass().add("circle");
        pause.setDefaultGraphic(Util.getImageView(20, BDIcon.PAUSE_HOVERED_DARK));
        pause.setSelectedGraphic(Util.getImageView(20, BDIcon.RESUME_HOVERED_DARK));

        close.getStyleClass().add("circle");
        close.setDefaultGraphic(Util.getImageView(20, BDIcon.STOP_HOVERED_DARK));
        close.setSelectable(false);
    }

    @Override
    public void initProperty() {
        BDMapping tempMapping = new BDMapping();
        mapping.binding(control.pausableProperty().and(control.taskProperty().isNotNull()), pause.visibleProperty(), pause.managedProperty())
                .binding(control.closableProperty().and(control.taskProperty().isNotNull()), close.visibleProperty(), close.managedProperty())
                .addListener(this::updateState, true, pause.hoverProperty(), close.hoverProperty())
                .addListener(() -> {
                    BDTask<?> nv = control.getTask();
                    updateState();
                    tempMapping.dispose();
                    if (nv != null)
                        tempMapping
                                .bindProperty(title.textProperty(), control.getTask().titleProperty())
                                .bindProperty(message.textProperty(), control.getTask().messageProperty())
                                .bindProperty(bar.progressProperty(), control.getTask().progressProperty())
                                .addListener(() -> {
                                    updateState();
                                    Worker.State state = control.getTask().getState();
                                    switch (state) {
                                        case RUNNING, READY, SCHEDULED -> {
                                            if (control.getTask().isPaused()) {
                                                pause.setDisable(false);
                                                close.setDisable(false);
                                                updatePseudoClass(PAUSE);
                                            } else {
                                                pause.setDisable(false);
                                                close.setDisable(false);
                                                updatePseudoClass(RUNNING);
                                            }
                                        }
                                        case FAILED, CANCELLED -> {
                                            pause.setDisable(true);
                                            close.setDisable(true);
                                            updatePseudoClass(FAILED);
                                        }
                                        case SUCCEEDED -> {
                                            pause.setDisable(true);
                                            close.setDisable(true);
                                            updatePseudoClass(SUCCEEDED);
                                        }
                                    }
                                }, true, control.getTask().stateProperty(), control.getTask().pausedProperty());
                    else {
                        bar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                        title.setText("---");
                        message.setText("---");
                        pause.setDisable(true);
                        close.setDisable(true);
                        updatePseudoClass(null);
                    }
                }, true, control.taskProperty())
                .addChildren(pause.getMapping(), close.getMapping(), tempMapping);
    }

    private void updatePseudoClass(PseudoClass pseudoClass) {
        // 收集所有需要更新的节点
        List<Node> allNodes = new ArrayList<>();
        allNodes.add(root);
        allNodes.addAll(root.getChildren());
        allNodes.addAll(List.of(message, bar));

        // 重置所有伪类状态
        allPseudoClasses.forEach(pc -> {
            if (!Objects.equals(pc, pseudoClass))
                setPseudoClassForAllNodes(allNodes, pc, false);
        });
        if (pseudoClass != null)
            setPseudoClassForAllNodes(allNodes, pseudoClass, true);
    }

    // 辅助方法：为所有节点设置伪类状态
    private void setPseudoClassForAllNodes(List<Node> nodes, PseudoClass pseudoClass, boolean active) {
        nodes.forEach(node -> node.pseudoClassStateChanged(pseudoClass, active));
    }

    private void updatePseudoClass(Node node, PseudoClass pseudoClass) {
        allPseudoClasses.forEach(pc -> {
            if (!Objects.equals(pc, pseudoClass))
                node.pseudoClassStateChanged(pc, false);
        });
        if (pseudoClass != null)
            node.pseudoClassStateChanged(pseudoClass, true);
    }

    private void updateState() {
        BDTask<?> task = control.getTask();
        if (task == null) state.setText("");
        else {
            switch (task.getState()) {
                case RUNNING -> {
                    if (close.isHover()) {
                        state.setText("结束任务");
                        updatePseudoClass(state, FAILED);
                    } else if (pause.isSelected()){
                        state.setText(pause.isHover() ? "继续任务" : "任务暂停");
                        updatePseudoClass(state, pause.isHover() ? RUNNING : PAUSE);
                    } else {
                        state.setText(pause.isHover() ? "暂停任务" : "执行中");
                        updatePseudoClass(state, pause.isHover() ? PAUSE : RUNNING);
                    }
                }
                case SUCCEEDED -> {
                    state.setText("执行成功");
                    updatePseudoClass(state, SUCCEEDED);
                }
                case CANCELLED -> {
                    state.setText("取消执行");
                    updatePseudoClass(state, FAILED);
                }
                case FAILED -> {
                    state.setText("执行失败");
                    updatePseudoClass(state, FAILED);
                }
                case SCHEDULED, READY -> {
                    if (close.isHover()) {
                        state.setText("结束任务");
                        updatePseudoClass(state, FAILED);
                    } else {
                        state.setText(pause.isHover() ? "准备执行" : "暂停任务");
                        updatePseudoClass(state, pause.isSelected() ? RUNNING : PAUSE);
                    }
                }
            }
        }
    }

    @Override
    public void initEvent() {
        mapping
                .addEventHandler(pause, ActionEvent.ACTION, _ -> {
                    boolean selected = pause.isSelected();
                    if (selected)
                        control.getTask().pauseTask();
                    else control.getTask().resumeTask();
                })
                .addEventHandler(close, ActionEvent.ACTION, _ -> control.getTask().cancel());
    }
}
