package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
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

@Mixin(PotionItem.class)
public class PotionItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void restrictDrinkPotion(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!world.isClient) {
            ItemStack stack = user.getStackInHand(hand);
            if (!ExclusiveItemUtil.ensureOwnedForUse(stack, user)) {
                user.sendMessage(
                        Text.translatable("exclusiveitem.message.drink_potion_denied")
                                .formatted(Formatting.RED),
                        true
                );
                cir.setReturnValue(TypedActionResult.fail(stack));
            }
        }
    }
}
