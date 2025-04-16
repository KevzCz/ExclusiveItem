package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemCommands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "inventoryTick", at = @At("HEAD"))
	private void bindToPlayerOnTick(World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
		ItemStack stack = (ItemStack)(Object)this;
		if (!world.isClient && entity instanceof PlayerEntity player) {
			if (!ExclusiveItemCommands.isBypassing(player.getUuid()) &&
					ExclusiveItemUtil.isExclusiveItem(stack) &&
					!ExclusiveItemUtil.isOwned(stack)) {

				ExclusiveItemUtil.bindToPlayer(stack, player);

				world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.4f, 0.75f);
				world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);

				((ServerWorld) world).spawnParticles(ParticleTypes.ENCHANT,
						player.getX(), player.getY() + 1, player.getZ(),
						50, 0.5, 0.5, 0.5, 0.1);

				((ServerWorld) world).spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
						player.getX(), player.getY() + 1, player.getZ(),
						20, 0.3, 0.3, 0.3, 0.01);

				player.sendMessage(Text.literal("§6⚔ The Item binds to your soul...").formatted(Formatting.GOLD), true);
			}
		}
	}

	@Inject(method = "getTooltip", at = @At("TAIL"))
	private void addTooltip(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
		ItemStack stack = (ItemStack)(Object)this;

		if (ExclusiveItemUtil.isExclusiveItem(stack)) {
			List<Text> tooltip = cir.getReturnValue();

			tooltip.add(Text.literal(""));
			tooltip.add(Text.literal("⚔ Exclusive Item").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));

			if (ExclusiveItemUtil.isOwned(stack)) {
				tooltip.add(Text.literal("Bound to: ")
						.append(Text.literal(ExclusiveItemUtil.getOwnerName(stack))
								.formatted(Formatting.GOLD))
						.formatted(Formatting.GRAY));
			} else {
				tooltip.add(Text.literal("Unbound").formatted(Formatting.RED, Formatting.ITALIC));
			}

			String tag = ExclusiveItemUtil.getRequiredTag(stack);
			if (tag != null && !tag.isEmpty() && ExclusiveItemUtil.shouldShowRequiredTag(stack)) {
				tooltip.add(Text.literal("Requires Tag: " + tag)
						.formatted(Formatting.DARK_AQUA, Formatting.ITALIC));
			}


			tooltip.add(Text.literal("Cannot be used by others.").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
		}
	}
}
