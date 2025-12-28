package com.xx.UI.complex.stage;

import com.xx.UI.basic.BDButton;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.Util;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.HeaderButtonType;
import javafx.scene.text.Text;

public class BDHeaderBarBuilder {
    private final HeaderBar headerBar = new HeaderBar();
    private final HBox leadingBox = new HBox();
    private final HBox centerBox = new HBox();
    private final HBox trailingBox = new HBox();
    {
        headerBar.getStyleClass().add("bd-header-bar");
        leadingBox.getStyleClass().add("bd-header-bar-leading");
        centerBox.getStyleClass().add("bd-header-bar-center");
        trailingBox.getStyleClass().add("bd-header-bar-trailing");
        headerBar.setLeading(leadingBox);
        headerBar.setCenter(centerBox);
        headerBar.setTrailing(trailingBox);
    }
    public BDHeaderBarBuilder addLeading(Node node){
        leadingBox.getChildren().add(node);
        return this;
    }
    public BDHeaderBarBuilder addIcon(ImageView imageView){
        return addLeading(imageView);
    }
    public BDHeaderBarBuilder addCenter(Node node){
        centerBox.getChildren().add(node);
        return this;
    }
    public BDHeaderBarBuilder addTitle(String title){
        Text node = new Text(title);
        node.getStyleClass().add("bd-header-bar-title");
        return addCenter(node);
    }
    public BDHeaderBarBuilder addTrailing(Node node){
        trailingBox.getChildren().add(node);
        return this;
    }
    public BDHeaderBarBuilder addMaximizeButton(){
        BDButton button = new BDButton();
        button.getStyleClass().add("bd-stage-maximize-button");
        button.setGraphic(Util.getImageView(20, BDIcon.MAXIMIZEINACTIVE));
        button.setSelectable(false);
        HeaderBar.setButtonType(button, HeaderButtonType.ICONIFY);
        return addTrailing(button);
    }
    public BDHeaderBarBuilder addMinimizeButton(){
        BDButton button = new BDButton();
        button.getStyleClass().add("bd-stage-minimize-button");
        button.setGraphic(Util.getImageView(20, BDIcon.MINIMIZEINACTIVE));
        button.setSelectable(false);
        HeaderBar.setButtonType(button, HeaderButtonType.MAXIMIZE);
        return addTrailing(button);
    }
    public BDHeaderBarBuilder addCloseButton(){
        BDButton button = new BDButton();
        button.getStyleClass().add("bd-stage-close-button");
        button.setGraphic(Util.getImageView(20, BDIcon.CLOSEINACTIVE));
        button.setSelectable(false);
        HeaderBar.setButtonType(button, HeaderButtonType.CLOSE);
        return addTrailing(button);
    }
    public HeaderBar build(){
        return headerBar;
    }
}
