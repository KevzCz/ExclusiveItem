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
        if (miner instanceof PlayerEntity player && !world.isClient &&
                ExclusiveItemUtil.isExclusiveItem(stack) && !ExclusiveItemUtil.isOwner(stack, player)) {
            player.sendMessage(Text.literal("You can't mine with this tool.").formatted(Formatting.RED), true);
            cir.setReturnValue(false);
        }
    }
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void preventUsingExclusiveItems(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient &&
                ExclusiveItemUtil.isExclusiveItem(stack) &&
                !ExclusiveItemUtil.isOwner(stack, user)) {

            if (stack.contains(DataComponentTypes.FOOD)) {
                user.sendMessage(Text.literal("§cYou can't eat this!"), true);
            } else if (stack.isOf(ModItems.BOOK_ITEM)) {
                user.sendMessage(Text.literal("§cThal'rin's Accord must be bound to you..."), true);
            } else {
                user.sendMessage(Text.literal("§cYou can't use this item."), true);
            }

            cir.setReturnValue(TypedActionResult.fail(stack));
        }
    }



    @Inject(method = "canMine", at = @At("HEAD"), cancellable = true)
    private void preventCanMine(BlockState state, World world, BlockPos pos, PlayerEntity miner, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = miner.getMainHandStack();
        if (!world.isClient && ExclusiveItemUtil.isExclusiveItem(stack) && !ExclusiveItemUtil.isOwner(stack, miner)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void preventGenericUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && ExclusiveItemUtil.isExclusiveItem(stack) && !ExclusiveItemUtil.isOwner(stack, user)) {
            if (stack.isOf(ModItems.BOOK_ITEM)) {
                user.sendMessage(Text.literal("§cThal'rin's Accord must be bound to you..."), true);
            } else {
                user.sendMessage(Text.literal("§cYou can't use this item."), true);
            }
            cir.setReturnValue(TypedActionResult.fail(stack));
        }
    }


    @Inject(method = "onStoppedUsing", at = @At("HEAD"), cancellable = true)
    private void preventUseRelease(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (user instanceof PlayerEntity player && !world.isClient &&
                ExclusiveItemUtil.isExclusiveItem(stack) && !ExclusiveItemUtil.isOwner(stack, player)) {
            player.sendMessage(Text.literal("You can't use this item.").formatted(Formatting.RED), true);
            ci.cancel();
        }
    }
}
