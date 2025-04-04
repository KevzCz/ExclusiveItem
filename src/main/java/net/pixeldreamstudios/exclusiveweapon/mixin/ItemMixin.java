package net.pixeldreamstudios.exclusiveweapon.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pixeldreamstudios.exclusiveweapon.ExclusiveWeaponUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "postMine", at = @At("HEAD"), cancellable = true)
    private void preventMining(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner, CallbackInfoReturnable<Boolean> cir) {
        if (miner instanceof PlayerEntity player && !world.isClient &&
                ExclusiveWeaponUtil.isExclusiveWeapon(stack) && !ExclusiveWeaponUtil.isOwner(stack, player)) {
            player.sendMessage(Text.literal("You can't mine with this tool."), true);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canMine", at = @At("HEAD"), cancellable = true)
    private void preventCanMine(BlockState state, World world, BlockPos pos, PlayerEntity miner, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = miner.getMainHandStack();
        if (!world.isClient && ExclusiveWeaponUtil.isExclusiveWeapon(stack) && !ExclusiveWeaponUtil.isOwner(stack, miner)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"), cancellable = true)
    private void preventUseRelease(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (user instanceof PlayerEntity player && !world.isClient &&
                ExclusiveWeaponUtil.isExclusiveWeapon(stack) && !ExclusiveWeaponUtil.isOwner(stack, player)) {
            player.sendMessage(Text.literal("You can't use this item."), true);
            ci.cancel();
        }
    }
}

