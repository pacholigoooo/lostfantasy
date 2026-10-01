package dev.lostfantasy.world;

import dev.lostfantasy.ModBlocks;
import java.util.Random;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

/** Small riverside patches, contained within the chunk being populated. */
public final class HiganFlowers implements IWorldGenerator {
    @Override public void generate(Random random,int cx,int cz,World world,IChunkGenerator generator,IChunkProvider provider) {
        if(world.provider.getDimension()!=0 || random.nextInt(5)!=0)return;
        int x=(cx<<4)+8,z=(cz<<4)+8;
        net.minecraft.world.biome.Biome biome=world.getBiome(new BlockPos(x,64,z));
        if(biome!=Biomes.RIVER && biome!=Biomes.SWAMPLAND)return;
        BlockPos encounter=null;
        for(int i=0;i<9;i++) {
            BlockPos p=world.getHeight(new BlockPos(x+random.nextInt(9)-4,0,z+random.nextInt(9)-4));
            if(world.isAirBlock(p) && world.getBlockState(p.down()).getMaterial().isSolid()) {
                world.setBlockState(p,ModBlocks.SPIDER_LILY.getDefaultState(),2);
                encounter=p;
            }
        }
        if(encounter!=null)KomachiEncounter.arrive(world,encounter);
    }
}
