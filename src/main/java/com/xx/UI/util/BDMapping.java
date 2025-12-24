package com.xx.UI.util;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;

import java.util.*;

@SuppressWarnings("unchecked")
public class BDMapping {
    /**
     * 存储需要的数据
     */
    private final LazyValue<Map<Object, Object>> data = new LazyValue<>(HashMap::new);
    /**
     * 存储子BDMapping，dispose时会顺序调用children的dispose
     */
    private final LazyValue<List<BDMapping>> children = new LazyValue<>(ArrayList::new);
    /**
     * 存储让dispose时执行的事件。
     */
    private final LazyValue<List<Runnable>> disposeEvent = new LazyValue<>(ArrayList::new);
    /**
     * 存储property的bind列表。key为被绑定值，value为绑定值列表。
     */
    private final LazyValue<Map<ObservableValue<?>, PropertyBindEntry<?>>> bindMap = new LazyValue<>(HashMap::new);
    /**
     * 存储双向绑定的属性列表。
     */
    private final LazyValue<List<PropertyBidirectional<?>>> bidirectionalMap = new LazyValue<>(ArrayList::new);
    /**
     * 存储绑定关系。key为绑定对象，value为绑定对象列表。
     */

    private final LazyValue<Map<Binding<?>, BindingEntry<?>>> bindingMap = new LazyValue<>(HashMap::new);
    /**
     * InvalidationListener列表。key为被监听对象，value为监听器。
     */
    private final LazyValue<Map<Observable, List<InvalidationListener>>> observableInvalidationListenerMap = new LazyValue<>(HashMap::new);
    /**
     * ObservableValueListener列表。key为被监听对象，value为监听器entry。
     */
    private final LazyValue<Map<ObservableValue<?>, ChangeListenerEntry<?>>> changeListenerMap = new LazyValue<>(HashMap::new);
    /**
     * ListChangeListener列表。key为被监听对象，value为监听器。
     */
    private final LazyValue<Map<ObservableList<?>, ListChangeListenerEntry<?>>> listChangeListenerMap = new LazyValue<>(HashMap::new);
    /**
     * SetChangeListener列表。key为被监听对象，value为监听器。
     */
    private final LazyValue<Map<ObservableSet<?>, SetChangeListenerEntry<?>>> setChangeListenerMap = new LazyValue<>(HashMap::new);
    /**
     * MapChangeListener列表。key为被监听对象，value为监听器。
     */
    private final LazyValue<Map<ObservableMap<?, ?>, MapChangeListenerEntry<?, ?>>> mapChangeListenerMap = new LazyValue<>(HashMap::new);
    /**
     * 事件处理器列表。key为被监听对象，value为监听器。
     */
    private final LazyValue<Map<EventTarget, List<EventHandEntry<?>>>> eventHandlerMap = new LazyValue<>(HashMap::new);
    /**
     * 事件过滤器列表。key为被监听对象，value为监听器。
     */
    private final LazyValue<Map<EventTarget, List<EventFilterEntry<?>>>> eventFilterMap = new LazyValue<>(HashMap::new);
    /**
     * 存储定时器列表。
     */
    private final LazyValue<List<BDScheduler>> schedulerList = new LazyValue<>(ArrayList::new);

    /**
     * @param <T>        属性值的类型
     * @param boundValue 源属性（被绑定的属性），其值将作为绑定源
     * @param bindValues 目标属性集合，这些属性将被绑定到源属性
     */
    public <T> BDMapping bindProperty(Property<? extends T> boundValue, Collection<Property<T>> bindValues) {
        Objects.requireNonNull(boundValue, "被绑定值不能为null");
        Objects.requireNonNull(bindValues, "绑定值集合不能为null");
        this.<T>getOrCreateBindEntry(boundValue).bindProperty(bindValues);
        return this;
    }

    /**
     * @param <T>        属性值的类型
     * @param boundValue 源属性（被绑定的属性），其值将作为绑定源
     * @param bindValues 目标属性数组，这些属性将被绑定到源属性
     */
    @SafeVarargs
    public final <T> BDMapping bindProperty(Property<? extends T> boundValue, Property<T>... bindValues) {
        Objects.requireNonNull(boundValue, "被绑定值不能为null");
        Objects.requireNonNull(bindValues, "绑定值数组不能为null");
        if (bindValues.length == 0) throw new IllegalArgumentException("绑定值数组不能为空");
        return bindProperty(boundValue, Arrays.asList(bindValues));
    }

    /**
     * @param <T>        属性值的类型
     * @param bindValue  目标属性，其值将被绑定到boundValue
     * @param boundValue 源属性（被绑定的属性），其值将作为绑定源
     *
     */
    public <T> BDMapping bindProperty(Property<T> bindValue, ObservableValue<? extends T> boundValue) {
        this.<T>getOrCreateBindEntry(boundValue).bindProperty(bindValue);
        return this;
    }

    private <T> PropertyBindEntry<T> getOrCreateBindEntry(ObservableValue<? extends T> boundValue) {
        return (PropertyBindEntry<T>) bindMap.get().computeIfAbsent(boundValue, k -> new PropertyBindEntry<>(k, new ArrayList<>()));
    }

    /**
     * 获取指定属性绑定的源属性。
     *
     * <p>该方法用于查找给定属性（{@code property}）所绑定的源属性（bound value）。
     * 如果该属性未被绑定到任何源属性，则返回 {@code null}。</p>
     *
     * @param <T>      属性值的类型
     * @param property 要查询的目标属性
     * @return 该属性绑定的源属性，如果未找到绑定关系则返回 {@code null}
     */
    public <T> ObservableValue<? extends T> getBoundValue(Property<T> property) {
        Objects.requireNonNull(property, "属性不能为null");
        if (bindMap.isNone()) return null;
        Map<ObservableValue<?>, PropertyBindEntry<?>> map = bindMap.get();
        for (PropertyBindEntry<?> entry : map.values()) {
            if (entry.bindValues.contains(property)) {

                PropertyBindEntry<T> typedEntry = (PropertyBindEntry<T>) entry;
                return typedEntry.boundValue;
            }
        }
        return null;
    }

