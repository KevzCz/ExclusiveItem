package net.pixeldreamstudios.exclusiveitem.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

import java.util.List;

public class ReclaimScreen extends Screen {
    private static final Identifier BG_TEXTURE = Identifier.of("exclusive-item", "textures/gui/background.png");
    private static final Identifier BACK_BUTTON = Identifier.of("exclusive-item", "textures/gui/back.png");
    private static final Identifier NEXT_BUTTON = Identifier.of("exclusive-item", "textures/gui/next.png");
    private final boolean isGravityGlitch = Math.random() < 0.05;

    private final Vector3f[] itemPositions = new Vector3f[12];

    private static final int TEXTURE_WIDTH = 324;
    private static final int TEXTURE_HEIGHT = 200;

    private float glowAlpha = 0.4f;
    private boolean increasing = true;
    private final long startTime = System.currentTimeMillis();

    private int currentPage = 0;

    private final Vector3f[] rotationAxes = new Vector3f[12];
    private final float[] angularSpeeds = new float[12];
    private final float[] tiltOffsets = new float[12];
    private boolean initializedOffsets = false;
    private final float[] itemVY = new float[12];
    private final float[] itemVX = new float[12];

    public ReclaimScreen() {
        super(Text.literal("Reclaim Exclusive Items"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (increasing) glowAlpha = Math.min(glowAlpha + delta * 0.015f, 0.6f);
        else glowAlpha = Math.max(glowAlpha - delta * 0.015f, 0.3f);
        increasing = !(glowAlpha >= 0.6f) && (glowAlpha <= 0.3f);

        int x = (this.width - TEXTURE_WIDTH) / 2;
        int y = (this.height - TEXTURE_HEIGHT) / 2;

        context.fill(0, 0, width, height, 0xAA000000);
        context.setShaderColor(1f, 1f, 1f, 0.95f);
        context.drawTexture(BG_TEXTURE, x, y, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        context.setShaderColor(1f, 1f, 1f, 1f);

        int thickness = 2;
        int glowOuter = applyAlpha(0xFF33CCFF, glowAlpha);
        int glowInner = applyAlpha(0xFF225577, glowAlpha);
        context.fillGradient(x - thickness, y - thickness, x + TEXTURE_WIDTH + thickness, y, glowOuter, glowInner);
        context.fillGradient(x - thickness, y + TEXTURE_HEIGHT, x + TEXTURE_WIDTH + thickness, y + TEXTURE_HEIGHT + thickness, glowInner, glowOuter);
        context.fillGradient(x - thickness, y, x, y + TEXTURE_HEIGHT, glowOuter, glowInner);
        context.fillGradient(x + TEXTURE_WIDTH, y, x + TEXTURE_WIDTH + thickness, y + TEXTURE_HEIGHT, glowInner, glowOuter);

        List<ItemStack> full = ClientExclusiveItemStorage.get();
        int itemsPerPage = 12;
        int totalPages = Math.max(1, (int) Math.ceil(full.size() / (float) itemsPerPage));
        currentPage = MathHelper.clamp(currentPage, 0, totalPages - 1);

        int from = Math.min(currentPage * itemsPerPage, full.size());
        int to = Math.min((currentPage + 1) * itemsPerPage, full.size());
        List<ItemStack> items = full.subList(from, to);

        if (!initializedOffsets) {
            for (int i = 0; i < rotationAxes.length; i++) {
                float xAxis = (float) (Math.random() * 2 - 1);
                float yAxis = (float) (Math.random() * 2 - 1);
                float zAxis = (float) (Math.random() * 2 - 1);
                Vector3f axis = new Vector3f(xAxis, yAxis, zAxis).normalize();

                rotationAxes[i] = axis;
                angularSpeeds[i] = (float) (Math.random() * 0.025f + 0.0125f);
                tiltOffsets[i] = (float) (Math.random() * Math.PI * 2);

                if (isGravityGlitch) {
                    int col = i % 4;
                    int row = i / 4;
                    int gridX = x + col * (TEXTURE_WIDTH / 4) + 16;
                    int gridY = y + row * (TEXTURE_HEIGHT / 3) + 16;

                    itemPositions[i] = new Vector3f(gridX, gridY, 0);
                    itemVY[i] = (float) (-Math.random() * 3);
                    itemVX[i] = (float) ((Math.random() - 0.5f) * 1.5f);

                }
            }

            initializedOffsets = true;
        }

        int cols = 4;
        int rows = 3;
        int cellW = TEXTURE_WIDTH / cols;
        int cellH = TEXTURE_HEIGHT / rows;

        long time = System.currentTimeMillis() % 100000;

        ItemStack hoveredItem = null;
        int hoveredX = 0, hoveredY = 0, hoveredW = 0, hoveredH = 0;

        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (index >= items.size()) break;
                ItemStack item = items.get(index);
                int cellX = x + col * cellW;
                int cellY = y + row * cellH;
                int right = cellX + (col == cols - 1 ? TEXTURE_WIDTH - (cellW * (cols - 1)) : cellW);
                int bottom = cellY + (row == rows - 1 ? TEXTURE_HEIGHT - (cellH * (rows - 1)) : cellH);
                boolean isHovered = mouseX >= cellX && mouseX < right && mouseY >= cellY && mouseY < bottom;

                int renderX = x + col * cellW + (cellW - 16) / 2;
                int renderY = y + row * cellH + (cellH - 16) / 2;

                if (isGravityGlitch) {
                    Vector3f pos = itemPositions[index];
                    float vy = itemVY[index];
                    float vx = itemVX[index];
                    float gravity = 0.45f;
                    vy += gravity;
                    pos.x += vx;
                    vx *= 0.98f;
                    if (Math.abs(vx) < 0.01f) vx = 0;
                    float rightBound = x + TEXTURE_WIDTH - 16;
                    if (pos.x < (float) x) {
                        pos.x = (float) x;
                        vx *= -0.5f;
                    } else if (pos.x > rightBound) {
                        pos.x = rightBound;
                        vx *= -0.5f;
                    }
                    float newY = pos.y + vy;
                    float maxY = y + TEXTURE_HEIGHT - 18;
                    float friction = 0.75f;
                    if (newY > maxY) {
                        newY = maxY;
                        vy *= -friction;
                        if (Math.abs(vy) < 0.5f) vy = 0;
                    }
                    for (int j = 0; j < index; j++) {
                        if (itemPositions[j] == null) continue;
                        Vector3f other = itemPositions[j];

                        if (Math.abs(pos.x - other.x) < 16 && Math.abs(newY - other.y) < 16) {
                            float top = other.y - 16;
                            if (newY > top) {
                                newY = top;
                                vy *= -friction;
                                if (Math.abs(vy) < 0.5f) vy = 0;
                            }
                        }
                    }
                    pos.y = newY;
                    itemVY[index] = vy;
                    itemVX[index] = vx;



                    renderFloatingItem(item, context, (int) pos.x, (int) pos.y, delta, time / 16.0f,
                            rotationAxes[index], angularSpeeds[index], tiltOffsets[index], isHovered);
                } else {
                    renderFloatingItem(item, context, renderX, renderY, delta, time / 16.0f,
                            rotationAxes[index], angularSpeeds[index], tiltOffsets[index], isHovered);
                }


                if (isHovered) {
                    hoveredItem = item;
                    hoveredX = cellX;
                    hoveredY = cellY;
                    hoveredW = right - cellX;
                    hoveredH = bottom - cellY;
                }

                index++;
            }
        }
        if (isGravityGlitch) {
            context.drawCenteredTextWithShadow(textRenderer, "§oOops... they fell again", width / 2, y - 10, 0xAAAAFF);
        }
        int btnW = 30, btnH = 30;
        int leftX = x - btnW - 5;
        int rightX = x + TEXTURE_WIDTH + 5;
        int btnY = y + TEXTURE_HEIGHT / 2 - btnH / 2;
        float scalePulse = 1.0f + 0.05f * MathHelper.sin((System.currentTimeMillis() - startTime) / 150.0f);

        if (currentPage > 0) {
            boolean hoverLeft = mouseX >= leftX && mouseX < leftX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
            context.setShaderColor(1f, 1f, 1f, hoverLeft ? 1f : 0.7f);
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(leftX + btnW / 2f, btnY + btnH / 2f, 0);
            matrices.scale(scalePulse, scalePulse, 1f);
            matrices.translate(-btnW / 2f, -btnH / 2f, 0);
            context.drawTexture(BACK_BUTTON, 0, 0, 0, 0, btnW, btnH, btnW, btnH);
            matrices.pop();
            context.setShaderColor(1f, 1f, 1f, 1f);
        }

        if (currentPage < totalPages - 1) {
            boolean hoverRight = mouseX >= rightX && mouseX < rightX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
            context.setShaderColor(1f, 1f, 1f, hoverRight ? 1f : 0.7f);
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(rightX + btnW / 2f, btnY + btnH / 2f, 0);
            matrices.scale(scalePulse, scalePulse, 1f);
            matrices.translate(-btnW / 2f, -btnH / 2f, 0);
            context.drawTexture(NEXT_BUTTON, 0, 0, 0, 0, btnW, btnH, btnW, btnH);
            matrices.pop();
            context.setShaderColor(1f, 1f, 1f, 1f);
        }


        if (hoveredItem != null) {
            context.fill(hoveredX, hoveredY, hoveredX + hoveredW, hoveredY + hoveredH, 0x55FFFFFF);
            context.drawTooltip(
                    textRenderer,
                    hoveredItem.getTooltip(
                            new Item.TooltipContext() {
                                @Override public RegistryWrapper.WrapperLookup getRegistryLookup() { return null; }
                                @Override public float getUpdateTickRate() { return 0; }
                                @Override public MapState getMapState(MapIdComponent mapIdComponent) { return null; }
                            },
                            MinecraftClient.getInstance().player,
                            MinecraftClient.getInstance().options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC
                    ),
                    mouseX, mouseY
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (this.width - TEXTURE_WIDTH) / 2;
        int y = (this.height - TEXTURE_HEIGHT) / 2;
        int btnW = 25, btnH = 25;
        int leftX = x - btnW - 5;
        int rightX = x + TEXTURE_WIDTH + 5;
        int btnY = y + TEXTURE_HEIGHT / 2 - btnH / 2;

        if (currentPage > 0 && mouseX >= leftX && mouseX < leftX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
            currentPage = Math.max(0, currentPage - 1);
            initializedOffsets = false;
            return true;
        }

        int totalPages = (int) Math.ceil(ClientExclusiveItemStorage.get().size() / 12.0);
        if (currentPage < totalPages - 1 && mouseX >= rightX && mouseX < rightX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
            currentPage = Math.min(totalPages - 1, currentPage + 1);
            initializedOffsets = false;
            return true;
        }
        List<ItemStack> full = ClientExclusiveItemStorage.get();
        int itemsPerPage = 12;
        int from = Math.min(currentPage * itemsPerPage, full.size());
        int to = Math.min((currentPage + 1) * itemsPerPage, full.size());
        List<ItemStack> items = full.subList(from, to);

        int cols = 4;
        int rows = 3;
        int cellW = TEXTURE_WIDTH / cols;
        int cellH = TEXTURE_HEIGHT / rows;
        int startX = (this.width - TEXTURE_WIDTH) / 2;
        int startY = (this.height - TEXTURE_HEIGHT) / 2;

        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (index >= items.size()) break;

                int cellX = startX + col * cellW;
                int cellY = startY + row * cellH;
                int right = cellX + (col == cols - 1 ? TEXTURE_WIDTH - (cellW * (cols - 1)) : cellW);
                int bottom = cellY + (row == rows - 1 ? TEXTURE_HEIGHT - (cellH * (rows - 1)) : cellH);

                if (mouseX >= cellX && mouseX < right && mouseY >= cellY && mouseY < bottom) {
                    ItemStack clickedItem = items.get(index);
                    MinecraftClient.getInstance().setScreen(new ClaimConfirmationScreen(clickedItem.copy()));
                    return true;
                }

                index++;
            }
        }


        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderFloatingItem(ItemStack stack, DrawContext context, int x, int y, float tickDelta, float time,
                                    Vector3f axis, float speed, float bobOffset, boolean isHovered) {
        MinecraftClient client = MinecraftClient.getInstance();
        ItemRenderer renderer = client.getItemRenderer();
        MatrixStack matrices = context.getMatrices();

        matrices.push();
        matrices.translate(x + 8, y + 8, 150);
        matrices.scale(24.0f, -24.0f, 24.0f);

        if (isHovered) {
            float bobbing = MathHelper.sin((time + tickDelta + bobOffset) / 12.0f) * 0.05f;
            matrices.translate(0, -bobbing, 0);
        }

        float angle = (time * speed * 60f) % 360f;
        matrices.multiply(RotationAxis.of(axis).rotationDegrees(angle));

        renderer.renderItem(stack, net.minecraft.client.render.model.json.ModelTransformationMode.GUI,
                15728880, OverlayTexture.DEFAULT_UV, matrices, context.getVertexConsumers(), null, 0);
        matrices.pop();
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
