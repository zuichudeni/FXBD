package com.xx.UI.complex.stage;

import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;

public class BDContentBuilder {
    private final BDContent bdContent = new BDContent();
    public BDContentBuilder addSideNode(BDDirection directory, BDInSequence inSequence, Node... nodes){
        switch (directory) {
            case TOP:
                for (Node node : nodes){
                    if (inSequence.equals(BDInSequence.FRONT))
                        bdContent.topSideBar.get().addFrontSideNode(node,inSequence);
                    else bdContent.topSideBar.get().addAfterSideNode(node);
                }
                break;
                case RIGHT:
                for (Node node : nodes)
                    bdContent.rightSideBar.get().addFrontSideNode(node,inSequence);
                break;
                case BOTTOM:
                for (Node node : nodes){
                    if (inSequence.equals(BDInSequence.FRONT))
                        bdContent.bottomSideBar.get().addFrontSideNode(node,inSequence);
                    else bdContent.bottomSideBar.get().addAfterSideNode(node);
                }
                break;
                case LEFT:
                for (Node node : nodes)
                    bdContent.leftSideBar.get().addFrontSideNode(node,inSequence);
                break;
                case null, default:
                break;
        }
        return this;
    }
    public BDContentBuilder addCenterNode(Node node){
        bdContent.setContent(node);
        return this;
    }
    public BDContent build() {
        return bdContent;
    }
}
