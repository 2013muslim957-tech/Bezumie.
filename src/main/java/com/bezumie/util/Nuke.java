package com.bezumie.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Nuke {
    public static final int RADIUS = 28;

    public static void detonate(ServerWorld w, BlockPos c) {
        if (w.getServer() != null) {
            w.getServer().getPlayerManager().broadcast(Text.literal("☢☢☢ ЯДЕРНЫЙ ВЗРЫВ ☢☢☢"), false);
        }
        w.playSound(null, c, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 60f, 0.4f);
        w.playSound(null, c, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 60f, 0.5f);

        // 1. Живые существа и предметы
        Box box = new Box(c).expand(RADIUS * 2.2);
        List<Entity> list = w.getOtherEntities(null, box, e -> true);
        for (Entity e : list) {
            double d = Math.sqrt(e.squaredDistanceTo(Vec3d.ofCenter(c)));
            if (d > RADIUS * 2.2) continue;
            if (e instanceof LivingEntity le) {
                if (d < RADIUS * 1.3 && !(le instanceof PlayerEntity p && p.isCreative())) {
                    le.damage(w.getDamageSources().explosion(null, null), 1000f);
                } else {
                    le.damage(w.getDamageSources().explosion(null, null), (float) (60 - d));
                    Vec3d push = le.getPos().subtract(Vec3d.ofCenter(c)).normalize().multiply(3).add(0, 1.5, 0);
                    le.addVelocity(push.x, push.y, push.z);
                    le.velocityModified = true;
                }
            } else if (d < RADIUS * 1.3) {
                e.discard();
            }
        }

        // 2. Кратер
        int r2 = RADIUS * RADIUS;
        int shell = (RADIUS - 3) * (RADIUS - 3);
        BlockPos.Mutable p = new BlockPos.Mutable();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    int d2 = dx * dx + dy * dy + dz * dz;
                    if (d2 > r2) continue;
                    p.set(c.getX() + dx, c.getY() + dy, c.getZ() + dz);
                    if (w.isOutOfHeightLimit(p)) continue;
                    BlockState st = w.getBlockState(p);
                    if (st.isAir() || st.isOf(Blocks.BEDROCK)) continue;
                    BlockState ns = Blocks.AIR.getDefaultState();
                    if (d2 > shell) {
                        int roll = w.random.nextInt(10);
                        if (roll < 3) ns = Blocks.MAGMA_BLOCK.getDefaultState();
                        else if (roll < 5) ns = Blocks.BLACKSTONE.getDefaultState();
                        else if (roll < 6) ns = Blocks.CRYING_OBSIDIAN.getDefaultState();
                        else if (roll < 7) ns = Blocks.FIRE.getDefaultState();
                    }
                    w.setBlockState(p, ns, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
                }
            }
        }

        // 3. Лава на дне кратера
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                if (dx * dx + dz * dz > 36) continue;
                p.set(c.getX() + dx, c.getY() - RADIUS + 3, c.getZ() + dz);
                if (!w.isOutOfHeightLimit(p) && !w.getBlockState(p).isOf(Blocks.BEDROCK)) {
                    w.setBlockState(p, Blocks.LAVA.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
            }
        }

        // 4. Молнии, гриб, гроза
        for (int i = 0; i < 16; i++) {
            LightningEntity l = EntityType.LIGHTNING_BOLT.create(w);
            if (l != null) {
                l.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(c.add(w.random.nextInt(81) - 40, 0, w.random.nextInt(81) - 40)));
                w.spawnEntity(l);
            }
        }
        w.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, c.getX(), c.getY(), c.getZ(), 40, 10, 6, 10, 0);
        for (int y = 0; y < 34; y += 2) {
            w.spawnParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, c.getX(), c.getY() + y, c.getZ(), 6, 2, 1, 2, 0.02);
            w.spawnParticles(ParticleTypes.FLAME, c.getX(), c.getY() + y, c.getZ(), 10, 2, 1, 2, 0.05);
        }
        for (int i = 0; i < 400; i++) {
            double a = w.random.nextDouble() * Math.PI * 2;
            double b = w.random.nextDouble() * Math.PI;
            w.spawnParticles(ParticleTypes.LARGE_SMOKE,
                    c.getX() + Math.cos(a) * Math.sin(b) * 12, c.getY() + 38 + Math.cos(b) * 7, c.getZ() + Math.sin(a) * Math.sin(b) * 12,
                    1, 0, 0, 0, 0.01);
        }
        w.setWeather(0, 12000, true, true);
    }
}