    /**
     * 释放bindList中的所有绑定关系。
     */
    public BDMapping disposeBindPropertyList() {
        bindMap.applyIfNotNone(map -> {
            map.values().forEach(PropertyBindEntry::dispose);
            map.clear();
        });
        return this;
    }

    /**
     * 绑定双向绑定关系。由property1来绑定property2。
     *
     * @param property1 第一个属性，其值将作为绑定源
     * @param property2 第二个属性，其值将作为绑定目标
     *
     */
    public <T> BDMapping bindBidirectional(Property<T> property1, Property<T> property2) {
        bidirectionalMap.get().add(new PropertyBidirectional<>(property1, property2));
        return this;
    }

    /**
     * 绑定双向绑定关系。参数集合中的每个属性都将被绑定到第一个属性。
     *
     * @param properties 属性集合，至少需要两个属性，第一个属性将作为绑定源，其余属性将作为绑定目标。
     *
     */
    public <T> BDMapping bindBidirectional(Collection<Property<T>> properties) {
        Objects.requireNonNull(properties, "属性集合不能为null");
        if (properties.size() < 2) throw new IllegalArgumentException("属性集合至少需要两个属性");
        HashSet<Property<T>> set = new HashSet<>(properties);
        if (set.size() < properties.size()) throw new IllegalArgumentException("属性集合不能包含重复属性");
        Property<T> first = new ArrayList<>(properties).getFirst();
        set.forEach(p -> {
            if (p != first) bindBidirectional(p, first);
        });
        return this;
    }

    /**
     * 绑定双向绑定关系。参数数组中的每个属性都将被绑定到第一个属性。
     *
     * @param properties 属性数组，至少需要两个属性，第一个属性将作为绑定源，其余属性将作为绑定目标。
     *
     */
    @SafeVarargs
    public final <T> BDMapping bindBidirectional(Property<T>... properties) {
        return bindBidirectional(Arrays.asList(properties));
    }

    /**
     * 解除指定属性的所有双向绑定关系。
     *
     * @param property 要解除绑定的属性
     *
     */
    public <T> BDMapping unbindBidirectional(Property<T> property) {
        bidirectionalMap.applyIfNotNone(list -> {
            list.forEach(p -> {
                if (p.property1 == property || p.property2 == property) p.unbind();
            });
            list.removeIf(p -> p.property1 == property || p.property2 == property);
        });
        return this;
    }

    /**
     * 释放bidirectionalList中的所有绑定关系。
     */
    public BDMapping disposeBidirectionalList() {
        bidirectionalMap.applyIfNotNone(list -> {
            list.forEach(PropertyBidirectional::unbind);
            list.clear();
        });
        return this;
    }

    /**
     * 绑定绑定关系
     *
     * @param <T>          属性值的类型
     * @param bindingValue 绑定对象，其值将作为绑定源
     * @param bindValues   目标属性集合，这些属性将被绑定到源属性
     *
     */
    public <T> BDMapping binding(Binding<T> bindingValue, Collection<Property<T>> bindValues) {
        Objects.requireNonNull(bindingValue, "绑定对象不能为null");
        Objects.requireNonNull(bindValues, "绑定值集合不能为null");
        if (bindValues.isEmpty()) throw new IllegalArgumentException("绑定值集合不能为空");
        this.getOrCreateBindingEntry(bindingValue).bindProperty(bindValues);
        return this;
    }

    /**
     * 绑定绑定关系
     *
     * @param <T>          属性值的类型
     * @param bindingValue 绑定对象，其值将作为绑定源
     * @param bindValues   目标属性数组，这些属性将被绑定到源属性
     *
     */
    @SafeVarargs
    public final <T> BDMapping binding(Binding<T> bindingValue, Property<T>... bindValues) {
        Objects.requireNonNull(bindingValue, "绑定对象不能为null");
        Objects.requireNonNull(bindValues, "绑定值数组不能为null");
        return binding(bindingValue, Arrays.asList(bindValues));
    }

    /**
     * 绑定绑定关系
     *
     * @param <T>          属性值的类型
     * @param bindValue    目标属性，其值将被绑定到boundValue
     * @param bindingValue 绑定对象，其值将作为绑定源
     *
     */
    public <T> BDMapping binding(Property<T> bindValue, Binding<T> bindingValue) {
        this.getOrCreateBindingEntry(bindingValue).bindProperty(bindValue);
        return this;
    }


    private <T> BindingEntry<T> getOrCreateBindingEntry(Binding<T> bindingValue) {
        return (BindingEntry<T>) bindingMap.get().computeIfAbsent(bindingValue, k -> new BindingEntry<>(k, new ArrayList<>()));
    }

    /**
     * 释放bindingList中的所有绑定关系。
     */
    public BDMapping disposeBindingList() {
        bindingMap.applyIfNotNone(map -> {
            map.values().forEach(BindingEntry::dispose);
            map.clear();
        });
        return this;
    }

    /**
     * 添加InvalidationListener监听器。
     *
     * @param observable 被监听对象
     * @param listener   监听器
     */
    public BDMapping addListener(Observable observable, InvalidationListener listener) {
        Objects.requireNonNull(observable, "被监听对象不能为null");
        Objects.requireNonNull(listener, "监听器不能为null");
        observableInvalidationListenerMap.get().computeIfAbsent(observable, _ -> new ArrayList<>()).add(listener);
        observable.addListener(listener);
        return this;
    }

