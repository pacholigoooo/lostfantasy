package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;
import static dev.lostfantasy.world.gensokyo.ChireidenRooms.Use.*;

/** A inhabited underground palace. Room arrangement is a Minecraft adaptation. */
final class Chireiden {
    static final int Z=520;
    private static final IBlockState PALE=Blocks.QUARTZ_BLOCK.getDefaultState(),
            PILLAR=Blocks.QUARTZ_BLOCK.getStateFromMeta(2),
            INK=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(15),
            VIOLET=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(11);
    private Chireiden() {}
    static BlockPos local(int x,int y,int z) {return OldHellWorld.local(x,y,z+Z);}
    static GensokyoBlueprint create() {
        GensokyoBlueprint p=new GensokyoBlueprint();
        GensokyoArchitecture a=new GensokyoArchitecture(p,OldHellWorld.ORIGIN,0,Z,"地灵殿");
        approach(a);shell(a);wings(a);hall(a);service(a);p.seal();return p;
    }
    private static void approach(GensokyoArchitecture a) {
        // A physical road from the old capital, with a three-step arrival terrace.
        a.box(-7,0,OldHellCity.EXIT_Z-Z,7,0,-81,STONE);
        for(int z=OldHellCity.EXIT_Z+12-Z;z<-80;z+=24)for(int x:new int[]{-10,10})a.lamp(x,0,z);
        a.box(-98,0,-82,98,0,70,STONE);
        a.box(-96,1,-80,96,2,68,STONE);
        a.box(-96,3,-80,96,3,68,PALE);
        for(int rise=1;rise<=3;rise++) {
            if(rise>1)a.box(-14,1,-83+rise,14,rise-1,-83+rise,STONE);
            a.box(-14,rise,-83+rise,14,rise,-83+rise,Blocks.QUARTZ_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        }
        for(int x:new int[]{-77,-52,52,77}) {
            a.box(x-8,4,-75,x+8,4,-64,STONE);
            a.box(x-7,4,-74,x+7,4,-65,Blocks.DIRT.getDefaultState());
            for(int u=-6;u<=6;u+=3)for(int z=-73;z<=-66;z+=3)a.block(x+u,5,z,Blocks.RED_FLOWER.getStateFromMeta((u+6)/3%2==0?4:6));
        }
        for(int x:new int[]{-93,-36,36,93})for(int z:new int[]{-76,63})a.lamp(x,3,z);
        a.room("前庭",0,3,-73);a.room("旧都来路",0,0,OldHellCity.EXIT_Z+16-Z);
    }
    private static void shell(GensokyoArchitecture a) {
        a.box(-88,-13,-59,88,3,59,STONE);
        a.box(-88,4,-59,88,39,59,WHITE);
        a.box(-87,4,-58,87,38,58,AIR);
        for(int f:new int[]{3,15,27,39}) {
            a.box(-88,f,-59,-30,f,59,PALE);a.box(30,f,-59,88,f,59,PALE);
            a.box(-89,f,-60,89,f,-58,STONE);a.box(-89,f,58,89,f,60,STONE);
        }
        // Central nave rises above the three-storey wings.
        a.box(-31,39,-59,31,55,59,WHITE);a.box(-30,39,-58,30,54,58,AIR);
        a.box(-30,55,-58,30,55,58,PALE);
        for(int side:new int[]{-1,1}) {
            int wall=side*88;
            for(int z=-54;z<=54;z+=18) {
                a.box(wall-1,4,z-1,wall+1,40,z+1,PILLAR);
                for(int f:new int[]{3,15,27})for(int dz:new int[]{-5,5})windowX(a,wall,z+dz,f+3,4,7);
            }
            roof(a,side<0?-91:28,side<0?-28:91,-62,62,40);
        }
        for(int x=-81;x<=81;x+=9)for(int z:new int[]{-59,59}) {
            a.box(x,4,z-1,x,40,z+1,PILLAR);
            for(int f:new int[]{3,15,27})windowZ(a,x+4,z,f+3,2,7);
        }
        for(int z=-45;z<=45;z+=18)for(int x:new int[]{-31,31})windowX(a,x,z,42,5,10);
        roof(a,-34,34,-62,62,56);
        // A deep entrance portal, stepped archivolts and a rose window.
        a.box(-19,3,-66,19,32,-60,WHITE);
        a.box(-14,4,-66,14,24,-58,AIR);
        for(int layer=0;layer<3;layer++) {
            int half=17-layer*2,z=-67+layer;
            a.box(-half,4,z,-half,22,z,PILLAR);a.box(half,4,z,half,22,z,PILLAR);
            for(int x=-half;x<=half;x++)a.box(x,23+half-Math.abs(x),z,x,24+half-Math.abs(x),z,PALE);
        }
        for(int x=-7;x<=7;x++)for(int y=41;y<=55;y++) {
            int r=x*x+(y-48)*(y-48);
            if(r<=49)a.block(x,y,-59,r>33?PILLAR:Blocks.STAINED_GLASS.getStateFromMeta((x==0 || y==48 || Math.abs(x)==Math.abs(y-48))?4:10));
        }
        for(int center:new int[]{-59,59})for(int x=-4;x<=4;x++)for(int y=-4;y<=4;y++) {
            int radius=x*x+y*y;
            if(radius<=16)a.block(center+x,47+y,-59,radius>9?PALE:Blocks.STAINED_GLASS.getStateFromMeta(x==0?4:10));
        }
        a.box(-8,4,59,8,10,59,AIR);
        for(int x:new int[]{-92,92})for(int z:new int[]{-62,62})buttress(a,x,z);
        for(int x:new int[]{-92,92})for(int z:new int[]{-36,0,36}) {
            a.box(x-1,3,z-1,x+1,39,z+1,STONE);
            a.box(Math.min(x,x<0?-88:88),37,z-1,Math.max(x,x<0?-88:88),40,z+1,PALE);
            a.block(x,41,z,SLAB);
        }
    }
    private static void roof(GensokyoArchitecture a,int x1,int x2,int z1,int z2,int y) {
        int mid=(x1+x2)/2;
        for(int x=x1;x<=x2;x++) {
            int rise=Math.max(0,((x2-x1)/2-Math.abs(x-mid))/2);
            a.box(x,y+rise,z1,x,y+rise,z2,VIOLET);
            for(int z:new int[]{z1+3,z2-3})if(rise>0)a.box(x,y,z,x,y+rise-1,z,WHITE);
        }
        a.box(mid,y+(x2-x1)/4+1,z1-1,mid,y+(x2-x1)/4+1,z2+1,SLAB);
    }
    private static void buttress(GensokyoArchitecture a,int x,int z) {
        a.box(x-2,1,z-2,x+2,5,z+2,STONE);
        a.box(x-1,6,z-1,x+1,40,z+1,PILLAR);
        a.box(x-2,40,z-2,x+2,41,z+2,STONE);
        for(int d=0;d<3;d++)a.box(x-2+d,42+d*2,z-2+d,x+2-d,43+d*2,z+2-d,VIOLET);
    }
    private static void windowX(GensokyoArchitecture a,int x,int z,int y,int half,int height) {
        for(int d=-half;d<=half;d++) {
            int top=y+height-Math.abs(d);
            a.box(x,y,z+d,x,top,z+d,Blocks.STAINED_GLASS.getStateFromMeta(d==0?4:10));
            a.block(x,top+1,z+d,PALE);
        }
    }
    private static void windowZ(GensokyoArchitecture a,int x,int z,int y,int half,int height) {
        for(int d=-half;d<=half;d++) {
            int top=y+height-Math.abs(d);
            a.box(x+d,y,z,x+d,top,z,Blocks.STAINED_GLASS.getStateFromMeta(d==0?4:10));
            a.block(x+d,top+1,z,PALE);
        }
    }
    private static void wings(GensokyoArchitecture a) {
        ChireidenRooms.Use[][][] uses={
            {{PETS,FEED,PETS,BATH,TOOLS,FEED},{KITCHEN,DINING,LINEN,BATH,TOOLS,FEED}},
            {{BOOKS,WRITING,LOUNGE,BED,BOOKS,WARDROBE},{LOUNGE,BED,BOOKS,PETS,BATH,WARDROBE}},
            {{BOOKS,BOOKS,WRITING,TOOLS,LINEN,TOOLS},{LINEN,TOOLS,FEED,TOOLS,BOOKS,LINEN}}
        };
        String[][][] names={
            {{"猫房","饲料间","宠物起居","洗护间","园具间","食盆收存"},{"厨房","食堂","布巾收存","洗涤间","修理间","宠物粮库"}},
            {{"觉的藏书","觉的书斋","觉的起居","觉的寝室","阅书间","衣物间"},{"恋的起居","恋的寝室","故事书间","宠物游戏室","内宅浴室","恋的衣物间"}},
            {{"文稿书库","旧书收存","装订间","装帧器具","织物收存","维修器具"},{"寝具收存","庭院用具","粮食收存","空笼修理","宠物照料记录","洗晒收存"}}
        };
        for(int level=0;level<3;level++) {
            int f=3+level*12;
            for(int side=0;side<2;side++) {
                int x1=side==0?-87:41,x2=side==0?-41:87;
                a.box(x1,f+1,-54,x2,f+11,54,WHITE);
                a.box(side==0?x1:x1+1,f+1,-53,side==0?x2-1:x2,f+11,53,AIR);
                int divider=side==0?-64:64;
                a.box(divider,f+1,-53,divider,f+11,53,WHITE);
                for(int z:new int[]{-18,18})a.box(x1,f+1,z,x2,f+11,z,WHITE);
                for(int row=0;row<3;row++) {
                    int z=-36+row*36;
                    a.openX(side==0?x2:x1,z,f,2,5);a.openX(divider,z,f,2,5);
                    for(int room=0;room<2;room++) {
                        int left=room==0?x1+1:divider+1,right=room==0?divider-1:x2-1;
                        ChireidenRooms.furnish(a,left,right,z-17,z+17,f,uses[level][side][row*2+room],names[level][side][row*2+room]);
                    }
                }
                // Rooms occupy the inner shell; glazing stays on the outer structural wall.
                a.room((side==0?"西":"东")+(level+1)+"层回廊",side==0?-36:36,f,0);
                for(int z=-45;z<=45;z+=18)ChireidenRooms.lamp(a,side==0?-36:36,f+8,z);
            }
        }
    }
    private static void hall(GensokyoArchitecture a) {
        // Four-block checker tiles, with lit stained panels let into a supported floor.
        for(int x=-29;x<=29;x++)for(int z=-58;z<=58;z++)a.block(x,3,z,((Math.floorDiv(x,4)+Math.floorDiv(z,4))&1)==0?PALE:INK);
        for(int z:new int[]{-35,0,35})mosaic(a,0,3,z,10);
        for(int f:new int[]{15,27}) {
            a.box(-30,f,-58,-22,f,58,PALE);a.box(22,f,-58,30,f,58,PALE);
            a.box(-30,f,-58,30,f,-47,PALE);a.box(-30,f,47,30,f,58,PALE);
            a.box(-22,f+1,-46,-22,f+1,46,Blocks.IRON_BARS.getDefaultState());
            a.box(22,f+1,-46,22,f+1,46,Blocks.IRON_BARS.getDefaultState());
            a.box(-22,f+1,-47,22,f+1,-47,Blocks.IRON_BARS.getDefaultState());
            a.box(-22,f+1,47,22,f+1,47,Blocks.IRON_BARS.getDefaultState());
            a.room(f==15?"中层前廊":"上层前廊",0,f,-52);a.room(f==15?"中层后廊":"上层后廊",0,f,52);
        }
        for(int x:new int[]{-25,25}) {
            a.box(x-2,15,30,x+2,15,43,AIR);a.stairsSouth(x,30,3,15,2);
            a.box(x-2,27,-20,x+2,27,-7,AIR);a.stairsSouth(x,-20,15,27,2);
        }
        for(int x:new int[]{-19,19})for(int z=-40;z<=40;z+=20) {
            a.box(x-2,4,z-2,x+2,5,z+2,STONE);
            a.box(x-1,6,z-1,x+1,45,z+1,PILLAR);
            a.box(x-2,44,z-2,x+2,46,z+2,STONE);
            // Transverse ribs spring from the column capitals into a pointed vault.
            for(int u=0;u<=18;u++)a.box(x<0?x+u:x-u,46+u/3,z-1,x<0?x+u:x-u,47+u/3,z+1,PALE);
        }
        for(int z:new int[]{-40,-10,20,45}) {
            a.box(0,36,z,0,54,z,Blocks.IRON_BARS.getDefaultState());
            a.box(-6,35,z,6,35,z,Blocks.IRON_BARS.getDefaultState());a.box(0,35,z-6,0,35,z+6,Blocks.IRON_BARS.getDefaultState());
            for(int x:new int[]{-6,0,6})a.block(x,34,z,ModBlocks.LIBRARY_LAMP.getDefaultState());
            for(int dz:new int[]{-6,6})a.block(0,34,z+dz,ModBlocks.LIBRARY_LAMP.getDefaultState());
        }
        for(int x:new int[]{-32,32})for(int z=-48;z<=48;z+=24) {
            a.lamp(x,3,z);
            for(int f:new int[]{15,27})a.lamp(x,f,z);
        }
        a.room("大堂",0,3,-18);a.room("北端休息厅",0,3,53);
        for(int x:new int[]{-10,6}) {a.table(x,3,52,5);a.box(x,4,54,x+4,4,54,Blocks.DARK_OAK_STAIRS.getDefaultState());}
        a.chest(-13,3,55,"palace_books");a.chest(13,3,55,"palace_books");
    }
    private static void mosaic(GensokyoArchitecture a,int mx,int y,int mz,int r) {
        for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++) {
            // Five-sided panel and a raised-wing bird silhouette, based on the stage emblem.
            double edge=dz<-r*.25?(dz+r)/.75:r-(dz+r*.25)*.28;
            if(Math.abs(dx)>edge || dz<-r || dz>r)continue;
            boolean bird=(Math.abs(dx)<=1 && dz>=-3 && dz<=6)
                    || (Math.abs(dx)>=2 && Math.abs(dx)<=7 && dz>=-Math.abs(dx)/2-2 && dz<=1-Math.abs(dx)/2);
            a.block(mx+dx,y-1,mz+dz,LIGHT);
            a.block(mx+dx,y,mz+dz,Blocks.STAINED_GLASS.getStateFromMeta(bird?15:Math.abs(dx)>edge-1.4?4:10));
        }
    }
    private static void service(GensokyoArchitecture a) {
        // A working-depth undercroft, with a central heating gallery and side maintenance bays.
        a.box(-30,-11,-50,30,-1,53,STONE);a.box(-29,-10,-49,29,1,52,AIR);
        a.box(-30,2,-50,30,2,53,STONE);
        for(int x:new int[]{-15,15})a.box(x,-10,-49,x,1,52,STONE);
        for(int z:new int[]{-32,0,32}) {
            for(int x:new int[]{-15,15})a.openX(x,z,-11,2,5);
            for(int x:new int[]{-23,23}) {
                a.room((x<0?"西":"东")+"暖廊检修"+z,x,-11,z);
                a.box(x-3,-9,z-6,x+3,-8,z-6,Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(12));
                a.chest(x-3,-11,z+5,"palace_household");a.block(x+3,-10,z+5,Blocks.CRAFTING_TABLE.getDefaultState());
            }
            a.box(0,-3,z,0,1,z,Blocks.IRON_BARS.getDefaultState());a.block(0,-4,z,ModBlocks.LIBRARY_LAMP.getDefaultState());
        }
        // Stair exits are in the rear hall, away from its furniture and primary nave.
        for(int x:new int[]{-10,10}) {
            a.box(x-2,2,31,x+2,3,46,AIR);a.stairsSouth(x,31,-11,3,2);
        }
        a.room("地下暖廊",0,-11,18);
        for(int x:new int[]{-7,7}) {
            a.box(x,-9,-46,x,-7,21,Blocks.NETHER_BRICK.getDefaultState());
            for(int z=-43;z<=19;z+=10) {a.block(x,-10,z,LIGHT);a.block(x,-6,z,Blocks.STONE_SLAB.getDefaultState());}
        }
        a.box(-9,-10,53,9,-3,71,AIR);a.box(-9,-11,53,9,-11,71,STONE);
        a.box(-9,-10,72,9,-3,72,Blocks.IRON_BARS.getDefaultState());
        for(int z=73;z<=105;z++) {
            int half=12+(int)Math.round(10*Math.sin((z-73)*Math.PI/32));
            a.box(-half,-38,z,half,-2,z,AIR);
            a.box(-half,-39,z,half,-39,z,Blocks.LAVA.getDefaultState());
        }
        a.room("深层观察廊",0,-11,64);
    }
}
