package com.xx.UI.basic.progressBar;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.concurrent.Task;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 增强的任务类，支持暂停、恢复和超时功能
 *
 * @param <T> 任务返回类型
 */
public abstract class BDTask<T> extends Task<T> {

    // 常量
    private static final long NANOS_PER_MILLI = 1_000_000L;
    private static final long DEFAULT_TIMEOUT = -1L;
    // 使用可重入锁替代synchronized，提供更好的控制
    private final Lock stateLock = new ReentrantLock();
    private final Condition pauseCondition = stateLock.newCondition();
    // 原子变量确保状态的一致性
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean timedOut = new AtomicBoolean(false);
    private final AtomicBoolean taskStarted = new AtomicBoolean(false);
    private final AtomicBoolean pausedFlagForUI = new AtomicBoolean(false);
    // 时间相关变量使用原子类型
    private final AtomicLong taskStartNanoTime = new AtomicLong(0);
    private final AtomicLong pauseStartNanoTime = new AtomicLong(0);
    private final AtomicLong totalPauseNanoTime = new AtomicLong(0);
    // 超时相关
    private final long timeoutNanos;
    // JavaFX属性（只读，避免外部修改）
    private final ReadOnlyBooleanWrapper pausedProperty = new ReadOnlyBooleanWrapper(false);
    // 用于追踪中断状态
    private final ThreadLocal<Boolean> wasInterruptedDuringPause = ThreadLocal.withInitial(() -> false);
    private volatile long remainingTimeoutNanos = -1;
    // 执行器相关
    private ScheduledExecutorService timeoutExecutor;
    private ScheduledFuture<?> timeoutFuture;

    public BDTask(long timeoutMillis) {
        this.timeoutNanos = timeoutMillis > 0 ? timeoutMillis * NANOS_PER_MILLI : DEFAULT_TIMEOUT;
    }

    public BDTask() {
        this(DEFAULT_TIMEOUT);
    }

    /**
     * 检查任务状态（必须在work()方法中周期性调用）
     *
     * @throws InterruptedException 如果任务被中断或取消
     * @throws TimeoutException     如果任务超时
     */
    protected final void checkState() throws InterruptedException, TimeoutException {
        checkPauseState();
        checkTimeoutState();

        // 如果任务已被取消，抛出异常
        if (isCancelled()) {
            throw new InterruptedException("任务已被取消");
        }
    }

    /**
     * 检查暂停状态
     */
    protected final void checkPauseState() throws InterruptedException {
        // 如果不在暂停状态，快速返回
        if (!paused.get()) {
            return;
        }

        stateLock.lock();
        try {
            // 双重检查，防止在获取锁期间状态发生变化
            while (paused.get() && !isCancelled()) {
                try {
                    // 标记为暂停状态（用于UI）
                    if (!pausedFlagForUI.get()) {
                        pausedFlagForUI.set(true);
                        updatePausedState(true);
                    }

                    // 等待恢复信号
                    pauseCondition.await();
                } catch (InterruptedException e) {
                    // 检查是否由于取消而中断
                    if (isCancelled()) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }

                    // 记录中断状态，但继续等待（除非被取消）
                    wasInterruptedDuringPause.set(true);
                }
            }

            // 如果已经退出暂停状态，更新UI状态
            if (pausedFlagForUI.get()) {
                pausedFlagForUI.set(false);
                updatePausedState(false);
            }

        } finally {
            stateLock.unlock();
        }