    /**
     * 为指定被监听对象数组添加InvalidationListener监听器。
     *
     * @param observables 被监听对象数组
     * @param runnableS   监听器
     * @param runFirst    是否先执行监听器
     */
    public BDMapping addListener(Runnable runnableS, Boolean runFirst, Observable... observables) {
        Objects.requireNonNull(runnableS, "监听器不能为null");
        Objects.requireNonNull(observables, "监听对象不能为null");
        if (observables.length == 0) throw new IllegalArgumentException("监听对象不能为空");
        if (runFirst) runnableS.run();
        for (Observable observable : observables)
            addListener(observable, _ -> runnableS.run());
        return this;
    }

    /**
     * 返回指定被监听对象上的监听器列表。
     *
     * @param observable 被监听对象
     */
    public List<InvalidationListener> getListeners(Observable observable) {
        Objects.requireNonNull(observable, "被监听对象不能为null");
        if (observableInvalidationListenerMap.isNone()) return null;
        if (!observableInvalidationListenerMap.get().containsKey(observable)) return null;
        return observableInvalidationListenerMap.get().get(observable);
    }

    /**
     * 释放指定被监听对象上的监听器。
     *
     * @param observables 被监听对象数组
     */
    public BDMapping disposeListener(Observable... observables) {
        Objects.requireNonNull(observables, "被监听对象不能为null");
        if (observables.length == 0) throw new IllegalArgumentException("被监听对象不能为空");
        observableInvalidationListenerMap.applyIfNotNone(map -> {
            for (Observable observable : observables) {
                if (map.containsKey(observable)) {
                    List<InvalidationListener> list = map.get(observable);
                    list.forEach(observable::removeListener);
                    list.clear();
                }
            }
        });
        return this;
    }

    public BDMapping disposeInvalidationListeners() {
        observableInvalidationListenerMap.applyIfNotNone(map -> {
            map.forEach((k, v) -> v.forEach(k::removeListener));
            map.clear();
        });
        return this;
    }

    /**
     * 添加ChangeListener监听器。
     *
     * @param observable 被监听对象
     * @param listener   监听器
     */

    public <T> BDMapping addListener(ObservableValue<T> observable, ChangeListener<? super T> listener) {
        Objects.requireNonNull(observable, "被监听对象不能为null");
        Objects.requireNonNull(listener, "监听器不能为null");
        ((ChangeListenerEntry<T>) changeListenerMap
                .get()
                .computeIfAbsent(observable, _ -> new ChangeListenerEntry<>(observable, new ArrayList<>())))
                .addListener(listener);
        return this;
    }

    /**
     * 为指定被监听对象数组添加ChangeListener监听器。
     *
     * @param observables 被监听对象数组
     * @param runnableS   监听器
     * @param runFirst    是否先执行监听器
     *
     */
    public BDMapping addListener(Runnable runnableS, Boolean runFirst, ObservableValue<?>... observables) {
        Objects.requireNonNull(runnableS, "监听器不能为null");
        Objects.requireNonNull(observables, "监听对象不能为null");
        if (observables.length == 0) throw new IllegalArgumentException("监听对象不能为空");
        if (runFirst) runnableS.run();
        for (ObservableValue<?> observable : observables)
            addListener(observable, (_, _, _) -> runnableS.run());
        return this;
    }

    /**
     * 返回指定被监听对象上的监听器列表。
     *
     * @param observable 被监听对象
     */

    public <T> ChangeListenerEntry<T> getListeners(ObservableValue<T> observable) {
        Objects.requireNonNull(observable, "被监听对象不能为null");
        if (changeListenerMap.isNone()) return null;
        if (!changeListenerMap.get().containsKey(observable)) return null;
        return (ChangeListenerEntry<T>) changeListenerMap.get().get(observable);
    }

    /**
     * 释放指定被监听对象上的监听器。
     *
     * @param observables 被监听对象数组
     */
    public <T> BDMapping disposeListener(ObservableValue<T>... observables) {
        Objects.requireNonNull(observables, "被监听对象不能为null");
        if (observables.length == 0) throw new IllegalArgumentException("被监听对象不能为空");
        changeListenerMap.applyIfNotNone(map -> {
            for (ObservableValue<T> observable : observables) {
                if (map.containsKey(observable)) map.get(observable).dispose();
            }
        });
        return this;
    }

    /**
     * 释放observerListenerList中的所有监听器。
     */
    public BDMapping disposeChangeListeners() {
        changeListenerMap.applyIfNotNone(map -> {
            map.values().forEach(ChangeListenerEntry::dispose);
            map.clear();
        });
        return this;
    }

    /**
     * 添加ListChangeListener监听器。
     *
     * @param observableList 被监听对象
     * @param listener       监听器
     */

    public <T> BDMapping addListener(ObservableList<T> observableList, ListChangeListener<? super T> listener) {
        Objects.requireNonNull(observableList, "被监听对象不能为null");
        Objects.requireNonNull(listener, "监听器不能为null");
        ((ListChangeListenerEntry<T>) listChangeListenerMap
                .get()
                .computeIfAbsent(observableList, _ -> new ListChangeListenerEntry<>(observableList, new ArrayList<>())))
                .addListener(listener);
        return this;
    }

    /**
     * 为指定被监听对象数组添加ListChangeListener监听器。
     *
     * @param observableLists 被监听对象数组
     * @param runnableS       监听器
     * @param runFirst        是否先执行监听器
     *
     */
    public BDMapping addListener(Runnable runnableS, Boolean runFirst, ObservableList<?>... observableLists) {
        Objects.requireNonNull(runnableS, "监听器不能为null");
        Objects.requireNonNull(observableLists, "监听对象不能为null");
        if (observableLists.length == 0) throw new IllegalArgumentException("监听对象不能为空");
        if (runFirst) runnableS.run();
        for (ObservableList<?> observableList : observableLists)
            addListener(observableList, (ListChangeListener<? super Object>) _ -> runnableS.run());
        return this;
    }

