package net.pixeldreamstudios.exclusiveitem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.exclusiveitem.api.ExclusiveItemEvents;
import net.pixeldreamstudios.exclusiveitem.api.BindingResult;
import net.pixeldreamstudios.exclusiveitem.config.ExclusiveItemConfig;
import net.pixeldreamstudios.exclusiveitem.util.NbtCache;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.pixeldreamstudios.exclusiveitem.api.ExclusiveItemConstants.*;

public class ExclusiveItemUtil {

    public static boolean isExclusiveItem(ItemStack stack) {
        return NbtCache.withNbt(stack, nbt -> nbt.getBoolean(NBT_EXCLUSIVE_ITEM), false);
    }

    public static boolean isOwned(ItemStack stack) {
        return NbtCache.withNbt(stack, nbt -> nbt.contains(NBT_EXCLUSIVE_OWNER), false);
    }

    public static boolean shouldBindOnUse(ItemStack stack) {
        return NbtCache.withNbt(stack, nbt -> nbt.contains(NBT_ON_USE_BIND) && nbt.getBoolean(NBT_ON_USE_BIND), false);
    }

    public static boolean ensureOwnedForUse(ItemStack stack, PlayerEntity player) {
        if (!isExclusiveItem(stack)) return true;
        if (isOwner(stack, player)) return true;

        if (shouldBindOnUse(stack)) {
            bindToPlayer(stack, player);
            return isOwner(stack, player);
        }
        return false;
    }

    public static void setBindOnUse(ItemStack stack, boolean value) {
        NbtCache.modifyNbt(stack, nbt -> nbt.putBoolean(NBT_ON_USE_BIND, value));
    }

    public static void bindToPlayer(ItemStack stack, PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }

        if (!ExclusiveItemEvents.SHOULD_ALLOW_BINDING.invoker().shouldAllow(serverPlayer, stack)) {
            ExclusiveItemEvents.ON_BINDING_ATTEMPT_FAILED.invoker().onFailed(
                serverPlayer, stack, BindingResult.BindingFailureReason.EVENT_CANCELLED
            );
            return;
        }

        boolean[] newlyBound = {false};
        
        NbtCache.modifyNbt(stack, nbt -> {
            if (!nbt.contains(NBT_EXCLUSIVE_OWNER)) {
                nbt.putUuid(NBT_EXCLUSIVE_OWNER, player.getUuid());
                nbt.putString(NBT_EXCLUSIVE_OWNER_NAME, player.getName().getString());
                newlyBound[0] = true;
            }

            if (!nbt.containsUuid(NBT_EXCLUSIVE_ID)) {
                nbt.putUuid(NBT_EXCLUSIVE_ID, UUID.randomUUID());
            }
        });

        if (isOwner(stack, player)) {
            ExclusiveItemEvents.ON_ITEM_BOUND.invoker().onBound(serverPlayer, stack, newlyBound[0]);
            
            boolean shouldAdd = ExclusiveItemEvents.SHOULD_ADD_TO_STORAGE
                    .invoker()
                    .shouldAdd(serverPlayer, stack);

            if (shouldAdd) {
                ExclusiveItemStorage.add(serverPlayer, stack);
            }

            if (newlyBound[0]) {
                ServerWorld sw = serverPlayer.getServerWorld();
                BindEffects.play(sw, serverPlayer);
            }
        }
    }


    @Nullable
    public static UUID getExclusiveID(ItemStack stack) {
        return NbtCache.withNbt(stack, nbt -> 
            nbt.containsUuid(NBT_EXCLUSIVE_ID) ? nbt.getUuid(NBT_EXCLUSIVE_ID) : null, null
        );
    }

    public static boolean isOwner(ItemStack stack, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer && ExclusiveItemCommands.isBypassing(serverPlayer)) {
            return true;
        }

        return NbtCache.withNbt(stack, nbt -> {
            if (!nbt.containsUuid(NBT_EXCLUSIVE_OWNER) || !player.getUuid().equals(nbt.getUuid(NBT_EXCLUSIVE_OWNER))) {
                return false;
            }

            if (nbt.contains(NBT_REQUIRED_TAG)) {
                String requiredTag = nbt.getString(NBT_REQUIRED_TAG);
                return player.getCommandTags().contains(requiredTag);
            }

            return true;
        }, false);
    }

    public static boolean shouldShowRequiredTag(ItemStack stack) {
        return NbtCache.withNbt(stack, nbt -> 
            !nbt.contains(NBT_SHOW_REQUIRED_TAG) || nbt.getBoolean(NBT_SHOW_REQUIRED_TAG), true
        );
    }

    public static String getOwnerName(ItemStack stack) {
        return NbtCache.withNbt(stack, nbt -> nbt.getString(NBT_EXCLUSIVE_OWNER_NAME), "Nobody");
    }

    public static String getRequiredTag(ItemStack stack) {
        return NbtCache.withNbt(stack, nbt -> 
            nbt.contains(NBT_REQUIRED_TAG) ? nbt.getString(NBT_REQUIRED_TAG) : null, null
        );
    }

    public static ItemStack createExclusiveItemOwnedBySomebodyElse(Item item) {
        ItemStack stack = new ItemStack(item);

        NbtCache.modifyNbt(stack, nbt -> {
            nbt.putBoolean(NBT_EXCLUSIVE_ITEM, true);
            UUID fakeOwnerUUID = UUID.nameUUIDFromBytes("SomebodyElse".getBytes());
            nbt.putUuid(NBT_EXCLUSIVE_OWNER, fakeOwnerUUID);
            nbt.putString(NBT_EXCLUSIVE_OWNER_NAME, "SomebodyElse");
            nbt.putString(NBT_REQUIRED_TAG, "TagRequirement");
        });
        
        return stack;
    }

    public static void applyAutoExclusiveRules(ItemStack stack) {
        if (!ExclusiveItemConfig.INSTANCE.autoExclusiveEnabled) return;

        ExclusiveItemConfig.AutoExclusiveEntry match = ExclusiveItemConfig.INSTANCE.match(stack);
        if (match == null) return;

        NbtCache.modifyNbt(stack, nbt -> {
            if (!nbt.getBoolean(NBT_EXCLUSIVE_ITEM)) {
                nbt.putBoolean(NBT_EXCLUSIVE_ITEM, true);
            }

            if (!nbt.contains(NBT_ON_USE_BIND) || nbt.getBoolean(NBT_ON_USE_BIND) != match.bindOnUse) {
                nbt.putBoolean(NBT_ON_USE_BIND, match.bindOnUse);
            }
        });
    }
}
