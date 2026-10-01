package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class GensokyoTestBlocks {
    private GensokyoTestBlocks() {}
    public static void register() {
        Bootstrap.register();
        dev.lostfantasy.block.CeilingChestTile.register();
        for(Block block:ModBlocks.ALL.values())
            if(!ForgeRegistries.BLOCKS.containsKey(block.getRegistryName()))ForgeRegistries.BLOCKS.register(block);
    }
}
