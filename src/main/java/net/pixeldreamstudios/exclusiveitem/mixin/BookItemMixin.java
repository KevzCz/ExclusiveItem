package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.item.WritableBookItem;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({WritableBookItem.class, WrittenBookItem.class})
public class BookItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void preventUseIfNotOwner(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && ExclusiveItemUtil.isExclusiveItem(stack) && !ExclusiveItemUtil.isOwner(stack, user)) {
            user.sendMessage(Text.literal("§c§lThis book does not recognize you."), true);
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
