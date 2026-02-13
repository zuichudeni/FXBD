package com.xx.demo;

import com.dlsc.fxmlkit.fxml.FxmlKit;
import com.xx.UI.basic.button.BDButton;
import com.xx.UI.basic.progressBar.BDTask;
import com.xx.UI.basic.progressBar.BDTaskControlCenter;
import com.xx.UI.complex.BDTabPane.BDTab;
import com.xx.UI.complex.BDTabPane.BDTabItem;
import com.xx.UI.complex.BDTabPane.BDTabPane;
import com.xx.UI.complex.stage.*;
import com.xx.UI.complex.textArea.content.segment.NodeSegment;
import com.xx.UI.complex.textArea.view.BDTextArea;
import com.xx.UI.complex.textArea.view.BDTextAreaSearch;
import com.xx.UI.complex.textArea.view.dataFormat.example.java.BDJavaTextInitFactory;
import com.xx.UI.complex.textArea.view.dataFormat.example.json.BDJsonTextInitFactory;
import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.BDMapping;
import com.xx.UI.util.Util;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

/**
 * BDStage 演示程序 - 增强版
 */
public class BDStageDemo extends Application {

    private final BDMapping globalMapping = new BDMapping();
    BDTextArea consoleTextArea = initConsoleTextArea();

    @Override
    public void start(Stage stage) throws Exception {
        FxmlKit.enableDevelopmentMode();
        FxmlKit.setApplicationUserAgentStylesheet(Util.getResourceUrl("/css/cupertino-light.css"));
        //        创建工具
        BDSideBarItem dialogItem = getDialogItem();
        BDSideBarItem badgeItem = getBadgeItem();
        BDSideBarItem projectItem = getProjectItem();
        BDSideBarItem searchItem = getSearchItem();
        BDSideBarItem gitItem = getGitItem();
        BDSideBarItem bookmarkItem = getBookmarkItem();
        BDSideBarItem settingItem = getSettingItem();
        BDSideBarItem helpItem = getHelpItem();
        BDSideBarItem consoleItem = getConsoleItem();
        BDSideBarItem outputItem = getOutputItem();
        BDSideBarItem debugItem = getDebugItem();
        BDTabItem rootTabItem = getRootTabItem();
        HBox toolbar = getToolbar(rootTabItem);

        BDTaskControlCenter controlCenter = new BDTaskControlCenter();
        controlCenter.setPadding(new Insets(10));
        globalMapping.binding(controlCenter.contentsProperty().emptyProperty().not(), controlCenter.visibleProperty(), controlCenter.managedProperty());

        BDSideBarItem taskItem = getTaskItem(controlCenter);

        // 构建标题栏
        BDHeaderBarBuilder headerBarBuilder = new BDHeaderBarBuilder()
                .addIcon(Util.getImageView(25, BDIcon.IDEA_MODULE))
                .addTitle("BD Stage Demo 测试 ")
                .addCenter(toolbar)
                .addMinimizeButton()
                .addMaximizeButton()
                .addCloseButton();

        // 构建内容8
        BDContentBuilder contentBuilder = new BDContentBuilder()
                .addSideNode(dialogItem, badgeItem, projectItem, searchItem, gitItem, bookmarkItem, helpItem, settingItem, consoleItem, outputItem, debugItem, taskItem)
                .addSideNode(BDDirection.BOTTOM, BDInSequence.AFTER, controlCenter)
                .addCenterNode(new BDTabPane(rootTabItem));

        // 构建主窗口
        Stage bdStage = new BDStageBuilder()
                .setHeaderBar(headerBarBuilder)
                .setContent(contentBuilder.build())
                .setSize(1200, 1000)
                .build();
        // 显示窗口
        bdStage.setAlwaysOnTop(true);
        bdStage.show();
//        添加事件
        windowAction(bdStage, rootTabItem);
    }

