package net.pixeldreamstudios.exclusiveitem.item;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemMod;

public class ModItems {

    public static Item BOOK_ITEM;

    public static void registerItems() {
        BOOK_ITEM = registerItem("book", new BookItem(new Item.Settings().rarity(Rarity.EPIC).maxCount(1)));
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(ExclusiveItemMod.MOD_ID, name), item);
    }
}
