package com.familiar_omen.create_pneumatics;

import com.familiar_omen.create_pneumatics.compat.curio.BacktankCurioRenderer;

import com.simibubi.create.content.equipment.armor.BacktankItem;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = CreatePneumatics.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = CreatePneumatics.MODID, value = Dist.CLIENT)
public class CreatePneumaticsClient {
    public CreatePneumaticsClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        CuriosRendererRegistry.register(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create", "copper_backtank")), () -> new BacktankCurioRenderer());
        CuriosRendererRegistry.register(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create", "netherite_backtank")), () -> new BacktankCurioRenderer());
    }
}
