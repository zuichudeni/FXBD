package com.xx.UI.complex.textArea.view;

import com.xx.UI.complex.search.BDSearchBox;
import com.xx.UI.complex.textArea.content.BDTextAreaContent;
import com.xx.UI.complex.textArea.content.listener.ContentChangeListener;
import com.xx.UI.complex.textArea.content.listener.SelectRangeChangeListener;
import com.xx.UI.complex.textArea.content.segment.NodeSegment;
import com.xx.UI.complex.textArea.content.segment.Paragraph;
import com.xx.UI.complex.textArea.view.dataFormat.analyse.*;
import com.xx.UI.complex.textArea.view.dataFormat.mark.MARK_DIRECTION;
import com.xx.UI.complex.textArea.view.dataFormat.mark.Mark;
import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import com.xx.UI.util.Util;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.css.*;
import javafx.css.converter.InsetsConverter;
import javafx.css.converter.PaintConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


@SuppressWarnings("unchecked")
public class BDTextArea extends BDControl {
    //    临时变量：记录本组件拖拽的内容。目的：区分拖拽的内容是否为本组件拖到本组件，如果是本组件拖到本组件，则会直接插入拖拽内容。主要是为了
    static final List<Paragraph> tempDragParagraphs = new ArrayList<>(10);
    private static final String CSS_CLASS_NAME = "bd-text-area";
    //    临时变量：记录是否为本组件拖拽。目的：区分拖拽的内容是否为本组件拖到本组件，如果是本组件拖到本组件，则会删除拖拽内容。
    static BDTextArea tempDragTextArea;
    protected final BDTextAreaContent content = new BDTextAreaContent();
    protected final BDTextAreaListView listView = new BDTextAreaListView(this, content);
    //    专门用作触发cell刷新的临时工具变量。
    final SimpleBooleanProperty refreshTempValue = new SimpleBooleanProperty(false);
    final Map<Integer, List<BDSearchBox.SearchResult>> searchResultMap = new HashMap<>();
    final List<BDSearchBox.SearchBlock> searchBlocks = new ArrayList<>();
    final SimpleIntegerProperty searchBlockIndex = new SimpleIntegerProperty(-1);
    private final SimpleBooleanProperty pannable = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<BDTextInitFactory<? extends Enum<? extends Analyse.BDTextEnum<?>>>> textInitFactory =
            new SimpleObjectProperty<>(new BDTextInitFactory<>(this, new BDAnalyse<DefaultEnum>() {

                @Override
                public List<BDToken<DefaultEnum>> getBDToken(String text) {
                    return List.of();
                }

                @Override
                public DefaultEnum getUndefinedType() {
                    return DefaultEnum.UNDEFINED;
                }
            }) {
                @Override
                public void renderingText(Text text, Region textPane, DataBlock<DefaultEnum, ?> dataBlock) {
                    text.getStyleClass().setAll("BDTextarea-java-text");
                }

                @Override
                public void renderingNode(Node node, Region nodePane, DataBlock<DefaultEnum, ?> dataBlock) {

                }
            });
    private final SimpleObjectProperty<BDTextAreaContent.Point> caretPosition = new SimpleObjectProperty<>(content.getCaretPosition());
    private final SimpleObjectProperty<HandleKeyEvent> handleKeyEvent = new SimpleObjectProperty<>(new HandleKeyEvent() {
    });
    private final SimpleObjectProperty<HandleMouseEvent> handleMouseEvent = new SimpleObjectProperty<>(new HandleMouseEvent() {
    });
    private final SimpleObjectProperty<HandleDragEvent> handleDragEvent = new SimpleObjectProperty<>(new HandleDragEvent() {
    });
    private final SimpleObjectProperty<SelectRange> selectRange = new SimpleObjectProperty<>();
    private final BooleanProperty focusedWith = Util.focusWithIn(this, mapping);
    private final SimpleBooleanProperty editable = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty displayCaret = new SimpleBooleanProperty(true);
    //    用以记录上一次渲染的dataBlockEntry 部分的text，从而清除多余的渲染内容。
    private final Set<OldDataBlockEntry> oldDataBlockEntries = new HashSet<>();
    Runnable refreshScroll;
    //    触发选中内容的临时变量。
    SimpleBooleanProperty tempRefresh = new SimpleBooleanProperty(false);
    private ObjectProperty<Paint> selectedRangeFill;
    private ObjectProperty<Insets> textFlowPadding;
    private DoubleProperty noneLineHeight;

    public BDTextArea(){
        getStyleClass().setAll(CSS_CLASS_NAME);
        init();
    }

