package com.xx.UI.complex.BDTabPane;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.input.DataFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 该组件是BDTabPane的子项容器。当分裂时，会生成两个BDTabItem，分别作为左右或上下两个子项。
 *
 */
public class BDTabItem extends BDControl implements BDTabItemImp {
    public static final DataFormat BD_TAB_FORMAT = new DataFormat("BD_TAB_FORMAT");
    //    拖动的tab
    static BDTab dragTab;
    //    为了解决拖动后新窗口的关闭问题。
    static Runnable tempRunnable;
    final SimpleObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(Orientation.VERTICAL);
    //    父节点
    private final SimpleObjectProperty<BDTabItem> parentItem = new SimpleObjectProperty<>();
    //    子节点
    private final SimpleObjectProperty<BDTabItemChild> child = new SimpleObjectProperty<>();
    //    记录显示历史
    private final LinkedHashSet<BDTab> tabDisplayHistory = new LinkedHashSet<>();
    //    tab列表
    private final SimpleListProperty<BDTab> tabs = new SimpleListProperty<>(FXCollections.observableArrayList());
    //    显示的tab
    final SimpleObjectProperty<BDTab> showTab = new SimpleObjectProperty<>();
    BDTabPane splitPane;

    BDTabDir tempDir;
    boolean dirAnimation;

    public BDTabItem(BDTab... tabs) {
        addTabs(tabs);
        getMapping().addDisposeEvent(() -> {
            if (child.get() instanceof BDTabItemChild(BDTabItem first, BDTabItem second)) {
                first.mapping.dispose();
                second.mapping.dispose();
            }
            for (BDTab tab : getTabs()) {
                tab.getMapping().dispose();
            }
            setChild(null);
            parentItem.set(null);
            splitPane = null;
        });
    }

    public BDTabItem(BDTabItem parent, BDTab... tabs) {
        this(tabs);
        this.parentItem.set(parent);
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDTabItemSkin(this);
    }

    @Override
    public boolean canAdd() {
        return child.get() == null;
    }

    @Override
    public boolean addItem(BDTabDir dir, BDTab... tabs) {
        if (tabs == null || tabs.length == 0)
            throw new IllegalArgumentException("Tabs can not be null or empty.");
        if (!canAdd()) return false;

        // 保存当前父节点引用
        BDTabItem oldParent = getParentItem();

        // 创建新父节点和兄弟节点
        BDTabItem newParent = initItem();
        BDTabItem item = new BDTabItem(newParent, tabs);

        // 设置方向
        if (dir.equals(BDTabDir.LEFT) || dir.equals(BDTabDir.RIGHT)) {
            newParent.orientation.set(Orientation.HORIZONTAL);
        } else {
            newParent.orientation.set(Orientation.VERTICAL);
        }

        // 创建子项关系
        BDTabItemChild child;
        if (dir.equals(BDTabDir.LEFT) || dir.equals(BDTabDir.TOP)) {
            child = new BDTabItemChild(item, this);  // 新节点在前
        } else {
            child = new BDTabItemChild(this, item);  // 当前节点在前
        }

        // 设置子节点
        newParent.setChild(child);

        // 更新父子关系
        if (oldParent != null) {
            // 替换旧父节点中的当前节点为新父节点
            BDTabItemChild parentChild = oldParent.getChild();
            if (parentChild != null) {
                if (parentChild.first().equals(this)) {
                    oldParent.child.set(new BDTabItemChild(newParent, parentChild.second()));
                } else if (parentChild.second().equals(this)) {
                    oldParent.child.set(new BDTabItemChild(parentChild.first(), newParent));
                }
            }
            newParent.parentItem.set(oldParent);

            // 更新映射
            oldParent.mapping.addChildren(newParent.mapping);
            oldParent.mapping.removeChild(this.mapping);
        } else {
            // 当前是根节点
            if (splitPane != null) {
                splitPane.setRoot(newParent);
            }
            newParent.parentItem.set(null);
        }

        // 设置当前节点和新节点的父节点
        this.parentItem.set(newParent);
        item.parentItem.set(newParent);

        // 更新映射关系
        newParent.mapping.addChildren(this.mapping);
        newParent.mapping.addChildren(item.mapping);

        // 设置动画标记
        newParent.tempDir = dir;
        newParent.dirAnimation = true;
        newParent.getRoot().splitPane.tabsCount.set(newParent.getRoot().splitPane.tabsCount.get() + tabs.length);
        return true;
    }

