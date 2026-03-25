package com.xx.UI.complex.list;

import com.xx.UI.complex.search.simple.box.BDSimpleSearchBox;
import com.xx.UI.ui.BDUI;
import com.xx.UI.ui.BDVirtualUI;
import com.xx.UI.util.BDMapping;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Objects;

public class BDListCell<T> extends ListCell<T> implements BDUI, BDVirtualUI {
    private final BDListView<T> listView;
    private final PseudoClass select = PseudoClass.getPseudoClass("selected");
    private final Text text = new Text();
    private final AnchorPane searchPane = new AnchorPane();
    private final HBox hBox = new HBox();
    private final Pane root = new AnchorPane(searchPane, hBox);
    private final BDMapping mapping = new BDMapping();

    public BDListCell(BDListView<T> listView) {
        this.listView = listView;
        initUI();
        initEvent();
        initProperty();
    }

    @Override
    public void initUI() {
        searchPane.getStyleClass().add("bd-list-cell-search-pane");
        text.getStyleClass().add("bd-list-cell-text");
        hBox.getStyleClass().add("bd-list-hbox");
        root.getStyleClass().add("bd-list-root");
        getStyleClass().add("bd-list-cell");
        AnchorPane.setLeftAnchor(hBox, .0);
        AnchorPane.setTopAnchor(hBox, .0);
        AnchorPane.setBottomAnchor(hBox, .0);
        setPadding(Insets.EMPTY);
        setBorder(Border.EMPTY);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setFont(Font.font(10));
        AnchorPane.setLeftAnchor(searchPane, .0);
        AnchorPane.setRightAnchor(searchPane, .0);
        AnchorPane.setTopAnchor(searchPane, .0);
        AnchorPane.setBottomAnchor(searchPane, .0);
    }

    @Override
    public void initProperty() {
        BDMapping tempMapping = new BDMapping();
        mapping.addChildren(tempMapping);
        mapping.addListener(() -> {
                    disposeVirtual();
                    if (!isEmpty()) {
                        initVirtualUI();
                        initVirtualEvent();
                        initVirtualProperty();
                    }
                }, true, itemProperty())
                .addListener(() -> {
                    tempMapping.dispose();
                    if (listView.searchBox.get()!=null) {
                        tempMapping.addListener(listView.searchBox.get().refreshProperty(), (_, _, _) -> refreshSearchWord());
                        tempMapping.addListener(listView.searchBox.get().searchResultProperty(), (_, ov, nv) -> {
                            if ((ov != null && Objects.equals(ov.t(), getItem())) || (nv != null && Objects.equals(nv.t(), getItem()))) {
                                refreshSearchWord();
                            }
                        });
                    }
                }, true, listView.searchBox);
    }

    @Override
    public void initEvent() {
        hBox.setMouseTransparent(true);
    }

    @Override
    protected void updateItem(T t, boolean b) {
        super.updateItem(t, b);
        disposeVirtual();
        if (!isEmpty()) {
            initVirtualUI();
            initVirtualEvent();
            initVirtualProperty();
        }
    }

    @Override
    public void initVirtualUI() {
        Node graphic = listView.getListCellInitFactory().initGraphic(getItem());
        String info = listView.getListCellInitFactory().getInfo(getItem());
        text.setText(info);
        hBox.getChildren().setAll(graphic, text);
        listView.getListCellInitFactory().rendering(getItem(), graphic, text, hBox);
//        检查text的内容是否被更改
        if (!Objects.equals(info, text.getText()))
            throw new UnsupportedOperationException("文本信息发生了变化：oldText=%s,newText=%s".formatted(info, text.getText()));
        if (!hBox.getChildren().contains(graphic))
            throw new UnsupportedOperationException("不能删除graphic");
        if (!hBox.getChildren().contains(text))
            throw new UnsupportedOperationException("不能删除text");
        setGraphic(root);
        applyCss();
        layout();
        refreshSearchWord();
    }

    private void refreshSearchWord() {
        searchPane.getChildren().clear();
        if (listView.searchBox.get() == null || listView.searchBox.get().getSearchMap().isEmpty()) return;
        List<BDSimpleSearchBox.SimpleSearchResult<T>> resultList = listView.searchBox.get().getSearchMap().get(getItem());
        if (resultList != null && !resultList.isEmpty()) {
            resultList.forEach(result -> {
                PathElement[] start = text.caretShape(result.startOffset(), true);
                PathElement[] end = text.caretShape(result.endOffset(), true);
                Pane pane = new Pane();
                AnchorPane.setTopAnchor(pane, .0);
                AnchorPane.setBottomAnchor(pane, .0);
                pane.setPrefWidth(((MoveTo) end[0]).getX() - ((MoveTo) start[0]).getX());
                pane.getStyleClass().add("bd-listview-search-back");
                BDSimpleSearchBox.SimpleSearchResult<T> tSimpleSearchResult = listView.searchBox.get().searchResultProperty().get();
                pane.pseudoClassStateChanged(select, Objects.equals(tSimpleSearchResult, result));
                Bounds textBounds = text.localToScene(text.getLayoutBounds());
                Bounds paneBounds = searchPane.localToScene(searchPane.getLayoutBounds());
                pane.setLayoutX(textBounds.getMinX() - paneBounds.getMinX() + ((MoveTo) text.caretShape(result.startOffset(), true)[0]).getX());
                pane.setOnMouseClicked(_ -> listView.searchBox.get().setSearchResult(result));
                searchPane.getChildren().add(pane);
            });
        }
    }

    @Override
    public void initVirtualEvent() {
        BDVirtualUI.super.initVirtualEvent();
    }

    @Override
    public void initVirtualProperty() {
        BDVirtualUI.super.initVirtualProperty();
    }

    @Override
    public void disposeVirtual() {
        setGraphic(null);
    }
}
