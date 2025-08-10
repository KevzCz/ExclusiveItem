package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import net.pixeldreamstudios.exclusiveitem.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "postMine", at = @At("HEAD"), cancellable = true)
    private void preventMining(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner, CallbackInfoReturnable<Boolean> cir) {
        if (miner instanceof PlayerEntity player && !world.isClient) {
            if (!ExclusiveItemUtil.ensureOwnedForUse(stack, player)) {
                player.sendMessage(Text.translatable("exclusiveitem.message.mine_with_tool_denied").formatted(Formatting.RED), true);
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void preventExclusiveUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (world.isClient) return;

        ItemStack stack = user.getStackInHand(hand);
        if (!ExclusiveItemUtil.ensureOwnedForUse(stack, user)) {
            if (stack.contains(DataComponentTypes.FOOD)) {
                user.sendMessage(Text.translatable("exclusiveitem.message.eat_denied").formatted(Formatting.RED), true);
            } else if (stack.isOf(ModItems.BOOK_ITEM)) {
                user.sendMessage(Text.translatable("exclusiveitem.message.thalrin_accord_denied").formatted(Formatting.RED), true);
            } else {
                user.sendMessage(Text.translatable("exclusiveitem.message.generic_use_denied").formatted(Formatting.RED), true);
            }
            cir.setReturnValue(TypedActionResult.fail(stack));
        }
    }

    @Inject(method = "canMine", at = @At("HEAD"), cancellable = true)
    private void preventCanMine(BlockState state, World world, BlockPos pos, PlayerEntity miner, CallbackInfoReturnable<Boolean> cir) {
        if (world.isClient) return;

        ItemStack stack = miner.getMainHandStack();
        if (!ExclusiveItemUtil.ensureOwnedForUse(stack, miner)) {
            miner.sendMessage(Text.translatable("exclusiveitem.message.mine_with_tool_denied").formatted(Formatting.RED), true);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"), cancellable = true)
    private void preventUseRelease(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (world.isClient || !(user instanceof PlayerEntity player)) return;

        if (!ExclusiveItemUtil.ensureOwnedForUse(stack, player)) {
            player.sendMessage(Text.translatable("exclusiveitem.message.use_release_denied").formatted(Formatting.RED), true);
            ci.cancel();
        }
    }
}
