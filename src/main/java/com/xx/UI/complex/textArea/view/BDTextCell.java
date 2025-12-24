package com.xx.UI.complex.textArea.view;

import com.xx.UI.complex.search.BDSearchBox;
import com.xx.UI.complex.textArea.content.BDTextAreaContent;
import com.xx.UI.complex.textArea.content.BDTextAreaContent.Point;
import com.xx.UI.complex.textArea.content.listener.SelectRangeChangeListener;
import com.xx.UI.complex.textArea.content.segment.NodeSegment;
import com.xx.UI.complex.textArea.content.segment.Paragraph;
import com.xx.UI.complex.textArea.content.segment.Segment;
import com.xx.UI.complex.textArea.content.segment.TextSegment;
import com.xx.UI.complex.textArea.view.dataFormat.analyse.Analyse;
import com.xx.UI.complex.textArea.view.dataFormat.analyse.BDAnalyse;
import com.xx.UI.complex.textArea.view.dataFormat.analyse.DataBlock;
import com.xx.UI.complex.textArea.view.dataFormat.mark.MARK_DIRECTION;
import com.xx.UI.complex.textArea.view.dataFormat.mark.Mark;
import com.xx.UI.complex.textArea.view.dataFormat.mark.MarkNode;
import com.xx.UI.ui.BDUI;
import com.xx.UI.ui.BDVirtualUI;
import com.xx.UI.util.BDMapping;
import com.xx.UI.util.BDScheduler;
import com.xx.UI.util.Util;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.HitInfo;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class BDTextCell extends ListCell<Object> implements BDUI, BDVirtualUI {
    private static final PseudoClass SELECT = PseudoClass.getPseudoClass("selected");
    protected final BDMapping mapping = new BDMapping();
    protected final BDMapping virtualMapping = new BDMapping();
    protected final BDTextAreaListView listView;
    protected final BDTextAreaContent content;
    protected final BDTextArea textArea;
    /* 专门负责放置文本节点的容器 */
    protected final HBox flowPane = new HBox();
    /* 光标 */
    private final Rectangle caretPath = new Rectangle();
    /* 专门负责放置高亮节点的容器 */
    private final AnchorPane selectionRange = new AnchorPane();
    private final AnchorPane tempSelectionRange = new BDTempFillPane();
    /* 专门储存搜索到的高亮节点容器 */
    private final AnchorPane searchResultLayout = new AnchorPane();
    private final AnchorPane fillLayout = new AnchorPane(selectionRange);
    protected final AnchorPane root = new AnchorPane(fillLayout, flowPane, searchResultLayout, tempSelectionRange, caretPath);
    // 带平滑动画的滚动
    private final Timeline scrollTimeline = new Timeline();
    Object lastEvent = null;
    private BDScheduler refreshCaretTask;
    private BDScheduler refreshSelectRangeTask;
    private BDScheduler refreshSearchResultTask;
    private List<DataBlock<?, ?>> tempDataBlocks;

    public BDTextCell(BDTextAreaListView listView) {
        this.listView = listView;
        this.content = listView.content;
        this.textArea = listView.textArea;
        listView.mapping.addChildren(mapping);
        mapping.addChildren(virtualMapping);
        mapping.addDisposeEvent(() -> {
            scrollTimeline.getKeyFrames().clear();
            scrollTimeline.stop();
        });
        initUI();
        initEvent();
        initProperty();
    }

    @Override
    public void initUI() {
        setGraphic(root);
        setCursor(Cursor.TEXT);
        setPadding(Insets.EMPTY);
        setBorder(Border.EMPTY);
        setFont(Font.font(0));
        setAlignment(Pos.CENTER_LEFT);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        selectionRange.setFocusTraversable(false);
        selectionRange.setMouseTransparent(true);
        AnchorPane.setBottomAnchor(selectionRange, 0.0);
        AnchorPane.setTopAnchor(selectionRange, 0.0);

        searchResultLayout.setFocusTraversable(false);
        searchResultLayout.setOpacity(.5);
        AnchorPane.setBottomAnchor(searchResultLayout, 0.0);
        AnchorPane.setTopAnchor(searchResultLayout, 0.0);
        AnchorPane.setLeftAnchor(searchResultLayout, 0.0);
        AnchorPane.setRightAnchor(searchResultLayout, 0.0);

        tempSelectionRange.setFocusTraversable(false);
        tempSelectionRange.setMouseTransparent(true);
        tempSelectionRange.setCursor(Cursor.DEFAULT);
        AnchorPane.setBottomAnchor(tempSelectionRange, 0.0);
        AnchorPane.setTopAnchor(tempSelectionRange, 0.0);

        caretPath.setWidth(2.0);
        caretPath.setMouseTransparent(true);
        caretPath.setFocusTraversable(false);

        root.setPadding(Insets.EMPTY);
        root.setBorder(Border.EMPTY);
        root.setBackground(Background.EMPTY);

        flowPane.setAlignment(Pos.CENTER_LEFT);
        flowPane.setBorder(Border.EMPTY);
        flowPane.setBackground(Background.EMPTY);
        flowPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        flowPane.setMaxWidth(Double.MAX_VALUE);
        flowPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        flowPane.setMaxHeight(Region.USE_PREF_SIZE);
        flowPane.setFillHeight(false);

        fillLayout.setMouseTransparent(true);
        fillLayout.setFocusTraversable(false);
        fillLayout.setBackground(Background.EMPTY);
        fillLayout.setBorder(Border.EMPTY);
        fillLayout.setPadding(Insets.EMPTY);
        fillLayout.setPrefWidth(Region.USE_COMPUTED_SIZE);
        fillLayout.setMaxWidth(Double.MAX_VALUE);
        fillLayout.setPrefHeight(Region.USE_COMPUTED_SIZE);
        fillLayout.setMaxHeight(Region.USE_PREF_SIZE);
        AnchorPane.setTopAnchor(fillLayout, 0.0);
        AnchorPane.setBottomAnchor(fillLayout, 0.0);
    }

    @Override
    public void initEvent() {
        mapping.addEventFilter(this, MouseEvent.MOUSE_PRESSED, event -> {
                    lastEvent = MouseEvent.MOUSE_PRESSED;
                    if (!listView.isFocused()) listView.requestFocus();
                    if (isEmpty() || textArea.getHandleMouseEvent().filterMousePressed(textArea, event))
                        return;
                    Point point = getPoint(event.getSceneX(), event.getSceneY());
                    if (point == null) return;
                    textArea.getHandleMouseEvent().handleMousePressed(textArea, event, point);
                })
                .addEventFilter(this, MouseEvent.MOUSE_CLICKED, e -> {
                    Object tempLastEvent = lastEvent;
                    if (tempLastEvent == null) return;
                    lastEvent = MouseEvent.MOUSE_CLICKED;
                    if (textArea.getHandleMouseEvent().filterMouseClicked(textArea, e)) return;
                    if (!tempLastEvent.equals(MouseDragEvent.MOUSE_DRAG_OVER) && !e.isShiftDown())
                        textArea.content.clearSelectRange();
                    textArea.getHandleMouseEvent().handleMouseClicked(textArea, e, isEmpty() ? new Point(textArea.getLineNum(), 0) : textArea.getCaretPosition());
                })
                .addEventFilter(this, MouseDragEvent.DRAG_DETECTED, event -> {
                    lastEvent = MouseDragEvent.DRAG_DETECTED;
                    if (isEmpty() || textArea.getHandleDragEvent().filterMouseDragDetected(textArea, event) || Objects.equals(tempSelectionRange, Util.searchEventTargetNode(event.getTarget(), BDTempFillPane.class)))
                        return;
                    startFullDrag();
                    listView.selectStart = textArea.getCaretPosition();
                })
                .addEventFilter(this, MouseDragEvent.MOUSE_DRAG_OVER, event -> {
                    lastEvent = MouseDragEvent.MOUSE_DRAG_OVER;
                    if (!listView.isFocused()) listView.requestFocus();
                    if (isEmpty() || textArea.getHandleDragEvent().filterMouseDragOver(textArea, event)) return;
                    // 使用更精确的坐标转换
                    Point2D paneLocal = root.sceneToLocal(event.getSceneX(), event.getSceneY());
                    if (paneLocal == null) return;
                    double mouseX = paneLocal.getX();
                    boolean findNode = false;
                    // 只根据 X 坐标判断命中节点
                    for (Node node : flowPane.getChildren()) {
                        double nodeStartX = node.getLayoutX();
                        double nodeEndX = nodeStartX + node.getBoundsInParent().getWidth();

                        // 只检查 X 坐标范围，忽略 Y 坐标
                        if (mouseX >= nodeStartX && mouseX <= nodeEndX) {

                            findNode = true;

                            // 转换到节点局部坐标系（只使用 X 坐标）
                            Point2D nodeLocal = flowPane.sceneToLocal(event.getSceneX(), event.getSceneY());
                            if (nodeLocal == null) break;

                            if (node instanceof BDTempTextPane textPane) {
                                BDText text = textPane.getContent();
                                // 转换到文本节点坐标系（只使用 X 坐标）
                                Point2D textLocal = text.sceneToLocal(event.getSceneX(), event.getSceneY());
                                if (textLocal != null) {
                                    Point end = calculatePointForNode(text, event.getSceneX(), mouseX, nodeStartX, nodeEndX, flowPane.getChildren().indexOf(node));
                                    textArea.setCaretPosition(end);
                                    textArea.setSelectRange(new BDTextArea.SelectRange(listView.selectStart, end));
                                }
                            } else if (node instanceof BDNodeTempPane nodeTempPane) {
                                Point end = calculatePointForNode(nodeTempPane.getContent(), event.getSceneX(), mouseX, nodeStartX, nodeEndX, flowPane.getChildren().indexOf(node));
                                textArea.setCaretPosition(end);
                                textArea.setSelectRange(new BDTextArea.SelectRange(listView.selectStart, end));
                            } else {
                                Point end = calculatePointForNode(node, event.getSceneX(), mouseX, nodeStartX, nodeEndX, flowPane.getChildren().indexOf(node));
                                textArea.setCaretPosition(end);
                                textArea.setSelectRange(new BDTextArea.SelectRange(listView.selectStart, end));
                            }
                            return;
                        }
                    }
                    // 未命中任何节点，将光标放在段落末尾
                    if (!findNode) {
                        Point end = createEndOfParagraphPoint();
                        textArea.setCaretPosition(end);
                        textArea.setSelectRange(new BDTextArea.SelectRange(listView.selectStart, end));
                    }
                })
                .addEventFilter(tempSelectionRange, MouseDragEvent.DRAG_DETECTED, event -> {
                    lastEvent = MouseDragEvent.DRAG_DETECTED;
                    if (textArea.getHandleDragEvent().filterMouseDragContentDetected(textArea, event)) return;
                    startFullDrag();
                    Dragboard dragboard = this.startDragAndDrop(TransferMode.MOVE);
                    HashMap<DataFormat, Object> map = new HashMap<>(1);
                    String selectedText = textArea.getSelectedText();
                    map.put(DataFormat.PLAIN_TEXT, selectedText);
                    dragboard.setContent(map);
                    BDTextArea.tempDragTextArea = textArea;
                    BDTextArea.SelectRange range = textArea.getSelectRange();
                    BDTextArea.tempDragParagraphs.clear();
                    if (range.start().paragraph() == range.end().paragraph()) {
                        BDTextArea.tempDragParagraphs.add(textArea.content.getParagraph(range.start().paragraph()).getParagraph(range.start().offset(), range.end().offset()));
                    } else {
                        BDTextArea.tempDragParagraphs.add(textArea.content.getParagraph(range.start().paragraph()).getParagraph(range.start().offset(), textArea.content.getParagraph(range.start().paragraph()).getLength()));
                        for (int i = range.start().paragraph() + 1; i < range.end().paragraph() - 1; i++)
                            BDTextArea.tempDragParagraphs.add(textArea.content.getParagraph(i));
                        BDTextArea.tempDragParagraphs.add(textArea.content.getParagraph(range.end().paragraph()).getParagraph(0, range.end().offset()));
                    }
                })
                .addEventFilter(this, DragEvent.DRAG_OVER, event -> {
                    lastEvent = DragEvent.DRAG_OVER;
                    if (!listView.getScene().getWindow().isFocused()) listView.getScene().getWindow().requestFocus();
                    if (!listView.isFocused()) listView.requestFocus();
                    if (isEmpty() || textArea.getHandleDragEvent().filterDragOver(textArea, event)) return;
                    event.acceptTransferModes(event.getTransferMode());
                    Point point = getPoint(event.getSceneX(), event.getSceneY());
                    textArea.setCaretPosition(point);
                })
                .addEventFilter(this, DragEvent.DRAG_DROPPED, event -> {
                    lastEvent = DragEvent.DRAG_DROPPED;
                    if (isEmpty() || !textArea.isEditable() || textArea.getHandleDragEvent().filterDragDropped(textArea, event))
                        return;
                    Point point = textArea.getCaretPosition();
                    if (Objects.equals(BDTextArea.tempDragTextArea, textArea)
                            && textArea.getSelectRange() instanceof BDTextArea.SelectRange(Point start, Point end)
                            && point.compareTo(start) >= 0) {
                        if (point.compareTo(end) > 0) {
                            String selectedText = textArea.getSelectedText();
                            if (point.paragraph() == end.paragraph())
                                point = new Point(end.paragraph(), point.offset() - selectedText.length());
                            else
                                point = new Point(point.paragraph() - (end.paragraph() - start.paragraph()), point.offset());
                            textArea.deleteSelect();
                            if (!BDTextArea.tempDragParagraphs.isEmpty() && hasNodeSegment())
                                insertTempParagraphs(point);
                            else insertDragboardContent(event, point);
                        }
                    } else {
                        if (BDTextArea.tempDragTextArea != null)
                            BDTextArea.tempDragTextArea.deleteSelect();
                        if (!BDTextArea.tempDragParagraphs.isEmpty() && hasNodeSegment())
                            insertTempParagraphs(point);
                        else insertDragboardContent(event, point);
                    }
                })
                .addEventFilter(this, DragEvent.DRAG_DONE, _ -> {
                    BDTextArea.tempDragTextArea = null;
                    BDTextArea.tempDragParagraphs.clear();
                })
                .addBDScheduler(refreshCaretTask = new BDScheduler(() -> refreshCaret(textArea.getCaretPosition()), 20))
                .addBDScheduler(refreshSelectRangeTask = new BDScheduler(() -> {
                    BDTextArea.SelectRange selectRange = textArea.getSelectRange();
                    if (selectRange != null)
                        refreshSelectRange(selectRange.start(), selectRange.end());
                    else refreshSelectRange(null, null);
                }, 30))
                .addBDScheduler(refreshSearchResultTask = new BDScheduler(() -> refreshSearchedRange(textArea.searchResultMap.get(getIndex())), 40));
    }

    private void insertDragboardContent(DragEvent event, Point point) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasString()) {
            String s = dragboard.getString();
            textArea.insertText(point, s);
            textArea.setSelectRange(new BDTextArea.SelectRange(point, point.moveNext(s)));
        }
        if (dragboard.hasUrl() && dragboard.hasFiles()) {
            Object content = dragboard.getContent(DataFormat.URL);
            textArea.insertNode(point, new NodeSegment<>(" ", _ -> new ImageView(new Image(content.toString()))));
            textArea.setSelectRange(new BDTextArea.SelectRange(point, new Point(point.paragraph(), point.offset() + 1)));
        }
    }

    private void insertTempParagraphs(Point point) {
        Point tempPoint = point.clone();
        StringBuilder temp = new StringBuilder();
        Paragraph last = BDTextArea.tempDragParagraphs.getLast();
        for (Paragraph tempDragParagraph : BDTextArea.tempDragParagraphs) {
            for (Segment<?> segment : tempDragParagraph.getSegments()) {
                if (segment instanceof TextSegment)
                    temp.append(segment);
                else if (segment instanceof NodeSegment<?> nodeSegment) {
                    if (!temp.isEmpty()) textArea.insertText(point, temp.toString());
                    textArea.insertNode(point, nodeSegment);
                    point = textArea.getCaretPosition();
                    temp.setLength(0);
                } else
                    throw new IllegalStateException("unknown segment type: " + segment.getClass());
            }
            if (last != tempDragParagraph) {
                temp.append("\n");
            }
        }
        if (!temp.isEmpty()) textArea.insertText(point, temp.toString());
        textArea.setSelectRange(new BDTextArea.SelectRange(tempPoint, textArea.getCaretPosition()));
        BDTextArea.tempDragParagraphs.clear();
    }

    private boolean hasNodeSegment() {
        for (Paragraph paragraph : BDTextArea.tempDragParagraphs)
            if (hasNodeSegment(paragraph)) return true;
        return false;
    }

    private boolean hasNodeSegment(Paragraph paragraph) {
        for (Segment<?> segment : paragraph.getSegments()) {
            if (segment instanceof NodeSegment<?>) return true;
        }
        return false;
    }

    private Point getPoint(double sceneX, double sceneY) {
        Point2D paneLocal = root.sceneToLocal(sceneX, sceneY);
        if (paneLocal == null) return null;
        double mouseX = paneLocal.getX();
        Point point = null;

        for (Node node : flowPane.getChildren()) {
            int i = flowPane.getChildren().indexOf(node);
            double nodeStartX = node.getLayoutX();
            double nodeEndX = nodeStartX + node.getBoundsInParent().getWidth();
            if (node instanceof BDTempPane<?> tempPane)
                node = tempPane.getContent();
            if (mouseX < nodeStartX || mouseX > nodeEndX) continue;
            point = calculatePointForNode(node, sceneX, mouseX, nodeStartX, nodeEndX, i);
            if (point != null) break;
        }
        if (point == null) return createEndOfParagraphPoint();
        return point;
    }

    @Override
    public void initProperty() {
        mapping
                .bindProperty(caretPath.opacityProperty(), Bindings.createIntegerBinding(() -> listView.caretVisible.get() && textArea.getCaretPosition().paragraph() == getIndex() ? 1 : 0, listView.caretVisible, textArea.caretPositionProperty(), indexProperty()))
                .bindProperty(selectionRange.backgroundProperty(), textArea.selectedRangeFillProperty().map(Background::fill))
                .bindProperty(tempSelectionRange.prefWidthProperty(), selectionRange.prefWidthProperty())
                .bindProperty(tempSelectionRange.visibleProperty(), selectionRange.visibleProperty())
                .bindProperty(flowPane.translateYProperty(), root.heightProperty().subtract(flowPane.heightProperty()).divide(2))
                .bindProperty(flowPane.paddingProperty(), textArea.textFlowPaddingProperty())
                .addListener(() -> refreshCaretTask.run(), true, textArea.caretPositionProperty(), textArea.refreshTempValue)
                .addListener(textArea.refreshTempValue, (_, _, _) -> initVirtualUI())
                .addListener(textArea.tempRefresh, (_, _, _) -> refreshSearchedRange(textArea.searchResultMap.get(getIndex())))
                .addListener(selectionRange.layoutXProperty(), (_, _, nv) -> tempSelectionRange.setLayoutX(nv.doubleValue()));
        SelectRangeChangeListener selectRangeChangeListener = event -> {
            Point newEndPoint = event.getNewEndPoint();
            Point newStartPoint = event.getNewStartPoint();
            refreshSelectRange(newStartPoint, newEndPoint);
        };
        textArea.addSelectRangeChangeListener(selectRangeChangeListener);
        mapping.addDisposeEvent(() -> textArea.removeSelectRangeChangeListener(selectRangeChangeListener));
    }

    private void refreshSelectRange(Point newStartPoint, Point newEndPoint) {
        selectionRange.prefWidthProperty().unbind();
        selectionRange.setPrefWidth(0);
        if (isEmpty() || newStartPoint == null || newEndPoint == null || getIndex() < newStartPoint.paragraph() || getIndex() > newEndPoint.paragraph()) {
            selectionRange.setVisible(false);
            return;
        }
        if (getIndex() == newEndPoint.paragraph() && getIndex() == newStartPoint.paragraph())
            computeSelectionRange(newStartPoint.offset(), newEndPoint.offset(), false);
        else if (getIndex() == newStartPoint.paragraph())
            computeSelectionRange(newStartPoint.offset(), -1, true);
        else if (getIndex() == newEndPoint.paragraph())
            computeSelectionRange(0, newEndPoint.offset(), false);
        else
            computeSelectionRange(0, -1, true);
    }

    private void refreshSearchedRange(List<BDSearchBox.SearchResult> searchResults) {
        searchResultLayout.getChildren().forEach(e -> ((BDSearchHighLightLayout) e).dispose());
        searchResultLayout.getChildren().clear();
        if (searchResults == null || searchResults.isEmpty()) {
            searchResultLayout.setVisible(false);
            return;
        }
        searchResults.forEach(result -> {
            BDSearchHighLightLayout lightLayout = computeSearchRange(result.startOffset(), result.endOffset(), result.resultIndex(), result.fullLine());
            if (lightLayout != null) {
                searchResultLayout.getChildren().add(lightLayout);
                boolean b = result.resultIndex() == textArea.searchBlockIndex.get();
                lightLayout.pseudoClassStateChanged(SELECT, b);
            }
        });
        searchResultLayout.setVisible(!searchResultLayout.getChildren().isEmpty());
    }

    @Override
    protected void updateItem(Object o, boolean b) {
        super.updateItem(o, b);
        disposeVirtual();
        if (isEmpty() || getIndex() >= content.linesNum() || getIndex() < 0)
            return;
        initVirtualUI();
    }

    @Override
    public void initVirtualUI() {
        if (isEmpty() || getIndex() >= content.linesNum() || getIndex() < 0) return;
        flowPane.getChildren().setAll(initParagraph(getIndex()));
        root.applyCss();
        if (flowPane.getChildren().isEmpty()) setPrefHeight(textArea.getNoneLineHeight());
        initVirtualEvent();
        initVirtualProperty();
        refreshCaretTask.run();
        refreshSelectRangeTask.run();
        refreshSearchResultTask.run();
    }

    @Override
    public void disposeVirtual() {
        caretPath.setHeight(0);
        virtualMapping.dispose();
        flowPane.getChildren().clear();
    }

    private void refreshCaret(Point caret) {
        Objects.requireNonNull(caret, "caret cannot be null");
        refreshCaretDataBlockEntry(caret);
        /* 保证为空时正常显示*/
        if (flowPane.getPadding() != null)
            caretPath.setHeight(getHeight() - flowPane.getPadding().getTop() - flowPane.getPadding().getBottom());
        else
            caretPath.setHeight(getHeight());
        caretPath.setTranslateY((getHeight() - caretPath.getHeight()) / 2);
        if (caret.paragraph() != getIndex() || tempDataBlocks == null || tempDataBlocks.isEmpty() || (tempDataBlocks.size() == 1 && Objects.equals(tempDataBlocks.getFirst().getSegment().getInfo(), ""))) {
            caretPath.setTranslateX(0);
            return;
        }
        if (flowPane.getChildren().isEmpty()) return;
        int caretOffset = caret.offset();
        int currentLen = 0;
        int blockIndex = -1;
        int offset = 0;
        // 遍历数据块定位光标所在块
        for (int i = 0; i < tempDataBlocks.size(); i++) {
            int blockLength = tempDataBlocks.get(i).getSegment().getLength();
            if (currentLen <= caretOffset && caretOffset < currentLen + blockLength) {
                blockIndex = i;
                offset = caretOffset - currentLen;
                break;
            }
            currentLen += blockLength;
        }
        // 处理光标在所有块之后的情况
        if (blockIndex < 0) {
            blockIndex = tempDataBlocks.size() - 1;
            offset = tempDataBlocks.get(blockIndex).getSegment().getLength();
        }
//        计算光标位置。
        Node node = getCaretNode(blockIndex);


//        光标virtualOffset不为0的情况。
        if (caret.virtualOffset() != 0) {
            int i = flowPane.getChildren().indexOf(node);
            Point caretPosition = new Point(caret.paragraph(), caret.offset(), 0);
            if (caret.virtualOffset() == -1) {
                if (i > 0 && flowPane.getChildren().get(i - 1) instanceof MarkNode markNode)
                    node = markNode;
                else {
                    textArea.setCaretPosition(caretPosition);
                    return;
                }
            } else if (caret.virtualOffset() == -2) {
                if (i > 1 && flowPane.getChildren().get(i - 2) instanceof MarkNode markNode)
                    node = markNode;
                else {
                    textArea.setCaretPosition(caretPosition);
                    return;
                }
            } else if (caret.virtualOffset() == 1) {
                if (i < flowPane.getChildren().size() - 1 && flowPane.getChildren().get(i + 1) instanceof MarkNode markNode)
                    node = markNode;
                else {
                    textArea.setCaretPosition(caretPosition);
                    return;
                }
            } else throw new UnsupportedOperationException("Unsupported virtualOffset: " + caret.virtualOffset());
        }

        double layoutX = node.getLayoutX();
        Bounds nodeBounds = node.getLayoutBounds();
        double height = nodeBounds.getHeight();
        if (node instanceof BDTempPane<?> tempPane)
            node = tempPane.getContent();
        if (blockIndex != 0 && !(node instanceof BDText))
            height = getCaretNode(blockIndex - 1).getLayoutBounds().getHeight();
        caretPath.setHeight(height);
        if (node instanceof BDText text) {
            PathElement[] pathElements = text.caretShape(offset, true);
            if (pathElements != null && pathElements.length > 0)
                caretPath.setTranslateX(((MoveTo) pathElements[0]).getX() + layoutX);
        } else caretPath.setTranslateX(layoutX + (offset > 0 ? nodeBounds.getWidth() : 0));
        caretPath.setTranslateY((getHeight() - height) / 2);

        smoothScrollToCaret();
    }

    private void smoothScrollToCaret() {
        double caretX = caretPath.getTranslateX();
        double currentScrollX = textArea.listView.getHorizontalScroll();
        double viewportWidth = textArea.getWidth();

        double leftMargin = viewportWidth * 0.05;  // 20% 左边界
        double rightMargin = viewportWidth * 0.95; // 30% 右边界

        double caretScreenX = caretX - currentScrollX;

        // 计算目标滚动位置
        double targetScrollX;
        if (caretScreenX < leftMargin) {
            // 光标在左边界区域，滚动使光标位于左边界
            targetScrollX = caretX - leftMargin;
        } else if (caretScreenX > rightMargin) {
            // 光标在右边界区域，滚动使光标位于右边界
            targetScrollX = caretX - rightMargin;
        } else {
            // 光标在可见区域内，不需要滚动
            return;
        }

        // 限制滚动范围
        targetScrollX = Math.max(0, Math.min(targetScrollX, textArea.listView.horizontalScrollMax.get()));

        // 如果目标位置与当前位置差异很小，不执行动画
        if (Math.abs(targetScrollX - currentScrollX) < 2) {
            textArea.listView.horizontalScroll.set(targetScrollX);
            return;
        }

        // 停止之前的动画
        scrollTimeline.stop();

        // 创建平滑滚动动画
        scrollTimeline.getKeyFrames().clear();
        scrollTimeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(textArea.listView.horizontalScroll, targetScrollX, Interpolator.EASE_BOTH)
                )
        );

        scrollTimeline.play();
    }

    private void refreshCaretDataBlockEntry(Point caret) {
        if (caret.paragraph() == getIndex()) {
            BDAnalyse.DataBlockEntry<?> dataBlockEntry = textArea.getTextInitFactory().getAnalyse().getDataBlockEntry(caret);
            if (dataBlockEntry != null) {
                Node left = null;
                Node right = null;
                BDText leftText = null;
                BDText rightText = null;
                Region leftRegion = null;
                Region rightRegion = null;
                if (dataBlockEntry.left() != null) {
                    int index = tempDataBlocks.indexOf(dataBlockEntry.left());
                    if (index >= 0) left = getCaretNode(index);
                    if (left instanceof BDTempTextPane textPane) {
                        leftText = textPane.getContent();
                        leftRegion = textPane;
                    }
                }
                if (dataBlockEntry.right() != null) {
                    int index = tempDataBlocks.indexOf(dataBlockEntry.right());
                    if (index >= 0) right = getCaretNode(index);
                    if (right instanceof BDTempTextPane textPane) {
                        rightText = textPane.getContent();
                        rightRegion = textPane;
                    }
                }
                textArea.addDataBlockEntryText(leftText, leftRegion, rightText, rightRegion);
                if (leftText == null && rightText == null) {
                    textArea.getTextInitFactory().renderingDataBlockEntryNode(dataBlockEntry, left, right);
                    textArea.getTextInitFactory().renderingDataBlockEntryText(dataBlockEntry, null, null, null, null);
                } else if (leftText == null) {
                    String tempString = rightText.getText();
                    textArea.getTextInitFactory().renderingDataBlockEntryNode(dataBlockEntry, left, right);
                    textArea.getTextInitFactory().renderingDataBlockEntryText(dataBlockEntry, null, null, rightText, rightRegion);
                    if (!Objects.equals(tempString, rightText.getText()))
                        throw new IllegalStateException("渲染后的文本内容不一致");
                } else if (rightText == null) {
                    String tempString = leftText.getText();
                    textArea.getTextInitFactory().renderingDataBlockEntryNode(dataBlockEntry, left, right);
                    textArea.getTextInitFactory().renderingDataBlockEntryText(dataBlockEntry, leftText, leftRegion, null, null);
                    if (!Objects.equals(tempString, leftText.getText()))
                        throw new IllegalStateException("渲染后的文本内容不一致");
                } else {
                    String leftTempString = leftText.getText();
                    String rightTempString = rightText.getText();
                    textArea.getTextInitFactory().renderingDataBlockEntryNode(dataBlockEntry, left, right);
                    textArea.getTextInitFactory().renderingDataBlockEntryText(dataBlockEntry, leftText, leftRegion, rightText, rightRegion);
                    if (!Objects.equals(leftTempString, leftText.getText()) && !Objects.equals(rightTempString, rightText.getText()))
                        throw new IllegalStateException("渲染后的文本内容不一致");
                }
            }
        }
    }

    private void computeSelectionRange(int start, int end, boolean fillRight) {
        if (start == end) return;
        int currentLen = 0;
        double startX = -1;
        double endX = -1;
        for (Node node : flowPane.getChildren()) {
            double layoutX = node.getLayoutX();
            switch (node) {
                case BDTempTextPane textPane -> {
                    BDText text = textPane.getContent();
                    if (start < currentLen + text.getText().length() && startX == -1) {
                        PathElement[] pathElements = text.caretShape(start - currentLen, true);
                        if (pathElements != null && pathElements.length > 0)
                            startX = ((MoveTo) pathElements[0]).getX() + layoutX;
                    } else if (start == currentLen + text.getText().length() && startX == -1) {
                        int i = flowPane.getChildren().indexOf(node);
                        if (i >= flowPane.getChildren().size() - 1 || !(flowPane.getChildren().get(i + 1) instanceof MarkNode)) {
                            PathElement[] pathElements = text.caretShape(start - currentLen, true);
                            if (pathElements != null && pathElements.length > 0)
                                startX = ((MoveTo) pathElements[0]).getX() + layoutX;
                        }
                    }
                    if (end <= currentLen + text.getText().length() && endX == -1 && !fillRight) {
                        PathElement[] pathElements = text.caretShape(end - currentLen, true);
                        if (pathElements != null && pathElements.length > 0)
                            endX = ((MoveTo) pathElements[0]).getX() + layoutX;
                        else endX = layoutX + node.getLayoutBounds().getWidth();
                    }
                    currentLen += text.getText().length();
                }
                case MarkNode _ -> {
                    Point startpoint = textArea.getSelectRange().start();
//                出现在当前段落的开头的情况。
                    if (startpoint.paragraph() == getIndex()) {
                        if (start <= currentLen && startpoint.virtualOffset() == -1 && startX == -1 && flowPane.getChildren().indexOf(node) == 0)
                            startX = layoutX;
                    } else if (flowPane.getChildren().indexOf(node) == 0 && start <= currentLen && startX == -1)
                        startX = layoutX;
                }
                case BDTempPane<?> tempPane -> {
                    if (start <= currentLen && startX == -1)
                        startX = layoutX;
                    if (end <= currentLen + 1 && endX == -1 && !fillRight) {
                        endX = layoutX + tempPane.getContent().getLayoutBounds().getWidth();
                    }
                    currentLen++;
                    if (start <= currentLen && startX == -1)
                        startX = layoutX + tempPane.getContent().getLayoutBounds().getWidth();
                }
                default -> {
                }
            }
            if (startX != -1 && (endX != -1 || fillRight)) break;
        }
        selectionRange.setLayoutX(startX);
        selectionRange.prefWidthProperty().unbind();
        if (fillRight) selectionRange.prefWidthProperty().bind(widthProperty().subtract(startX + 2));
        else selectionRange.setPrefWidth((endX - startX));
        selectionRange.setVisible(true);
    }

    private BDSearchHighLightLayout computeSearchRange(int start, int end, int resultIndex, boolean fillRight) {
        int currentLen = 0;
        double startX = -1;
        double endX = -1;
        for (Node node : flowPane.getChildren()) {
            double layoutX = node.getLayoutX();
            switch (node) {
                case BDTempTextPane textPane -> {
                    BDText text = textPane.getContent();
                    if (start < currentLen + text.getText().length() && startX == -1) {
                        PathElement[] pathElements = text.caretShape(start - currentLen, true);
                        if (pathElements != null && pathElements.length > 0)
                            startX = ((MoveTo) pathElements[0]).getX() + layoutX;
                    } else if (start == currentLen + text.getText().length() && startX == -1) {
                        int i = flowPane.getChildren().indexOf(node);
                        if (i >= flowPane.getChildren().size() - 1 || !(flowPane.getChildren().get(i + 1) instanceof MarkNode)) {
                            PathElement[] pathElements = text.caretShape(start - currentLen, true);
                            if (pathElements != null && pathElements.length > 0)
                                startX = ((MoveTo) pathElements[0]).getX() + layoutX;
                        }
                    }
                    if (end <= currentLen + text.getText().length() && endX == -1 && !fillRight) {
                        PathElement[] pathElements = text.caretShape(end - currentLen, true);
                        if (pathElements != null && pathElements.length > 0)
                            endX = ((MoveTo) pathElements[0]).getX() + layoutX;
                        else endX = layoutX + node.getLayoutBounds().getWidth();
                    }
                    currentLen += text.getText().length();
                }
                case MarkNode _ -> {
                    Point startpoint = textArea.getCaretPosition();
//                出现在当前段落的开头的情况。
                    if (startpoint.paragraph() == getIndex()) {
                        if (start <= currentLen && startpoint.virtualOffset() == -1 && startX == -1 && flowPane.getChildren().indexOf(node) == 0)
                            startX = layoutX;
                    } else if (flowPane.getChildren().indexOf(node) == 0 && start <= currentLen && startX == -1)
                        startX = layoutX;
                }
                case BDTempPane<?> tempPane -> {
                    if (start <= currentLen && startX == -1)
                        startX = layoutX;
                    if (end <= currentLen + 1 && endX == -1 && !fillRight) {
                        endX = layoutX + tempPane.getContent().getLayoutBounds().getWidth();
                    }
                    currentLen++;
                    if (start <= currentLen && startX == -1)
                        startX = layoutX + tempPane.getContent().getLayoutBounds().getWidth();
                }
                default -> {
                }
            }
            if (startX != -1 && (endX != -1 || fillRight)) break;
        }
        BDSearchHighLightLayout lightLayout = new BDSearchHighLightLayout(resultIndex, textArea.searchBlockIndex);
        lightLayout.setLayoutX(startX);
        if (fillRight) lightLayout.prefWidthProperty().bind(widthProperty().subtract(startX + 2));
        else lightLayout.setPrefWidth((endX - startX));
        return lightLayout;
    }

    // 新增辅助方法
    private Point calculatePointForNode(Node node, double sceneX, double mouseX, double nodeStartX, double nodeEndX, int index) {
        if (node instanceof BDText text) {
            return handleTextNode(text, sceneX, index);
        } else if (node instanceof MarkNode markNode) {
            return handleMarkNode(markNode, mouseX, nodeStartX, nodeEndX, index);
        } else {
            return handleOtherNode(node, mouseX, nodeStartX, nodeEndX, index);
        }
    }

    private Point handleTextNode(BDText text, double sceneX, int index) {
        Point2D textLocal = text.sceneToLocal(sceneX, 0);
        if (textLocal == null) return null;

        HitInfo hitInfo = text.hitTest(new Point2D(textLocal.getX(), 0));
        int offsetInBlock = hitInfo.getCharIndex() + (hitInfo.isLeading() ? 0 : 1);
        int virtualOffset = calculateVirtualOffsetForText(index, offsetInBlock, text);

        return new Point(getIndex(), getOffset(text, offsetInBlock), virtualOffset);
    }

    private int calculateVirtualOffsetForText(int index, int offsetInBlock, BDText text) {
        if (offsetInBlock != text.getText().length()) return 0;

        int remaining = flowPane.getChildren().size() - index - 1;
        if (remaining >= 2 &&
                flowPane.getChildren().get(index + 1) instanceof MarkNode &&
                flowPane.getChildren().get(index + 2) instanceof MarkNode) {
            return -2;
        } else if (remaining >= 1 &&
                flowPane.getChildren().get(index + 1) instanceof MarkNode markNode &&
                flowPane.getChildren().getLast() != markNode
        ) {
            return -1;
        }
        return 0;
    }

    private Point handleMarkNode(MarkNode markNode, double mouseX, double nodeStartX, double nodeEndX, int index) {
        boolean hitLeft = mouseX < (nodeStartX + nodeEndX) / 2;
        MARK_DIRECTION direction = markNode.getMark().getDirection();
        int virtualOffset = 0;

        if (direction == MARK_DIRECTION.LEFT && hitLeft) {
            virtualOffset = -1;
        } else if (direction == MARK_DIRECTION.RIGHT) {
            int nextIndex = index + 1;
            boolean hasNext = nextIndex < flowPane.getChildren().size();
            boolean nextIsMark = hasNext && flowPane.getChildren().get(nextIndex) instanceof MarkNode;

            if (!hitLeft) {
                virtualOffset = nextIsMark ? -1 : 1;
            } else {
                virtualOffset = nextIsMark ? -2 : -1;
            }
            if (flowPane.getChildren().indexOf(markNode) == flowPane.getChildren().size() - 1 && hitLeft)
                virtualOffset = 0;
        }
        return new Point(getIndex(), getOffset(markNode, 0), virtualOffset);
    }

    private Point handleOtherNode(Node node, double mouseX, double nodeStartX, double nodeEndX, int index) {
        boolean hitLeft = mouseX < (nodeStartX + nodeEndX) / 2;
        int offsetInNode = hitLeft ? 0 : 1;
        int virtualOffset = hitLeft ? 0 : calculateVirtualOffsetForOtherNode(index);

        return new Point(getIndex(), getOffset(node, offsetInNode), virtualOffset);
    }

    private int calculateVirtualOffsetForOtherNode(int index) {
        int remaining = flowPane.getChildren().size() - index - 1;
        if (remaining >= 2 &&
                flowPane.getChildren().get(index + 1) instanceof MarkNode &&
                flowPane.getChildren().get(index + 2) instanceof MarkNode) {
            return -2;
        } else if (remaining >= 1 &&
                flowPane.getChildren().get(index + 1) instanceof MarkNode markNode &&
                flowPane.getChildren().getLast() != markNode) {
            return -1;
        }
        return 0;
    }

    private Point createEndOfParagraphPoint() {
        int virtualOffset = 0;
        if (!tempDataBlocks.isEmpty()) {
            Mark lastMark = tempDataBlocks.getLast().getMark();
            if (lastMark != null && lastMark.getDirection() == MARK_DIRECTION.RIGHT) {
                virtualOffset = 1;
            }
        }

        int totalLength = tempDataBlocks.stream()
                .mapToInt(block -> block.getSegment().getLength())
                .sum();

        return new Point(getIndex(), totalLength, virtualOffset);
    }

    @SuppressWarnings("unchecked")
    protected final List<Node> initParagraph(int paragraphIndex) {
        tempDataBlocks = textArea.getTextInitFactory().splitDataBlocks(paragraphIndex, content.getParagraph(paragraphIndex));
        List<Node> nodes = new ArrayList<>();
        for (DataBlock<?, ?> block : tempDataBlocks) {
            Node node = viewDataBlock(block);
            if (block.getMark() != null) {
                if (block.getMark().getDirection() == MARK_DIRECTION.LEFT) {
                    nodes.add(block.getMark().getNode());
                    nodes.add(node);
                } else if (block.getMark().getDirection() == MARK_DIRECTION.RIGHT) {
                    nodes.add(node);
                    nodes.add(block.getMark().getNode());
                } else
                    throw new UnsupportedOperationException("Unsupported mark direction: " + block.getMark().getDirection());
            } else nodes.add(node);
        }
        return nodes;
    }

    private int getOffset(Node node, int offsetInNode) {
        int totalOffset = 0;
        for (Node n : flowPane.getChildren()) {
            if (n == node || n instanceof BDTempPane<?> tempPane && tempPane.getContent() == node) {
                // 到达目标节点，添加节点内偏移
                return totalOffset + offsetInNode;
            }
            if (n instanceof MarkNode) continue;
            // 累计前面节点的长度
            if (n instanceof BDTempTextPane textPane) {
                totalOffset += textPane.getContent().getText().length();
            } else {
                totalOffset += 1; // NodeSegment视为长度1
            }
        }
        return totalOffset; // 未找到时返回
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<?> & Analyse.BDTextEnum<T>> Node viewDataBlock(DataBlock<T, ?> dataBlock) {
        if (dataBlock.getSegment() instanceof TextSegment) {
            BDText text = new BDText(dataBlock.getSegment().getInfo());
            String content = text.getText();
            BDTempTextPane textPane = new BDTempTextPane(text);
            textArea.getTextInitFactory().renderingText(text, textPane, dataBlock);
            if (!Objects.equals(content, text.getText()))
                throw new IllegalStateException("renderingText() should not modify the text content");
            return textPane;
        } else if (dataBlock.getSegment() instanceof NodeSegment<?> nodeSegment) {
            Node node = nodeSegment.getNode();
            BDNodeTempPane nodePane = new BDNodeTempPane(node);
            textArea.getTextInitFactory().renderingNode(node, nodePane, dataBlock);
            return nodePane;
        }
        throw new UnsupportedOperationException("Unsupported segment type: " + dataBlock.getSegment().getClass());
    }

    private Node getCaretNode(int blockIndex) {
        List<Node> list = flowPane.getChildren().stream().filter(node -> !(node instanceof MarkNode)).toList();
        if (list.isEmpty()) throw new IllegalStateException("flowPane is empty");
        return list.get(blockIndex);
    }

    /**
     * BDText 类，用于包装 Text 节点, 目的是与 Text 节点分离，以便在 BDTextCell 中使用
     */
    static final class BDText extends Text {
        public BDText(String text) {
            super(text);
        }
    }

    /**
     * BDTempFillPane 类，用于包装 AnchorPane 节点, 目的是与 AnchorPane 节点分离，以便在 BDTextCell 中使用
     *
     */
    static final class BDTempFillPane extends AnchorPane {
    }

    /**
     * BDTempTextPane 专门用来包装Text节点，目的是保证Text的背景色
     */
    static final class BDTempTextPane extends BDTempPane<BDText> {
        public BDTempTextPane(BDText content) {
            super(content);
        }
    }

    /**
     * BDSearchHighLightLayout 类，目的是实现搜索高亮效果
     *
     */
    static final class BDSearchHighLightLayout extends Pane {
        private final static String STYLE_CLASS = "search-highlight-layout";

        public BDSearchHighLightLayout(int index, IntegerProperty integerProperty) {
            addEventFilter(MouseEvent.MOUSE_PRESSED, _ -> integerProperty.set(index));
            AnchorPane.setTopAnchor(this, .0);
            AnchorPane.setBottomAnchor(this, .0);
            getStyleClass().setAll(STYLE_CLASS);
        }

        public void dispose() {
            prefWidthProperty().unbind();
        }
    }
}
