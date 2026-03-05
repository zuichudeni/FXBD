package com.xx.UI.complex.tree;

import com.xx.UI.ui.BDIcon;
import com.xx.UI.util.Util;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

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

    static int getDeep(TreeItem<?> parent,TreeItem<?> child){
        if (child == null || parent == null) return -1;
        Stack<TreeItem<?>> stack = new Stack<>();
        AtomicInteger deep = new AtomicInteger(0);
        stack.push(child);
        while (!stack.isEmpty()) {
            TreeItem<?> pop = stack.pop();
            if (Objects.equals(parent,pop)) return deep.get();
            if (pop.getParent() != null)
                stack.push(pop.getParent());
            deep.getAndIncrement();
        }
        return -1;
    }
    static void openAt(TreeItem<?> item){
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
