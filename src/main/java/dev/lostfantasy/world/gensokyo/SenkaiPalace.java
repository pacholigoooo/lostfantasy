package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A furnished dojo, twin octagonal towers and the relocated mausoleum in the rear garden. */
final class SenkaiPalace {
    private static final IBlockState TILE=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(4),
            PILLAR=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(14),GILT=Blocks.GOLD_BLOCK.getDefaultState();
    private SenkaiPalace() {}
    static GensokyoBlueprint create() {
        GensokyoBlueprint p=new GensokyoBlueprint();GensokyoArchitecture a=new GensokyoArchitecture(p,GensokyoAtlas.MYOUREN,0,0,"神灵庙");
        a.box(-116,-3,-116,116,0,106,STONE);a.box(-116,1,-116,116,65,106,AIR);
        a.box(-110,0,-110,110,0,100,Blocks.GRASS.getDefaultState());
        a.box(-82,0,20,82,0,91,STONE);a.box(-8,0,-107,8,0,102,STONE);
        a.box(-100,0,-48,100,0,-38,STONE);a.box(-105,0,-105,-41,0,-51,STONE);
        hall(a);tower(a,-62,35,15,false);tower(a,62,35,15,false);tower(a,-77,-79,23,true);
        home(a,18,-106,53,-67,new String[]{"神子书斋","青娥静室"},true);home(a,62,-106,100,-67,new String[]{"布都起居","屠自古起居"},false);
        service(a);garden(a);
        a.room("前庭",0,0,74);a.room("后庭",0,0,-43);a.room("归路",0,0,98);
        p.seal();return p;
    }
    private static void hall(GensokyoArchitecture a) {
        a.box(-45,1,-29,45,3,26,STONE);a.box(-44,4,-28,44,4,25,WOOD);
        a.box(-43,5,-27,43,20,24,WHITE);a.box(-42,5,-26,42,19,23,AIR);a.box(-43,20,-27,43,20,24,WOOD);
        for(int x:new int[]{-43,-29,-15,0,15,29,43})for(int z:new int[]{-27,24}) {
            a.box(x,5,z,x,21,z,PILLAR);a.box(x-1,17,z-1,x+1,18,z+1,GILT);
        }
        for(int x:new int[]{-36,-22,22,36})for(int z:new int[]{-27,24}) {
            a.box(x-4,7,z,x+4,13,z,DARK);a.box(x-3,8,z,x+3,12,z,PAPER);
        }
        for(int x:new int[]{-43,43})for(int z:new int[]{-18,-5,9}) {
            a.box(x,6,z-4,x,14,z+4,DARK);a.box(x,7,z-3,x,13,z+3,PAPER);
        }
        a.openZ(0,24,4,6,8);a.openZ(0,-27,4,4,6);a.stairsSouth(0,27,0,4,9);
        // Rear entrance descends northwards to the garden.
        for(int i=1;i<=4;i++)a.box(-5,1,-28-i,5,5-i,-28-i,STONE);
        a.box(-5,4,-28,5,4,-28,STONE);
        for(int x:new int[]{-15,15}) {a.box(x,5,-26,x,14,23,WHITE);a.openX(x,5,4,2,5);}
        a.box(-10,5,-23,10,5,-14,DARK);a.box(-8,6,-21,8,6,-19,WOOD);
        a.block(0,7,-20,ModBlocks.WRITING_DESK.getDefaultState());
        for(int x:new int[]{-8,0,8})for(int z:new int[]{-8,1,10})a.box(x-2,5,z-1,x+2,5,z+1,Blocks.CARPET.getStateFromMeta(4));
        for(int x:new int[]{-38,-28})for(int z:new int[]{-16,-4,8})VillageJoinery.lowDesk(a,x,4,z,6);
        for(int z=-21;z<=17;z+=9) {a.box(38,5,z,40,8,z+4,Blocks.BOOKSHELF.getDefaultState());a.chest(22,4,z,"school_supplies");}
        a.box(-40,5,-25,-19,8,-25,Blocks.BOOKSHELF.getDefaultState());
        for(int x:new int[]{28,33})for(int z:new int[]{-22,-8,9})a.box(x,5,z,x,8,z+8,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.lowDesk(a,26,4,4,9);
        for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++)if(x*x+z*z<=81) {
            boolean dark=x<0;
            if(x*x+(z-4)*(z-4)<16)dark=true;
            if(x*x+(z+4)*(z+4)<16)dark=false;
            if(x*x+(z-4)*(z-4)<=1)dark=false;
            if(x*x+(z+4)*(z+4)<=1)dark=true;
            a.block(x,4,z+1,dark?DARK:WHITE);
        }
        for(int x:new int[]{-31,0,31})for(int z:new int[]{-15,8})lamp(a,x,17,z,20);
        roof(a,-48,-32,48,29,22,TILE);roof(a,-34,-24,34,21,30,TILE);roof(a,-20,-14,20,11,37,TILE);
        a.box(-5,14,25,5,15,25,DARK);a.sign(0,14,26,EnumFacing.SOUTH,"神灵庙","");
        a.room("讲道与修行",0,4,5);a.room("讲席",0,5,-16);a.room("抄录课堂",-19,4,5);a.room("道藏",19,4,5);
    }
    private static boolean oct(int x,int z,int r) {return Math.max(Math.abs(x),Math.abs(z))<=r && Math.abs(x)+Math.abs(z)<=r*3/2;}
    private static void tower(GensokyoArchitecture a,int cx,int cz,int r,boolean old) {
        IBlockState wall=old?DARK:WHITE,roof=old?ROOF:TILE;
        for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)if(oct(dx,dz,r)) {
            a.box(cx+dx,1,cz+dz,cx+dx,3,cz+dz,STONE);a.box(cx+dx,4,cz+dz,cx+dx,40,cz+dz,wall);
            if(oct(dx,dz,r-1))a.box(cx+dx,5,cz+dz,cx+dx,39,cz+dz,AIR);
            for(int f:new int[]{4,16,28,40})a.block(cx+dx,f,cz+dz,WOOD);
        }
        for(int f:new int[]{4,16,28}) {
            for(int d:new int[]{-1,1}) {
                a.box(cx-4,f+3,cz+d*r,cx+4,f+7,cz+d*r,PAPER);a.box(cx+d*r,f+3,cz-4,cx+d*r,f+7,cz+4,PAPER);
                a.box(cx-5,f+1,cz+d*r,cx-5,f+10,cz+d*r,old?LOG:PILLAR);
                a.box(cx+5,f+1,cz+d*r,cx+5,f+10,cz+d*r,old?LOG:PILLAR);
            }
            for(int n=0;n<5;n++)for(int dx=-r-3+n;dx<=r+3-n;dx++)for(int dz=-r-3+n;dz<=r+3-n;dz++) {
                int edge=r+3-n;if(oct(dx,dz,edge) && !oct(dx,dz,edge-1))a.block(cx+dx,f+10+n/2,cz+dz,roof);
            }
            a.chest(cx+2,f,cz-3,old?"hieda_records":"school_supplies");a.chest(cx+5,f,cz-3,"bookbinding");
            VillageJoinery.lowDesk(a,cx-4,f,cz+2,6);a.box(cx-4,f+1,cz-4,cx-1,f+3,cz-4,Blocks.BOOKSHELF.getDefaultState());
            lamp(a,cx,f+8,cz,f+12);
            if(old) {
                if(f==4) {
                    a.box(cx-7,5,cz-12,cx+7,6,cz-8,STONE);a.box(cx-5,7,cz-11,cx+5,7,cz-9,DARK);
                    for(int x:new int[]{cx-4,cx,cx+4})a.block(x,8,cz-10,ModBlocks.LACQUER_BOWL.getDefaultState());
                    for(int x:new int[]{cx-8,cx+8})for(int z:new int[]{cz+8,cz+12})a.box(x-2,5,z-1,x+2,5,z+1,Blocks.CARPET.getStateFromMeta(13));
                } else if(f==16) {
                    for(int x:new int[]{cx-10,cx+10})for(int z:new int[]{cz-12,cz+5})a.box(x,f+1,z,x,f+4,z+7,Blocks.BOOKSHELF.getDefaultState());
                    for(int z:new int[]{cz-11,cz+10})VillageJoinery.lowDesk(a,cx-3,f,z,7);
                } else {
                    for(int x:new int[]{cx-9,cx+9})for(int z:new int[]{cz-9,cz+9}) {
                        a.box(x-3,f+1,z-2,x+3,f+1,z+2,Blocks.CARPET.getStateFromMeta(4));
                        a.box(x-3,f+2,z+3,x+3,f+4,z+3,DARK);
                    }
                }
            } else {
                a.box(cx-6,f+1,cz-5,cx-6,f+4,cz+4,Blocks.BOOKSHELF.getDefaultState());
                a.box(cx+6,f+1,cz-5,cx+6,f+3,cz+4,DARK);
                for(int z:new int[]{cz-2,cz+3})a.block(cx+6,f+4,z,ModBlocks.LACQUER_BOWL.getDefaultState());
                a.box(cx-5,f+1,cz+6,cx+5,f+1,cz+6,Blocks.SPRUCE_STAIRS.getDefaultState());
            }
            String[] uses=old?new String[]{"祀仪厅","旧典收藏","静修层"}:new String[]{"茶席与器物","抄典室","观景静室"};
            a.room((old?"梦殿大祀庙":cx<0?"西塔":"东塔")+"·"+uses[(f-4)/12],cx,f,cz);
        }
        // Broad perimeter ramps leave a furnished central room on each level.
        int rad=r-5,len=rad*2;
        for(int level=0;level<2;level++)for(int i=0;i<len*4;i++) {
            int side=i/len,t=i%len,x=side==0?-rad+t:side==1?rad:side==2?rad-t:-rad;
            int z=side==0?-rad:side==1?-rad+t:side==2?rad:rad-t,f=4+12*level+i*12/(len*4);
            int wx=side%2,wz=1-wx;
            boolean rise=i==0?level>0:i*12/(len*4)>(i-1)*12/(len*4);
            EnumFacing direction=i==0?EnumFacing.NORTH:new EnumFacing[]{EnumFacing.EAST,EnumFacing.SOUTH,EnumFacing.WEST,EnumFacing.NORTH}[side];
            IBlockState step=rise?Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(net.minecraft.block.BlockStairs.FACING,direction):WOOD;
            a.box(cx+x-wx,f,cz+z-wz,cx+x+wx,f,cz+z+wz,step);a.box(cx+x-wx,f+1,cz+z-wz,cx+x+wx,f+4,cz+z+wz,AIR);
            if(i>6 && i<len*4-6) {
                int rx=side==1?-2:side==3?2:0,rz=side==0?2:side==2?-2:0;
                a.block(cx+x+rx,f,cz+z+rz,WOOD);a.block(cx+x+rx,f+1,cz+z+rz,Blocks.SPRUCE_FENCE.getDefaultState());
            }
        }
        a.openZ(cx,cz+r,4,3,5);a.stairsSouth(cx,cz+r+1,0,4,4);
        for(int n=0;n<=r;n++)for(int dx=-r+n;dx<=r-n;dx++)for(int dz=-r+n;dz<=r-n;dz++)
            if(oct(dx,dz,r-n) && !oct(dx,dz,r-n-1))a.block(cx+dx,41+n/2,cz+dz,roof);
        a.box(cx,42+r/2,cz,cx,45+r/2,cz,GILT);
    }
    private static void home(GensokyoArchitecture a,int x1,int z1,int x2,int z2,String[] names,boolean study) {
        VillageJoinery.house(a,x1,z1,x2,z2,4,1);a.openZ((x1+x2)/2,z2,4,2,3);a.stairsSouth((x1+x2)/2,z2+1,0,4,3);
        int mid=(x1+x2)/2;VillageJoinery.wallX(a,mid,z1+1,z2-1,4,z2-9);
        int index=0;
        for(int x:new int[]{x1+5,mid+6}) {
            int left=index==0?x1+1:mid+1,right=index==0?mid-1:x2-1;
            VillageJoinery.wallZ(a,left,right,z1+21,4,x+5);
            a.bed(x,4,z1+9);a.chest(x+4,4,z1+4,study?"hieda_records":"doll_materials");
            VillageJoinery.lowDesk(a,x,4,z2-9,5);a.box(x,5,z1+1,x+7,7,z1+1,Blocks.BOOKSHELF.getDefaultState());
            a.box(left,5,z1+6,left+1,7,z1+14,DARK);a.box(x-1,5,z1+12,x+3,5,z1+17,Blocks.CARPET.getStateFromMeta(index==0?10:9));
            a.box(x-2,5,z1+19,x+1,7,z1+19,WOOD);
            a.box(left,5,z2-15,left,7,z2-5,study?Blocks.BOOKSHELF.getDefaultState():DARK);
            a.block(x+6,5,z2-14,Blocks.CAULDRON.getStateFromMeta(3));
            VillageJoinery.lantern(a,x+3,4,z2-9);VillageJoinery.lantern(a,x+3,4,z1+12);
            a.room(names[index]+"·起居",x+2,4,z2-5);a.room(names[index++]+"·寝间",x+5,4,z1+17);
        }
    }
    private static void service(GensokyoArchitecture a) {
        VillageJoinery.house(a,62,-52,104,-14,2,1);a.openZ(81,-14,2,2,3);a.stairsSouth(81,-13,0,2,3);
        VillageJoinery.wallX(a,84,-51,-15,2,-23);
        for(int z:new int[]{-43,-34,-25})VillageJoinery.lowDesk(a,66,2,z,12);
        for(int z:new int[]{-47,-41,-35}) {a.block(100,3,z,Blocks.FURNACE.getDefaultState());a.chest(88,2,z,"village_pantry");}
        a.box(89,3,-49,98,3,-49,WOOD);a.block(95,4,-49,Blocks.CAULDRON.getStateFromMeta(3));
        VillageJoinery.lantern(a,74,2,-35);VillageJoinery.lantern(a,93,2,-28);
        a.room("茶饭堂",79,2,-23);a.room("厨房",91,2,-23);
        VillageJoinery.house(a,-107,-33,-61,5,2,1);a.openZ(-85,5,2,2,3);a.stairsSouth(-85,6,0,2,3);
        VillageJoinery.wallZ(a,-106,-62,-15,2,-84);VillageJoinery.wallX(a,-84,-32,-16,2,-24);
        for(int x:new int[]{-102,-96,-78,-71})a.bed(x,2,-25);
        for(int x:new int[]{-101,-72}) {a.chest(x,2,-30,"village_pantry");VillageJoinery.lowDesk(a,x-1,2,-6,6);}
        a.block(-103,3,0,Blocks.CRAFTING_TABLE.getDefaultState());a.block(-98,3,0,Blocks.ANVIL.getDefaultState());a.chest(-67,2,0,"kourindou_tools");
        for(int x:new int[]{-96,-71})for(int z:new int[]{-25,-4})VillageJoinery.lantern(a,x,2,z);
        a.room("弟子西寝",-90,2,-21);a.room("弟子东寝",-80,2,-21);a.room("起居与修补",-85,2,-7);
    }
    private static void garden(GensokyoArchitecture a) {
        for(int[] at:new int[][]{{-30,-89},{-20,-57},{32,-50},{93,12}})VillageJoinery.maple(a,at[0],at[1],10,6);
        for(int[] at:new int[][]{{-34,-62},{17,-53},{45,-53}}) {
            a.box(at[0]-3,1,at[1]-2,at[0]+3,2,at[1]+2,Blocks.MOSSY_COBBLESTONE.getDefaultState());
            a.box(at[0]-1,3,at[1]-1,at[0]+2,5,at[1]+1,STONE);
        }
        for(int x:new int[]{-93,93})for(int z:new int[]{59,85})VillageJoinery.maple(a,x,z,11,7);
        for(int x:new int[]{-33,33}) {
            VillageJoinery.pond(a,x,65,15,8);
            for(int dx=-10;dx<=10;dx+=5)a.block(x+dx,1,65,Blocks.WATERLILY.getDefaultState());
        }
        for(int x:new int[]{-10,10})for(int z:new int[]{46,80,99})a.lamp(x,1,z);
        for(int x:new int[]{-111,111}) {a.box(x,1,-111,x,5,103,WHITE);a.box(x,6,-112,x,6,104,SLAB);}
        a.box(-111,1,-111,111,5,-111,WHITE);a.box(-112,6,-111,112,6,-111,SLAB);
        for(int x:new int[]{-6,6})a.box(x,1,99,x,10,99,PILLAR);
        a.box(-7,9,99,7,10,99,DARK);roof(a,-10,96,10,102,11,TILE);
        a.sign(0,9,98,EnumFacing.NORTH,"归路","命莲寺墓地");
    }
    private static void lamp(GensokyoArchitecture a,int x,int y,int z,int ceiling) {
        a.box(x,y+1,z,x,ceiling-1,z,Blocks.OAK_FENCE.getDefaultState());a.block(x,y,z,ModBlocks.RED_LANTERN.getDefaultState());
    }
    private static void roof(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int y,IBlockState tile) {
        int depth=Math.min(x2-x1,z2-z1)/2;
        for(int n=0;n<=depth;n++) {
            int h=y+n/3+(n<3?2-n:0);
            a.box(x1+n,h,z1+n,x2-n,h,z1+n,tile);a.box(x1+n,h,z2-n,x2-n,h,z2-n,tile);
            a.box(x1+n,h,z1+n,x1+n,h,z2-n,tile);a.box(x2-n,h,z1+n,x2-n,h,z2-n,tile);
        }
        for(int x:new int[]{x1,x2})for(int z:new int[]{z1,z2}) {a.block(x,y+3,z,GILT);a.block(x+(x==x1?1:-1),y+4,z,GILT);}
        a.box(x1+depth,y+depth/3+1,(z1+z2)/2,x2-depth,y+depth/3+1,(z1+z2)/2,GILT);
    }
}
