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

/** An enclosed garden opening into wooded hills; no custom sky or per-frame effect. */
public final class BoundaryGenerator implements IChunkGenerator {
    private final World world;private final long seed;private final GensokyoNoise noise;
    private final GensokyoBlueprint house;
    public BoundaryGenerator(World world) {this(world,world.getSeed());}
    BoundaryGenerator(World world,long seed) {this.world=world;this.seed=seed;noise=new GensokyoNoise(seed);house=BoundaryHouse.create();}
    int ground(int x,int z) {
        double dx=x-GensokyoAtlas.YAKUMO.x,dz=z-GensokyoAtlas.YAKUMO.z;
        double blend=GensokyoNoise.smooth((Math.max(Math.abs(dx)/66,Math.abs(dz-12)/68)-1)/1.4);
        double hills=165+noise.value(x,z,131,601)*29+noise.value(x,z,41,603)*5;
        hills+=19*Math.exp(-Math.pow((dz+210)/104,2))*GensokyoNoise.smooth((Math.abs(dx)-50)/140);
        return (int)Math.round(GensokyoNoise.lerp(GensokyoAtlas.YAKUMO.y,hills,blend));
    }
    public ChunkPrimer primer(int cx,int cz) {
        ChunkPrimer p=new ChunkPrimer();int minX=cx<<4,minZ=cz<<4;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int h=ground(minX+x,minZ+z);
            for(int y=0;y<=h;y++)p.setBlockState(x,y,z,y==0?Blocks.BEDROCK.getDefaultState():y==h?Blocks.GRASS.getDefaultState():y>h-4?Blocks.DIRT.getDefaultState():Blocks.STONE.getDefaultState());
        }
        for(int gx=Math.floorDiv(minX-8,18);gx<=Math.floorDiv(minX+23,18);gx++)for(int gz=Math.floorDiv(minZ-8,18);gz<=Math.floorDiv(minZ+23,18);gz++) {
            long hash=noise.hash(gx,gz,607);int x=gx*18+4+(int)Math.floorMod(hash,9),z=gz*18+4+(int)Math.floorMod(hash>>>16,9);
            if(Math.abs(x-GensokyoAtlas.YAKUMO.x)<66 && z>GensokyoAtlas.YAKUMO.z-51 && z<GensokyoAtlas.YAKUMO.z+82)continue;
            int h=ground(x,z),height=9+(int)Math.floorMod(hash>>>24,11);
            if(noise.value(x,z,73,609)<-.3 && Math.floorMod(hash>>>37,3)!=0)continue;
            for(int dx=-7;dx<=7;dx++)for(int dz=-7;dz<=7;dz++)for(int dy=-4;dy<=3;dy++)
                if(dx*dx+dz*dz+dy*dy*3<49 && Math.floorMod(dx*11+dz*7+dy,23)!=0)
                    put(p,minX,minZ,x+dx,h+height+dy,z+dz,Blocks.LEAVES.getStateFromMeta(4));
            for(int y=h+1;y<=h+height;y++)put(p,minX,minZ,x,y,z,Blocks.LOG.getDefaultState());
        }
        house.paint(p,cx,cz);return p;
    }
    private static void put(ChunkPrimer p,int minX,int minZ,int x,int y,int z,IBlockState state) {
        if(x>=minX && x<minX+16 && z>=minZ && z<minZ+16 && y>0 && y<256)p.setBlockState(x-minX,y,z-minZ,state);
    }
    @Override public Chunk generateChunk(int cx,int cz) {
        Chunk c=new Chunk(world,primer(cx,cz),cx,cz);java.util.Arrays.fill(c.getBiomeArray(),(byte)Biome.getIdForBiome(Biomes.FOREST));
        house.installContainers(c,seed);c.generateSkylightMap();return c;
    }
    @Override public void populate(int x,int z) {}
    @Override public boolean generateStructures(Chunk c,int x,int z) {return false;}
    @Override public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type,BlockPos p) {return type==EnumCreatureType.MONSTER?Collections.emptyList():Biomes.FOREST.getSpawnableList(type);}
    @Override public BlockPos getNearestStructurePos(World w,String name,BlockPos p,boolean unexplored) {return "yakumo".equalsIgnoreCase(name)?BoundaryWorld.arrival():null;}
    @Override public void recreateStructures(Chunk c,int x,int z) {}
    @Override public boolean isInsideStructure(World w,String name,BlockPos p) {return "yakumo".equalsIgnoreCase(name) && GensokyoAtlas.YAKUMO.contains(p.getX(),p.getZ(),0);}
}
