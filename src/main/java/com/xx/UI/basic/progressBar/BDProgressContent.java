package com.xx.UI.basic.progressBar;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class BDProgressContent extends BDControl {
    private final static String BD_PROGRESS_CONTENT_CLASS = "bd-progress-content";
    private final SimpleBooleanProperty closable = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty pausable = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<BDTask<?>> task = new SimpleObjectProperty<>();

    public BDProgressContent(BDTask<?> task) {
        this();
        setTask(task);
    }

    public BDProgressContent() {
        getStyleClass().setAll(BD_PROGRESS_CONTENT_CLASS);
    }

    public void setClosable(boolean closable){
        this.closable.set(closable);
    }

    public boolean isClosable() {
        return closable.get();
    }

    public SimpleBooleanProperty closableProperty() {
        return closable;
    }

    public void setPausable(boolean pausable){
        this.pausable.set(pausable);
    }
    public boolean isPausable() {
        return pausable.get();
    }

    public SimpleBooleanProperty pausableProperty() {
        return pausable;
    }

    public BDTask<?> getTask() {
        return task.get();
    }

    public void setTask(BDTask<?> task) {
        this.task.set(task);
    }

    public SimpleObjectProperty<BDTask<?>> taskProperty() {
        return task;
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDProgressContentSkin(this);
    }
}