    private BDSideBarItem getDialogItem() {
        BDSideContent sideContent = new BDSideContent();
        BDSideBarItem dialogItem = new BDSideBarItem("弹窗", Util.getImageView(30, BDIcon.INFORMATION_DIALOG), Util.getImageView(20, BDIcon.INFORMATION_DIALOG_DARK), BDDirection.BOTTOM, BDInSequence.FRONT, sideContent);
        Button dialog1 = new Button("弹窗一");
        globalMapping.addEventHandler(dialog1, ActionEvent.ACTION, _ -> {
            Button ok = new Button("确定");
            Stage stage = new BDDialog()
                    .setHeader(new BDHeaderBarBuilder().addCloseButton()
                            .addTitle("弹窗测试")
                            .addIcon(Util.getImageView(40, BDIcon.FINAL_MARK)))
                    .setHeaderText("我是header text，右边的是header graphic。")
                    .setHeaderGraphic(Util.getImageView(60, BDIcon.INFORMATION_DIALOG))
                    .setContent(new Text("我是text content"))
                    .setExpandContent(new Text("我是被隐藏起来的text expand content"))
                    .addAfterActionNode(ok)
                    .build();
            stage.setAlwaysOnTop(true);
            stage.show();
        });

        HBox root = new HBox(20, dialog1);
        sideContent.setTitle("弹窗测试");
        sideContent.setContent(root);
        return dialogItem;
    }

    private BDSideBarItem getBadgeItem() {
        HBox root = new HBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        BDSideContent sideContent = new BDSideContent();
        sideContent.setContent(root);
        sideContent.setTitle("badge 测试");
        BDSideBarItem badgeItem = new BDSideBarItem("勋章", Util.getImageView(30, BDIcon.AREA_LABEL),
                Util.getImageView(30, BDIcon.AREA_LABEL_DARK), BDDirection.RIGHT, BDInSequence.AFTER, sideContent);
        BDButton addBadge = new BDButton();
        addBadge.setSelectable(false);
        addBadge.setDefaultGraphic(Util.getImageView(25, BDIcon.ADD));
        BDButton subBadge = new BDButton();
        subBadge.setSelectable(false);
        subBadge.setDefaultGraphic(Util.getImageView(25, BDIcon.REMOVE_14X14));
        globalMapping.addEventHandler(subBadge, ActionEvent.ACTION, _ -> badgeItem.setBadge(Math.max(0, badgeItem.getBadge() - 1)))
                .addEventHandler(addBadge, ActionEvent.ACTION, _ -> badgeItem.setBadge(Math.max(0, badgeItem.getBadge() + 1)));
        root.getChildren().addAll(subBadge, addBadge);
        return badgeItem;
    }

    private BDSideBarItem getTaskItem(BDTaskControlCenter controlCenter) {
        BDSideContent taskControl = new BDSideContent();
        taskControl.setTitle("任务控制中心");
        BDButton insertTask1 = new BDButton("插入任务", Util.getImageView(20, BDIcon.ADD_DARK));
        insertTask1.setSelectable(false);
        taskControl.setContent(insertTask1);
        globalMapping.addEventHandler(insertTask1, ActionEvent.ACTION, _ -> controlCenter.pushTask(getSimpleTask(), true, true));
        return new BDSideBarItem("任务", Util.getImageView(25, BDIcon.RUN), Util.getImageView(25, BDIcon.RUN_DARK), BDDirection.BOTTOM, BDInSequence.FRONT, taskControl);
    }

    private BDTask<String> getSimpleTask() {
        BDTask<String> task = new BDTask<>() {
            @Override
            protected String work() throws InterruptedException, TimeoutException {
                for (int i = 0; i < 101; i++) {
                    /*
                     * 必须调用此函数！！！只有这样，暂停、超时等功能才生效。
                     * */
                    checkState();
                    Thread.sleep(100);
                    updateProgress(i, 100);
                    updateMessage("正在加载(%d/%d)".formatted(i, 100));
                    updateTitle("更新测试");
                }
                return "done";
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                updateMessage("更新成功");
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                updateMessage("取消任务");
            }

            @Override
            protected void failed() {
                super.failed();
                updateMessage("任务执行失败");
            }
        };
        new Thread(task).start();
        return task;
    }

