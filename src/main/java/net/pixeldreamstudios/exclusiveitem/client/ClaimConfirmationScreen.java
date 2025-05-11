package net.pixeldreamstudios.exclusiveitem.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.pixeldreamstudios.exclusiveitem.config.ExclusiveItemConfig;
import net.pixeldreamstudios.exclusiveitem.network.ClaimExclusiveItemPayload;

import java.util.List;

public class ClaimConfirmationScreen extends Screen {
    private static final Identifier BG_TEXTURE = Identifier.of("exclusive-item", "textures/gui/background.png");

    private final ItemStack itemToClaim;
    private final List<ItemStack> requiredItems = ExclusiveItemConfig.INSTANCE.getRequiredItemStacks();
    private final int requiredXp = ExclusiveItemConfig.INSTANCE.requiredXpLevels;
    private long startTime;

    public ClaimConfirmationScreen(ItemStack item) {
        super(Text.literal("✨ Confirm Claim ✨"));
        this.itemToClaim = item;
    }

    @Override
    protected void init() {
        this.startTime = System.currentTimeMillis();
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.playSound(SoundEvents.UI_TOAST_IN, 1.0f, 1.0f);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int x = (this.width - 324) / 2;
        int y = (this.height - 200) / 2;
        int centerX = this.width / 2;

        context.fill(0, 0, width, height, 0xAA000000);
        context.setShaderColor(1f, 1f, 1f, 1f);
        context.drawTexture(BG_TEXTURE, x, y, 0, 0, 324, 200, 324, 200);

        // Glow border
        int thickness = 2;
        float glowAlpha = 0.45f + 0.15f * MathHelper.sin((System.currentTimeMillis() - startTime) / 200f);
        int glowOuter = applyAlpha(0xFF33CCFF, glowAlpha);
        int glowInner = applyAlpha(0xFF225577, glowAlpha);
        context.fillGradient(x - thickness, y - thickness, x + 324 + thickness, y, glowOuter, glowInner);
        context.fillGradient(x - thickness, y + 200, x + 324 + thickness, y + 200 + thickness, glowInner, glowOuter);
        context.fillGradient(x - thickness, y, x, y + 200, glowOuter, glowInner);
        context.fillGradient(x + 324, y, x + 324 + thickness, y + 200, glowInner, glowOuter);

        context.drawCenteredTextWithShadow(textRenderer, "§b✨ Claim Item ✨", centerX, y + 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "Requirements:", centerX, y + 40, 0xAAAAAA);

        int startX = centerX - (requiredItems.size() * 20) / 2;
        int itemY = y + 60;

        boolean hasItems = true;
        boolean hasXp = true;
        boolean[] hoveringItems = new boolean[requiredItems.size()];

        var player = MinecraftClient.getInstance().player;

        for (int i = 0; i < requiredItems.size(); i++) {
            ItemStack required = requiredItems.get(i);
            int itemX = startX + i * 20;
            boolean hovering = mouseX >= itemX && mouseX <= itemX + 16 && mouseY >= itemY && mouseY <= itemY + 16;
            hoveringItems[i] = hovering;

            if (hovering)
                context.fill(itemX - 2, itemY - 2, itemX + 18, itemY + 18, 0x55FFFFFF);

            context.drawItem(required, itemX, itemY);
            String countStr = required.getCount() > 1 ? "x" + required.getCount() : "";
            if (!countStr.isEmpty()) {
                int countWidth = textRenderer.getWidth(countStr);
                context.drawTextWithShadow(textRenderer, countStr, itemX + 8 - countWidth / 2, itemY - 12, 0xFFFFFF);
            }

            if (player != null && !player.getInventory().contains(required)) {
                hasItems = false;
            }
        }

        if (requiredXp > 0 && player != null) {
            int xpY = itemY + 22;
            String xpText = "XP Levels: " + player.experienceLevel + " / " + requiredXp;
            int color = player.experienceLevel >= requiredXp ? 0x55FF55 : 0xFF5555;
            context.drawCenteredTextWithShadow(textRenderer, xpText, centerX, xpY, color);

            if (player.experienceLevel < requiredXp) {
                hasXp = false;
            }
        }

        context.drawCenteredTextWithShadow(textRenderer, "Item to Claim:", centerX, y + 110, 0xAAAAAA);
        int claimItemX = centerX - 8;
        int claimItemY = y + 130;
        boolean hoveringClaim = mouseX >= claimItemX && mouseX <= claimItemX + 16 &&
                mouseY >= claimItemY && mouseY <= claimItemY + 16;

        if (hoveringClaim)
            context.fill(claimItemX - 2, claimItemY - 2, claimItemX + 18, claimItemY + 18, 0x55FFFFFF);

        float time = (System.currentTimeMillis() - startTime) / 1000f;
        float bob = MathHelper.sin(time * 2f) * 2f;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(claimItemX + 8, claimItemY + 8 - bob, 150);
        matrices.scale(1.75f, 1.75f, 1.75f);
        context.drawItem(itemToClaim, -8, -8);
        matrices.pop();

        // Tooltips
        for (int i = 0; i < requiredItems.size(); i++) {
            if (hoveringItems[i]) {
                context.drawItemTooltip(textRenderer, requiredItems.get(i), mouseX, mouseY);
            }
        }

        if (hoveringClaim)
            context.drawItemTooltip(textRenderer, itemToClaim, mouseX, mouseY);

        // Buttons
        int btnWidth = 80;
        int btnHeight = 20;
        int spacing = 20;
        int btnY = y + 170;

        boolean hoverCancel = mouseX >= centerX - btnWidth - spacing && mouseX <= centerX - spacing &&
                mouseY >= btnY && mouseY <= btnY + btnHeight;
        context.fill(centerX - btnWidth - spacing, btnY, centerX - spacing, btnY + btnHeight, hoverCancel ? 0xFF666666 : 0xFF444444);
        context.drawCenteredTextWithShadow(textRenderer, "Cancel", centerX - btnWidth / 2 - spacing, btnY + 6, 0xFFFFFF);

        boolean hoverClaim = mouseX >= centerX + spacing && mouseX <= centerX + btnWidth + spacing &&
                mouseY >= btnY && mouseY <= btnY + btnHeight;

        boolean canClaim = hasItems && hasXp;
        int claimBtnColor = canClaim
                ? (hoverClaim ? 0xFF66BB66 : 0xFF449944)
                : 0xFF555555;

        context.fill(centerX + spacing, btnY, centerX + btnWidth + spacing, btnY + btnHeight, claimBtnColor);
        context.drawCenteredTextWithShadow(textRenderer, canClaim ? "§aClaim" : "§7Claim", centerX + btnWidth / 2 + spacing, btnY + 6, 0xFFFFFF);

        if (hoverClaim && !canClaim) {
            List<Text> tooltip = new java.util.ArrayList<>();
            tooltip.add(Text.literal("§cMissing Requirements:"));

            // XP check
            int requiredXp = ExclusiveItemConfig.INSTANCE.requiredXpLevels;
            int currentXp = MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.experienceLevel : 0;
            if (currentXp < requiredXp) {
                tooltip.add(Text.literal(" - §b" + requiredXp + " XP§r (You have " + currentXp + ")"));
            }

            // Items check
            if (MinecraftClient.getInstance().player != null) {
                for (ItemStack required : ExclusiveItemConfig.INSTANCE.getRequiredItemStacks()) {
                    int found = 0;
                    for (ItemStack stack : MinecraftClient.getInstance().player.getInventory().main) {
                        if (ItemStack.areItemsAndComponentsEqual(stack, required)) {
                            found += stack.getCount();
                        }
                    }
                    if (found < required.getCount()) {
                        tooltip.add(Text.literal(" - §b" + required.getCount() + "x " + required.getName().getString() +
                                "§r (You have " + found + ")"));
                    }
                }
            }

            context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
        }

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = width / 2;
        int btnWidth = 80;
        int btnHeight = 20;
        int spacing = 20;
        int btnY = (this.height - 200) / 2 + 170;

        if (mouseX >= centerX - btnWidth - spacing && mouseX <= centerX - spacing &&
                mouseY >= btnY && mouseY <= btnY + btnHeight) {
            MinecraftClient.getInstance().setScreen(new ReclaimScreen());
            return true;
        }

        if (mouseX >= centerX + spacing && mouseX <= centerX + btnWidth + spacing &&
                mouseY >= btnY && mouseY <= btnY + btnHeight) {

            var player = MinecraftClient.getInstance().player;
            if (player == null) return true;

            // Validate XP
            if (player.experienceLevel < requiredXp) return true;

            // Validate required items
            for (ItemStack req : requiredItems) {
                if (!player.getInventory().contains(req)) return true;
            }

            DynamicRegistryManager registryManager = MinecraftClient.getInstance().getNetworkHandler().getRegistryManager();
            RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, registryManager);

            ItemStack oneCopy = itemToClaim.copy();
            NbtElement encoded = ItemStack.CODEC.encodeStart(ops, oneCopy).result().orElse(null);

            if (!(encoded instanceof NbtCompound itemNbt)) {
                System.err.println("[ExclusiveItem] Failed to encode item stack.");
                return true;
            }

            ClientPlayNetworking.send(new ClaimExclusiveItemPayload(itemNbt));
            MinecraftClient.getInstance().setScreen(null);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int applyAlpha(int color, float alpha) {
        int a = (int) (alpha * 255) & 0xFF;
        return (a << 24) | (color & 0x00FFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
