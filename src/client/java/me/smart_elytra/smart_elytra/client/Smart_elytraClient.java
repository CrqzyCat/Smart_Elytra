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
        // Register the keybinding in the 'MISC' category
        elytraKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.swap_elytra", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, KeyBinding.Category.MISC));


        // Register a client tick event to check if the key was pressed
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (elytraKey.wasPressed()) {
                handleElytraSwap(client);
            }
        });
    }

    /**
     * Handles the logic for swapping between an Elytra and a chestplate.
     *
     * @param client The Minecraft client instance.
     */
    private void handleElytraSwap(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        // Index 6 is the chest slot in the PlayerScreenHandler
        int armorChestSlot = 6;
        ItemStack currentChest = client.player.getEquippedStack(EquipmentSlot.CHEST);

        // Case A: Elytra is equipped -> Search for the best available chestplate in the inventory.
        if (currentChest.isOf(Items.ELYTRA)) {
            int bestSlot = -1;
            // Iterate through the player's inventory to find a chestplate.
            for (int i = 0; i < 36; i++) {
                if (isChestplate(client.player.getInventory().getStack(i))) {
                    bestSlot = i;
                    break; // Found a chestplate, no need to search further.
                }
            }
            // If a chestplate is found, execute the swap.
            if (bestSlot != -1) {
                executeSwap(client, bestSlot, armorChestSlot);
            }
        }
        // Case B: A chestplate is equipped -> Search for an Elytra in the inventory.
        else {
            int elytraSlot = -1;
            // Iterate through the player's inventory to find an Elytra.
            for (int i = 0; i < 36; i++) {
                if (client.player.getInventory().getStack(i).isOf(Items.ELYTRA)) {
                    elytraSlot = i;
                    break; // Found an Elytra, no need to search further.
                }
            }
            // If an Elytra is found, execute the swap.
            if (elytraSlot != -1) {
                executeSwap(client, elytraSlot, armorChestSlot);
            }
        }
    }

    /**
     * Executes the inventory clicks to swap an item from the inventory with the equipped chest item.
     *
     * @param client    The Minecraft client instance.
     * @param invSlot   The inventory slot of the item to swap.
     * @param armorSlot The armor slot (always the chest slot in this case).
     */
    private void executeSwap(MinecraftClient client, int invSlot, int armorSlot) {
        // Map the inventory slot to the correct sync ID for the clickSlot method.
        // Hotbar slots (0-8) are mapped to 36-44, while main inventory slots (9-35) are mapped to 9-35.
        int syncInvSlot = invSlot < 9 ? invSlot + 36 : invSlot;

        // 1. Click: Pick up the item from the inventory slot onto the cursor.
        client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, syncInvSlot, 0, SlotActionType.PICKUP, client.player);

        // 2. Click: Click on the armor slot. This swaps the item on the cursor with the equipped item.
        client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, armorSlot, 0, SlotActionType.PICKUP, client.player);

        // 3. Click: Place the previously equipped item (now on the cursor) back into the now-empty inventory slot.
        client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, syncInvSlot, 0, SlotActionType.PICKUP, client.player);
    }

    /**
     * Checks if a given ItemStack is a chestplate.
     *
     * @param stack The ItemStack to check.
     * @return True if the ItemStack is a chestplate, false otherwise.
     */
    private boolean isChestplate(ItemStack stack) {
        return stack.isOf(Items.NETHERITE_CHESTPLATE) || stack.isOf(Items.DIAMOND_CHESTPLATE) || stack.isOf(Items.IRON_CHESTPLATE) || stack.isOf(Items.GOLDEN_CHESTPLATE) || stack.isOf(Items.CHAINMAIL_CHESTPLATE) || stack.isOf(Items.LEATHER_CHESTPLATE);
    }
}
