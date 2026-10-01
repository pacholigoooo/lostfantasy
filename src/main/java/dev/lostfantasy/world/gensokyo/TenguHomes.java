package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Each residence has a working ground floor and sleeping rooms reached by ordinary stairs. */
final class TenguHomes {
    private TenguHomes() {}
    static void build(GensokyoBlueprint plan) {
        for(TenguLayout.Plot p:TenguLayout.HOMES) {
            GensokyoArchitecture a=TenguLayout.at(plan,p);TenguJoinery.house(a,p);
            int w=p.w,d=p.d,top=(p.floors-1)*6,workZ=p.floors==1?-1:-d+3;
            InteriorFinishes.tatami(a,-w+2,-d+2,w-2,-5,top);
            a.box(-w+6,1,workZ-1,-w+8,1,workZ+1,WOOD);
            a.block(-w+7,2,workZ,ModBlocks.RESEARCH_NOTES.getDefaultState());
            kitchen(a,w-6,0,p.floors==1?d-7:-d+3);
            a.chest(w-3,0,p.floors==1?d-3:-d+7,"tengu_residence");
            switch(p.form) {
                case 1:
                    shelves(a,-w+2,0,-d+2,9);
                    VillageJoinery.lowDesk(a,-5,0,1,5);VillageJoinery.lowDesk(a,4,0,1,4);
                    a.room("书房",0,0,5);break;
                case 2:
                    a.box(3,1,2,w-5,1,2,DARK);a.box(3,2,2,w-5,2,2,Blocks.WOODEN_SLAB.getDefaultState());
                    a.block(6,3,2,ModBlocks.RESEARCH_NOTES.getDefaultState());
                    a.chest(w-4,0,-2,"tengu_printing");
                    a.box(3,3,d+1,w-4,3,d+1,Blocks.CARPET.getStateFromMeta(11));
                    a.room("临街小铺",1,0,5);break;
                case 3:
                    for(int x:new int[]{-5,3}) {
                        a.block(x,1,0,Blocks.CRAFTING_TABLE.getDefaultState());
                        a.box(x,1,-5,x+3,1,-5,WOOD);a.block(x+1,2,-5,ModBlocks.RESEARCH_NOTES.getDefaultState());
                    }
                    a.block(7,1,0,Blocks.ANVIL.getDefaultState());a.room("修造间",0,0,4);break;
                case 4:
                    VillageJoinery.wallX(a,0,-d+1,d-5,0,-1);
                    VillageJoinery.lowDesk(a,-8,0,2,4);VillageJoinery.lowDesk(a,4,0,2,4);
                    a.room("西间",-3,0,5);a.room("东间",3,0,5);break;
                default:
                    VillageJoinery.lowDesk(a,-4,0,1,7);
                    a.box(-5,1,5,5,1,6,Blocks.CARPET.getStateFromMeta(13));
                    a.room("起居间",1,0,d-3);
            }
            VillageJoinery.wallZ(a,-w+1,w-1,-3,top,0);
            VillageJoinery.wallX(a,0,-d+1,-4,top,-d+5);
            for(int side:new int[]{-1,1}) {
                int x=side*(w-6);
                a.bed(x,top,-d+4);a.chest(x-side*3,top,-d+3,"tengu_residence");
                a.box(x-1,top+1,-d+7,x+1,top+1,-d+7,Blocks.CARPET.getStateFromMeta(p.form%2==0?14:11));
                a.room(side<0?"西寝间":"东寝间",side*4,top,-6);
            }
            if(p.floors>1) {
                VillageJoinery.lowDesk(a,-5,top,1,4);
                a.room("楼上起居间",0,top,5);
            }
            if(p.floors>2) {
                shelves(a,-w+2,6,-d+2,w*2-4);
                for(int x:new int[]{-6,4})VillageJoinery.lowDesk(a,x,6,-5,5);
                a.block(0,7,0,ModBlocks.TENGU_CAMERA.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
                a.room("取材书斋",0,6,3);
            }
            // Personal work tools remain inside; the narrow veranda stays a passage.
            if(p.form==1 || p.form==5) {
                a.box(w-2,1,3,w-2,3,7,Blocks.BOOKSHELF.getDefaultState());
                a.block(w-2,4,5,ModBlocks.RESEARCH_NOTES.getDefaultState());
            }
            a.box(-w+2,top+4,-d+1,w-2,top+4,-d+1,Blocks.WOODEN_SLAB.getStateFromMeta(13));
            a.block(-w+4,top+5,-d+1,ModBlocks.LACQUER_BOWL.getDefaultState());
        }
    }
    static void kitchen(GensokyoArchitecture a,int x,int floor,int z) {
        a.block(x,floor+1,z,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.block(x+1,floor+1,z,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(x+2,floor+1,z,Blocks.CAULDRON.getDefaultState());
        a.box(x-1,floor+1,z+2,x+3,floor+1,z+2,WOOD);
    }
    static void shelves(GensokyoArchitecture a,int x,int floor,int z,int length) {
        a.box(x,floor+1,z,x+length-1,floor+3,z,Blocks.BOOKSHELF.getDefaultState());
        a.box(x,floor+4,z,x+length-1,floor+4,z,Blocks.WOODEN_SLAB.getDefaultState());
    }
}
