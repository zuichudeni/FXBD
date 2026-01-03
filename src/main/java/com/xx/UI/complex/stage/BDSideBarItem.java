package com.xx.UI.complex.stage;

import com.xx.UI.basic.BDButton;
import com.xx.UI.util.LazyValue;
import javafx.beans.property.*;
import javafx.scene.control.Skin;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.Objects;

public class BDSideBarItem extends BDButton {
   private final String name;
   private String shortcutKey;
   private final BDSideContent sideContent;
   private final SimpleBooleanProperty windowOpen = new SimpleBooleanProperty(false);
   private final SimpleObjectProperty<BDDirection> direction = new SimpleObjectProperty<>();
   private final SimpleObjectProperty<BDInSequence> inSequence = new SimpleObjectProperty<>();
   final SimpleObjectProperty<BDSidebar> sidebar = new SimpleObjectProperty<>();
   final LazyValue<Stage> stage = new LazyValue<>(Stage::new);
   public BDSideBarItem(String name, ImageView defaultIcon, ImageView selectIcon,BDDirection direction,BDInSequence inSequence, BDSideContent sideContent) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(defaultIcon);
        Objects.requireNonNull(selectIcon);
        Objects.requireNonNull(direction);
        Objects.requireNonNull(inSequence);
        this.name = name;
        this.sideContent = sideContent;
        this.direction.set(direction);
        this.inSequence.set(inSequence);
        setDefaultGraphic(defaultIcon);
        setSelectedGraphic(selectIcon);
    }
    public BDSideBarItem(String name,String shortcutKey, ImageView defaultIcon, ImageView selectIcon,BDDirection direction,BDInSequence inSequence, BDSideContent sideContent) {
        this(name, defaultIcon, selectIcon, direction, inSequence, sideContent);
        this.shortcutKey = shortcutKey;
    }

    public String getName() {
        return name;
    }

    public String getShortcutKey() {
        return shortcutKey;
    }

    public BDSideContent getSideContent() {
        return sideContent;
    }
    public boolean isWindowOpen() {
        return windowOpen.get();
    }
    public void setWindowOpen(boolean windowOpen) {
        this.windowOpen.set(windowOpen);
    }
    public BooleanProperty windowOpenProperty() {
        return windowOpen;
    }
    public BDDirection getDirection() {
        return direction.get();
    }
    public void setDirection(BDDirection direction) {
        this.direction.set(direction);
    }
    public ObjectProperty<BDDirection> directionProperty() {
        return direction;
    }
    public BDInSequence getInSequence() {
        return inSequence.get();
    }
    public void setInSequence(BDInSequence inSequence) {
        this.inSequence.set(inSequence);
    }
    public ObjectProperty<BDInSequence> inSequenceProperty() {
        return inSequence;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new BDSideBarItemSkin(this);
    }
}

