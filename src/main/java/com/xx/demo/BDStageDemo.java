package com.xx.demo;

import com.xx.UI.basic.BDButton;
import com.xx.UI.complex.stage.*;
import com.xx.UI.complex.textArea.view.BDTextArea;
import com.xx.UI.complex.textArea.view.BDTextAreaSearch;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.Util;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * BDStage演示程序
 * 优化后的代码，具有更好的可读性和可维护性
 */
public class BDStageDemo extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 构建标题栏
        BDHeaderBarBuilder headerBarBuilder = new BDHeaderBarBuilder()
                .addIcon(Util.getImageView(25, BDIcon.IDEA_MODULE))
                .addTitle("BDStageDemo")
                .addMinimizeButton()
                .addMaximizeButton()
                .addCloseButton();

//        构建内容区域
        BDSideContent sideContent = new BDSideContent();
        sideContent.setTitle("文件");
        BDSideBarItem item = new BDSideBarItem("项目",Util.getImageView(30,BDIcon.FOLDER),Util.getImageView(30,BDIcon.FOLDER_DARK),BDDirection.LEFT,BDInSequence.FRONT,sideContent);
        BDContentBuilder contentBuilder = new BDContentBuilder()
                .addSideNode(BDDirection.LEFT, BDInSequence.FRONT,new BDButton())
                .addSideNode(BDDirection.LEFT,BDInSequence.AFTER,item)
                .addCenterNode(new BDTextAreaSearch(new BDTextArea()));
        // 构建主窗口
        BDStageBuilder stageBuilder = new BDStageBuilder()
                .setContent(contentBuilder.build())
                .setStyle(Util.getResourceUrl("/css/cupertino-light.css"))
                .setHeaderBar(headerBarBuilder);

        // 显示窗口
        stageBuilder.build().show();
    }

}