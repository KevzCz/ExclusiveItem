package net.pixeldreamstudios.exclusiveitem.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.internals.target.SpellTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpellHelper.class)
public class SpellHelperMixin {
    @Inject(method = "performSpell", at = @At("HEAD"), cancellable = true)
    private static void restrictExclusiveSpellUse(
            net.minecraft.world.World world,
            PlayerEntity player,
            RegistryEntry<Spell> spell,
            SpellTarget.SearchResult target,
            SpellCast.Action action,
            float progress,
            CallbackInfo ci) {

        if (!world.isClient) {
            ItemStack stack = player.getMainHandStack();
            if (!ExclusiveItemUtil.ensureOwnedForUse(stack, player)) {
                player.sendMessage(
                        Text.translatable("exclusiveitem.message.not_eligible_spell")
                                .formatted(Formatting.RED),
                        true
                );
                ci.cancel();
            }
        }
    }
}
