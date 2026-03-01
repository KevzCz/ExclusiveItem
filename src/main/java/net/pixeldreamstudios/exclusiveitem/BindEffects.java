package net.pixeldreamstudios.exclusiveitem;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.pixeldreamstudios.exclusiveitem.api.ExclusiveItemConstants.*;

public final class BindEffects {
    private static final Map<UUID, Long> lastTickForPlayer = new ConcurrentHashMap<>();

    private BindEffects() {}

    public static void play(ServerWorld world, ServerPlayerEntity player) {
        long serverTick = world.getServer().getTicks();
        UUID id = player.getUuid();

        Long prev = lastTickForPlayer.put(id, serverTick);
        if (prev != null && prev == serverTick) {
            return;
        }

        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, BIND_SOUND_VOLUME, BIND_SOUND_PITCH);
        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);

        world.spawnParticles(ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1, player.getZ(),
                BIND_PARTICLE_COUNT, 0.5, 0.5, 0.5, 0.1);

        world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY() + 1, player.getZ(),
                SOUL_FLAME_PARTICLE_COUNT, 0.3, 0.3, 0.3, 0.01);

        player.sendMessage(
                Text.translatable("exclusiveitem.message.bind_soul").formatted(Formatting.RED),
                true
        );
    }
}
