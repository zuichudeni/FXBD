package com.xx.UI.complex.tree;

import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.Util;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public interface BDTreeCellInitFactory<T> {
    static int getDepth(TreeItem<?> item) {
        Stack<TreeItem<?>> stack = new Stack<>();
        stack.push(item);
        int depth = 0;
        while (!stack.isEmpty()) {
            TreeItem<?> pop = stack.pop();
            if (pop.getParent() != null)
                stack.push(pop.getParent());
            depth++;
        }
        return depth == 0 ? 1 : depth;
    }

    static <T> LinkedHashMap<T, String> toMap(TreeItem<T> rootItem, BDTreeCellInitFactory<T> cellInitFactory) {
        LinkedHashMap<T, String> map = new LinkedHashMap<>();
        toMap(rootItem, cellInitFactory, map);
        return map;
    }

    static <T> TreeItem<T> getItem(TreeItem<T> rootItem, T t) {
        Stack<TreeItem<T>> stack = new Stack<>();
        stack.push(rootItem);
        while (!stack.isEmpty()) {
            TreeItem<T> pop = stack.pop();
            if (pop.getValue().equals(t))
                return pop;
            pop.getChildren().forEach(stack::push);
        }
        return null;
    }

    static int getIndex(TreeItem<?> root, TreeItem<?> current) {
        Stack<TreeItem<?>> stack = new Stack<>();
        stack.push(root);
        AtomicInteger index = new AtomicInteger(0);
        while (!stack.isEmpty()) {
            TreeItem<?> pop = stack.pop();
            if (pop.equals(current))
                return index.get();
            if (pop.isExpanded()) {
                for (int i = pop.getChildren().size() - 1; i >= 0; i--) {
                    stack.push(pop.getChildren().get(i));
                }
            }
            index.incrementAndGet();
        }
        return -1;
    }


    static void openTo(TreeItem<?> item) {
        TreeItem<?> parent = item.getParent();
        while (parent != null) {
            parent.setExpanded(true);
            parent = parent.getParent();
        }
    }

    static <T> void selectAndShow(TreeView<T> treeView, T value) {
        TreeItem<T> root = treeView.getRoot();
        if (root == null) return;
        TreeItem<T> current = getItem(root, value);
        openTo(Objects.requireNonNull(current));
        int index = getIndex(root, current);
        treeView.getSelectionModel().clearAndSelect(index);
        int[] range = getVisibleRowRange(treeView);
        if (range[0] != -1 && index >= range[0] && index < range[1]) return;
        if (range[0] == range[1]) {
            index -= 1;
        } else if (index < range[0])
            index -= 2;
        else if (index > range[1])
            index -= (range[1] - range[0]) - 3;
        else if (index == range[1])
            index -= (range[1] - range[0]) - 1;
        treeView.scrollTo(index);

    }

    static int[] getVisibleRowRange(TreeView<?> treeView) {
        VirtualFlow<?> virtualFlow = (VirtualFlow<?>) treeView.lookup(".virtual-flow");
        IndexedCell<?> firstVisibleCell = virtualFlow.getFirstVisibleCell();
        IndexedCell<?> lastVisibleCell = virtualFlow.getLastVisibleCell();
        return new int[]{firstVisibleCell.getIndex(), lastVisibleCell.getIndex()};
    }

    private static <T> void toMap(TreeItem<T> rootItem, BDTreeCellInitFactory<T> cellInitFactory, Map<T, String> map) {
        map.put(rootItem.getValue(), cellInitFactory.getInfo(rootItem.getValue()));
        rootItem.getChildren().forEach(child -> toMap(child, cellInitFactory, map));
    }


    static int getDeep(TreeItem<?> parent, TreeItem<?> child) {
        if (child == null || parent == null) return -1;
        Stack<TreeItem<?>> stack = new Stack<>();
        AtomicInteger deep = new AtomicInteger(0);
        stack.push(child);
        while (!stack.isEmpty()) {
            TreeItem<?> pop = stack.pop();
            if (Objects.equals(parent, pop)) return deep.get();
            if (pop.getParent() != null)
                stack.push(pop.getParent());
            deep.getAndIncrement();
        }
        return -1;
    }

    static void openAt(TreeItem<?> item) {
        Stack<TreeItem<?>> stack = new Stack<>();
        stack.push(item);
        while (!stack.isEmpty()) {
            TreeItem<?> pop = stack.pop();
            if (pop.getParent() != null)
                stack.push(pop);
            pop.setExpanded(true);
        }
    }

    static void openAll(TreeItem<?> item) {
        Stack<TreeItem<?>> stack = new Stack<>();
        stack.push(item);
        while (!stack.empty()) {
            TreeItem<?> pop = stack.pop();
            pop.setExpanded(true);
            pop.getChildren().forEach(stack::push);
        }
    }

    static void closeAll(TreeItem<?> item) {
        Stack<TreeItem<?>> stack = new Stack<>();
        stack.push(item);
        while (!stack.empty()) {
            TreeItem<?> pop = stack.pop();
            pop.setExpanded(false);
            pop.getChildren().forEach(stack::push);
        }
    }

    //      初始化图标。
    Node initGraphic(T t);

    default Image getDisclosureNodeImage(boolean isExpanded) {
        return Util.getImage(isExpanded ? BDIcon.CHEVRON_DOWN : BDIcon.CHEVRON_RIGHT);
    }

    //      初始化text信息。
    default String getInfo(T t) {
        return t.toString();
    }

    //    比较（同一目录下的参数对比）
    int compare(T o1, T o2);

    //    渲染
    void rendering(T t, Node graphic, Text text, Region pane);
}
