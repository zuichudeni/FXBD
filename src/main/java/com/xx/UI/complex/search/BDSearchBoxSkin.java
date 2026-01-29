package com.xx.UI.complex.search;

import com.xx.UI.basic.button.BDButton;
import com.xx.UI.complex.textArea.content.BDTextAreaContent;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.Util;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.util.List;


public class BDSearchBoxSkin extends BDSkin<BDSearchBox> {
    private static final PseudoClass ERROR =
            PseudoClass.getPseudoClass("error");
    public ContextMenu searchFilterButtonContextMenu;
    private SplitPane splitPane;
    private StackPane retractContent;
    private BDButton retractButton;
    private VBox leftPane;
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

    private VBox rightPane;
    private BDButton replaceHistoryButton;
    private TextArea replaceField;
    private BDButton replaceCleanButton;
    private BDButton replaceNewLineButton;
    private Button replaceButton;
    private Button replaceAllButton;
    private HBox content;
    private HBox leftTop;
    private HBox letBottom;
    private HBox rightTop;
    private HBox rightBottom;
    private CheckMenuItem searchSelect;

    protected BDSearchBoxSkin(BDSearchBox bdSearchBox) {
        super(bdSearchBox);
    }

    @Override
    public void initEvent() {
        KeyCombination newLine = KeyCombination.keyCombination("Ctrl+Shift+Enter");
        KeyCombination caseKey = KeyCombination.keyCombination("Alt+C");
        KeyCombination regularExpression = KeyCombination.keyCombination("Alt+X");
        KeyCombination searchSelectText = KeyCombination.keyCombination("Ctrl+Alt+E");
        searchSelect.setAccelerator(searchSelectText);
        mapping
                .addEventHandler(searchPreviousButton, ActionEvent.ACTION, _ -> control.previousSearchBlock())
                .addEventHandler(searchNextButton, ActionEvent.ACTION, _ -> control.nextSearchBlock())
                .addEventHandler(searchFilterButton, ActionEvent.ACTION, _ -> {
                    searchFilterButtonContextMenu.show(searchFilterButton, Side.BOTTOM, 0, 0);
                })
                .addEventHandler(searchCloseButton, ActionEvent.ACTION, _ -> control.searchPane.setShowSearchBox(false))
                .addEventFilter(control, KeyEvent.KEY_PRESSED, event -> {
                    if (caseKey.match(event))
                        searchCaseButton.fire();
                    if (regularExpression.match(event))
                        searchRegularExpressionButton.fire();
                    if (searchFilterButtonContextMenu.isShowing() && searchSelectText.match(event))
                        searchSelect.setSelected(!searchSelect.isSelected());
                })
                .addEventFilter(searchField, KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode().equals(KeyCode.ENTER))
                        event.consume();
                    if (newLine.match(event)) searchNewLineButton.fire();
                })
                .addEventFilter(replaceField, KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode().equals(KeyCode.ENTER))
                        event.consume();
                    if (newLine.match(event)) replaceNewLineButton.fire();
                })
                .addEventHandler(retractButton, ActionEvent.ACTION, _ -> control.setRetract(!control.isRetract()))
                .addEventHandler(searchCleanButton, ActionEvent.ACTION, _ -> {
                    searchField.clear();
                    searchField.requestFocus();
                })
                .addEventHandler(replaceCleanButton, ActionEvent.ACTION, _ -> {
                    replaceField.clear();
                    replaceField.requestFocus();
                })
                .addEventHandler(replaceNewLineButton, ActionEvent.ACTION, _ -> {
                    replaceField.appendText("\n");
                    replaceField.requestFocus();
                })
                .addEventHandler(searchNewLineButton, ActionEvent.ACTION, _ -> {
                    searchField.appendText("\n");
                    searchField.requestFocus();
                })
                .addEventHandler(searchCaseButton, ActionEvent.ACTION, _ -> searchField.requestFocus())
                .addEventHandler(searchRegularExpressionButton, ActionEvent.ACTION, _ -> searchField.requestFocus())
                .addEventHandler(replaceButton, ActionEvent.ACTION, _ -> {
                    BDTextAreaContent.Point start = null;
                    BDTextAreaContent.Point end = null;
                    for (List<BDSearchBox.SearchResult> value : control.getSearchResults().values()) {
                        List<BDSearchBox.SearchResult> list = value
                                .stream()
                                .filter(r -> r.resultIndex() == control.getSearchBlockIndex())
                                .toList();
                        if (!list.isEmpty()) {
                            start = new BDTextAreaContent.Point(list.getFirst().line(), list.getFirst().startOffset());
                            end = new BDTextAreaContent.Point(list.getLast().line(), list.getLast().endOffset());
                            break;
                        }
                    }
                    control.searchPane.replace(start, end, replaceField.getText());
                    Platform.runLater(control::refresh);
                })
                .addEventHandler(replaceAllButton, ActionEvent.ACTION, _ -> {
                    String patter = control.regularExpression.get();
                    String resource = control.isSearchSelected()?control.searchPane.bdSearchResource.getSelectedResource(): control.searchPane.bdSearchResource.getResource();
                    String replaceStr = replaceField.getText();
                    control.searchPane.replaceAll().replace(resource, resource.replaceAll(patter, replaceStr), replaceStr,control.isSearchSelected());
                });
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
                .bindBidirectional(searchSelect.selectedProperty(), control.searchSelectedProperty())
                .bindProperty(control.regularExpression, searchField.textProperty().map(this::getRegularExpression))
                .bindProperty(retractButton.rotateProperty(), control.retractProperty().map(r -> r ? 180 : -90))
                .bindProperty(searchCleanButton.visibleProperty(), searchField.textProperty().isNotEmpty())
                .bindProperty(replaceCleanButton.visibleProperty(), replaceField.textProperty().isNotEmpty())
                .bindProperty(replaceButton.disableProperty(), control.searchBlockCountProperty().lessThan(1))
                .bindProperty(replaceAllButton.disableProperty(), control.searchBlockCountProperty().lessThan(1))
                .addListener(() -> {
                    String s = "搜索";
                    if (searchRegularExpressionButton.isSelected() && searchCaseButton.isSelected())
                        s = "区分大小写(c) 和 正则表达式(x)";
                    else if (searchCaseButton.isSelected())
                        s = "区分大小写(c)";
                    else if (searchRegularExpressionButton.isSelected())
                        s = "正则表达式(x)";
                    searchField.setPromptText(s);
                }, true, searchCaseButton.selectedProperty(), searchRegularExpressionButton.selectedProperty())
                .addListener(() -> {
                    if (retractButton.getTooltip() == null)
                        retractButton.setTooltip(new Tooltip());
                    retractButton.getTooltip().setText((control.isRetract() ? "显示" : "隐藏") + "替换字段  Ctrl+" + (control.isRetract() ? "R" : "F"));
                    if (control.isRetract()) {
                        leftPane.getChildren().remove(letBottom);
                        rightPane.getChildren().remove(rightBottom);
                    } else {
                        leftPane.getChildren().add(letBottom);
                        rightPane.getChildren().add(rightBottom);
                    }
                }, true, control.retractProperty());
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

    @Override
    public void initUI() {
        content = new HBox(retractContent = new StackPane(retractButton = new BDButton()), splitPane = new SplitPane());
        content.setFillHeight(true);
        HBox.setHgrow(splitPane, Priority.ALWAYS);
        content.getStyleClass().add("content");
        retractContent.setMinHeight(Region.USE_PREF_SIZE);
        retractContent.setMaxHeight(Region.USE_PREF_SIZE);
        retractContent.getStyleClass().add("retract-content");
        retractButton.setSelectable(false);
        retractButton.setDefaultGraphic(Util.getImageView(20, BDIcon.PLAY_BACK));
        splitPane.setOrientation(Orientation.HORIZONTAL);
        initLeftPane();
        initRightPane();
        getChildren().setAll(content);

        control.setPrefWidth(Region.USE_COMPUTED_SIZE);
        control.setMaxWidth(Double.MAX_VALUE);
        control.setPrefHeight(Region.USE_COMPUTED_SIZE);
        control.setMaxHeight(Region.USE_PREF_SIZE);
    }

    private void initLeftPane() {
        leftPane = new VBox();
        leftPane.setAlignment(Pos.CENTER);
        leftPane.getStyleClass().add("left-pane");
        splitPane.getItems().add(leftPane);
        searchHistoryButton = new BDButton();
        searchHistoryButton.setSelectable(false);
        searchHistoryButton.setDefaultGraphic(Util.getImageView(20, BDIcon.SEARCH_HISTORY));
        searchHistoryButton.setTooltip(new Tooltip("搜索历史记录  Alt+向下箭头"));
        searchField = Util.getInputContent(3);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchCleanButton = new BDButton();
        searchCleanButton.setSelectable(false);
        searchCleanButton.getStyleClass().add("circle");
        searchCleanButton.setDefaultGraphic(Util.getImageView(20, BDIcon.CLOSE_SMALL));
        searchNewLineButton = new BDButton();
        searchNewLineButton.setSelectable(false);
        searchNewLineButton.setDefaultGraphic(Util.getImageView(20, BDIcon.NEW_LINE));
        searchNewLineButton.setTooltip(new Tooltip("新行  Ctrl+Shift+Enter"));
        searchCaseButton = new BDButton();
        searchCaseButton.setDefaultGraphic(Util.getImageView(20, BDIcon.MATCH_CASE));
        searchCaseButton.setTooltip(new Tooltip("区分大小写  Alt+C"));
        searchRegularExpressionButton = new BDButton();
        searchRegularExpressionButton.setDefaultGraphic(Util.getImageView(20, BDIcon.REGEX));
        searchRegularExpressionButton.setTooltip(new Tooltip("正则表达式  Alt+X"));

        replaceHistoryButton = new BDButton();
        replaceHistoryButton.setSelectable(false);
        replaceHistoryButton.setDefaultGraphic(Util.getImageView(20, BDIcon.SEARCH_HISTORY));
        replaceHistoryButton.setTooltip(new Tooltip("替换历史记录  Alt+向下箭头"));
        replaceField = Util.getInputContent(3);
        replaceField.setPromptText("替换");
        HBox.setHgrow(replaceField, Priority.ALWAYS);
        replaceCleanButton = new BDButton();
        replaceCleanButton.getStyleClass().add("circle");
        replaceCleanButton.setSelectable(false);
        replaceCleanButton.setDefaultGraphic(Util.getImageView(20, BDIcon.CLOSE_SMALL));
        replaceNewLineButton = new BDButton();
        replaceNewLineButton.setSelectable(false);
        replaceNewLineButton.setDefaultGraphic(Util.getImageView(20, BDIcon.NEW_LINE));
        replaceNewLineButton.setTooltip(new Tooltip("新行  Ctrl+Shift+Enter"));
        replaceButton = new Button();

        leftTop = new HBox(10, searchHistoryButton, searchField, searchCleanButton, searchNewLineButton, searchCaseButton, searchRegularExpressionButton);
        leftTop.setAlignment(Pos.CENTER_LEFT);
        leftTop.getStyleClass().add("top-pane");
        letBottom = new HBox(10, replaceHistoryButton, replaceField, replaceCleanButton, replaceNewLineButton);
        letBottom.setAlignment(Pos.CENTER_LEFT);
        letBottom.getStyleClass().add("bottom-pane");
        leftPane.getChildren().add(leftTop);

        leftPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        leftPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
        leftPane.setMinHeight(Region.USE_COMPUTED_SIZE);
    }

    private void initRightPane() {
        rightPane = new VBox();
        rightPane.setAlignment(Pos.CENTER);
        rightPane.getStyleClass().add("right-pane");
        splitPane.getItems().add(rightPane);
        searchResultText = new Text();
        searchResultText.getStyleClass().add("result");
        searchPreviousButton = new BDButton();
        searchPreviousButton.setSelectable(false);
        searchPreviousButton.setDefaultGraphic(Util.getImageView(20, BDIcon.UP));
        searchPreviousButton.setTooltip(new Tooltip("上一个匹配项  Shift+F3"));
        searchNextButton = new BDButton();
        searchNextButton.setSelectable(false);
        searchNextButton.setDefaultGraphic(Util.getImageView(20, BDIcon.DOWN));
        searchNextButton.setTooltip(new Tooltip("下一个匹配项  F3"));
        searchFilterButton = new BDButton();
        searchFilterButton.setSelectable(false);
        searchFilterButton.setDefaultGraphic(Util.getImageView(20, BDIcon.FILTER));
        searchFilterButton.setTooltip(new Tooltip("筛选搜索结果  Ctrl+Alt+F"));
        searchFilterButtonContextMenu = new ContextMenu();
        searchSelect = new CheckMenuItem("在所选内容中搜索");
        searchFilterButtonContextMenu.getItems().add(searchSelect);
        searchMorButton = new BDButton();
        searchMorButton.setSelectable(false);
        searchMorButton.setDefaultGraphic(Util.getImageView(20, BDIcon.MORE_HORIZONTAL));
        searchCloseButton = new BDButton();
        searchCloseButton.setSelectable(false);
        searchCloseButton.getStyleClass().add("circle");
        searchCloseButton.setDefaultGraphic(Util.getImageView(20, BDIcon.CLOSE_SMALL));
        searchCloseButton.setTooltip(new Tooltip("关闭搜索  Esc"));

        replaceButton = new Button("替换(P)");
        replaceAllButton = new Button("全部替换(A)");

        rightTop = new HBox(10, searchResultText, searchPreviousButton, searchNextButton, searchFilterButton, searchMorButton, Util.getHBoxSpring(), searchCloseButton);
        rightTop.setAlignment(Pos.CENTER_RIGHT);
        rightTop.getStyleClass().add("top-pane");
        rightBottom = new HBox(10, replaceButton, replaceAllButton);
        rightBottom.setAlignment(Pos.CENTER_LEFT);
        rightBottom.getStyleClass().add("bottom-pane");
        rightPane.getChildren().add(rightTop);
    }
}