    //    根据传入的dataBlocks和point、dir，返回该point在位移后的值。
    static BDTextAreaContent.Point caretMove(List<DataBlock<?, ?>> dataBlocks, BDTextAreaContent.Point point, MARK_DIRECTION dir) {
        Objects.requireNonNull(dataBlocks, "dataBlocks cannot be null");
        Objects.requireNonNull(point, "point cannot be null");
        Objects.requireNonNull(dir, "dir cannot be null");
        if ((point.offset() == 0 && dir.equals(MARK_DIRECTION.LEFT)) || (point.offset() == dataBlocks.stream().mapToInt(block -> block.getSegment().getLength()).sum() && dir.equals(MARK_DIRECTION.RIGHT)))
            throw new IllegalArgumentException("point %s cannot be at the start or end of dataBlocks".formatted(point));
        AtomicInteger preLen = new AtomicInteger(0);
//        返回的是靠前一个datablock，即如果在两个datablock之间，则返回前一个。
        DataBlock<?, ?> dataBlock = Objects.requireNonNull(getDataBlock(dataBlocks, point, preLen), "dataBlock cannot be null");
        int i = dataBlocks.indexOf(dataBlock);
//        dataBlock内的offset
        int rLen = point.offset() - preLen.get();
//        光标向左位移
        if (dir.equals(MARK_DIRECTION.LEFT)) {
//            需要进行标记处理
            if (rLen == dataBlock.getSegment().getLength()) {
//                当前datablock的有标记节点且在右边。
                if (dataBlock.getMark() instanceof Mark mark && mark.getDirection().equals(MARK_DIRECTION.RIGHT)) {
//                    后一个datablock的有标记节点且在左边。
                    if (i < dataBlocks.size() - 1
                            && dataBlocks.get(i + 1).getMark() instanceof Mark nextMark
                            && nextMark.getDirection().equals(MARK_DIRECTION.LEFT)) {
                        if (point.virtualOffset() == 0)
                            return new BDTextAreaContent.Point(point.paragraph(), point.offset(), -1);
                        else if (point.virtualOffset() == -1)
                            return new BDTextAreaContent.Point(point.paragraph(), point.offset(), -2);
                        else if (point.virtualOffset() == -2)
                            return new BDTextAreaContent.Point(point.paragraph(), point.offset() - 1, 0);
                        else throw new RuntimeException("invalid virtualOffset " + point.virtualOffset());
                    }
//                    后一个datablock没有标记节点或者标记点不在左边。
                    else {
                        if (point.virtualOffset() == 0)
                            return new BDTextAreaContent.Point(point.paragraph(), point.offset() - 1, 0);
                        else if (point.virtualOffset() == -1)
                            return new BDTextAreaContent.Point(point.paragraph(), point.offset() - 1, 0);
                        else if (point.virtualOffset() == 1)
                            return new BDTextAreaContent.Point(point.paragraph(), point.offset(), 0);
                        else throw new RuntimeException("invalid virtualOffset " + point.virtualOffset());
                    }
                }
//                当前datablock没有标记节点或者在左边。
                else {
//                    后一个datablock的有标记节点且在右边。
                    if (i < dataBlocks.size() - 1
                            && dataBlocks.get(i + 1).getMark() instanceof Mark nextMark
                            && nextMark.getDirection().equals(MARK_DIRECTION.LEFT)) {
                        if (point.virtualOffset() == 0)
                            return new BDTextAreaContent.Point(point.paragraph(), point.offset(), -1);
                        else if (point.virtualOffset() == -1)
                            return new BDTextAreaContent.Point(point.paragraph(), point.offset() - 1, 0);
                        else throw new RuntimeException("invalid virtualOffset " + point.virtualOffset());
                    }
//                    后一个没有标记节点或者不在右边。
                    else return new BDTextAreaContent.Point(point.paragraph(), point.offset() - 1, 0);
                }
            } else return new BDTextAreaContent.Point(point.paragraph(), point.offset() - 1, 0);
        }
//        光标向右位移
        else {
//            需要进行标记处理
            if (rLen == dataBlock.getSegment().getLength() || rLen == dataBlock.getSegment().getLength() - 1) {
//                当前datablock的有标记节点且在右边。
                if (dataBlock.getMark() instanceof Mark mark
                        && mark.getDirection().equals(MARK_DIRECTION.RIGHT)) {
//                    后一个datablock的有标记节点。
                    if (i < dataBlocks.size() - 1
                            && dataBlocks.get(i + 1).getMark() instanceof Mark nextMark) {
//                        后一个datablock的标记节点在左边。
                        if (nextMark.getDirection().equals(MARK_DIRECTION.LEFT)) {
                            if (point.virtualOffset() == -2)
                                return new BDTextAreaContent.Point(point.paragraph(), point.offset(), -1);
                            else if (point.virtualOffset() == -1)
                                return new BDTextAreaContent.Point(point.paragraph(), point.offset(), 0);
//                        即将跳到下一个datablock,且下一个datablock的长度为1，并且下下个datablock有左的标记节点。
                            else if (point.virtualOffset() == 0) {
                                if (rLen == dataBlock.getSegment().getLength()) {
                                    if (dataBlocks.get(i + 1).getSegment().getLength() == 1
                                            && i < dataBlocks.size() - 2
                                            && dataBlocks.get(i + 2).getMark() instanceof Mark nextNextMark
                                            && nextNextMark.getDirection().equals(MARK_DIRECTION.LEFT)) {
                                        return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, -1);
                                    } else return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, 0);
                                }
                                return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, -2);
                            } else throw new RuntimeException("invalid virtualOffset " + point.virtualOffset());
                        }
//                    后一个datablock的标记节点在右边。
                        else {
                            if (point.virtualOffset() == -1)
                                return new BDTextAreaContent.Point(point.paragraph(), point.offset(), 0);
                            else if (point.virtualOffset() == 0) {
                                if (dataBlocks.get(i + 1).getSegment().getLength() == 1) {
                                    if (i < dataBlocks.size() - 2
                                            && dataBlocks.get(i + 1).getSegment().getLength() == 1
                                            && dataBlocks.get(i + 2).getMark() instanceof Mark nextNextMark
                                            && nextNextMark.getDirection().equals(MARK_DIRECTION.LEFT))
                                        return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, -2);
                                    else return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, -1);
                                } else return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, 0);
                            } else throw new RuntimeException("invalid virtualOffset " + point.virtualOffset());
                        }
                    }
//                    后一个datablock没有标记节点。
                    else {
                        if (point.virtualOffset() == -1)
                            return new BDTextAreaContent.Point(point.paragraph(), point.offset(), 0);
                        else if (point.virtualOffset() == 0) {
                            if (i < dataBlocks.size() - 2 &&
                                    dataBlocks.get(i + 1).getSegment().getLength() == 1 &&
                                    dataBlocks.get(i + 2).getMark() instanceof Mark nextNextMark
                                    && nextNextMark.getDirection().equals(MARK_DIRECTION.LEFT))
                                return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, -1);
                            else return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, 0);
                        } else throw new RuntimeException("invalid virtualOffset " + point.virtualOffset());
                    }
                }
//                当前datablock没有标记节点或者在左边。
                else {
                    //                    后一个datablock的有标记节点。
                    if (i < dataBlocks.size() - 1
                            && dataBlocks.get(i + 1).getMark() instanceof Mark nextMark) {
//                        后一个datablock的标记节点在左边。
                        if (nextMark.getDirection().equals(MARK_DIRECTION.LEFT)) {
                            if (point.virtualOffset() == -1)
                                return new BDTextAreaContent.Point(point.paragraph(), point.offset(), 0);
//                        即将跳到下一个datablock,且下一个datablock的长度为1，并且下下个datablock有左的标记节点。
                            else if (point.virtualOffset() == 0) {
                                if (i < dataBlocks.size() - 2
                                        && dataBlocks.get(i + 1).getSegment().getLength() == 1
                                        && dataBlocks.get(i + 2).getMark() instanceof Mark nextNextMark
                                        && nextNextMark.getDirection().equals(MARK_DIRECTION.LEFT)) {
                                    return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, -1);
                                } else return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, 0);
                            } else throw new RuntimeException("invalid virtualOffset " + point.virtualOffset());
                        }
