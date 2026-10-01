package dev.lostfantasy.block;

import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Ceiling-mounted furnishings use ordinary baked models, including the storage chest. */
public final class CeilingFurniture extends LibraryDecoration {
    public enum Use { NONE, CHEST, WORKBENCH }
    private final Use use;
    public CeilingFurniture(Kind kind,Use use) {super(kind);this.use=use;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) {
        AxisAlignedBB b=super.getBoundingBox(state,world,pos);
        return new AxisAlignedBB(b.minX,1-b.maxY,b.minZ,b.maxX,1-b.minY,b.maxZ);
    }
    @Override public boolean hasTileEntity(IBlockState state) {return use==Use.CHEST;}
    @Override public TileEntity createTileEntity(World world,IBlockState state) {return use==Use.CHEST?new CeilingChestTile():null;}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,
                                              EnumHand hand,EnumFacing side,float x,float y,float z) {
        if(use==Use.NONE)return false;
        if(world.isRemote)return true;
        if(use==Use.CHEST) {
            TileEntity tile=world.getTileEntity(pos);
            if(tile instanceof CeilingChestTile && !world.getBlockState(pos.down()).isNormalCube())
                player.displayGUIChest((CeilingChestTile)tile);
        } else player.displayGui(new BlockWorkbench.InterfaceCraftingTable(world,pos) {
            @Override public Container createContainer(InventoryPlayer inventory,EntityPlayer owner) {
                return new ContainerWorkbench(inventory,world,pos) {
                    @Override public boolean canInteractWith(EntityPlayer p) {
                        return world.getBlockState(pos).getBlock()==CeilingFurniture.this && p.getDistanceSqToCenter(pos)<=64;
                    }
                };
            }
        });
        return true;
    }
    @Override public void breakBlock(World world,BlockPos pos,IBlockState state) {
        TileEntity tile=world.getTileEntity(pos);
        if(tile instanceof CeilingChestTile) {
            InventoryHelper.dropInventoryItems(world,pos,(CeilingChestTile)tile);
            world.updateComparatorOutputLevel(pos,this);
        }
        super.breakBlock(world,pos,state);
        if(use==Use.CHEST)world.removeTileEntity(pos);
    }
    @Override public boolean hasComparatorInputOverride(IBlockState state) {return use==Use.CHEST;}
    @Override public int getComparatorInputOverride(IBlockState state,World world,BlockPos pos) {
        TileEntity tile=world.getTileEntity(pos);
        return tile instanceof CeilingChestTile?Container.calcRedstoneFromInventory((CeilingChestTile)tile):0;
    }
}
