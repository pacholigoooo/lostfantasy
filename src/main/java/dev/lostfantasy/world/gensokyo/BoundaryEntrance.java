package dev.lostfantasy.world.gensokyo;

import net.minecraft.init.Blocks;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

final class BoundaryEntrance {
    private BoundaryEntrance() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.YAKUMO);
        for(int[] p:new int[][]{{-17,52},{19,50},{-15,27},{17,21},{-4,7},{-31,73},{30,71}})KasenGarden.tree(a,p[0],p[1],15,8);
        a.box(-3,0,28,3,0,84,Blocks.GRAVEL.getDefaultState());a.box(-2,1,28,2,4,84,AIR);
        gate(a);
        a.room("山中院门",0,0,68);
    }
    static void gate(GensokyoArchitecture a) {
        a.box(-6,0,57,6,0,65,STONE);
        for(int x:new int[]{-4,4}) {
            a.box(x,1,59,x,5,63,LOG);a.box(x,1,54,x,3,58,Blocks.SPRUCE_FENCE.getDefaultState());
            a.box(x,1,64,x,3,68,Blocks.SPRUCE_FENCE.getDefaultState());
        }
        a.box(-4,5,59,4,5,63,DARK);a.gable(-7,56,7,66,6);
        a.box(-2,1,54,2,4,68,AIR);
    }
}
