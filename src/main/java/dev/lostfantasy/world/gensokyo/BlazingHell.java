package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A walkable industrial landscape inside the much larger former hell cavern. */
final class BlazingHell {
    static final int CORE_Z=1430, DECK=-35;
    private static final IBlockState BASALT=ModBlocks.COLUMNAR_BASALT.getDefaultState(),
            RAIL=Blocks.IRON_BARS.getDefaultState(),METAL=Blocks.IRON_BLOCK.getDefaultState();
    private BlazingHell() {}
    static BlockPos local(int x,int y,int z) {return OldHellWorld.local(x,y,z);}
    static GensokyoBlueprint create() {
        GensokyoBlueprint p=new GensokyoBlueprint();
        GensokyoArchitecture a=new GensokyoArchitecture(p,OldHellWorld.ORIGIN,0,0,"旧灼热地狱");
        descent(a);
        // North approach, two side routes and a southern return form a complete walking loop.
        bridge(a,54,850,54,1110,DECK,4);
        bridge(a,54,1110,0,1110,DECK,4);
        bridge(a,0,1110,0,CORE_Z-74,DECK,4);
        bridge(a,0,1110,-240,1110,DECK,4);bridge(a,-240,1110,-240,1690,DECK,4);
        bridge(a,0,1110,240,1110,DECK,4);bridge(a,240,1110,240,1690,DECK,4);
        bridge(a,-240,1690,240,1690,DECK,4);
        bridge(a,0,CORE_Z+74,0,1690,DECK,4);
        bridge(a,-240,CORE_Z,-74,CORE_Z,DECK,4);bridge(a,74,CORE_Z,240,CORE_Z,DECK,4);
        for(int[] point:new int[][]{{54,960},{0,1210},{-240,1280},{240,1540},{0,1640}})
            a.room("熔岩栈道",point[0],DECK,point[1]);
        // Different activities occupy distinct sites; the bridges preserve open views between them.
        facility(a,-240,1170,"火焰管理所",0);
        facility(a,240,1240,"河童检修所",1);
        facility(a,-240,1580,"灰渣转运所",2);
        facility(a,240,1620,"地热换热所",3);
        overlook(a,-240,1490,-1);overlook(a,240,1460,1);
        FusionFurnace.build(a);
        GeyserCenter.buildLower(p);
        // Open bridge junctions after their railings have been drawn.
        for(int[] point:new int[][]{{54,1110},{0,1110},{-240,1110},{240,1110},{-240,1690},{0,1690},{240,1690},{-240,CORE_Z},{240,CORE_Z}}) {
            a.box(point[0]-4,DECK+1,point[1]-4,point[0]+4,DECK+3,point[1]+4,AIR);
            // Preserve the exposed outside of a corner while clearing only the walking crossing.
            for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++) {
                int x=point[0]+dx,z=point[1]+dz;BlockPos pos=local(x,DECK,z);
                if(!p.at(pos.getX(),pos.getY(),pos.getZ()).isFullCube())continue;
                boolean edge=false;
                for(EnumFacing side:EnumFacing.HORIZONTALS) {BlockPos n=pos.offset(side);edge|=!p.at(n.getX(),n.getY(),n.getZ()).isFullCube();}
                if(edge)a.block(x,DECK+1,z,RAIL);
            }
        }
        BloodPoolRemains.buildUpper(p);
        p.seal();return p;
    }
    private static void descent(GensokyoArchitecture a) {
        // Side door from the existing undercroft leaves its observation railing intact.
        a.box(24,-11,550,58,-11,558,STONE);a.box(24,-10,550,58,-3,558,AIR);
        bridge(a,54,554,54,676,-11,4);
        for(int z=677;z<=772;z++) {
            int drop=(z-677)/4+1,f=-11-drop;
            a.box(49,f-2,z,59,f,z,BASALT);a.box(50,f+1,z,58,f+8,z,AIR);
            a.block(49,f+1,z,RAIL);a.block(59,f+1,z,RAIL);
            if((z-677)%4==0)a.box(50,f+1,z,58,f+1,z,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
            if(z%16==0)for(int x:new int[]{49,59})a.block(x,f,z,LIGHT);
        }
        bridge(a,54,773,54,850,DECK,4);
        a.room("地灵殿下行廊",54,-11,630);a.room("裂谷下行台",54,DECK,802);
    }
    /** Axis-aligned bridge, with deep grounded piers, crossbeams and guarded edges. */
    static void bridge(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int f,int half) {
        boolean alongZ=x1==x2;int first=alongZ?Math.min(z1,z2):Math.min(x1,x2),last=alongZ?Math.max(z1,z2):Math.max(x1,x2);
        for(int n=first;n<=last;n++) {
            int x=alongZ?x1:n,z=alongZ?n:z1;
            a.box(x-(alongZ?half:0),f-2,z-(alongZ?0:half),x+(alongZ?half:0),f,z+(alongZ?0:half),BASALT);
            a.box(x-(alongZ?half:0),f+1,z-(alongZ?0:half),x+(alongZ?half:0),f+7,z+(alongZ?0:half),AIR);
            for(int s:new int[]{-1,1}) {
                int rx=x+(alongZ?s*half:0),rz=z+(alongZ?0:s*half);
                a.block(rx,f+1,rz,RAIL);
                if(n%24==0) {a.block(rx,f,rz,LIGHT);a.box(rx,-79,rz,rx,f-3,rz,BASALT);}
            }
            if(n%24==0)a.box(x-(alongZ?half+1:0),f-3,z-(alongZ?0:half+1),x+(alongZ?half+1:0),f-3,z+(alongZ?0:half+1),METAL);
        }
    }
    private static void overlook(GensokyoArchitecture a,int x,int z,int direction) {
        bridge(a,x,z,x+direction*72,z,DECK,3);
        int end=x+direction*72;
        a.box(end-12,DECK-2,z-14,end+12,DECK,z+14,BASALT);
        a.box(end-11,DECK+1,z-13,end+11,DECK+8,z+13,AIR);
        for(int px:new int[]{end-12,end+12})a.box(px,DECK+1,z-14,px,DECK+1,z+14,RAIL);
        for(int pz:new int[]{z-14,z+14})a.box(end-12,DECK+1,pz,end+12,DECK+1,pz,RAIL);
        a.box(end-direction*12,DECK+1,z-2,end-direction*12,DECK+2,z+2,AIR);
        a.room(direction<0?"西熔岩断崖":"东岩柱观测台",end,DECK,z);
        // A stone bench and a lit survey desk, with no interface covering the view.
        a.box(end-6,DECK+1,z+8,end+6,DECK+1,z+8,Blocks.STONE_BRICK_STAIRS.getDefaultState());
        a.block(end,DECK+1,z-8,ModBlocks.WRITING_DESK.getDefaultState());
        for(int px:new int[]{end-11,end+11})a.lamp(px,DECK,z+11);
        a.box(x-4,DECK+1,z-3,x+4,DECK+3,z+3,AIR);
    }
    private static void facility(GensokyoArchitecture a,int x,int z,String name,int use) {
        int f=DECK;
        a.box(x-27,f-4,z-29,x+27,f,z+29,BASALT);
        a.box(x-27,f+1,z-29,x+27,f+17,z+29,STONE);
        a.box(x-26,f+1,z-28,x+26,f+16,z+28,AIR);
        a.box(x-26,f+9,z-28,x-5,f+9,z+28,METAL);
        a.box(x+5,f+9,z-28,x+26,f+9,z+28,METAL);
        a.box(x-26,f+9,z+15,x+26,f+9,z+28,METAL);
        for(int px:new int[]{x-5,x+5})a.box(px,f+10,z-27,px,f+10,z+14,RAIL);
        a.box(x-5,f+10,z+15,x+5,f+10,z+15,RAIL);
        // Main bridge passes down a wide nave between working bays.
        for(int pz:new int[]{z-29,z+29})a.openZ(x,pz,f,4,8);
        for(int px:new int[]{x-27,x+27})for(int dz:new int[]{-19,0,19})
            a.box(px,f+4,z+dz-5,px,f+7,z+dz+5,Blocks.STAINED_GLASS.getStateFromMeta(5));
        for(int pz=z-24;pz<=z+24;pz+=12)for(int px:new int[]{x-26,x+26}) {
            a.box(px,f+1,pz,px,f+16,pz,METAL);a.block(px,f+8,pz,LIGHT);
        }
        // Upper deck is reached by two short flights; its walkway connects both wings.
        for(int dx:new int[]{-18,18}) {a.box(x+dx-2,f+9,z+2,x+dx+2,f+9,z+12,AIR);a.stairsSouth(x+dx,z+2,f,f+9,2);}
        for(int level:new int[]{0,9})for(int side:new int[]{-1,1}) {
            int px=x+side*16;
            a.room(name+(level==0?"·工作间":"·资料与补给"),px,f+level,z-10);
            a.chest(px-5,f+level,z-23,"hell_maintenance");a.chest(px+5,f+level,z-23,"hell_maintenance");
            a.block(px+7,f+level+1,z-11,Blocks.CRAFTING_TABLE.getDefaultState());
            a.block(px+7,f+level+1,z-8,Blocks.FURNACE.getDefaultState());
            a.box(px-7,f+level+1,z-18,px-7,f+level+3,z-5,Blocks.BOOKSHELF.getDefaultState());
            a.block(px-3,f+level+1,z-11,ModBlocks.WRITING_DESK.getDefaultState());
            a.block(px-3,f+level+1,z-5,ModBlocks.RESEARCH_NOTES.getDefaultState());
            a.box(px,f+level+7,z-10,px,f+(level==0?8:16),z-10,RAIL);a.block(px,f+level+6,z-10,ModBlocks.LIBRARY_LAMP.getDefaultState());
        }
        a.room(name+"·穿行廊",x,f,z);a.room(name+"·上层连廊",x,f+9,z+21);
        for(int side:new int[]{-1,1}) {
            int px=x+side*16;
            if(use==0) {
                a.box(px-4,f+1,z+17,px+4,f+5,z+21,Blocks.NETHER_BRICK.getDefaultState());
                for(int dx:new int[]{-2,2}) {a.block(px+dx,f+2,z+16,Blocks.LIT_FURNACE.getDefaultState());a.block(px+dx,f+4,z+16,RAIL);}
            }else if(use==1) {
                a.block(px-4,f+1,z+19,Blocks.ANVIL.getDefaultState());
                for(int dz:new int[]{15,21,25})a.box(px+3,f+1,z+dz,px+6,f+3,z+dz,METAL);
            }else if(use==2) {
                a.box(px-5,f+1,z+15,px+5,f+2,z+24,STONE);
                a.box(px-4,f+2,z+16,px+4,f+2,z+23,Blocks.SOUL_SAND.getDefaultState());
                a.chest(px-6,f,z+25,"hell_maintenance");
            }else {
                for(int dx:new int[]{-4,4}) {
                    a.box(px+dx-1,f+1,z+15,px+dx+1,f+6,z+24,Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(9));
                    a.box(px+dx,f+7,z+15,px+dx,f+7,z+25,ModBlocks.KAPPA_PIPE.getDefaultState().withProperty(net.minecraft.block.BlockRotatedPillar.AXIS,EnumFacing.Axis.Z));
                }
            }
            a.table(px-4,f+9,z+22,5);a.box(px-4,f+10,z+24,px,f+10,z+24,Blocks.STONE_BRICK_STAIRS.getDefaultState());
        }
        furnishBays(a,x,z,f,name);
        for(int px:new int[]{x-26,x+26})for(int pz:new int[]{z-28,z+28})a.box(px,-79,pz,px,f-5,pz,BASALT);
    }
    private static void furnishBays(GensokyoArchitecture a,int x,int z,int f,String name) {
        for(int side:new int[]{-1,1}) {
            int left=side<0?x-26:x+5,right=side<0?x-5:x+26,mid=x+side*16;
            // Transverse partitions separate the working benches from equipment and rest bays.
            for(int level:new int[]{0,9}) {
                a.box(left,f+level+1,z-1,right,f+level+7,z-1,STONE);
                a.openZ(mid,z-1,f+level,2,5);
                for(int dz:new int[]{-25,-19,-4})a.box(left,f+level+7,z+dz,right,f+level+7,z+dz,METAL);
                a.box(mid-4,f+level+1,z-19,mid+3,f+level+1,z-19,Blocks.STONE_SLAB.getStateFromMeta(8));
            }
            // Low workbenches, tray storage and a clear central working aisle.
            a.box(mid-4,f+1,z-17,mid+2,f+1,z-16,METAL);
            for(int dx:new int[]{-4,-1,2})a.block(mid+dx,f+2,z-16,Blocks.FLOWER_POT.getDefaultState());
            a.box(mid-4,f+1,z-5,mid+1,f+1,z-5,Blocks.STONE_SLAB.getStateFromMeta(8));
            for(int dx:new int[]{-3,0,3}) {
                a.box(mid+dx,f+10,z-17,mid+dx,f+12,z-15,Blocks.BOOKSHELF.getDefaultState());
                a.block(mid+dx,f+13,z-16,Blocks.STONE_SLAB.getDefaultState());
            }
            a.box(mid-7,f+10,z+16,mid-7,f+16,z+27,STONE);
            a.openX(mid-7,z+21,f+9,1,4);
            a.bed(mid-9,f+9,z+25);a.chest(mid-10,f+9,z+18,"hell_maintenance");
            a.room(name+"·轮值休息",mid-9,f+9,z+21);
            a.room(name+"·设备间",mid,f,z+12);
            // Mechanical housings have visible rails and closed tops rather than loose stacks.
            a.box(mid-6,f+8,z+15,mid+6,f+8,z+26,METAL);
            for(int dx:new int[]{-6,6})a.box(mid+dx,f+1,z+15,mid+dx,f+7,z+15,RAIL);
            a.block(mid,f+8,z+23,LIGHT);
        }
        // Two narrow clerestories break the flat industrial roof.
        for(int side:new int[]{-1,1}) {
            int px=x+side*16;
            a.box(px-4,f+17,z-22,px+4,f+19,z+23,BASALT);
            a.box(px-3,f+17,z-21,px+3,f+18,z+22,Blocks.STAINED_GLASS.getStateFromMeta(5));
            a.box(px-5,f+20,z-23,px+5,f+20,z+24,METAL);
        }
    }
}
