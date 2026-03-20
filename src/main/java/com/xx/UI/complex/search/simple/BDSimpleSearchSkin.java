package com.xx.UI.complex.search.simple;

import atlantafx.base.layout.InputGroup;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.Util;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.util.concurrent.atomic.AtomicBoolean;

public class BDSimpleSearchSkin extends BDSkin<BDSimpleSearch> {
    private final InputGroup inputGroup;
    private final TextField textField;
    private final PseudoClass empty = PseudoClass.getPseudoClass("empty");

    protected BDSimpleSearchSkin(BDSimpleSearch bdSimpleSearch) {
        this.textField = new TextField();
        this.inputGroup = new InputGroup();
        super(bdSimpleSearch);
    }

    @Override
    public void initUI() {
        inputGroup.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        inputGroup.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        textField.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        textField.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        textField.setPromptText("搜索");

        var label = new Label();
        label.setGraphic(Util.getImageView(19, BDIcon.SEARCH));
        inputGroup.getChildren().addAll(label, textField);
        label.getStyleClass().add("bd-simple-search-label");
        getChildren().add(inputGroup);
        control.getStyleClass().add("bd-simple-search-box");
        inputGroup.getStyleClass().add("bd-simple-search-box-input-group");
        textField.getStyleClass().add("bd-simple-search-box-text-field");

    }

    @Override
    public void initProperty() {
        mapping.bindBidirectional(control.searchProperty(), textField.textProperty())
                .addListener(() -> {
                    AtomicBoolean empty = new AtomicBoolean(!control.getSearch().isEmpty());
                    control.getSearchUpdates().forEach(simpleSearchUpdate -> {
                        if (simpleSearchUpdate.searchUpdate(control.getSearch()))
                            empty.set(false);
                    });
                    textField.pseudoClassStateChanged(this.empty, empty.get());
                    adjustTextFieldWidth(textField);
                     }, true, control.searchProperty());
    }
private void adjustTextFieldWidth(TextField textField) {
    Text text = new Text(textField.getText());
    text.setFont(textField.getFont()); // 使用相同的字体
    double width = text.getLayoutBounds().getWidth();
    // 加上内边距和边框宽度（通常 20-30 像素，可根据样式调整）
    textField.setPrefWidth(Math.max(60,width + (text.getText().isEmpty() ?60:30)));
}
    @Override
    public void initEvent() {
        super.initEvent();
    }
}
