package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.IChunkGenerator;

/** Spring cherry hills. All terrain, foliage and architecture writes stay within the current chunk. */
public final class NetherworldGenerator implements IChunkGenerator {
    private final World world;private final long seed;private final GensokyoNoise noise;
    private final GensokyoBlueprint buildings=Hakugyokurou.create();
    public NetherworldGenerator(World world) {this(world,world.getSeed());}
    NetherworldGenerator(World world,long seed) {this.world=world;this.seed=seed;noise=new GensokyoNoise(seed);}
    int ground(int x,int z) {
        int dx=x-GensokyoAtlas.NETHER_GATE.x,dz=z-GensokyoAtlas.NETHER_GATE.z;
        double base=140+NetherworldEntrance.stairFloor(dz);
        double spread=dz>=140?Math.abs(dx)-48:Math.max(Math.abs(dx)-130,Math.max(-dz-300,dz-140));
        double hills=14+30*noise.value(x,z,123,781)+8*noise.value(x,z,37,783);
        return (int)Math.max(24,Math.min(210,Math.round(base+hills*GensokyoNoise.smooth(spread/100))));
    }
    public ChunkPrimer primer(int cx,int cz) {
        ChunkPrimer p=new ChunkPrimer();int minX=cx<<4,minZ=cz<<4;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int h=ground(minX+x,minZ+z);
            for(int y=0;y<=h;y++)p.setBlockState(x,y,z,y==0?Blocks.BEDROCK.getDefaultState():y==h?Blocks.GRASS.getDefaultState():y>h-4?Blocks.DIRT.getDefaultState():Blocks.STONE.getDefaultState());
        }
        buildings.paint(p,cx,cz);
        for(int gx=Math.floorDiv(minX-15,29);gx<=Math.floorDiv(minX+30,29);gx++)for(int gz=Math.floorDiv(minZ-15,29);gz<=Math.floorDiv(minZ+30,29);gz++) {
            long hash=noise.hash(gx,gz,787);int x=gx*29+5+(int)Math.floorMod(hash,12),z=gz*29+5+(int)Math.floorMod(hash>>>16,12);
            int dx=x-GensokyoAtlas.NETHER_GATE.x,dz=z-GensokyoAtlas.NETHER_GATE.z;
            if(!treeAllowed(dx,dz) || Math.floorMod(hash>>>34,100)<10)continue;
            int h=ground(x,z),height=11+(int)Math.floorMod(hash>>>24,11);
            int crownRadius=6+(int)Math.floorMod(hash>>>39,3),side=(hash&1)==0?1:-1;
            for(int[] lobe:new int[][]{{0,0,0},{-5,-1,2*side},{5,1,-2*side},{1,0,-5*side}}) {
                int radius=crownRadius,centerX=x+lobe[0],centerZ=z+lobe[2];
                int minOx=Math.max(-radius,minX-centerX),maxOx=Math.min(radius,minX+15-centerX);
                int minOz=Math.max(-radius,minZ-centerZ),maxOz=Math.min(radius,minZ+15-centerZ);
                for(int ox=minOx;ox<=maxOx;ox++)for(int oz=minOz;oz<=maxOz;oz++)for(int oy=-3;oy<=3;oy++)
                    if(ox*ox+oz*oz+oy*oy*5<radius*radius && Math.floorMod(ox*13+oz*7+oy,23)!=0)
                        put(p,minX,minZ,x+ox+lobe[0],h+height+oy+lobe[1],z+oz+lobe[2],ModBlocks.CHERRY_LEAVES.getDefaultState());
            }
            for(int y=h+1;y<=h+height;y++)put(p,minX,minZ,x,y,z,Blocks.LOG.getDefaultState());
            for(int direction:new int[]{-1,1})for(int n=1;n<=6;n++)put(p,minX,minZ,x+direction*n,h+height-3+n/2,z,Blocks.LOG.getStateFromMeta(12));
        }
        return p;
    }
    private static boolean treeAllowed(int x,int z) {
        if(Math.abs(x)<26 || z>=140 && z<=960 && Math.abs(x)<50)return false;
        if(z>=-135 && z<=70 && Math.abs(x)<138)return false;
        if(z>=-305 && z<-135 && Math.abs(x)<90)return false;
        if(z>110 && z<157 && Math.abs(x)<138)return false;
        if(Math.abs(x-108)<19 && z>=-230 && z<145)return false;
        return !(Math.abs(x-94)<15 && Math.abs(z-80)<15);
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
    @Override public BlockPos getNearestStructurePos(World w,String name,BlockPos p,boolean unexplored) {return "hakugyokurou".equalsIgnoreCase(name)?NetherworldWorld.local(0,1,100):null;}
    @Override public void recreateStructures(Chunk c,int x,int z) {}
    @Override public boolean isInsideStructure(World w,String name,BlockPos p) {return "hakugyokurou".equalsIgnoreCase(name) && Math.abs(p.getX()-GensokyoAtlas.NETHER_GATE.x)<=120 && p.getZ()>=GensokyoAtlas.NETHER_GATE.z-123 && p.getZ()<=GensokyoAtlas.NETHER_GATE.z+139;}
}
