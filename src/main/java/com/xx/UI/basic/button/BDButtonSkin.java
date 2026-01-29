package com.xx.UI.basic.button;

import com.xx.UI.ui.BDUI;
import com.xx.UI.util.BDMapping;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.skin.ButtonSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

public class BDButtonSkin extends ButtonSkin implements BDUI {
    protected final BDMapping mapping;
    protected final BDButton control;
    private final SimpleBooleanProperty pressed = new SimpleBooleanProperty(false);

    public BDButtonSkin(final BDButton button) {
        super(button);
        control = button;
        mapping = button.getMapping();
        initUI();
        initEvent();
        initProperty();
    }

    @Override
    public void initEvent() {
        mapping.addEventFilter(control, ActionEvent.ACTION, _ -> {
                    if (control.isSelectable())
                        control.setSelected(!control.isSelected());
                })
                .addEventFilter(control, MouseEvent.MOUSE_RELEASED, _ -> pressed.set(false))
                .addEventFilter(control, MouseEvent.MOUSE_PRESSED, _ -> pressed.set(true))
                .addEventFilter(control, MouseEvent.MOUSE_EXITED_TARGET, _ -> pressed.set(false));
    }

    @Override
    public void initProperty() {
        Background background = control.getBackground();
        // 背景绑定逻辑
        mapping.binding(control.backgroundProperty(),
                        Bindings.createObjectBinding(() -> {
                                    // 获取当前背景的圆角和内边距，如果背景为空则使用默认值
                                    CornerRadii radii = background == null ? CornerRadii.EMPTY : 
                                        (background.getFills().isEmpty() ? CornerRadii.EMPTY : background.getFills().getFirst().getRadii());
                                    Insets insets = background == null ? Insets.EMPTY : 
                                        (background.getFills().isEmpty() ? Insets.EMPTY : background.getFills().getFirst().getInsets());
                                    
                                    if (control.isSelected()) {
                                        if (pressed.get()) {
                                            return new Background(new BackgroundFill(control.getSelectedPressedFill(), radii, insets));
                                        } else if (control.isHover()) {
                                            return new Background(new BackgroundFill(control.getSelectedHoverFill(), radii, insets));
                                        } else {
                                            return new Background(new BackgroundFill(control.getSelectedFill(), radii, insets));
                                        }
                                    } else {
                                        if (pressed.get()) {
                                            return new Background(new BackgroundFill(control.getPressedFill(), radii, insets));
                                        } else if (control.isHover()) {
                                            return new Background(new BackgroundFill(control.getHoverFill(), radii, insets));
                                        } else {
                                            return new Background(new BackgroundFill(control.getDefaultFill(), radii, insets));
                                        }
                                    }
                                },
                                control.defaultFillProperty(),
                                control.hoverFillProperty(),
                                control.pressedFillProperty(),
                                control.selectedFillProperty(),
                                control.selectedHoverFillProperty(),
                                control.selectedPressedFillProperty(),
                                control.selectedProperty(),
                                control.hoverProperty(),
                                pressed))
                // 图标切换逻辑
                .addListener(() -> {
                            // 根据按钮状态切换图标
                            if (control.isSelected()) {
                                if (pressed.get()) {
                                    // 选中状态下按下
                                    if (control.getSelectedPressGraphic() != null) {
                                        control.setGraphic(control.getSelectedPressGraphic());
                                    } else if (control.getSelectedGraphic() != null) {
                                        control.setGraphic(control.getSelectedGraphic());
                                    } else if (control.getPressGraphic() != null) {
                                        control.setGraphic(control.getPressGraphic());
                                    } else {
                                        control.setGraphic(control.getDefaultGraphic());
                                    }
                                } else if (control.isHover()) {
                                    // 选中状态下悬停
                                    if (control.getSelectedGraphic() != null) {
                                        control.setGraphic(control.getSelectedGraphic());
                                    } else {
                                        control.setGraphic(control.getDefaultGraphic());
                                    }
                                } else {
                                    // 选中状态下正常
                                    if (control.getSelectedGraphic() != null) {
                                        control.setGraphic(control.getSelectedGraphic());
                                    } else {
                                        control.setGraphic(control.getDefaultGraphic());
                                    }
                                }
                            } else {
                                if (pressed.get()) {
                                    // 未选中状态下按下
                                    if (control.getPressGraphic() != null) {
                                        control.setGraphic(control.getPressGraphic());
                                    } else {
                                        control.setGraphic(control.getDefaultGraphic());
                                    }
                                } else if (control.isHover()) {
                                    // 未选中状态下悬停
                                    if (control.getDefaultGraphic() != null) {
                                        control.setGraphic(control.getDefaultGraphic());
                                    } else {
                                        control.setGraphic(null);
                                    }
                                } else {
                                    // 未选中状态下正常
                                    if (control.getDefaultGraphic() != null) {
                                        control.setGraphic(control.getDefaultGraphic());
                                    } else {
                                        control.setGraphic(null);
                                    }
                                }
                            }
                        }, 
                        true,
                        pressed,
                        control.selectedProperty(),
                        control.hoverProperty(),
                        control.defaultGraphicProperty(),
                        control.selectedGraphicProperty(),
                        control.selectedPressGraphicProperty(),
                        control.pressGraphicProperty());
    }
}
