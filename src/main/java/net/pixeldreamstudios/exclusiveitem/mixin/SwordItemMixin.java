package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SwordItem.class)
public class SwordItemMixin {

    @Inject(method = "postHit", at = @At("HEAD"), cancellable = true)
    private void restrictAttack(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (attacker instanceof PlayerEntity player && !player.getWorld().isClient) {
            if (!ExclusiveItemUtil.ensureOwnedForUse(stack, player)) {
                player.sendMessage(Text.translatable("exclusiveitem.message.attack_denied").formatted(Formatting.RED), true);
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "canMine", at = @At("HEAD"), cancellable = true)
    private void restrictMine(BlockState state, World world, BlockPos pos, PlayerEntity miner, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = miner.getMainHandStack();

        if (!world.isClient) {
            if (!ExclusiveItemUtil.ensureOwnedForUse(stack, miner)) {
                miner.sendMessage(Text.translatable("exclusiveitem.message.mine_with_tool_denied").formatted(Formatting.RED), true);
                cir.setReturnValue(false);
            }
        }
    }
}
