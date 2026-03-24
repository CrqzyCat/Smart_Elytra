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
        // Keybinding im MISC-Stil registrieren
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

        // Index 6 ist der Chest-Slot im PlayerScreenHandler
        int armorChestSlot = 6;
        ItemStack currentChest = client.player.getEquippedStack(EquipmentSlot.CHEST);

        // Fall A: Elytra ist an -> Suche beste Chestplate
        if (currentChest.isOf(Items.ELYTRA)) {
            int bestSlot = -1;
            for (int i = 0; i < 36; i++) {
                if (isChestplate(client.player.getInventory().getStack(i))) {
                    bestSlot = i;
                    break;
                }
            }
            if (bestSlot != -1) {
                executeSwap(client, bestSlot, armorChestSlot);
            }
        }
        // Fall B: Chestplate ist an -> Suche Elytra
        else {
            int elytraSlot = -1;
            for (int i = 0; i < 36; i++) {
                if (client.player.getInventory().getStack(i).isOf(Items.ELYTRA)) {
                    elytraSlot = i;
                    break;
                }
            }
            if (elytraSlot != -1) {
                executeSwap(client, elytraSlot, armorChestSlot);
            }
        }
    }

    private void executeSwap(MinecraftClient client, int invSlot, int armorSlot) {
        // Mapping für Hotbar vs. Inventar
        int syncInvSlot = invSlot < 9 ? invSlot + 36 : invSlot;

        // 1. Klick: Item aus dem Inventar auf den Cursor nehmen
        client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, syncInvSlot, 0, SlotActionType.PICKUP, client.player);

        // 2. Klick: Auf den Rüstungsslot klicken (tauscht Cursor-Item mit angelegtem Item)
        client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, armorSlot, 0, SlotActionType.PICKUP, client.player);

        // 3. Klick: Das alte Item (jetzt am Cursor) zurück in den nun leeren Inventar-Slot legen
        client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, syncInvSlot, 0, SlotActionType.PICKUP, client.player);
    }

    private boolean isChestplate(ItemStack stack) {
        return stack.isOf(Items.NETHERITE_CHESTPLATE)
                || stack.isOf(Items.DIAMOND_CHESTPLATE)
                || stack.isOf(Items.IRON_CHESTPLATE)
                || stack.isOf(Items.GOLDEN_CHESTPLATE)
                || stack.isOf(Items.CHAINMAIL_CHESTPLATE)
                || stack.isOf(Items.LEATHER_CHESTPLATE);
    }
}