    /**
     * 返回指定被监听对象上的监听器列表。
     *
     * @param observableList 被监听对象
     */

    public <T> ListChangeListenerEntry<T> getListeners(ObservableList<T> observableList) {
        Objects.requireNonNull(observableList, "被监听对象不能为null");
        if (listChangeListenerMap.isNone()) return null;
        if (!listChangeListenerMap.get().containsKey(observableList)) return null;
        return (ListChangeListenerEntry<T>) listChangeListenerMap.get().get(observableList);
    }

    /**
     * 释放指定被监听对象上的监听器。
     *
     * @param observableList 被监听对象
     */
    public BDMapping disposeListener(ObservableList<?>... observableList) {
        Objects.requireNonNull(observableList, "被监听对象不能为null");
        if (observableList.length == 0) throw new IllegalArgumentException("被监听对象不能为空");
        listChangeListenerMap.applyIfNotNone(map -> {
            for (ObservableList<?> observable : observableList) {
                if (map.containsKey(observable)) map.get(observable).dispose();
            }
        });
        return this;
    }

    /**
     * 释放listChangeListenerList中的所有监听器。
     */
    public BDMapping disposeListChangeListeners() {
        listChangeListenerMap.applyIfNotNone(map -> {
            map.values().forEach(ListChangeListenerEntry::dispose);
            map.clear();
        });
        return this;
    }

    /**
     * 添加SetChangeListener监听器。
     *
     * @param observableSet 被监听对象
     * @param listener      监听器
     */

    public <T> BDMapping addListener(ObservableSet<T> observableSet, SetChangeListener<? super T> listener) {
        Objects.requireNonNull(observableSet, "被监听对象不能为null");
        Objects.requireNonNull(listener, "监听器不能为null");
        ((SetChangeListenerEntry<T>) setChangeListenerMap
                .get()
                .computeIfAbsent(observableSet, _ -> new SetChangeListenerEntry<>(observableSet, new ArrayList<>())))
                .addListener(listener);
        return this;
    }

    /**
     * 为指定被监听对象数组添加SetChangeListener监听器。
     *
     * @param observableSets 被监听对象数组
     * @param runnableS      监听器
     * @param runFirst       是否先执行监听器
     *
     */
    public BDMapping addListener(Runnable runnableS, Boolean runFirst, ObservableSet<?>... observableSets) {
        Objects.requireNonNull(runnableS, "监听器不能为null");
        Objects.requireNonNull(observableSets, "监听对象不能为null");
        if (observableSets.length == 0) throw new IllegalArgumentException("监听对象不能为空");
        if (runFirst) runnableS.run();
        for (ObservableSet<?> observableSet : observableSets)
            addListener(observableSet, (SetChangeListener<? super Object>) _ -> runnableS.run());
        return this;
    }

    /**
     * 返回指定被监听对象上的监听器列表。
     *
     * @param observableSet 被监听对象
     *
     */

    public <T> SetChangeListenerEntry<T> getListeners(ObservableSet<T> observableSet) {
        Objects.requireNonNull(observableSet, "被监听对象不能为null");
        if (setChangeListenerMap.isNone()) return null;
        if (!setChangeListenerMap.get().containsKey(observableSet)) return null;
        return (SetChangeListenerEntry<T>) setChangeListenerMap.get().get(observableSet);
    }

    /**
     * 释放指定被监听对象上的监听器。
     *
     * @param observableSet 被监听对象
     */
    public BDMapping disposeListener(ObservableSet<?>... observableSet) {
        Objects.requireNonNull(observableSet, "被监听对象不能为null");
        if (observableSet.length == 0) throw new IllegalArgumentException("被监听对象不能为空");
        setChangeListenerMap.applyIfNotNone(map -> {
            for (ObservableSet<?> observable : observableSet) {
                if (map.containsKey(observable)) map.get(observable).dispose();
            }
        });
        return this;
    }

    /**
     * 释放setChangeListenerList中的所有监听器。
     */
    public BDMapping disposeSetChangeListeners() {
        setChangeListenerMap.applyIfNotNone(map -> {
            map.values().forEach(SetChangeListenerEntry::dispose);
            map.clear();
        });
        return this;
    }

    /**
     * 添加MapChangeListener监听器。
     *
     * @param observableMap 被监听对象
     * @param listener      监听器
     */

    public <K, V> BDMapping addListener(ObservableMap<K, V> observableMap, MapChangeListener<? super K, ? super V> listener) {
        Objects.requireNonNull(observableMap, "被监听对象不能为null");
        Objects.requireNonNull(listener, "监听器不能为null");
        ((MapChangeListenerEntry<K, V>) mapChangeListenerMap
                .get()
                .computeIfAbsent(observableMap, _ -> new MapChangeListenerEntry<>(observableMap, new ArrayList<>())))
                .addListener(listener);
        return this;
    }

    /**
     * 为指定被监听对象数组添加MapChangeListener监听器。
     *
     * @param observableMaps 被监听对象数组
     * @param runnableS      监听器
     * @param runFirst       是否先执行监听器
     *
     */
    public BDMapping addListener(Runnable runnableS, Boolean runFirst, ObservableMap<?, ?>... observableMaps) {
        Objects.requireNonNull(runnableS, "监听器不能为null");
        Objects.requireNonNull(observableMaps, "监听对象不能为null");
        if (observableMaps.length == 0) throw new IllegalArgumentException("监听对象不能为空");
        if (runFirst) runnableS.run();
        for (ObservableMap<?, ?> observableMap : observableMaps)
            addListener(observableMap, (MapChangeListener<? super Object, ? super Object>) _ -> runnableS.run());
        return this;
    }

