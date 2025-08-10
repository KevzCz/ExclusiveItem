package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderPearlItem.class)
public class EnderPearlItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void restrictUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (world.isClient) return;

        ItemStack stack = user.getStackInHand(hand);
        if (!ExclusiveItemUtil.ensureOwnedForUse(stack, user)) {
            user.sendMessage(Text.translatable("exclusiveitem.message.use_pearl_denied").formatted(Formatting.RED), true);
            cir.setReturnValue(TypedActionResult.fail(stack));
        }
    }
}