    private BDSideBarItem getHelpItem() {
        // 构建内容区域 - 帮助侧边栏 (RIGHT, FRONT)
        BDSideContent helpContent = new BDSideContent();
        helpContent.setTitle("帮助和文档");
        BDTextArea helpTextArea = new BDTextArea("""
                欢迎使用BDUI框架！
                
                核心组件：
                1. BDStage - 窗口管理
                2. BDTabPane - 标签页管理
                3. BDTextArea - 文本编辑
                4. BDMapping - 绑定管理
                
                快速开始：
                • 查看示例代码
                • 阅读API文档
                • 加入社区讨论
                
                技术支持：
                • Email: support@xx.com
                • GitHub: github.com/xx/BDUI
                """);
        helpContent.setContent(helpTextArea);
        return new BDSideBarItem("帮助", Util.getImageView(30, BDIcon.HELP), Util.getImageView(30, BDIcon.HELP_DARK), BDDirection.RIGHT, BDInSequence.FRONT, helpContent);
    }

    private BDSideBarItem getGitItem() {
        // 构建内容区域 - Git侧边栏 (LEFT, AFTER)
        BDSideContent gitContent = new BDSideContent();
        gitContent.setTitle("版本控制");
        BDTextArea gitTextArea = new BDTextArea("""
                Git状态：
                • 主分支: main
                • 当前修改: 2个文件
                • 未暂存: BDMappingDemo.java
                • 已暂存: BDStageDemo.java
                
                提交记录：
                • 修复BDMapping解绑问题
                • 优化BDStage性能
                • 添加BDTabPane示例
                """);
        gitContent.setContent(gitTextArea);
        return new BDSideBarItem("Git", Util.getImageView(30, BDIcon.FOLDER_GITHUB), Util.getImageView(30, BDIcon.FOLDER_GITHUB_DARK), BDDirection.LEFT, BDInSequence.AFTER, gitContent);
    }

    private BDSideBarItem getSearchItem() {
        // 构建内容区域 - 搜索侧边栏 (LEFT, AFTER)
        BDSideContent searchContent = new BDSideContent();
        searchContent.setTitle("智能搜索");
        BDTextArea searchTextArea = new BDTextArea("""
                搜索功能演示：
                1. 全局搜索 (Ctrl+Shift+F)
                2. 文件内搜索 (Ctrl+F)
                3. 替换功能 (Ctrl+R)
                4. 正则表达式搜索
                
                搜索历史：
                • BDTextArea.java
                • BDMapping.java
                • BDStageBuilder.java
                """);
        searchContent.setContent(new BDTextAreaSearch(searchTextArea));
        return new BDSideBarItem("搜索", Util.getImageView(30, BDIcon.SEARCH), Util.getImageView(30, BDIcon.SEARCH_DARK), BDDirection.LEFT, BDInSequence.AFTER, searchContent);
    }

    private BDSideBarItem getDebugItem() {
        // 构建内容区域 - 调试侧边栏 (BOTTOM, FRONT)
        BDSideContent debugContent = new BDSideContent();
        debugContent.setTitle("调试工具");
        VBox debugVBox = new VBox(10);

        // 断点列表
        ListView<String> breakpointList = new ListView<>();
        breakpointList.getItems().addAll("BDMappingDemo.java:25", "BDStageDemo.java:120", "BDTextAreaDemo.java:45", "BDTabPaneDemo.java:80");

        // 调试按钮
        HBox debugButtons = new HBox(10);
        BDButton btnStep = new BDButton("单步执行");
        BDButton btnContinue = new BDButton("继续");
        BDButton btnStop = new BDButton("停止");
        debugButtons.getChildren().addAll(btnStep, btnContinue, btnStop);
        debugButtons.setAlignment(Pos.CENTER);

        // 变量监视
        BDTextArea watchArea = new BDTextArea("""
                监视变量：
                • globalMapping: BDMapping实例
                • stage: Stage实例
                • consoleTextArea: BDTextArea实例
                """);

        debugVBox.getChildren().addAll(new Label("断点列表:"), breakpointList, debugButtons, new Separator(), new Label("变量监视:"), watchArea);

        debugContent.setContent(debugVBox);
        return new BDSideBarItem("调试", Util.getImageView(30, BDIcon.DEBUG), Util.getImageView(30, BDIcon.DEBUG_DARK), BDDirection.BOTTOM, BDInSequence.FRONT, debugContent);
    }

