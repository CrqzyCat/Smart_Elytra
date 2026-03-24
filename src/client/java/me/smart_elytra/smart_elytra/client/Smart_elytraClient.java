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

public class Smart_elytraClient implements ClientModInitializer {

    private static KeyBinding elytraKey;

    private int swapStep = 0;
    private int originalSlot = -1;
    private int targetHotbarSlot = -1;

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

            // Start-Trigger
            while (elytraKey.wasPressed() && swapStep == 0) {
                startHotbarSwap(client);
            }

            // 3-Tick Ablauf
            if (swapStep > 0) {
                tickHotbarSwap(client);
            }
        });
    }

    private void startHotbarSwap(MinecraftClient client) {
        ItemStack currentChest = client.player.getEquippedStack(EquipmentSlot.CHEST);
        int foundSlot = -1;

        // Suche nur in der Hotbar (0-8)
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

    private void tickHotbarSwap(MinecraftClient client) {
        switch (swapStep) {
            case 1: // Tick 1: Gehe zum Slot
                client.player.getInventory().setSelectedSlot(targetHotbarSlot);
                swapStep = 2;
                break;

            case 2: // Tick 2: Rechtsklick zum Ausrüsten
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                swapStep = 3;
                break;

            case 3: // Tick 3: Zurück zum alten Slot
                client.player.getInventory().setSelectedSlot(originalSlot);
                // Reset
                swapStep = 0;
                originalSlot = -1;
                targetHotbarSlot = -1;
                break;
        }
    }

    private boolean isChestplate(ItemStack stack) {
        return stack.isOf(Items.NETHERITE_CHESTPLATE) || stack.isOf(Items.DIAMOND_CHESTPLATE) ||
                stack.isOf(Items.IRON_CHESTPLATE) || stack.isOf(Items.GOLDEN_CHESTPLATE) ||
                stack.isOf(Items.CHAINMAIL_CHESTPLATE) || stack.isOf(Items.LEATHER_CHESTPLATE);
    }
}