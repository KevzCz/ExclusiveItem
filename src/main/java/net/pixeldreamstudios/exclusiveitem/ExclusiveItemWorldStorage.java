package net.pixeldreamstudios.exclusiveitem;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.*;

public class ExclusiveItemWorldStorage extends PersistentState {
    private final Map<UUID, NbtList> playerData = new HashMap<>();

    public static final String NAME = "exclusive_items";

    public static final PersistentState.Type<ExclusiveItemWorldStorage> TYPE =
            new PersistentState.Type<>(
                    ExclusiveItemWorldStorage::new,
                    ExclusiveItemWorldStorage::fromNbt,
                    null
            );


    public static ExclusiveItemWorldStorage get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE, NAME);
    }



    public static ExclusiveItemWorldStorage fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        ExclusiveItemWorldStorage state = new ExclusiveItemWorldStorage();
        for (String key : nbt.getKeys()) {
            try {
                UUID playerId = UUID.fromString(key);
                state.playerData.put(playerId, nbt.getList(key, NbtElement.COMPOUND_TYPE));
            } catch (IllegalArgumentException e) {

            }
        }
        return state;
    }



    public List<ItemStack> getStacks(RegistryWrapper.WrapperLookup registry, UUID playerId) {
        NbtList list = playerData.getOrDefault(playerId, new NbtList());
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            NbtCompound stackNbt = list.getCompound(i);
            ItemStack.fromNbt(registry, stackNbt).ifPresent(result::add);
        }
        return result;
    }


    public void setStacks(RegistryWrapper.WrapperLookup registry, UUID playerId, List<ItemStack> stacks) {
        NbtList list = new NbtList();
        for (ItemStack stack : stacks) {
            try {
                NbtElement encoded = stack.encode(registry);
                if (encoded instanceof NbtCompound compound) {
                    list.add(compound);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        playerData.put(playerId, list);
        markDirty();
    }


    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        for (Map.Entry<UUID, NbtList> entry : playerData.entrySet()) {
            nbt.put(entry.getKey().toString(), entry.getValue());
        }
        return nbt;
    }
}
