package net.pixeldreamstudios.exclusiveitem.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class ExclusiveItemConfig {
    public class RequiredItem {
        public String id;
        public int count;

        public RequiredItem(String id, int count) {
            this.id = id;
            this.count = count;
        }

        // Needed for GSON
        public RequiredItem() {}
    }
    public boolean guiEnabled = true;
    public int requiredXpLevels = 0;
    public List<RequiredItem> requiredItems = List.of(new RequiredItem("minecraft:nether_star", 1));


    public static final ExclusiveItemConfig INSTANCE = new ExclusiveItemConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/exclusiveitem_config.json");

    public void load() {
        try {
            if (!CONFIG_FILE.exists()) {
                save(); // write default
                return;
            }

            FileReader reader = new FileReader(CONFIG_FILE);
            ExclusiveItemConfig loaded = GSON.fromJson(reader, ExclusiveItemConfig.class);
            if (loaded != null) {
                this.guiEnabled = loaded.guiEnabled;
                this.requiredXpLevels = loaded.requiredXpLevels;
                this.requiredItems = loaded.requiredItems != null ? loaded.requiredItems : this.requiredItems;
            }
        } catch (Exception e) {
            System.err.println("[ExclusiveItem] Failed to load config: " + e.getMessage());
        }
    }

    public void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(CONFIG_FILE);
            GSON.toJson(this, writer);
            writer.close();
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

}
