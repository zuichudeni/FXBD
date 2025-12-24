package com.xx.UI.complex.textArea.view.dataFormat.analyse;

import com.xx.UI.complex.textArea.content.BDTextAreaContent;
import com.xx.UI.complex.textArea.content.segment.Paragraph;
import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class BDAnalyse<T extends Enum<?> & Analyse.BDTextEnum<T>> implements Analyse<T> {
     final Map<Integer, List<DataBlock<T, ?>>> dataBlockCacheMap = new ConcurrentHashMap<>();
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "BDAnalyse-Worker");
        thread.setDaemon(true); // 设置为守护线程
        return thread;
    });
    final Map<Integer, BDTokenEntryList<T>> tokenEntryCacheMap = new ConcurrentHashMap<>();
    private final Object taskLock = new Object();
    private final AtomicReference<TaskContext<T>> currentTaskContext = new AtomicReference<>();

    // 任务上下文，封装任务相关状态
    private static class TaskContext<T extends Enum<?> & Analyse.BDTextEnum<T>> {
        final Future<?> future;
        final AtomicBoolean cancellationFlag;
        final Runnable completionCallback;
        final long creationTime;

        TaskContext(Future<?> future, AtomicBoolean cancellationFlag,
                   Runnable completionCallback) {
            this.future = future;
            this.cancellationFlag = cancellationFlag;
            this.completionCallback = completionCallback;
            this.creationTime = System.currentTimeMillis();
        }
    }

 @Override
    public final List<DataBlock<T, ?>> transform(
            int paragraphIndex,
            final Paragraph paragraph,
            BDTokenEntryList<T> tokenEntryList) {
        Objects.requireNonNull(paragraph);
        List<DataBlock<T, ?>> list = new ArrayList<>();
        if (tokenEntryList == null || tokenEntryList.getTokenEntries().isEmpty()) {
            paragraph.getSegments()
                    .stream()
                    .map(segment -> new DataBlock<>(getUndefinedType(), segment, null))
                    .forEach(list::add);
        } else {
            tokenEntryList.checkTokenEntries(paragraph)
                    .forEach(list::addAll);
        }
        return list;
    }

     @Override
    public final Map<Integer, BDTokenEntryList<T>> transformTokenEntry(String text) {
        Map<Integer, BDTokenEntryList<T>> map = new ConcurrentHashMap<>();
        List<BDToken<T>> tokens = getBDToken(text);

        for (BDToken<T> token : tokens) {
            final int paraIndex = token.getParagraphIndex();
            BDTokenEntryList<T> entryList = map.computeIfAbsent(paraIndex,
                    _ -> new BDTokenEntryList<>(getUndefinedType()));

            List<BDToken.Range<T>> ranges = token.getRanges();
            if (!ranges.isEmpty()) {
                for (BDToken.Range<T> range : ranges) {
                    entryList.addTokenEntry(new BDTokenEntry<>(
                            range.start(),
                            range.end(),
                            range.type(),
                            range.info()
                    ));
                }
            }
        }
        return map;
    }

