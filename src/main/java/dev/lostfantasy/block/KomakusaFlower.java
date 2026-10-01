package dev.lostfantasy.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.util.BlockRenderLayer;

/** Low alpine flower, drawn by the normal cutout block pass. */
public final class KomakusaFlower extends BlockBush {
    public KomakusaFlower() {setHardness(0);setSoundType(SoundType.PLANT);setTickRandomly(false);}
    @Override public BlockRenderLayer getRenderLayer() {return BlockRenderLayer.CUTOUT_MIPPED;}
}
