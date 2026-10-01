package dev.lostfantasy.world;

import dev.lostfantasy.Balance;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/** Resolves study interactions in the Scarlet Mansion library. */
public final class LibrarySites {
    private LibrarySites() {}
    public static boolean dimension(int dimension) {return dimension==Balance.gensokyoDimensionId;}
    public static LibraryRuinLayout.Site generated(WorldServer world,BlockPos pos) {
        if(world.provider.getDimension()!=Balance.gensokyoDimensionId)return null;
        LibraryRuinLayout.Site site=ScarletLibrary.site(world.getSeed());
        return ScarletLibrary.contains(site,pos) && world.getChunkProvider().getLoadedChunk(pos.getX()>>4,pos.getZ()>>4)!=null?site:null;
    }
    static IBlockState planned(World world,LibraryRuinLayout.Site site,int x,int y,int z) {
        return ScarletLibrary.stateAt(site,x,y,z);
    }
}
