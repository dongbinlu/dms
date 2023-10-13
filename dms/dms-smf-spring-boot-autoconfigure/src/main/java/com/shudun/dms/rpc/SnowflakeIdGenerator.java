package com.shudun.dms.rpc;

public class SnowflakeIdGenerator {
    private final long workerId;
    private long sequence = 0L;
    private final long workerIdBits = 6L;
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long timestampBits = 41L;
    private final long sequenceBits = 16L;
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long workerId) {
        if (workerId < 0 || workerId > maxWorkerId) {
            throw new IllegalArgumentException("Worker ID must be between 0 and " + maxWorkerId);
        }
        this.workerId = workerId;
    }

    public synchronized long generateUniqueId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id.");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                // Sequence overflow, wait until next millisecond.
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        long id = ((timestamp << (sequenceBits + workerIdBits)) | (workerId << sequenceBits) | sequence);
        return id;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    public static void main(String[] args) {
        System.out.println((-1L ^ (-1L << 6L)));
    }

}
