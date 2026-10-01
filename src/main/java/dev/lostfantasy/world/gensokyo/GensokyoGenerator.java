package dev.lostfantasy.world.gensokyo;

import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

/** Terrain and structures are generated locally. Population never forces neighbouring chunks. */
public final class GensokyoGenerator implements IChunkGenerator {
    private final World world;
    private final GensokyoTerrain terrain;
    private final GensokyoBlueprint buildings;
    private final dev.lostfantasy.world.LibraryRuinLayout.Site library;
    private final long seed;
    public GensokyoGenerator(World world) {this(world,world.getSeed());}
    public GensokyoGenerator(World world,long seed) {
        this.world=world;this.seed=seed;terrain=new GensokyoTerrain(seed);buildings=GensokyoStructures.create();
        library=dev.lostfantasy.world.ScarletLibrary.site(seed);
    }
    public ChunkPrimer primer(int cx,int cz) {return primer(cx,cz,null);}
    private ChunkPrimer primer(int cx,int cz,byte[] biomes) {
        ChunkPrimer p=new ChunkPrimer();
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=(cx<<4)+x,wz=(cz<<4)+z;GensokyoTerrain.Column c=terrain.column(wx,wz);
            boolean sanzu=SanzuCoast.water(wx,wz);
            if(biomes!=null)biomes[(z<<4)|x]=(byte)Biome.getIdForBiome(GensokyoBiomes.biome(c.region));
            IBlockState top=c.path()?roadSurface(c,wx,wz):c.basalt?dev.lostfantasy.ModBlocks.COLUMNAR_BASALT.getDefaultState():c.wet()?Blocks.GRAVEL.getDefaultState():c.canalBank?Blocks.STONEBRICK.getDefaultState():
                    c.rockSurface || c.ground>207?Blocks.STONE.getDefaultState():Blocks.GRASS.getDefaultState();
            for(int y=0;y<=c.surface();y++)p.setBlockState(x,y,z,y==0?Blocks.BEDROCK.getDefaultState():
                    y>c.ground?(sanzu?dev.lostfantasy.ModBlocks.SANZU_WATER.getDefaultState():c.falling && y<c.water?Blocks.WATER.getStateFromMeta(8):Blocks.WATER.getDefaultState()):y==c.ground?top:
                    c.basalt && y>c.ground-48?dev.lostfantasy.ModBlocks.COLUMNAR_BASALT.getDefaultState():c.rockSurface?Blocks.STONE.getDefaultState():
                    y>c.ground-4?(c.canalBank?Blocks.STONEBRICK.getDefaultState():Blocks.DIRT.getDefaultState()):Blocks.STONE.getDefaultState());
            if(c.wet() && c.path()) {
                int deck=c.roadY;
                p.setBlockState(x,deck,z,Blocks.PLANKS.getStateFromMeta(1));
                if(Math.floorMod(wx+wz,7)==0 && c.road.distance>c.road.width-1)
                    for(int y=c.ground+1;y<deck;y++)p.setBlockState(x,y,z,Blocks.LOG.getStateFromMeta(1));
                if(c.road.distance>c.road.width-1)p.setBlockState(x,deck+1,z,Blocks.SPRUCE_FENCE.getDefaultState());
            } else if(!c.wet() && !c.path() && !c.rockSurface && !GensokyoAtlas.reserved(wx,wz,3)) {
                long h=terrain.hash(wx,wz,19);int chance=(int)Math.floorMod(h,100);
                if(!FlowerLandscapes.paintColumn(p,x,z,wx,wz,c,terrain) && c.ground<203 && chance<16)
                    p.setBlockState(x,c.ground+1,z,Blocks.TALLGRASS.getStateFromMeta(1));
                RoadsideScenery.paint(p,x,z,wx,wz,c,terrain);
            }
        }
        YoukaiMountain.paint(p,cx,cz,terrain);
        GensokyoVegetation.paint(p,cx,cz,terrain);
        SeasonalWays.paint(p,cx,cz,terrain);
        PhantomMeadow.paint(p,cx,cz,terrain);
        YoukaiWoodlands.paint(p,cx,cz,terrain);
        ForestWetlands.paint(p,cx,cz,terrain);
        LakeMargins.paint(p,cx,cz,terrain);
        RicePaddies.paint(p,cx,cz,terrain);
        ToadPond.paint(p,cx,cz,terrain);
        SecretHighland.paint(p,cx,cz,terrain);
        Muenzuka.paint(p,cx,cz,terrain);
        TenguTerrain.paint(p,cx,cz,terrain);
        CirnoIceHouse.paintIce(p,cx,cz,terrain);
        SanzuCoast.paint(p,cx,cz,terrain);
        clearRoadFoliage(p,cx,cz);
        dev.lostfantasy.world.ScarletLibrary.paint(p,cx,cz,library);
        buildings.paint(p,cx,cz);
        return p;
    }
    private IBlockState roadSurface(GensokyoTerrain.Column c,int x,int z) {
        if(!c.road.segment.contour || c.wet())return Blocks.GRAVEL.getDefaultState();
        int patch=(int)Math.floorMod(terrain.hash(x,z,964),20);
        if(c.road.distance>1.6 && patch>3)return c.rockSurface || c.ground>207?Blocks.STONE.getDefaultState():Blocks.GRASS.getDefaultState();
        if(c.ground>190 || c.rockSurface)return patch<6?Blocks.COBBLESTONE.getDefaultState():Blocks.STONE.getDefaultState();
        return patch<3?Blocks.GRAVEL.getDefaultState():patch<9?Blocks.DIRT.getStateFromMeta(1):Blocks.GRASS_PATH.getDefaultState();
    }
    private void clearRoadFoliage(ChunkPrimer primer,int cx,int cz) {
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            GensokyoTerrain.Column c=terrain.column((cx<<4)+x,(cz<<4)+z);
            if(!c.path())continue;
            int floor=c.wet()?c.roadY:c.ground;
            for(int y=floor+1;y<=Math.min(255,floor+2);y++)
                if(primer.getBlockState(x,y,z).getMaterial()==net.minecraft.block.material.Material.LEAVES)
                    primer.setBlockState(x,y,z,Blocks.AIR.getDefaultState());
        }
    }
    @Override public Chunk generateChunk(int cx,int cz) {
        byte[] biomes=new byte[256];Chunk c=new Chunk(world,primer(cx,cz,biomes),cx,cz);
        System.arraycopy(biomes,0,c.getBiomeArray(),0,256);
        dev.lostfantasy.world.ScarletLibrary.installContainers(c,library);
        buildings.installContainers(c,seed);
        MayohigaCats.install(c);GhostHouse.installSpirits(c);
        AquaticMarket.installBalloon(c);
        LiminalMarket.install(c);
        c.generateSkylightMap();return c;
    }
    @Override public void populate(int x,int z) {}
    @Override public boolean generateStructures(Chunk c,int x,int z) {return false;}
    @Override public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type,BlockPos pos) {
        if(SanzuCoast.region(pos.getX(),pos.getZ()) || SanzuCoast.market(pos.getX(),pos.getZ(),5))return Collections.emptyList();
        if(type==EnumCreatureType.MONSTER && GensokyoAtlas.reserved(pos.getX(),pos.getZ(),12))return Collections.emptyList();
        return GensokyoBiomes.biome(terrain.column(pos.getX(),pos.getZ()).region).getSpawnableList(type);
    }
    @Override public BlockPos getNearestStructurePos(World w,String name,BlockPos p,boolean unexplored) {
        for(GensokyoAtlas s:GensokyoAtlas.values())if(s.name().equalsIgnoreCase(name))return new BlockPos(s.x,s.y+1,s.approachZ());
        return null;
    }
    @Override public void recreateStructures(Chunk c,int x,int z) {}
    @Override public boolean isInsideStructure(World w,String name,BlockPos p) {
        for(GensokyoAtlas s:GensokyoAtlas.values())if(s.name().equalsIgnoreCase(name))return s.contains(p.getX(),p.getZ(),0);
        return false;
    }
}
