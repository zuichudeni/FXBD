package com.xx.UI.complex.textArea.view.dataFormat.analyse;

import com.xx.UI.complex.textArea.content.segment.Paragraph;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 用来存储当前段落的token列表（需高亮的内容）
 **/
public class BDTokenEntryList<T extends Enum<?> & Analyse.BDTextEnum<T>> {
    private final List<BDTokenEntry<T>> tokenEntries = new ArrayList<>();
    private final T undefinedType;
    public BDTokenEntryList(T undefinedType) {
        if (undefinedType == null) throw new IllegalArgumentException("undefinedType cannot be null");
        this.undefinedType = undefinedType;
    }

    public void addTokenEntry(BDTokenEntry<T> tokenEntry) {
        // 直接处理重叠，避免创建临时任务列表
        List<BDTokenEntry<T>> currentEntries = new ArrayList<>(tokenEntries);
        for (BDTokenEntry<T> entry : currentEntries) {
            if (entry.getStart() < tokenEntry.getEnd() && tokenEntry.getStart() < entry.getEnd()) {
                int overlapStart = Math.max(entry.getStart(), tokenEntry.getStart());
                int overlapEnd = Math.min(entry.getEnd(), tokenEntry.getEnd());
                entry.slicing(overlapStart, overlapEnd, this);
            }
        }
        tokenEntries.add(tokenEntry);
        marg();
    }
    public void marg(){
        // 2. 按起始位置排序
    tokenEntries.sort(Comparator.comparingInt(BDTokenEntry::getStart));

    // 3. 合并相邻且类型相同、info 相同的条目
    List<BDTokenEntry<T>> merged = new ArrayList<>();
    for (BDTokenEntry<T> entry : tokenEntries) {
        if (merged.isEmpty()) {
            merged.add(entry);
            continue;
        }
        BDTokenEntry<T> last = merged.getLast();
        if (last.getEnd() == entry.getStart() &&
            last.getType() == entry.getType() &&
            Objects.equals(last.getInfo(), entry.getInfo())) {
            // 合并为新条目（保留前者的 info）
            BDTokenEntry<T> combined = new BDTokenEntry<>(
                last.getStart(), entry.getEnd(), last.getType(), last.getInfo()
            );
            merged.set(merged.size() - 1, combined);
        } else {
            merged.add(entry);
        }
    }
    tokenEntries.clear();
    tokenEntries.addAll(merged);
    }

    public List<List<DataBlock<T, ?>>> checkTokenEntries(Paragraph paragraph) {
        if (tokenEntries.isEmpty()) return List.of();
        sort();
        if (tokenEntries.getFirst().getStart() < 0)
            throw new IllegalStateException("first token start is: %d, paragraph length is: %d".formatted(tokenEntries.getFirst().getStart(), paragraph.getLength()));
        if (tokenEntries.getLast().getEnd() > paragraph.getLength()) {
            tokenEntries.clear();
            return List.of();
        }

        int lastEnd = 0;
        int index = 0;
        final int size = tokenEntries.size();

        // 检查开头间隙
        if (tokenEntries.getFirst().getStart() > 0)
            addToken(0, tokenEntries.getFirst().getStart(), paragraph, undefinedType, null);


        // 检查中间间隙
        while (index < size) {
            BDTokenEntry<T> curr = tokenEntries.get(index);
            if (index > 0) {
                BDTokenEntry<T> prev = tokenEntries.get(index - 1);
                if (prev.getEnd() < curr.getStart())
                    addToken(prev.getEnd(), curr.getStart(), paragraph, undefinedType, null);
            }
            lastEnd = curr.getEnd();
            index++;
        }
        if (lastEnd > paragraph.getLength())
            throw new IllegalStateException("结束的位置为：%d,超过段落长度：%d".formatted(lastEnd, paragraph.getLength()));
        // 检查结尾间隙
        if (lastEnd < paragraph.getLength())
            addToken(lastEnd, paragraph.getLength(), paragraph, undefinedType, null);
        sort();
        if (tokenEntries.getFirst().getStart() != 0)
            throw new IllegalStateException("first token start is: %d, paragraph length is: %d".formatted(tokenEntries.getFirst().getStart(), paragraph.getLength()));
        if (tokenEntries.getLast().getEnd() != paragraph.getLength())
            throw new IllegalStateException("last token end is: %d, paragraph length is: %d".formatted(tokenEntries.getLast().getEnd(), paragraph.getLength()));
        return tokenEntries.stream().map(tokenEntry -> tokenEntry.transferToDataBlock(paragraph)).toList();
    }

    public void removeTokenEntry(BDTokenEntry<T> tokenEntry) {
        tokenEntries.remove(tokenEntry);
    }

    public List<BDTokenEntry<T>> getTokenEntries() {
        return tokenEntries;
    }

    private void addToken(int start, int end, Paragraph paragraph, T type, Object info) {
        if (type == null) throw new IllegalArgumentException("type cannot be null");
        if (end > paragraph.getLength())
            throw new IllegalStateException("结束索引为：%d,段落长度为：%d,结束索引不能超过段落长度".formatted(end, paragraph.getLength()));
        AtomicInteger startOffset = new AtomicInteger(start);
        paragraph
                .getParagraph(start, end)
                .getSegments()
                .forEach(segment ->
                        tokenEntries.add(new BDTokenEntry<>(startOffset.getAndAdd(segment.getLength()), startOffset.get(), type, info)));
    }

    private void sort() {
        tokenEntries.sort(BDTokenEntry::compareTo);
    }

