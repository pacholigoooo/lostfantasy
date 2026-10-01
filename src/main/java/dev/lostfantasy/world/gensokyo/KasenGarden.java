package dev.lostfantasy.world.gensokyo;

import net.minecraft.init.Blocks;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A sheltered flower garden; paths and pond banks are built before furnishings. */
final class KasenGarden {
    private KasenGarden() {}
    static void build(GensokyoArchitecture a) {
        a.box(-46,-2,-35,46,0,59,Blocks.GRASS.getDefaultState());
        a.box(-3,0,16,3,0,67,STONE);a.box(-39,0,19,39,0,23,STONE);
        a.box(-41,0,-22,-37,0,21,STONE);a.box(37,0,-22,41,0,21,STONE);
        a.box(-38,0,-25,38,0,-21,STONE);
        VillageJoinery.pond(a,21,36,13,9);
        // The low stone bridge has a continuous deck and two banks above the water.
        a.box(18,0,23,22,0,49,STONE);a.box(18,1,28,22,1,44,SLAB);
        for(int z:new int[]{28,34,40,44})for(int x:new int[]{17,23})a.box(x,0,z,x,2,z,Blocks.COBBLESTONE_WALL.getDefaultState());
        a.box(2,0,48,21,0,50,STONE);
        GensokyoNoise noise=new GensokyoNoise(41);
        for(int x=-40;x<=40;x++)for(int z=-32;z<=56;z++) {
            boolean bed=x<-8 && z>26 && z<52 || x>30 && z>26 && z<40 || z<-27 && Math.abs(x)<20;
            if(bed && noise.value(x,z,9,2)>.03 && Math.floorMod(noise.hash(x,z,3),7)<3) {
                a.block(x,0,z,Blocks.GRASS.getDefaultState());
                a.block(x,1,z,Blocks.RED_FLOWER.getStateFromMeta(Math.floorMod(x+z,8)));
            }
        }
        for(int[] p:new int[][]{{-25,35},{-13,47},{36,48},{-33,-31},{29,-31}})tree(a,p[0],p[1],10,5);
        for(int x=-18;x<=18;x+=4)for(int z:new int[]{-34,-29})
            BambooGrove.gardenCulm(a,x,z,13+Math.floorMod(x+z,6),Math.floorMod(x+z,2)==0?net.minecraft.util.EnumFacing.NORTH:net.minecraft.util.EnumFacing.SOUTH);
        // Perches and a feeding alcove describe animal care without spawning substitute pets.
        a.box(37,1,-15,37,4,-15,LOG);a.box(43,1,-15,43,4,-15,LOG);a.box(36,5,-15,44,5,-15,Blocks.LOG.getStateFromMeta(13));
        a.box(44,1,-10,44,1,-5,STONE);a.box(44,2,-10,44,2,-5,Blocks.WOODEN_SLAB.getDefaultState());
        for(int[] p:new int[][]{{-7,24},{7,24},{-38,12},{38,12},{-7,55},{7,55}})a.lamp(p[0],0,p[1]);
        for(int x:new int[]{-4,4})a.box(x,1,61,x,5,61,DARK);
        a.box(-5,6,60,5,6,62,Blocks.PRISMARINE.getStateFromMeta(2));a.box(-4,7,61,4,7,61,SLAB);
        GardenScenery.arbour(a,-25,56,7,3);
        a.box(-18,0,54,-4,0,56,Blocks.GRAVEL.getDefaultState());
        for(int z=-10;z<=-5;z+=3) {
            a.block(44,2,z,Blocks.WOODEN_SLAB.getStateFromMeta(9));
            a.block(44,3,z,dev.lostfantasy.ModBlocks.LACQUER_BOWL.getDefaultState());
        }
        GardenScenery.plantedBed(a,44,5,2,4,8);
        a.room("花庭歇脚席",-25,0,56);
        a.room("花庭",-6,0,22);a.room("池畔",20,0,49);a.room("后庭",0,0,-23);a.room("归山小径",0,0,59);
    }
    static void tree(GensokyoArchitecture a,int x,int z,int height,int radius) {
        for(int dy=-2;dy<=3;dy++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)
            if(dx*dx+dz*dz+dy*dy*3<=radius*radius && Math.floorMod(dx*11+dz*7+dy,17)!=0)
                a.block(x+dx,height+dy,z+dz,Blocks.LEAVES.getStateFromMeta(4));
        a.box(x,1,z,x,height,z,Blocks.LOG.getDefaultState());
        for(int[] d:new int[][]{{1,0},{-1,1},{0,-1}})for(int n=1;n<radius-1;n++) {
            int px=x+d[0]*(n-1),pz=z+d[1]*(n-1),y=height-3+n/2;
            a.box(Math.min(px,x+d[0]*n),y-1,Math.min(pz,z+d[1]*n),Math.max(px,x+d[0]*n),y,Math.max(pz,z+d[1]*n),Blocks.LOG.getStateFromMeta(12));
        }
    }
}
