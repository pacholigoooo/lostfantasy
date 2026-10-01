package dev.lostfantasy.world.gensokyo;

import net.minecraft.init.Blocks;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Walled formal garden with a broad central walk, fountain, service yards and two gate lodges. */
final class ScarletGardens {
    private ScarletGardens() {}
    static void build(GensokyoArchitecture a) {
        a.box(-110,0,30,110,0,107,Blocks.GRASS.getDefaultState());
        for(int x:new int[]{-112,112}) {
            a.box(x,1,-82,x,2,109,ScarletMansion.BRICK);
            a.box(x,3,-82,x,5,109,Blocks.IRON_BARS.getDefaultState());
        }
        for(int z:new int[]{-82,109}) {
            a.box(-112,1,z,112,2,z,ScarletMansion.BRICK);
            a.box(-112,3,z,112,5,z,Blocks.IRON_BARS.getDefaultState());
        }
        for(int x=-112;x<=112;x+=16)for(int z:new int[]{-82,109})pier(a,x,z);
        a.box(-7,1,108,7,7,110,AIR);
        for(int x:new int[]{-9,9}) {pier(a,x,109);a.box(x-1,6,108,x+1,7,110,ScarletMansion.TRIM);}
        a.box(-6,0,34,6,0,117,ScarletMansion.TRIM);
        a.box(-102,0,91,102,0,97,ScarletMansion.TRIM);
        for(int x:new int[]{-63,63})a.box(x-3,0,35,x+3,0,99,ScarletMansion.TRIM);
        for(int z:new int[]{42,81})a.box(-65,0,z-2,65,0,z+2,ScarletMansion.TRIM);
        // Fountain paths form a wide ring; crossing the water is never needed.
        for(int x=-19;x<=19;x++)for(int z=47;z<=85;z++) {
            int r=x*x+(z-66)*(z-66);
            if(r<=19*19)a.block(x,0,z,ScarletMansion.TRIM);
            if(r<=11*11)a.block(x,1,z,Blocks.WATER.getDefaultState());
            if(r>11*11 && r<=13*13)a.block(x,1,z,SLAB);
        }
        a.box(-2,1,64,2,3,68,ScarletMansion.TRIM);
        a.box(-3,4,63,3,4,69,ScarletMansion.TRIM);
        a.box(-2,5,64,2,5,68,Blocks.WATER.getDefaultState());
        a.box(0,5,66,0,7,66,ScarletMansion.TRIM);
        a.box(-1,8,65,1,8,67,ScarletMansion.TRIM);
        a.block(0,9,66,Blocks.WATER.getDefaultState());
        for(int side:new int[]{-1,1}) {
            for(int z:new int[]{53,72})flowerBed(a,side*39,z,15,6);
            for(int z:new int[]{41,64,82})flowerBed(a,side*87,z,13,5);
            lodge(a,side*89,99);
            for(int z:new int[]{46,87,101})a.lamp(side*17,1,z);
            for(int z:new int[]{51,76})a.lamp(side*69,1,z);
            for(int z:new int[]{-61,-11,18})cypress(a,side*106,z);
        }
        // Rear deliveries reach the west service door along the side of the house.
        a.box(-102,0,-74,-94,0,94,Blocks.GRAVEL.getDefaultState());
        a.box(-101,0,-76,89,0,-72,Blocks.GRAVEL.getDefaultState());
        a.box(-100,0,-35,-89,0,-28,ScarletMansion.TRIM);
        a.stairsSouth(-94,-32,0,3,3);
        a.box(-91,3,-34,-87,3,-30,ScarletMansion.TRIM);
        a.room("前庭",0,0,99);
    }
    private static void pier(GensokyoArchitecture a,int x,int z) {
        a.box(x-1,1,z-1,x+1,5,z+1,ScarletMansion.BRICK);
        a.box(x-1,6,z-1,x+1,6,z+1,ScarletMansion.TRIM);
        a.block(x,7,z,Blocks.IRON_BARS.getDefaultState());
    }
    private static void flowerBed(GensokyoArchitecture a,int cx,int cz,int rx,int rz) {
        for(int x=-rx;x<=rx;x++)for(int z=-rz;z<=rz;z++) {
            double d=x*x/(double)(rx*rx)+z*z/(double)(rz*rz);
            if(d>1)continue;
            if(d>.69)a.block(cx+x,1,cz+z,Blocks.LEAVES.getStateFromMeta(4));
            else if(Math.floorMod(x+z,3)!=0) {
                double scroll=Math.abs(z-2*Math.sin(x*.31));
                a.block(cx+x,1,cz+z,Blocks.RED_FLOWER.getStateFromMeta(scroll<1.3?6:4));
            }
        }
        a.block(cx,1,cz,Blocks.LEAVES.getStateFromMeta(4));
        a.block(cx,2,cz,Blocks.LEAVES.getStateFromMeta(4));
    }
    private static void cypress(GensokyoArchitecture a,int x,int z) {
        a.box(x,1,z,x,10,z,Blocks.LOG.getStateFromMeta(1));
        for(int y=3;y<=13;y++) {
            int radius=y<8?2:1;
            for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)
                if(dx*dx+dz*dz<=radius*radius+1 && (dx!=0 || dz!=0 || y>10))
                    a.block(x+dx,y,z+dz,Blocks.LEAVES.getStateFromMeta(5));
        }
        a.block(x,14,z,Blocks.LEAVES.getStateFromMeta(5));
    }
    private static void lodge(GensokyoArchitecture a,int x,int z) {
        a.box(x-8,0,z-7,x+8,0,z+7,STONE);
        a.box(x-7,1,z-6,x+7,8,z+6,ScarletMansion.BRICK);
        a.box(x-6,1,z-5,x+6,7,z+5,AIR);
        for(int side:new int[]{-1,1})a.box(x+side*7,3,z-3,x+side*7,5,z+3,ScarletMansion.GLASS);
        a.openZ(x,z-6,0,1,4);a.openZ(x,z+6,0,1,4);
        for(int level=0;level<8;level++) {
            int r=8-level;a.box(x-r,9+level,z-r,x+r,9+level,z+r,ScarletMansion.TILE);
        }
        a.block(x,17,z,Blocks.IRON_BARS.getDefaultState());
        a.bed(x-4,0,z+3);a.chest(x+4,0,z+3);a.table(x+3,0,z-2,2);
        ScarletRooms.ceilingLamp(a,x,7,z);
        a.room(x<0?"门卫休息室":"门卫值勤室",x,0,z);
    }
}
