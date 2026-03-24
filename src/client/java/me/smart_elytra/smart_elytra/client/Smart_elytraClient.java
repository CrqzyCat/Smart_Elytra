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

    // Variablen für den verzögerten Swap
    private int swapStep = 0;
    private int targetInvSlot = -1;
    private final int armorChestSlot = 6;

    @Override
    public void onInitializeClient() {
        elytraKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.swap_elytra",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Start des Swaps
            while (elytraKey.wasPressed() && swapStep == 0) {
                prepareSwap(client);
            }

            // Die 3-Tick Logik
            if (swapStep > 0) {
                processSwap(client);
            }
        });
    }

    private void prepareSwap(MinecraftClient client) {
        ItemStack currentChest = client.player.getEquippedStack(EquipmentSlot.CHEST);
        int foundSlot = -1;

        if (currentChest.isOf(Items.ELYTRA)) {
            for (int i = 0; i < 36; i++) {
                if (isChestplate(client.player.getInventory().getStack(i))) {
                    foundSlot = i;
                    break;
                }
            }
        } else {
            for (int i = 0; i < 36; i++) {
                if (client.player.getInventory().getStack(i).isOf(Items.ELYTRA)) {
                    foundSlot = i;
                    break;
                }
            }
        }

        if (foundSlot != -1) {
            // Mappe Hotbar (0-8) auf ScreenHandler Slots (36-44)
            this.targetInvSlot = foundSlot < 9 ? foundSlot + 36 : foundSlot;
            this.swapStep = 1; // Starte den Prozess
        }
    }

    private void processSwap(MinecraftClient client) {
        if (client.interactionManager == null || client.player == null) {
            swapStep = 0;
            return;
        }

        int syncId = client.player.currentScreenHandler.syncId;

        switch (swapStep) {
            case 1: // Tick 1: Item aus Inventar auf den Cursor nehmen
                client.interactionManager.clickSlot(syncId, targetInvSlot, 0, SlotActionType.PICKUP, client.player);
                swapStep = 2;
                break;

            case 2: // Tick 2: Item in den Rüstungsslot legen (tauscht mit altem Item)
                client.interactionManager.clickSlot(syncId, armorChestSlot, 0, SlotActionType.PICKUP, client.player);
                swapStep = 3;
                break;

            case 3: // Tick 3: Altes Item zurück ins Inventar legen
                client.interactionManager.clickSlot(syncId, targetInvSlot, 0, SlotActionType.PICKUP, client.player);
                swapStep = 0; // Fertig
                targetInvSlot = -1;
                break;
        }
    }

    private boolean isChestplate(ItemStack stack) {
        return stack.isOf(Items.NETHERITE_CHESTPLATE) || stack.isOf(Items.DIAMOND_CHESTPLATE) ||
                stack.isOf(Items.IRON_CHESTPLATE) || stack.isOf(Items.GOLDEN_CHESTPLATE) ||
                stack.isOf(Items.CHAINMAIL_CHESTPLATE) || stack.isOf(Items.LEATHER_CHESTPLATE);
    }
}