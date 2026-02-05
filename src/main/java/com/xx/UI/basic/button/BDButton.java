package com.xx.UI.basic.button;

import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.BDMapping;
import com.xx.UI.util.Util;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.*;
import javafx.css.converter.PaintConverter;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public class BDButton extends Button {
    private static final String CSS_CLASS_NAME = "bd-button";
    private final BDMapping mapping = new BDMapping();
    //    默认图标
    private final ObjectProperty<Node> defaultGraphic = new SimpleObjectProperty<>();
    //    按下图标
    private final ObjectProperty<Node> pressGraphic = new SimpleObjectProperty<>();
    //    选中图标
    private final ObjectProperty<Node> selectedGraphic = new SimpleObjectProperty<>();
    //    选中按下图标
    private final ObjectProperty<Node> selectedPressGraphic = new SimpleObjectProperty<>();
    //    选中状态
    private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
    //    是否可以被选中
    private final SimpleBooleanProperty selectable = new SimpleBooleanProperty(true);
    //    加载图标
    private final ObjectProperty<Node> loadGraphic = new SimpleObjectProperty<>(Util.getImageView(20, BDIcon.LOADER_A));
    //    加载状态
    private final SimpleBooleanProperty load = new SimpleBooleanProperty(false);
    final PseudoClass LOAD = PseudoClass.getPseudoClass("load");
    //    默认填充色
    private ObjectProperty<Paint> defaultFill;
    //    悬停填充色
    private ObjectProperty<Paint> hoverFill;
    //    按下填充色
    private ObjectProperty<Paint> pressedFill;
    //    选中填充色
    private ObjectProperty<Paint> selectedFill;
    //    选中悬停填充色
    private ObjectProperty<Paint> selectedHoverFill;
    //    选中按下填充色
    private ObjectProperty<Paint> selectedPressedFill;
    //    加载的背景色
    private ObjectProperty<Paint> loadFill;

    public BDButton() {
        getStyleClass().add(CSS_CLASS_NAME);
    }

    public BDButton(String text) {
        super(text);
        getStyleClass().add(CSS_CLASS_NAME);
    }

    public BDButton(String text, Node graphic) {
        this(text);
        setDefaultGraphic(graphic);
    }


    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return BDButton.StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public BDMapping getMapping() {
        return mapping;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public ReadOnlyBooleanProperty selectedProperty() {
        return selected;
    }

    public boolean isSelectable() {
        return selectable.get();
    }

    public void setSelectable(boolean selectable) {
        this.selectable.set(selectable);
    }

    public SimpleBooleanProperty selectableProperty() {
        return selectable;
    }

    public Paint getDefaultFill() {
        return defaultFillProperty().get();
    }

    public void setDefaultFill(Paint defaultFill) {
        defaultFillProperty().set(defaultFill);
    }

    public boolean isLoad() {
        return load.get();
    }

    public void setLoad(boolean load) {
        this.load.set(load);
    }

    public SimpleBooleanProperty loadProperty() {
        return load;
    }

    public ObjectProperty<Paint> defaultFillProperty() {
        if (defaultFill == null)
            this.defaultFill = new StyleableObjectProperty<>(Color.TRANSPARENT) {

                @Override
                public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
                    return StyleableProperties.DEFAULT_FILL;
                }

                @Override
                public Object getBean() {
                    return BDButton.this;
                }

                @Override
                public String getName() {
                    return "-bd-default-fill";
                }
            };
        return defaultFill;
    }

    public Paint getHoverFill() {
        return hoverFillProperty().get();
    }

    public void setHoverFill(Paint hoverFill) {
        hoverFillProperty().set(hoverFill);
    }

    public ObjectProperty<Paint> hoverFillProperty() {
        if (hoverFill == null)
            this.hoverFill = new StyleableObjectProperty<>(Color.web("#EDEDED")) {

                @Override
                public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
                    return StyleableProperties.HOVER_FILL;
                }

                @Override
                public Object getBean() {
                    return BDButton.this;
                }

                @Override
                public String getName() {
                    return "-bd-hover-fill";
                }
            };
        return hoverFill;
    }

    public Paint getPressedFill() {
        return pressedFillProperty().get();
    }

    public void setPressedFill(Paint pressedFill) {
        pressedFillProperty().set(pressedFill);
    }

    public ObjectProperty<Paint> pressedFillProperty() {
        if (pressedFill == null)
            this.pressedFill = new StyleableObjectProperty<>(Color.web("#DDDDDF")) {

                @Override
                public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
                    return StyleableProperties.PRESSED_FILL;
                }

                @Override
                public Object getBean() {
                    return BDButton.this;
                }

                @Override
                public String getName() {
                    return "-bd-pressed-fill";
                }
            };
        return pressedFill;
    }

    public Paint getSelectedFill() {
        return selectedFillProperty().get();
    }

    public void setSelectedFill(Paint selectedFill) {
        selectedFillProperty().set(selectedFill);
    }

    public ObjectProperty<Paint> selectedFillProperty() {
        if (selectedFill == null)
            this.selectedFill = new StyleableObjectProperty<>(Color.web("#D4E2FF")) {

                @Override
                public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
                    return StyleableProperties.SELECTED_FILL;
                }

                @Override
                public Object getBean() {
                    return BDButton.this;
                }

                @Override
                public String getName() {
                    return "-bd-selected-fill";
                }
            };
        return selectedFill;
    }

    public Paint getSelectedHoverFill() {
        return selectedHoverFillProperty().get();
    }

    public void setSelectedHoverFill(Paint selectedHoverFill) {
        selectedHoverFillProperty().set(selectedHoverFill);
    }

    public ObjectProperty<Paint> selectedHoverFillProperty() {
        if (selectedHoverFill == null)
            this.selectedHoverFill = new StyleableObjectProperty<>(Color.web("#C2D6FC")) {

                @Override
                public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
                    return StyleableProperties.SELECTED_HOVER_FILL;
                }

                @Override
                public Object getBean() {
                    return BDButton.this;
                }

                @Override
                public String getName() {
                    return "-bd-selected-hover-fill";
                }
            };
        return selectedHoverFill;
    }

    public Paint getSelectedPressedFill() {
        return selectedPressedFillProperty().get();
    }

    public void setSelectedPressedFill(Paint selectedPressedFill) {
        selectedPressedFillProperty().set(selectedPressedFill);
    }

    public ObjectProperty<Paint> selectedPressedFillProperty() {
        if (selectedPressedFill == null)
            this.selectedPressedFill = new StyleableObjectProperty<>(Color.web("#C2D6FC")) {
                @Override
                public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
                    return StyleableProperties.SELECTED_PRESSED_FILL;
                }

                @Override
                public Object getBean() {
                    return BDButton.this;
                }

                @Override
                public String getName() {
                    return "-bd-selected-pressed-fill";
                }
            };
        return selectedPressedFill;
    }

    public void setLoadFill(Paint loadFill) {
        loadFillProperty().set(loadFill);
    }

    public Paint getLoadFil() {
        return loadFillProperty().get();
    }

    public ObjectProperty<Paint> loadFillProperty() {
        if (loadFill == null)
            this.loadFill = new StyleableObjectProperty<>(Color.web("#E2E2E2")) {
                @Override
                public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
                    return StyleableProperties.SELECTED_PRESSED_FILL;
                }


                @Override
                public Object getBean() {
                    return BDButton.this;
                }

                @Override
                public String getName() {
                    return "-bd-load-fill";
                }
            };
        return loadFill;
    }

    public Node getDefaultGraphic() {
        return defaultGraphic.get();
    }

    public void setDefaultGraphic(Node defaultGraphic) {
        defaultGraphicProperty().set(defaultGraphic);
    }

    public ObjectProperty<Node> defaultGraphicProperty() {
        return defaultGraphic;
    }

    public Node getPressGraphic() {
        return pressGraphic.get();
    }

    public void setPressGraphic(Node pressGraphic) {
        pressGraphicProperty().set(pressGraphic);
    }

    public ObjectProperty<Node> pressGraphicProperty() {
        return pressGraphic;
    }

    public Node getSelectedGraphic() {
        return selectedGraphic.get();
    }

    public void setSelectedGraphic(Node selectedGraphic) {
        selectedGraphicProperty().set(selectedGraphic);
    }

    public ObjectProperty<Node> selectedGraphicProperty() {
        return selectedGraphic;
    }

    public Node getSelectedPressGraphic() {
        return selectedPressGraphic.get();
    }

    public ObjectProperty<Node> selectedPressGraphicProperty() {
        return selectedPressGraphic;
    }

    public Node getLoadGraphic() {
        return loadGraphic.get();
    }

    public void setLoadGraphic(Node graphic) {
        this.loadGraphic.set(graphic);
    }

    public ObjectProperty<Node> loadGraphicProperty() {
        return loadGraphic;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new BDButtonSkin(this);
    }

    private static final class StyleableProperties {
        private static final CssMetaData<BDButton, Paint> DEFAULT_FILL;
        private static final CssMetaData<BDButton, Paint> HOVER_FILL;
        private static final CssMetaData<BDButton, Paint> PRESSED_FILL;
        private static final CssMetaData<BDButton, Paint> SELECTED_FILL;
        private static final CssMetaData<BDButton, Paint> SELECTED_HOVER_FILL;
        private static final CssMetaData<BDButton, Paint> SELECTED_PRESSED_FILL;
        private static final CssMetaData<BDButton, Paint> LOAD_FILL;
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            DEFAULT_FILL = new CssMetaData<>("-bd-default-fill", PaintConverter.getInstance(), Color.TRANSPARENT) {
                @Override
                public boolean isSettable(BDButton bdButton) {
                    return bdButton.defaultFill == null || !bdButton.defaultFill.isBound();
                }

                @Override
                public StyleableProperty<Paint> getStyleableProperty(BDButton bdButton) {
                    return (StyleableProperty<Paint>) bdButton.defaultFillProperty();
                }
            };
            HOVER_FILL = new CssMetaData<>("-bd-hover-fill", PaintConverter.getInstance(), Color.web("#EDEDED")) {
                @Override
                public boolean isSettable(BDButton bdButton) {
                    return bdButton.hoverFill == null || !bdButton.hoverFill.isBound();
                }

                @Override
                public StyleableProperty<Paint> getStyleableProperty(BDButton bdButton) {
                    return (StyleableProperty<Paint>) bdButton.hoverFillProperty();
                }
            };
            PRESSED_FILL = new CssMetaData<>("-bd-pressed-fill", PaintConverter.getInstance(), Color.web("#DDDDDF")) {
                @Override
                public boolean isSettable(BDButton bdButton) {
                    return bdButton.pressedFill == null || !bdButton.pressedFill.isBound();
                }

                @Override
                public StyleableProperty<Paint> getStyleableProperty(BDButton bdButton) {
                    return (StyleableProperty<Paint>) bdButton.pressedFillProperty();
                }
            };
            SELECTED_FILL = new CssMetaData<>("-bd-selected-fill", PaintConverter.getInstance(), Color.web("#D4E2FF")) {
                @Override
                public boolean isSettable(BDButton bdButton) {
                    return bdButton.selectedFill == null || !bdButton.selectedFill.isBound();
                }

                @Override
                public StyleableProperty<Paint> getStyleableProperty(BDButton bdButton) {
                    return (StyleableProperty<Paint>) bdButton.selectedFillProperty();
                }
            };
            SELECTED_HOVER_FILL = new CssMetaData<>("-bd-selected-hover-fill", PaintConverter.getInstance(), Color.web("#C2D6FC")) {
                @Override
                public boolean isSettable(BDButton bdButton) {
                    return bdButton.selectedHoverFill == null || !bdButton.selectedHoverFill.isBound();
                }

                @Override
                public StyleableProperty<Paint> getStyleableProperty(BDButton bdButton) {
                    return (StyleableProperty<Paint>) bdButton.selectedHoverFillProperty();
                }
            };
            SELECTED_PRESSED_FILL = new CssMetaData<>("-bd-selected-pressed-fill", PaintConverter.getInstance(), Color.web("#C2D6FC")) {
                @Override
                public boolean isSettable(BDButton bdButton) {
                    return bdButton.selectedPressedFill == null || !bdButton.selectedPressedFill.isBound();
                }

                @Override
                public StyleableProperty<Paint> getStyleableProperty(BDButton bdButton) {
                    return (StyleableProperty<Paint>) bdButton.selectedPressedFillProperty();
                }
            };
            LOAD_FILL = new CssMetaData<>("-bd-load-fill", PaintConverter.getInstance(), Color.web("#D8D8D8")) {
                @Override
                public boolean isSettable(BDButton bdButton) {
                    return bdButton.loadFill == null || !bdButton.loadFill.isBound();
                }

                @Override
                public StyleableProperty<Paint> getStyleableProperty(BDButton bdButton) {
                    return (StyleableProperty<Paint>) bdButton.loadFillProperty();
                }
            };
            @SuppressWarnings("rawtypes")
            ArrayList var0 = new ArrayList(Control.getClassCssMetaData());
            Collections.addAll(var0, DEFAULT_FILL, HOVER_FILL, PRESSED_FILL, SELECTED_FILL, SELECTED_HOVER_FILL, SELECTED_PRESSED_FILL, LOAD_FILL);
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.addAll(var0);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