    @Override
    public boolean removeItem(BDTabItem item) {
        if (item == null || item == this || (!item.equals(getChild().first()) && !item.equals(getChild().second())))
            return false;
        return item.removeItemFromParent();
    }

    @Override
    public boolean removeItemFromParent() {
        BDTabItem parent = getParentItem();
        if (parent == null || getChild() != null) {
            // 没有父节点或者有子节点，不能移除
            return false;
        }

        // 清理标签
        cleanTabs();

        // 获取兄弟节点
        BDTabItem brother = getBroItem();
        if (brother == null) {
            // 如果没有兄弟节点，这是不应该发生的情况
            return false;
        }

        // 获取祖父节点
        BDTabItem grandParent = parent.getParentItem();

        if (grandParent != null) {
            // 父节点不是根节点

            // 1. 从祖父节点中移除父节点
            grandParent.mapping.removeChild(parent.mapping);

            // 2. 将兄弟节点提升到祖父节点的位置
            BDTabItem parentBrother = parent.getBroItem();
            if (parentBrother != null) {
                // 重新组织祖父节点的子节点
                BDTabItemChild grandParentChild = grandParent.getChild();
                BDTabItemChild newChild;

                if (grandParentChild != null) {
                    if (grandParentChild.first().equals(parent)) {
                        // 父节点是第一个子节点
                        newChild = new BDTabItemChild(brother, grandParentChild.second());
                    } else {
                        // 父节点是第二个子节点
                        newChild = new BDTabItemChild(grandParentChild.first(), brother);
                    }
                    grandParent.setChild(newChild);

                    // 更新父节点引用
                    if (grandParentChild.first().equals(parent)) {
                        brother.parentItem.set(grandParent);
                    } else {
                        brother.parentItem.set(grandParent);
                    }
                }
            }

            // 3. 更新映射
            grandParent.mapping.addChildren(brother.mapping);
        } else {
            // 父节点是根节点
            if (parent.splitPane != null) {
                // 兄弟节点成为新的根节点
                parent.splitPane.setRoot(brother);
                brother.parentItem.set(null);
            }
        }

        // 清理父节点资源
        parent.child.set(null);
        parent.mapping.removeChild(brother.mapping);
        parent.mapping.dispose();
        return true;
    }

    /**
     * 检查并清理空节点
     */
    @Override
    public void check() {
        // 如果是叶节点且没有标签，尝试移除
        if (getChild() == null && tabs.isEmpty()) {
            removeItemFromParent();
        }

        // 递归检查父节点
        if (getParentItem() != null) {
            getParentItem().check();
        }
    }

    private BDTabItem getBroItem() {
        BDTabItem parent = getParentItem();
        if (parent == null) return null;

        BDTabItemChild child = parent.getChild();
        if (child == null) return null;

        if (child.first() == this) {
            return child.second();
        } else if (child.second() == this) {
            return child.first();
        }
        return null;
    }


    @Override
    public BDTabItem initItem() {
        return new BDTabItem();
    }

    public BDTabItem getParentItem() {
        return parentItem.get();
    }

    public ReadOnlyObjectProperty<BDTabItem> parentItemProperty() {
        return parentItem;
    }

    public ReadOnlyObjectProperty<BDTabItemChild> childProperty() {
        return child;
    }

    public BDTabItemChild getChild() {
        return child.get();
    }

    public void setChild(BDTabItemChild child) {
        this.child.set(child);
    }

    public List<BDTab> getTabs() {
        return tabs.get().stream().toList();
    }

    public ReadOnlyListProperty<BDTab> tabsProperty() {
        return tabs;
    }

    public BDTab getTab(int index) {
        return tabs.get().get(index);
    }

    public void addTab(BDTab tab) {
        addTab(tabs.size(), tab);
    }

    public void addTabs(BDTab... tabs) {
        for (BDTab tab : tabs) {
            addTab(tab);
        }
    }


    public void addTab(int index, BDTab tab) {
        if (tab.splitItem.get() != null)
            throw new IllegalArgumentException("tab的父容器不为空,应为null,实际为：" + tab.splitItem.get());
        tab.splitItem.set(this);
        tabs.add(index, tab);
        if (getRoot().splitPane != null)
            getRoot().splitPane.tabsCount.set(getRoot().splitPane.tabsCount.get() + 1);
        tab.show();
    }

