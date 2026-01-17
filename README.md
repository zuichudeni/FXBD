# FXBD简介：一些有趣的JavaFX自定义组件集合

## 目前有的内容

### 1. BDMapping
- **功能**：为了方便管理property的绑定、监听、解绑、移除以及event的注册与删除而创建的工具类BDMapping。
- **特点**：
  - 可以调用`dispose`方法一键清除所有property与event
  - 也可指定清除某一类property或者event
  - 可以使代码更加简洁

#### 示例对比

**传统方式**：
```java
Text t = new Text();
Text t1 = new Text();
Text t2 = new Text();

// 绑定属性    监听属性变化    添加事件
t2.textProperty().bindBidirectional(t.textProperty());
t1.textProperty().bind(t2.textProperty());
ChangeListener<String> stringChangeListener = (obs, oldVal, newVal) -> System.out.println("监听到属性变化：" + newVal);
t2.textProperty().addListener(stringChangeListener);
EventHandler<MouseEvent> eventHandler = _ -> System.out.println("点击了t");
t.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);

// 解绑，移除等一切操作
t2.textProperty().unbindBidirectional(t.textProperty());
t1.textProperty().unbind();
t2.textProperty().removeListener(stringChangeListener);
t.removeEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
```

**使用BDMapping**：
```java
Text t = new Text();
Text t1 = new Text();
Text t2 = new Text();
BDMapping mapping = new BDMapping();

// 绑定属性    监听属性变化    添加事件
mapping.bindBidirectional(t2.textProperty(), t.textProperty())
        .bindProperty(t1.textProperty(), t2.textProperty())
        .addListener(t2.textProperty(), (_, _, nv) -> System.out.println("监听到属性变化：" + nv))
        .addEventHandler(t, MouseEvent.MOUSE_CLICKED,  _ -> System.out.println("点击了t"));

// 解绑，移除等一切操作
mapping.dispose();
```

### 2. BDTextArea
- **功能**：通过ListView实现的富文本组件，基于虚拟滚动实现
- **特点**：
  - 可以根据自定义的规则进行文本的渲染
  - 目前支持ANTRL4语法规则和正则表达式规则两种方式渲染
  - 实现了检索功能

### 3. BDTabPane
- **功能**：参考IntelliJ IDEA的TabPane实现的布局

### 4. BDStage
- **功能**：参考IntelliJ IDEA的界面布局实现的窗口类

体验请参考demo目录下相应的demo文件。

## 注意
使用时请添加虚拟机参数：
```
-XX:+UseZGC
-Djavafx.enablePreview=true
--enable-native-access=javafx.graphics
-Djavafx.suppressPreviewWarning=true
```