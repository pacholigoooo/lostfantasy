package dev.lostfantasy;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;

/** In-memory blocks only: tests must never create or load a real chunk. */
public class TestWorld extends World {
    public final Map<BlockPos, IBlockState> blocks = new HashMap<>();
    public boolean loaded = true;

    public TestWorld() {
        this(new WorldProviderSurface());
    }
    public TestWorld(net.minecraft.world.WorldProvider provider) {
        super(null, new WorldInfo(new NBTTagCompound()), provider, new Profiler(), false);
    }

    @Override protected IChunkProvider createChunkProvider() { throw new AssertionError("Chunk loading is forbidden"); }
    @Override protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) { return loaded; }
    @Override public BlockPos getSpawnPoint() { return new BlockPos(0, 64, 0); }
    @Override public IBlockState getBlockState(BlockPos pos) {
        if (!loaded) throw new AssertionError("Read from an unloaded chunk");
        return blocks.getOrDefault(pos, Blocks.AIR.getDefaultState());
    }
}
