package com.xx.UI.complex.search.simple.box;

import com.xx.UI.basic.button.BDButton;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.Util;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

public class BDSimpleSearchBoxSkin extends BDSkin<BDSimpleSearchBox> {

    private static final PseudoClass ERROR =
            PseudoClass.getPseudoClass("error");
    public ContextMenu searchFilterButtonContextMenu;
    private BDButton searchHistoryButton;
    private TextArea searchField;
    private BDButton searchCleanButton;
    private BDButton searchNewLineButton;
    private BDButton searchCaseButton;
    private BDButton searchRegularExpressionButton;
    private Text searchResultText;
    private BDButton searchPreviousButton;
    private BDButton searchNextButton;
    private BDButton searchFilterButton;
    private BDButton searchMorButton;
    private BDButton searchCloseButton;

    private HBox leftTop;
    private HBox rightTop;

    public BDSimpleSearchBoxSkin(BDSimpleSearchBox bdSimpleSearchBox) {
        super(bdSimpleSearchBox);
    }

    @Override
    public void initUI() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);

        initLeftPane();
        initRightPane();
        getChildren().add(splitPane);
        splitPane.getItems().addAll(leftTop,rightTop);
    }

    private void initRightPane() {
        searchResultText = new Text();
        searchResultText.getStyleClass().add("result");
        searchPreviousButton = getBdButton();
        searchPreviousButton.setSelectable(false);
        searchPreviousButton.setDefaultGraphic(Util.getImageView(20, BDIcon.UP));
        searchPreviousButton.setTooltip(new Tooltip("上一个匹配项  Shift+F3"));
        searchNextButton = getBdButton();
        searchNextButton.setSelectable(false);
        searchNextButton.setDefaultGraphic(Util.getImageView(20, BDIcon.DOWN));
        searchNextButton.setTooltip(new Tooltip("下一个匹配项  F3"));
        searchFilterButton = getBdButton();
        searchFilterButton.setSelectable(false);
        searchFilterButton.setDefaultGraphic(Util.getImageView(20, BDIcon.FILTER));
        searchFilterButton.setTooltip(new Tooltip("筛选搜索结果  Ctrl+Alt+F"));
        searchFilterButtonContextMenu = new ContextMenu();
        searchMorButton = getBdButton();
        searchMorButton.setSelectable(false);
        searchMorButton.setDefaultGraphic(Util.getImageView(20, BDIcon.MORE_VERTICAL));
        searchCloseButton = getBdButton();
        searchCloseButton.setSelectable(false);
        searchCloseButton.getStyleClass().add("circle");
        searchCloseButton.setDefaultGraphic(Util.getImageView(20, BDIcon.CLOSE_SMALL));
        searchCloseButton.setTooltip(new Tooltip("关闭搜索  Esc"));

        rightTop = new HBox(searchResultText, searchPreviousButton, searchNextButton, searchFilterButton, searchMorButton, Util.getHBoxSpring(), searchCloseButton);
        rightTop.getStyleClass().add("top-pane");
    }

    private void initLeftPane() {
        searchField = Util.getInputContent(3);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchHistoryButton = getBdButton();
        searchHistoryButton.setSelectable(false);
        searchHistoryButton.setDefaultGraphic(Util.getImageView(20, BDIcon.SEARCH_HISTORY));
        searchHistoryButton.setTooltip(new Tooltip("搜索历史记录  Alt+向下箭头"));
        searchCleanButton = getBdButton();
        searchCleanButton.setSelectable(false);
        searchCleanButton.getStyleClass().add("circle");
        searchCleanButton.setDefaultGraphic(Util.getImageView(20, BDIcon.CLOSE_SMALL));
        searchNewLineButton = getBdButton();
        searchNewLineButton.setSelectable(false);
        searchNewLineButton.setDefaultGraphic(Util.getImageView(20, BDIcon.NEW_LINE));
        searchNewLineButton.setTooltip(new Tooltip("新行  Ctrl+Shift+Enter"));
        searchCaseButton = getBdButton();
        searchCaseButton.setDefaultGraphic(Util.getImageView(20, BDIcon.MATCH_CASE));
        searchCaseButton.setTooltip(new Tooltip("区分大小写  Alt+C"));
        searchRegularExpressionButton = getBdButton();
        searchRegularExpressionButton.setDefaultGraphic(Util.getImageView(20, BDIcon.REGEX));
        searchRegularExpressionButton.setTooltip(new Tooltip("正则表达式  Alt+X"));
        leftTop = new HBox(searchHistoryButton,searchField, searchCleanButton, searchNewLineButton, searchCaseButton, searchRegularExpressionButton);
        leftTop.getStyleClass().add("top-pane");
    }

    @Override
    public void initProperty() {
        mapping.binding(searchResultText.textProperty(), Bindings.createStringBinding(() -> {
                    boolean b = searchField.getText() != null && !searchField.getText().isEmpty() && control.getSearchBlockCount() == 0;
                    searchResultText.pseudoClassStateChanged(ERROR, b);
                    if (control.getSearchBlockCount() == 0) return "0 个结果";
                    return control.getSearchBlockIndex() + 1 + " / " + control.getSearchBlockCount();
                }, control.searchBlockCountProperty(), control.searchBlockIndexProperty(), searchField.textProperty()))
                .bindBidirectional(searchField.textProperty(), control.searchTextProperty())
                .bindProperty(control.regularExpression, searchField.textProperty().map(this::getRegularExpression))
                .bindProperty(searchCleanButton.visibleProperty(), searchField.textProperty().isNotEmpty())
                .addListener(() -> {
                    String s = "搜索";
                    if (searchRegularExpressionButton.isSelected() && searchCaseButton.isSelected())
                        s = "区分大小写(c) 和 正则表达式(x)";
                    else if (searchCaseButton.isSelected())
                        s = "区分大小写(c)";
                    else if (searchRegularExpressionButton.isSelected())
                        s = "正则表达式(x)";
                    searchField.setPromptText(s);
                }, true, searchCaseButton.selectedProperty(), searchRegularExpressionButton.selectedProperty());
    }
    private String getRegularExpression(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return "";
        }
        if (searchRegularExpressionButton.isSelected()) {
            // 直接使用用户输入的正则表达式
            return searchCaseButton.isSelected() ? searchText : "(?i)" + searchText;
        } else {
            // 转义所有正则特殊字符
            String s = searchText.replaceAll("([\\\\\\[\\]{}()*+?.^$|])", "\\\\$1");
            return searchCaseButton.isSelected() ? s : "(?i)" + s;
        }
    }

    private BDButton getBdButton(){
        BDButton bdButton = new BDButton();
        bdButton.getStyleClass().add("bd-search-box-button");
        return bdButton;
    }
    @Override
    public void initEvent() {
        KeyCombination newLine = KeyCombination.keyCombination("Ctrl+Shift+Enter");
        KeyCombination caseKey = KeyCombination.keyCombination("Alt+C");
        KeyCombination regularExpression = KeyCombination.keyCombination("Alt+X");
        mapping
                .addEventHandler(searchPreviousButton, ActionEvent.ACTION, _ -> control.previousSearchBlock())
                .addEventHandler(searchNextButton, ActionEvent.ACTION, _ -> control.nextSearchBlock())
                .addEventHandler(searchFilterButton, ActionEvent.ACTION, _ -> {
                    searchFilterButtonContextMenu.show(searchFilterButton, Side.BOTTOM, 0, 0);
                })
                .addEventHandler(searchCloseButton, ActionEvent.ACTION, _ -> control.simpleSearchPane.setShowSearchBox(false))
                .addEventFilter(control, KeyEvent.KEY_PRESSED, event -> {
                    if (caseKey.match(event))
                        searchCaseButton.fire();
                    if (regularExpression.match(event))
                        searchRegularExpressionButton.fire();
                })
                .addEventFilter(searchField, KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode().equals(KeyCode.ENTER))
                        event.consume();
                    if (newLine.match(event)) searchNewLineButton.fire();
                })
                .addEventHandler(searchCleanButton, ActionEvent.ACTION, _ -> {
                    searchField.clear();
                    searchField.requestFocus();
                })
                .addEventHandler(searchNewLineButton, ActionEvent.ACTION, _ -> {
                    searchField.appendText("\n");
                    searchField.requestFocus();
                })
                .addEventHandler(searchCaseButton, ActionEvent.ACTION, _ -> searchField.requestFocus())
                .addEventHandler(searchRegularExpressionButton, ActionEvent.ACTION, _ -> searchField.requestFocus());
    }
}
