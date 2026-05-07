package com.xx.UI.complex.search.simple.box;

import com.xx.UI.ui.BDControl;
import com.xx.UI.ui.BDSkin;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class BDSimpleSearchBox<T> extends BDControl {
    private static final String CSS_CLASS_NAME = "bd-search-box";
    protected final SimpleIntegerProperty searchResultIndex = new SimpleIntegerProperty(0);
    final SimpleBooleanProperty show = new SimpleBooleanProperty();
    private final SimpleStringProperty searchText = new SimpleStringProperty();
    private final SimpleBooleanProperty searchCase = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty searchRegex = new SimpleBooleanProperty(false);
    private final SimpleIntegerProperty searchBlockCount = new SimpleIntegerProperty(0);
    private final SimpleObjectProperty<SimpleSearchResult<T>> searchResult = new SimpleObjectProperty<>();
    private final Map<T, List<SimpleSearchResult<T>>> searchMap = new LinkedHashMap<>();
    //    这个refresh是为了刷新treeview的。
    private final SimpleBooleanProperty refresh = new SimpleBooleanProperty();
    //    刷新搜索行为
    private final SimpleBooleanProperty refreshSearch = new SimpleBooleanProperty();
    private final List<SimpleSearchResult<T>> searchList = new ArrayList<>();
    // 线程池管理
    private final ExecutorService executor;
    private final AtomicReference<Task<?>> currentTaskRef = new AtomicReference<>();
    private Future<?> currentTask;
    private SearchEvent changeEvent;

    public BDSimpleSearchBox() {
        getStyleClass().add(CSS_CLASS_NAME);
        // 创建单线程池，线程设置为守护线程
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        mapping.addDisposeEvent(this::dispose)
                .addListener(searchResult, (_, _, nv) -> {
                    if (nv == null) searchResultIndex.set(0);
                    else searchResultIndex.set(searchList.indexOf(nv));
                });
    }

    public void setChangeEvent(SearchEvent changeEvent) {
        this.changeEvent = changeEvent;
    }

    public abstract LinkedHashMap<T, String> getSearchSource();

    void search() {

        if (changeEvent != null) changeEvent.onSearchStart();
        searchList.clear(); // 清空，UI 线程安全

        // 取消正在运行的旧任务
        Task<?> oldTask = currentTaskRef.getAndSet(null);
        if (oldTask != null && !oldTask.isDone()) {
            oldTask.cancel(true);
        }
        if (!show.get()) return;
        String regex = getRegularExpression();
        Map<T, String> source = getSearchSource();

        if (regex.isEmpty() || source == null || source.isEmpty()) {
            Platform.runLater(this::clean);
            return;
        }

        // 创建搜索任务
        Task<Map<T, List<SimpleSearchResult<T>>>> task = new Task<>() {
            @Override
            protected Map<T, List<SimpleSearchResult<T>>> call() {
                Map<T, List<SimpleSearchResult<T>>> resultMap = new LinkedHashMap<>();
                for (Map.Entry<T, String> entry : source.entrySet()) {
                    if (isCancelled()) return Collections.emptyMap();
                    String text = entry.getValue();
                    if (text == null || text.isEmpty()) continue;
                    try {
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(text);
                        List<SimpleSearchResult<T>> matches = new ArrayList<>();
                        while (matcher.find()) {
                            // 跳过空匹配，避免产生无意义的高亮块
                            if (matcher.start() == matcher.end()) {
                                continue;
                            }
                            matches.add(new SimpleSearchResult<>(entry.getKey(), matcher.start(), matcher.end()));
                        }
                        if (!matches.isEmpty()) {
                            resultMap.put(entry.getKey(), matches);
                        }
                    } catch (PatternSyntaxException e) {
                        System.err.println("Invalid regex: " + regex);
                    }
                }
                return resultMap;
            }
        };

        currentTaskRef.set(task);

        task.setOnSucceeded(_ -> {
            if (currentTaskRef.get() != task) return; // 不是最新任务，丢弃
            Map<T, List<SimpleSearchResult<T>>> resultMap = task.getValue();
            int totalMatches = resultMap.values().stream().mapToInt(List::size).sum();
            Platform.runLater(() -> {
                // 原子性更新
                searchMap.clear();
                searchMap.putAll(resultMap);
                List<SimpleSearchResult<T>> allMatches = resultMap.values().stream()
                        .flatMap(List::stream)
                        .toList();
                searchList.clear();
                searchList.addAll(allMatches);
                searchBlockCount.set(totalMatches);
                refresh.set(!refresh.get());
                if (!searchList.isEmpty()) {
                    searchResult.set(searchList.getFirst());
                }
                if (changeEvent != null) changeEvent.onSearchEnd();
            });
        });

        task.setOnFailed(ev -> {
            if (currentTaskRef.get() != task) return;
            Platform.runLater(() -> {
                searchMap.clear();
                searchBlockCount.set(0);
                searchList.clear();
                refresh.set(!refresh.get());
            });
            task.getException().printStackTrace();
        });

        executor.submit(task);
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
        clean();
    }

    void clean() {
        searchList.clear();
        searchMap.clear();
        searchBlockCount.set(0);
        searchResult.set(null);
        // 取消正在运行的旧任务
        Task<?> oldTask = currentTaskRef.getAndSet(null);
        if (oldTask != null && !oldTask.isDone()) {
            oldTask.cancel(true);
        }
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
        if (searchList.isEmpty()) return;
        int ov = searchResultIndex.get();
        if (searchResultIndex.get() > 0) searchResult.set(searchList.get(searchResultIndex.get() - 1));
        else searchResult.set(searchList.getLast());
        if (changeEvent != null)
            changeEvent.onPrevious(ov, searchResultIndex.get());
    }

    public void nextSearchBlock() {
        if (searchList.isEmpty()) return;
        int ov = searchResultIndex.get();
        if (searchList.size() > searchResultIndex.get() + 1)
            searchResult.set(searchList.get(searchResultIndex.get() + 1));
        else searchResult.set(searchList.getFirst());
        if (changeEvent != null)
            changeEvent.onNext(ov, searchResultIndex.get());
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


    public Map<T, List<SimpleSearchResult<T>>> getSearchMap() {
        return searchMap;
    }

    public SimpleBooleanProperty refreshProperty() {
        return refresh;
    }

    public void refreshSearch() {
        refreshSearch.set(!refreshSearch.get());
    }

    public SimpleBooleanProperty refreshSearchProperty() {
        return refreshSearch;
    }

    public interface SearchEvent {
        void onSearchStart();

        void onSearchEnd();

        void onPrevious(int ov, int nv);

        void onNext(int ov, int nv);
    }

    public record SimpleSearchResult<T>(T t, int startOffset, int endOffset) {
    }
}