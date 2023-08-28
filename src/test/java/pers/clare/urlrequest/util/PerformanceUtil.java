package pers.clare.urlrequest.util;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class PerformanceUtil {

    public static void byCount(long max, Runnable runnable) throws ExecutionException, InterruptedException {
        byCount(Runtime.getRuntime().availableProcessors(), max, runnable);
    }

    public static void byCount(int thread, long max, Runnable runnable) throws ExecutionException, InterruptedException {
        byCondition(thread, (count) -> count <= max, runnable);
    }

    public static void byTime(long ms, Runnable runnable) throws ExecutionException, InterruptedException {
        byTime(Runtime.getRuntime().availableProcessors(), ms, runnable);
    }

    public static void byTime(int thread, long ms, Runnable runnable) throws ExecutionException, InterruptedException {
        long endTime = System.currentTimeMillis() + ms;
        byCondition(thread, (count) -> System.currentTimeMillis() < endTime, runnable);
    }

    public static void byCondition(Function<Long, Boolean> condition, Runnable runnable) throws ExecutionException, InterruptedException {
        byCondition(Runtime.getRuntime().availableProcessors(), condition, runnable);
    }

    public static void byCondition(int thread, Function<Long, Boolean> condition, Runnable runnable) throws ExecutionException, InterruptedException {
        String format = "concurrency: %d, count: %d, took time(ms): %d, %d/s, avg(ms): %d, min: %d, max: %d";
        long startTime = System.currentTimeMillis();
        AtomicLong counter = new AtomicLong();

        AtomicReference<Long> min = new AtomicReference<>(Long.MAX_VALUE);
        AtomicReference<Long> max = new AtomicReference<>(0L);
        Map<Long, AtomicLong> avgMap = new ConcurrentHashMap<>();
        Runnable shutdown = performance(thread, () -> {
            long currentTime;
            long printTime = 0;
            while (condition.apply(counter.incrementAndGet())) {
                currentTime = System.currentTimeMillis();
                if (currentTime > printTime) {
                    printTime = currentTime + 1000;
                    long time = System.currentTimeMillis() - startTime;
                    long c = counter.get();
                    System.out.printf(format + "\r", thread, c, time, rps(c, time), per(c, time), min.get(), max.get());
                }
                currentTime = System.currentTimeMillis();

                runnable.run();
                long t = System.currentTimeMillis() - currentTime;
                if (min.get() > t) {
                    min.set(t);
                } else if (max.get() < t) {
                    max.set(t);
                }
                avgMap.computeIfAbsent(t, (k) -> new AtomicLong()).incrementAndGet();
            }
            counter.decrementAndGet();
            return null;
        });
        shutdown.run();

        long time = System.currentTimeMillis() - startTime;
        long c = counter.get();
        System.out.printf(format + "\n", thread, c, time, rps(c, time), per(c, time), min.get(), max.get());
    }

    public static Runnable performance(int thread, Callable<Void> callable) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(thread);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < thread; i++) {
            tasks.add(callable);
        }
        for (Future<Void> future : executor.invokeAll(tasks)) {
            future.get();
        }
        return executor::shutdown;
    }

    private static long rps(long count, long ms) {
        ms = ms == 0 ? 1 : ms;
        return count * 1000 / ms;
    }


    private static long per(long count, long ms) {
        if (count == 0) return 0;
        return ms * 1000 / count;
    }
}
