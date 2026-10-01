package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.ChunkPrimer;

/** Shallow hollows beneath the forest canopy, sampled before roads and building pads. */
final class ForestWetlands {
    static final int[][] HOLLOWS={
            {GensokyoAtlas.FAIRY_OLD_TREE.x-231,GensokyoAtlas.FAIRY_OLD_TREE.z+140,59,39},
            {GensokyoAtlas.ALICE.x-450,GensokyoAtlas.ALICE.z+26,47,61},
            {GensokyoAtlas.FAIRY_SHRINE.x-60,GensokyoAtlas.FAIRY_SHRINE.z-389,42,32},
            {GensokyoAtlas.LARVA.x-144,GensokyoAtlas.LARVA.z+200,38,45}};
    private ForestWetlands() {}

    static Sample at(int x,int z,GensokyoNoise noise) {
        for(int[] hollow:HOLLOWS) {
            if(Math.abs(x-hollow[0])>hollow[2]*1.6 || Math.abs(z-hollow[1])>hollow[3]*1.6)continue;
            double d=Math.hypot((x-hollow[0])/(double)hollow[2],(z-hollow[1])/(double)hollow[3]);
            d+=noise.value(x,z,27,861)*.22+noise.value(x,z,11,863)*.05;
            double nx=(x-hollow[0])/(double)hollow[2],nz=(z-hollow[1])/(double)hollow[3];
            double island=5.3*Math.exp(-square((nx-.2)/.22)-square((nz+.16)/.3));
            double spur=4.7*Math.exp(-square((nx+.71)/.28)-square((nz-.34)/.34));
            if(d<1.4)return new Sample(hollow[0],hollow[1],Math.max(0,d),island+spur);
        }
        return null;
    }
    private static double square(double value) {return value*value;}

    static void paint(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;
        boolean near=false;
        for(int[] h:HOLLOWS)near|=ox<h[0]+h[2]*1.6 && ox+15>h[0]-h[2]*1.6 && oz<h[1]+h[3]*1.6 && oz+15>h[1]-h[3]*1.6;
        if(!near)return;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=ox+x,wz=oz+z;GensokyoTerrain.Column c=terrain.column(wx,wz);
            if(!c.wetland || c.rockSurface || GensokyoAtlas.reserved(wx,wz,12)
                    || c.road!=null && c.road.distance<c.road.width+6)continue;
            int chance=(int)Math.floorMod(terrain.hash(wx,wz,869),100);
            if(c.wet()) {
                p.setBlockState(x,c.ground,z,chance<23?Blocks.CLAY.getDefaultState():Blocks.DIRT.getDefaultState());
                if(chance<3 && p.getBlockState(x,c.water,z).getBlock()==Blocks.WATER && p.getBlockState(x,c.water+1,z).getBlock()==Blocks.AIR)
                    p.setBlockState(x,c.water+1,z,Blocks.WATERLILY.getDefaultState());
                continue;
            }
            IBlockState floor=p.getBlockState(x,c.ground,z),above=p.getBlockState(x,c.ground+1,z);
            if(floor.getBlock()!=Blocks.GRASS && floor.getBlock()!=Blocks.DIRT)continue;
            boolean mushroom=above.getBlock()==Blocks.BROWN_MUSHROOM || above.getBlock()==Blocks.RED_MUSHROOM;
            p.setBlockState(x,c.ground,z,Blocks.DIRT.getStateFromMeta(mushroom || chance<70?2:1));
            if(above.getBlock()!=Blocks.AIR && above.getBlock()!=Blocks.TALLGRASS)continue;
            boolean waterside=false;
            if(chance<25)for(EnumFacing side:EnumFacing.HORIZONTALS) {
                GensokyoTerrain.Column next=terrain.column(wx+side.getXOffset(),wz+side.getZOffset());
                waterside|=next.wet() && next.water==c.ground;
            }
            if(waterside) {
                // Vanilla reeds need dirt next to water at the same level as their base.
                p.setBlockState(x,c.ground,z,Blocks.DIRT.getDefaultState());
                p.setBlockState(x,c.ground+1,z,Blocks.REEDS.getDefaultState());
            } else if(chance<5) {
                p.setBlockState(x,c.ground,z,Blocks.DIRT.getStateFromMeta(2));
                p.setBlockState(x,c.ground+1,z,Blocks.BROWN_MUSHROOM.getDefaultState());
            } else if(chance<39)p.setBlockState(x,c.ground+1,z,Blocks.TALLGRASS.getStateFromMeta(2));
            else if(chance>97)p.setBlockState(x,c.ground+1,z,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        }
        // Sparse rounded outcrops break up the bank; each column stays grounded in its local terrain.
        for(int gx=Math.floorDiv(ox-3,24);gx<=Math.floorDiv(ox+18,24);gx++)for(int gz=Math.floorDiv(oz-3,24);gz<=Math.floorDiv(oz+18,24);gz++) {
            long hash=terrain.hash(gx,gz,987);
            if(Math.floorMod(hash,100)>=26)continue;
            int bx=gx*24+6+(int)Math.floorMod(hash>>>9,12),bz=gz*24+6+(int)Math.floorMod(hash>>>25,12);
            GensokyoTerrain.Column base=terrain.column(bx,bz);
            if(!base.wetland || base.wet())continue;
            int height=2+(int)Math.floorMod(hash>>>40,3);
            for(int wx=Math.max(ox,bx-3);wx<=Math.min(ox+15,bx+3);wx++)for(int wz=Math.max(oz,bz-3);wz<=Math.min(oz+15,bz+3);wz++) {
                double d=(wx-bx)*(wx-bx)/10.0+(wz-bz)*(wz-bz)/7.0;
                if(d>=1)continue;
                GensokyoTerrain.Column c=terrain.column(wx,wz);
                if(c.wet() || c.rockSurface || GensokyoAtlas.reserved(wx,wz,12)
                        || c.road!=null && c.road.distance<c.road.width+6 || Math.abs(c.ground-base.ground)>2)continue;
                int top=base.ground+1+(int)Math.round(height*Math.sqrt(1-d));
                for(int y=c.ground;y<=top;y++) {
                    IBlockState existing=p.getBlockState(wx-ox,y,wz-oz);
                    if(existing.getBlock()==Blocks.LOG || existing.getBlock()==Blocks.LOG2)break;
                    p.setBlockState(wx-ox,y,wz-oz,y<=c.ground+1 && Math.floorMod(wx+wz,3)!=0
                            ?Blocks.MOSSY_COBBLESTONE.getDefaultState():Blocks.STONE.getStateFromMeta(5));
                }
            }
        }
    }

    static final class Sample {
        final int x,z;final double distance,mound;
        Sample(int x,int z,double distance,double mound) {this.x=x;this.z=z;this.distance=distance;this.mound=mound;}
    }
}
