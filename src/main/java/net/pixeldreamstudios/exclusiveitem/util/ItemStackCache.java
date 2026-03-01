package net.pixeldreamstudios.exclusiveitem.util;

import net.minecraft.item.ItemStack;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ItemStackCache {
    
    private static final Map<Integer, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 100;

    private ItemStackCache() {}

    public static <T> T getCached(ItemStack stack, String key, CacheFunction<T> function) {
        int stackHashCode = System.identityHashCode(stack);
        long now = System.currentTimeMillis();
        
        CacheEntry entry = cache.get(stackHashCode);
        
        if (entry != null && entry.isValid(now)) {
            ItemStack cachedStack = entry.stackRef.get();
            if (cachedStack == stack) {
                @SuppressWarnings("unchecked")
                T result = (T) entry.values.get(key);
                if (result != null) {
                    return result;
                }
            }
        }
        
        T result = function.compute(stack);
        
        if (entry == null) {
            entry = new CacheEntry(stack, now);
            cache.put(stackHashCode, entry);
        }
        
        entry.values.put(key, result);
        entry.timestamp = now;
        
        cleanupOldEntries(now);
        
        return result;
    }

    public static void invalidate(ItemStack stack) {
        cache.remove(System.identityHashCode(stack));
    }

    public static void clear() {
        cache.clear();
    }

    private static void cleanupOldEntries(long now) {
        if (cache.size() > 1000) {
            cache.entrySet().removeIf(entry -> 
                !entry.getValue().isValid(now) || entry.getValue().stackRef.get() == null
            );
        }
    }

    @FunctionalInterface
    public interface CacheFunction<T> {
        T compute(ItemStack stack);
    }

    private static class CacheEntry {
        final WeakReference<ItemStack> stackRef;
        final Map<String, Object> values;
        long timestamp;

        CacheEntry(ItemStack stack, long timestamp) {
            this.stackRef = new WeakReference<>(stack);
            this.values = new ConcurrentHashMap<>();
            this.timestamp = timestamp;
        }

        boolean isValid(long now) {
            return (now - timestamp) < CACHE_DURATION_MS;
        }
    }
}
