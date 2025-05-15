package net.pixeldreamstudios.exclusiveitem.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import net.pixeldreamstudios.exclusiveitem.config.ExclusiveItemConfig;

import java.util.List;

public class BookItem extends Item {

    public BookItem(Settings settings) {
        super(settings);
    }

    @Override
    public int getMaxCount() {
        return 1;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity) {
            NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
            NbtCompound nbt = component != null ? component.copyNbt() : new NbtCompound();

            if (!nbt.getBoolean("ExclusiveItem")) {
                nbt.putBoolean("ExclusiveItem", true);
                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        BookItemClient.appendTooltip(stack, context, tooltip, type);
    }



    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient || !ExclusiveItemConfig.INSTANCE.guiEnabled) {
            return super.use(world, user, hand);
        }

        return BookItemClient.use(stack, world, user, hand);
    }
}
