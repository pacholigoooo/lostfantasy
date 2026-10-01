package dev.lostfantasy.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/** Bound straw roof covering rendered as an ordinary opaque block. */
public final class ThatchBlock extends Block {
    public ThatchBlock() {
        super(Material.WOOD);setHardness(.8f);setResistance(2);setSoundType(SoundType.PLANT);
    }
}
