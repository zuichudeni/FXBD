package com.xx.UI.basic.progressBar;

import com.xx.UI.complex.stage.BDHeaderBarBuilder;
import com.xx.UI.complex.stage.BDStageBuilder;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.BDMapping;
import com.xx.UI.util.LazyValue;
import com.xx.UI.util.Util;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class BDTaskControlCenterSkin extends BDSkin<BDTaskControlCenter> {
    private final HBox root;
    private final Label title;
    private final ProgressBar bar;
    private final Text size;
    private final VBox contentList;
    private LazyValue<Stage> stage;
    private LazyValue<Node> success;
    private final Tooltip tooltip;

    protected BDTaskControlCenterSkin(BDTaskControlCenter bdTaskControlCenter) {
        root = new HBox();
        title = new Label();
        bar = new ProgressBar();
        size = new Text();
        contentList = new VBox(5);
        tooltip = new Tooltip();

        super(bdTaskControlCenter);
    }

    @Override
    public void initEvent() {
        mapping.addEventFilter(control, MouseEvent.MOUSE_CLICKED, _ -> {
            if (stage.get().isShowing())
                stage.get().close();
            else
                stage.get().show();
        });
    }

    @Override
    public void initProperty() {
        BDMapping tempMapping = new BDMapping();
        mapping.addListener(() -> {
                    ObservableList<BDProgressContent> list = control.contents.get();
                    tempMapping.dispose();
                    contentList.getChildren().clear();
                    if (list.isEmpty()) {
                        title.setText("--");
                        bar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                        size.setText("");
                        contentList.getChildren().addAll(success.get());
                    } else {
                        tempMapping.bindProperty(title.textProperty(), list.getFirst().getTask().messageProperty())
                                .bindProperty(bar.progressProperty(), list.getFirst().getTask().progressProperty());
                        if (list.size() > 1) size.setText("+" + (list.size() - 1));
                        else size.setText("");
                        for (BDProgressContent content : list)
                            contentList.getChildren().add(content);
                    }
                }, true, (ObservableList<?>) control.contents)
                .bindProperty(tooltip.textProperty(),title.textProperty())
                .addChildren(tempMapping);
    }

    @Override
    public void initUI() {

        control.setTooltip(tooltip);
        success = new LazyValue<>(() -> {
            HBox hBox = new HBox();
            Text emptyText = new Text("所有任务后台已完成");
            emptyText.getStyleClass().add("bd-control-center-stage-empty-text");
            hBox.getStyleClass().add("bd-control-center-stage-empty-content");
            hBox.getChildren().addAll(Util.getImageView(25, BDIcon.SUCCESS_DIALOG), emptyText);
            return hBox;
        });
        stage = new LazyValue<>(
                () -> {
                    Text title = new Text("进程");
                    title.getStyleClass().add("bd-task-control-center-stage-title");
                    contentList.getStyleClass().add("bd-task-control-center-stage-content");
                    BDHeaderBarBuilder header = new BDHeaderBarBuilder()
                            .addCenter(title)
                            .addTitle("进程")
                            .setBackFill(Color.web("#ffffff"))
                            .addCloseButton();
                    ScrollPane content = new ScrollPane(contentList);
                    content.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    mapping.bindProperty(contentList.prefWidthProperty(), content.widthProperty());
                    Stage build = new BDStageBuilder()
                            .setHeaderBar(header)
                            .setContent(content)
                            .build();
                    build.setWidth(600);
                    build.setHeight(400);
                    build.setAlwaysOnTop(true);
                    build.initOwner(control.getScene().getWindow());
                    return build;
                }
        );

        root.getStyleClass().add("bd-task-control-center");
        title.getStyleClass().add("bd-task-control-center-title");
        bar.getStyleClass().add("bd-task-control-center-bar");
        size.getStyleClass().add("bd-task-control-center-size");

        bar.setMaxWidth(Double.MAX_VALUE);  // 1. 允许 bar 无限扩展
        HBox.setHgrow(bar, Priority.ALWAYS); // 2. 设置增长优先级
        root.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(bar, Priority.ALWAYS);

        root.getChildren().addAll(title, bar, size);
        getChildren().setAll(root);
    }

}
