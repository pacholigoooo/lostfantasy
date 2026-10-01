package dev.lostfantasy.world.gensokyo;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;

/** Ordinary block lighting under a physical rock ceiling; no custom sky renderer. */
public final class OldHellProvider extends WorldProvider {
    @Override protected void init() {biomeProvider=new BiomeProviderSingle(Biomes.STONE_BEACH);hasSkyLight=false;}
    @Override public DimensionType getDimensionType() {return OldHellWorld.TYPE;}
    @Override public IChunkGenerator createChunkGenerator() {return new OldHellGenerator(world);}
    @Override public boolean isSurfaceWorld() {return false;}
    @Override public boolean canRespawnHere() {return true;}
    @Override public double getMovementFactor() {return 1;}
    @Override public BlockPos getSpawnPoint() {return OldHellWorld.arrival(0);}
    @Override public BlockPos getRandomizedSpawnPoint() {return getSpawnPoint();}
    @Override public boolean canCoordinateBeSpawn(int x,int z) {return x==getSpawnPoint().getX() && z==getSpawnPoint().getZ();}
}