    public void removeTab(BDTab tab) {
        if (tab == null) return;

        tab.splitItem.set(null);
        tabs.remove(tab);
        tabDisplayHistory.remove(tab);
        if (getRoot().splitPane != null)
            getRoot().splitPane.tabsCount.set(getRoot().splitPane.tabsCount.get() - 1);

        if (tab.equals(getShowTab())) {
            // 从历史记录中找到最近显示且仍存在的tab
            BDTab nextTab = findNextFromHistory();
            if (nextTab != null) {
                nextTab.show();
            } else if (!tabs.isEmpty()) {
                // 历史记录为空，选择第一个tab
                tabs.getFirst().show();
            } else {
                setShowTab(null);
            }
        }
    }

    private BDTab findNextFromHistory() {
        // 逆序遍历历史记录，找到第一个仍在tabs中的tab
        List<BDTab> historyList = new ArrayList<>(tabDisplayHistory);
        Collections.reverse(historyList);

        for (BDTab tab : historyList) {
            if (tabs.contains(tab)) {
                return tab;
            }
        }
        return null;
    }

    public void cleanTabs() {
        List<BDTab> list = new ArrayList<>(tabs);
        list.forEach(this::removeTab);
        tabDisplayHistory.clear();
        showTab.set(null);
    }

    public Orientation getOrientation() {
        return orientation.get();
    }

    public ReadOnlyObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    public BDTab getShowTab() {
        return showTab.get();
    }

    public void setShowTab(BDTab showTab) {
        if (showTab == null || tabs.contains(showTab)) {
            if (this.showTab.get() != null) {
                // 更新显示历史
                tabDisplayHistory.add(this.showTab.get());
                this.showTab.get().show.set(false);
            }
            // 新显示的tab放到历史记录最前面
            if (showTab != null) {
                tabDisplayHistory.remove(showTab);
                tabDisplayHistory.add(showTab);
                showTab.show.set(true);
            }
            this.showTab.set(showTab);
        } else {
            throw new IllegalArgumentException("tab: '%s' 不在tabs列表中".formatted(
                    showTab.getTitle()));
        }
    }

    public BDTabItem getRoot() {
        BDTabItem item = this;
        while (item.getParentItem() != null)
            item = item.getParentItem();
        return item;
    }

    public void printTree() {
        printTree(0); // 从根节点开始，初始缩进为0
    }

    /**
     * 递归打印树形结构的私有辅助方法。
     *
     * @param depth 当前节点的深度（用于计算缩进）
     */
    private void printTree(int depth) {
        // 根据深度生成缩进字符串
        StringBuilder indent = new StringBuilder();
        indent.append("  ".repeat(Math.max(0, depth))); // 每一层缩进两个空格

        // 打印当前节点的信息
        System.out.print(indent);
        System.out.print("+-- BDTabItem [");

        // 显示当前节点的一些关键状态
        System.out.print("Tabs=" + getTabs().size()); // 包含的标签数
        System.out.print(", ShowTab=" + (getShowTab() != null ? getShowTab().getTitle() : "null"));
        System.out.print(", Orientation=" + getOrientation());
        System.out.print(", HasChild=" + (getChild() != null));

        System.out.println("]");

        // 如果有子节点（即这是一个分裂容器），则递归打印两个分支
        BDTabItemChild child = getChild();
        if (child != null) {
            System.out.println(indent + "    |");
            System.out.println(indent + "    +-- First Branch:");
            child.first().printTree(depth + 2); // 递归打印第一个子节点，增加缩进
            System.out.println(indent + "    |");
            System.out.println(indent + "    +-- Second Branch:");
            child.second().printTree(depth + 2); // 递归打印第二个子节点，增加缩进
        } else {
            // 如果没有子节点，打印所包含的所有标签的标题
            if (!getTabs().isEmpty()) {
                System.out.println(indent + "    |-- Contained Tabs:");
                for (BDTab tab : getTabs()) {
                    System.out.println(indent + "        |-- " + tab.getTitle() +
                            (tab.equals(getShowTab()) ? " (Showing)" : ""));
                }
            }
        }
    }

    public ReadOnlyObjectProperty<BDTab> showTabProperty() {
        return showTab;
    }
}
