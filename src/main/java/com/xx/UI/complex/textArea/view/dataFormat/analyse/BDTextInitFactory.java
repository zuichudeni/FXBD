package com.xx.UI.complex.textArea.view.dataFormat.analyse;

import com.xx.UI.complex.textArea.content.listener.ContentChangeEvent;
import com.xx.UI.complex.textArea.content.listener.ContentChangeListener;
import com.xx.UI.complex.textArea.content.segment.Paragraph;
import com.xx.UI.complex.textArea.content.segment.Segment;
import com.xx.UI.complex.textArea.view.BDTextArea;
import com.xx.UI.util.BDScheduler;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class BDTextInitFactory<T extends Enum<?> & Analyse.BDTextEnum<T>> {
    private static final PseudoClass DATABLOCK_ENTRY_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("caret");
    // 默认工厂使用泛型适配器
    protected final BDAnalyse<T> analyse;
    private final BDTextArea textArea;
    private ContentChangeListener changeListener;
    private Runnable disableRunnable;
    private BDScheduler scheduler;

    public BDTextInitFactory(BDTextArea textArea, BDAnalyse<T> analyse) {
        this.textArea = textArea;
        this.analyse = analyse;
        if (textArea != null) {
            textArea
                    .getMapping()
                    .addBDScheduler(scheduler = new BDScheduler(() ->
                            analyse.setTextAsync(textArea.toString(), textArea::refresh), 100));
            changeListener = e -> {
                analyse.setProcessing(true);
                if (e.getChangeType().equals(ContentChangeEvent.ChangeType.INSERT))
                    analyse.append(e.getStartParaIndex(), e.getStartOffset(), e.getChangedParagraphs());
                else if (e.getChangeType().equals(ContentChangeEvent.ChangeType.DELETE))
                    analyse.delete(e.getStartParaIndex(), e.getStartOffset(), e.getEndParaIndex(), e.getEndOffset(), e.getChangedParagraphs());
                else {
                    analyse.delete(e.getStartParaIndex(), e.getStartOffset(), e.getEndParaIndex(), e.getEndOffset(), e.getChangedParagraphs());
                    Paragraph paragraph = new Paragraph();
                    paragraph.appendString(e.getChangedSegment().getInfo());
                    analyse.append(e.getStartParaIndex(), e.getStartOffset(), List.of(paragraph));
                }
                textArea.refresh();
                scheduler.run();
            };
            textArea.addContentChangeListener(changeListener);
            disableRunnable = analyse::shutdown;
            textArea.getMapping().addDisposeEvent(disableRunnable);
        }
    }

    /**
     * 根据传入的段落索引和段落内容，返回该段落的返回该段落的block列表。
     */
    public final List<DataBlock<T, ?>> splitDataBlocks(int paragraphIndex, Paragraph paragraph) {
        if ((analyse.isProcessing() || analyse.getTokenEntryCacheMap() == null) && (analyse.tokenEntryCacheMap == null ||analyse.tokenEntryCacheMap.isEmpty()))
            // 使用类型安全的默认实现
            return getDefaultDataBlocks(paragraph);
        else return analyse.getDataBlock(paragraphIndex, paragraph);

    }

    private List<DataBlock<T, ?>> getDefaultDataBlocks(Paragraph paragraph) {
        // 创建类型安全的副本
        List<DataBlock<T, ?>> result = new ArrayList<>();
        T undefinedType = analyse.getUndefinedType();
        for (Segment<?> segment : paragraph.getSegments()) {
            DataBlock<T, ?> dataBlock = new DataBlock<>(undefinedType, segment, null);
            result.add(dataBlock);
        }
        return result;
    }

    /**
     * 务必不要修改Text的内容，否则会导致显示异常。
     * 原因：会进行内容一致性检测以保证文本内容与显示一致。
     */
    public abstract void renderingText(Text text, Region textPane, DataBlock<T, ?> dataBlock);

    public abstract void renderingNode(Node node, Region nodePane, DataBlock<T, ?> dataBlock);

    /**
     * 用以渲染DataBlockEntry 的文本部分，同样务必不要修改Text的内容，否则会导致显示异常。
     */
    public void renderingDataBlockEntryText(BDAnalyse.DataBlockEntry<T> entry, Text left, Region leftPane, Text right, Region rightPane) {
        if (!Objects.equals(left, right)) caretDataBlockEntryText(left, leftPane);
        caretDataBlockEntryText(right, rightPane);
    }

    public void caretDataBlockEntryText(Text text, Region pane) {
        if (text != null)
            text.pseudoClassStateChanged(DATABLOCK_ENTRY_PSEUDO_CLASS, true);
        if (pane != null)
            pane.pseudoClassStateChanged(DATABLOCK_ENTRY_PSEUDO_CLASS, true);
    }

    public void removeCaretDataBlockEntryText(Text text, Region pane) {
        if (text != null)
            text.pseudoClassStateChanged(DATABLOCK_ENTRY_PSEUDO_CLASS, false);

        if (pane != null)
            pane.pseudoClassStateChanged(DATABLOCK_ENTRY_PSEUDO_CLASS, false);

    }

    public void renderingDataBlockEntryNode(BDAnalyse.DataBlockEntry<T> entry, Node left, Node right) {

    }

    public void dispose() {
        if (textArea != null) {
            textArea.removeContentChangeListener(changeListener);
            textArea.getMapping().removeBDScheduler(scheduler);
            textArea.getMapping().removeDisposeEvent(disableRunnable);
        }
    }

    public BDAnalyse<T> getAnalyse() {
        return analyse;
    }
}