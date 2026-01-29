package com.xx.UI.complex.stage;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * BDContent 的侧边栏
 * */
class BDSidebar extends BDControl {
    public static final DataFormat BD_SIDE_BAR_FORMAT = new DataFormat("BD_SIDE_BAR_FORMAT");
    final SimpleListProperty<Node> fronFrontSideBarItems = new SimpleListProperty<>(FXCollections.observableArrayList());
    final SimpleListProperty<Node> fronSideBarItems = new SimpleListProperty<>(FXCollections.observableArrayList());
    final SimpleListProperty<Node> afterSideBarItems = new SimpleListProperty<>(FXCollections.observableArrayList());
    final BDContent content;
    final BDDirection direction;
    final Rectangle itemBack = new Rectangle();
    Separator separator;
    Pane fronFrontPane;
    Pane frontPane;
    Pane afterPane;


    BDSidebar(BDContent content, BDDirection direction) {
        this.content = content;
        this.direction = direction;
        if ((direction == BDDirection.LEFT || direction == BDDirection.RIGHT))
            separator = new Separator();
    }

    final List<BDSideBarItem> getItems(){
        List<BDSideBarItem> items = new ArrayList<>();
        fronFrontSideBarItems.stream().filter(item->item instanceof BDSideBarItem).forEach(item->items.add((BDSideBarItem) item));
        fronSideBarItems.stream().filter(item->item instanceof BDSideBarItem).forEach(item->items.add((BDSideBarItem) item));
        afterSideBarItems.stream().filter(item->item instanceof BDSideBarItem).forEach(item->items.add((BDSideBarItem) item));
        return items;
    }
    final BDDragData calculateDragData(double sceneY) {
//        fronFront 区域
        for (int i = 0; i < fronFrontSideBarItems.size(); i++) {
            Node node = fronFrontSideBarItems.get(i);
            Bounds bounds = node.localToScene(node.getLayoutBounds());
            if (sceneY <= bounds.getCenterY())
                return new BDDragData(direction, BDInSequence.FRONT, i);
            else if (sceneY <= bounds.getMaxY())
                return new BDDragData(direction, BDInSequence.FRONT, i + 1);
        }
        if (fronFrontSideBarItems.isEmpty() && fronFrontPane != null) {
            Bounds bounds = fronFrontPane.localToScene(fronFrontPane.getLayoutBounds());
            if (sceneY <= bounds.getMaxY())
                return new BDDragData(direction, BDInSequence.FRONT, 0);
        }
        if (separator != null) {
            Bounds bounds = separator.localToScene(separator.getLayoutBounds());
            if (sceneY <= bounds.getCenterY())
                return new BDDragData(direction, BDInSequence.FRONT, fronFrontSideBarItems.size());
            else if (sceneY <= bounds.getMaxY())
                return new BDDragData(direction, BDInSequence.AFTER, 0);
        }
//front区域

        for (int i = 0; i < fronSideBarItems.size(); i++) {
            Node node = fronSideBarItems.get(i);
            Bounds bounds = node.localToScene(node.getLayoutBounds());
            if (sceneY <= bounds.getCenterY())
                return new BDDragData(direction, BDInSequence.AFTER, i);
            else if (sceneY <= bounds.getMaxY())
                return new BDDragData(direction, BDInSequence.AFTER, i + 1);
        }

        if (fronSideBarItems.isEmpty() && frontPane != null) {
            Bounds bounds = frontPane.localToScene(frontPane.getLayoutBounds());
            if (sceneY <= bounds.getMaxY())
                return new BDDragData(direction, BDInSequence.AFTER, 0);
        }

//        中间空白区域
        if (frontPane != null && afterPane != null) {
            Bounds frontBounds = frontPane.localToScene(frontPane.getLayoutBounds());
            Bounds afterBounds = afterPane.localToScene(afterPane.getLayoutBounds());
            double minY = frontBounds.getMinY();
            double height = afterBounds.getMinY() - minY;
            if (sceneY <= minY + height / 3)
                return new BDDragData(direction, BDInSequence.AFTER, fronSideBarItems.size());
            else if (sceneY <= minY + height * 2 / 3)
                return null;
            else if (sceneY <= minY + height)
                return new BDDragData(BDDirection.BOTTOM, direction.equals(BDDirection.LEFT) ? BDInSequence.FRONT : BDInSequence.AFTER, 0);
        }

//after 区域
        for (int i = 0; i < afterSideBarItems.size(); i++) {
            Node node = afterSideBarItems.get(i);
            Bounds bounds = node.localToScene(node.getLayoutBounds());
            if (sceneY <= bounds.getCenterY())
                return new BDDragData(BDDirection.BOTTOM, direction.equals(BDDirection.LEFT) ? BDInSequence.FRONT : BDInSequence.AFTER, i);
            else if (sceneY <= bounds.getMaxY())
                return new BDDragData(BDDirection.BOTTOM, direction.equals(BDDirection.LEFT) ? BDInSequence.FRONT : BDInSequence.AFTER, i + 1);
        }
        if (afterSideBarItems.isEmpty() && afterPane != null) {
            Bounds bounds = afterPane.localToScene(afterPane.getLayoutBounds());
            if (sceneY <= bounds.getMaxY())
                return new BDDragData(BDDirection.BOTTOM, direction.equals(BDDirection.LEFT) ? BDInSequence.FRONT : BDInSequence.AFTER, 0);
        }

        return null;
    }

    final void addFrontSideNode(Node node, BDInSequence inSequence) {
        switch (inSequence) {
            case FRONT -> {
                fronFrontSideBarItems.add(node);
                if (node instanceof BDSideBarItem item) {
                    item.sidebar.set(this);
                    item.oldIndex = fronFrontSideBarItems.size() - 1;
                }
            }
            case AFTER -> {
                fronSideBarItems.add(node);
                if (node instanceof BDSideBarItem item) {
                    item.sidebar.set(this);
                    item.oldIndex = fronSideBarItems.size() - 1;
                }
            }
            case null -> {
            }
        }
    }

