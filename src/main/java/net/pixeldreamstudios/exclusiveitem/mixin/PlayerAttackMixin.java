package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerAttackMixin {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void preventExclusiveWeaponAttack(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack stack = player.getMainHandStack();

        if (!player.getWorld().isClient &&
                ExclusiveItemUtil.isExclusiveWeapon(stack) &&
                !ExclusiveItemUtil.isOwner(stack, player)) {
            player.sendMessage(Text.literal("You can't attack with this weapon.").formatted(net.minecraft.util.Formatting.RED), true);
            ci.cancel(); // Cancel the attack
        }
    }
}
