package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.casting.SpellCast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin_CastBlock {

    @Inject(method = "startSpellCast", at = @At("HEAD"), cancellable = true)
    private void restrictExclusiveSpellCast(ItemStack itemStack, RegistryEntry<Spell> spellEntry, CallbackInfoReturnable<SpellCast.Attempt> cir) {
        if (net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil.isExclusiveItem(itemStack)
                && !net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil.isOwner(itemStack, (ClientPlayerEntity) (Object) this)) {

            ((ClientPlayerEntity) (Object) this).sendMessage(
                    net.minecraft.text.Text.literal("You can't cast spells with this item!")
                            .formatted(net.minecraft.util.Formatting.RED),
                    true
            );
            cir.setReturnValue(SpellCast.Attempt.none()); // Cancel the spell cast attempt
        }
    }
}
