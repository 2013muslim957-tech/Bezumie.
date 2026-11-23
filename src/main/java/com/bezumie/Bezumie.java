package com.bezumie;

import com.bezumie.block.ChaosBlock;
import com.bezumie.block.NukeBlock;
import com.bezumie.item.CrazySword;
import com.bezumie.item.CrazyWand;
import com.bezumie.item.HouseKey;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class Bezumie implements ModInitializer {
    public static final String MOD_ID = "bezumie";

    public static final Block NUKE_BLOCK = new NukeBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)
            .luminance(state -> state.get(NukeBlock.LIT) ? 15 : 4));
    public static final Block CHAOS_BLOCK = new ChaosBlock(AbstractBlock.Settings.copy(Blocks.STONE).luminance(s -> 10));

    public static final Item NUKE_ITEM = new BlockItem(NUKE_BLOCK, new Item.Settings().rarity(Rarity.EPIC));
    public static final Item CHAOS_ITEM = new BlockItem(CHAOS_BLOCK, new Item.Settings().rarity(Rarity.RARE));
    public static final Item CRAZY_WAND = new CrazyWand(new Item.Settings().maxCount(1).rarity(Rarity.EPIC));
    public static final Item CRAZY_SWORD = new CrazySword(new Item.Settings().rarity(Rarity.EPIC));
    public static final Item HOUSE_KEY = new HouseKey(new Item.Settings().maxCount(16).rarity(Rarity.RARE));
    public static final Item CRAZY_APPLE = new Item(new Item.Settings().rarity(Rarity.EPIC).food(
            new FoodComponent.Builder().hunger(8).saturationModifier(1.2f).alwaysEdible()
                    .statusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 2400, 4), 1f)
                    .statusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 2400, 6), 1f)
                    .statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 2400, 4), 1f)
                    .statusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 2400, 0), 1f)
                    .statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0), 1f)
                    .build()));

    @Override
    public void onInitialize() {
        reg("nuke_block", NUKE_BLOCK, NUKE_ITEM);
        reg("chaos_block", CHAOS_BLOCK, CHAOS_ITEM);
        Registry.register(Registries.ITEM, id("crazy_wand"), CRAZY_WAND);
        Registry.register(Registries.ITEM, id("crazy_sword"), CRAZY_SWORD);
        Registry.register(Registries.ITEM, id("house_key"), HOUSE_KEY);
        Registry.register(Registries.ITEM, id("crazy_apple"), CRAZY_APPLE);

        Registry.register(Registries.ITEM_GROUP, id("main"), FabricItemGroup.builder()
                .icon(() -> new ItemStack(NUKE_ITEM))
                .displayName(Text.translatable("itemGroup.bezumie"))
                .entries((ctx, entries) -> {
                    entries.add(NUKE_ITEM);
                    entries.add(CRAZY_WAND);
                    entries.add(CRAZY_SWORD);
                    entries.add(HOUSE_KEY);
                    entries.add(CHAOS_ITEM);
                    entries.add(CRAZY_APPLE);
                }).build());

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                handler.getPlayer().sendMessage(Text.literal("☢ БЕЗУМИЕ загружено. Берегись. Всё может взорваться. ☢")
                        .formatted(Formatting.RED, Formatting.BOLD), false));
    }

    private static void reg(String name, Block block, Item item) {
        Registry.register(Registries.BLOCK, id(name), block);
        Registry.register(Registries.ITEM, id(name), item);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