//                    后一个datablock的标记节点在右边。
                        else {
                            if (rLen == dataBlock.getSegment().getLength()) {
                                if (point.virtualOffset() == 0) {
                                    if (dataBlocks.get(i + 1).getSegment().getLength() == 1) {
                                        if (i < dataBlocks.size() - 2
                                                && dataBlocks.get(i + 1).getSegment().getLength() == 1
                                                && dataBlocks.get(i + 2).getMark() instanceof Mark nextNextMark
                                                && nextNextMark.getDirection().equals(MARK_DIRECTION.LEFT))
                                            return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, -2);
                                        else
                                            return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, 0);
                                    } else return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, 0);
                                } else if (point.virtualOffset() == -1) {
                                    return new BDTextAreaContent.Point(point.paragraph(), point.offset(), 0);
                                } else throw new RuntimeException("invalid virtualOffset " + point.virtualOffset());
                            } else {
                                if (dataBlock.getMark() instanceof Mark mark && mark.getDirection().equals(MARK_DIRECTION.LEFT)) {
                                    if (point.virtualOffset() == -1)
                                        return new BDTextAreaContent.Point(point.paragraph(), point.offset(), 0);
                                    else if (point.virtualOffset() == 0)
                                        return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, 0);
                                    else throw new RuntimeException("invalid virtualOffset " + point.virtualOffset());
                                }
                                return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, 0);
                            }
                        }
                    }
