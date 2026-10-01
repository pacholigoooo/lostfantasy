package dev.lostfantasy.world.gensokyo;

import java.util.Collections;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

/** Warm mountain refuge with ordinary blocks, sky and water, independent of the outside weather. */
public final class KasenGenerator implements IChunkGenerator {
    private final World world;private final long seed;private final GensokyoNoise noise;
    private final GensokyoBlueprint house;
    public KasenGenerator(World world) {this(world,world.getSeed());}
    KasenGenerator(World world,long seed) {this.world=world;this.seed=seed;noise=new GensokyoNoise(seed);house=KasenHouse.create();}
    int ground(int x,int z) {
        double dx=x-GensokyoAtlas.KASEN.x,dz=z-GensokyoAtlas.KASEN.z;
        double garden=GensokyoNoise.smooth((Math.max(Math.abs(dx)/52,Math.abs(dz-15)/60)-1)/1.3);
        double hill=136+24*noise.value(x,z,97,513)+86*Math.pow(Math.max(0,(noise.value(x,z,193,515)+.45)/.95),2);
        double ridge=1-Math.abs(noise.value(x,z,87,535));
        hill+=26*Math.pow(ridge,5)*GensokyoNoise.smooth((Math.hypot(dx,dz)-120)/160);
        double land=GensokyoNoise.lerp(GensokyoAtlas.KASEN.y,Math.min(236,Math.max(112,hill)),garden);
        return (int)Math.round(GensokyoNoise.lerp(128,land,GensokyoNoise.smooth((riverDistance(x,z)-8)/22)));
    }
    private double riverDistance(int x,int z) {
        double dx=x-GensokyoAtlas.KASEN.x,dz=z-GensokyoAtlas.KASEN.z;
        return Math.abs(dx-(-138+20*Math.sin(dz/113)+12*noise.value(x,z,109,527)));
    }
    public ChunkPrimer primer(int cx,int cz) {
        ChunkPrimer p=new ChunkPrimer();
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=(cx<<4)+x,wz=(cz<<4)+z,h=ground(wx,wz);boolean stone=h>207,wet=h<132;
            for(int y=0;y<=Math.max(h,wet?132:h);y++)p.setBlockState(x,y,z,y==0?Blocks.BEDROCK.getDefaultState():y>h?Blocks.WATER.getDefaultState():
                    y==h && wet?Blocks.GRAVEL.getDefaultState():y==h && !stone?Blocks.GRASS.getDefaultState():y>h-4 && !stone?Blocks.DIRT.getDefaultState():Blocks.STONE.getDefaultState());
            if(!stone && !wet && !garden(wx,wz) && noise.value(wx,wz,33,531)+noise.value(wx,wz,13,533)*.25>.12 && Math.floorMod(noise.hash(wx,wz,517),13)<2)
                p.setBlockState(x,h+1,z,Blocks.RED_FLOWER.getStateFromMeta((int)Math.floorMod(noise.hash(wx,wz,519),9)));
        }
        trees(p,cx,cz);house.paint(p,cx,cz);return p;
    }
    private static boolean garden(int x,int z) {return Math.abs(x-GensokyoAtlas.KASEN.x)<54 && z>GensokyoAtlas.KASEN.z-43 && z<GensokyoAtlas.KASEN.z+73;}
    private void trees(ChunkPrimer p,int cx,int cz) {
        int minX=cx<<4,minZ=cz<<4;
        for(int gx=Math.floorDiv(minX-8,25);gx<=Math.floorDiv(minX+23,25);gx++)for(int gz=Math.floorDiv(minZ-8,25);gz<=Math.floorDiv(minZ+23,25);gz++) {
            long hash=noise.hash(gx,gz,523);int x=gx*25+6+(int)Math.floorMod(hash,12),z=gz*25+6+(int)Math.floorMod(hash>>>16,12);
            int h=ground(x,z),height=8+(int)Math.floorMod(hash>>>24,10);if(garden(x,z) || h>203 || h<133 || riverDistance(x,z)<20)continue;
            if(noise.value(x,z,69,537)<-.28 && Math.floorMod(hash>>>37,3)!=0)continue;
            for(int dx=-6;dx<=6;dx++)for(int dz=-6;dz<=6;dz++)for(int dy=-3;dy<=3;dy++)
                if(dx*dx+dz*dz+dy*dy*4<36 && Math.floorMod(dx*11+dz*7+dy,23)!=0)
                    put(p,minX,minZ,x+dx,h+height+dy,z+dz,Blocks.LEAVES.getStateFromMeta(4));
            for(int y=h+1;y<=h+height;y++)put(p,minX,minZ,x,y,z,Blocks.LOG.getDefaultState());
        }
    }
    private static void put(ChunkPrimer p,int minX,int minZ,int x,int y,int z,IBlockState state) {
        if(x>=minX && x<minX+16 && z>=minZ && z<minZ+16 && y>0 && y<256)p.setBlockState(x-minX,y,z-minZ,state);
    }
    @Override public Chunk generateChunk(int cx,int cz) {
        Chunk chunk=new Chunk(world,primer(cx,cz),cx,cz);
        java.util.Arrays.fill(chunk.getBiomeArray(),(byte)Biome.getIdForBiome(Biomes.FOREST));
        house.installContainers(chunk,seed);chunk.generateSkylightMap();return chunk;
    }
    @Override public void populate(int x,int z) {}
    @Override public boolean generateStructures(Chunk c,int x,int z) {return false;}
    @Override public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type,BlockPos p) {
        return type==EnumCreatureType.MONSTER?Collections.emptyList():Biomes.FOREST.getSpawnableList(type);
    }
    @Override public BlockPos getNearestStructurePos(World w,String name,BlockPos p,boolean unexplored) {return "kasen".equalsIgnoreCase(name)?KasenWorld.arrival():null;}
    @Override public void recreateStructures(Chunk c,int x,int z) {}
    @Override public boolean isInsideStructure(World w,String name,BlockPos p) {return "kasen".equalsIgnoreCase(name) && GensokyoAtlas.KASEN.contains(p.getX(),p.getZ(),0);}
}
