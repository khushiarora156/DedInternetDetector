package com.detector.service;

/**
 * Value object capturing comparative performance metrics between sequential and multithreaded analysis.
 */
public class BenchmarkResult {
    private final int accountCount;
    private final int threadCount;
    private final long sequentialDurationMs;
    private final long parallelDurationMs;

    public BenchmarkResult(int accountCount, int threadCount, long sequentialDurationMs, long parallelDurationMs) {
        this.accountCount = accountCount;
        this.threadCount = threadCount;
        this.sequentialDurationMs = Math.max(0, sequentialDurationMs);
        this.parallelDurationMs = Math.max(0, parallelDurationMs);
    }

    public int getAccountCount() {
        return accountCount;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public long getSequentialDurationMs() {
        return sequentialDurationMs;
    }

    public long getParallelDurationMs() {
        return parallelDurationMs;
    }

    /**
     * Calculates speedup factor: sequentialDuration / parallelDuration.
     * Returns 1.0 if parallelDuration is 0.
     *
     * @return speedup factor
     */
    public double getSpeedup() {
        if (parallelDurationMs <= 0) {
            return 1.0;
        }
        return (double) sequentialDurationMs / parallelDurationMs;
    }

    @Override
    public String toString() {
        return String.format(
                "Benchmark[Accounts=%d, Threads=%d, Sequential=%d ms, Parallel=%d ms, Speedup=%.2fx]",
                accountCount, threadCount, sequentialDurationMs, parallelDurationMs, getSpeedup());
    }
}