    @Override
    public String toString() {
        return "BDTokenEntryList{" +
                "tokenEntries=" + tokenEntries +
                '}';
    }

    //    只能由BDAnalyse的append调用。
    public boolean addTokenEntry(int offset, Paragraph first, T undefinedType) {
        if (tokenEntries.isEmpty() || first.getLength() == 0) return false;
        sort();
        boolean isAdded = false;
        for (BDTokenEntry<T> tokenEntry : tokenEntries) {
            if (tokenEntry.getStart() < offset && offset < tokenEntry.getEnd()) {
                replaceTokenEntry(tokenEntry, tokenEntry.getStart(), tokenEntry.getEnd() + first.getLength());
                isAdded = true;
                continue;
            }
            if (isAdded || tokenEntry.getStart() >= offset) {
                replaceTokenEntry(tokenEntry, tokenEntry.getStart() + first.getLength(), tokenEntry.getEnd() + first.getLength());
            }
        }
        if (!isAdded) {
            tokenEntries.add(new BDTokenEntry<>(offset, offset + first.getLength(), undefinedType, null));
            sort();
        }
        marg();
        return true;
    }

    public BDTokenEntryList<T> remove(int offset) {
        sort();
        BDTokenEntryList<T> result = new BDTokenEntryList<>(undefinedType);
        List<BDTokenEntry<T>> tempResult = new ArrayList<>();
        BDTokenEntryList<T> temp = new BDTokenEntryList<>(undefinedType);
        for (BDTokenEntry<T> tokenEntry : tokenEntries) {
            if (tokenEntry.getEnd() <= offset)
                temp.addTokenEntry(tokenEntry);
            else if (tokenEntry.getStart() < offset && offset < tokenEntry.getEnd()) {
                temp.addTokenEntry(new BDTokenEntry<>(tokenEntry.getStart(), offset, tokenEntry.getType(), tokenEntry.getInfo()));
                tempResult.add(new BDTokenEntry<>(offset, tokenEntry.getEnd(), tokenEntry.getType(), tokenEntry.getInfo()));
            } else if (tokenEntry.getStart() >= offset)
                tempResult.add(new BDTokenEntry<>(tokenEntry.getStart(), tokenEntry.getEnd(), tokenEntry.getType(), tokenEntry.getInfo()));
        }
        tokenEntries.clear();
        if (!tempResult.isEmpty()) {
            int start = tempResult.getFirst().getStart();
            tempResult.forEach(tokenEntry -> result.addTokenEntry(new BDTokenEntry<>(tokenEntry.getStart() - start, tokenEntry.getEnd() - start, tokenEntry.getType(), tokenEntry.getInfo())));
        }
        tokenEntries.addAll(temp.getTokenEntries());
        return result;
    }

    public void remove(int start, int end) {
        // 计算删除长度
        int deleteLength = end - start;

        // 使用插入排序（更高效）
        tokenEntries.sort(Comparator.comparingInt(BDTokenEntry::getStart));

        // 使用迭代器避免ConcurrentModificationException
        ListIterator<BDTokenEntry<T>> it = tokenEntries.listIterator();
        while (it.hasNext()) {
            BDTokenEntry<T> entry = it.next();
            int entryStart = entry.getStart();
            int entryEnd = entry.getEnd();

            // 1. Token完全在删除范围之后：整体前移
            if (entryStart >= end) {
                it.set(new BDTokenEntry<>(
                        entryStart - deleteLength,
                        entryEnd - deleteLength,
                        entry.getType(),
                        entry.getInfo()
                ));
            }
            // 2. Token完全在删除范围内：直接移除
            else if (entryStart >= start && entryEnd <= end) {
                it.remove();
            }
            // 3. Token横跨删除范围：拆分为前后两部分
            else if (entryStart < start && entryEnd > end) {
                // 前部分（删除范围前）
                BDTokenEntry<T> prefix = new BDTokenEntry<>(
                        entryStart,
                        start,
                        entry.getType(),
                        entry.getInfo()
                );

                // 后部分（删除范围后，需要前移）
                BDTokenEntry<T> suffix = new BDTokenEntry<>(
                        start, // 位置调整：end - deleteLength = start
                        entryEnd - deleteLength,
                        entry.getType(),
                        entry.getInfo()
                );

                it.remove();
                it.add(prefix);
                it.add(suffix);
            }
            // 4. Token部分重叠：截断处理
            else if (entryEnd > end) {
                // 保留后部分并前移
                it.set(new BDTokenEntry<>(
                        start, // 位置调整
                        entryEnd - deleteLength,
                        entry.getType(),
                        entry.getInfo()
                ));
            }
            // 5. Token部分重叠（左侧）：截断前部
            else if (entryEnd > start) {
                it.set(new BDTokenEntry<>(
                        entryStart,
                        start,
                        entry.getType(),
                        entry.getInfo()
                ));
            }
        }
        marg();
    }

    private void replaceTokenEntry(BDTokenEntry<T> tokenEntry, int start, int end) {
        int i = tokenEntries.indexOf(tokenEntry);
        BDTokenEntry<T> entry = new BDTokenEntry<>(start, end, tokenEntry.getType(), tokenEntry.getInfo());
        tokenEntries.set(i, entry);
    }

    public T getType(int offset) {
        for (BDTokenEntry<T> tokenEntry : tokenEntries)
            if (tokenEntry.getStart() <= offset && offset <= tokenEntry.getEnd())
                return tokenEntry.getType();
        return null;
    }
}