//                    后一个datablock没有标记节点。
                    else {
                        if (rLen == dataBlock.getSegment().getLength()) {
                            if (point.virtualOffset() == 0) {
                                if (i < dataBlocks.size() - 2
                                        && dataBlocks.get(i + 1).getSegment().getLength() == 1
                                        && dataBlocks.get(i + 2).getMark() instanceof Mark nextNextMark
                                        && nextNextMark.getDirection().equals(MARK_DIRECTION.LEFT))
                                    return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, -1);
                                else return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, 0);
                            } else if (point.virtualOffset() == -1) {
                                return new BDTextAreaContent.Point(point.paragraph(), point.offset(), 0);
                            } else throw new RuntimeException("invalid virtualOffset " + point.virtualOffset());
                        } else {
                            if (point.virtualOffset() == -1)
                                return new BDTextAreaContent.Point(point.paragraph(), point.offset(), 0);
                            else if (point.virtualOffset() == 0)
                                return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, 0);
                            else throw new RuntimeException("invalid virtualOffset " + point.virtualOffset());
                        }
                    }
                }
            } else return new BDTextAreaContent.Point(point.paragraph(), point.offset() + 1, 0);
        }
    }

    private static DataBlock<?, ?> getDataBlock(List<DataBlock<?, ?>> dataBlocks, BDTextAreaContent.Point point, AtomicInteger preLen) {
        int tempLen = 0;
        for (DataBlock<?, ?> dataBlock : dataBlocks) {
            if (tempLen + dataBlock.getSegment().getLength() >= point.offset()) return dataBlock;
            int len = dataBlock.getSegment().getLength();
            tempLen += len;
            preLen.addAndGet(len);
        }
        throw new RuntimeException("not find dataBlock in this dataBlocks at point " + point.toString());
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    public Range getShowRange() {
        IndexedCell<?> firstVisibleCell = listView.getFirstVisibleCell();
        IndexedCell<?> lastVisibleCell = listView.getLastVisibleCell();
        if (firstVisibleCell == null || lastVisibleCell == null) return new Range(0, 0);
        return new Range(firstVisibleCell.getIndex(), lastVisibleCell.getIndex());
    }
    public void scrollTo(int index){
        listView.scrollTo(index);
    }
    private void init() {
        content.addCaretChangeListener(event -> caretPosition.set(event.getNewPoint()));
        content.addSelectRangeChangeListener(event -> selectRange.set(new SelectRange(event.getNewStartPoint(), event.getNewEndPoint())));
        setCaretPosition(new BDTextAreaContent.Point(0, 0));
        mapping.addChildren(listView.mapping);
    }

    public void caretUp() {
        content.caretUp(1);
    }

    public void caretDown() {
        content.caretDown(1);
    }

    public void caretRight() {
        //        位于段落结尾时，光标右移到下一段且下一段开头为标记节点。
        if (getCaretPosition().offset() == content.getParagraph(getCaretPosition().paragraph()).getLength()) {
            if (getCaretPosition().paragraph() < content.linesNum() - 1) {
                List<?> dataBlocks = getTextInitFactory().splitDataBlocks(getCaretPosition().paragraph(), content.getParagraph(getCaretPosition().paragraph()));
                if (!dataBlocks.isEmpty()
                        && dataBlocks.getLast() instanceof DataBlock<?, ?> dataBlock
                        && dataBlock.getMark() instanceof Mark mark
                        && mark.getDirection().equals(MARK_DIRECTION.RIGHT)) {
                    if (getCaretPosition().virtualOffset() == 0) {
                        setCaretPosition(new BDTextAreaContent.Point(getCaretPosition().paragraph(), getCaretPosition().offset(), 1));
                        return;
                    }
                }
                dataBlocks = getTextInitFactory().splitDataBlocks(getCaretPosition().paragraph() + 1, content.getParagraph(getCaretPosition().paragraph() + 1));
                if (!dataBlocks.isEmpty() &&
                        dataBlocks.getFirst() instanceof DataBlock<?, ?> dataBlock &&
                        dataBlock.getMark() instanceof Mark mark &&
                        mark.getDirection().equals(MARK_DIRECTION.LEFT))
                    setCaretPosition(new BDTextAreaContent.Point(getCaretPosition().paragraph() + 1, 0, -1));
                else setCaretPosition(new BDTextAreaContent.Point(getCaretPosition().paragraph() + 1, 0, 0));
            } else
                setCaretPosition(new BDTextAreaContent.Point(content.linesNum() - 1, content.getParagraph(content.linesNum() - 1).getLength(), 0));
            return;
        }
        setCaretPosition(caretMove(getTextInitFactory().splitDataBlocks(getCaretPosition().paragraph(), content.getParagraph(getCaretPosition().paragraph())), getCaretPosition(), MARK_DIRECTION.RIGHT));
    }

    //    注意光标的offset为0时，还要考虑是否有标记节点。
    public void caretLeft() {
//        位于段落开头时，光标左移到上一段且上一段末尾为标记节点。
        if (getCaretPosition().offset() == 0) {
            List<?> dataBlocks = getTextInitFactory().splitDataBlocks(getCaretPosition().paragraph(), content.getParagraph(getCaretPosition().paragraph()));
            if (getCaretPosition().virtualOffset() == 0
                    && !dataBlocks.isEmpty()
                    && dataBlocks.getFirst() instanceof DataBlock<?, ?> dataBlock
                    && dataBlock.getMark() instanceof Mark mark
                    && mark.getDirection().equals(MARK_DIRECTION.LEFT))
                setCaretPosition(new BDTextAreaContent.Point(getCaretPosition().paragraph(), getCaretPosition().offset(), -1));
            else if (getCaretPosition().paragraph() > 0) {
                dataBlocks = getTextInitFactory().splitDataBlocks(getCaretPosition().paragraph() - 1, content.getParagraph(getCaretPosition().paragraph() - 1));
                if (!dataBlocks.isEmpty() &&
                        dataBlocks.getLast() instanceof DataBlock<?, ?> dataBlock &&
                        dataBlock.getMark() instanceof Mark mark &&
                        mark.getDirection().equals(MARK_DIRECTION.RIGHT)) {
                    setCaretPosition(new BDTextAreaContent.Point(getCaretPosition().paragraph() - 1,
                            content.getParagraph(getCaretPosition().paragraph() - 1).getLength(),
                            1));
                } else setCaretPosition(new BDTextAreaContent.Point(getCaretPosition().paragraph() - 1,
                        content.getParagraph(getCaretPosition().paragraph() - 1).getLength(),
                        0));
            } else {
                if (!dataBlocks.isEmpty()
                        && dataBlocks.getFirst() instanceof DataBlock<?, ?> dataBlock
                        && dataBlock.getMark() instanceof Mark mark
                        && mark.getDirection().equals(MARK_DIRECTION.LEFT))
                    setCaretPosition(new BDTextAreaContent.Point(0, 0, -1));
                else setCaretPosition(new BDTextAreaContent.Point(0, 0, 0));
            }
            return;
        }
        setCaretPosition(caretMove(
                getTextInitFactory().splitDataBlocks(getCaretPosition().paragraph(), content.getParagraph(getCaretPosition().paragraph())),
                getCaretPosition(),
                MARK_DIRECTION.LEFT));
    }

    public void caretEnd() {
        content.caretEnd();
    }

    public void caretStart() {
        content.caretStart();
    }

    public void undo() {
        content.undo();
        if (refreshScroll != null) {
            refreshScroll.run();
        }
    }

    public void redo() {
        content.redo();
        if (refreshScroll != null) {
            refreshScroll.run();
        }
    }

    public void refresh() {
        refreshTempValue.set(!refreshTempValue.get());
    }

    public void insertText(BDTextAreaContent.Point point, String text) {
        content.insert(point, text, true);
    }

    public void insertText(int index, String text) {
        content.insert(index, text, true);
    }
    public void appendText(String text) {
        insertText(getLength(), text);
    }

    public void insertNode(BDTextAreaContent.Point point, NodeSegment<?> nodeSegment) {
        content.insert(point, nodeSegment, true);
    }

    public void insertNode(int index, NodeSegment<?> nodeSegment) {
        content.insert(index, nodeSegment, true);
    }
    public void appendNode(NodeSegment<?> nodeSegment) {
        insertNode(getLength(), nodeSegment);
    }

    public void deleteSelect() {
        content.deleteSelect(true);
    }

    public void delete(BDTextAreaContent.Point start, BDTextAreaContent.Point end) {
        content.delete(start, end, true);
    }

    public void delete(int start, int end) {
        content.delete(start, end, true);
    }
    public void replace(BDTextAreaContent.Point start, BDTextAreaContent.Point end, String text){
        content.delete(start, end, true);
        content.insert(start, text, true);
    }

    public BDTextInitFactory getTextInitFactory() {
        return textInitFactory.get();
    }

    public <T extends Enum<?> & Analyse.BDTextEnum<T>> void setTextInitFactory(BDTextInitFactory<T> textInitFactory) {
        Objects.requireNonNull(textInitFactory, "TextInitFactory cannot be null");
        this.textInitFactory.get().dispose();
        this.textInitFactory.set((BDTextInitFactory<? extends Enum<? extends Analyse.BDTextEnum<?>>>) textInitFactory);
        Platform.runLater(() -> textInitFactory.getAnalyse().setTextAsync(toString(), this::refresh));
    }

    public ReadOnlyObjectProperty<BDTextInitFactory<? extends Enum<? extends Analyse.BDTextEnum<?>>>> textInitFactoryProperty() {
        return textInitFactory;
    }

    public SelectRange getSelectRange() {
        return selectRange.get();
    }

    public void setSelectRange(SelectRange range) {
        if (range == null || range.start == null || range.end == null) return;
        BDTextAreaContent.Point start = range.start;
        BDTextAreaContent.Point end = range.end;
        if (start.paragraph() == end.paragraph() && start.offset() == end.offset()) {
            content.clearSelectRange();
            return;
        }
        if (start.paragraph() > end.paragraph()) {
            BDTextAreaContent.Point temp = start;
            start = end;
            end = temp;
        } else if (start.paragraph() == end.paragraph() && start.offset() > end.offset()) {
            BDTextAreaContent.Point temp = start;
            start = end;
            end = temp;
        }
        content.setSelectRange(start, end);
    }

    public ReadOnlyObjectProperty<SelectRange> selectRangeProperty() {
        return selectRange;
    }

    public BDTextAreaContent.Point getCaretPosition() {
        return caretPosition.get();
    }

    public void setCaretPosition(BDTextAreaContent.Point caretPosition) {
        content.setCaretPosition(caretPosition);
    }

    public void addSelectRangeChangeListener(SelectRangeChangeListener listener) {
        content.addSelectRangeChangeListener(listener);
    }

    public void removeSelectRangeChangeListener(SelectRangeChangeListener listener) {
        content.removeSelectRangeChangeListener(listener);
    }

    public void addContentChangeListener(ContentChangeListener listener) {
        content.addContentChangeListener(listener);
    }

    public void removeContentChangeListener(ContentChangeListener listener) {
        content.removeContentChangeListener(listener);
    }

    public void addChangeRunnable(Runnable runnable) {
        content.addChangeRunnable(runnable);
    }

    public void removeChangeRunnable(Runnable runnable) {
        content.removeChangeRunnable(runnable);
    }

    public ReadOnlyObjectProperty<BDTextAreaContent.Point> caretPositionProperty() {
        return caretPosition;
    }

    public boolean isFocusedWith() {
        return focusedWith.get();
    }

    public ReadOnlyBooleanProperty focusedWithProperty() {
        return focusedWith;
    }

    public boolean isPannable() {
        return pannable.get();
    }

    public void setPannable(boolean pannable) {
        this.pannable.set(pannable);
    }

    public SimpleBooleanProperty pannableProperty() {
        return pannable;
    }

    public boolean isDisplayCaret() {
        return displayCaret.get();
    }

    public SimpleBooleanProperty displayCaretProperty() {
        return displayCaret;
    }

    public boolean isEditable() {
        return editable.get();
    }

    public SimpleBooleanProperty editableProperty() {
        return editable;
    }

    public HandleKeyEvent getHandleKeyEvent() {
        return handleKeyEvent.get();
    }

    public void setHandleKeyEvent(HandleKeyEvent handleKeyEvent) {
        this.handleKeyEvent.set(handleKeyEvent);
    }

    public SimpleObjectProperty<HandleKeyEvent> handleKeyEventProperty() {
        return handleKeyEvent;
    }

    public HandleMouseEvent getHandleMouseEvent() {
        return handleMouseEvent.get();
    }

    public void setHandleMouseEvent(HandleMouseEvent handleMouseEvent) {
        this.handleMouseEvent.set(handleMouseEvent);
    }

    public SimpleObjectProperty<HandleMouseEvent> handleMouseEventProperty() {
        return handleMouseEvent;
    }

    public HandleDragEvent getHandleDragEvent() {
        return handleDragEvent.get();
    }

    public void setHandleDragEvent(HandleDragEvent handleDragEvent) {
        this.handleDragEvent.set(handleDragEvent);
    }

    public SimpleObjectProperty<HandleDragEvent> handleDragEventProperty() {
        return handleDragEvent;
    }

    public final Paint getSelectedRangeFill() {
        return this.selectedRangeFill == null ? Color.web("#A6D2FF") : this.selectedRangeFill.get();
    }

    public final void setSelectedRangeFill(Paint var1) {
        this.selectedRangeFillProperty().set(var1);
    }

    public final ObjectProperty<Paint> selectedRangeFillProperty() {
        if (this.selectedRangeFill == null) {
            this.selectedRangeFill = new StyleableObjectProperty<>(Color.web("#A6D2FF")) {
                public CssMetaData<BDTextArea, Paint> getCssMetaData() {
                    return StyleableProperties.SELECTED_RANGE_FILL;
                }

                public Object getBean() {
                    return BDTextArea.this;
                }

                public String getName() {
                    return "selectedRangeFill";
                }
            };
        }

        return this.selectedRangeFill;
    }

    public final DoubleProperty noneLineHeightProperty() {
        if (this.noneLineHeight == null) {
            this.noneLineHeight = new StyleableDoubleProperty(35.0F) {
                public CssMetaData<BDTextArea, Number> getCssMetaData() {
                    return StyleableProperties.NONE_LINE_HEIGHT;
                }

                public Object getBean() {
                    return BDTextArea.this;
                }

                public String getName() {
                    return "noneLineHeight";
                }
            };
        }

        return this.noneLineHeight;
    }

    public final double getNoneLineHeight() {
        return this.noneLineHeight == null ? (double) 35.0F : this.noneLineHeight.getValue();
    }

    public final void setNoneLineHeight(double var1) {
        this.noneLineHeightProperty().setValue(var1);
    }

    public final ObjectProperty<Insets> textFlowPaddingProperty() {
        if (this.textFlowPadding == null) {
            this.textFlowPadding = new StyleableObjectProperty<>(new Insets(5, 0, 5, 0)) {
                public CssMetaData<BDTextArea, Insets> getCssMetaData() {
                    return StyleableProperties.TEXT_FLOW_PADDING;
                }

                public Object getBean() {
                    return BDTextArea.this;
                }

                public String getName() {
                    return "textFlowPadding";
                }
            };
        }

        return this.textFlowPadding;
    }

    public final Insets getTextFlowPadding() {
        return this.textFlowPadding == null ? new Insets(5, 0, 5, 0) : this.textFlowPadding.get();
    }

    public final void setTextFlowPadding(Insets var1) {
        this.textFlowPaddingProperty().set(var1);
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDTextAreaSkin(this);
    }

    @Override
    public String toString() {
        return content.toString();
    }

    public String getText(int start, int end) {
        return content.get(start, end);
    }

    public String getSelectedText() {
        return content.getSelectedText();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public boolean isPointInSelectRange(BDTextAreaContent.Point caretPosition) {
        Objects.requireNonNull(caretPosition, "caretPosition cannot be null");
        if (getSelectRange() == null || getSelectRange().start == null || getSelectRange().end == null) return false;
        if (getSelectRange().start.paragraph() == caretPosition.paragraph() && getSelectRange().end.paragraph() == caretPosition.paragraph())
            return getSelectRange().start.offset() <= caretPosition.offset() && getSelectRange().end.offset() >= caretPosition.offset();
        else if (getSelectRange().start.paragraph() == caretPosition.paragraph())
            return getSelectRange().start.offset() <= caretPosition.offset();
        else if (getSelectRange().end.paragraph() == caretPosition.paragraph())
            return getSelectRange().end.offset() >= caretPosition.offset();
        else
            return getSelectRange().start.paragraph() < caretPosition.paragraph() && getSelectRange().end.paragraph() > caretPosition.paragraph();
    }

    public int getLength() {
        return content.length();
    }

    public int getLineNum() {
        return content.linesNum();
    }

    void addDataBlockEntryText(Text leftText, Region leftRegion, Text rightText, Region rightRegion) {
        if (Objects.equals(leftText, rightText))
            leftText = null;

        // 准备要保留的条目
        Set<OldDataBlockEntry> toKeep = new HashSet<>();

        for (OldDataBlockEntry entry : oldDataBlockEntries) {
            boolean isLeft = Objects.equals(entry.text, leftText);
            boolean isRight = Objects.equals(entry.text, rightText);
            if (!isLeft && !isRight) {
                getTextInitFactory().removeCaretDataBlockEntryText(entry.text, entry.region);
            } else {
                toKeep.add(entry);
                if (isLeft) leftText = null;  // 标记已存在
                if (isRight) rightText = null; // 标记已存在
            }
        }

        // 更新集合
        oldDataBlockEntries.clear();
        oldDataBlockEntries.addAll(toKeep);

        // 添加新条目
        if (leftText != null && leftRegion != null)
            oldDataBlockEntries.add(new OldDataBlockEntry(leftText, leftRegion));
        if (rightText != null && rightRegion != null)
            oldDataBlockEntries.add(new OldDataBlockEntry(rightText, rightRegion));
    }

    public void selectAll() {
        setSelectRange(new SelectRange(new BDTextAreaContent.Point(0, 0), new BDTextAreaContent.Point(content.linesNum() - 1, content.getParagraph(content.linesNum() - 1).getLength())));
    }

    public void paste() {
        if (!isEditable()) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        SelectRange range = getSelectRange();
        if (clipboard.hasString()) {
            if (range != null && range.start != null && range.end != null) {
                delete(range.start, range.end);
            }
            insertText(getCaretPosition(), clipboard.getString());
        } else if (clipboard.hasImage()) {
            if (range != null && range.start != null && range.end != null) {
                delete(range.start, range.end);
            }
            insertNode(getCaretPosition(), new NodeSegment<>(" this is image", (NodeSegment.NodeInit<Serializable>) _ -> new ImageView(clipboard.getImage())));
        }
    }

    public void copy() {
        SelectRange range = getSelectRange();
        if (range != null && range.start != null && range.end != null) {
            HashMap<DataFormat, Object> map = new HashMap<>();
            map.put(DataFormat.PLAIN_TEXT, getSelectedText());
            Clipboard.getSystemClipboard().setContent(map);
        } else {
            setSelectRange(new SelectRange(new BDTextAreaContent.Point(getCaretPosition().paragraph(), 0), new BDTextAreaContent.Point(getCaretPosition().paragraph(), content.getParagraph(getCaretPosition().paragraph()).getLength())));
            HashMap<DataFormat, Object> map = new HashMap<>();
            map.put(DataFormat.PLAIN_TEXT, getSelectedText());
            Clipboard.getSystemClipboard().setContent(map);
        }
    }

    public interface HandleDragEvent {
        /**
         * 是否过滤MouseDragDetected事件，如果返回true，则该事件将被忽略
         */
        default boolean filterMouseDragDetected(BDTextArea textArea, MouseEvent event) {
            return false;
        }

        default boolean filterMouseDragContentDetected(BDTextArea textArea, MouseEvent event) {
            return textArea.getSelectRange() == null ||
                    textArea.getSelectRange().start == null ||
                    textArea.getSelectRange().end == null;
        }

        default boolean filterDragOver(BDTextArea textArea, DragEvent event) {
            return false;
        }

        /**
         * 是否过滤MouseDragOver事件，如果返回true，则该事件将被忽略
         */
        default boolean filterMouseDragOver(BDTextArea textArea, MouseDragEvent event) {
            return false;
        }

        /**
         * 是否过滤DragDone事件，如果返回true，则该事件将被忽略
         */
        default boolean filterDragDone(BDTextArea textArea, DragEvent event) {
            return false;
        }

        default boolean filterDragDropped(BDTextArea textArea, DragEvent event) {
            return false;
        }

        ;
    }

    public interface HandleKeyEvent {
        /**
         *
         * 处理KeyPressed事件
         */
        KeyCombination copy = KeyCombination.keyCombination("Ctrl+C");
        KeyCombination paste = KeyCombination.keyCombination("Ctrl+V");
        KeyCombination cut = KeyCombination.keyCombination("Ctrl+X");
        KeyCombination selectAll = KeyCombination.keyCombination("Ctrl+A");
        KeyCombination undo = KeyCombination.keyCombination("Ctrl+Z");
        KeyCombination redo = KeyCombination.keyCombination("Ctrl+Y");

        /**
         * 是否过滤KeyTyped事件，如果返回true，则该事件将被忽略
         */
        default boolean filterKeyTyped(BDTextArea textArea, KeyEvent event) {
            return event.getCharacter() == null
                    || event.getCharacter().isEmpty()
                    || event.getCharacter().isBlank()
                    || event.getCharacter().charAt(0) <= 26;
        }

        /**
         * 是否过滤KeyPressed事件，如果返回true，则该事件将被忽略
         */
        default boolean filterKeyPressed(BDTextArea textArea, KeyEvent event) {
            return false;
        }

        /**
         * 处理KeyTyped事件
         */
        default void handleKeyTyped(BDTextArea area, KeyEvent event) {
            if (!area.isEditable()) return;
            SelectRange range = area.getSelectRange();
            if (range != null && range.start != null && range.end != null) {
                area.delete(range.start, range.end);
            }
            area.insertText(area.getCaretPosition(), event.getCharacter());
        }

        default void handleKeyPressed(BDTextArea textArea, KeyEvent event) {
            if (copy.match(event)) {
                event.consume();
                textArea.copy();
            } else if (paste.match(event)) {
                event.consume();
                textArea.paste();
            } else if (cut.match(event)) {
                event.consume();
                textArea.copy();
                if (!textArea.isEditable()) return;
                if (textArea.getSelectRange() != null && textArea.getSelectRange().start != null && textArea.getSelectRange().end != null)
                    textArea.delete(textArea.getSelectRange().start, textArea.getSelectRange().end);
            } else if (selectAll.match(event)) {
                event.consume();
                textArea.selectAll();
            } else if (undo.match(event)) {
                event.consume();
                if (!textArea.isEditable()) return;
                textArea.undo();
            } else if (redo.match(event)) {
                event.consume();
                if (!textArea.isEditable()) return;
                textArea.redo();
            } else switch (event.getCode()) {
                case UP:
                    event.consume();
                    BDTextAreaContent.Point temp = null;
                    if (event.isShiftDown()) {
                        if (textArea.getSelectRange() == null || textArea.getSelectRange().end == null || textArea.getSelectRange().end == null)
                            temp = textArea.getCaretPosition();
                        else if (textArea.getCaretPosition().equals(textArea.getSelectRange().end))
                            temp = textArea.getSelectRange().start;
                        else temp = textArea.getSelectRange().end;
                    }
                    textArea.caretUp();
                    if (event.isShiftDown() && temp != null)
                        textArea.setSelectRange(new SelectRange(temp, textArea.getCaretPosition()));
                    break;
                case DOWN:
                    event.consume();
                    temp = null;
                    if (event.isShiftDown()) {
                        if (textArea.getSelectRange() == null || textArea.getSelectRange().end == null || textArea.getSelectRange().end == null)
                            temp = textArea.getCaretPosition();
                        else if (textArea.getCaretPosition().equals(textArea.getSelectRange().end))
                            temp = textArea.getSelectRange().start;
                        else temp = textArea.getSelectRange().end;
                    }
                    textArea.caretDown();
                    if (event.isShiftDown() && temp != null)
                        textArea.setSelectRange(new SelectRange(temp, textArea.getCaretPosition()));
                    break;
                case LEFT:
                    event.consume();
                    temp = null;
                    if (event.isShiftDown()) {
                        if (textArea.getSelectRange() == null || textArea.getSelectRange().end == null || textArea.getSelectRange().end == null)
                            temp = textArea.getCaretPosition();
                        else if (textArea.getCaretPosition().equals(textArea.getSelectRange().end))
                            temp = textArea.getSelectRange().start;
                        else temp = textArea.getSelectRange().end;
                    }
                    textArea.caretLeft();
                    if (event.isShiftDown() && temp != null)
                        textArea.setSelectRange(new SelectRange(temp, textArea.getCaretPosition()));
                    if (!event.isShiftDown()) textArea.content.clearSelectRange();
                    break;
                case RIGHT:
                    event.consume();
                    temp = null;
                    if (event.isShiftDown()) {
                        if (textArea.getSelectRange() == null || textArea.getSelectRange().end == null || textArea.getSelectRange().end == null)
                            temp = textArea.getCaretPosition();
                        else if (textArea.getCaretPosition().equals(textArea.getSelectRange().end))
                            temp = textArea.getSelectRange().start;
                        else temp = textArea.getSelectRange().end;
                    }
                    textArea.caretRight();
                    if (event.isShiftDown() && temp != null)
                        textArea.setSelectRange(new SelectRange(temp, textArea.getCaretPosition()));
                    if (!event.isShiftDown()) textArea.content.clearSelectRange();
                    break;
                case ENTER:
                    event.consume();
                    if (!textArea.isEditable()) return;
                    SelectRange range = textArea.getSelectRange();
                    if (range != null && range.start != null && range.end != null)
                        textArea.delete(range.start, range.end);
                    textArea.insertText(textArea.getCaretPosition(), "\n");
                    break;

                case SPACE:
                    event.consume();
                    if (!textArea.isEditable()) return;
                    range = textArea.getSelectRange();
                    if (range != null && range.start != null && range.end != null)
                        textArea.delete(range.start, range.end);
                    textArea.insertText(textArea.getCaretPosition(), " ");
                    break;

                case TAB:
                    event.consume();
                    if (!textArea.isEditable()) return;
                    range = textArea.getSelectRange();
                    if (range != null && range.start != null && range.end != null)
                        textArea.delete(range.start, range.end);
                    textArea.insertText(textArea.getCaretPosition(), "\t");
                    event.consume();
                    break;

                case END:
                    textArea.caretEnd();
                    event.consume();
                    break;
                case STAR:
                    textArea.caretStart();
                    event.consume();
                    break;
                case BACK_SPACE:
                    event.consume();
                    if (!textArea.isEditable()) return;
                    if (textArea.getSelectRange() != null
                            && textArea.getSelectRange().start != null
                            && textArea.getSelectRange().end != null)
                        textArea.delete(textArea.getSelectRange().start, textArea.getSelectRange().end);
                    else {
                        BDTextAreaContent.Point caretPoint = textArea.getCaretPosition();
                        if (caretPoint.offset() == 0) {
                            List<?> dataBlocks = textArea.getTextInitFactory().splitDataBlocks(caretPoint.paragraph(), textArea.content.getParagraph(caretPoint.paragraph()));
                            if (caretPoint.virtualOffset() == 0
                                    && !dataBlocks.isEmpty()
                                    && dataBlocks.getFirst() instanceof DataBlock<?, ?> dataBlock
                                    && dataBlock.getMark() instanceof Mark mark
                                    && mark.getDirection().equals(MARK_DIRECTION.LEFT)) {
                                textArea.setCaretPosition(new BDTextAreaContent.Point(caretPoint.paragraph(), 0, -1));
                                return;
                            }
                            if (caretPoint.paragraph() > 0)
                                textArea.delete(new BDTextAreaContent.Point(caretPoint.paragraph(),
                                                -1),
                                        caretPoint);
                            return;
                        }
                        BDTextAreaContent.Point point = caretMove(textArea.getTextInitFactory().splitDataBlocks(caretPoint.paragraph(), textArea.content.getParagraph(caretPoint.paragraph())), caretPoint, MARK_DIRECTION.LEFT);
                        if (caretPoint.offset() != point.offset() || caretPoint.paragraph() != point.paragraph())
                            textArea.delete(new BDTextAreaContent.Point(caretPoint.paragraph(),
                                            caretPoint.offset() - 1),
                                    caretPoint);
                        else textArea.setCaretPosition(point);
                    }
                    break;
                case DELETE:
                    event.consume();
                    if (!textArea.isEditable()) return;
                    if (textArea.getSelectRange() != null && textArea.getSelectRange().start != null && textArea.getSelectRange().end != null)
                        textArea.delete(textArea.getSelectRange().start, textArea.getSelectRange().end);
                    else {
                        if (textArea.getCaretPosition().offset() == textArea.content.getParagraph(textArea.getCaretPosition().paragraph()).getLength()) {
                            if (textArea.getCaretPosition().paragraph() < textArea.content.linesNum() - 1) {
                                List<?> dataBlocks = textArea.getTextInitFactory().splitDataBlocks(textArea.getCaretPosition().paragraph(), textArea.content.getParagraph(textArea.getCaretPosition().paragraph()));
                                if (!dataBlocks.isEmpty()
                                        && dataBlocks.getLast() instanceof DataBlock<?, ?> dataBlock
                                        && dataBlock.getMark() instanceof Mark mark
                                        && mark.getDirection().equals(MARK_DIRECTION.RIGHT)) {
                                    if (textArea.getCaretPosition().virtualOffset() == 0) {
                                        textArea.setCaretPosition(new BDTextAreaContent.Point(textArea.getCaretPosition().paragraph(), textArea.getCaretPosition().offset(), 1));
                                        return;
                                    }
                                }
                            }
                            textArea.delete(textArea.getCaretPosition(), new BDTextAreaContent.Point(textArea.getCaretPosition().paragraph(), textArea.getCaretPosition().offset() + 1));
                            return;
                        }
                        BDTextAreaContent.Point caretPoint = textArea.getCaretPosition();
                        BDTextAreaContent.Point point = caretMove(
                                textArea.getTextInitFactory().splitDataBlocks(caretPoint.paragraph(),
                                        textArea.content.getParagraph(caretPoint.paragraph())),
                                caretPoint, MARK_DIRECTION.RIGHT);
                        if (caretPoint.offset() != point.offset() || caretPoint.paragraph() != point.paragraph())
                            textArea.delete(textArea.getCaretPosition(), new BDTextAreaContent.Point(textArea.getCaretPosition().paragraph(), textArea.getCaretPosition().offset() + 1));
                        else textArea.setCaretPosition(point);
                    }
                    break;
            }
        }
    }

    public interface HandleMouseEvent {
        /* 过滤鼠标点击事件，如果返回true，则该事件将被忽略 */
        default boolean filterMousePressed(BDTextArea textArea, MouseEvent event) {
            return false;
        }

        default void handleMousePressed(BDTextArea textArea, MouseEvent event, BDTextAreaContent.Point pressedPoint) {
            if (pressedPoint != null) {
                SelectRange tempRange = textArea.getSelectRange();
                BDTextAreaContent.Point tempPoint = textArea.getCaretPosition();
                if (!event.isShiftDown()) textArea.setCaretPosition(pressedPoint);
                else if (tempRange != null && tempRange.start != null && tempRange.end != null) {
                    textArea.setCaretPosition(pressedPoint);
                    if (tempPoint.equals(tempRange.start))
                        textArea.setSelectRange(new SelectRange(tempRange.end, pressedPoint));
                    else if (tempPoint.equals(tempRange.end))
                        textArea.setSelectRange(new SelectRange(tempRange.start, pressedPoint));
                } else {
                    textArea.setCaretPosition(pressedPoint);
                    textArea.setSelectRange(new SelectRange(tempPoint, pressedPoint));
                }
            }
        }

        default boolean filterMouseClicked(BDTextArea textArea, MouseEvent event) {
            return false;
        }

        default void handleMouseClicked(BDTextArea textArea, MouseEvent event, BDTextAreaContent.Point clickedPoint) {
            if (clickedPoint.paragraph() < textArea.getLineNum()) {
                if (event.getClickCount() == 3) {
                    if (clickedPoint.paragraph() != textArea.content.linesNum() - 1)
                        textArea.setSelectRange(new SelectRange(new BDTextAreaContent.Point(clickedPoint.paragraph(), 0), new BDTextAreaContent.Point(clickedPoint.paragraph() + 1, 0)));
                    else textArea.setSelectRange(new SelectRange(new BDTextAreaContent.Point(0, 0), clickedPoint));
                }
            }
        }
    }

    record OldDataBlockEntry(Text text, Region region) {
    }

    @SuppressWarnings("unchecked")
    private static final class StyleableProperties {
        private static final CssMetaData<BDTextArea, Paint> SELECTED_RANGE_FILL;
        private static final CssMetaData<BDTextArea, Insets> TEXT_FLOW_PADDING;
        private static final CssMetaData<BDTextArea, Number> NONE_LINE_HEIGHT;
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            SELECTED_RANGE_FILL = new CssMetaData<>("-bd-selected-Range-fill", PaintConverter.getInstance(), Color.web("#A6D2FF")) {
                @Override
                public boolean isSettable(BDTextArea textArea) {
                    return textArea.selectedRangeFill == null || !textArea.selectedRangeFill.isBound();
                }

                @Override
                public StyleableProperty<Paint> getStyleableProperty(BDTextArea textArea) {
                    return (StyleableProperty<Paint>) textArea.selectedRangeFillProperty();
                }
            };
            TEXT_FLOW_PADDING = new CssMetaData<>("-bd-text-flow-padding", InsetsConverter.getInstance(), new Insets(5, 0, 5, 0)) {
                @Override
                public boolean isSettable(BDTextArea textArea) {
                    return textArea.textFlowPadding == null || !textArea.textFlowPadding.isBound();
                }

                @Override
                public StyleableProperty<Insets> getStyleableProperty(BDTextArea textArea) {
                    return (StyleableProperty<Insets>) textArea.textFlowPaddingProperty();
                }
            };
            NONE_LINE_HEIGHT = new CssMetaData<>("-bd-text-flow-none-line-height", SizeConverter.getInstance(), 35.) {
                @Override
                public boolean isSettable(BDTextArea textArea) {
                    return textArea.noneLineHeight == null || !textArea.noneLineHeight.isBound();
                }

                @Override
                public StyleableProperty<Number> getStyleableProperty(BDTextArea textArea) {
                    return (StyleableProperty<Number>) textArea.noneLineHeightProperty();
                }
            };
            @SuppressWarnings("rawtypes")
            ArrayList var0 = new ArrayList(Control.getClassCssMetaData());
            Collections.addAll(var0, SELECTED_RANGE_FILL, TEXT_FLOW_PADDING, NONE_LINE_HEIGHT);
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.addAll(var0);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public record SelectRange(BDTextAreaContent.Point start, BDTextAreaContent.Point end) {
    }

    public record Range(int start, int end) {
    }

    ;
}
