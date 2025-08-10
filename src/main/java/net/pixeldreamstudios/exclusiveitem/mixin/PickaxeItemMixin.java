package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PickaxeItem.class)
public class PickaxeItemMixin {

    @Inject(method = "canMine", at = @At("HEAD"), cancellable = true)
    private void ei$restrictMine(BlockState state, World world, BlockPos pos, PlayerEntity miner, CallbackInfoReturnable<Boolean> cir) {
        if (world.isClient) return;

        ItemStack stack = miner.getMainHandStack();
        if (!ExclusiveItemUtil.ensureOwnedForUse(stack, miner)) {
            miner.sendMessage(
                    Text.translatable("exclusiveitem.message.mine_with_tool_denied").formatted(Formatting.RED),
                    true
            );
            cir.setReturnValue(false);
        }
    }
}
