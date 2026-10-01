package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Furniture stays along the edges, leaving each room's central doorway and circulation clear. */
final class ScarletRooms {
    private ScarletRooms() {}
    static void ceilingLamp(GensokyoArchitecture a,int x,int y,int z) {
        a.block(x,y,z,Blocks.IRON_BARS.getDefaultState());
        a.block(x,y-1,z,LIGHT);
        a.block(x,y-2,z,Blocks.STONE_SLAB.getStateFromMeta(7));
    }
    private static void room(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int floor,String name) {
        int x=(x1+x2)/2;
        a.room(name,x,floor,z2);
        InteriorFinishes.rug(a,x1+1,z1+1,x2-1,z2-1,floor,14);
        // Shallow cornices stay above bookcases, canopy beds and all room entrances.
        for(int z:new int[]{z1,z2})a.box(x1,floor+7,z,x2,floor+7,z,ScarletMansion.TRIM);
        for(int xx:new int[]{x1,x2})a.box(xx,floor+7,z1,xx,floor+7,z2,ScarletMansion.TRIM);
        ceilingLamp(a,x,floor+6,(z1+z2)/2);
    }
    private static void seat(GensokyoArchitecture a,int x,int floor,int z,EnumFacing facing) {
        a.block(x,floor+1,z,dev.lostfantasy.ModBlocks.UPHOLSTERED_CHAIR.getDefaultState()
                .withProperty(net.minecraft.block.BlockHorizontal.FACING,facing.getOpposite()));
    }
    static void dining(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int floor,String name) {
        room(a,x1,z1,x2,z2,floor,name);
        int mx=(x1+x2)/2,mz=(z1+z2)/2,half=Math.min(9,(x2-x1)/2-4);
        a.table(mx-half,floor,mz,half*2+1);
        a.box(mx-half,floor+2,mz,mx+half,floor+2,mz,Blocks.WOODEN_SLAB.getStateFromMeta(13));
        for(int x=mx-half;x<=mx+half;x+=3) {
            seat(a,x,floor,mz-2,EnumFacing.NORTH);seat(a,x,floor,mz+2,EnumFacing.SOUTH);
            a.block(x,floor+3,mz,dev.lostfantasy.ModBlocks.LACQUER_BOWL.getDefaultState());
        }
        a.box(x1,floor+1,z1,x1+2,floor+1,z1+1,DARK);a.chest(x2-1,floor,z1);
        a.block(x1+1,floor+2,z1,Blocks.FLOWER_POT.getDefaultState());
    }
    static void lounge(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int floor,String name) {
        room(a,x1,z1,x2,z2,floor,name);
        int mx=(x1+x2)/2,mz=(z1+z2)/2;
        // Two sitting groups keep the north/south passage through the room open.
        for(int x:new int[]{x1+3,x2-4}) {
            a.block(x,floor+1,mz,dev.lostfantasy.ModBlocks.TEA_TABLE.getDefaultState());
            for(int dx=0;dx<2;dx++) {
                seat(a,x+dx,floor,mz-2,EnumFacing.NORTH);seat(a,x+dx,floor,mz+2,EnumFacing.SOUTH);
            }
        }
        a.box(x1,floor+1,z1,x1+3,floor+3,z1,Blocks.BOOKSHELF.getDefaultState());
        a.box(x2-3,floor+1,z1,x2,floor+2,z1,ScarletMansion.TRIM);
        a.block(x2-1,floor+3,z1,Blocks.FLOWER_POT.getDefaultState());
        a.block(mx,floor,z1,ScarletMansion.TRIM);
    }
    static void library(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int floor,String name) {
        room(a,x1,z1,x2,z2,floor,name);
        a.box(x1,floor+1,z1,x1,floor+4,z2-1,Blocks.BOOKSHELF.getDefaultState());
        a.box(x2,floor+1,z1,x2,floor+4,z2-1,Blocks.BOOKSHELF.getDefaultState());
        int mx=(x1+x2)/2,mz=(z1+z2)/2;
        a.table(mx-4,floor,mz,3);seat(a,mx-3,floor,mz+2,EnumFacing.SOUTH);
        a.chest(x2-2,floor,z1);a.block(x1+2,floor+1,z1,Blocks.CRAFTING_TABLE.getDefaultState());
    }
    static void bedroom(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int floor,String name,boolean master) {
        room(a,x1,z1,x2,z2,floor,name);
        int bx=x1+3,bz=z1+3;
        a.bed(bx,floor,bz);if(master)a.bed(bx+1,floor,bz);
        int end=master?bx+2:bx+1;
        for(int x:new int[]{bx-1,end})a.box(x,floor+1,z1+1,x,floor+4,z1+4,DARK);
        a.box(bx-1,floor+5,z1+1,end,floor+5,z1+4,ScarletMansion.RUG);
        a.chest(x2-1,floor,z1+1);
        a.box(x2,floor+1,z2-3,x2,floor+3,z2-1,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.cabinet(a,x2-4,floor,z2-2,EnumFacing.NORTH);
        a.block(x2-2,floor+1,z2-2,dev.lostfantasy.ModBlocks.WASHSTAND.getDefaultState());
        seat(a,x2-4,floor,z2,EnumFacing.SOUTH);
    }
    static void storage(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int floor,String name) {
        room(a,x1,z1,x2,z2,floor,name);
        for(int z=z1;z<=z2-1;z+=3) {
            a.chest(x1+1,floor,z);a.chest(x2-1,floor,z);
            a.box(x1,floor+4,z,x1+2,floor+4,z,Blocks.WOODEN_SLAB.getDefaultState());
            a.box(x2-2,floor+4,z,x2,floor+4,z,Blocks.WOODEN_SLAB.getDefaultState());
        }
        a.block(x1+1,floor+1,z2,Blocks.CRAFTING_TABLE.getDefaultState());
    }
    static void kitchen(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int floor,String name) {
        room(a,x1,z1,x2,z2,floor,name);
        for(int z=z1;z<=z2-2;z++)a.block(x1,floor+1,z,ScarletMansion.TRIM);
        a.block(x1+1,floor+1,z1,Blocks.FURNACE.getDefaultState());
        a.block(x1+2,floor+1,z1,Blocks.FURNACE.getDefaultState());
        a.block(x1,floor+1,z2-2,Blocks.CAULDRON.getStateFromMeta(3));
        a.block(x2,floor+1,z1,Blocks.CRAFTING_TABLE.getDefaultState());
        a.chest(x2,floor,z1+2);a.chest(x2,floor,z1+5);
        a.table(x2-5,floor,(z1+z2)/2,3);
        a.block(x1+4,floor+1,z1,dev.lostfantasy.ModBlocks.KITCHEN_SHELF.getDefaultState()
                .withProperty(net.minecraft.block.BlockHorizontal.FACING,EnumFacing.SOUTH));
    }
    static void music(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int floor) {
        room(a,x1,z1,x2,z2,floor,"音乐厅");
        a.box(x1+2,floor+1,z1+2,x1+12,floor+1,z1+9,DARK);
        a.box(x1+4,floor+2,z1+3,x1+8,floor+3,z1+5,Blocks.COAL_BLOCK.getDefaultState());
        a.box(x1+4,floor+2,z1+6,x1+8,floor+2,z1+6,Blocks.QUARTZ_BLOCK.getDefaultState());
        a.block(x1+10,floor+2,z1+3,Blocks.JUKEBOX.getDefaultState());
        for(int x=x1+4;x<x2-2;x+=4)for(int z=z1+13;z<=z2-2;z+=4)
            seat(a,x,floor,z,EnumFacing.SOUTH);
    }
}
