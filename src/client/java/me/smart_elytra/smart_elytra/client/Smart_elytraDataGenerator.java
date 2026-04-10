package me.smart_elytra.smart_elytra.client;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * This class serves as the entry point for client-side data generation for the Smart Elytra mod.
 * It is used to register data generators for various assets like models, languages, and recipes.
 */
public class Smart_elytraDataGenerator implements DataGeneratorEntrypoint {

    /**
     * Called by Fabric to initialize data generators.
     * This method is where you register your data generator packs.
     *
     * @param fabricDataGenerator The FabricDataGenerator instance provided by Fabric.
     */
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        // Create a new data pack. Data generators for models, languages, etc., would be registered here.
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        // Example: pack.addProvider(ModelGenerator::new);
        // Currently, no specific data generators are implemented for this client-side data generator.
    }
}
