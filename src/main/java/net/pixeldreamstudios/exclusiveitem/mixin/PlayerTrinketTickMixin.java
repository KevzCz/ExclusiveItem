package net.pixeldreamstudios.exclusiveitem.mixin;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(PlayerEntity.class)
public class PlayerTrinketTickMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void checkExclusiveTrinkets(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getWorld().isClient) return;

        TrinketsApi.getTrinketComponent(player).ifPresent((TrinketComponent component) -> {
            Iterator<Pair<SlotReference, ItemStack>> it = component.getAllEquipped().iterator();
            while (it.hasNext()) {
                Pair<SlotReference, ItemStack> pair = it.next();
                SlotReference slotRef = pair.getLeft();
                ItemStack stack = pair.getRight();

                if (stack.isEmpty()) continue;

                if (!ExclusiveItemUtil.ensureOwnedForUse(stack, player)) {
                    player.sendMessage(
                            Text.translatable("exclusiveitem.message.trinket_reject").formatted(Formatting.RED),
                            true
                    );

                    if (!player.getInventory().insertStack(stack)) {
                        player.dropItem(stack, true);
                    }

                    TrinketInventory inv = slotRef.inventory();
                    inv.setStack(slotRef.index(), ItemStack.EMPTY);
                    inv.markDirty();
                    inv.markUpdate();
                }
            }
        });
    }
}
