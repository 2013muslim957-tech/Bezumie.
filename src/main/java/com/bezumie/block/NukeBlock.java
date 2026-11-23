package com.bezumie.block;

import com.bezumie.util.Nuke;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** Ядерка: поджигаешь огнивом / редстоуном - 10 секунд обратного отсчёта - кратер радиусом 28 блоков. */
public class NukeBlock extends Block {
    public static final BooleanProperty LIT = Properties.LIT;
    public static final IntProperty COUNT = IntProperty.of("count", 0, 10);

    public NukeBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(LIT, false).with(COUNT, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT, COUNT);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (!state.get(LIT) && (stack.isOf(Items.FLINT_AND_STEEL) || stack.isOf(Items.FIRE_CHARGE))) {
            if (!world.isClient) {
                arm(world, pos, state);
                if (!player.isCreative()) {
                    if (stack.isOf(Items.FLINT_AND_STEEL)) stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                    else stack.decrement(1);
                }
            }
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient && !state.get(LIT) && world.isReceivingRedstonePower(pos)) {
            arm(world, pos, state);
        }
    }

    private void arm(World world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, state.with(LIT, true).with(COUNT, 0));
        world.scheduleBlockTick(pos, this, 20);
        world.playSound(null, pos, SoundEvents.ENTITY_CREEPER_PRIMED, SoundCategory.BLOCKS, 3f, 0.5f);
        if (world.getServer() != null) {
            world.getServer().getPlayerManager().broadcast(
                    Text.literal("☢ ЯДЕРКА АКТИВИРОВАНА! БЕГИТЕ! (10 секунд) ☢"), false);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int c = state.get(COUNT) + 1;
        if (c >= 10) {
            world.removeBlock(pos, false);
            Nuke.detonate(world, pos);
            return;
        }
        world.setBlockState(pos, state.with(COUNT, c));
        world.playSound(null, pos, SoundEvents.ENTITY_CREEPER_PRIMED, SoundCategory.BLOCKS, 3f, 0.5f + c * 0.15f);
        world.scheduleBlockTick(pos, this, 20);
    }
}