    /**
     * 返回指定被监听对象上的监听器列表。
     *
     * @param observableMap 被监听对象
     */

    public <K, V> MapChangeListenerEntry<K, V> getListeners(ObservableMap<K, V> observableMap) {
        Objects.requireNonNull(observableMap, "被监听对象不能为null");
        if (mapChangeListenerMap.isNone()) return null;
        if (!mapChangeListenerMap.get().containsKey(observableMap)) return null;
        return (MapChangeListenerEntry<K, V>) mapChangeListenerMap.get().get(observableMap);
    }

    /**
     * 释放指定被监听对象上的监听器。
     *
     * @param observableMap 被监听对象
     */
    public BDMapping disposeListener(ObservableMap<?, ?>... observableMap) {
        Objects.requireNonNull(observableMap, "被监听对象不能为null");
        if (observableMap.length == 0) throw new IllegalArgumentException("被监听对象不能为空");
        mapChangeListenerMap.applyIfNotNone(map -> {
            for (ObservableMap<?, ?> observable : observableMap) {
                if (map.containsKey(observable)) map.get(observable).dispose();
            }
        });
        return this;
    }

    /**
     * 释放mapChangeListenerList中的所有监听器。
     */
    public BDMapping disposeMapChangeListeners() {
        mapChangeListenerMap.applyIfNotNone(map -> {
            map.values().forEach(MapChangeListenerEntry::dispose);
            map.clear();
        });
        return this;
    }

    /**
     * 添加EventHandler事件。
     *
     * @param target    事件源
     * @param eventType 事件类型
     * @param handler   触发器
     */
    public <T extends Event> BDMapping addEventHandler(EventTarget target, EventType<T> eventType, EventHandler<? super T> handler) {
        Objects.requireNonNull(eventType, "事件类型不能为null");
        Objects.requireNonNull(target, "事件源不能为null");
        Objects.requireNonNull(handler, "监听器不能为null");
        if (eventHandlerMap.get().containsKey(target)) {
            eventHandlerMap
                    .get()
                    .get(target)
                    .stream()
                    .filter(entry -> entry.eventType == eventType)
                    .findAny()
                    .ifPresentOrElse(entry -> {
                                @SuppressWarnings("unchecked")
                                EventHandEntry<T> typedEntry = (EventHandEntry<T>) entry;
                                typedEntry.addHandler(target, handler);
                            },
                            () -> eventHandlerMap
                                    .get()
                                    .get(target)
                                    .add(new EventHandEntry<>(eventType, new ArrayList<>()).addHandler(target, handler)));
        } else
            eventHandlerMap
                    .get()
                    .put(target, new ArrayList<>(List.of(new EventHandEntry<>(eventType, new ArrayList<>())
                            .addHandler(target, handler))));

        return this;
    }

    /**
     * 为指定事件源数组添加EventHandler事件。
     *
     * @param target   事件源。
     * @param runnable 触发器。
     * @param runFirst 是否先执行触发器。
     */
    public <T extends Event> BDMapping addEventHandler(EventTarget target, EventType<T> eventType, Runnable runnable, boolean runFirst) {
        Objects.requireNonNull(eventType, "事件类型不能为null");
        Objects.requireNonNull(target, "事件源不能为null");
        Objects.requireNonNull(runnable, "监听器不能为null");
        if (runFirst) runnable.run();
        return addEventHandler(target, eventType, event -> runnable.run());
    }

    /**
     * 移除指定事件源上的指定事件类型的监听器。
     *
     * @param target 事件源
     * @param types  事件类型数组
     */
    public <T extends Event> BDMapping removeEventHandler(EventTarget target, EventType<T>... types) {
        Objects.requireNonNull(target, "事件类型不能为null");
        Objects.requireNonNull(target, "事件源不能为null");
        if (types.length == 0) throw new IllegalArgumentException("事件类型不能为空");
        if (eventHandlerMap.isNone()) return this;
        if (!eventHandlerMap.get().containsKey(target)) return this;
        List<EventHandEntry<?>> list = eventHandlerMap.get().get(target);
        for (EventHandEntry<?> entry : list) {
            for (EventType<T> type : types) {
                if (entry.eventType == type) {
                    entry.dispose(target);
                    break;
                }
            }
        }
        return this;
    }

    /**
     * 移除指定事件源上的指定事件类型上的指定监听器。
     *
     * @param target    事件源
     * @param eventType 事件类型
     * @param handler   监听器
     */

    public <T extends Event> BDMapping removeEventHandler(EventTarget target, EventType<T> eventType, EventHandler<? super T> handler) {
        Objects.requireNonNull(eventType, "事件类型不能为null");
        Objects.requireNonNull(target, "事件源不能为null");
        Objects.requireNonNull(handler, "监听器不能为null");
        if (eventHandlerMap.isNone()) return this;
        if (!eventHandlerMap.get().containsKey(target)) return this;
        List<EventHandEntry<?>> list = eventHandlerMap.get().get(target);
        for (EventHandEntry<?> entry : list) {
            if (entry.eventType == eventType) {
                ((EventHandEntry<T>) entry).removeHandler(target, handler);
                break;
            }
        }
        return this;
    }

    /**
     * 返回指定事件源上的指定事件类型上的监听器列表。
     *
     * @param target    事件源
     * @param eventType 事件类型
     */

    public <T extends Event> EventHandEntry<T> getEventHandlerEntry(EventTarget target, EventType<T> eventType) {
        Objects.requireNonNull(eventType, "事件类型不能为null");
        Objects.requireNonNull(target, "事件源不能为null");
        if (eventHandlerMap.isNone()) return null;
        if (!eventHandlerMap.get().containsKey(target)) return null;
        List<EventHandEntry<?>> list = eventHandlerMap.get().get(target);
        for (EventHandEntry<?> entry : list) {
            if (entry.eventType == eventType)
                return (EventHandEntry<T>) entry;
        }
        return null;
    }

