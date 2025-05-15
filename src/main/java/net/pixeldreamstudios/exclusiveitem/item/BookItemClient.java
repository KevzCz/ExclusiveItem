package net.pixeldreamstudios.exclusiveitem.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.item.tooltip.TooltipType;

import net.pixeldreamstudios.exclusiveitem.client.ReclaimScreen;
import net.pixeldreamstudios.exclusiveitem.network.ModPackets;
import net.pixeldreamstudios.exclusiveitem.network.RequestExclusiveItemsPayload;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class BookItemClient {

    public static void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("§7Obeys only to the bound one."));
        tooltip.add(Text.literal("§8Will bind to you upon pickup."));

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && !ExclusiveItemUtil.isOwner(stack, player)) {
            tooltip.add(Text.literal("§c§lWho are you?"));
        }
    }

    public static TypedActionResult<ItemStack> use(ItemStack stack, World world, PlayerEntity user, Hand hand) {

        if (!ExclusiveItemUtil.isOwner(stack, user)) {
            return TypedActionResult.fail(stack);
        }

        if (!ClientPlayNetworking.canSend(ModPackets.REQUEST_EXCLUSIVE_ITEMS)) {
            return TypedActionResult.fail(stack);
        }

        MinecraftClient.getInstance().setScreen(new ReclaimScreen());
        ClientPlayNetworking.send(new RequestExclusiveItemsPayload());

        return TypedActionResult.success(stack);
    }
}