        // 如果在暂停期间被中断过，恢复中断状态
        if (wasInterruptedDuringPause.get()) {
            wasInterruptedDuringPause.set(false);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 检查超时状态
     */
    protected final void checkTimeoutState() throws TimeoutException {
        if (timedOut.get()) {
            throw new TimeoutException("任务执行超时");
        }
    }

    @Override
    protected final T call() throws Exception {
        // 记录任务开始时间
        taskStartNanoTime.set(System.nanoTime());
        taskStarted.set(true);

        // 如果设置了超时时间，启动超时监控
        if (timeoutNanos > 0) startTimeoutMonitor(timeoutNanos);

        try {
            // 调用实际的工作方法
            T result = work();

            // 任务正常完成，取消超时监控
            cancelTimeoutMonitor();
            return result;

        } catch (Exception e) {
            // 任务异常，取消超时监控
            cancelTimeoutMonitor();
            throw e;
        } finally {
            // 确保清理资源
            shutdownTimeoutExecutor();
        }
    }

    /**
     * 实际的工作方法，由子类实现
     */
    protected abstract T work() throws Exception;

    /**
     * 启动超时监控
     */
    private void startTimeoutMonitor(long timeoutNanos) {
        // 创建单线程执行器
        timeoutExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "BDTask-Timeout-Monitor-" + hashCode());
            thread.setDaemon(true);
            return thread;
        });

        // 计算初始超时时间
        remainingTimeoutNanos = timeoutNanos;
        timeoutFuture = timeoutExecutor.schedule(
                this::handleTimeout,
                timeoutNanos,
                TimeUnit.NANOSECONDS
        );
    }

    /**
     * 重新启动超时监控（用于恢复时）
     */
    private void restartTimeoutMonitor() {
        stateLock.lock();
        try {
            // 只有在任务未完成且未取消且未超时时才重新启动监控
            if (isDone() || isCancelled() || timedOut.get() || !taskStarted.get()) {
                return;
            }

            // 计算准确的剩余时间
            long elapsedNanos = getElapsedNanoTime();
            long remainingNanos = timeoutNanos - elapsedNanos;

            // 如果剩余时间小于等于0，直接触发超时
            if (remainingNanos <= 0) {
                handleTimeout();
                return;
            }

            // 取消之前的监控任务
            cancelTimeoutMonitor();

            // 重新创建执行器（如果已关闭）
            if (timeoutExecutor == null || timeoutExecutor.isShutdown()) {
                timeoutExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread thread = new Thread(r, "BDTask-Timeout-Monitor-" + hashCode());
                    thread.setDaemon(true);
                    return thread;
                });
            }

            // 重新调度
            remainingTimeoutNanos = remainingNanos;
            timeoutFuture = timeoutExecutor.schedule(
                    this::handleTimeout,
                    remainingNanos,
                    TimeUnit.NANOSECONDS
            );

        } finally {
            stateLock.unlock();
        }
    }

    /**
     * 处理超时
     */
    private void handleTimeout() {
        // 设置超时标志
        timedOut.set(true);

        // 在JavaFX应用线程中取消任务
        Platform.runLater(() -> {
            if (!isDone() && !isCancelled()) {
                updateMessage("任务执行超时，已取消");
                cancel(true);
            }
        });

        // 唤醒可能处于暂停状态的线程
        stateLock.lock();
        try {
            pauseCondition.signalAll();
        } finally {
            stateLock.unlock();
        }
    }

    /**
     * 取消超时监控
     */
    private void cancelTimeoutMonitor() {
        if (timeoutFuture != null && !timeoutFuture.isDone()) {
            timeoutFuture.cancel(false);
            timeoutFuture = null;
        }
    }

    /**
     * 关闭超时监控执行器
     */
    private void shutdownTimeoutExecutor() {
        if (timeoutExecutor != null && !timeoutExecutor.isShutdown()) {
            try {
                // 先取消所有任务
                timeoutExecutor.shutdownNow();

                // 等待终止
                if (!timeoutExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    System.err.println("BDTask: 超时执行器未能及时关闭");
                }
            } catch (InterruptedException e) {
                // 重新中断当前线程
                Thread.currentThread().interrupt();
            } finally {
                timeoutExecutor = null;
            }
        }
    }

    @Override
    public final boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancelled = super.cancel(mayInterruptIfRunning);

        if (cancelled) {
            // 取消超时监控
            cancelTimeoutMonitor();

            // 关闭执行器
            shutdownTimeoutExecutor();

            // 唤醒可能处于暂停状态的线程
            stateLock.lock();
            try {
                pauseCondition.signalAll();
            } finally {
                stateLock.unlock();
            }

            // 更新UI状态
            if (pausedFlagForUI.get()) {
                pausedFlagForUI.set(false);
                updatePausedState(false);
            }
        }

        return cancelled;
    }

    /**
     * 暂停任务
     */
    public final void pauseTask() {
        stateLock.lock();
        try {
            // 如果任务已经暂停、完成、取消或超时，直接返回
            if (paused.get() || isDone() || isCancelled() || timedOut.get()) {
                return;
            }

            // 设置暂停状态
            paused.set(true);
            pauseStartNanoTime.set(System.nanoTime());

            // 暂停超时监控
            if (timeoutFuture != null && !timeoutFuture.isDone() && !timedOut.get()) {
                // 计算剩余超时时间
                long elapsedNanos = getElapsedNanoTime();
                long remainingNanos = timeoutNanos - elapsedNanos;

                if (remainingNanos > 0) {
                    remainingTimeoutNanos = remainingNanos;
                    timeoutFuture.cancel(false);
                }
            }

        } finally {
            stateLock.unlock();
        }

        // 更新UI状态（不在锁内执行，避免死锁）
        updatePausedState(true);
    }

    /**
     * 恢复任务
     */
    public final void resumeTask() {
        boolean wasPaused;

        stateLock.lock();
        try {
            wasPaused = paused.getAndSet(false);

            if (wasPaused) {
                // 更新累计暂停时间
                long pauseDuration = System.nanoTime() - pauseStartNanoTime.get();
                if (pauseDuration > 0) {
                    totalPauseNanoTime.addAndGet(pauseDuration);
                }

                // 唤醒等待的线程
                pauseCondition.signalAll();

                // 恢复超时监控
                if (remainingTimeoutNanos > 0 && !isCancelled() && !timedOut.get() && taskStarted.get()) {
                    // 重新计算剩余时间（因为暂停期间可能已经过了更多时间）
                    long elapsedSincePause = System.nanoTime() - pauseStartNanoTime.get();
                    long adjustedRemaining = Math.max(0, remainingTimeoutNanos - elapsedSincePause);

                    if (adjustedRemaining > 0) {
                        restartTimeoutMonitor();
                    } else {
                        // 如果已经没有剩余时间，触发超时
                        handleTimeout();
                    }
                }
            }
        } finally {
            stateLock.unlock();
        }

        // 更新UI状态
        if (wasPaused) {
            updatePausedState(false);
        }
    }

    /**
     * 获取任务已运行时间（不包括暂停时间）
     */
    public final long getElapsedTimeMillis() {
        long elapsedNanos = getElapsedNanoTime();
        return elapsedNanos / NANOS_PER_MILLI;
    }

    /**
     * 获取任务已运行时间（纳秒，不包括暂停时间）
     */
    private long getElapsedNanoTime() {
        if (taskStartNanoTime.get() == 0) {
            return 0;
        }

        long now = System.nanoTime();
        long totalPaused = totalPauseNanoTime.get();

        // 如果当前正在暂停中，需要加上当前的暂停时间
        if (paused.get() && pauseStartNanoTime.get() > 0) {
            totalPaused += (now - pauseStartNanoTime.get());
        }

        long elapsed = now - taskStartNanoTime.get() - totalPaused;
        return Math.max(elapsed, 0);
    }

    /**
     * 获取剩余超时时间
     */
    public final long getRemainingTimeMillis() {
        if (timeoutNanos <= 0 || taskStartNanoTime.get() == 0) {
            return -1;
        }

        long elapsedNanos = getElapsedNanoTime();
        long remainingNanos = timeoutNanos - elapsedNanos;

        if (remainingNanos <= 0) {
            return 0;
        }

        return remainingNanos / NANOS_PER_MILLI;
    }

    /**
     * 获取超时时间（毫秒）
     */
    public final long getTimeoutMillis() {
        return timeoutNanos > 0 ? timeoutNanos / NANOS_PER_MILLI : -1;
    }

    /**
     * 是否已超时
     */
    public final boolean isTimedOut() {
        return timedOut.get();
    }

    /**
     * 是否处于暂停状态
     */
    public final boolean isPaused() {
        return paused.get();
    }

    /**
     * 暂停属性（只读）
     */
    public final ReadOnlyBooleanProperty pausedProperty() {
        return pausedProperty.getReadOnlyProperty();
    }

    /**
     * 获取累计暂停时间（毫秒）
     */
    public final long getTotalPauseTimeMillis() {
        return totalPauseNanoTime.get() / NANOS_PER_MILLI;
    }

    /**
     * 更新暂停状态（在UI线程上执行）
     */
    private void updatePausedState(boolean paused) {
        if (Platform.isFxApplicationThread())
            pausedProperty.set(paused);
        else
            Platform.runLater(() -> pausedProperty.set(paused));
    }
}