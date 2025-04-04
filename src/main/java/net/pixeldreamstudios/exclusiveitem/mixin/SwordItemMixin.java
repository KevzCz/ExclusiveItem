package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SwordItem.class)
public class SwordItemMixin {

    @Inject(method = "postHit", at = @At("HEAD"), cancellable = true)
    private void restrictAttack(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (attacker instanceof PlayerEntity player && !player.getWorld().isClient &&
                ExclusiveItemUtil.isExclusiveWeapon(stack) && !ExclusiveItemUtil.isOwner(stack, player)) {
            player.sendMessage(Text.literal("You can't use this sword!").formatted(Formatting.RED), true);
            cir.setReturnValue(false);
        }
    }
}

