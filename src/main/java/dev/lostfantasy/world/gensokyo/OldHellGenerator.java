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

/** Connected entrance caves, an inhabited city floor and the much deeper former hell basin. */
public final class OldHellGenerator implements IChunkGenerator {
    private final World world;private final long seed;private final GensokyoNoise noise;
    private final GensokyoBlueprint city=OldHellCity.create();
    private final GensokyoBlueprint palace=Chireiden.create();
    private final GensokyoBlueprint blazing=BlazingHell.create();
    private final GensokyoBlueprint approach=HellDeepRoad.create();
    private final BlazingHellTerrain deep;
    private final HellDeepRoadTerrain deepRoad;
    public OldHellGenerator(World world) {this(world,world.getSeed());}
    OldHellGenerator(World world,long seed) {
        this.world=world;this.seed=seed;noise=new GensokyoNoise(seed);
        deep=new BlazingHellTerrain(noise);deepRoad=new HellDeepRoadTerrain(noise);
    }
    public ChunkPrimer primer(int cx,int cz) {
        ChunkPrimer p=new ChunkPrimer();
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int dx=(cx<<4)+x-OldHellWorld.ORIGIN.x,dz=(cz<<4)+z-OldHellWorld.ORIGIN.z,base=OldHellWorld.ORIGIN.y;
            double d=dx*(double)dx/(495.0*495)+(dz-145.0)*(dz-145)/(380.0*380);
            boolean north=Math.abs(dx+52)<=8 && dz>=-179 && dz<=-80;
            boolean east=Math.abs(dz-80)<=8 && dx>=366 && dx<=490;
            int floor=base,roof=base;
            if(d<1) {
                roof=base+18+(int)Math.round(119*Math.sqrt(1-d)+noise.value(dx,dz,23,921)*4);
                roof+=Math.max(0,(int)Math.round(12*noise.value(dx,dz,83,926)));
                if(Math.abs(dx)>OldHellCity.HALF_WIDTH+3 || dz>OldHellCity.SOUTH || dz<-100)floor+=Math.max(0,(int)Math.round(noise.value(dx,dz,31,922)*6));
                if(dz>=-59 && dz<=-27)floor=base-23+(int)Math.round(noise.value(dx,dz,19,923)*3);
            }
            if(north || east) {floor=base;roof=Math.max(roof,base+12);}
            double palaceD=dx*(double)dx/(185.0*185)+(dz-Chireiden.Z)*(double)(dz-Chireiden.Z)/(178.0*178);
            if(palaceD<1) {
                roof=Math.max(roof,base+23+(int)Math.round(122*Math.sqrt(1-palaceD)+noise.value(dx,dz,27,924)*4));
                floor=base+((Math.abs(dx)>115 || Math.abs(dz-Chireiden.Z)>88)?Math.max(0,(int)Math.round(noise.value(dx,dz,31,925)*5)):0);
            }
            // The palace cavern overlaps the rear streets; keep their inhabited floor level.
            if(d<1 && Math.abs(dx)<=OldHellCity.HALF_WIDTH+3 && dz>=-13 && dz<=OldHellCity.SOUTH)floor=base;
            if(Math.abs(dx)<=18 && dz>=OldHellCity.SOUTH && dz<=Chireiden.Z-75) {floor=base;roof=Math.max(roof,base+25);}
            BlazingHellTerrain.Column hot=deep.column(dx,dz);
            HellDeepRoadTerrain.Column passage=deepRoad.column(dx,dz);
            int layerShift=(int)Math.round(noise.value(dx,dz,51,927)*7);
            IBlockState darkRock=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(15);
            for(int y=0;y<256;y++) {
                if(y==0 || y==255)p.setBlockState(x,y,z,Blocks.BEDROCK.getDefaultState());
                else if(hot!=null && y>hot.floor && y<hot.roof) {
                    if(y<=hot.lava)p.setBlockState(x,y,z,Blocks.LAVA.getDefaultState());
                }
                else if(passage!=null && passage.open(y))continue;
                else if(y<=floor || y>=roof) {
                    IBlockState rock=y==floor?Blocks.STONE.getStateFromMeta(5):Blocks.STONE.getDefaultState();
                    if(hot!=null && (y>=hot.floor-3 && y<=hot.floor || y>=hot.roof && y<=hot.roof+3))
                        rock=Math.floorMod(y+layerShift,17)<3?Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(14):darkRock;
                    else if(y>=roof && y<=roof+3 || y<=floor && y>=floor-2 && (d>.83 && d<1 || palaceD>.83 && palaceD<1))
                        rock=Math.floorMod(y+layerShift,19)<4?Blocks.STONE.getStateFromMeta(1):Blocks.STONE.getStateFromMeta(5);
                    p.setBlockState(x,y,z,passage!=null && passage.surface(y)?passage.skin:rock);
                }
                else if(y<=base-18 && dz>=-59 && dz<=-27)p.setBlockState(x,y,z,Blocks.WATER.getDefaultState());
            }
        }
        approach.paint(p,cx,cz);city.paint(p,cx,cz);palace.paint(p,cx,cz);blazing.paint(p,cx,cz);return p;
    }
    @Override public Chunk generateChunk(int cx,int cz) {
        Chunk c=new Chunk(world,primer(cx,cz),cx,cz);java.util.Arrays.fill(c.getBiomeArray(),(byte)Biome.getIdForBiome(Biomes.STONE_BEACH));
        approach.installContainers(c,seed);city.installContainers(c,seed);palace.installContainers(c,seed);blazing.installContainers(c,seed);ChireidenPets.install(c);c.generateSkylightMap();return c;
    }
    @Override public void populate(int x,int z) {}
    @Override public boolean generateStructures(Chunk c,int x,int z) {return false;}
    @Override public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type,BlockPos p) {return Collections.emptyList();}
    @Override public BlockPos getNearestStructurePos(World w,String name,BlockPos p,boolean unexplored) {
        if("blood_pool".equalsIgnoreCase(name))return BloodPoolWorld.returnPoint();
        if("fusion_furnace".equalsIgnoreCase(name))return BlazingHell.local(0,BlazingHell.DECK+1,BlazingHell.CORE_Z-68);
        if("blazing_hell".equalsIgnoreCase(name))return BlazingHell.local(54,BlazingHell.DECK+1,850);
        if("deep_road".equalsIgnoreCase(name))return HellDeepRoad.arrival();
        return "chireiden".equalsIgnoreCase(name)?Chireiden.local(0,4,-73):"old_capital".equalsIgnoreCase(name)?OldHellWorld.local(0,1,90):null;
    }
    @Override public void recreateStructures(Chunk c,int x,int z) {}
    @Override public boolean isInsideStructure(World w,String name,BlockPos p) {
        if("deep_road".equalsIgnoreCase(name)) {
            HellDeepRoadTerrain.Column c=deepRoad.column(p.getX()-OldHellWorld.ORIGIN.x,p.getZ()-OldHellWorld.ORIGIN.z);
            return HellDeepRoad.contains(p) || c!=null && c.open(p.getY());
        }
        if("fusion_furnace".equalsIgnoreCase(name))return Math.abs(p.getX()-OldHellWorld.ORIGIN.x)<=110 && Math.abs(p.getZ()-OldHellWorld.ORIGIN.z-BlazingHell.CORE_Z)<=76 && p.getY()>=32 && p.getY()<=230;
        if("blazing_hell".equalsIgnoreCase(name)) {BlazingHellTerrain.Column c=deep.column(p.getX()-OldHellWorld.ORIGIN.x,p.getZ()-OldHellWorld.ORIGIN.z);return c!=null && p.getY()>c.floor && p.getY()<c.roof;}
        if("chireiden".equalsIgnoreCase(name))return Math.abs(p.getX()-OldHellWorld.ORIGIN.x)<=98 && Math.abs(p.getZ()-OldHellWorld.ORIGIN.z-Chireiden.Z)<=83 && p.getY()>=OldHellWorld.ORIGIN.y-11 && p.getY()<=OldHellWorld.ORIGIN.y+75;
        return "old_capital".equalsIgnoreCase(name) && Math.abs(p.getX()-OldHellWorld.ORIGIN.x)<=OldHellCity.HALF_WIDTH && p.getZ()-OldHellWorld.ORIGIN.z>=-5 && p.getZ()-OldHellWorld.ORIGIN.z<=OldHellCity.EXIT_Z;
    }
}
