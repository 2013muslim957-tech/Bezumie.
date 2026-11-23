package com.bezumie.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.math.Vec3d;

/** Меч: каждый удар - молния, подбрасывание и случайное проклятие для жертвы. */
public class CrazySword extends SwordItem {
    public CrazySword(Settings settings) {
        super(ToolMaterials.NETHERITE, 12, -2.0f, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.getWorld().isClient) {
            target.addVelocity(0, 1.2, 0);
            target.velocityModified = true;
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 40, 1));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0));
            if (target.getRandom().nextInt(3) == 0) {
                LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(target.getWorld());
                if (bolt != null) {
                    bolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(target.getBlockPos()));
                    target.getWorld().spawnEntity(bolt);
                }
            }
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
