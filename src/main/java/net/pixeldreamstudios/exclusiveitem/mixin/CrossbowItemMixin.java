package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
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

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void restrictCrossbowUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && ExclusiveItemUtil.isExclusiveItem(stack) && !ExclusiveItemUtil.isOwner(stack, user)) {
            user.sendMessage(
                    Text.translatable("exclusiveitem.message.use_crossbow_denied")
                            .formatted(Formatting.RED),
                    true
            );
            cir.setReturnValue(TypedActionResult.fail(stack));
        }
    }
}
