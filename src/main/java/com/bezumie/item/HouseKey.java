package com.bezumie.item;

import com.bezumie.util.HouseBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;

/** ПКМ по земле - мгновенно вырастает БЕЗУМНЫЙ ДОМ. */
public class HouseKey extends Item {
    public HouseKey(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        if (ctx.getWorld() instanceof ServerWorld world) {
            HouseBuilder.build(world, ctx.getBlockPos().up());
            if (ctx.getPlayer() != null && !ctx.getPlayer().isCreative()) ctx.getStack().decrement(1);
        }
        return ActionResult.success(ctx.getWorld().isClient);
    }
}
