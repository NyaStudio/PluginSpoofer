package cn.nekopixel.pluginspoofer.utils;

import net.kyori.adventure.text.event.ClickEvent;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public final class UnknownCommandRewriteTracker {
    private static final Map<UUID, Queue<RewriteEntry>> PENDING = new ConcurrentHashMap<>();
    private static final AtomicLong MARKER_SEQUENCE = new AtomicLong();
    private static final long ENTRY_TTL_MILLIS = 10_000L;
    private static final int MAX_PENDING_PER_PLAYER = 16;
    private static final boolean RESPONSE_REWRITE_SUPPORTED = detectResponseRewriteSupport();

    private UnknownCommandRewriteTracker() {
    }

    public static boolean isResponseRewriteSupported() {
        return RESPONSE_REWRITE_SUPPORTED;
    }

    public static String register(UUID playerId, String originalCommandLabel) {
        String marker = "__pluginspoofer_blocked_" + Long.toHexString(MARKER_SEQUENCE.incrementAndGet());
        if (playerId == null) {
            return marker;
        }

        String normalizedOriginalLabel = normalizeOriginalLabel(originalCommandLabel);
        Queue<RewriteEntry> entries = PENDING.computeIfAbsent(playerId, ignored -> new ConcurrentLinkedQueue<>());
        entries.offer(new RewriteEntry(marker, normalizedOriginalLabel, System.currentTimeMillis() + ENTRY_TTL_MILLIS));

        while (entries.size() > MAX_PENDING_PER_PLAYER) {
            entries.poll();
        }

        return marker;
    }

    public static String replaceIfTracked(UUID playerId, String content) {
        if (playerId == null || content == null || content.isEmpty()) {
            return content;
        }

        Queue<RewriteEntry> entries = PENDING.get(playerId);
        if (entries == null || entries.isEmpty()) {
            return content;
        }

        long now = System.currentTimeMillis();
        int attempts = entries.size();
        boolean replaced = false;
        String rewrittenContent = content;

        for (int i = 0; i < attempts; i++) {
            RewriteEntry entry = entries.poll();
            if (entry == null) {
                break;
            }

            if (entry.expireAt <= now) {
                continue;
            }

            if (!replaced && rewrittenContent.contains(entry.marker)) {
                rewrittenContent = rewrittenContent.replace(entry.marker, entry.originalLabel);
                replaced = true;
                continue;
            }

            entries.offer(entry);
        }

        if (entries.isEmpty()) {
            PENDING.remove(playerId, entries);
        }

        return rewrittenContent;
    }

    private static String normalizeOriginalLabel(String originalCommandLabel) {
        if (originalCommandLabel == null || originalCommandLabel.isEmpty()) {
            return "pluginspoofer_blocked";
        }

        String normalized = originalCommandLabel.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        return normalized.isEmpty() ? "pluginspoofer_blocked" : normalized;
    }

    private static boolean detectResponseRewriteSupport() {
        try {
            ClickEvent.Action.class.getMethod("payloadType");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static final class RewriteEntry {
        private final String marker;
        private final String originalLabel;
        private final long expireAt;

        private RewriteEntry(String marker, String originalLabel, long expireAt) {
            this.marker = marker;
            this.originalLabel = originalLabel;
            this.expireAt = expireAt;
        }
    }
}
