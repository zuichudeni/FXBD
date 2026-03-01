package com.xx.UI.complex.textArea.view.dataFormat.analyse;

import com.xx.UI.complex.textArea.content.listener.ContentChangeEvent;
import com.xx.UI.complex.textArea.content.segment.NodeSegment;
import com.xx.UI.complex.textArea.content.segment.Paragraph;
import com.xx.UI.complex.textArea.view.BDTextArea;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
// 指定渲染，而不是通过匹配的方式。
public class BDSimpleTextInitFactory<T extends Enum<?> & Analyse.BDTextEnum<T>> extends BDTextInitFactory<T> {
    private final Map<T, BDSimpleTextRendering<T>> textRenderingMap = new HashMap<>();
    private final Map<T, BDSimpleNodeRendering<T>> nodeRenderingMap = new HashMap<>();
    private T tempType;

    public BDSimpleTextInitFactory(BDTextArea textArea, T undefined) {
        super(textArea, new BDAnalyse<T>() {
            {
                rendering = false;
            }
            @Override
            public List<BDToken<T>> getBDToken(String text) {
                return List.of();
            }

            @Override
            public T getUndefinedType() {
                return undefined;
            }
        });
        textArea.removeContentChangeListener(changeListener);
        textArea.addContentChangeListener(changeListener = e -> {
            analyse.setProcessing(true);
            if (e.getChangeType().equals(ContentChangeEvent.ChangeType.INSERT)) {

                if (tempType == null)
                    analyse.append(e.getStartParaIndex(), e.getStartOffset(), e.getChangedParagraphs());
                else {
                    analyse.append(e.getStartParaIndex(), e.getStartOffset(), e.getChangedParagraphs(), tempType);
                    tempType = null;
                }
            } else if (e.getChangeType().equals(ContentChangeEvent.ChangeType.DELETE))
                analyse.delete(e.getStartParaIndex(), e.getStartOffset(), e.getEndParaIndex(), e.getEndOffset(), e.getChangedParagraphs());
            else {
                analyse.delete(e.getStartParaIndex(), e.getStartOffset(), e.getEndParaIndex(), e.getEndOffset(), e.getChangedParagraphs());
                Paragraph paragraph = new Paragraph();
                paragraph.appendString(e.getChangedSegment().getInfo());
                if (tempType == null)
                    analyse.append(e.getStartParaIndex(), e.getStartOffset(), List.of(paragraph));
                else {
                    analyse.append(e.getStartParaIndex(), e.getStartOffset(), List.of(paragraph), tempType);
                    tempType = null;
                }
            }
            textArea.refresh();
        });
    }

    public void append(String text, T type) {
        tempType = type;
        textArea.appendText(text);
    }

    public void insert(int index, String text, T type) {
        tempType = type;
        textArea.insertText(index, text);
    }

    public void append(NodeSegment<?> nodeSegment, T type) {
        tempType = type;
        textArea.appendNode(nodeSegment);
    }

    public void insert(int index, NodeSegment<?> nodeSegment, T type) {
        tempType = type;
        textArea.insertNode(index, nodeSegment);
    }

    @Override
    public void renderingText(Text text, Region textPane, DataBlock<T, ?> dataBlock) {
        if (textRenderingMap.containsKey(dataBlock.getType()))
            textRenderingMap.get(dataBlock.getType()).rendering(text, textPane, dataBlock);
    }

    @Override
    public void renderingNode(Node node, Region nodePane, DataBlock<T, ?> dataBlock) {
        if (nodeRenderingMap.containsKey(dataBlock.getType()))
            nodeRenderingMap.get(dataBlock.getType()).rendering(node, nodePane, dataBlock);
    }

    public void pushTextRenderingRuler(T type, BDSimpleTextRendering<T> rendering) {
        textRenderingMap.put(type, rendering);
    }

    public void pushNodeRenderingRuler(T type, BDSimpleNodeRendering<T> rendering) {
        nodeRenderingMap.put(type, rendering);
    }

    public interface BDSimpleTextRendering<T extends Enum<?> & Analyse.BDTextEnum<T>> {
        void rendering(Text text, Region textPane, DataBlock<T, ?> dataBlock);
    }

    public interface BDSimpleNodeRendering<T extends Enum<?> & Analyse.BDTextEnum<T>> {
        void rendering(Node node, Region nodePane, DataBlock<T, ?> dataBlock);
    }
}