    private BDSideBarItem getOutputItem() {
        // 构建内容区域 - 输出侧边栏 (BOTTOM, AFTER)
        BDSideContent outputContent = new BDSideContent();
        outputContent.setTitle("构建和输出");
        BDTextArea outputTextArea = new BDTextArea("""
                === 构建开始 ===
                [INFO] 清理目标目录...
                [INFO] 编译Java文件...
                [INFO] 编译成功: 15个文件
                [INFO] 打包应用...
                [INFO] 构建完成!
                =================
                
                性能统计：
                • 编译时间: 2.3秒
                • 内存使用: 256MB
                • 文件数量: 15个
                """);
        outputContent.setContent(outputTextArea);
        return new BDSideBarItem("输出", Util.getImageView(30, BDIcon.DBMS_OUTPUT), Util.getImageView(30, BDIcon.DBMS_OUTPUT_DARK), BDDirection.BOTTOM, BDInSequence.AFTER, outputContent);
    }


    private void windowAction(Stage bdStage, BDTabItem rootTabItem) {
        // 添加窗口事件监听器
        globalMapping.addEventHandler(bdStage, javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST, _ -> {
            System.out.println("窗口关闭请求 - 保存工作...");
            // 可以在这里添加保存逻辑
        }).addEventHandler(bdStage, javafx.stage.WindowEvent.WINDOW_SHOWN, _ -> {
            System.out.println("BDStage窗口已显示");
            consoleTextArea.appendText("[系统] BDStageDemo 启动成功\n");
            consoleTextArea.appendText("[系统] 加载了 " + rootTabItem.getTabs().size() + " 个标签页\n");
        });
    }

