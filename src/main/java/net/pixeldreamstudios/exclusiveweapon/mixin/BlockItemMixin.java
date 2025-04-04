package net.pixeldreamstudios.exclusiveweapon.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.pixeldreamstudios.exclusiveweapon.ExclusiveWeaponUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void preventBlockPlacement(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = context.getPlayer();
        if (player == null || player.getWorld().isClient) return;

        ItemStack stack = context.getStack();

        if (ExclusiveWeaponUtil.isExclusiveWeapon(stack) && !ExclusiveWeaponUtil.isOwner(stack, player)) {
            player.sendMessage(Text.literal("You can't place this block.").formatted(Formatting.RED), true);
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
