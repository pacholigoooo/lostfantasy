package dev.lostfantasy.world.gensokyo;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;

public final class BloodPoolProvider extends WorldProvider {
    @Override protected void init() {biomeProvider=new BiomeProviderSingle(Biomes.STONE_BEACH);hasSkyLight=false;}
    @Override public DimensionType getDimensionType() {return BloodPoolWorld.TYPE;}
    @Override public IChunkGenerator createChunkGenerator() {return new BloodPoolGenerator(world);}
    @Override public boolean isSurfaceWorld() {return false;}
    @Override public boolean canRespawnHere() {return true;}
    @Override public double getMovementFactor() {return 1;}
    @Override public BlockPos getSpawnPoint() {return BloodPoolWorld.arrival();}
    @Override public BlockPos getRandomizedSpawnPoint() {return getSpawnPoint();}
    @Override public boolean canCoordinateBeSpawn(int x,int z) {return x==getSpawnPoint().getX() && z==getSpawnPoint().getZ();}
}
