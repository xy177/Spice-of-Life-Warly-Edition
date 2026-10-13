package com.xy177.solwarly.block;

import com.xy177.solwarly.spice.*;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class BlockSpiceGrinder extends BaseEntityBlock {
    @Override protected com.mojang.serialization.MapCodec<BlockSpiceGrinder> codec() { return simpleCodec(properties->new BlockSpiceGrinder()); }
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public BlockSpiceGrinder() {
        super(Properties.of().strength(2,3).sound(SoundType.WOOD).noOcclusion());registerDefaultState(stateDefinition.any().setValue(FACING,Direction.SOUTH));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite()); }
    @Override public BlockState rotate(BlockState state,Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state,Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new TileEntitySpiceGrinder(pos,state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type) {
        return level.isClientSide?null:createTickerHelper(type,SpiceRegistry.GRINDER_ENTITY.get(),(world,pos,block,tile)->tile.update());
    }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
    @Override public VoxelShape getShape(BlockState state,BlockGetter world,BlockPos pos,CollisionContext context) { return Block.box(0,0,0,16,24,16); }
    @Override protected ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if(!level.isClientSide&&level.getBlockEntity(pos) instanceof TileEntitySpiceGrinder grinder)grinder.insertHeld(player,hand);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TileEntitySpiceGrinder grinder) grinder.insertHeld(player, InteractionHand.MAIN_HAND);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving) {
        if(state.getBlock()!=next.getBlock()&&!level.isClientSide&&level.getBlockEntity(pos) instanceof TileEntitySpiceGrinder grinder)grinder.dropContents();
        super.onRemove(state,level,pos,next,moving);
    }
}
