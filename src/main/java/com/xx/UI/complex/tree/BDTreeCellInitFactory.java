package com.xx.UI.complex.tree;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.util.Stack;

public interface BDTreeCellInitFactory<T> {
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

    //      初始化text信息。
    default String getInfo(T t) {
        return t.toString();
    }


    //    比较（同一目录下的参数对比）
    int compare(T o1, T o2);
    //    渲染
    void rendering(T t,Node graphic, Text text, Region pane);
}
