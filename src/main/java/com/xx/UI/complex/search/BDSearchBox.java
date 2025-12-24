package com.xx.UI.complex.search;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class BDSearchBox extends BDControl {
    private static final String CSS_CLASS_NAME = "bd-search-box";
    // 存储当前搜索结果的索引
    protected final SimpleIntegerProperty searchBlockIndex = new SimpleIntegerProperty(-1);
    // 触发刷新的变量
    final SimpleBooleanProperty refresh = new SimpleBooleanProperty(false);
    final SimpleStringProperty regularExpression = new SimpleStringProperty();
    final BDSearchPane searchPane;
    private final SimpleBooleanProperty searchCase = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty searchRegex = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty retract = new SimpleBooleanProperty(true);
    // 存储搜索片段，key为行号，value为该行搜索结果,目的是为了cell渲染
    private final Map<Integer, List<SearchResult>> searchResults = new HashMap<>();
    // 存储搜索结果数
    private final SimpleIntegerProperty searchBlockCount = new SimpleIntegerProperty(0);
    // 存储搜索结果，一个搜索结果包含至少一个搜索片段（是否换行）
    private final List<SearchBlock> searchBlocks = new ArrayList<>();
    private final SimpleStringProperty searchText = new SimpleStringProperty();
    private final SimpleBooleanProperty searchSelected = new SimpleBooleanProperty(false);
    private final ScheduledExecutorService scheduler;
    // 用于管理搜索任务
    private final AtomicReference<Task<Void>> currentSearchTask = new AtomicReference<>();
    private ScheduledFuture<?> scheduledSearch;
    // 添加一个标记，表示是否已关闭
    private volatile boolean disposed = false;
    // 任务状态锁
    private final Object taskLock = new Object();

    public BDSearchBox(BDSearchPane searchPane) {
        getStyleClass().add(CSS_CLASS_NAME);
        this.searchPane = searchPane;
        mapping.addDisposeEvent(this::dispose);
        searchPane.getMapping().addChildren(getMapping());

        // 创建单线程调度器，设置线程为守护线程
        this.scheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r, "BDSearchBox-Scheduler");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void refresh() {
        this.refresh.set(!this.refresh.get());
    }

    public boolean isSearchCase() {
        return searchCase.get();
    }

    public void setSearchCase(boolean searchCase) {
        this.searchCase.set(searchCase);
    }

    public SimpleBooleanProperty searchCaseProperty() {
        return searchCase;
    }

    public boolean isSearchRegex() {
        return searchRegex.get();
    }

    public void setSearchRegex(boolean searchRegex) {
        this.searchRegex.set(searchRegex);
    }

    public SimpleBooleanProperty searchRegexProperty() {
        return searchRegex;
    }

    public boolean isRetract() {
        return retract.get();
    }

    public void setRetract(boolean retract) {
        this.retract.set(retract);
    }

    public SimpleBooleanProperty retractProperty() {
        return retract;
    }

    public String getRegularExpression() {
        return regularExpression.get();
    }

    public void setRegularExpression(String regularExpression) {
        this.regularExpression.set(regularExpression);
    }

    public ReadOnlyStringProperty regularExpressionProperty() {
        return regularExpression;
    }

    public String getSearchText() {
        return searchText.get();
    }

    public void setSearchText(String searchText) {
        this.searchText.set(searchText);
    }

    public boolean isSearchSelected() {
        return searchSelected.get();
    }

    public void setSearchSelected(boolean searchSelected) {
        this.searchSelected.set(searchSelected);
    }

    public SimpleBooleanProperty searchSelectedProperty() {
        return searchSelected;
    }

    public SimpleStringProperty searchTextProperty() {
        return searchText;
    }

    public Map<Integer, List<SearchResult>> getSearchResults() {
        return new HashMap<>(searchResults); // 返回副本以保证线程安全
    }

    public List<SearchBlock> getSearchBlocks() {
        return new ArrayList<>(searchBlocks); // 返回副本以保证线程安全
    }

    public int getSearchBlockCount() {
        return searchBlockCount.get();
    }

    public ReadOnlyIntegerProperty searchBlockCountProperty() {
        return searchBlockCount;
    }

    public int getSearchBlockIndex() {
        return searchBlockIndex.get();
    }

    public ReadOnlyIntegerProperty searchBlockIndexProperty() {
        return searchBlockIndex;
    }

    public void previousSearchBlock() {
        int index = searchBlockIndex.get();
        if (index > 0) {
            searchBlockIndex.set(index - 1);
        } else if (searchBlocks.size() > 0) {
            searchBlockIndex.set(searchBlocks.size() - 1);
        }
    }

    public void nextSearchBlock() {
        int index = searchBlockIndex.get();
        if (index < searchBlocks.size() - 1) {
            searchBlockIndex.set(index + 1);
        } else if (!searchBlocks.isEmpty()) {
            searchBlockIndex.set(0);
        }
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDSearchBoxSkin(this);
    }

    public void triggerSearch(String text, long delayMillis) {
        // 检查是否已关闭
        if (disposed) {
            System.err.println("BDSearchBox is disposed, cannot trigger search");
            return;
        }

        synchronized (taskLock) {
            // 取消之前计划的搜索
            cancelScheduledSearch();

            // 取消当前正在执行的任务
            cancelCurrentSearchTask();

            // 安排新的搜索任务
            scheduledSearch = scheduler.schedule(() -> {
                if (!disposed) {
                    Platform.runLater(() -> search(text));
                }
            }, delayMillis, TimeUnit.MILLISECONDS);
        }
    }

    // 取消已计划的搜索
    private void cancelScheduledSearch() {
        if (scheduledSearch != null && !scheduledSearch.isDone()) {
            scheduledSearch.cancel(false);
        }
    }

    // 取消当前搜索任务
    private void cancelCurrentSearchTask() {
        Task<Void> task = currentSearchTask.get();
        if (task != null && !task.isDone()) {
            task.cancel(true);
            // 等待任务取消完成（有限等待）
            try {
                task.get(50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                // 忽略异常，任务可能仍在进行中
            }
        }
    }

    // 为了方便使用，添加一个立即搜索的方法
    public void triggerSearchImmediate(String text) {
        triggerSearch(text, 0);
    }

    private void search(String text) {
        // 检查是否已关闭
        if (disposed) {
            return;
        }

        // 在UI线程中清空之前的搜索结果
        clearSearchResultsOnUI();

        String searchPattern = regularExpression.get();
        if (searchPattern == null || searchPattern.isEmpty() || text == null || text.isEmpty()) {
            resetSearchState();
            return;
        }

        // 当搜索类型为搜索选中内容时，需要获取选中内容的起始行和偏移量
        final int startParagraph = isSearchSelected() ?
            searchPane.bdSearchResource.getSelectedStartParagraph() : 0;
        final int startOffset = isSearchSelected() ?
            searchPane.bdSearchResource.getSelectedOffset() : 0;

        // 创建并执行后台任务
        createAndExecuteSearchTask(text, searchPattern, startParagraph, startOffset);
    }

    private void clearSearchResultsOnUI() {
        searchResults.clear();
        searchBlocks.clear();
    }

    private void resetSearchState() {
        searchBlockIndex.set(-1);
        searchBlockCount.set(0);
    }

    private void createAndExecuteSearchTask(String text, String searchPattern,
                                           int startParagraph, int startOffset) {
        Task<Void> searchTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    // 预计算行信息，避免在循环中重复计算
                    List<Integer> lineStarts = new ArrayList<>();
                    List<Integer> lineLengths = new ArrayList<>();
                    calculateLineInfo(text, lineStarts, lineLengths);

                    int flags = isSearchCase() ? 0 : Pattern.CASE_INSENSITIVE;
                    Pattern pattern = Pattern.compile(searchPattern, flags);
                    Matcher matcher = pattern.matcher(text);

                    // 在后台线程中收集结果
                    Map<Integer, List<SearchResult>> resultsByLine = new HashMap<>();
                    List<SearchBlock> newSearchBlocks = new ArrayList<>();

                    while (matcher.find() && !disposed) {
                        if (isCancelled()) {
                            break;
                        }

                        int globalStart = matcher.start();
                        int globalEnd = matcher.end();

                        // 使用二分查找确定起始行和结束行
                        int startLine = findLineIndex(lineStarts, globalStart);
                        int endLine = findLineIndex(lineStarts, globalEnd - 1);

                        if (startLine == -1 || endLine == -1) {
                            continue; // 行定位失败，跳过这个匹配
                        }

                        processMatch(globalStart, globalEnd, startLine, endLine,
                                   lineStarts, lineLengths, startParagraph,
                                   startOffset, newSearchBlocks, resultsByLine);
                    }

                    // 检查任务状态后再更新UI
                    if (!isCancelled() && !disposed) {
                        updateSearchResultsOnUI(newSearchBlocks, resultsByLine);
                    }

                } catch (PatternSyntaxException _) {

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            private void calculateLineInfo(String text, List<Integer> lineStarts,
                                         List<Integer> lineLengths) {
                lineStarts.add(0);
                int lineStart = 0;
                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) == '\n') {
                        lineLengths.add(i - lineStart);
                        lineStart = i + 1;
                        lineStarts.add(lineStart);
                    }
                }
                // 处理最后一行
                lineLengths.add(text.length() - lineStart);
            }

            private void processMatch(int globalStart, int globalEnd, int startLine,
                                    int endLine, List<Integer> lineStarts,
                                    List<Integer> lineLengths, int startParagraph,
                                    int startOffset, List<SearchBlock> newSearchBlocks,
                                    Map<Integer, List<SearchResult>> resultsByLine) {
                int resultIndex = newSearchBlocks.size();
                SearchBlock searchBlock = new SearchBlock(startLine + startParagraph);
                newSearchBlocks.add(searchBlock);

                // 处理匹配（单行或跨行）
                if (startLine == endLine) {
                    // 单行匹配
                    processSingleLineMatch(globalStart, globalEnd, startLine, lineStarts,
                                         lineLengths, startParagraph, startOffset,
                                         resultIndex, searchBlock, resultsByLine);
                } else {
                    // 跨行匹配
                    processMultiLineMatch(globalStart, globalEnd, startLine, endLine,
                                        lineStarts, lineLengths, startParagraph,
                                        startOffset, resultIndex, searchBlock, resultsByLine);
                }
            }

            private void processSingleLineMatch(int globalStart, int globalEnd,
                                              int line, List<Integer> lineStarts,
                                              List<Integer> lineLengths,
                                              int startParagraph, int startOffset,
                                              int resultIndex, SearchBlock searchBlock,
                                              Map<Integer, List<SearchResult>> resultsByLine) {
                int lineStartPos = lineStarts.get(line);
                int lineStartOffset = globalStart - lineStartPos + (line == 0 ? startOffset : 0);
                int lineEndOffset = globalEnd - lineStartPos + (line == 0 ? startOffset : 0);
                boolean fullLine = (globalStart - lineStartPos == 0 &&
                                  globalEnd - lineStartPos == lineLengths.get(line));

                int displayLine = line + startParagraph;
                SearchResult result = new SearchResult(displayLine, lineStartOffset,
                        lineEndOffset, resultIndex, fullLine);
                resultsByLine.computeIfAbsent(displayLine, _ -> new ArrayList<>()).add(result);
                searchBlock.addResult(result);
            }

            private void processMultiLineMatch(int globalStart, int globalEnd,
                                             int startLine, int endLine,
                                             List<Integer> lineStarts,
                                             List<Integer> lineLengths,
                                             int startParagraph, int startOffset,
                                             int resultIndex, SearchBlock searchBlock,
                                             Map<Integer, List<SearchResult>> resultsByLine) {
                // 第一部分：开始行
                int firstLineStartPos = lineStarts.get(startLine);
                int firstLineStart = globalStart - firstLineStartPos;
                int firstLineEnd = lineLengths.get(startLine);

                int displayStartLine = startLine + startParagraph;
                SearchResult firstResult = new SearchResult(displayStartLine,
                        firstLineStart + (startLine == 0 ? startOffset : 0),
                        firstLineEnd + (startLine == 0 ? startOffset : 0),
                        resultIndex, true);
                resultsByLine.computeIfAbsent(displayStartLine, _ -> new ArrayList<>()).add(firstResult);
                searchBlock.addResult(firstResult);

                // 中间行
                for (int line = startLine + 1; line < endLine; line++) {
                    int displayLine = line + startParagraph;
                    SearchResult midResult = new SearchResult(displayLine, 0,
                            lineLengths.get(line), resultIndex, true);
                    resultsByLine.computeIfAbsent(displayLine, _ -> new ArrayList<>()).add(midResult);
                    searchBlock.addResult(midResult);
                }

                // 最后一行
                int lastLineStartPos = lineStarts.get(endLine);
                int lastLineEnd = globalEnd - lastLineStartPos;

                int displayEndLine = endLine + startParagraph;
                SearchResult lastResult = new SearchResult(displayEndLine, 0,
                        lastLineEnd, resultIndex, false);
                resultsByLine.computeIfAbsent(displayEndLine, _ -> new ArrayList<>()).add(lastResult);
                searchBlock.addResult(lastResult);
            }

            private void updateSearchResultsOnUI(List<SearchBlock> newSearchBlocks,
                                               Map<Integer, List<SearchResult>> resultsByLine) {
                Platform.runLater(() -> {
                    if (!disposed) {
                        searchBlocks.clear();
                        searchBlocks.addAll(newSearchBlocks);

                        searchResults.clear();
                        searchResults.putAll(resultsByLine);

                        searchBlockCount.set(newSearchBlocks.size());
                        searchBlockIndex.set(newSearchBlocks.isEmpty() ? -1 : 0);
                        refresh();
                    }
                });
            }
        };

        // 设置并启动任务
        currentSearchTask.set(searchTask);

        // 创建并启动线程
        Thread searchThread = new Thread(searchTask, "BDSearchBox-Worker");
        searchThread.setDaemon(true);
        searchThread.start();
    }

    // 使用二分查找优化行定位
    private int findLineIndex(List<Integer> lineStarts, int position) {
        int low = 0;
        int high = lineStarts.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = lineStarts.get(mid);

            if (midVal <= position) {
                if (mid == lineStarts.size() - 1 || lineStarts.get(mid + 1) > position) {
                    return mid;
                }
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    public void dispose() {
        // 防止重复调用
        if (disposed) {
            return;
        }
        disposed = true;

        synchronized (taskLock) {
            // 1. 取消计划的搜索任务
            cancelScheduledSearch();

            // 2. 取消当前正在执行的搜索任务
            cancelCurrentSearchTask();

            // 3. 关闭线程池
            shutdownScheduler();
        }

        // 4. 清空数据结构，释放内存
        Platform.runLater(() -> {
            clearSearchResultsOnUI();
            resetSearchState();
        });
    }

    private void shutdownScheduler() {
        if (!scheduler.isShutdown()) {
            try {
                // 先尝试优雅关闭
                scheduler.shutdown();

                // 等待一段时间让任务结束
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    // 如果超时，强制关闭
                    List<Runnable> notExecuted = scheduler.shutdownNow();
                    if (!notExecuted.isEmpty()) {
                        System.out.println("BDSearchBox: " + notExecuted.size() +
                                         " tasks were not executed");
                    }
                }
            } catch (InterruptedException e) {
                // 如果等待过程中被中断，强制关闭
                scheduler.shutdownNow();
                Thread.currentThread().interrupt(); // 恢复中断状态
            }
        }
    }

    public void clearSearch() {
        if (!disposed) {
            Platform.runLater(() -> {
                clearSearchResultsOnUI();
                resetSearchState();
            });
        }
    }

    // 存储搜索片段。原因：搜索结果可能跨越多行，因此需要存储多个搜索片段。
    public record SearchResult(int line, int startOffset, int endOffset, int resultIndex, boolean fullLine) {
    }

    // 存储搜索片段。这才是真正的搜索结果。
    public static class SearchBlock {
        private final List<SearchResult> results = new ArrayList<>();
        private final int startLine;

        public SearchBlock(int startLine) {
            this.startLine = startLine;
        }

        public int getStartLine() {
            return startLine;
        }

        public void addResult(SearchResult result) {
            results.add(result);
        }

        public List<SearchResult> getResults() {
            return Collections.unmodifiableList(results);
        }

        public boolean contains(SearchResult result) {
            return results.contains(result);
        }

        @Override
        public String toString() {
            return "SearchBlock{" +
                    "results=" + results +
                    '}';
        }
    }
}