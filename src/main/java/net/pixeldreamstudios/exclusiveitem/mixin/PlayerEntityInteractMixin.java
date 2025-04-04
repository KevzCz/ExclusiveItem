package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
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
        ItemStack stack = player.getStackInHand(hand);

        if (!player.getWorld().isClient &&
                ExclusiveItemUtil.isExclusiveItem(stack) &&
                !ExclusiveItemUtil.isOwner(stack, player)) {
            player.sendMessage(Text.literal("This item is exclusive to someone else.").formatted(net.minecraft.util.Formatting.RED), true);
            cir.setReturnValue(ActionResult.FAIL); // Cancels interaction like right-clicking on item frames
        }
    }
}


