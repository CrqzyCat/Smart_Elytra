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
 * The swap is performed over a 3-tick sequence to ensure reliability on servers.
 */
public class Smart_elytraClient implements ClientModInitializer {

    /**
     * The keybinding that triggers the Elytra swap.
     */
    private static KeyBinding elytraKey;

    /**
     * The current step in the 3-tick swap sequence.
     * 0: Inactive
     * 1: Select target slot
     * 2: Use item (equip)
     * 3: Select original slot
     */
    private int swapStep = 0;
    /**
     * The player's selected hotbar slot before the swap started.
     */
    private int originalSlot = -1;
    /**
     * The hotbar slot containing the item to be swapped with the currently equipped chest item.
     */
    private int targetHotbarSlot = -1;

    /**
     * Initializes the client-side of the mod.
     * This method registers the keybinding and sets up a client tick event listener
     * to handle the key press and the swap sequence.
     */
    @Override
    public void onInitializeClient() {
        elytraKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Swap Elytra/Chestplate",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Listen for the key press to start the swap sequence, but only if not already swapping.
            if (elytraKey.wasPressed() && swapStep == 0) {
                startHotbarSwap(client);
            }

            // If a swap is in progress, continue with the next step.
            if (swapStep > 0) {
                tickHotbarSwap(client);
            }
        });
    }

    /**
     * Starts the hotbar swap process.
     * It identifies the currently equipped chest item (Elytra or chestplate) and searches the player's
     * hotbar for the corresponding item to swap with (chestplate or Elytra).
     * If a suitable item is found, it initiates the 3-tick swap sequence.
     *
     * @param client The Minecraft client instance.
     */
    private void startHotbarSwap(MinecraftClient client) {
        ItemStack currentChest = client.player.getEquippedStack(EquipmentSlot.CHEST);
        int foundSlot = -1;

        // Search logic: If Elytra is equipped, find a chestplate in the hotbar.
        // Otherwise, find an Elytra in the hotbar.
        if (currentChest.isOf(Items.ELYTRA)) {
            for (int i = 0; i < 9; i++) { // Hotbar slots are 0-8
                if (isChestplate(client.player.getInventory().getStack(i))) {
                    foundSlot = i;
                    break;
                }
            }
        } else {
            for (int i = 0; i < 9; i++) { // Hotbar slots are 0-8
                if (client.player.getInventory().getStack(i).isOf(Items.ELYTRA)) {
                    foundSlot = i;
                    break;
                }
            }
        }

        // If a valid item was found in the hotbar, start the swap sequence.
        if (foundSlot != -1) {
            this.originalSlot = client.player.getInventory().getSelectedSlot();
            this.targetHotbarSlot = foundSlot;
            this.swapStep = 1; // Set to 1 to begin the tick-based swap.
        }
    }

    /**
     * Executes the 3-tick hotbar swap sequence. This is done over multiple ticks
     * to ensure the server can process the actions correctly.
     * <p>
     * <b>Tick 1:</b> Selects the hotbar slot with the item to swap.
     * <p>
     * <b>Tick 2:</b> Simulates a right-click to equip the item.
     * <p>
     * <b>Tick 3:</b> Selects the original hotbar slot and resets the state.
     *
     * @param client The Minecraft client instance.
     */
    private void tickHotbarSwap(MinecraftClient client) {
        switch (swapStep) {
            case 1: // Tick 1: Switch to the hotbar slot of the item to be equipped.
                client.player.getInventory().setSelectedSlot(targetHotbarSlot);
                swapStep = 2;
                break;

            case 2: // Tick 2: Use the item (right-click) to equip it.
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                swapStep = 3;
                break;

            case 3: // Tick 3: Switch back to the original hotbar slot.
                client.player.getInventory().setSelectedSlot(originalSlot);
                // Reset state machine to allow for another swap.
                swapStep = 0;
                originalSlot = -1;
                targetHotbarSlot = -1;
                break;
        }
    }

    /**
     * Checks if the given ItemStack is a valid chestplate for swapping.
     *
     * @param stack The ItemStack to check.
     * @return {@code true} if the ItemStack is a Netherite, Diamond, Iron, Golden, Chainmail, or Leather chestplate; {@code false} otherwise.
     */
    private boolean isChestplate(ItemStack stack) {
        return stack.isOf(Items.NETHERITE_CHESTPLATE) || stack.isOf(Items.DIAMOND_CHESTPLATE) ||
                stack.isOf(Items.IRON_CHESTPLATE) || stack.isOf(Items.GOLDEN_CHESTPLATE) ||
                stack.isOf(Items.CHAINMAIL_CHESTPLATE) || stack.isOf(Items.LEATHER_CHESTPLATE);
    }
}