package com.xy177.solwarly.block;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.spice.TileEntitySpiceGrinder;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class BlockSpiceGrinder extends BlockContainer {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final BlockSpiceGrinder INSTANCE = new BlockSpiceGrinder();

    private BlockSpiceGrinder() {
        super(Material.WOOD); setRegistryName(SOLWarly.MOD_ID, "spice_grinder");
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.SOUTH));
        setTranslationKey(SOLWarly.MOD_ID + ".spice_grinder"); setHardness(2.0F); setResistance(3.0F);
        setSoundType(SoundType.WOOD); setCreativeTab(net.minecraft.creativetab.CreativeTabs.DECORATIONS);
    }
    @SubscribeEvent public static void register(RegistryEvent.Register<Block> event) { event.getRegistry().register(INSTANCE); }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, FACING); }
    @Override public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(FACING).getHorizontalIndex(); }
    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntitySpiceGrinder(); }
    @Override public boolean hasTileEntity(IBlockState state) { return true; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.ENTITYBLOCK_ANIMATED; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos) {
        return new AxisAlignedBB(0, 0, 0, 1, 1.5, 1);
    }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntitySpiceGrinder) ((TileEntitySpiceGrinder) tile).insertHeld(player, hand);
        return true;
    }
    @Override public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntitySpiceGrinder) ((TileEntitySpiceGrinder) tile).dropContents();
        super.breakBlock(world, pos, state);
    }
}
