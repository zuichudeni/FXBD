package com.xx.UI.complex.search.simple.box;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class BDSimpleSearchBox<T> extends BDControl {
    private static final String CSS_CLASS_NAME = "bd-search-box";
    protected final SimpleIntegerProperty searchResultIndex = new SimpleIntegerProperty(-1);
    private final SimpleStringProperty searchText = new SimpleStringProperty();
    private final SimpleBooleanProperty searchCase = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty searchRegex = new SimpleBooleanProperty(false);
    private final SimpleIntegerProperty searchBlockCount = new SimpleIntegerProperty(0);
    private final SimpleObjectProperty<SimpleSearchResult<T>> searchResult = new SimpleObjectProperty<>();
    private final SimpleMapProperty<T, List<SimpleSearchResult<T>>> searchMap = new SimpleMapProperty<>(FXCollections.observableHashMap());
    // 线程池管理
    private final ExecutorService executor;
    BDSimpleSearchPane<T> simpleSearchPane;
    private Future<?> currentTask;

    public BDSimpleSearchBox() {
        getStyleClass().add(CSS_CLASS_NAME);
        // 创建单线程池，线程设置为守护线程
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        mapping.addDisposeEvent(this::dispose)
                .addListener(searchResult, (_, _, nv) -> searchResultIndex.set(nv.index));
    }

    public abstract Map<T, String> getSearchSource();

    void search() {
        // 取消当前正在运行的任务
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }

        String regex = getRegularExpression();
        Map<T, String> source = getSearchSource();

        // 如果没有搜索词或没有源数据，清空结果并返回
        if (regex.isEmpty() || source == null || source.isEmpty()) {
            Platform.runLater(() -> {
                searchMap.clear();
                searchBlockCount.set(0);
            });
            return;
        }

        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger index = new AtomicInteger(0);
        // 创建搜索任务
        Task<Map<T, List<SimpleSearchResult<T>>>> task = new Task<>() {
            @Override
            protected Map<T, List<SimpleSearchResult<T>>> call() {
                // 支持取消
                if (isCancelled()) {
                    return Collections.emptyMap();
                }

                Map<T, List<SimpleSearchResult<T>>> resultMap = new HashMap<>();

                for (Map.Entry<T, String> entry : source.entrySet()) {
                    if (isCancelled()) {
                        return Collections.emptyMap();
                    }

                    String text = entry.getValue();
                    if (text == null || text.isEmpty()) {
                        resultMap.put(entry.getKey(), Collections.emptyList());
                        continue;
                    }

                    try {
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(text);
                        List<SimpleSearchResult<T>> matches = new ArrayList<>();
                        while (matcher.find()) {
                            matches.add(new SimpleSearchResult<T>(entry.getKey(),index.getAndIncrement(), matcher.start(), matcher.end()));
                            count.incrementAndGet();
                        }
                        resultMap.put(entry.getKey(), matches);
                    } catch (PatternSyntaxException e) {
                        // 正则表达式无效，记录日志并忽略该块的匹配
                        System.err.println("Invalid regex: " + regex);
                        resultMap.put(entry.getKey(), Collections.emptyList());
                    }
                }
                return resultMap;
            }
        };

        task.setOnSucceeded(_ -> {
            Map<T, List<SimpleSearchResult<T>>> resultMap = task.getValue();
            Platform.runLater(() -> {
                searchMap.clear();
                searchMap.putAll(resultMap);
                searchBlockCount.set(count.get());
            });
        });

        task.setOnFailed(_ -> {
            // 搜索失败，清空结果
            Platform.runLater(() -> {
                searchMap.clear();
                searchBlockCount.set(0);
            });
            task.getException().printStackTrace();
        });

        // 提交任务到线程池，并记录当前任务
        currentTask = executor.submit(task);
    }

    private String getRegularExpression() {
        String searchText = this.searchText.get();
        if (searchText == null || searchText.isEmpty()) {
            return "";
        }
        if (searchRegex.get()) {
            // 直接使用用户输入的正则表达式
            return searchCase.get() ? searchText : "(?i)" + searchText;
        } else {
            // 转义所有正则特殊字符
            String s = searchText.replaceAll("([\\\\\\[\\]{}()*+?.^$|])", "\\\\$1");
            return searchCase.get() ? s : "(?i)" + s;
        }
    }

    private void dispose() {
        // 取消当前正在运行的任务
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
        // 关闭线程池，不再接受新任务，并尝试中断正在执行的任务
        if (executor != null)
            executor.shutdownNow();
    }

    @Override
    protected BDSkin<? extends BDControl> createDefaultSkin() {
        return new BDSimpleSearchBoxSkin(this);
    }

    public int getSearchBlockCount() {
        return searchBlockCount.get();
    }

    public SimpleIntegerProperty searchBlockCountProperty() {
        return searchBlockCount;
    }

    public int getSearchResultIndex() {
        return searchResultIndex.get();
    }

    public SimpleIntegerProperty searchResultIndexProperty() {
        return searchResultIndex;
    }

    public SimpleSearchResult<T> getSearchResult() {
        return searchResult.get();
    }

    public void setSearchResult(SimpleSearchResult<T> searchResult) {
        this.searchResult.set(searchResult);
    }

    public SimpleObjectProperty<SimpleSearchResult<T>> searchResultProperty() {
        return searchResult;
    }

    public String getSearchText() {
        return searchText.get();
    }

    public void setSearchText(String searchText) {
        this.searchText.set(searchText);
    }

    public SimpleStringProperty searchTextProperty() {
        return searchText;
    }

    public void previousSearchBlock() {
    }

    public void nextSearchBlock() {
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

    public void clearSearch() {
    }

    public void refresh() {
    }

    public SimpleMapProperty<T, List<SimpleSearchResult<T>>> searchMapProperty() {
        return searchMap;
    }

    public Map<T, List<SimpleSearchResult<T>>> getSearchMap() {
        return searchMap.get();
    }

    public record SimpleSearchResult<T>(T t,int index, int startOffset, int endOffset) {
    }
}