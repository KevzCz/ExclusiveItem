package net.pixeldreamstudios.exclusiveitem.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemMod;

public class ModItemGroups {

    public static ItemGroup EXCLUSIVE_GROUP;

    public static void registerItemGroups() {
        EXCLUSIVE_GROUP = Registry.register(
                Registries.ITEM_GROUP,
                Identifier.of(ExclusiveItemMod.MOD_ID, "exclusive_group"),
                FabricItemGroup.builder()
                        .displayName(Text.translatable("itemGroup.exclusive-item.exclusive_group"))
                        .icon(() -> new ItemStack(ModItems.BOOK_ITEM))
                        .entries((displayContext, entries) -> {
                            entries.add(new ItemStack(ModItems.BOOK_ITEM));
                        })
                        .build()
        );
    }
}
