package dev.lostfantasy.world.gensokyo;

import java.util.*;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

/** Vanilla biomes supply foliage, weather and shader identities; geography is atlas-driven. */
public final class GensokyoBiomes extends BiomeProvider {
    private final GensokyoTerrain terrain;
    public GensokyoBiomes(long seed) {terrain=new GensokyoTerrain(seed);}
    public static Biome biome(GensokyoTerrain.Region region) {
        switch(region) {
            case MOUNTAIN:return Biomes.EXTREME_HILLS_WITH_TREES;
            case FOREST:return Biomes.ROOFED_FOREST;
            case BAMBOO:return Biomes.JUNGLE;
            case FLOWERS:return Biomes.MUTATED_FOREST;
            case LAKE:case RIVER:return Biomes.RIVER;
            default:return Biomes.PLAINS;
        }
    }
    @Override public Biome getBiome(BlockPos pos) {return getBiome(pos,Biomes.PLAINS);}
    @Override public Biome getBiome(BlockPos pos,Biome fallback) {return biome(terrain.column(pos.getX(),pos.getZ()).region);}
    @Override public Biome[] getBiomes(Biome[] into,int x,int z,int width,int height) {
        return getBiomes(into,x,z,width,height,false);
    }
    @Override public Biome[] getBiomes(Biome[] into,int x,int z,int width,int height,boolean cache) {
        return sample(into,x,z,width,height,1);
    }
    @Override public Biome[] getBiomesForGeneration(Biome[] into,int x,int z,int width,int height) {
        return sample(into,x,z,width,height,4);
    }
    private Biome[] sample(Biome[] into,int x,int z,int width,int height,int scale) {
        if(into==null || into.length<width*height)into=new Biome[width*height];
        for(int dz=0;dz<height;dz++)for(int dx=0;dx<width;dx++)
            into[dx+dz*width]=biome(terrain.column((x+dx)*scale,(z+dz)*scale).region);
        return into;
    }
    @Override public boolean areBiomesViable(int x,int z,int radius,List<Biome> allowed) {
        for(int px=(x-radius)>>2;px<=(x+radius)>>2;px++)for(int pz=(z-radius)>>2;pz<=(z+radius)>>2;pz++)
            if(!allowed.contains(biome(terrain.column(px*4,pz*4).region)))return false;
        return true;
    }
    @Override public BlockPos findBiomePosition(int x,int z,int range,List<Biome> allowed,Random random) {
        BlockPos result=null;int matches=0;
        for(int px=(x-range)>>2;px<=(x+range)>>2;px++)for(int pz=(z-range)>>2;pz<=(z+range)>>2;pz++)
            if(allowed.contains(biome(terrain.column(px*4,pz*4).region)) && random.nextInt(++matches)==0)
                result=new BlockPos(px*4,0,pz*4);
        return result;
    }
    @Override public List<Biome> getBiomesToSpawnIn() {return Collections.singletonList(Biomes.PLAINS);}
    @Override public boolean isFixedBiome() {return false;}
}