    /**
     * 释放指定事件源上的事件。
     *
     * @param targets 事件源数组
     */

    public BDMapping disposeEventHandler(EventTarget... targets) {
        Objects.requireNonNull(targets, "事件源不能为null");
        if (targets.length == 0) throw new IllegalArgumentException("事件源不能为空");
        eventHandlerMap.applyIfNotNone(map -> {
            for (EventTarget target : targets) {
                if (map.containsKey(target)) {
                    List<EventHandEntry<?>> list = map.get(target);
                    list.forEach(entry -> entry.dispose(target));
                }
            }
        });
        return this;
    }

    /**
     * 释放eventHandlerList中的所有监听器。
     */
    public BDMapping disposeEventHandlers() {
        eventHandlerMap.applyIfNotNone(map -> {
            map.values().forEach(list -> list.forEach(entry -> entry.dispose(null)));
            map.clear();
        });
        return this;
    }

    /**
     * 添加EventFilter事件。
     *
     * @param target    事件源
     * @param eventType 事件类型
     * @param handler   触发器
     */
    public <T extends Event> BDMapping addEventFilter(EventTarget target, EventType<T> eventType, EventHandler<? super T> handler) {
        Objects.requireNonNull(eventType, "事件类型不能为null");
        Objects.requireNonNull(target, "事件源不能为null");
        Objects.requireNonNull(handler, "触发器不能为null");
        if (eventFilterMap.get().containsKey(target)) {
            eventFilterMap
                    .get()
                    .get(target)
                    .stream()
                    .filter(entry -> entry.eventType == eventType)
                    .findAny()
                    .ifPresentOrElse(entry -> {
                                @SuppressWarnings("unchecked")
                                EventFilterEntry<T> typedEntry = (EventFilterEntry<T>) entry;
                                typedEntry.addFilter(target, handler);
                            },
                            () -> eventFilterMap
                                    .get()
                                    .get(target)
                                    .add(new EventFilterEntry<>(eventType, new ArrayList<>()).addFilter(target, handler)));
        } else
            eventFilterMap
                    .get()
                    .put(target, new ArrayList<>(List.of(new EventFilterEntry<>(eventType, new ArrayList<>())
                            .addFilter(target, handler))));
        return this;
    }

    /**
     * 为指定事件源数组添加EventFilter事件。
     *
     * @param target   事件源。
     * @param runnable 触发器。
     * @param runFirst 是否先执行触发器。
     */
    public <T extends Event> BDMapping addEventFilter(EventTarget target, EventType<T> eventType, Runnable runnable, boolean runFirst) {
        Objects.requireNonNull(eventType, "事件类型不能为null");
        Objects.requireNonNull(target, "事件源不能为null");
        Objects.requireNonNull(runnable, "触发器不能为null");
        if (runFirst) runnable.run();
        addEventFilter(target, eventType, _ -> runnable.run());
        return this;
    }

    /**
     * 移除指定事件源上的指定事件类型的事件。
     *
     * @param target 事件源
     * @param types  事件类型数组
     */
    public <T extends Event> BDMapping removeEventFilter(EventTarget target, EventType<T>... types) {
        Objects.requireNonNull(target, "事件类型不能为null");
        Objects.requireNonNull(types, "事件源不能为null");
        if (types.length == 0) throw new IllegalArgumentException("事件类型不能为空");
        if (eventFilterMap.isNone()) return this;
        if (!eventFilterMap.get().containsKey(target)) return this;
        List<EventFilterEntry<?>> list = eventFilterMap.get().get(target);
        for (EventFilterEntry<?> entry : list) {
            for (EventType<T> type : types) {
                if (entry.eventType == type) {
                    entry.dispose(target);
                    break;
                }
            }
        }
        return this;
    }

    /**
     * 移除指定事件源上的指定事件类型上的指定监听器。
     *
     * @param target    事件源
     * @param eventType 事件类型
     * @param handler   监听器
     */

    public <T extends Event> BDMapping removeEventFilter(EventTarget target, EventType<T> eventType, EventHandler<? super T> handler) {
        Objects.requireNonNull(eventType, "事件类型不能为null");
        Objects.requireNonNull(target, "事件源不能为null");
        Objects.requireNonNull(handler, "监听器不能为null");
        if (eventFilterMap.isNone()) return this;
        if (!eventFilterMap.get().containsKey(target)) return this;
        List<EventFilterEntry<?>> list = eventFilterMap.get().get(target);
        for (EventFilterEntry<?> entry : list) {
            if (entry.eventType == eventType) {
                ((EventFilterEntry<T>) entry).removeFilter(target, handler);
                break;
            }
        }
        return this;
    }

    /**
     * 返回指定事件源上的指定事件类型上的监听器列表。
     *
     * @param target    事件源
     * @param eventType 事件类型
     */

    public <T extends Event> EventFilterEntry<T> getEventFilterEntry(EventTarget target, EventType<T> eventType) {
        Objects.requireNonNull(eventType, "事件类型不能为null");
        Objects.requireNonNull(target, "事件源不能为null");
        if (eventFilterMap.isNone()) return null;
        if (!eventFilterMap.get().containsKey(target)) return null;
        List<EventFilterEntry<?>> list = eventFilterMap.get().get(target);
        for (EventFilterEntry<?> entry : list) {
            if (entry.eventType == eventType)
                return (EventFilterEntry<T>) entry;
        }
        return null;
    }

    /**
     * 释放指定事件源上的监听器。
     *
     * @param targets 事件源数组
     */
    public BDMapping disposeEventFilter(EventTarget... targets) {
        Objects.requireNonNull(targets, "事件源不能为null");
        if (targets.length == 0) throw new IllegalArgumentException("事件源不能为空");
        eventFilterMap.applyIfNotNone(map -> {
            for (EventTarget target : targets) {
                if (map.containsKey(target)) {
                    List<EventFilterEntry<?>> list = map.get(target);
                    list.forEach(entry -> entry.dispose(target));
                }
            }

        });
        return this;
    }

