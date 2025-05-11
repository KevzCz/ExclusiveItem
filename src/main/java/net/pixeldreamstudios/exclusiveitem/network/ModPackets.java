package net.pixeldreamstudios.exclusiveitem.network;

import net.minecraft.util.Identifier;
import static net.pixeldreamstudios.exclusiveitem.ExclusiveItemMod.MOD_ID;

public class ModPackets {
    public static final Identifier REQUEST_EXCLUSIVE_ITEMS = Identifier.of(MOD_ID, "request_exclusive_items");
}
