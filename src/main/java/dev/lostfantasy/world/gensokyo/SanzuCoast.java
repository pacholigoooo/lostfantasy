package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;
import static dev.lostfantasy.world.gensokyo.GensokyoAtlas.*;

/** Continuous near bank, broad river and far shore within the Gensokyo map. */
public final class SanzuCoast {
    private static final int NORTH=SAI_BANK.z-800,SOUTH=SANZU_PIER.z+800;
    private SanzuCoast() {}
    public static double bank(int z) {
        if(z<SAI_BANK.z)return SAI_BANK.x-220+(SAI_BANK.z-z)*.045+Math.sin(z/137.0)*16;
        double t=Math.max(0,Math.min(1,(z-SAI_BANK.z)/(double)(SANZU_PIER.z-SAI_BANK.z)));
        if(z>SANZU_PIER.z)return SANZU_PIER.x-20-(z-SANZU_PIER.z)*.38;
        return GensokyoNoise.lerp(SAI_BANK.x-220,SANZU_PIER.x-20,t)+Math.sin(t*Math.PI*2)*26;
    }
    static boolean near(int x,int z) {return z>NORTH && z<SOUTH && x<bank(z)+465;}
    public static boolean region(double x,double z) {return z>NORTH+80 && z<SOUTH-80 && x<bank((int)z)+310 && x>bank((int)z)-1480;}
    static boolean stoneBank(int x,int z) {return z>NORTH+200 && z<SOUTH-200 && x>bank(z)-8 && x<bank(z)+280;}
    static boolean market(int x,int z,int margin) {return Math.abs(x-LIMINAL_ROAD.x)<=76+margin && Math.abs(z-LIMINAL_ROAD.z)<=138+margin;}
    static boolean treeless(int x,int z) {return near(x,z) && x<bank(z)+325 || market(x,z,15);}
    static double shape(int x,int z,double old,GensokyoNoise noise) {
        if(near(x,z)) {
            double distance=x-bank(z),far=bank(z)-1280-x;
            double shore=far>0?72+Math.min(18,far*.07)+noise.value(x,z,94,814)*2:distance<0?64+noise.value(x,z,80,811)*2:
                    72+distance*.065+noise.value(x,z,67,813)*2;
            double blend=(1-GensokyoNoise.smooth((distance-285)/180))*edge(z);
            old=GensokyoNoise.lerp(old,shore,blend);
        }
        // Keep the field's central lane level without flattening the whole beach.
        double d=Math.max(Math.abs((double)x-SAI_BANK.x)-17,Math.abs((double)z-SAI_BANK.z)-113);
        if(d<40)old=GensokyoNoise.lerp(old,SAI_BANK.y,1-GensokyoNoise.smooth(d/40));
        double landingDistance=Math.max(Math.abs(x-(dev.lostfantasy.world.HiganTerrain.farBankX()-18))-16,Math.abs(z-SANZU_PIER.z)-14);
        if(landingDistance<16)old=GensokyoNoise.lerp(old,76,1-GensokyoNoise.smooth(landingDistance/16));
        return old;
    }
    static boolean water(int x,int z) {return near(x,z) && x<bank(z) && x>bank(z)-1280 && edge(z)>.5;}
    private static double edge(int z) {return GensokyoNoise.smooth((z-NORTH)/200.0)*(1-GensokyoNoise.smooth((z-SOUTH+200)/200.0));}
    static void paint(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;
        if(!near(ox,oz) && !near(ox+15,oz+15) && !market(ox,oz,40))return;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=ox+x,wz=oz+z;GensokyoTerrain.Column c=terrain.column(wx,wz);
            if(c.wet() || c.path())continue;
            if(stoneBank(wx,wz)) {
                long h=terrain.hash(wx,wz,815);IBlockState surface=Math.floorMod(h,9)==0?Blocks.COBBLESTONE.getDefaultState():
                        Math.floorMod(h,6)==0?Blocks.STONE.getStateFromMeta(5):Blocks.GRAVEL.getDefaultState();
                p.setBlockState(x,c.ground,z,surface);p.setBlockState(x,c.ground+1,z,Blocks.AIR.getDefaultState());
            }
            if(GensokyoAtlas.reserved(wx,wz,5))continue;
            int landingX=dev.lostfantasy.world.HiganTerrain.farBankX();
            if(wx>=landingX-40 && wx<=landingX+54 && Math.abs(wz-SANZU_PIER.z)<=16)continue;
            // Patches on the sheltered landward edge leave the walking route clear.
            if(Math.abs(wx-SANZU_PIER.x)<105 && Math.abs(wz-SANZU_PIER.z)<125 && wx>bank(wz)+18
                    && Math.floorMod(terrain.hash(wx,wz,817),100)<25 && terrain.patchNoise(wx,wz,27,818)>-.05)
                p.setBlockState(x,c.ground+1,z,ModBlocks.SPIDER_LILY.getDefaultState());
            if(wx<bank(wz)-1285 && wx>bank(wz)-1480 && Math.floorMod(terrain.hash(wx,wz,821),100)<48
                    && terrain.patchNoise(wx,wz,41,823)>-.3)
                p.setBlockState(x,c.ground+1,z,ModBlocks.SPIDER_LILY.getDefaultState());
        }
        // Mossy pinnacles rise from the actual river bed; the ferry corridor remains open.
        for(int gx=Math.floorDiv(ox-8,53);gx<=Math.floorDiv(ox+23,53);gx++)for(int gz=Math.floorDiv(oz-8,53);gz<=Math.floorDiv(oz+23,53);gz++) {
            long hash=terrain.hash(gx,gz,827);int x=gx*53+12+(int)Math.floorMod(hash,25),z=gz*53+12+(int)Math.floorMod(hash>>>16,25);
            if(!water(x,z) || x<bank(z)-1180 || x>bank(z)-16 || Math.floorMod(hash>>>24,3)!=0)continue;
            if(dev.lostfantasy.world.HiganTerrain.ferryCorridor(x,z))continue;
            int top=75+(int)Math.floorMod(hash>>>32,13),radius=3+(int)Math.floorMod(hash>>>40,4);
            for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++) {
                double r=Math.hypot(dx,dz)/radius;if(r>1)continue;int peak=top-(int)Math.round(r*(top-64));
                int floor=terrain.column(x+dx,z+dz).ground;
                for(int y=floor+1;y<=peak;y++)put(p,ox,oz,x+dx,y,z+dz,y>=peak-2?Blocks.MOSSY_COBBLESTONE.getDefaultState():Blocks.STONE.getDefaultState());
            }
        }
        for(int gx=Math.floorDiv(ox-4,13);gx<=Math.floorDiv(ox+19,13);gx++)for(int gz=Math.floorDiv(oz-4,13);gz<=Math.floorDiv(oz+19,13);gz++) {
            long hash=terrain.hash(gx,gz,819);int x=gx*13+3+(int)Math.floorMod(hash,7),z=gz*13+3+(int)Math.floorMod(hash>>>16,7);
            if(Math.abs(x-SAI_BANK.x)>255 || Math.abs(z-SAI_BANK.z)>220 || !stoneBank(x,z) || Math.abs(x-SAI_BANK.x)<27)continue;
            GensokyoTerrain.Column c=terrain.column(x,z);
            if(c.wet() || c.path() || c.road!=null && c.road.distance<c.road.width+6)continue;
            int height=2+(int)Math.floorMod(hash>>>24,4);
            // Each pile has a broad base; its feet follow the same terrain sampler as the chunk.
            for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++) {
                int floor=terrain.column(x+dx,z+dz).ground;
                for(int y=floor+1;y<=c.ground+1;y++)put(p,ox,oz,x+dx,y,z+dz,Blocks.COBBLESTONE.getDefaultState());
            }
            for(int y=2;y<height;y++)put(p,ox,oz,x,c.ground+y,z,Blocks.COBBLESTONE_WALL.getDefaultState());
            put(p,ox,oz,x,c.ground+height,z,Blocks.STONE_SLAB.getStateFromMeta(3));
        }
    }
    private static void put(ChunkPrimer p,int ox,int oz,int x,int y,int z,IBlockState block) {
        if(x>=ox && x<ox+16 && z>=oz && z<oz+16 && y>0 && y<256)p.setBlockState(x-ox,y,z-oz,block);
    }
}