public boolean isTaskRunning() {
        TaskContext<T> context = currentTaskContext.get();
        return context != null && !context.future.isDone();
    }
  public boolean wasLastTaskCancelled() {
        TaskContext<T> context = currentTaskContext.get();
        return context != null && context.cancellationFlag.get();
    }
 public void setTextAsync(String text, Runnable onComplete) {
        synchronized (taskLock) {
            // 取消当前正在运行的任务
            cancelCurrentTask();

            // 创建新的任务上下文
            AtomicBoolean cancellationFlag = new AtomicBoolean(false);
            TaskContext<T> newContext = new TaskContext<>(
                null, // 将在提交任务后设置
                cancellationFlag,
                onComplete != null ? onComplete : () -> {}
            );

            // 提交新任务
            TaskContext<T> finalNewContext = newContext;
            Future<?> future = executor.submit(() ->
                processTextAsync(text, cancellationFlag, finalNewContext.completionCallback));

            // 更新任务上下文
            newContext = new TaskContext<>(future, cancellationFlag, newContext.completionCallback);
            currentTaskContext.set(newContext);
        }
    }

    private void processTextAsync(String text, AtomicBoolean cancellationFlag,
                                 Runnable completionCallback) {
        // 线程开始执行时设置处理状态
        processing.set(true);

        try {
            // 检查任务是否已被取消
            if (Thread.interrupted() || cancellationFlag.get()) {
                return;
            }

            // 执行文本转换
            Map<Integer, BDTokenEntryList<T>> tempTokenEntryMap = transformTokenEntry(text);

            // 再次检查取消状态
            if (cancellationFlag.get()) {
                return;
            }

            // 安全更新共享状态
            synchronized (taskLock) {
                // 只有在任务未被取消时才更新缓存
                if (!cancellationFlag.get()) {
                    tokenEntryCacheMap.clear();
                    dataBlockCacheMap.clear();
                    tokenEntryCacheMap.putAll(tempTokenEntryMap);
                }
            }

            // 只有在任务成功完成时才回调
            if (!cancellationFlag.get()) {
                // 安全更新UI
                Platform.runLater(() -> {
                    if (!cancellationFlag.get()) {
                        try {
                            completionCallback.run();
                        } catch (Exception e) {
                            // 防止回调异常影响后续处理
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (CancellationException e) {
            // 任务被取消，正常退出
        } catch (Exception e) {
            // 记录异常但不抛出，避免影响后续任务
            e.printStackTrace();
            // 可以考虑在这里执行错误回调
        } finally {
            // 确保处理状态被重置
            processing.set(false);
        }
    }
     private void cancelCurrentTask() {
        TaskContext<T> currentContext = currentTaskContext.get();
        if (currentContext != null && !currentContext.future.isDone()) {
            // 设置取消标志
            currentContext.cancellationFlag.set(true);
            // 尝试中断线程
            currentContext.future.cancel(true);

            // 等待任务取消完成（有限等待）
            try {
                currentContext.future.get(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                // 任务取消可能需要时间，这里忽略超时
            }
        }
    }
    // 检查任务状态
    public boolean isProcessing() {
        return processing.get();
    }
 public void cancelCurrentTaskIfRunning() {
        synchronized (taskLock) {
            cancelCurrentTask();
        }
    }
    public void setProcessing(boolean processing) {
        this.processing.set(processing);
    }

    // 关闭线程池（在不再需要时调用）
    public void shutdown() {
        synchronized (taskLock) {
            cancelCurrentTask();

            // 关闭线程池
            executor.shutdown();
            try {
                // 等待现有任务完成
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    // 再次等待
                    if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                        System.err.println("BDAnalyse executor did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // 强制关闭
    public void shutdownNow() {
        synchronized (taskLock) {
            cancelCurrentTask();
            executor.shutdownNow();
        }
    }


    public Map<Integer, BDTokenEntryList<T>> getTokenEntryCacheMap() {
        return tokenEntryCacheMap;
    }

    public List<DataBlock<T, ?>> getDataBlock(int paragraphIndex, Paragraph paragraph) {
        return dataBlockCacheMap.computeIfAbsent(paragraphIndex,
                _ -> transform(paragraphIndex, paragraph, tokenEntryCacheMap.get(paragraphIndex)));
    }


    void append(int paragraphIndex, int offset, List<Paragraph> paragraphs) {
        if (paragraphs == null || paragraphs.isEmpty()) throw new IllegalArgumentException("变化的段落不能为空");
        T type = getUndefinedType();
        if (tokenEntryCacheMap.containsKey(paragraphIndex)) {
            T type1 = tokenEntryCacheMap.get(paragraphIndex).getType(offset);
            type = type1 == null ? type : type1;
        }
        if (tokenEntryCacheMap.containsKey(paragraphIndex)) {
            BDTokenEntryList<T> entryList = tokenEntryCacheMap.get(paragraphIndex);
            entryList.addTokenEntry(offset, paragraphs.getFirst(), getUndefinedType());
        }
        if (paragraphs.size() > 1) {
            addParagraphIndex(paragraphIndex + 1, paragraphs.size() - 1);
            if (tokenEntryCacheMap.containsKey(paragraphIndex)) {
                BDTokenEntryList<T> entryList = tokenEntryCacheMap.get(paragraphIndex);
                BDTokenEntryList<T> remove = entryList.remove(offset);
                entryList.addTokenEntry(entryList.getTokenEntries().isEmpty() ? 0 : entryList.getTokenEntries().getLast().getEnd(), paragraphs.getFirst(), type);
                remove.addTokenEntry(0, paragraphs.getLast(), type);
                tokenEntryCacheMap.put(paragraphIndex + paragraphs.size() - 1, remove);
            }
            for (int i = 1; i < paragraphs.size(); i++) {
                BDTokenEntryList<T> value = new BDTokenEntryList<>(getUndefinedType());
                int length = paragraphs.get(i).getLength();
                if (length <= 0) continue;
                value.addTokenEntry(new BDTokenEntry<>(0, length, type, null));
                tokenEntryCacheMap.put(paragraphIndex + i, value);
            }
        }
        dataBlockCacheMap.clear();
    }

    private void addParagraphIndex(int startParagraphIndex, int line) {
        if (line < 1) return;
        // 获取需要移动的键（已排序）
        List<Integer> keys = new ArrayList<>(tokenEntryCacheMap.keySet());
        Collections.sort(keys);

        // 从大到小处理键值，避免覆盖
        for (int i = keys.size() - 1; i >= 0; i--) {
            int key = keys.get(i);
            if (key >= startParagraphIndex) {
                // 直接修改键值（避免创建新条目）
                BDTokenEntryList<T> value = tokenEntryCacheMap.remove(key);
                if (value == null) continue;
                tokenEntryCacheMap.put(key + line, value);
            }
        }
    }

    void delete(int startParagraphIndex, int startOffset, int endParagraphIndex, int endOffset, List<Paragraph> paragraphs) {
        if (paragraphs == null || paragraphs.isEmpty()) throw new IllegalArgumentException("变化的段落不能为空");
        if (paragraphs.size() == 1) {
            if (tokenEntryCacheMap.containsKey(startParagraphIndex))
                tokenEntryCacheMap.get(startParagraphIndex).remove(startOffset, endOffset);
        } else {
            if (tokenEntryCacheMap.containsKey(startParagraphIndex)) {
                BDTokenEntryList<T> startEntryList = tokenEntryCacheMap.get(startParagraphIndex);
                startEntryList.remove(startOffset);
                if (tokenEntryCacheMap.containsKey(endParagraphIndex)) {
                    tokenEntryCacheMap.get(endParagraphIndex).remove(0, endOffset);
                    tokenEntryCacheMap.get(endParagraphIndex).getTokenEntries().forEach(tokenEntry -> startEntryList.addTokenEntry(new BDTokenEntry<>(startOffset + tokenEntry.getStart(), startOffset + tokenEntry.getEnd(), tokenEntry.getType(), tokenEntry.getInfo())));
                }
            }
            for (int i = 1; i < paragraphs.size(); i++)
                tokenEntryCacheMap.remove(startParagraphIndex + i);
            moveUpParagraphIndex(startParagraphIndex + 1, paragraphs.size() - 1);
        }
        dataBlockCacheMap.clear();
    }

    private void moveUpParagraphIndex(int startParagraphIndex, int line) {
        if (line <= 0) return; // 无效移动行数直接返回

        // 获取需要移动的键（已排序）
        List<Integer> keys = new ArrayList<>(tokenEntryCacheMap.keySet());
        Collections.sort(keys); // 升序排列

        // 从小到大处理键值，避免覆盖
        for (int key : keys) {
            if (key >= startParagraphIndex) {
                // 计算新键值（上移）
                int newKey = key - line;

                // 检查新键值有效性
                if (newKey < 0) {
                    throw new IllegalArgumentException("移动导致段落索引变为负数: " + newKey);
                }

                // 先移除旧键值对
                BDTokenEntryList<T> value = tokenEntryCacheMap.remove(key);

                // 检查目标位置是否被占用（仅限小于startParagraphIndex的键）
                if (tokenEntryCacheMap.containsKey(newKey) && newKey < startParagraphIndex) {
                    throw new IllegalStateException("目标位置已被未移动段落占用: " + newKey);
                }

                // 放入新位置
                tokenEntryCacheMap.put(newKey, value);
            }
        }
    }

     public DataBlockEntry<T> getDataBlockEntry(BDTextAreaContent.Point point) {
        if (dataBlockCacheMap.containsKey(point.paragraph())) {
            List<DataBlock<T, ?>> dataBlocks = dataBlockCacheMap.get(point.paragraph());
            int tempLen = 0;

            for (int i = 0; i < dataBlocks.size(); i++) {
                DataBlock<T, ?> dataBlock = dataBlocks.get(i);
                int blockLength = dataBlock.getSegment().getLength();

                if (tempLen == point.offset()) {
                    return new DataBlockEntry<>(i > 0 ? dataBlocks.get(i - 1) : null, dataBlock);
                }

                if (tempLen + blockLength == point.offset()) {
                    return new DataBlockEntry<>(dataBlock,
                            (i < dataBlocks.size() - 1) ? dataBlocks.get(i + 1) : null);
                }

                if (tempLen + blockLength > point.offset()) {
                    return new DataBlockEntry<>(dataBlock, dataBlock);
                }

                tempLen += blockLength;
            }
        }
        return null;
    }


    public record DataBlockEntry<T extends Enum<?> & Analyse.BDTextEnum<T>>(DataBlock<T, ?> left,
                                                                            DataBlock<T, ?> right) {
    }
}
