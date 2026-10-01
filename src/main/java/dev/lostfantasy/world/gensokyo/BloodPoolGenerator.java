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

/** Broad flooded basins with irregular islands, hanging rock and an enclosed cavern roof. */
public final class BloodPoolGenerator implements IChunkGenerator {
    static final int WATER=56,CENTER_Z=1050,RX=1040,RZ=1530;
    private static final int[][] ISLANDS={{-255,360,84,60},{410,760,114,95},{0,1070,160,48},{-410,1570,160,78},{480,1810,180,100}};
    private final World world;private final long seed;private final GensokyoNoise noise;
    private final GensokyoBlueprint remains=BloodPoolRemains.create();
    public BloodPoolGenerator(World world) {this(world,world.getSeed());}
    BloodPoolGenerator(World world,long seed) {this.world=world;this.seed=seed;noise=new GensokyoNoise(seed);}
    Column column(int x,int z) {
        double dx=x+noise.value(x,z,137,971)*26,dz=z-CENTER_Z+noise.value(x,z,163,972)*31;
        double d=dx*dx/(RX*(double)RX)+dz*dz/(RZ*(double)RZ);
        if(d>=1)return new Column(254,254);
        int floor=25+(int)Math.round(noise.value(x,z,117,973)*14+noise.value(x,z,37,974)*4+Math.pow(d,5)*58);
        int roof=100+(int)Math.round(139*Math.sqrt(1-d)+noise.value(x,z,63,975)*7);
        for(int[] island:ISLANDS) {
            double r=Math.hypot(x-island[0],z-island[1])/(island[2]*(1+.12*noise.value(x,z,31,976)));
            if(r<1)floor=Math.max(floor,WATER+(int)Math.round(island[3]*Math.pow(1-r,.7)));
        }
        // Widely separated grounded columns and stalactites make the sea readable at a distance.
        int gx=Math.floorDiv(x,233),gz=Math.floorDiv(z,233);
        for(int ox=-1;ox<=1;ox++)for(int oz=-1;oz<=1;oz++) {
            long hash=noise.hash(gx+ox,gz+oz,979);
            int px=(gx+ox)*233+45+(int)Math.floorMod(hash,140),pz=(gz+oz)*233+45+(int)Math.floorMod(hash>>>16,140);
            if(Math.abs(px)<320 && pz<740 || Math.floorMod(hash>>>32,3)!=0)continue;
            double r=Math.hypot(x-px,z-pz),radius=13+Math.floorMod(hash>>>40,20);
            if(r<radius)floor=Math.max(floor,60+(int)Math.round(146*Math.pow(1-r/radius,.55)));
            if(r<radius*1.6)roof-=Math.max(0,(int)Math.round(47*(1-r/(radius*1.6))));
        }
        return new Column(Math.max(9,Math.min(242,floor)),Math.max(70,Math.min(248,roof)));
    }
    public ChunkPrimer primer(int cx,int cz) {
        ChunkPrimer p=new ChunkPrimer();IBlockState rock=ModBlocks.COLUMNAR_BASALT.getDefaultState();
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int dx=(cx<<4)+x-OldHellWorld.ORIGIN.x,dz=(cz<<4)+z-OldHellWorld.ORIGIN.z;Column c=column(dx,dz);
            for(int y=0;y<256;y++) {
                IBlockState s=null;
                if(y==0 || y==255)s=Blocks.BEDROCK.getDefaultState();
                else if(y<=c.floor || y>=c.roof) {
                    s=rock;
                    if(y==c.floor || y==c.roof) {
                        long h=noise.hash(dx,dz,y);
                        if(Math.floorMod(h,19)==0)s=Blocks.NETHERRACK.getDefaultState();
                        if(Math.floorMod(h,127)==0)s=Blocks.MAGMA.getDefaultState();
                    }
                } else if(y<=WATER)s=ModBlocks.CURSED_BLOOD.getDefaultState();
                if(s!=null)p.setBlockState(x,y,z,s);
            }
        }
        remains.paint(p,cx,cz);return p;
    }
    @Override public Chunk generateChunk(int cx,int cz) {
        Chunk c=new Chunk(world,primer(cx,cz),cx,cz);Arrays.fill(c.getBiomeArray(),(byte)Biome.getIdForBiome(Biomes.STONE_BEACH));
        remains.installContainers(c,seed);c.generateSkylightMap();return c;
    }
    @Override public void populate(int x,int z) {}
    @Override public boolean generateStructures(Chunk c,int x,int z) {return false;}
    @Override public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type,BlockPos p) {return Collections.emptyList();}
    @Override public BlockPos getNearestStructurePos(World w,String name,BlockPos p,boolean unexplored) {return "blood_pool".equalsIgnoreCase(name)?BloodPoolWorld.local(0,BloodPoolRemains.FLOOR+1,60):null;}
    @Override public void recreateStructures(Chunk c,int x,int z) {}
    @Override public boolean isInsideStructure(World w,String name,BlockPos p) {
        Column c=column(p.getX()-OldHellWorld.ORIGIN.x,p.getZ()-OldHellWorld.ORIGIN.z);
        return "blood_pool".equalsIgnoreCase(name) && p.getY()>c.floor && p.getY()<c.roof;
    }
    static final class Column {final int floor,roof;Column(int floor,int roof){this.floor=floor;this.roof=roof;}}
}
