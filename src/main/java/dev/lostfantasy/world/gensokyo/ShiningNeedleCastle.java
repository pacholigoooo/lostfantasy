package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Inverted keep, rooms and furnishings. Access to the upside-down fittings is by flight. */
final class ShiningNeedleCastle {
    static final GensokyoAtlas SITE=GensokyoAtlas.SHINING_NEEDLE;
    private static final IBlockState TILE=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(15),
            MAT=Blocks.WOOL.getStateFromMeta(5),BORDER=Blocks.WOOL.getStateFromMeta(13),
            RAIL=Blocks.DARK_OAK_FENCE.getDefaultState(),LIME=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(0);
    private ShiningNeedleCastle() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,SITE);
        a.box(-82,-44,-42,82,49,78,AIR);
        tier(a,0,0,40,32,20,31,30,24);
        tier(a,0,0,30,24,3,14,22,17);
        tier(a,0,0,22,17,-14,-3,12,13);
        tier(a,0,0,12,13,-31,-20,0,0);
        crown(a,0,0,40,32,32,47);
        for(int side:new int[]{-1,1}) {
            tier(a,side*60,0,15,22,20,31,12,18);
            tier(a,side*60,0,12,18,3,14,0,0);
            crown(a,side*60,0,15,22,32,41);
            galleryX(a,side,20,40,45);galleryX(a,side,3,30,48);
        }
        tier(a,0,55,18,14,20,31,15,14);
        tier(a,0,55,15,14,3,14,0,0);
        crown(a,0,55,18,14,32,40);
        galleryZ(a,20,32,41);galleryZ(a,3,24,41);
        upperRooms(a);middleRooms(a);lowerRooms(a);lookout(a);sideTowers(a);gatehouse(a);
        for(int bottom:new int[]{-31,-14,3})stair(a,0,0,bottom,bottom+17);
        for(int x:new int[]{-60,60})stair(a,x,0,3,20);
        stair(a,0,55,3,20);
    }
    private static void tier(GensokyoArchitecture a,int cx,int cz,int hx,int hz,int floor,int ceiling,int nextX,int nextZ) {
        a.box(cx-hx,floor,cz-hz,cx+hx,ceiling,cz+hz,LIME);
        a.box(cx-hx+1,floor+1,cz-hz+1,cx+hx-1,ceiling-1,cz+hz-1,AIR);
        a.box(cx-hx,floor,cz-hz,cx+hx,floor,cz+hz,DARK);
        a.box(cx-hx,ceiling,cz-hz,cx+hx,ceiling,cz+hz,WOOD);
        // The former ceiling is underfoot; its beam grid is part of the walking surface.
        for(int x=cx-hx+8;x<cx+hx;x+=8)a.box(x,floor,cz-hz+1,x,floor,cz+hz-1,WOOD);
        for(int z=cz-hz+8;z<cz+hz;z+=8)a.box(cx-hx+1,floor,z,cx+hx-1,floor,z,WOOD);
        // Former tatami floors now face downward, divided by their dark woven borders.
        for(int x=cx-hx+2;x<cx+hx-3;x+=6)for(int z=cz-hz+2;z<cz+hz-4;z+=8) {
            a.box(x,ceiling,z,Math.min(x+4,cx+hx-2),ceiling,Math.min(z+6,cz+hz-2),MAT);
            a.box(x,ceiling,z,Math.min(x+4,cx+hx-2),ceiling,z,BORDER);
        }
        for(int x=cx-hx;x<=cx+hx;x+=8)for(int z:new int[]{cz-hz,cz+hz})a.box(x,floor+1,z,x,ceiling,z,LOG);
        for(int z=cz-hz;z<=cz+hz;z+=8)for(int x:new int[]{cx-hx,cx+hx})a.box(x,floor+1,z,x,ceiling,z,LOG);
        for(int y:new int[]{floor+1,ceiling-1}) {
            a.box(cx-hx,y,cz-hz,cx+hx,y,cz-hz,DARK);a.box(cx-hx,y,cz+hz,cx+hx,y,cz+hz,DARK);
            a.box(cx-hx,y,cz-hz,cx-hx,y,cz+hz,DARK);a.box(cx+hx,y,cz-hz,cx+hx,y,cz+hz,DARK);
        }
        for(int x=cx-hx+4;x<=cx+hx-4;x+=8)for(int z:new int[]{cz-hz,cz+hz}) {
            a.box(x-1,ceiling-6,z,x+1,ceiling-3,z,PAPER);a.box(x-1,ceiling-2,z,x+1,ceiling-2,z,DARK);
            // Window mouldings follow the inverted floor, with brackets facing downward.
            for(int edge:new int[]{x-2,x+2})a.block(edge,ceiling-2,z,
                    Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,edge<x?EnumFacing.WEST:EnumFacing.EAST)
                            .withProperty(BlockStairs.HALF,BlockStairs.EnumHalf.TOP));
        }
        for(int z=cz-hz+4;z<=cz+hz-4;z+=8)for(int x:new int[]{cx-hx,cx+hx}) {
            a.box(x,ceiling-6,z-1,x,ceiling-3,z+1,PAPER);a.box(x,ceiling-2,z-1,x,ceiling-2,z+1,DARK);
        }
        for(int z=cz-hz+4;z<cz+hz-3;z+=8)for(int x:new int[]{cx-hx+1,cx+hx-1})
            a.box(x,floor+1,z,x,floor+1,z+2,Blocks.DARK_OAK_STAIRS.getDefaultState()
                    .withProperty(BlockStairs.FACING,x<cx?EnumFacing.WEST:EnumFacing.EAST));
        invertedRoof(a,cx,cz,hx+4,hz+4,floor-1,nextX,nextZ);
        for(int x:new int[]{cx-Math.max(9,hx-6),cx+Math.max(9,hx-6)})for(int z:new int[]{cz-Math.max(10,hz-6),cz+Math.max(10,hz-6)})lamp(a,x,floor,z);
    }
    private static void invertedRoof(GensokyoArchitecture a,int cx,int cz,int hx,int hz,int y,int holeX,int holeZ) {
        for(int x=-hx;x<=hx;x++)for(int z=-hz;z<=hz;z++) {
            if(holeX>0 && Math.abs(x)<=holeX && Math.abs(z)<=holeZ)continue;
            int edge=Math.min(hx-Math.abs(x),hz-Math.abs(z));int drop=Math.min(11,edge/2);
            a.block(cx+x,y-drop,cz+z,TILE);
            if(edge==0)a.block(cx+x,y+1,cz+z,Blocks.STONE_SLAB.getStateFromMeta(8));
        }
        // Downward gable ridges distinguish the keep from a stack of plain platforms.
        for(int side:new int[]{-1,1})for(int x=-7;x<=7;x++) {
            int drop=6-Math.abs(x)/2;
            for(int z=hz-7;z<=hz;z++) {
                if(holeX>0 && Math.abs(x)<=holeX && z<=holeZ)continue;
                a.block(cx+x,y-drop,cz+side*z,TILE);
                if(z==hz && drop>0)a.box(cx+x,y-drop+1,cz+side*z,cx+x,y,cz+side*z,LIME);
            }
        }
        if(holeX==0)for(int z:new int[]{-hz+2,hz-2})a.block(cx,y-8,cz+z,Blocks.GOLD_BLOCK.getDefaultState());
    }
    private static void crown(GensokyoArchitecture a,int cx,int cz,int hx,int hz,int from,int to) {
        for(int y=from;y<=to;y++) {
            int lip=(y-from)/4;
            a.box(cx-hx-lip,y,cz-hz-lip,cx+hx+lip,y,cz+hz+lip,STONE);
            if(y<to && (y-from)%3==0) {
                for(int x=cx-hx-lip+2;x<cx+hx+lip;x+=5)for(int z:new int[]{cz-hz-lip,cz+hz+lip})a.block(x,y,z,Blocks.COBBLESTONE.getDefaultState());
            }
        }
        a.box(cx-hx-3,to+1,cz-hz-3,cx+hx+3,to+1,cz+hz+3,Blocks.STONE_SLAB.getDefaultState());
    }
    private static void galleryX(GensokyoArchitecture a,int side,int f,int inner,int outer) {
        a.box(side*inner,f,-4,side*outer,f+10,4,LIME);a.box(side*inner,f+1,-3,side*outer,f+9,3,AIR);
        a.box(side*inner,f,-4,side*outer,f,4,DARK);a.box(side*inner,f+10,-4,side*outer,f+10,4,MAT);
        for(int z:new int[]{-4,4})a.box(side*inner,f+4,z,side*outer,f+7,z,PAPER);
        a.box(side*inner,f-1,-6,side*outer,f-1,6,TILE);
        a.room((side<0?"西":"东")+"连廊"+f,side*(inner+outer)/2,f,0);
    }
    private static void galleryZ(GensokyoArchitecture a,int f,int from,int to) {
        a.box(-4,f,from,4,f+10,to,LIME);a.box(-3,f+1,from,3,f+9,to,AIR);
        a.box(-4,f,from,4,f,to,DARK);a.box(-4,f+10,from,4,f+10,to,MAT);
        for(int x:new int[]{-4,4})a.box(x,f+4,from,x,f+7,to,PAPER);
        a.box(-6,f-1,from,6,f-1,to,TILE);a.room("门楼连廊"+f,0,f,(from+to)/2);
    }
    private static void partition(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int f,int height) {
        a.box(x1,f+1,z1,x2,f+height,z2,LIME);a.box(x1,f+1,z1,x2,f+1,z2,WOOD);
    }
    private static void doorX(GensokyoArchitecture a,int x,int z,int f,int half,int height) {
        a.box(x,f+11-height,z-half,x,f+10,z+half,AIR);
        a.box(x,f+11-height-1,z-half,x,f+11-height-1,z+half,WOOD);
    }
    private static void doorZ(GensokyoArchitecture a,int x,int z,int f,int half,int height) {
        a.box(x-half,f+11-height,z,x+half,f+10,z,AIR);
        a.box(x-half,f+11-height-1,z,x+half,f+11-height-1,z,WOOD);
    }
    private static void chest(GensokyoArchitecture a,int x,int f,int z,String loot) {
        a.block(x,f+10,z,ModBlocks.CEILING_CHEST.getDefaultState());
        a.plan.container(SITE.x+x,SITE.y+f+10,SITE.z+z,new net.minecraft.util.ResourceLocation("lostfantasy","chests/"+loot));
    }
    private static void bed(GensokyoArchitecture a,int x,int f,int z) {
        IBlockState bed=ModBlocks.CEILING_BED.getDefaultState().withProperty(BlockBed.FACING,EnumFacing.NORTH);
        a.block(x,f+10,z,bed.withProperty(BlockBed.PART,BlockBed.EnumPartType.FOOT));
        a.block(x,f+10,z-1,bed.withProperty(BlockBed.PART,BlockBed.EnumPartType.HEAD));
    }
    private static void upperRooms(GensokyoArchitecture a) {
        int f=20;
        for(int side:new int[]{-1,1}) {
            partition(a,side*16,-30,side*16,30,f,10);partition(a,side*28,-30,side*28,30,f,10);
            for(int z:new int[]{-12,6})partition(a,side*17,z,side*39,z,f,10);
            for(int z:new int[]{-21,-3,18}) {
                doorX(a,side*16,z,f,2,5);doorX(a,side*28,z,f,2,5);
                String[] uses=side<0?new String[]{"藏书阅卷","服饰收存","茶事接待","布料裁剪","修缮用具","备用器皿"}:
                        new String[]{"画卷记录","家用器物","会客暖间","笔墨装订","食器备餐","布草收存"};
                int index=z== -21?0:z== -3?1:2;
                a.room(uses[index],side*23,f,z+1);a.room(uses[index+3],side*33,f,z);
                upperFixtures(a,side,index,f,z);
                chest(a,side*25,f,z+5,z<0?"castle_records":"castle_household");
                chest(a,side*35,f,z+5,z<0?"castle_crafts":"castle_household");
                lowTable(a,side*22,f,z+2,3);lowTable(a,side*33,f,z+2,3);
                lamp(a,side*22,f,z);lamp(a,side*34,f,z);
            }
            doorX(a,side*40,0,f,3,8);doorX(a,side*28,0,f,3,8);doorX(a,side*16,0,f,3,8);
        }
        partition(a,-15,-13,15,-13,f,10);doorZ(a,0,-13,f,3,6);
        for(int x:new int[]{-10,0,10}) {shelves(a,x,f,-28,7);chest(a,x,f,-23,"castle_records");}
        a.box(-10,f+10,24,10,f+10,27,WOOD);a.box(-9,f+9,25,9,f+9,26,Blocks.WOODEN_SLAB.getStateFromMeta(8));
        for(int x:new int[]{-10,10})for(int z:new int[]{9,16})lowTable(a,x,f,z,5);
        doorZ(a,0,32,f,3,8);a.room("倒悬大殿",0,f,15);a.room("会见上座",0,f,23);
        a.room("北侧书卷殿",0,f,-19);a.room("大殿西通道",-11,f,0);a.room("大殿东通道",11,f,0);
        for(int x:new int[]{-11,11})for(int z:new int[]{-18,11,23})lamp(a,x,f,z);
    }
    private static void upperFixtures(GensokyoArchitecture a,int side,int index,int f,int z) {
        int inner=side*22,outer=side*34;
        if(index==0) {
            shelves(a,inner,f,z-6,7);
            a.block(inner,f+10,z-3,ModBlocks.CEILING_WRITING_DESK.getDefaultState());
            if(side<0) {
                clothRack(a,outer,f,z-7);
                a.box(outer-2,f+10,z-4,outer+2,f+10,z-3,WOOD);
                a.box(outer-1,f+9,z-4,outer+1,f+9,z-3,Blocks.WOOL.getStateFromMeta(14));
                a.block(outer+3,f+10,z-4,ModBlocks.CEILING_WORKBENCH.getDefaultState());
            } else {
                shelves(a,outer,f,z-7,5);
                a.block(outer-1,f+10,z-4,ModBlocks.CEILING_WRITING_DESK.getDefaultState());
                a.block(outer+2,f+10,z-4,ModBlocks.CEILING_WORKBENCH.getDefaultState());
            }
        } else if(index==1) {
            wardrobe(a,inner-2,f,z-6);wardrobe(a,inner+2,f,z-6);
            if(side<0) {
                a.box(outer-3,f+10,z-6,outer+3,f+10,z-6,WOOD);
                a.block(outer-2,f+10,z-4,ModBlocks.CEILING_WORKBENCH.getDefaultState());
                a.block(outer+2,f+10,z-4,ModBlocks.CEILING_WORKBENCH.getDefaultState());
            } else {
                kitchen(a,outer,f,z-6);
            }
        } else {
            a.box(inner-3,f+8,z-7,inner+3,f+10,z-7,WOOD);
            a.box(inner-2,f+9,z-7,inner+2,f+9,z-7,PAPER);
            if(side<0) {
                kitchen(a,outer,f,z-6);
            } else {
                clothRack(a,outer,f,z-6);
                wardrobe(a,outer,f,z-3);
            }
        }
    }
    private static void middleRooms(GensokyoArchitecture a) {
        int f=3;
        for(int side:new int[]{-1,1}) {
            partition(a,side*9,-22,side*9,22,f,10);
            for(int z:new int[]{-7,8})partition(a,side*10,z,side*29,z,f,10);
            for(int z:new int[]{-16,1,16}) {
                doorX(a,side*9,z,f,2,5);a.room((side<0?"西":"东")+"起居间"+z,side*15,f,z);
                chest(a,side*24,f,z+3,"castle_household");lowTable(a,side*15,f,z+2,5);
                if(z== -16) {bed(a,side*25,f,z-2);wardrobe(a,side*20,f,z-4);}
                else if(z==16) {bed(a,side*25,f,z);wardrobe(a,side*20,f,z-4);}
                else {shelves(a,side*23,f,z-4,7);a.block(side*15,f+10,z-3,ModBlocks.CEILING_WRITING_DESK.getDefaultState());}
                lamp(a,side*16,f,z);
            }
            doorX(a,side*30,0,f,3,8);doorX(a,side*9,0,f,3,8);
        }
        for(int x:new int[]{-5,5})lowTable(a,x,f,17,5);
        doorZ(a,0,24,f,3,8);a.room("中层茶食厅",0,f,16);a.room("中层后回廊",0,f,-18);
        shelves(a,0,f,-22,11);lamp(a,0,f,20);lamp(a,0,f,-17);
    }
    private static void lowerRooms(GensokyoArchitecture a) {
        int f=-14;
        for(int side:new int[]{-1,1}) {
            partition(a,side*9,-15,side*9,15,f,10);partition(a,side*10,0,side*21,0,f,10);
            for(int z:new int[]{-8,8}) {
                doorX(a,side*9,z,f,2,5);a.room((side<0?"西":"东")+(z<0?"裁缝器物间":"留宿小间"),side*15,f,z);
                chest(a,side*18,f,z+4,z<0?"castle_crafts":"castle_household");
                if(z>0){bed(a,side*18,f,z);wardrobe(a,side*13,f,z-3);}
                else {a.block(side*18,f+10,z-3,ModBlocks.CEILING_WORKBENCH.getDefaultState());a.block(side*14,f+10,z-3,ModBlocks.CEILING_WORKBENCH.getDefaultState());}
                lowTable(a,side*15,f,z+2,3);lamp(a,side*16,f,z);
            }
        }
        a.room("下层前厅",0,f,11);a.room("下层西廊",-8,f,4);a.room("下层东廊",8,f,4);
        for(int x:new int[]{-4,4})a.block(x,f+10,13,ModBlocks.CEILING_LACQUER_BOWL.getDefaultState());
        lamp(a,0,f,12);
    }
    private static void lookout(GensokyoArchitecture a) {
        int f=-31;
        for(int x:new int[]{-9,9}) {
            lowTable(a,x,f,7,3);chest(a,x,f,-8,"castle_records");
            a.room((x<0?"西":"东")+"望楼回廊",x,f,0);
        }
        a.block(0,f+10,10,ModBlocks.CEILING_LACQUER_BOWL.getDefaultState());
        a.room("天守阁观景席",0,f,8);lamp(a,0,f,9);
    }
    private static void sideTowers(GensokyoArchitecture a) {
        for(int cx:new int[]{-60,60})for(int f:new int[]{3,20}) {
            int hz=f==20?22:18,hx=f==20?15:12;String title=cx<0?"西楼":"东楼";
            for(int z:new int[]{-10,10}) {
                partition(a,cx-hx+1,z,cx+hx-1,z,f,10);
                if(z<0)for(int side:new int[]{-1,1})doorZ(a,cx+side*9,z,f,1,5);
                else doorZ(a,cx,z,f,2,5);
            }
            for(int z:new int[]{-hz+5,hz-5}) {
                String use=z<0?(cx<0?"裁剪与修补":"抄写与器物装订"):(cx<0?"布草与日用收存":"备餐与食器");
                a.room(title+use+f,cx+4,f,z);
                if(z<0) {
                    if(cx<0)clothRack(a,cx,f,z-3);else shelves(a,cx,f,z-3,11);
                    a.block(cx-4,f+10,z-2,cx<0?ModBlocks.CEILING_WORKBENCH.getDefaultState():ModBlocks.CEILING_WRITING_DESK.getDefaultState());
                } else if(cx<0) {
                    clothRack(a,cx,f,z+2);
                } else {
                    kitchen(a,cx,f,z+2);
                }
                chest(a,cx-8,f,z,"castle_crafts");chest(a,cx+8,f,z,"castle_household");
                lowTable(a,cx,f,z,5);lamp(a,cx-8,f,z-2);lamp(a,cx+8,f,z-2);
            }
            doorX(a,cx<0?cx+(f==20?15:12):cx-(f==20?15:12),0,f,3,8);
        }
    }
    private static void gatehouse(GensokyoArchitecture a) {
        for(int f:new int[]{3,20}) {
            doorZ(a,0,41,f,3,8);
            for(int x:new int[]{-11,11}) {
                chest(a,x,f,63,"castle_household");a.block(x,f+10,47,ModBlocks.CEILING_WRITING_DESK.getDefaultState());
                a.room((x<0?"门楼接待":"门楼收存")+f,x,f,55);lamp(a,x,f,58);
            }
        }
        doorZ(a,0,69,20,4,8);a.box(-8,31,70,8,31,75,DARK);
        for(int x:new int[]{-8,8})a.box(x,30,70,x,30,75,RAIL);
        a.room("倒悬门廊入口",0,20,73);a.room("门楼前厅",0,20,65);
        a.sign(0,28,70,EnumFacing.SOUTH,"辉针城","");
    }
    private static void stair(GensokyoArchitecture a,int cx,int cz,int bottom,int top) {
        for(int x:new int[]{cx-7,cx+7})a.box(x,bottom+1,cz-9,x,top+10,cz+7,LIME);
        for(int z:new int[]{cz-9,cz+7})a.box(cx-7,bottom+1,z,cx+7,top+10,z,LIME);
        a.box(cx-6,bottom+11,cz-8,cx+6,top+10,cz+6,AIR);
        for(int i=1;i<=9;i++)step(a,cx-4,cz+4-i,top+11-i,EnumFacing.NORTH);
        a.box(cx-5,top+2,cz-7,cx+5,top+2,cz-6,WOOD);
        a.box(cx-5,top+1,cz-8,cx+5,top+1,cz-8,RAIL);
        for(int i=1;i<=8;i++)step(a,cx+4,cz-6+i,top+2-i,EnumFacing.SOUTH);
        a.box(cx+3,bottom+11,cz+3,cx+5,bottom+11,cz+4,WOOD);
        a.box(cx-5,top+11,cz+4,cx-3,top+11,cz+4,WOOD);
        a.box(cx-7,bottom+11,cz+5,cx+7,bottom+11,cz+7,DARK);
        a.box(cx-7,top+11,cz+5,cx+7,top+11,cz+7,DARK);
        for(int floor:new int[]{bottom,top}) {
            for(int x:new int[]{cx-7,cx+7})doorX(a,x,cz+5,floor,1,4);
            doorZ(a,cx,cz+7,floor,2,4);
        }
        a.room("折返梯落台"+cx+":"+cz+":"+bottom,cx,bottom,cz+6);
        if(top==20)a.room("折返梯落台"+cx+":"+cz+":"+top,cx,top,cz+6);
    }
    private static void step(GensokyoArchitecture a,int x,int z,int y,EnumFacing facing) {
        a.box(x-1,y+1,z,x+1,y+1,z,WOOD);
        a.box(x-1,y,z,x+1,y,z,Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,facing).withProperty(BlockStairs.HALF,BlockStairs.EnumHalf.TOP));
        a.block(x+(facing==EnumFacing.NORTH?2:-2),y-1,z,RAIL);
    }
    private static void lowTable(GensokyoArchitecture a,int x,int f,int z,int length) {
        for(int dx:new int[]{-length/2,length/2})a.box(x+dx,f+9,z,x+dx,f+10,z,LOG);
        a.box(x-length/2,f+8,z,x+length/2,f+8,z,Blocks.WOODEN_SLAB.getStateFromMeta(8));
        for(int dx:new int[]{-1,1})for(int dz:new int[]{-2,2})a.block(x+dx,f+11,z+dz,Blocks.WOOL.getStateFromMeta(14));
    }
    private static void shelves(GensokyoArchitecture a,int x,int f,int z,int width) {
        a.box(x-width/2,f+8,z,x+width/2,f+10,z,ModBlocks.CEILING_BOOKSHELF.getDefaultState());
        a.box(x-width/2,f+7,z,x+width/2,f+7,z,Blocks.WOODEN_SLAB.getStateFromMeta(8));
    }
    private static void wardrobe(GensokyoArchitecture a,int x,int f,int z) {
        a.box(x-1,f+8,z,x+1,f+10,z,WOOD);a.box(x-1,f+7,z,x+1,f+7,z,Blocks.WOODEN_SLAB.getStateFromMeta(8));
    }
    private static void clothRack(GensokyoArchitecture a,int x,int f,int z) {
        for(int dx:new int[]{-3,3})a.box(x+dx,f+7,z,x+dx,f+10,z,LOG);
        for(int y:new int[]{f+8,f+10})a.box(x-2,y,z,x+2,y,z,Blocks.WOODEN_SLAB.getStateFromMeta(8));
        for(int dx:new int[]{-1,1})for(int y:new int[]{f+7,f+9})
            a.block(x+dx,y,z,Blocks.WOOL.getStateFromMeta(dx<0?0:14));
    }
    private static void kitchen(GensokyoArchitecture a,int x,int f,int z) {
        a.box(x-3,f+10,z,x+3,f+10,z,WOOD);
        a.block(x-2,f+10,z,ModBlocks.CEILING_WORKBENCH.getDefaultState());
        for(int dx:new int[]{-1,0,1})a.block(x+dx,f+9,z,Blocks.WOODEN_SLAB.getStateFromMeta(8));
    }
    private static void lamp(GensokyoArchitecture a,int x,int f,int z) {
        a.block(x,f+10,z,LOG);a.block(x,f+9,z,ModBlocks.CEILING_LANTERN.getDefaultState());
    }
}
