package dev.lostfantasy.block;

import dev.lostfantasy.LostFantasy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

/** A single, non-ticking inventory. No vanilla chest adjacency or tile renderer. */
public final class CeilingChestTile extends TileEntityLockableLoot {
    private static boolean registered;
    private NonNullList<ItemStack> items=NonNullList.withSize(27,ItemStack.EMPTY);
    public static void register() {
        if(!registered) {TileEntity.register(new ResourceLocation(LostFantasy.ID,"ceiling_chest").toString(),CeilingChestTile.class);registered=true;}
    }
    @Override public int getSizeInventory(){return 27;}
    @Override public int getInventoryStackLimit(){return 64;}
    @Override public boolean isEmpty(){for(ItemStack item:items)if(!item.isEmpty())return false;return true;}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override public String getName(){return hasCustomName()?customName:"tile.lostfantasy.ceiling_chest.name";}
    @Override public String getGuiID(){return "minecraft:chest";}
    @Override public Container createContainer(InventoryPlayer inventory,EntityPlayer player){fillWithLoot(player);return new ContainerChest(inventory,this,player);}
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);items=NonNullList.withSize(27,ItemStack.EMPTY);lootTable=null;lootTableSeed=0;
        if(!checkLootAndRead(tag))ItemStackHelper.loadAllItems(tag,items);
        customName=tag.hasKey("CustomName",8)?tag.getString("CustomName"):null;
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);if(!checkLootAndWrite(tag))ItemStackHelper.saveAllItems(tag,items);
        if(hasCustomName())tag.setString("CustomName",customName);return tag;
    }
}
