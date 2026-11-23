package com.bezumie.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Наступил - получи случайное безумие (иногда ещё и телепорт). */
public class ChaosBlock extends Block {
    private static final StatusEffect[] EFFECTS = {
            StatusEffects.LEVITATION, StatusEffects.NAUSEA, StatusEffects.SPEED, StatusEffects.JUMP_BOOST,
            StatusEffects.GLOWING, StatusEffects.SLOW_FALLING, StatusEffects.BLINDNESS, StatusEffects.REGENERATION,
            StatusEffects.STRENGTH, StatusEffects.NIGHT_VISION
    };

    public ChaosBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClient && entity instanceof LivingEntity living) {
            StatusEffect e = EFFECTS[world.random.nextInt(EFFECTS.length)];
            living.addStatusEffect(new StatusEffectInstance(e, 100 + world.random.nextInt(200), world.random.nextInt(6)));
            if (world.random.nextInt(8) == 0) {
                living.requestTeleport(pos.getX() + world.random.nextInt(17) - 8, pos.getY() + 1, pos.getZ() + world.random.nextInt(17) - 8);
            }
        }
        super.onSteppedOn(world, pos, state, entity);
    }
}
