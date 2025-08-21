package net.pixeldreamstudios.exclusiveitem.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class ExclusiveItemConfig {

    public static final ExclusiveItemConfig INSTANCE = new ExclusiveItemConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/exclusiveitem_config.json");

    public static final class RequiredItem {
        public String id;
        public int count;

        public RequiredItem(String id, int count) {
            this.id = id;
            this.count = count;
        }

        public RequiredItem() {}
    }

    public static final class AutoExclusiveEntry {
        public String item;
        public String tag;
        public boolean bindOnUse = true;

        public AutoExclusiveEntry() {}

        public static AutoExclusiveEntry forItem(String id, boolean bindOnUse) {
            AutoExclusiveEntry e = new AutoExclusiveEntry();
            e.item = id;
            e.bindOnUse = bindOnUse;
            return e;
        }

        public static AutoExclusiveEntry forTag(String tagId, boolean bindOnUse) {
            AutoExclusiveEntry e = new AutoExclusiveEntry();
            e.tag = tagId;
            e.bindOnUse = bindOnUse;
            return e;
        }
    }

    public int schemaVersion = 1;

    public boolean guiEnabled = true;
    public int requiredXpLevels = 0;

    public boolean autoExclusiveEnabled = false;

    public List<RequiredItem> requiredItems = defaultRequiredItems();
    public List<AutoExclusiveEntry> autoExclusive = defaultAutoExclusive();

    public void load() {
        try {
            if (!CONFIG_FILE.exists()) {
                save();
                return;
            }

            JsonObject root;
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                root = parsed != null && parsed.isJsonObject() ? parsed.getAsJsonObject() : new JsonObject();
            }

            ExclusiveItemConfig loaded;
            try (FileReader reader2 = new FileReader(CONFIG_FILE)) {
                loaded = GSON.fromJson(reader2, ExclusiveItemConfig.class);
            }

            if (loaded == null) {
                save();
                return;
            }

            boolean changed = false;

            if (!root.has("schemaVersion")) {
                loaded.schemaVersion = 1;
                changed = true;
            }

            if (!root.has("guiEnabled")) {
                loaded.guiEnabled = true;
                changed = true;
            }
            if (!root.has("requiredXpLevels")) {
                loaded.requiredXpLevels = 0;
                changed = true;
            }

            if (!root.has("autoExclusiveEnabled")) {
                loaded.autoExclusiveEnabled = false;
                changed = true;
            }

            List<RequiredItem> sanitizedReq = new ArrayList<>();
            if (!root.has("requiredItems") || !root.get("requiredItems").isJsonArray()) {
                sanitizedReq = defaultRequiredItems();
                changed = true;
            } else {
                JsonArray arr = root.getAsJsonArray("requiredItems");
                List<RequiredItem> source = loaded.requiredItems != null ? loaded.requiredItems : new ArrayList<>();
                for (int i = 0; i < source.size(); i++) {
                    RequiredItem ri = source.get(i);
                    if (ri == null) continue;
                    String id = ri.id;
                    int count = Math.max(1, ri.count);
                    if (id == null || id.isBlank()) continue;
                    sanitizedReq.add(new RequiredItem(id, count));
                    if (ri.count != count) changed = true;
                }
                if (sanitizedReq.isEmpty()) {
                    sanitizedReq = defaultRequiredItems();
                    changed = true;
                }
            }
            loaded.requiredItems = sanitizedReq;

            if (!root.has("autoExclusive") || !root.get("autoExclusive").isJsonArray()) {
                loaded.autoExclusive = defaultAutoExclusive();
                changed = true;
            } else {
                JsonArray arr = root.getAsJsonArray("autoExclusive");
                List<AutoExclusiveEntry> fixed = new ArrayList<>();

                List<AutoExclusiveEntry> source = loaded.autoExclusive != null ? loaded.autoExclusive : new ArrayList<>();

                for (int i = 0; i < source.size(); i++) {
                    AutoExclusiveEntry e = source.get(i);
                    if (e == null) {
                        changed = true;
                        continue;
                    }

                    boolean hasBindInJson = false;
                    if (i < arr.size() && arr.get(i).isJsonObject()) {
                        JsonObject eo = arr.get(i).getAsJsonObject();
                        hasBindInJson = eo.has("bindOnUse");
                    }

                    String item = e.item != null && !e.item.isBlank() ? e.item : null;
                    String tag = e.tag != null && !e.tag.isBlank() ? e.tag : null;

                    if (item != null && tag != null) {
                        tag = null;
                        changed = true;
                    }
                    if (item == null && tag == null) {
                        changed = true;
                        continue;
                    }

                    boolean bind = e.bindOnUse;
                    if (!hasBindInJson) {
                        bind = true;
                        changed = true;
                    }

                    if (item != null) {
                        Identifier id = Identifier.tryParse(item);
                        if (id == null) {
                            changed = true;
                            continue;
                        }
                        fixed.add(AutoExclusiveEntry.forItem(id.toString(), bind));
                    } else {
                        Identifier tagId = Identifier.tryParse(tag);
                        if (tagId == null) {
                            changed = true;
                            continue;
                        }
                        fixed.add(AutoExclusiveEntry.forTag(tagId.toString(), bind));
                    }
                }

                if (fixed.isEmpty()) {
                    fixed = defaultAutoExclusive();
                    changed = true;
                }

                loaded.autoExclusive = fixed;
            }

            this.schemaVersion = loaded.schemaVersion;
            this.guiEnabled = loaded.guiEnabled;
            this.requiredXpLevels = loaded.requiredXpLevels;
            this.autoExclusiveEnabled = loaded.autoExclusiveEnabled;
            this.requiredItems = loaded.requiredItems;
            this.autoExclusive = loaded.autoExclusive;

            if (changed) save();

        } catch (Exception e) {
            System.err.println("[ExclusiveItem] Failed to load config: " + e.getMessage());
        }
    }

    public void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            System.err.println("[ExclusiveItem] Failed to save config: " + e.getMessage());
        }
    }

    public List<ItemStack> getRequiredItemStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (RequiredItem entry : requiredItems) {
            Identifier id = Identifier.tryParse(entry.id);
            if (id != null && Registries.ITEM.containsId(id)) {
                Item item = Registries.ITEM.get(id);
                stacks.add(new ItemStack(item, Math.max(1, entry.count)));
            }
        }
        return stacks;
    }

    public AutoExclusiveEntry match(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (id != null) {
            for (AutoExclusiveEntry e : autoExclusive) {
                if (e != null && e.item != null) {
                    Identifier want = Identifier.tryParse(e.item);
                    if (want != null && want.equals(id)) {
                        return e;
                    }
                }
            }
        }

        for (AutoExclusiveEntry e : autoExclusive) {
            if (e != null && e.tag != null) {
                Identifier tagId = Identifier.tryParse(e.tag);
                if (tagId != null) {
                    TagKey<Item> key = TagKey.of(RegistryKeys.ITEM, tagId);
                    if (stack.isIn(key)) {
                        return e;
                    }
                }
            }
        }
        return null;
    }

    private static List<RequiredItem> defaultRequiredItems() {
        List<RequiredItem> list = new ArrayList<>();
        list.add(new RequiredItem("minecraft:nether_star", 1));
        return list;
    }

    private static List<AutoExclusiveEntry> defaultAutoExclusive() {
        List<AutoExclusiveEntry> list = new ArrayList<>();
        list.add(AutoExclusiveEntry.forItem("minecraft:nether_star", false));
        return list;
    }
}
