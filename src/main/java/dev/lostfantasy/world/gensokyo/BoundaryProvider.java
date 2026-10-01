package dev.lostfantasy.world.gensokyo;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;

public final class BoundaryProvider extends WorldProvider {
    @Override protected void init() {biomeProvider=new BiomeProviderSingle(Biomes.FOREST);hasSkyLight=true;}
    @Override public DimensionType getDimensionType() {return BoundaryWorld.TYPE;}
    @Override public IChunkGenerator createChunkGenerator() {return new BoundaryGenerator(world);}
    @Override public boolean isSurfaceWorld() {return true;}
    @Override public boolean canRespawnHere() {return true;}
    @Override public double getMovementFactor() {return 1;}
    @Override public BlockPos getSpawnPoint() {return BoundaryWorld.arrival();}
    @Override public BlockPos getRandomizedSpawnPoint() {return getSpawnPoint();}
    @Override public boolean canCoordinateBeSpawn(int x,int z) {return Math.abs(x-getSpawnPoint().getX())<3 && Math.abs(z-getSpawnPoint().getZ())<3;}
}
