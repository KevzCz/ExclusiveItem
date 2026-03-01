package net.pixeldreamstudios.exclusiveitem.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class NbtCache {
    
    private NbtCache() {}

    @Nullable
    public static NbtCompound getNbt(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        return component != null ? component.copyNbt() : null;
    }

    public static Optional<NbtCompound> getNbtOptional(ItemStack stack) {
        return Optional.ofNullable(getNbt(stack));
    }

    public static NbtCompound getOrCreateNbt(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        return component != null ? component.copyNbt() : new NbtCompound();
    }

    public static void setNbt(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    public static <T> T withNbt(ItemStack stack, NbtReader<T> reader, T defaultValue) {
        NbtCompound nbt = getNbt(stack);
        if (nbt == null) return defaultValue;
        return reader.read(nbt);
    }

    public static void modifyNbt(ItemStack stack, NbtModifier modifier) {
        NbtCompound nbt = getOrCreateNbt(stack);
        modifier.modify(nbt);
        setNbt(stack, nbt);
    }

    @FunctionalInterface
    public interface NbtReader<T> {
        T read(NbtCompound nbt);
    }

    @FunctionalInterface
    public interface NbtModifier {
        void modify(NbtCompound nbt);
    }
}
