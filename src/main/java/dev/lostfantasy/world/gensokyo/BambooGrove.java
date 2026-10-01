package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.block.BambooStem;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.ChunkPrimer;

/** A deterministic grove of individual leaning culms. Only the current chunk is written. */
final class BambooGrove {
    private BambooGrove() {}
    static void gardenCulm(GensokyoArchitecture a,int x,int z,int height,EnumFacing direction) {
        for(int y=0;y<height;y++)a.block(x+direction.getXOffset()*(y/4),y+1,z+direction.getZOffset()*(y/4),
                ModBlocks.BAMBOO_STEM.getDefaultState().withProperty(BlockHorizontal.FACING,direction).withProperty(BambooStem.STEP,y&3));
        for(int y=height-7;y<height;y+=3) {
            int tx=x+direction.getXOffset()*(y/4),tz=z+direction.getZOffset()*(y/4);
            a.block(tx,y+1,tz,ModBlocks.BAMBOO_FOLIAGE.getDefaultState()
                    .withProperty(BlockHorizontal.FACING,direction).withProperty(BambooStem.STEP,y&3));
        }
    }
    static void paint(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;
        if(!GensokyoTerrain.nearBamboo(ox+8,oz+8,20))return;
        for(int gx=Math.floorDiv(ox-12,6);gx<=Math.floorDiv(ox+27,6);gx++)
            for(int gz=Math.floorDiv(oz-12,6);gz<=Math.floorDiv(oz+27,6);gz++) {
                long h=terrain.hash(gx,gz,192);
                int x=gx*6+(int)Math.floorMod(h>>>8,4),z=gz*6+(int)Math.floorMod(h>>>12,4);
                int density=74+(int)(terrain.patchNoise(x,z,67,197)*28);
                if(Math.floorMod(h,100)>=density)continue;
                if(terrain.region(x,z)!=GensokyoTerrain.Region.BAMBOO)continue;
                GensokyoTerrain.Column c=terrain.column(x,z);
                if(c.region!=GensokyoTerrain.Region.BAMBOO || c.wet() || GensokyoAtlas.reserved(x,z,12)
                        || c.road!=null && c.road.distance<c.road.width+4)continue;
                int height=14+(int)Math.floorMod(h>>>18,14);
                if(Math.floorMod(h>>>28,9)==0)height=4+(int)Math.floorMod(h>>>34,5);
                EnumFacing facing=EnumFacing.byHorizontalIndex((int)(h>>>23)&3);
                int dx=facing.getXOffset(),dz=facing.getZOffset();
                for(int y=0;y<height;y++) {
                    IBlockState stem=ModBlocks.BAMBOO_STEM.getDefaultState().withProperty(BlockHorizontal.FACING,facing).withProperty(BambooStem.STEP,y&3);
                    put(p,ox,oz,x+dx*(y/4),c.ground+1+y,z+dz*(y/4),stem,false);
                }
                // Each leafy segment includes its own culm and connected branches.
                for(int y=Math.max(3,height-9);y<height;y+=3) {
                    int tx=x+dx*(y/4),tz=z+dz*(y/4);
                    put(p,ox,oz,tx,c.ground+1+y,tz,ModBlocks.BAMBOO_FOLIAGE.getDefaultState()
                            .withProperty(BlockHorizontal.FACING,facing).withProperty(BambooStem.STEP,y&3),false);
                }
                for(int ax=-1;ax<=1;ax++)for(int az=-1;az<=1;az++) {
                    GensokyoTerrain.Column soil=terrain.column(x+ax,z+az);
                    if(!soil.wet() && !soil.path() && Math.floorMod(terrain.hash(x+ax,z+az,196),3)==0)
                        put(p,ox,oz,x+ax,soil.ground,z+az,Blocks.DIRT.getStateFromMeta(2),false);
                }
            }
    }
    private static void put(ChunkPrimer p,int ox,int oz,int x,int y,int z,IBlockState state,boolean airOnly) {
        x-=ox;z-=oz;if(x<0 || x>15 || z<0 || z>15 || y<1 || y>254)return;
        if(!airOnly || p.getBlockState(x,y,z).getBlock()==Blocks.AIR)p.setBlockState(x,y,z,state);
    }
}
