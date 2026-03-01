package com.xx.UI.complex.textArea.content;

import com.xx.UI.complex.textArea.content.listener.*;
import com.xx.UI.complex.textArea.content.segment.NodeSegment;
import com.xx.UI.complex.textArea.content.segment.Paragraph;
import com.xx.UI.complex.textArea.content.segment.Segment;
import com.xx.UI.complex.textArea.content.segment.TextSegment;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class BDTextAreaContent implements Serializable, Content {
    public static Long CHANGED__INTERVALS_TIME = 2000L;
    private final transient List<Paragraph> paragraphs = new ArrayList<>();
    private final transient HistoryMemos historyMemos = new HistoryMemos(); // 历史记录
    private final StringBuilder fullContent = new StringBuilder(); // 缓存整个文本内容
    private final transient List<ContentChangeListener> listeners = new CopyOnWriteArrayList<>(); // 新增监听器列表
    private final transient List<Runnable> changeRunnable = new CopyOnWriteArrayList<>();// 如果发生变更就调用。
    private final transient List<CaretChangeListener> caretChangeListeners = new CopyOnWriteArrayList<>(); // 新增光标监听器列表
    private final transient List<SelectRangeChangeListener> selectRangeChangeListeners = new CopyOnWriteArrayList<>(); // 新增光标监听器列表
    private Point  caretPosition = new Point(0, 0); // 光标位置
    //    选中区域
    private Point start = null; // 开始位置
    private Point end = null; // 结束位置


    public BDTextAreaContent() {
        ensureAtLeastOneParagraph();
    }

    public static String formatParagraphs(List<Paragraph> paragraphs) {
        StringBuilder fullContent = new StringBuilder();
        int size = paragraphs.size();
        for (int i = 0; i < size; i++) {
            fullContent.append(paragraphs.get(i).toString());
            if (i < size - 1) {
                fullContent.append('\n'); // 段落间添加换行符
            }
        }
        return fullContent.toString();
    }

    @Override
    public String get(int start, int end) {
        if (start < 0 || end > length() || start > end) {
            throw new IndexOutOfBoundsException(
                    String.format("Invalid Range [%d, %d] in content of length %d", start, end, length())
            );
        }
        if (start == end) return "";
        return toString().substring(start, end);
    }

    public String getSelectedText() {
        if (start == null || end == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = start.paragraph; i <= end.paragraph; i++) {
            Paragraph paragraph = paragraphs.get(i);
            if (i == start.paragraph) {
                sb.append(paragraph.getText(start.offset, paragraph.getLength()));
                if (start.paragraph != end.paragraph) sb.append('\n');
                else sb.delete(end.offset - start.offset, sb.length());
            } else if (i == end.paragraph) {
                sb.append(paragraph.getText(0, end.offset));
            } else {
                sb.append(paragraph.toString());
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    @Override
    public void insert(int index, String text, boolean fireEvent) {
        if (index < 0 || index > length()) {
            throw new IndexOutOfBoundsException(
                    String.format("Insert position %d out of bounds [0, %d]", index, length())
            );
        }

        if (text == null) throw new IllegalArgumentException("Text cannot be null");
        if (text.isEmpty()) return;

        text = Content.filterInput(text, false, false);
        if (text.isEmpty()) return;
        historyMemos.execute(new InsertCommand(index, text, fireEvent, this));
        clearSelectRange();
    }

    public void insert(Point caretPoint, String text, boolean fireEvent) {
        // 验证输入点有效性
        if (caretPoint == null) {
            throw new IllegalArgumentException("Caret point cannot be null");
        }
        if (caretPoint.paragraph < 0 || caretPoint.paragraph >= paragraphs.size()) {
            throw new IndexOutOfBoundsException(
                    String.format("Invalid paragraph index %d in caret point", caretPoint.paragraph)
            );
        }
        Paragraph targetPara = paragraphs.get(caretPoint.paragraph);
        if (caretPoint.offset < 0 || caretPoint.offset > targetPara.getLength()) {
            throw new IndexOutOfBoundsException(
                    String.format("Invalid offset %d in paragraph %d", caretPoint.offset, caretPoint.paragraph)
            );
        }

        // 计算全局插入位置
        int globalIndex = 0;
        for (int i = 0; i < caretPoint.paragraph; i++) {
            globalIndex += paragraphs.get(i).getLength() + 1; // +1 for newline
        }
        globalIndex += caretPoint.offset;

        // 调用现有的insert方法
        insert(globalIndex, text, fireEvent);
    }

    void insertAction(int index, String text, boolean fireEvent) {
        // 记录事件参数
        int startParaIndex;
        int endParaIndex;
        int startOffset;
        int endOffset;


        ensureAtLeastOneParagraph();
        List<String> lines = splitTextIntoLines(text);
        if (lines.isEmpty()) return;

        Point location = locatePosition(index);
        int paraIndex = location.paragraph;
        int offset = location.offset;
        Paragraph targetPara = paragraphs.get(paraIndex);

        if (lines.size() == 1) {
            targetPara.insertString(offset, lines.getFirst());
            // 设置事件参数（单行）
            startParaIndex = paraIndex;
            endParaIndex = paraIndex;
            startOffset = offset;
            endOffset = offset + lines.getFirst().length();
        } else {
            Paragraph endParagraph = targetPara.getParagraph(offset, targetPara.getLength());
            targetPara.replaceString(offset, targetPara.getLength(), lines.getFirst());

            List<Paragraph> newParagraphs = new ArrayList<>();
            for (int i = 1; i < lines.size() - 1; i++) {
                Paragraph p = createParagraphWithText(lines.get(i));
                newParagraphs.add(p);
            }

            Paragraph lastPara = createParagraphWithText(lines.getLast());
            lastPara.insertParagraph(lastPara.getLength(), endParagraph);
            newParagraphs.add(lastPara);

            paragraphs.addAll(paraIndex + 1, newParagraphs);

            // 设置事件参数（多行）
            startParaIndex = paraIndex;
            endParaIndex = paraIndex + lines.size() - 1;
            startOffset = offset;
            endOffset = lines.getLast().length();
        }
        fullContent.insert(index, text);
        setCaretPosition(new Point(endParaIndex, endOffset));
        clearSelectRange();
        if (fireEvent && !changeRunnable.isEmpty()) fireChangeRunnable();
        if (fireEvent && !listeners.isEmpty()) {
            List<Paragraph> tempParagraphs = new ArrayList<>();
            lines.forEach(line -> tempParagraphs.add(createParagraphWithText(line)));
            fireContentChangeEvent(ContentChangeEvent.ChangeType.INSERT,
                    startParaIndex, endParaIndex,
                    tempParagraphs, startOffset, endOffset);
        }
    }

    @Override
    public <T > void insert(int index, NodeSegment<T> segment, boolean fireEvent) {
        historyMemos.execute(new InsertCommand(index, segment, fireEvent, this));
    }

    public <T > void insert(Point caretPoint, NodeSegment<T> segment, boolean fireEvent) {
        // 验证输入点有效性
        if (caretPoint == null) {
            throw new IllegalArgumentException("Caret point cannot be null");
        }
        if (caretPoint.paragraph < 0 || caretPoint.paragraph >= paragraphs.size()) {
            throw new IndexOutOfBoundsException(
                    String.format("Invalid paragraph index %d in caret point", caretPoint.paragraph)
            );
        }
        Paragraph targetPara = paragraphs.get(caretPoint.paragraph);
        if (caretPoint.offset < 0 || caretPoint.offset > targetPara.getLength()) {
            System.out.println(caretPoint.offset);
            System.out.println(targetPara.getLength());
            throw new IndexOutOfBoundsException(
                    String.format("Invalid offset %d in paragraph %d", caretPoint.offset, caretPoint.paragraph)
            );
        }

        // 计算全局插入位置
        int globalIndex = getGlobalPosition(caretPoint);

        // 调用现有的insert方法
        insert(globalIndex, segment, fireEvent);
    }

    <T > void insertAction(int index, NodeSegment<T> segment, boolean fireEvent) {
        Objects.requireNonNull(segment, "Segment cannot be null");
        if (index < 0 || index > length()) throw new IndexOutOfBoundsException("Index out of bounds");

        Point position = locatePosition(index);
        Paragraph targetParagraph = paragraphs.get(position.paragraph);
        targetParagraph.insertSegment(position.offset, segment);

        int endOffset = position.offset + segment.getLength();
        fullContent.insert(index, segment.getInfo());
        setCaretPosition(new Point(position.paragraph, endOffset));
        clearSelectRange();
        if (fireEvent && !changeRunnable.isEmpty()) fireChangeRunnable();
        if (fireEvent && !listeners.isEmpty()) {
            fireContentChangeEvent(ContentChangeEvent.ChangeType.INSERT,
                    position.paragraph, position.paragraph,
                    List.of(createParagraphWithText(segment.getInfo())),
                    position.offset, endOffset);
        }
    }

    @Override
    public void delete(int start, int end, boolean notify) {
        if (start < 0 || end > length() || start > end) {
            throw new IndexOutOfBoundsException(
                    String.format("Invalid delete Range [%d, %d] in content of length %d", start, end, length())
            );
        }
        historyMemos.execute(new DeleteCommand(start, end, getCaretPosition(), notify, this));
    }

    public void deleteSelect(boolean notify) {
        if (start != null && end != null)
            delete(start, end, notify);
    }

    public void delete(Point startPoint, Point endPoint, boolean notify) {
        // 验证输入点有效性
        if (startPoint == null || endPoint == null) {
            throw new IllegalArgumentException("Start and end points cannot be null");
        }
        if (startPoint.paragraph < 0 || startPoint.paragraph >= paragraphs.size() ||
                endPoint.paragraph < 0 || endPoint.paragraph >= paragraphs.size()) {
            throw new IndexOutOfBoundsException("Invalid paragraph index in points");
        }

        // 计算全局位置
        int startIndex = getGlobalPosition(startPoint);
        int endIndex = getGlobalPosition(endPoint);

        // 确保开始位置小于结束位置
        if (startIndex > endIndex) {
            int temp = startIndex;
            startIndex = endIndex;
            endIndex = temp;
        }

        // 调用现有的delete方法
        delete(startIndex, endIndex, notify);
        clearSelectRange();
    }

    List<Paragraph> deleteAction(int start, int end, boolean notify) {
        if (start == end) return List.of();
        Point startLoc = locatePosition(start);
        int startParaIndex = startLoc.paragraph;
        int startOffset = startLoc.offset;

        Point endLoc = locatePosition(end);
        int endParaIndex = endLoc.paragraph;
        int endOffset = endLoc.offset;
        List<Paragraph> deleteParagraphs = getParagraphs(startParaIndex, endParaIndex, startOffset, endOffset);

        if (startParaIndex == endParaIndex) {
            Paragraph para = paragraphs.get(startParaIndex);
            para.remove(startOffset, endOffset);
        } else {
            /*删除开头与结尾*/
            Paragraph startPara = paragraphs.get(startParaIndex);
            startPara.remove(startOffset, startPara.getLength());

            Paragraph endPara = paragraphs.get(endParaIndex);
            endPara.remove(0, endOffset);
            /*删除中间段落*/
            if (endParaIndex > startParaIndex + 1) {
                paragraphs.subList(startParaIndex + 1, endParaIndex).clear();
            }
            /*合并结尾内容*/
            if (startParaIndex < paragraphs.size() - 1) {
                Paragraph nextPara = paragraphs.get(startParaIndex + 1);
                startPara.insertParagraph(startPara.getLength(), nextPara);
                paragraphs.remove(startParaIndex + 1);
            }
        }
        ensureAtLeastOneParagraph();
        fullContent.delete(start, end);
        setCaretPosition(new Point(startParaIndex, startOffset));
        clearSelectRange();
        if (notify && !changeRunnable.isEmpty()) fireChangeRunnable();
        if (notify && !listeners.isEmpty()) {
            fireContentChangeEvent(ContentChangeEvent.ChangeType.DELETE,
                    startParaIndex, endParaIndex,
                    deleteParagraphs, startOffset, endOffset);
        }
        return deleteParagraphs;
    }

    @Override
    public <T > void replace(int start, int end, NodeSegment<T> segment, boolean fireEvent) {
        replaceAction(start, end, segment, fireEvent);
    }

    public <T > void replace(Point startPoint, Point endPoint, NodeSegment<T> segment, boolean fireEvent) {
        // 验证输入点有效性
        if (startPoint == null || endPoint == null) {
            throw new IllegalArgumentException("Start and end points cannot be null");
        }
        if (startPoint.paragraph < 0 || startPoint.paragraph >= paragraphs.size() ||
                endPoint.paragraph < 0 || endPoint.paragraph >= paragraphs.size()) {
            throw new IndexOutOfBoundsException("Invalid paragraph index in points");
        }

        // 计算全局位置
        int startIndex = getGlobalPosition(startPoint);
        int endIndex = getGlobalPosition(endPoint);

        // 确保开始位置小于结束位置
        if (startIndex > endIndex) {
            int temp = startIndex;
            startIndex = endIndex;
            endIndex = temp;
        }

        // 调用现有的replace方法
        replace(startIndex, endIndex, segment, fireEvent);
    }

    <T > void replaceAction(int start, int end, NodeSegment<T> segment, boolean fireEvent) {
        Objects.requireNonNull(segment, "Segment cannot be null");
        if (start < 0 || end > length() || start > end) throw new IndexOutOfBoundsException("Invalid Range");
        List<Paragraph> deleteParagraph = null;
        if (fireEvent && !listeners.isEmpty()) {
            Point startLoc = locatePosition(start);
            Point endLoc = locatePosition(end);
            deleteParagraph = getParagraphs(startLoc.paragraph, endLoc.paragraph, startLoc.offset, endLoc.offset);
        }
        delete(start, end, false);
        insert(start, segment, false);
        clearSelectRange();
        if (fireEvent && !changeRunnable.isEmpty()) fireChangeRunnable();
        if (fireEvent && !listeners.isEmpty()) {
            Point position = locatePosition(start);
            fireContentChangeEvent(ContentChangeEvent.ChangeType.REPLACE,
                    position.paragraph, position.paragraph,
                    deleteParagraph,
                    position.offset, position.offset + segment.getLength());
        }
    }

    @Override
    public void replace(int start, int end, String text, boolean fireEvent) {
        Objects.requireNonNull(text, "Text cannot be null");
        if (start < 0 || end > length() || start > end) throw new IndexOutOfBoundsException("Invalid Range");
        text = Content.filterInput(text, false, false);
        replaceAction(start, end, text, fireEvent);
    }
    public void replace(Point start, Point end, String text, boolean fireEvent){
        Objects.requireNonNull(text, "Text cannot be null");
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end points cannot be null");
        }
        if (start.paragraph < 0 || start.paragraph >= paragraphs.size() ||
                end.paragraph < 0 || end.paragraph >= paragraphs.size()) {
            throw new IndexOutOfBoundsException("Invalid paragraph index in points");
        }

        // 计算全局位置
        int startIndex = getGlobalPosition(start);
        int endIndex = getGlobalPosition(end);

        // 确保开始位置小于结束位置
        if (startIndex > endIndex) {
            int temp = startIndex;
            startIndex = endIndex;
            endIndex = temp;
        }

        // 调用现有的replace方法
        replace(startIndex, endIndex, text, fireEvent);
    }

    void replaceAction(int start, int end, String text, boolean fireEvent) {
        List<Paragraph> deleteParagraph = null;
        if (fireEvent && !listeners.isEmpty()) {
            Point startLoc = locatePosition(start);
            Point endLoc = locatePosition(end);
            deleteParagraph = getParagraphs(startLoc.paragraph, endLoc.paragraph, startLoc.offset, endLoc.offset);
        }

        if (text.isEmpty()) return;
        delete(start, end, false);
        insert(start, text, false);
        clearSelectRange();
        if (fireEvent && !changeRunnable.isEmpty()) fireChangeRunnable();
        if (fireEvent && !listeners.isEmpty()) {
            Point position = locatePosition(start);
            fireContentChangeEvent(ContentChangeEvent.ChangeType.REPLACE,
                    position.paragraph, position.paragraph,
                    new TextSegment(text), deleteParagraph,
                    position.offset, position.offset + text.length());
        }
    }

    void insertParagraphs(int index, List<Paragraph> paragraphs, boolean fireEvent) {
        if (index < 0 || index > length()) throw new IndexOutOfBoundsException("Invalid index index");
        if (paragraphs.isEmpty()) return;
        Point point = locatePosition(index);
        int lastOffset = paragraphs.getLast().getLength();
        if (paragraphs.size() == 1) {
            this.paragraphs.get(point.paragraph).insertParagraph(point.offset, paragraphs.getFirst());
            lastOffset = point.offset + paragraphs.getFirst().getLength();
        } else if (paragraphs.size() == 2) {
            Paragraph p1 = this.paragraphs.get(point.paragraph);
            Paragraph temp = p1.getParagraph(point.offset, p1.getLength());
            p1.remove(point.offset, p1.getLength());
            p1.insertParagraph(point.offset, paragraphs.getFirst());
            temp.insertParagraph(0, paragraphs.getLast());
            this.paragraphs.add(point.paragraph + 1, temp);
        } else {
            Paragraph p1 = this.paragraphs.get(point.paragraph);
            Paragraph temp = p1.getParagraph(point.offset, p1.getLength());
            p1.remove(point.offset, p1.getLength());
            p1.insertParagraph(point.offset, paragraphs.getFirst());
            temp.insertParagraph(0, paragraphs.getLast());
            this.paragraphs.addAll(point.paragraph + 1, paragraphs.subList(1, paragraphs.size() - 1));
            this.paragraphs.add(point.paragraph + paragraphs.size() - 1, temp);
        }
        fullContent.insert(index, formatParagraphs(paragraphs));
        setCaretPosition(new Point(point.paragraph + paragraphs.size() - 1, lastOffset));
        clearSelectRange();
        if (fireEvent && !changeRunnable.isEmpty()) fireChangeRunnable();
        if (fireEvent && !listeners.isEmpty())
            fireContentChangeEvent(ContentChangeEvent.ChangeType.INSERT,
                    point.paragraph, point.paragraph + paragraphs.size() - 1, paragraphs, point.offset, lastOffset);
    }

    public void undo() {
        historyMemos.undo();
    }

    public void redo() {
        historyMemos.redo();
    }

    public int linesNum() {
        return paragraphs.size();
    }

    public Paragraph getParagraph(int index) {
        return paragraphs.get(index);
    }

    @Override
    public int length() {
        return fullContent.length();
    }

    // 新增辅助方法：确保至少有一个段落
    private void ensureAtLeastOneParagraph() {
        if (paragraphs.isEmpty()) {
            paragraphs.add(new Paragraph());
        }
    }

// ================== 辅助方法 ==================

    // 新增辅助方法：创建包含文本的段落
    private Paragraph createParagraphWithText(String text) {
        Paragraph p = new Paragraph();
        if (!text.isEmpty()) {
            p.appendString(text);
        }
        return p;
    }

    /**
     * 将文本分割为段落列表
     */
    private List<String> splitTextIntoLines(String text) {
        List<String> lines = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        text = Content.filterInput(text, false, false);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                lines.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }

        // 添加最后一行（即使为空）
        lines.add(sb.toString());

        return lines;
    }

    /*
      定位文本位置对应的段落和偏移量

      @return [段落索引, 段落内偏移]
     */

    /**
     * 定位文本位置对应的段落和偏移量
     *
     * @return [段落索引, 段落内偏移]
     */
    public Point locatePosition(int pos) {
        if (pos < 0) throw new IndexOutOfBoundsException("Position " + pos + " is out of bounds");
        int currentPos = 0;
        for (int i = 0; i < paragraphs.size(); i++) {
            Paragraph para = paragraphs.get(i);
            int paraLength = para.getLength();
            int endPos = currentPos + paraLength; // 当前段落结束位置

            // 检查位置是否在当前段落范围内（包含段落末尾位置）
            if (pos <= endPos) {
                return new Point(i, pos - currentPos);
            }

            // 更新当前位置：段落内容结束 + 换行符（如果不是最后段落）
            currentPos = endPos + 1; // +1 代表换行符位置
        }

        // 处理文档末尾位置（最后段落的结束位置）
        if (!paragraphs.isEmpty() && pos == currentPos) {
            Paragraph lastPara = paragraphs.getLast();
            return new Point(paragraphs.size() - 1, lastPara.getLength());
        }

        throw new IndexOutOfBoundsException("Position " + pos + " is out of bounds");
    }

    private List<Paragraph> getParagraphs(int startParagraph, int endParagraph, int startOff, int endOff) {
        List<Paragraph> result = new ArrayList<>();
        if (startParagraph == endParagraph) result.add(paragraphs.get(startParagraph).getParagraph(startOff, endOff));
        else {
            Paragraph start = paragraphs.get(startParagraph);
            result.add(start.getParagraph(startOff, start.getLength()));
            for (int i = startParagraph + 1; i < endParagraph; i++)
                result.add(paragraphs.get(i).clone());
            Paragraph end = paragraphs.get(endParagraph);
            result.add(end.getParagraph(0, endOff));

        }
        return result;
    }

    @Override
    public String toString() {
        return fullContent.toString();
    }

    // 新增监听器管理方法
    public void addContentChangeListener(ContentChangeListener listener) {
        listeners.add(listener);
    }

    public void removeContentChangeListener(ContentChangeListener listener) {listeners.remove(listener);
    }

    public void addChangeRunnable(Runnable runnable) {
        changeRunnable.add(runnable);
    }

    public void removeChangeRunnable(Runnable runnable) {
        changeRunnable.remove(runnable);
    }

    public void addCaretChangeListener(CaretChangeListener listener) {
        caretChangeListeners.add(listener);
    }

    public void removeCaretChangeListener(CaretChangeListener listener) {
        caretChangeListeners.remove(listener);
    }

    public void addSelectRangeChangeListener(SelectRangeChangeListener listener) {
        selectRangeChangeListeners.add(listener);
    }

    public void removeSelectRangeChangeListener(SelectRangeChangeListener listener) {
        selectRangeChangeListeners.remove(listener);
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    public void setSelectRange(Point start, Point end) {
        if (start != null && (start.paragraph < 0 || start.paragraph >= paragraphs.size() || start.offset < 0 || start.offset > paragraphs.get(start.paragraph).getLength()))
            throw new IndexOutOfBoundsException("Invalid start position");
        if (end != null && (end.paragraph < 0 || end.paragraph >= paragraphs.size() || end.offset < 0 || end.offset > paragraphs.get(end.paragraph).getLength()))
            throw new IndexOutOfBoundsException("Invalid end position");
        Point oldStart = this.start;
        Point oldEnd = this.end;
        this.start = start;
        this.end = end;
        fireSelectRangeChangeEvent(oldStart, oldEnd, start, end);
    }

    public void clearSelectRange() {
        setSelectRange(null, null);
    }

    public Point getCaretPosition() {
        return caretPosition;
    }

    public void setCaretPosition(Point caretPosition) {
        Objects.requireNonNull(caretPosition, "Caret position cannot be null");
        if (caretPosition.paragraph < 0 || caretPosition.paragraph >= paragraphs.size()) {
            throw new IndexOutOfBoundsException("Invalid paragraph index " + caretPosition.paragraph);
        }
        if (caretPosition.offset < 0 || caretPosition.offset > paragraphs.get(caretPosition.paragraph).getLength()) {
            throw new IndexOutOfBoundsException("Invalid offset " + caretPosition.offset);
        }
        if (Objects.equals(caretPosition, this.caretPosition)) return;
        Point old = this.caretPosition;
        this.caretPosition = caretPosition;
        fireCaretChangeEvent(old, caretPosition);
    }

    private int getGlobalPosition(Point point) {
        int pos = 0;
        for (int i = 0; i < point.paragraph; i++) {
            pos += paragraphs.get(i).getLength();
            if (i < paragraphs.size() - 1) {
                pos++;
            }
        }
        return pos + point.offset;
    }

    public void caretNext(int num) {
        if (num <= 0) throw new IllegalArgumentException("Number of lines to move must be positive");

        int currentGlobal = getGlobalPosition(caretPosition);
        int newGlobal = Math.min(currentGlobal + num, length());
        setCaretPosition(locatePosition(newGlobal));
        clearSelectRange();
    }

    public void caretPrevious(int num) {
        if (num <= 0) throw new IllegalArgumentException("Number of lines to move must be positive");

        int currentGlobal = getGlobalPosition(caretPosition);
        int newGlobal = Math.max(currentGlobal - num, 0);
        setCaretPosition(locatePosition(newGlobal));
        clearSelectRange();
    }

    public void caretUp(int num) {
        if (num <= 0) throw new IllegalArgumentException("Number of lines to move must be positive");

        int currentPara = caretPosition.paragraph;
        int currentOffset = caretPosition.offset;

        // 计算目标行（向上移动num行）
        int targetPara = Math.max(0, currentPara - num);

        // 获取目标行的段落对象
        Paragraph targetParagraph = paragraphs.get(targetPara);

        // 计算目标行中的偏移量：
        // 1. 如果目标行长度 >= 当前偏移量，保持原偏移量
        // 2. 如果目标行更短，将光标放在行末
        int targetOffset = Math.min(currentOffset, targetParagraph.getLength());

        // 设置新光标位置
        setCaretPosition(new Point(targetPara, targetOffset));
        clearSelectRange();
    }

    public void caretDown(int num) {
        if (num <= 0) throw new IllegalArgumentException("Number of lines to move must be positive");

        int currentPara = caretPosition.paragraph;
        int currentOffset = caretPosition.offset;

        // 计算目标行（向下移动num行）
        int targetPara = Math.min(paragraphs.size() - 1, currentPara + num);

        // 获取目标行的段落对象
        Paragraph targetParagraph = paragraphs.get(targetPara);

        // 计算目标行中的偏移量：
        // 1. 如果目标行长度 >= 当前偏移量，保持原偏移量
        // 2. 如果目标行更短，将光标放在行末
        int targetOffset = Math.min(currentOffset, targetParagraph.getLength());

        // 设置新光标位置
        setCaretPosition(new Point(targetPara, targetOffset));
        clearSelectRange();
    }

    public void caretStart() {
        setCaretPosition(new Point(getCaretPosition().paragraph, 0));
        clearSelectRange();
    }

    public void caretEnd() {
        setCaretPosition(new Point(getCaretPosition().paragraph, paragraphs.get(getCaretPosition().paragraph).getLength()));
        clearSelectRange();
    }

    private void fireCaretChangeEvent(Point oldPoint, Point newPoint) {
        if (caretChangeListeners.isEmpty()) return;
        CaretChangeEvent event = new CaretChangeEvent(this, oldPoint, newPoint);
        caretChangeListeners.forEach(listener -> listener.caretChanged(event));
    }

    /**
     * 触发内容变更事件
     */
    private void fireContentChangeEvent(ContentChangeEvent.ChangeType changeType,
                                        int startParaIndex, int endParaIndex,List<Paragraph> changedParagraphs, int startOffset, int endOffset) {
        ContentChangeEvent event = new ContentChangeEvent(
                this, changeType,
                startParaIndex, endParaIndex, changedParagraphs, startOffset, endOffset
        );

        for (ContentChangeListener listener : listeners) {
            listener.contentChanged(event);
        }
    }
    private void fireContentChangeEvent(ContentChangeEvent.ChangeType changeType,
                                        int startParaIndex, int endParaIndex,Segment<?> changedSegment,List<Paragraph> changedParagraphs, int startOffset, int endOffset) {
        ContentChangeEvent event = new ContentChangeEvent(
                this, changeType,
                startParaIndex, endParaIndex,changedSegment, changedParagraphs, startOffset, endOffset
        );

        for (ContentChangeListener listener : listeners) {
            listener.contentChanged(event);
        }
    }

    private void fireChangeRunnable() {
        changeRunnable.forEach(Runnable::run);
    }

    private void fireSelectRangeChangeEvent(Point oldStart, Point oldEnd, Point newStart, Point newEnd) {
        if (selectRangeChangeListeners.isEmpty()) return;
        SelectRangeChangeEvent event = new SelectRangeChangeEvent(this, oldStart, oldEnd, newStart, newEnd);
        selectRangeChangeListeners.forEach(listener -> listener.selectRangeChanged(event));
    }

    public record Point(int paragraph, int offset,
                        int virtualOffset) implements Serializable, Comparable<Point>, Cloneable {
        public Point(int paragraph, int offset) {
            this(paragraph, offset, 0);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Point(int paragraph1, int offset1, int virtualOffset1))
                return paragraph == paragraph1 && offset == offset1 && virtualOffset == virtualOffset1;
            return false;
        }

//        不比较virtualOffset
        @Override
        public int compareTo(Point o) {
            if (paragraph < o.paragraph) return -1;
            if (paragraph > o.paragraph) return 1;
            return Integer.compare(offset, o.offset);
        }

        @Override
        public Point clone() {
            return new Point(paragraph, offset, virtualOffset);
        }

        public Point MovePrevious(String s) {
            // 空文本直接返回起始点
            if (s == null || s.isEmpty()) {
                return this.clone();
            }

            // 按换行符分割文本（保留末尾空行）
            String[] lines = s.split("\n", -1);
            int lineCount = lines.length;

            // 单行文本（无换行符）
            if (lineCount == 1) {
                return new BDTextAreaContent.Point(
                        paragraph,
                        Math.max(0, offset - s.length()),
                        0
                );
            }
            // 多行文本
            else {
                int lastLineLength = lines[lineCount - 1].length();
                return new BDTextAreaContent.Point(
                        paragraph - lineCount + 1,
                         Math.max(0, offset -lastLineLength),
                        0
                );
            }
        }

        public Point moveNext(String s) {
            // 空文本直接返回起始点
            if (s == null || s.isEmpty()) {
                return this.clone();
            }

            // 按换行符分割文本（保留末尾空行）
            String[] lines = s.split("\n", -1);
            int lineCount = lines.length;

            // 单行文本（无换行符）
            if (lineCount == 1) {
                return new BDTextAreaContent.Point(
                        paragraph,
                        offset + s.length(),
                        0
                );
            }
            // 多行文本
            else {
                int lastLineLength = lines[lineCount - 1].length();
                return new BDTextAreaContent.Point(
                        paragraph + lineCount - 1,
                        lastLineLength,
                        0
                );
            }
        }
    }
    public void saveToFile(String path){
        try {
            File file = new File(path);
            if (file.exists()) file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(fullContent.toString());
            writer.close();
        } catch (IOException e) {
            System.err.println("保存文件失败：" + e);
        }
    }
}
