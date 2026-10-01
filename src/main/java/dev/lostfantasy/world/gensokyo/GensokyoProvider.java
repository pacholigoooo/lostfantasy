package dev.lostfantasy.world.gensokyo;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;

/** Standard sky, weather, daylight and block lighting are left to Minecraft and the shader pack. */
public final class GensokyoProvider extends WorldProvider {
    @Override protected void init() {biomeProvider=new GensokyoBiomes(world.getSeed());hasSkyLight=true;}
    @Override public DimensionType getDimensionType() {return GensokyoWorld.TYPE;}
    @Override public IChunkGenerator createChunkGenerator() {return new GensokyoGenerator(world);}
    @Override public boolean isSurfaceWorld() {return true;}
    @Override public boolean canRespawnHere() {return true;}
    @Override public double getMovementFactor() {return 1;}
    @Override public BlockPos getSpawnPoint() {return GensokyoWorld.arrival();}
    @Override public BlockPos getRandomizedSpawnPoint() {return getSpawnPoint();}
    @Override public boolean canCoordinateBeSpawn(int x,int z) {
        GensokyoTerrain.Column c=new GensokyoTerrain(world.getSeed()).column(x,z);
        return !c.wet() && c.ground<230;
    }
}
