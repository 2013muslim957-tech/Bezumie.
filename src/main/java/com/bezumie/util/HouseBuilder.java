package com.bezumie.util;

import com.bezumie.Bezumie;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/** Безумный дом 15x15, цветные случайные стены, пирамидальная крыша, ловушки, лава, сундуки и ЯДЕРКА в центре. */
public class HouseBuilder {
    private static final Block[] WALLS = {
            Blocks.RED_WOOL, Blocks.ORANGE_WOOL, Blocks.YELLOW_WOOL, Blocks.LIME_WOOL, Blocks.CYAN_WOOL,
            Blocks.MAGENTA_WOOL, Blocks.PURPLE_WOOL, Blocks.GLOWSTONE, Blocks.SEA_LANTERN, Blocks.SLIME_BLOCK,
            Blocks.HONEY_BLOCK, Blocks.OBSIDIAN
    };
    private static final Block[] FLOOR = {
            Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.EMERALD_BLOCK, Blocks.LAPIS_BLOCK,
            Blocks.REDSTONE_BLOCK, Blocks.PURPUR_BLOCK, Blocks.MAGENTA_GLAZED_TERRACOTTA
    };
    private static final int[][] TRAPS = {{3, -3}, {-3, -3}, {-4, 3}, {0, -4}, {5, 0}, {-5, -3}};

    private static ServerWorld w;
    private static BlockPos o;

    private static void set(int x, int y, int z, Block b) {
        w.setBlockState(o.add(x, y, z), b.getDefaultState(), Block.NOTIFY_LISTENERS);
    }

    private static Block pick(Block[] a) {
        return a[w.random.nextInt(a.length)];
    }

    public static void build(ServerWorld world, BlockPos origin) {
        w = world;
        o = origin;

        // фундамент
        for (int x = -7; x <= 7; x++) {
            for (int z = -7; z <= 7; z++) {
                for (int y = -1; y >= -8; y--) {
                    BlockState s = w.getBlockState(o.add(x, y, z));
                    if (s.isAir() || s.isLiquid() || y == -1) set(x, y, z, Blocks.STONE_BRICKS);
                    else break;
                }
            }
        }

        // пол, стены, внутренность
        for (int x = -7; x <= 7; x++) {
            for (int z = -7; z <= 7; z++) {
                boolean edge = Math.abs(x) == 7 || Math.abs(z) == 7;
                set(x, 0, z, pick(FLOOR));
                for (int y = 1; y <= 5; y++) {
                    if (!edge) {
                        set(x, y, z, Blocks.AIR);
                        continue;
                    }
                    int along = Math.abs(x) == 7 ? z : x;
                    boolean corner = Math.abs(x) == 7 && Math.abs(z) == 7;
                    if (!corner && (y == 3 || y == 4) && Math.abs(along) < 7 && along % 3 == 0) {
                        set(x, y, z, y == 3 ? Blocks.MAGENTA_STAINED_GLASS : Blocks.LIME_STAINED_GLASS);
                    } else {
                        set(x, y, z, pick(WALLS));
                    }
                }
            }
        }
        // дверь (север, z = -7)
        for (int y = 1; y <= 2; y++) set(0, y, -7, Blocks.AIR);
        set(0, 0, -8, Blocks.GOLD_BLOCK);

        // крыша-пирамида с громоотводом
        for (int k = 0; k <= 7; k++) {
            int half = 7 - k;
            for (int x = -half; x <= half; x++) {
                for (int z = -half; z <= half; z++) {
                    set(x, 6 + k, z, k % 2 == 0 ? Blocks.MAGENTA_GLAZED_TERRACOTTA : pick(WALLS));
                }
            }
        }
        set(0, 14, 0, Blocks.LIGHTNING_ROD);

        // ядерка в центре на обсидиановом постаменте
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) set(x, 0, z, Blocks.CRYING_OBSIDIAN);
        set(0, 1, 0, Blocks.AIR);
        w.setBlockState(o.add(0, 1, 0), Bezumie.NUKE_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
        set(1, 1, 1, Blocks.JACK_O_LANTERN);
        set(-1, 1, -1, Blocks.JACK_O_LANTERN);
        set(1, 1, -1, Blocks.JACK_O_LANTERN);
        set(-1, 1, 1, Blocks.JACK_O_LANTERN);

        // лавовый бассейн
        for (int x = 2; x <= 3; x++) for (int z = 2; z <= 3; z++) set(x, 0, z, Blocks.LAVA);

        // полоса хаос-блоков
        for (int x = -5; x <= -2; x++) {
            w.setBlockState(o.add(x, 0, 0), Bezumie.CHAOS_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
        }

        // ловушки: ТНТ под нажимной плитой
        for (int[] t : TRAPS) {
            set(t[0], 0, t[1], Blocks.TNT);
            set(t[0], 1, t[1], Blocks.STONE_PRESSURE_PLATE);
        }

        // сундуки с добычей
        chest(5, 5, new ItemStack(Bezumie.CRAZY_WAND), new ItemStack(Items.DIAMOND, 16));
        chest(-5, 5, new ItemStack(Bezumie.CRAZY_SWORD), new ItemStack(Items.GOLDEN_APPLE, 8));
        chest(5, -5, new ItemStack(Bezumie.CRAZY_APPLE, 3), new ItemStack(Items.ENDER_PEARL, 16));
        chest(-5, -5, new ItemStack(Bezumie.NUKE_ITEM), new ItemStack(Items.FLINT_AND_STEEL));

        // безумные курицы-охранники
        for (int i = 0; i < 8; i++) {
            ChickenEntity c = EntityType.CHICKEN.create(w);
            if (c != null) {
                c.refreshPositionAndAngles(o.getX() + w.random.nextInt(9) - 4 + 0.5, o.getY() + 1, o.getZ() + 3 + w.random.nextInt(3) + 0.5, 0, 0);
                c.setCustomName(Text.literal("Безумная курица"));
                c.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 1_000_000, 0));
                w.spawnEntity(c);
            }
        }
    }

    private static void chest(int x, int z, ItemStack a, ItemStack b) {
        set(x, 1, z, Blocks.CHEST);
        if (w.getBlockEntity(o.add(x, 1, z)) instanceof ChestBlockEntity ch) {
            ch.setStack(0, a);
            ch.setStack(1, b);
        }
    }
}
