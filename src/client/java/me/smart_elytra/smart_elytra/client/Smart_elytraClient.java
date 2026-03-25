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
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

/**
 * This class handles the client-side logic for the Smart Elytra mod.
 * It allows the player to swap the Elytra with a chestplate from the hotbar with a single key press.
 */
public class Smart_elytraClient implements ClientModInitializer {

    private static KeyBinding elytraKey;

    private int swapStep = 0;
    private int originalSlot = -1;
    private int targetHotbarSlot = -1;

    @Override
    public void onInitializeClient() {
        elytraKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Swap Elytra and Chestplate",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Start trigger for the swap
            while (elytraKey.wasPressed() && swapStep == 0) {
                startHotbarSwap(client);
            }

            // 3-tick sequence for the swap
            if (swapStep > 0) {
                tickHotbarSwap(client);
            }
        });
    }

    /**
     * Starts the hotbar swap process.
     * It searches for an Elytra or a chestplate in the hotbar and initiates the swap if found.
     * @param client The Minecraft client instance.
     */
    private void startHotbarSwap(MinecraftClient client) {
        ItemStack currentChest = client.player.getEquippedStack(EquipmentSlot.CHEST);
        int foundSlot = -1;

        // Search only in the hotbar (0-8)
        if (currentChest.isOf(Items.ELYTRA)) {
            for (int i = 0; i < 9; i++) {
                if (isChestplate(client.player.getInventory().getStack(i))) {
                    foundSlot = i;
                    break;
                }
            }
        } else {
            for (int i = 0; i < 9; i++) {
                if (client.player.getInventory().getStack(i).isOf(Items.ELYTRA)) {
                    foundSlot = i;
                    break;
                }
            }
        }

        if (foundSlot != -1) {
            this.originalSlot = client.player.getInventory().getSelectedSlot();
            this.targetHotbarSlot = foundSlot;
            this.swapStep = 1;
        }
    }

    /**
     * Executes the 3-tick hotbar swap sequence.
     * Tick 1: Selects the hotbar slot with the item to swap.
     * Tick 2: Right-clicks to equip the item.
     * Tick 3: Selects the original hotbar slot.
     * @param client The Minecraft client instance.
     */
    private void tickHotbarSwap(MinecraftClient client) {
        switch (swapStep) {
            case 1: // Tick 1: Go to the slot
                client.player.getInventory().setSelectedSlot(targetHotbarSlot);
                swapStep = 2;
                break;

            case 2: // Tick 2: Right-click to equip
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                swapStep = 3;
                break;

            case 3: // Tick 3: Go back to the old slot
                client.player.getInventory().setSelectedSlot(originalSlot);
                // Reset
                swapStep = 0;
                originalSlot = -1;
                targetHotbarSlot = -1;
                break;
        }
    }

    /**
     * Checks if the given ItemStack is a chestplate.
     * @param stack The ItemStack to check.
     * @return True if the ItemStack is a chestplate, false otherwise.
     */
    private boolean isChestplate(ItemStack stack) {
        return stack.isOf(Items.NETHERITE_CHESTPLATE) || stack.isOf(Items.DIAMOND_CHESTPLATE) ||
                stack.isOf(Items.IRON_CHESTPLATE) || stack.isOf(Items.GOLDEN_CHESTPLATE) ||
                stack.isOf(Items.CHAINMAIL_CHESTPLATE) || stack.isOf(Items.LEATHER_CHESTPLATE);
    }
}