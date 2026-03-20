package com.xx.UI.complex.search.simple.box;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class BDSimpleSearchBox extends BDControl {
    private static final String CSS_CLASS_NAME = "bd-search-box";
    private final SimpleStringProperty searchText = new SimpleStringProperty();
    final BDSimpleSearchPane simpleSearchPane;
    final SimpleStringProperty regularExpression = new SimpleStringProperty();
    private final SimpleBooleanProperty searchCase = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty searchRegex = new SimpleBooleanProperty(false);
    private final SimpleIntegerProperty searchBlockCount = new SimpleIntegerProperty(0);
    protected final SimpleIntegerProperty searchBlockIndex = new SimpleIntegerProperty(-1);

    public BDSimpleSearchBox(BDSimpleSearchPane simpleSearchPane) {
        getStyleClass().add(CSS_CLASS_NAME);
        this.simpleSearchPane = simpleSearchPane;
         mapping.addDisposeEvent(this::dispose);
        simpleSearchPane.getMapping().addChildren(getMapping());
    }

    private void dispose() {
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDSimpleSearchBoxSkin(this);
    }

    public int getSearchBlockCount() {
        return searchBlockCount.get();
    }

    public SimpleIntegerProperty searchBlockCountProperty() {
        return searchBlockCount;
    }

    public int getSearchBlockIndex() {
        return searchBlockIndex.get();
    }

    public SimpleIntegerProperty searchBlockIndexProperty() {
        return searchBlockIndex;
    }

    public void setSearchText(String searchText){this.searchText.set(searchText);}
    public String getSearchText() {
        return searchText.get();
    }

    public SimpleStringProperty searchTextProperty() {
        return searchText;
    }

    public void previousSearchBlock() {
    }

    public void nextSearchBlock() {
    }
    public void setRegularExpression(String regularExpression){
        this.regularExpression.set(regularExpression);
    }
    public void setSearchCase(boolean searchCase){
        this.searchCase.set(searchCase);
    }
    public void setSearchRegex(boolean searchRegex){
        this.searchRegex.set(searchRegex);
    }


    public BDSimpleSearchPane getSimpleSearchPane() {
        return simpleSearchPane;
    }

    public String getRegularExpression() {
        return regularExpression.get();
    }

    public SimpleStringProperty regularExpressionProperty() {
        return regularExpression;
    }

    public boolean isSearchCase() {
        return searchCase.get();
    }

    public SimpleBooleanProperty searchCaseProperty() {
        return searchCase;
    }

    public boolean isSearchRegex() {
        return searchRegex.get();
    }

    public SimpleBooleanProperty searchRegexProperty() {
        return searchRegex;
    }


    public void clearSearch() {
    }

    public void refresh() {
    }
}
