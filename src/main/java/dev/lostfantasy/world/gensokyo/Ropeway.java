package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

final class Ropeway {
    private static final IBlockState FRAME=ModBlocks.COLUMNAR_BASALT.getDefaultState(),METAL=Blocks.IRON_BLOCK.getDefaultState();
    private Ropeway() {}
    static void build(GensokyoBlueprint p) {
        // Clear just the running envelope; the forest and mountain beneath the route remain intact.
        for(int z=0;z<=2600;z++) {
            Vec3d at=RopewayPath.point(z/2600.0,0);int x=(int)Math.floor(at.x),wz=(int)Math.floor(at.z),y=(int)Math.floor(at.y);
            p.box(x-7,y-2,wz-1,x+7,y+9,wz+1,AIR);
        }
        for(int end=0;end<2;end++)station(p,end);
        for(int z=130;z<2500;z+=210) {
            Vec3d at=RopewayPath.point(z/2600.0,0);int x=(int)Math.floor(at.x),wz=(int)Math.floor(at.z),y=(int)Math.floor(at.y+RopewayPath.HANGER);
            for(int side:new int[]{-1,1}) {
                p.box(x+side*10-1,8,wz-1,x+side*10+1,y+3,wz+1,FRAME);
                p.box(x+side*10-2,y-3,wz-2,x+side*10+2,y-2,wz+2,METAL);
            }
            p.box(x-11,y+3,wz-1,x+11,y+4,wz+1,METAL);
            for(int lane:new int[]{-4,4})p.box(x+lane,y+1,wz-2,x+lane,y+2,wz+2,METAL);
        }
        IBlockState cable=ModBlocks.ROPEWAY_CABLE.getDefaultState();
        for(int lane:new int[]{-4,4}) {
            BlockPos previous=null;
            for(int z=0;z<=2600;z++) {
                BlockPos at=new BlockPos(RopewayPath.point(z/2600.0,lane).add(0,RopewayPath.HANGER,0));
                p.box(at.getX(),at.getY(),at.getZ(),at.getX(),at.getY(),at.getZ(),cable);
                if(previous!=null) {
                    p.box(Math.min(at.getX(),previous.getX()),previous.getY(),at.getZ(),Math.max(at.getX(),previous.getX()),previous.getY(),at.getZ(),cable);
                    if(at.getY()!=previous.getY())p.box(at.getX(),Math.min(at.getY(),previous.getY()),at.getZ(),at.getX(),Math.max(at.getY(),previous.getY()),at.getZ(),cable);
                }
                previous=at;
            }
        }
    }
    private static void station(GensokyoBlueprint p,int end) {
        GensokyoArchitecture a=new GensokyoArchitecture(p,RopewayPath.station(end));
        a.box(-35,-5,-27,35,0,32,STONE);a.box(-35,1,-27,35,27,32,AIR);
        a.box(-33,1,-25,33,3,25,STONE);a.box(-32,4,-24,32,4,25,WOOD);
        a.box(-32,5,-24,32,17,25,WHITE);a.box(-31,5,-23,31,16,24,AIR);
        for(int x:new int[]{-32,-11,11,32}) {
            for(int z:new int[]{-24,-8,8,25})a.box(x,5,z,x,18,z,LOG);
            if(Math.abs(x)==11)a.box(x,5,-23,x,13,15,WHITE);
            if(Math.abs(x)==32)for(int z:new int[]{-17,-1,15}) {
                a.box(x,6,z-4,x,12,z+4,DARK);a.box(x,7,z-3,x,11,z+3,PAPER);
                a.box(x,7,z,x,11,z,LOG);
            }
        }
        for(int x:new int[]{-22,22}) {
            a.box(x-7,6,25,x+7,12,25,DARK);a.box(x-6,7,25,x+6,11,25,PAPER);
            for(int dx:new int[]{-2,2})a.box(x+dx,7,25,x+dx,11,25,LOG);
        }
        a.box(-32,14,25,32,14,25,DARK);
        for(int x:new int[]{-11,11}) {a.openX(x,7,4,3,5);a.box(x,7,-17,x,11,-4,PAPER);}
        a.gable(-35,-27,35,28,19);
        a.openZ(0,-24,4,8,10);a.openZ(0,25,4,6,7);a.stairsSouth(0,26,0,4,6);
        // The uphill terminal receives the cable from the south; its front entrance remains below it.
        if(end==1)a.openZ(0,25,4,8,10);
        a.box(-11,14,26,11,14,30,WOOD);a.box(-12,15,26,12,15,31,SLAB);
        for(int x:new int[]{-10,10}) {
            a.box(x,1,29,x,14,29,LOG);a.block(x+(x<0?1:-1),12,29,ModBlocks.LIBRARY_LAMP.getDefaultState());
            a.block(x+(x<0?1:-1),13,29,Blocks.OAK_FENCE.getDefaultState());
        }
        for(int x:new int[]{-4,4})a.box(x,13,-19,x,14,12,METAL);
        for(int z:new int[]{-16,10})a.box(-10,14,z,10,15,z,METAL);
        // Waiting lounge, baggage alcove, dispatch desk and a compact service room.
        for(int z:new int[]{-16,-5,10}) {
            a.table(-28,4,z,10);a.box(-28,5,z+2,-19,5,z+2,Blocks.SPRUCE_STAIRS.getDefaultState());
        }
        a.chest(-29,4,-21,"village_pantry");a.chest(-24,4,-21,"school_supplies");
        a.box(12,5,-2,31,13,-2,WOOD);a.openZ(17,-2,4,2,5);
        a.table(20,4,10,9);a.chest(29,4,5,"kappa_tools");
        for(int x:new int[]{15,22,29})a.chest(x,4,-20,"kappa_parts");
        a.block(29,5,-12,Blocks.CRAFTING_TABLE.getDefaultState());a.block(29,5,-8,Blocks.FURNACE.getDefaultState());
        a.box(21,5,-13,25,6,-7,METAL);a.block(23,7,-10,Blocks.ANVIL.getDefaultState());
        for(int x:new int[]{-25,25})for(int z:new int[]{-15,5}) {
            a.box(x,13,z,x,16,z,Blocks.OAK_FENCE.getDefaultState());a.block(x,12,z,ModBlocks.LIBRARY_LAMP.getDefaultState());
        }
        a.block(end==0?-9:9,5,4,ModBlocks.ROPEWAY_STOP.getDefaultState());
        a.room("候车厅",-17,4,5);a.room("检票与调度",17,4,10);a.room("绞盘检修",17,4,-11);
        a.room("乘车台",end==0?-9:9,4,7);a.room("下客台",end==0?9:-9,4,8);a.room("站前",0,0,32);
        InteriorFinishes.rug(a,-29,-18,-15,18,4,12);
        a.box(-30,8,-22,-17,8,-22,Blocks.WOODEN_SLAB.getStateFromMeta(13));
        a.block(-27,9,-22,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.box(20,6,10,28,6,10,Blocks.WOODEN_SLAB.getStateFromMeta(9));
        a.block(22,7,10,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.block(26,7,10,ModBlocks.LACQUER_BOWL.getDefaultState());
        // Flush strips mark the platform edge without putting obstacles in the car envelope.
        for(int x:new int[]{-10,10})for(int z=-18;z<=12;z+=4)a.box(x,4,z,x,4,z+1,Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(4));
        a.box(-5,16,25,5,17,25,DARK);a.sign(0,16,26,EnumFacing.SOUTH,end==0?"索道人里站":"索道山上站",end==0?"往守矢神社":"往人间之里");
        a.sign(end==0?-10:10,7,9,end==0?EnumFacing.EAST:EnumFacing.WEST,"乘车处","触碰乘车牌");
        a.block(end==0?-11:11,7,9,DARK);
        if(end==1) {
            a.box(-18,1,43,-18,3,43,LOG);
            a.sign(-18,3,44,EnumFacing.SOUTH,"← 玄云海","守矢神社 →");
        }
    }
}
