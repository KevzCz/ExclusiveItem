package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityInteractMixin {

    @Inject(
            method = "interact(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventRightClick(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (player.getWorld().isClient) return;

        ItemStack stack = player.getStackInHand(hand);

        if (!ExclusiveItemUtil.ensureOwnedForUse(stack, player)) {
            player.sendMessage(
                    Text.translatable("exclusiveitem.message.interact_denied").formatted(Formatting.RED),
                    true
            );
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