    private HBox getToolbar(BDTabItem rootTabItem) {
        // 创建带有工具栏的中心区域

        // 工具栏
        HBox toolbar = new HBox(10);
        BDButton btnNew = new BDButton();
        btnNew.setDefaultGraphic(Util.getImageView(25, BDIcon.OPEN_NEW_TAB_DARK));
        btnNew.getStyleClass().add("icon");
        btnNew.setHoverFill(Color.web("#3D3E43"));
        btnNew.setPressedFill(Color.web("#3D3E43"));
        btnNew.setSelectable(false);
        BDButton btnOpen = new BDButton();
        btnOpen.setDefaultGraphic(Util.getImageView(25, BDIcon.OPEN_DARK));
        btnOpen.getStyleClass().add("icon");
        btnOpen.setHoverFill(Color.web("#3D3E43"));
        btnOpen.setPressedFill(Color.web("#3D3E43"));
        btnOpen.setSelectable(false);
        BDButton btnSave = new BDButton();
        btnSave.setDefaultGraphic(Util.getImageView(25, BDIcon.SAVE_DARK));
        btnSave.getStyleClass().add("icon");
        btnSave.setHoverFill(Color.web("#3D3E43"));
        btnSave.setPressedFill(Color.web("#3D3E43"));
        btnSave.setSelectable(false);
        BDButton btnRun = new BDButton();
        btnRun.setDefaultGraphic(Util.getImageView(25, BDIcon.RUN_DARK));
        btnRun.getStyleClass().add("icon");
        btnRun.setHoverFill(Color.web("#3D3E43"));
        btnRun.setPressedFill(Color.web("#3D3E43"));
        btnRun.setSelectable(false);
        BDButton btnDebug = new BDButton();
        btnDebug.setDefaultGraphic(Util.getImageView(25, BDIcon.DEBUG_DARK));
        btnDebug.getStyleClass().add("icon");
        btnDebug.setHoverFill(Color.web("#3D3E43"));
        btnDebug.setPressedFill(Color.web("#3D3E43"));
        btnDebug.setSelectable(false);
        toolbar.getChildren().addAll(btnNew, btnOpen, btnSave, btnRun, btnDebug);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(1), new BorderWidths(0, 0, 1, 0))));

        // 绑定工具栏按钮事件
        globalMapping.addEventHandler(btnNew, ActionEvent.ACTION, _ -> {
            System.out.println("新建文件");
            rootTabItem.addTab(createDemoTab("新文件", "NewFile.java", "// 新文件内容"));
        });
        return toolbar;
    }

    private BDTabItem getRootTabItem() {
        // 创建中心内容区域 - 带标签页的文本编辑器
        BDTabItem rootTabItem = new BDTabItem();

        // 添加示例标签页
        try {
            // 添加Java文件标签页
            Path javaPath = Util.getPath("src/main/java/com/xx/UI/complex/stage/BDStageBuilder.java");
            rootTabItem.addTab(createFileTab(javaPath));

            // 添加JSON文件标签页
            Path jsonPath = Util.getPath("src/main/resources/business.json");
            if (Files.exists(jsonPath)) {
                rootTabItem.addTab(createFileTab(jsonPath));
            }

            // 添加其他示例标签页
            rootTabItem.addTab(createDemoTab("BDMapping示例", BDMappingDemo.class.getSimpleName() + ".java", """
                    package com.xx.demo;
                    
                    import com.xx.UI.util.BDMapping;
                    import javafx.beans.property.SimpleStringProperty;
                    
                    public class BDMappingDemo {
                        public static void main(String[] args) {
                            // 创建BDMapping实例管理所有绑定
                            BDMapping mapping = new BDMapping();
                    
                            SimpleStringProperty prop1 = new SimpleStringProperty("初始值");
                            SimpleStringProperty prop2 = new SimpleStringProperty();
                    
                            // 双向绑定
                            mapping.bindBidirectional(prop1, prop2);
                    
                            // 添加监听器
                            mapping.addListener(prop1, (obs, oldVal, newVal) -> {
                                System.out.println("属性变化: " + oldVal + " -> " + newVal);
                            });
                    
                            prop1.set("新值"); // 会触发监听器
                            System.out.println("prop2的值: " + prop2.get());
                    
                            // 清理所有绑定
                            mapping.dispose();
                        }
                    }
                    """));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootTabItem;
    }

    private BDSideBarItem getConsoleItem() {
        // 构建内容区域 - 控制台侧边栏 (BOTTOM, FRONT)
        BDSideContent consoleContent = new BDSideContent();
        consoleContent.setTitle("控制台输出");
        consoleContent.setContent(new BDTextAreaSearch(consoleTextArea));
        return new BDSideBarItem("控制台", Util.getImageView(30, BDIcon.CONSOLE_RUN), Util.getImageView(30, BDIcon.CONSOLE_RUN_DARK), BDDirection.BOTTOM, BDInSequence.FRONT, consoleContent);
    }

    private BDSideBarItem getSettingItem() {
        // 构建内容区域 - 设置侧边栏 (RIGHT, AFTER)
        BDSideContent settingContent = new BDSideContent();
        settingContent.setTitle("系统设置和配置");
        VBox settingVBox = new VBox(10);

        // 主题选择
        ComboBox<String> themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll("浅色主题", "深色主题", "蓝色主题", "绿色主题");
        themeComboBox.setValue("浅色主题");
        globalMapping.addEventHandler(themeComboBox, ActionEvent.ACTION, e -> {
            System.out.println("切换主题为: " + themeComboBox.getValue());
        });

        // 字体大小选择
        Slider fontSizeSlider = new Slider(10, 24, 14);
        fontSizeSlider.setShowTickLabels(true);
        fontSizeSlider.setShowTickMarks(true);
        fontSizeSlider.setMajorTickUnit(2);

        // 自动保存开关
        ToggleSwitch autoSaveSwitch = new ToggleSwitch("自动保存");
        autoSaveSwitch.setSelected(true);

        // 快捷键显示
        BDTextArea shortcutArea = new BDTextArea("""
                常用快捷键：
                Ctrl+S: 保存文件
                Ctrl+F: 查找
                Ctrl+R: 替换
                Ctrl+D: 复制行
                Ctrl+/: 注释行
                Ctrl+Shift+F: 全局搜索
                """);

        settingVBox.getChildren().addAll(new Label("主题:"), themeComboBox, new Label("字体大小:"), fontSizeSlider, autoSaveSwitch, new Separator(), new Label("快捷键列表:"), shortcutArea);

        settingContent.setContent(settingVBox);
        return new BDSideBarItem("设置", Util.getImageView(30, BDIcon.SETTINGS), Util.getImageView(30, BDIcon.SETTINGS_DARK), BDDirection.RIGHT, BDInSequence.AFTER, settingContent);
    }

    private BDSideBarItem getBookmarkItem() {
        // 构建内容区域 - 书签侧边栏 (RIGHT, FRONT)
        BDSideContent bookmarkContent = new BDSideContent();
        bookmarkContent.setTitle("书签和收藏");
        BDTextArea bookmarkTextArea = initBookmarkTextArea();
        bookmarkContent.setContent(bookmarkTextArea);
        return new BDSideBarItem("书签", Util.getImageView(30, BDIcon.BOOKMARK), Util.getImageView(30, BDIcon.BOOKMARK_DARK), BDDirection.RIGHT, BDInSequence.FRONT, bookmarkContent);
    }

    private BDSideBarItem getProjectItem() {
        // 构建内容区域 - 项目侧边栏 (LEFT, FRONT)
        BDSideContent fileContent = new BDSideContent();
        fileContent.setTitle("项目文件管理器");
        BDTextArea fileTextArea = initProjectTextArea();
        fileContent.setContent(new BDTextAreaSearch(fileTextArea));
        return new BDSideBarItem("项目", "Alt+1", Util.getImageView(30, BDIcon.FOLDER), Util.getImageView(30, BDIcon.FOLDER_DARK), BDDirection.LEFT, BDInSequence.FRONT, fileContent);
    }

    /**
     * 初始化项目文本区域
     */
    private BDTextArea initProjectTextArea() {
        BDTextArea area = new BDTextArea();

        // 添加交互式节点
        area.appendNode(new NodeSegment<>(" ", _ -> {
            BDButton refreshBtn = new BDButton("刷新");
            globalMapping.addEventHandler(refreshBtn, ActionEvent.ACTION, e -> area.appendText("[刷新] 项目文件已更新\n"));
            return refreshBtn;
        }));

        area.appendText("\n项目结构：\n");
        area.appendText("📁 src/\n");
        area.appendText("  📁 main/\n");
        area.appendText("    📁 java/com/xx/\n");
        area.appendText("      📁 UI/\n");
        area.appendText("        📁 basic/\n");
        area.appendText("        📁 complex/\n");
        area.appendText("        📁 util/\n");
        area.appendText("    📁 resources/\n");
        area.appendText("📁 test/\n");
        area.appendText("📁 target/\n");
        area.appendText("📄 pom.xml\n");
        area.appendText("📄 README.md\n");

        return area;
    }

    /**
     * 初始化书签文本区域
     */
    private BDTextArea initBookmarkTextArea() {
        BDTextArea area = new BDTextArea();

        // 设置JSON语法高亮
        area.setTextInitFactory(new BDJsonTextInitFactory(area));

        area.appendText("""
                {
                  "bookmarks": [
                    {
                      "name": "BDUI官方文档",
                      "url": "https://bdui.xx.com/docs",
                      "category": "文档"
                    },
                    {
                      "name": "GitHub仓库",
                      "url": "https://github.com/xx/BDUI",
                      "category": "开发"
                    },
                    {
                      "name": "API参考",
                      "url": "https://bdui.xx.com/api",
                      "category": "文档"
                    },
                    {
                      "name": "问题反馈",
                      "url": "https://github.com/xx/BDUI/issues",
                      "category": "社区"
                    }
                  ],
                  "recent": [
                    "BDStageBuilder.java",
                    "BDMappingDemo.java",
                    "BDTabPaneDemo.java"
                  ]
                }
                """);

        return area;
    }

    /**
     * 初始化控制台文本区域
     */
    private BDTextArea initConsoleTextArea() {
        BDTextArea area = new BDTextArea();

        // 添加控制按钮
        area.appendNode(new NodeSegment<>(" ", _ -> {
            BDButton clearBtn = new BDButton("清空");
            globalMapping.addEventHandler(clearBtn, ActionEvent.ACTION, _ -> area.delete(0, area.getLength()));
            return clearBtn;
        }));

        area.appendNode(new NodeSegment<>(" ", _ -> {
            BDButton copyBtn = new BDButton("复制");
            globalMapping.addEventHandler(copyBtn, ActionEvent.ACTION, _ -> System.out.println("复制控制台内容"));
            return copyBtn;
        }));

        area.appendText("\n=== 控制台日志 ===\n");
        area.appendText("[INFO] 应用程序启动\n");
        area.appendText("[INFO] 加载配置文件\n");
        area.appendText("[INFO] 初始化UI组件\n");
        area.appendText("[DEBUG] BDStage构建完成\n");

        return area;
    }

    /**
     * 创建文件标签页
     */
    private BDTab createFileTab(Path path) {
        BDTab tab = new BDTab(path.getFileName().toString());

        // 设置图标
        String fileName = path.toString();
        BDIcon icon = fileName.endsWith(".java") ? BDIcon.JAVA : fileName.endsWith(".json") ? BDIcon.JSON : fileName.endsWith(".xml") ? BDIcon.XML : BDIcon.FILE_UNREAD;
        tab.setGraphic(Util.getImageView(20, icon));

        try {
            // 读取文件内容
            String content = Files.readString(path);
            BDTextArea textArea = new BDTextArea();
            textArea.insertText(0, content);

            // 设置语法高亮
            if (fileName.endsWith(".java")) {
                textArea.setTextInitFactory(new BDJavaTextInitFactory(textArea));
            } else if (fileName.endsWith(".json")) {
                textArea.setTextInitFactory(new BDJsonTextInitFactory(textArea));
            }

            // 添加搜索功能
            BDTextAreaSearch search = new BDTextAreaSearch(textArea);
            tab.setContent(search);

            // 绑定清理
            tab.getMapping().addChildren(search.getMapping());

        } catch (IOException e) {
            BDTextArea errorArea = new BDTextArea("无法读取文件: " + e.getMessage());
            tab.setContent(errorArea);
        }

        return tab;
    }

    /**
     * 创建示例标签页
     */
    private BDTab createDemoTab(String title, String fileName, String content) {
        BDTab tab = new BDTab(title);
        tab.setGraphic(Util.getImageView(20, BDIcon.CLASS));

        BDTextArea textArea = new BDTextArea();
        textArea.insertText(0, content);

        // 根据文件类型设置语法高亮
        if (fileName.endsWith(".java")) {
            textArea.setTextInitFactory(new BDJavaTextInitFactory(textArea));
        } else if (fileName.endsWith(".json")) {
            textArea.setTextInitFactory(new BDJsonTextInitFactory(textArea));
        }

        BDTextAreaSearch search = new BDTextAreaSearch(textArea);
        tab.setContent(search);
        tab.getMapping().addChildren(search.getMapping());

        return tab;
    }

    @Override
    public void stop() throws Exception {
        // 清理所有绑定和监听器
        globalMapping.dispose();
        System.out.println("应用程序关闭");
        super.stop();
    }

    // 自定义ToggleSwitch组件（简化版）
    static class ToggleSwitch extends HBox {
        private final Button button = new Button();

        public ToggleSwitch(String text) {
            Label label = new Label();
            label.setText(text);
            label.setStyle("-fx-font-size: 14px;");

            button.setPrefWidth(40);
            button.setPrefHeight(20);
            updateButtonStyle(false);

            getChildren().addAll(label, button);
            setSpacing(10);
            setAlignment(Pos.CENTER_LEFT);

            button.setOnAction(_ -> setSelected(!isSelected()));
        }

        public boolean isSelected() {
            return button.getStyle().contains("-fx-background-color: #4CAF50");
        }

        public void setSelected(boolean selected) {
            updateButtonStyle(selected);
        }

        private void updateButtonStyle(boolean selected) {
            if (selected) {
                button.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 10;");
                button.setText("ON");
                button.setTextFill(Color.WHITE);
            } else {
                button.setStyle("-fx-background-color: #CCCCCC; -fx-background-radius: 10;");
                button.setText("OFF");
                button.setTextFill(Color.BLACK);
            }
        }
    }
}