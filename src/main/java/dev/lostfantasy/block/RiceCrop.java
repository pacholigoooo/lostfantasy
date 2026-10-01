package dev.lostfantasy.block;

import dev.lostfantasy.ModItems;
import net.minecraft.block.BlockCrops;
import net.minecraft.item.Item;

/** Grain can be replanted; ordinary farmland supplies the light and hydration rules. */
public final class RiceCrop extends BlockCrops {
    @Override protected Item getSeed() {return ModItems.RICE_GRAIN;}
    @Override protected Item getCrop() {return ModItems.RICE_GRAIN;}
}
