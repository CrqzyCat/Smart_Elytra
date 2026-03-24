package me.smart_elytra.smart_elytra.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class Smart_elytraClient implements ClientModInitializer {

    private static KeyBinding elytraKey;

    @Override
    public void onInitializeClient() {
        elytraKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.swap_elytra",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (elytraKey.wasPressed()) {
                handleElytraSwap(client);
            }
        });
    }

    private void handleElytraSwap(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        ItemStack currentChest = client.player.getEquippedStack(EquipmentSlot.CHEST);

        // Elytra → beste Chestplate (einfach feste Reihenfolge)
        if (currentChest.isOf(Items.ELYTRA)) {
            int bestSlot = -1;

            for (int i = 0; i < 36; i++) {
                ItemStack stack = client.player.getInventory().getStack(i);

                if (isChestplate(stack)) {
                    bestSlot = i;
                    break; // erste gefundene nehmen
                }
            }

            if (bestSlot != -1) {
                quickEquip(client, bestSlot);
            }
        }
        // Rüstung → Elytra
        else {
            int elytraSlot = -1;

            for (int i = 0; i < 36; i++) {
                if (client.player.getInventory().getStack(i).isOf(Items.ELYTRA)) {
                    elytraSlot = i;
                    break;
                }
            }

            if (elytraSlot != -1) {
                quickEquip(client, elytraSlot);
            }
        }
    }

    // ✅ KEIN ArmorItem nötig
    private boolean isChestplate(ItemStack stack) {
        return stack.isOf(Items.NETHERITE_CHESTPLATE)
                || stack.isOf(Items.DIAMOND_CHESTPLATE)
                || stack.isOf(Items.IRON_CHESTPLATE)
                || stack.isOf(Items.GOLDEN_CHESTPLATE)
                || stack.isOf(Items.CHAINMAIL_CHESTPLATE)
                || stack.isOf(Items.LEATHER_CHESTPLATE);
    }

    private void quickEquip(MinecraftClient client, int invSlot) {
        int syncSlot = invSlot < 9 ? invSlot + 36 : invSlot;

        client.interactionManager.clickSlot(
                client.player.currentScreenHandler.syncId,
                syncSlot,
                0,
                SlotActionType.QUICK_MOVE,
                client.player
        );
    }
}