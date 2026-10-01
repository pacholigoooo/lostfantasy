package dev.lostfantasy.world;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
public final class GapProvider extends WorldProvider {
    private static final Vec3d FOG=new Vec3d(.085,.04,.14);
    @Override protected void init() {biomeProvider=new BiomeProviderSingle(Biomes.SKY);hasSkyLight=false;}
    @Override public DimensionType getDimensionType() {return GapWorld.TYPE;}
    @Override public IChunkGenerator createChunkGenerator() {return new GapGenerator(world);}
    @Override public boolean canRespawnHere() {return false;}
    @Override public boolean isSurfaceWorld() {return false;}
    @Override public float calculateCelestialAngle(long time,float partial) {return .5f;}
    @Override protected void generateLightBrightnessTable() {
        for(int level=0;level<16;level++) {
            float darkness=1-level/15f;
            lightBrightnessTable[level]=.18f+.82f*(1-darkness)/(darkness*3+1);
        }
    }
    @Override public void getLightmapColors(float partial,float sun,float sky,float block,float[] colors) {
        colors[0]=Math.max(colors[0],.24f);colors[1]=Math.max(colors[1],.20f);colors[2]=Math.max(colors[2],.32f);
    }
    @Override public Vec3d getFogColor(float a,float b) {return FOG;}
    @Override public double getHorizon() {return -1024;}
    @Override public BlockPos getSpawnCoordinate() {return new BlockPos(0,GapSupport.HEIGHT,0);}
    @Override public boolean canCoordinateBeSpawn(int x,int z) {return Math.abs(x)<16&&Math.abs(z)<16;}
}
