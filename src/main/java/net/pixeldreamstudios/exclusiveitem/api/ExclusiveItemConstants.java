package net.pixeldreamstudios.exclusiveitem.api;

public final class ExclusiveItemConstants {
    public static final int DEFAULT_COOLDOWN_TICKS = 200;
    public static final int BIND_ANIMATION_DURATION_MS = 4000;
    public static final int CRACK_PHASE_1_DURATION_MS = 1000;
    public static final int CRACK_PHASE_2_DURATION_MS = 1000;
    
    public static final int BIND_PARTICLE_COUNT = 50;
    public static final int SOUL_FLAME_PARTICLE_COUNT = 20;
    public static final int CLAIM_PARTICLE_COUNT = 20;
    
    public static final float BIND_SOUND_VOLUME = 0.4f;
    public static final float BIND_SOUND_PITCH = 0.75f;
    
    public static final String NBT_EXCLUSIVE_ITEM = "ExclusiveItem";
    public static final String NBT_EXCLUSIVE_OWNER = "exclusiveOwner";
    public static final String NBT_EXCLUSIVE_OWNER_NAME = "exclusiveOwnerName";
    public static final String NBT_EXCLUSIVE_ID = "exclusiveID";
    public static final String NBT_ON_USE_BIND = "on_use_bind";
    public static final String NBT_REQUIRED_TAG = "requiredTag";
    public static final String NBT_SHOW_REQUIRED_TAG = "showRequiredTag";

    private ExclusiveItemConstants() {}
}
