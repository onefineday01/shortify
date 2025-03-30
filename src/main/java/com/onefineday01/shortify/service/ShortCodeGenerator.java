package com.onefineday01.shortify.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ShortCodeGenerator {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final long EPOCH = 1711497600000L; // March 27, 2025, 00:00:00 UTC in ms
    private static final int TIMESTAMP_BITS = 26;    // ~2 years
    private static final int MACHINE_ID_BITS = 4;    // 16 machines max
    private static final int SEQUENCE_BITS = 12;     // 4096 IDs per ms
    private static final int CODE_LENGTH = 7;

    private final long machineId; // 0-15
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public ShortCodeGenerator(@Value("${shortify.machine-id:0}") long machineId) {
        if (machineId < 0 || machineId > 15) {
            throw new IllegalArgumentException("Machine ID must be between 0 and 15");
        }
        this.machineId = machineId;
    }

    public synchronized String generateShortCode() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & 4095; // Max 4096 (2^12 - 1)
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;

        long id = ((timestamp - EPOCH) << (MACHINE_ID_BITS + SEQUENCE_BITS)) // 26 bits timestamp
                | (machineId << SEQUENCE_BITS)                               // 4 bits machine ID
                | sequence;                                                  // 12 bits sequence

        return toBase62(id);
    }

    private long waitNextMillis(long last) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= last) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    private String toBase62(long id) {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(BASE62_CHARS.charAt((int) (id % 62)));
            id /= 62;
        } while (id > 0);
        while (sb.length() < CODE_LENGTH) {
            sb.append('0'); // Pad with zeros to ensure 7 chars
        }
        return sb.reverse().toString();
    }
}