    /**
     * 释放eventFilterList中的所有监听器。
     */
    public BDMapping disposeEventFilters() {
        eventFilterMap.applyIfNotNone(map -> {
            map.values().forEach(list -> list.forEach(entry -> entry.dispose(null)));
            map.clear();
        });
        return this;
    }

    public BDMapping addDisposeEvent(Runnable runnable) {
        disposeEvent.get().add(runnable);
        return this;
    }

    public BDMapping removeDisposeEvent(Runnable runnable) {
        disposeEvent.applyIfNotNone(list -> list.remove(runnable));
        return this;
    }

    /**
     * 添加子节点
     */
    public BDMapping addChildren(BDMapping... children) {
        Objects.requireNonNull(children, "子节点不能为null");
        if (children.length == 0) throw new IllegalArgumentException("子节点不能为空");
        this.children.get().addAll(List.of(children));
        return this;
    }

    /*
     * 移除子节点
     * */
    public BDMapping removeChild(BDMapping child) {
        Objects.requireNonNull(child, "子节点不能为null");
        this.children.applyIfNotNone(list -> list.remove(child));
        return this;
    }

    /**
     * 添加定时器。
     */
    public BDMapping addBDScheduler(BDScheduler scheduler) {
        Objects.requireNonNull(scheduler, "定时器不能为null");
        schedulerList.get().add(scheduler);
        return this;
    }

    public BDMapping removeBDScheduler(BDScheduler scheduler) {
        schedulerList.applyIfNotNone(list -> list.remove(scheduler));
        return this;
    }

    public BDMapping disposeBDScheduler() {
        schedulerList.applyIfNotNone(list -> list.forEach(BDScheduler::dispose));
        return this;
    }


    public void dispose() {
        children.applyIfNotNone(children -> {
            children.forEach(BDMapping::dispose);
            children.clear();
        });
        disposeBindPropertyList().
                disposeBindingList().
                disposeChangeListeners().
                disposeListChangeListeners().
                disposeSetChangeListeners().
                disposeMapChangeListeners().
                disposeEventHandlers().
                disposeEventFilters()
                .disposeBDScheduler().
                disposeEvent.applyIfNotNone(event -> {
                    event.forEach(Runnable::run);
                    event.clear();
                });
        data.applyIfNotNone(Map::clear);
    }

    public Map<Object, Object> getDatas() {
        if (data.isNone()) return null;
        return data.get();
    }

    public Object getData(Object key) {
        Objects.requireNonNull(key, "键不能为null");
        if (data.isNone()) return null;
        return data.get().get(key);
    }


    /**
     * 存储property的绑定关系。
     *
     * @param <T>        属性值的类型
     * @param boundValue 源属性（被绑定的属性），其值将作为绑定源
     * @param bindValues 目标属性集合，这些属性将被绑定到源属性
     */
    public record PropertyBindEntry<T>(ObservableValue<? extends T> boundValue, List<Property<T>> bindValues) {
        /**
         * 绑定属性。
         *
         * @param property 要绑定的属性
         */
        public void bindProperty(Property<T> property) {
            Objects.requireNonNull(property, "绑定属性不能为null");
            if (property.isBound()) throw new IllegalArgumentException("属性已绑定");
            property.bind(boundValue);
            bindValues.add(property);
        }

        /**
         * 绑定属性。
         *
         * @param properties 要绑定的属性集合
         */
        public void bindProperty(Collection<Property<T>> properties) {
            Objects.requireNonNull(properties, "绑定属性集合不能为null");
            for (Property<T> prop : properties) {
                bindProperty(prop);
            }
        }

        public void dispose() {
            bindValues.forEach(Property::unbind);
            bindValues.clear();
        }
    }

    /**
     * 存储双向绑定关系。
     *
     * @param <T>       属性值的类型
     * @param property1 第一个属性，其值将作为绑定源
     * @param property2 第二个属性，其值将作为绑定目标
     */
    public record PropertyBidirectional<T>(Property<T> property1, Property<T> property2) {
        public PropertyBidirectional {
            Objects.requireNonNull(property1, "属性1不能为null");
            Objects.requireNonNull(property2, "属性2不能为null");
            if (property1 == property2) throw new IllegalArgumentException("属性1和属性2不能相同");
            if (property1.isBound()) throw new IllegalArgumentException("属性1已绑定");
            if (property2.isBound()) throw new IllegalArgumentException("属性2已绑定");
            property1.bindBidirectional(property2);
        }

        public void unbind() {
            property1.unbindBidirectional(property2);
        }
    }

    /**
     * 存储绑定关系。
     *
     * @param <T>          属性值的类型
     * @param bindingValue 绑定对象，其值将作为绑定源
     * @param bindValues   目标属性集合，这些属性将被绑定到源属性
     */
    public record BindingEntry<T>(Binding<T> bindingValue, List<Property<T>> bindValues) {
        /**
         * 绑定属性。
         *
         * @param property 要绑定的属性
         */
        public void bindProperty(Property<T> property) {
            Objects.requireNonNull(property, "绑定属性不能为null");
            if (property.isBound()) throw new IllegalArgumentException("属性已绑定");
            property.bind(bindingValue);
            bindValues.add(property);
        }

        /**
         * 绑定属性。
         *
         * @param properties 要绑定的属性集合
         */
        public void bindProperty(Collection<Property<T>> properties) {
            Objects.requireNonNull(properties, "绑定属性集合不能为null");
            for (Property<T> prop : properties) bindProperty(prop);
        }

