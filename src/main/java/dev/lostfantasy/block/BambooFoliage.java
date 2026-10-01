package dev.lostfantasy.block;

import net.minecraft.util.BlockRenderLayer;

/** A culm segment with attached leaf sprays; only the solid stem collides. */
public final class BambooFoliage extends BambooStem {
    public BambooFoliage() {super();setLightOpacity(1);}
    @Override public BlockRenderLayer getRenderLayer() {return BlockRenderLayer.CUTOUT_MIPPED;}
}
