package dev.lostfantasy.world;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.IChunkGenerator;
import java.util.*;
public final class GapGenerator implements IChunkGenerator {
    private final World world;
    public GapGenerator(World world) {this.world=world;}
    @Override public Chunk generateChunk(int chunkX,int chunkZ) {
        ChunkPrimer primer=new ChunkPrimer();
        Chunk chunk=new Chunk(world,primer,chunkX,chunkZ);Arrays.fill(chunk.getBiomeArray(),(byte)Biome.getIdForBiome(Biomes.SKY));chunk.generateSkylightMap();return chunk;
    }
    @Override public void populate(int x,int z) {}
    @Override public boolean generateStructures(Chunk chunk,int x,int z) {return false;}
    @Override public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type,BlockPos pos) {return Collections.emptyList();}
    @Override public BlockPos getNearestStructurePos(World world,String name,BlockPos pos,boolean findUnexplored) {return null;}
    @Override public void recreateStructures(Chunk chunk,int x,int z) {}
    @Override public boolean isInsideStructure(World world,String name,BlockPos pos) {return false;}
}
