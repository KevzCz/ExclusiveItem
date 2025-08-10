package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
public class AxeItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void ei$restrictUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = context.getPlayer();
        if (player == null || player.getWorld().isClient) return;

        ItemStack stack = context.getStack();
        if (!ExclusiveItemUtil.ensureOwnedForUse(stack, player)) {
            player.sendMessage(Text.translatable("exclusiveitem.message.mine_with_tool_denied").formatted(Formatting.RED), true);
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "canMine", at = @At("HEAD"), cancellable = true)
    private void ei$restrictMine(BlockState state, World world, BlockPos pos, PlayerEntity miner, CallbackInfoReturnable<Boolean> cir) {
        if (world.isClient) return;

        ItemStack stack = miner.getMainHandStack();
        if (!ExclusiveItemUtil.ensureOwnedForUse(stack, miner)) {
            miner.sendMessage(Text.translatable("exclusiveitem.message.mine_with_tool_denied").formatted(Formatting.RED), true);
            cir.setReturnValue(false);
        }
    }
}