        public void dispose() {
            bindValues.forEach(Property::unbind);
            bindValues.clear();
            bindingValue.dispose();
        }
    }

    /**
     * 存储ChangeListener监听器。
     *
     * @param <T>             属性值的类型
     * @param observableValue 被监听对象
     * @param listeners       监听器集合
     */
    public record ChangeListenerEntry<T>(ObservableValue<T> observableValue,
                                         List<ChangeListener<? super T>> listeners) {
        public ChangeListenerEntry {
            Objects.requireNonNull(observableValue, "被监听对象不能为null");
            Objects.requireNonNull(listeners, "监听器集合不能为null");
            listeners.forEach(observableValue::addListener);
        }

        public void addListener(ChangeListener<? super T> listener) {
            observableValue.addListener(listener);
            listeners.add(listener);
        }

        public void dispose() {
            listeners.forEach(observableValue::removeListener);
            listeners.clear();
        }
    }

    /**
     * 存储ListChangeListener监听器。
     *
     * @param <T>            属性值的类型
     * @param observableList 被监听对象
     * @param listeners      监听器集合
     */
    public record ListChangeListenerEntry<T>(ObservableList<T> observableList,
                                             List<ListChangeListener<? super T>> listeners) {
        public ListChangeListenerEntry {
            Objects.requireNonNull(observableList, "被监听对象不能为null");
            Objects.requireNonNull(listeners, "监听器集合不能为null");
            listeners.forEach(observableList::addListener);
        }

        public void addListener(ListChangeListener<? super T> listener) {
            observableList.addListener(listener);
            listeners.add(listener);
        }

        public void dispose() {
            listeners.forEach(observableList::removeListener);
            listeners.clear();
        }

    }

    /**
     * 存储SetChangeListener监听器。
     *
     * @param <T>           属性值的类型2
     * @param observableSet 被监听对象
     * @param listeners     监听器集合
     *
     */
    public record SetChangeListenerEntry<T>(ObservableSet<T> observableSet,
                                            List<SetChangeListener<? super T>> listeners) {
        public SetChangeListenerEntry {
            Objects.requireNonNull(observableSet, "被监听对象不能为null");
            Objects.requireNonNull(listeners, "监听器集合不能为null");
            listeners.forEach(observableSet::addListener);
        }

        public void addListener(SetChangeListener<? super T> listener) {
            observableSet.addListener(listener);
            listeners.add(listener);
        }

        public void dispose() {
            listeners.forEach(observableSet::removeListener);
            listeners.clear();
        }
    }

    /**
     * 存储MapChangeListener监听器。
     *
     * @param <K>           属性值的类型1
     * @param <V>           属性值的类型2
     * @param observableMap 被监听对象
     * @param listeners     监听器集合
     *
     */
    public record MapChangeListenerEntry<K, V>(ObservableMap<K, V> observableMap,
                                               List<MapChangeListener<? super K, ? super V>> listeners) {
        public MapChangeListenerEntry {
            Objects.requireNonNull(observableMap, "被监听对象不能为null");
            Objects.requireNonNull(listeners, "监听器集合不能为null");
            listeners.forEach(observableMap::addListener);
        }

        public void addListener(MapChangeListener<? super K, ? super V> listener) {
            observableMap.addListener(listener);
            listeners.add(listener);
        }

        public void dispose() {
            listeners.forEach(observableMap::removeListener);
            listeners.clear();
        }
    }

    /**
     * 存储事件处理器。
     *
     * @param <T>       事件类型
     * @param eventType 事件类型
     * @param handlers  监听器集合
     */
    public record EventHandEntry<T extends Event>(EventType<T> eventType, List<EventHandler<? super T>> handlers) {
        public EventHandEntry {
            Objects.requireNonNull(eventType, "事件类型不能为null");
            Objects.requireNonNull(handlers, "监听器集合不能为null");
        }

        public EventHandEntry<T> addHandler(EventTarget target, EventHandler<? super T> handler) {
            Objects.requireNonNull(target, "事件源不能为null");
            Objects.requireNonNull(handler, "监听器不能为null");
            handlers.add(handler);
            target.addEventHandler(eventType, handler);
            return this;
        }

        public void removeHandler(EventTarget target, EventHandler<? super T> handler) {
            Objects.requireNonNull(target, "事件源不能为null");
            Objects.requireNonNull(handler, "监听器不能为null");
            target.removeEventHandler(eventType, handler);
            handlers.remove(handler);
        }

        public void dispose(EventTarget target) {
            if (target != null)
                handlers.forEach(h -> target.removeEventHandler(eventType, h));
            handlers.clear();
        }
    }

    /**
     * 存储事件过滤器。
     *
     * @param <T>       事件类型
     * @param eventType 事件类型
     * @param filters   过滤器集合
     */
    public record EventFilterEntry<T extends Event>(EventType<T> eventType, List<EventHandler<? super T>> filters) {
        public EventFilterEntry {
            Objects.requireNonNull(eventType, "事件类型不能为null");
            Objects.requireNonNull(filters, "过滤器集合不能为null");
        }

        public EventFilterEntry<T> addFilter(EventTarget target, EventHandler<? super T> filter) {
            Objects.requireNonNull(target, "事件源不能为null");
            Objects.requireNonNull(filter, "过滤器不能为null");
            filters.add(filter);
            target.addEventFilter(eventType, filter);
            return this;
        }

        public void removeFilter(EventTarget target, EventHandler<? super T> filter) {
            Objects.requireNonNull(target, "事件源不能为null");
            Objects.requireNonNull(filter, "过滤器不能为null");
            target.removeEventFilter(eventType, filter);
            filters.remove(filter);
        }

        public void dispose(EventTarget target) {
            if (target != null)
                filters.forEach(f -> target.removeEventFilter(eventType, f));
            filters.clear();
        }
    }
}
