package com.xx.UI.complex.stage;

import javafx.scene.Node;

import java.util.Arrays;

public class BDContentBuilder {
    private final BDContent bdContent = new BDContent();

    void addNode(BDDirection directory, BDInSequence inSequence, Node... nodes) {
        switch (directory) {
            case TOP:
                for (Node node : nodes) {
                    if (inSequence.equals(BDInSequence.FRONT))
                        bdContent.topSideBar.get().addFrontSideNode(node, inSequence);
                    else bdContent.topSideBar.get().addAfterSideNode(node);
                }
                break;
            case RIGHT:
                for (Node node : nodes)
                    bdContent.rightSideBar.get().addFrontSideNode(node, inSequence);
                break;
            case BOTTOM:
                for (Node node : nodes) {
                    if (inSequence.equals(BDInSequence.FRONT))
                        bdContent.bottomSideBar.get().addFrontSideNode(node, inSequence);
                    else bdContent.bottomSideBar.get().addAfterSideNode(node);
                }
                break;
            case LEFT:
                for (Node node : nodes)
                    bdContent.leftSideBar.get().addFrontSideNode(node, inSequence);
                break;
            case null, default:
                break;
        }
    }

    public BDContentBuilder addSideNode(BDDirection directory, BDInSequence inSequence, Node... node) {
        if (node.length != 0) {
            BDSideBarItem[] items = Arrays.stream(node).filter(n -> n instanceof BDSideBarItem).map(n -> (BDSideBarItem) n).toArray(BDSideBarItem[]::new);
            Node[] nodes = Arrays.stream(node).filter(n -> !(n instanceof BDSideBarItem)).toArray(Node[]::new);
            addSideNode(items);
            addNode(directory, inSequence, nodes);
        }
        return this;
    }

    public BDContentBuilder addSideNode(BDSideBarItem... items) {
        for (BDSideBarItem item : items) {
            if (item.getDirection().equals(BDDirection.LEFT) || item.getDirection().equals(BDDirection.RIGHT))
                addNode(item.getDirection(), item.getInSequence(), item);
            else if (item.getInSequence().equals(BDInSequence.FRONT))
                bdContent.leftSideBar.get().addAfterSideNode(item);
            else bdContent.rightSideBar.get().addAfterSideNode(item);
        }
        return this;
    }

    public BDContentBuilder addCenterNode(Node node) {
        bdContent.setContent(node);
        return this;
    }

    public BDContentBuilder setStyleClass(String styleClass) {
        this.bdContent.setStyleClass(styleClass);
        return this;
    }

    public BDContent build() {
        return bdContent;
    }
}
