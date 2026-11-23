package com.bezumie.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Жезл хаоса: каждый взмах - случайное безумие. */
public class CrazyWand extends Item {
    public CrazyWand(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world instanceof ServerWorld sw) {
            int r = world.random.nextInt(9);
            Vec3d look = player.getRotationVec(1f);
            Vec3d pos = player.getPos();
            switch (r) {
                case 0 -> {
                    msg(player, "ВЗЛЁТ!");
                    player.addVelocity(0, 2.5, 0);
                    player.velocityModified = true;
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 300, 0));
                }
                case 1 -> {
                    msg(player, "ГРОМОВОЕ КОЛЬЦО!");
                    for (int i = 0; i < 10; i++) {
                        double a = i * Math.PI / 5;
                        bolt(sw, new Vec3d(pos.x + Math.cos(a) * 6, pos.y, pos.z + Math.sin(a) * 6));
                    }
                }
                case 2 -> {
                    msg(player, "ТНТ С НЕБА!");
                    for (int i = 0; i < 12; i++) {
                        TntEntity t = new TntEntity(world, pos.x + world.random.nextInt(11) - 5, pos.y + 15,
                                pos.z + world.random.nextInt(11) - 5, player);
                        t.setFuse(60 + world.random.nextInt(40));
                        world.spawnEntity(t);
                    }
                }
                case 3 -> {
                    msg(player, "ЗАРЯЖЕННЫЕ КРИПЕРЫ!");
                    Vec3d at = pos.add(look.multiply(8));
                    for (int i = 0; i < 3; i++) {
                        CreeperEntity c = EntityType.CREEPER.create(world);
                        if (c != null) {
                            c.refreshPositionAndAngles(at.x + i * 2, at.y, at.z, 0, 0);
                            world.spawnEntity(c);
                            bolt(sw, c.getPos());
                        }
                    }
                }
                case 4 -> {
                    msg(player, "КУРИНЫЙ ДОЖДЬ!");
                    for (int i = 0; i < 25; i++) {
                        ChickenEntity ch = EntityType.CHICKEN.create(world);
                        if (ch != null) {
                            ch.refreshPositionAndAngles(pos.x + world.random.nextInt(9) - 4, pos.y + 20 + world.random.nextInt(6),
                                    pos.z + world.random.nextInt(9) - 4, 0, 0);
                            ch.setCustomName(Text.literal("Безумная курица"));
                            world.spawnEntity(ch);
                        }
                    }
                }
                case 5 -> {
                    msg(player, "ТЫ - БОГ (на 30 секунд)!");
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 600, 5));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 600, 8));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 600, 5));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 600, 3));
                }
                case 6 -> {
                    msg(player, "ОГНЕННЫЙ ШАР СМЕРТИ!");
                    FireballEntity f = new FireballEntity(world, player, look.x, look.y, look.z, 6);
                    f.setPosition(player.getX() + look.x * 2, player.getEyeY() + look.y * 2, player.getZ() + look.z * 2);
                    world.spawnEntity(f);
                }
                case 7 -> {
                    msg(player, "ГРОЗА И НОЧЬ!");
                    sw.setTimeOfDay(18000);
                    sw.setWeather(0, 6000, true, true);
                }
                default -> {
                    msg(player, "ТЕЛЕПОРТ!");
                    player.requestTeleport(pos.x + look.x * 30, pos.y + 2, pos.z + look.z * 30);
                }
            }
            player.getItemCooldownManager().set(this, 20);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    private static void bolt(ServerWorld w, Vec3d p) {
        LightningEntity l = EntityType.LIGHTNING_BOLT.create(w);
        if (l != null) {
            l.refreshPositionAfterTeleport(p);
            w.spawnEntity(l);
        }
    }

    private static void msg(PlayerEntity p, String s) {
        p.sendMessage(Text.literal("🌀 " + s), true);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
