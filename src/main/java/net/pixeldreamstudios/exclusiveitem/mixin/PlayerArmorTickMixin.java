package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerArmorTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void checkExclusiveArmor(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getWorld().isClient) return;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack stack = player.getEquippedStack(slot);
                if (stack.isEmpty()) continue;

                if (!ExclusiveItemUtil.ensureOwnedForUse(stack, player)) {
                    player.sendMessage(
                            Text.translatable("exclusiveitem.message.armor_reject").formatted(Formatting.RED),
                            true
                    );
                    if (!player.getInventory().insertStack(stack)) {
                        player.dropItem(stack, true);
                    }
                    player.equipStack(slot, ItemStack.EMPTY);
                }
            }
        }
    }
}
