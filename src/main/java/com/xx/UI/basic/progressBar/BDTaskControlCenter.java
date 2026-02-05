package com.xx.UI.basic.progressBar;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.BDMapping;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;

import java.util.ArrayList;
import java.util.List;

public class BDTaskControlCenter extends BDControl {
    final SimpleListProperty<BDProgressContent> contents = new SimpleListProperty<>(FXCollections.observableArrayList());
    final List<BDProgressContent> tempContents = new ArrayList<>();
    final BDMapping tempMapping = new BDMapping();
    private final SimpleBooleanProperty show = new SimpleBooleanProperty();

    public BDTaskControlCenter() {
        mapping.addChildren(tempMapping);
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDTaskControlCenterSkin(this);
    }

    public void pushTask(BDTask<?> task, boolean pausable, boolean closeable) {
        BDProgressContent content = utilize();
        content.setTask(task);
        content.setPausable(pausable);
        content.setClosable(closeable);
        contents.add(content);

        // 监听任务状态变化
        tempMapping.addListener(task.stateProperty(), (_, oldState, newState) -> {
            if (newState.equals(Worker.State.CANCELLED) ||
                    newState.equals(Worker.State.FAILED) ||
                    newState.equals(Worker.State.SUCCEEDED)) {
                // 延迟回收，避免在事件监听过程中修改集合
                Platform.runLater(() -> recycle(content));
            }
        });
    }

    private BDProgressContent utilize() {
        if (!tempContents.isEmpty()) {
            BDProgressContent content = tempContents.removeFirst();
            content.setTask(null);
            return content;
        } else {
            return new BDProgressContent();
        }
    }

    // 回收指定任务
    private void recycle(BDProgressContent contentToRecycle) {
        // 确保content在contents列表中
        if (!contents.contains(contentToRecycle)) {
            return;
        }

        // 检查任务状态
        BDTask<?> task = contentToRecycle.getTask();
        if (task != null) {
            Worker.State state = task.getState();
            if (!state.equals(Worker.State.CANCELLED) &&
                    !state.equals(Worker.State.FAILED) &&
                    !state.equals(Worker.State.SUCCEEDED)) {
                return;
            }
            // 清理监听器
            tempMapping.disposeListener(task.stateProperty());
            contentToRecycle.setTask(null);
        }

        // 从contents移除并添加到tempContents
        contents.remove(contentToRecycle);
        tempContents.add(contentToRecycle);
    }

    public boolean isShow() {
        return show.get();
    }

    public SimpleBooleanProperty showProperty() {
        return show;
    }

    public void show() {
        this.show.set(true);
    }

    public void hide() {
        this.show.set(false);
    }

    public ReadOnlyListProperty<BDProgressContent> contentsProperty() {
        return contents;
    }
}