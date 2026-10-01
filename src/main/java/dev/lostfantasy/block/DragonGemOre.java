package dev.lostfantasy.block;

import dev.lostfantasy.ModItems;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

/** The mine's harvestable mineral. Its light uses the ordinary block light map. */
public final class DragonGemOre extends Block {
    public DragonGemOre() {
        super(Material.ROCK);
        setHardness(3);setResistance(5);setHarvestLevel("pickaxe",2);setLightLevel(5/15.0f);
    }
    @Override public Item getItemDropped(IBlockState state,Random random,int fortune) {return ModItems.DRAGON_GEM;}
    @Override public int quantityDroppedWithBonus(int fortune,Random random) {
        return fortune>0?Math.max(1,random.nextInt(fortune+2)):1;
    }
}