    final void addAfterSideNode(Node node) {
        afterSideBarItems.add(node);
        if (node instanceof BDSideBarItem item) {
            item.sidebar.set(this);
            item.oldIndex = afterSideBarItems.size() - 1;
        }
    }

    public boolean acceptDragItem(BDSideBarItem item) {
        return true;
    }

    void cleanBDSideBarItem(BDSideBarItem item) {
        content.closeSidebar(item);
    }

    final void hideItemBack() {
        removeItemNode(itemBack);
    }

    int addItemNode(BDDirection direction, BDInSequence inSequence, int index, Node item) {
        // 定义所有可能的列表
        List<List<Node>> allLists = List.of(
                fronFrontSideBarItems,
                fronSideBarItems,
                afterSideBarItems
        );

        List<Node> targetList = null;

        // 确定目标列表
        switch (direction) {
            case LEFT, RIGHT -> {
                if (inSequence.equals(BDInSequence.FRONT)) {
                    targetList = fronFrontSideBarItems;
                } else {
                    targetList = fronSideBarItems;
                }
            }
            case BOTTOM -> targetList = afterSideBarItems;
        }

        if (targetList == null) {
            return -1;
        }

        // 在所有列表中查找项目
        List<Node> sourceList = null;
        int currentIndex = -1;

        for (List<Node> list : allLists) {
            int idx = list.indexOf(item);
            if (idx != -1) {
                sourceList = list;
                currentIndex = idx;
                break;
            }
        }

        if (currentIndex != -1) {
            // 项目已存在
            if (sourceList == targetList && currentIndex == index) {
                // 在同一个列表的同一个位置，直接跳过
                return index;
            }

            // 从原列表中移除
            sourceList.remove(currentIndex);

            // 如果是同一个列表，需要调整索引
            if (sourceList == targetList && currentIndex < index) {
                index--;
            }
        }

        // 添加到目标列表的新位置
        if (index > targetList.size()) index = targetList.size();
        targetList.add(index, item);
        if (item instanceof BDSideBarItem item1)
            item1.oldIndex = index;
        return targetList.indexOf(item);
    }


    void addItemNode(BDSideBarItem item, int index) {
        addItemNode(item.getDirection(), item.getInSequence(), index, item);
    }

    //    仅仅修改Node而不动content
    void removeItemNode(Node node) {
        if (!fronSideBarItems.remove(node) && !afterSideBarItems.remove(node)) {
            fronFrontSideBarItems.remove(node);
        }
    }

    void showSideBarItem(BDSideBarItem sideBarItem) {
//        判断是否需要启动动画。
        AtomicBoolean b = new AtomicBoolean(true);
        if (sideBarItem.isWindowOpen()) {
            if (sideBarItem.stage.get() == null) {
                content.closeSidebar(sideBarItem);
                Stage stage = sideBarItem.windowShow();
                stage.initOwner(this.getScene().getWindow());
                stage.show();
            } else sideBarItem.stage.get().show();
        } else {
            switch (sideBarItem.getDirection()) {
                case LEFT, RIGHT -> {
                    if (sideBarItem.getInSequence().equals(BDInSequence.FRONT)) fronFrontSideBarItems.forEach(node -> {
                        if (node != sideBarItem && node instanceof BDSideBarItem item && !item.isWindowOpen() && item.getDirection().equals(sideBarItem.getDirection()) && item.getInSequence().equals(item.getInSequence()) && item.isSelected()) {
                            b.set(false);
                            item.setSelected(false);
                        }
                    });
                    else fronSideBarItems.forEach(node -> {
                        if (node != sideBarItem && node instanceof BDSideBarItem item && !item.isWindowOpen() && item.getDirection().equals(sideBarItem.getDirection()) && item.getInSequence().equals(item.getInSequence()) && item.isSelected()) {
                            b.set(false);
                            item.setSelected(false);
                        }
                    });
                }
                case BOTTOM -> afterSideBarItems.forEach(node -> {
                    if (node != sideBarItem && node instanceof BDSideBarItem item && !item.isWindowOpen() && item.getDirection().equals(sideBarItem.getDirection()) && item.getInSequence().equals(item.getInSequence()) && item.isSelected()) {
                        b.set(false);
                        item.setSelected(false);
                    }
                });
            }
            sideBarItem.tempAnimation = b.get();
            content.showSidebar(sideBarItem);
            sideBarItem.tempAnimation = true;
        }
    }

    void closeSideBarItem(BDSideBarItem sideBarItem) {
        if (sideBarItem.isWindowOpen())
            sideBarItem.windowHide();
        else
            content.closeSidebar(sideBarItem);
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDSideBarSkin(this);
    }

    public class BDDragData {
        BDDirection direction;
        BDInSequence inSequence;
        int index;
        int itemBackIndex;

        public BDDragData(BDDirection direction, BDInSequence inSequence, int index) {
            this.direction = direction;
            this.index = index;
            this.inSequence = inSequence;
            this.itemBackIndex = index;
        }

        public BDDirection getDirection() {
            return direction;
        }

        public void setDirection(BDDirection direction) {
            this.direction = direction;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public BDInSequence getInSequence() {
            return inSequence;
        }

        public void setInSequence(BDInSequence inSequence) {
            this.inSequence = inSequence;
        }

        public int getItemBackIndex() {
            return itemBackIndex;
        }

        public void setItemBackIndex(int itemBackIndex) {
            this.itemBackIndex = itemBackIndex;
        }
    }
}
