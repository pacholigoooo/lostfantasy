package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;
import static dev.lostfantasy.world.gensokyo.ScarletMansion.*;

/** The rounded entrance arcade, with doors from the mansion's two upper galleries. */
final class ScarletPorch {
    private ScarletPorch() {}
    static void build(GensokyoArchitecture a) {
        for(int x=-20;x<=20;x++) {
            int front=front(x);
            a.box(x,0,28,x,2,front,STONE);
            a.box(x,3,28,x,3,front,TRIM);
            a.box(x,4,29,x,22,Math.max(29,front),AIR);
            for(int floor:new int[]{3,13}) {
                a.box(x,floor+1,front-1,x,floor+9,front,BRICK);
                a.box(x,floor+10,28,x,floor+10,front,TRIM);
                int arch=archHeight(x);
                if(arch>0)a.box(x,floor+1,front-1,x,floor+arch,front,AIR);
            }
            // Continuous balustrade follows the curved outer edge of the upper terrace.
            a.block(x,24,front,Blocks.IRON_BARS.getDefaultState());
            if(x%5==0)a.block(x,24,front,TRIM);
        }
        for(int x:new int[]{-18,-7,7,18}) {
            int z=front(x);
            for(int floor:new int[]{3,13}) {
                a.box(x,floor+1,z,x,floor+8,z,TRIM);
                a.box(x-1,floor+9,z,x+1,floor+9,z,TRIM);
            }
        }
        for(int floor:new int[]{13,23})a.openZ(0,28,floor,3,5);
        a.stairsSouth(0,43,0,3,6);
        for(int floor:new int[]{3,13})for(int x:new int[]{-11,11})
            ScarletRooms.ceilingLamp(a,x,floor+7,32);
        for(int x:new int[]{-12,10}) {
            for(int dx=0;dx<3;dx++)a.block(x+dx,24,31,Blocks.DARK_OAK_STAIRS.getDefaultState()
                    .withProperty(BlockStairs.FACING,EnumFacing.NORTH));
            a.box(x,24,34,x+2,24,34,Blocks.WOODEN_SLAB.getStateFromMeta(13));
        }
        for(int x:new int[]{-15,15}) {
            a.block(x,24,32,TRIM);
            a.block(x,25,32,Blocks.FLOWER_POT.getDefaultState());
        }
        a.room("弧形门廊",0,3,37);a.room("上层拱廊",0,13,35);a.room("前庭露台",0,23,36);
    }
    private static int front(int x) {
        return 28+(int)Math.floor(14*Math.sqrt(Math.max(0,1-x*x/400.0)));
    }
    private static int archHeight(int x) {
        for(int center:new int[]{-12,0,12}) {
            int half=center==0?5:4,distance=Math.abs(x-center);
            if(distance<=half)return 5+(int)Math.floor(4*Math.sqrt(1-distance*distance/(double)(half*half)));
        }
        return 0;
    }
}
