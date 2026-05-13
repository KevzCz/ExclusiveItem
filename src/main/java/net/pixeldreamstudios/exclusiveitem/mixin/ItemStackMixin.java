package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemCommands;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemStorage;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import net.pixeldreamstudios.exclusiveitem.api.ExclusiveItemEvents;
import net.pixeldreamstudios.exclusiveitem.util.NbtCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.pixeldreamstudios.exclusiveitem.api.ExclusiveItemConstants.NBT_EXCLUSIVE_ID;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "inventoryTick", at = @At("HEAD"))
	private void bindToPlayerOnTick(World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
		ItemStack stack = (ItemStack)(Object)this;
		if (world.isClient || !(entity instanceof PlayerEntity player)) return;

		ExclusiveItemUtil.applyAutoExclusiveRules(stack);

			if (!ExclusiveItemUtil.isExclusiveItem(stack)) return;

			if (!ExclusiveItemCommands.isBypassing(player.getUuid()) &&
					!ExclusiveItemUtil.isOwned(stack) &&
					!ExclusiveItemUtil.shouldBindOnUse(stack)) {
				ExclusiveItemUtil.bindToPlayer(stack, player);
			}

			if (!NbtCache.withNbt(stack, nbt -> nbt.containsUuid(NBT_EXCLUSIVE_ID), false)) {
				NbtCache.modifyNbt(stack, nbt -> nbt.putUuid(NBT_EXCLUSIVE_ID, java.util.UUID.randomUUID()));
			}

			if (player instanceof ServerPlayerEntity serverPlayer) {
				boolean shouldAdd = ExclusiveItemEvents.SHOULD_ADD_TO_STORAGE
						.invoker()
						.shouldAdd(serverPlayer, stack);
				if (shouldAdd) {
					ExclusiveItemStorage.add(serverPlayer, stack);
				}
			}
	}

	@Inject(method = "getTooltip", at = @At("TAIL"))
	private void addTooltip(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
		ItemStack stack = (ItemStack)(Object)this;

		if (ExclusiveItemUtil.isExclusiveItem(stack)) {
			List<Text> tooltip = cir.getReturnValue();

			tooltip.add(Text.literal(""));
			tooltip.add(
					Text.translatable("exclusiveitem.tooltip.exclusive_item")
							.formatted(Formatting.DARK_PURPLE, Formatting.BOLD)
			);
			if (ExclusiveItemUtil.isOwned(stack)) {
				tooltip.add(
						Text.translatable(
								"exclusiveitem.tooltip.bound_to",
								ExclusiveItemUtil.getOwnerName(stack)
						).formatted(Formatting.GRAY, Formatting.GOLD)
				);
			} else {
				if (ExclusiveItemUtil.shouldBindOnUse(stack)) {
					tooltip.add(Text.translatable("exclusiveitem.tooltip.bind_on_use")
							.formatted(Formatting.RED, Formatting.ITALIC));
				} else {
					tooltip.add(Text.translatable("exclusiveitem.tooltip.bind_on_pickup")
							.formatted(Formatting.RED, Formatting.ITALIC));
				}
			}

			String tag = ExclusiveItemUtil.getRequiredTag(stack);
			if (tag != null && !tag.isEmpty() && ExclusiveItemUtil.shouldShowRequiredTag(stack)) {
				tooltip.add(
						Text.translatable("exclusiveitem.tooltip.requires_tag", tag)
								.formatted(Formatting.DARK_AQUA, Formatting.ITALIC)
				);
			}

			tooltip.add(
					Text.translatable("exclusiveitem.tooltip.cannot_be_used")
							.formatted(Formatting.DARK_GRAY, Formatting.ITALIC)
			);
		}
	}
}
