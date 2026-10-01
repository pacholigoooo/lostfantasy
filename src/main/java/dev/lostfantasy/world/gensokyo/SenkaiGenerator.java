package dev.lostfantasy.world.gensokyo;

import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.IChunkGenerator;

/** A planted mountain basin around the dojo. Generation writes only the requested chunk. */
public final class SenkaiGenerator implements IChunkGenerator {
    private final World world;private final long seed;private final GensokyoNoise noise;
    private final GensokyoBlueprint buildings=SenkaiPalace.create();
    public SenkaiGenerator(World world) {this(world,world.getSeed());}
    SenkaiGenerator(World world,long seed) {this.world=world;this.seed=seed;noise=new GensokyoNoise(seed);}
    int ground(int x,int z) {
        double dx=x-GensokyoAtlas.MYOUREN.x,dz=z-GensokyoAtlas.MYOUREN.z;
        double distance=Math.max(Math.abs(dx),Math.abs(dz));
        double hills=117+72*Math.max(0,noise.value(x,z,151,771)+.25)+12*noise.value(x,z,47,773);
        double radius=Math.hypot(dx*.88,dz),back=GensokyoNoise.smooth((160-dz)/320);
        hills+=43*Math.exp(-Math.pow((radius-370)/83,2))*back;
        hills+=22*Math.exp(-Math.pow((radius-610)/140,2))*back;
        return (int)Math.round(GensokyoNoise.lerp(93,hills,GensokyoNoise.smooth((distance-125)/100)));
    }
    public ChunkPrimer primer(int cx,int cz) {
        ChunkPrimer p=new ChunkPrimer();int minX=cx<<4,minZ=cz<<4;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int h=ground(minX+x,minZ+z);boolean crag=h>185;
            for(int y=0;y<=h;y++)p.setBlockState(x,y,z,y==0?Blocks.BEDROCK.getDefaultState():y==h && !crag?Blocks.GRASS.getDefaultState():y>h-4 && !crag?Blocks.DIRT.getDefaultState():Blocks.STONE.getDefaultState());
        }
        for(int gx=Math.floorDiv(minX-8,23);gx<=Math.floorDiv(minX+23,23);gx++)for(int gz=Math.floorDiv(minZ-8,23);gz<=Math.floorDiv(minZ+23,23);gz++) {
            long hash=noise.hash(gx,gz,777);int x=gx*23+5+(int)Math.floorMod(hash,12),z=gz*23+5+(int)Math.floorMod(hash>>>16,12);
            if(Math.abs(x-GensokyoAtlas.MYOUREN.x)<130 && Math.abs(z-GensokyoAtlas.MYOUREN.z)<128)continue;
            int h=ground(x,z),height=8+(int)Math.floorMod(hash>>>24,9);
            if(h>184 || noise.value(x,z,71,779)<-.29 && Math.floorMod(hash>>>37,3)!=0)continue;
            for(int dx=-6;dx<=6;dx++)for(int dz=-6;dz<=6;dz++)for(int dy=-3;dy<=3;dy++)
                if(dx*dx+dz*dz+dy*dy*4<37 && Math.floorMod(dx*11+dz*7+dy,23)!=0)
                    put(p,minX,minZ,x+dx,h+height+dy,z+dz,Blocks.LEAVES.getStateFromMeta(4));
            for(int y=h+1;y<=h+height;y++)put(p,minX,minZ,x,y,z,Blocks.LOG.getDefaultState());
        }
        buildings.paint(p,cx,cz);return p;
    }
    private static void put(ChunkPrimer p,int minX,int minZ,int x,int y,int z,IBlockState state) {
        if(x>=minX && x<minX+16 && z>=minZ && z<minZ+16 && y>0 && y<256)p.setBlockState(x-minX,y,z-minZ,state);
    }
    @Override public Chunk generateChunk(int cx,int cz) {
        Chunk c=new Chunk(world,primer(cx,cz),cx,cz);Arrays.fill(c.getBiomeArray(),(byte)Biome.getIdForBiome(Biomes.FOREST));
        buildings.installContainers(c,seed);c.generateSkylightMap();return c;
    }
    @Override public void populate(int x,int z) {}
    @Override public boolean generateStructures(Chunk c,int x,int z) {return false;}
    @Override public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type,BlockPos p) {return Collections.emptyList();}
    @Override public BlockPos getNearestStructurePos(World w,String name,BlockPos p,boolean unexplored) {return "senkai".equalsIgnoreCase(name)?SenkaiWorld.arrival():null;}
    @Override public void recreateStructures(Chunk c,int x,int z) {}
    @Override public boolean isInsideStructure(World w,String name,BlockPos p) {return "senkai".equalsIgnoreCase(name) && Math.abs(p.getX()-GensokyoAtlas.MYOUREN.x)<=116 && Math.abs(p.getZ()-GensokyoAtlas.MYOUREN.z)<=116;}
}
