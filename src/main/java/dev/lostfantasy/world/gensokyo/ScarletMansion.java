package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Three storeys around a reception hall; service stairs continue into the library below. */
final class ScarletMansion {
    static final IBlockState BRICK=Blocks.BRICK_BLOCK.getDefaultState(),TRIM=Blocks.SANDSTONE.getStateFromMeta(2),
            GLASS=Blocks.STAINED_GLASS.getStateFromMeta(14),TILE=Blocks.NETHER_BRICK.getDefaultState(),
            RUG=Blocks.WOOL.getStateFromMeta(14);
    private final GensokyoArchitecture a;
    private ScarletMansion(GensokyoBlueprint plan) {a=new GensokyoArchitecture(plan,GensokyoAtlas.SCARLET);}
    static void build(GensokyoBlueprint plan) {new ScarletMansion(plan).build();}
    private void build() {
        shell(-29,-69,29,28);shell(-88,-48,-29,20);shell(29,-48,88,20);
        for(int floor:new int[]{3,13,23}) {
            // Central circulation remains continuous through the abutting wings.
            a.box(-87,floor+1,-4,87,floor+6,4,AIR);
            for(int x:new int[]{-29,29})a.box(x-1,floor+1,-24,x+1,floor+5,-20,AIR);
            for(int side:new int[]{-1,1})wing(side,floor);
            a.box(-86,floor,-2,86,floor,2,RUG);
            for(int x=-80;x<=80;x+=16)ScarletRooms.ceilingLamp(a,x,floor+7,0);
        }
        // Two-storey entrance hall and galleries, with stairs on opposite sides.
        a.box(-20,13,6,20,13,15,AIR);a.box(-20,23,6,20,23,15,AIR);
        a.stairsSouth(-16,6,3,13,2);a.stairsSouth(16,6,13,23,2);
        a.box(-6,4,28,6,9,28,AIR);a.box(-12,3,29,12,3,34,TRIM);a.stairsSouth(0,35,0,3,6);
        for(int floor:new int[]{13,23}) {
            a.box(-20,floor+1,15,20,floor+1,15,Blocks.IRON_BARS.getDefaultState());
            a.box(floor==13?-19:13,floor+1,15,floor==13?-13:19,floor+3,15,AIR);
            a.box(-20,floor+1,6,-20,floor+1,14,Blocks.IRON_BARS.getDefaultState());
            a.box(20,floor+1,6,20,floor+1,14,Blocks.IRON_BARS.getDefaultState());
        }
        a.box(-9,3,5,9,3,27,RUG);a.room("玄关大厅",0,3,21);
        for(int x:new int[]{-23,21}) {
            a.table(x,3,23,3);
            for(int dx=0;dx<3;dx++)a.block(x+dx,4,25,Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        }
        ScarletRooms.ceilingLamp(a,0,29,10);
        a.box(-20,4,-9,20,10,-9,WHITE);a.openZ(0,-9,3,3,5);
        a.box(-20,4,-38,20,10,-38,WHITE);a.openZ(0,-38,3,2,4);
        ScarletRooms.dining(a,-18,-35,18,-13,3,"内宴会厅");
        ScarletRooms.music(a,-20,-65,20,-42,3);
        a.box(-20,14,-9,20,20,-9,WHITE);a.openZ(0,-9,13,2,4);
        a.box(-20,14,-39,20,20,-39,WHITE);a.openZ(0,-39,13,2,4);
        ScarletRooms.lounge(a,-18,-35,18,-14,13,"会客沙龙");
        ScarletRooms.library(a,-20,-65,20,-43,13,"阅览与书信室");
        a.box(-20,24,-9,20,30,-9,WHITE);a.openZ(0,-9,23,2,4);
        a.box(-20,24,-38,20,30,-38,WHITE);a.openZ(0,-38,23,2,4);
        ScarletRooms.lounge(a,-18,-34,18,-14,23,"主人起居室");
        ScarletRooms.bedroom(a,-20,-65,20,-43,23,"主人寝室",true);
        // Roof terrace and clock tower can be reached from the upper gallery.
        a.box(22,24,5,26,37,18,AIR);a.stairsSouth(24,6,23,33,2);
        a.box(-29,34,28,29,34,28,Blocks.IRON_BARS.getDefaultState());
        a.room("屋顶露台",24,33,4);
        mansard(-31,-72,31,-7,34);mansard(-91,-51,-30,23,34);mansard(30,-51,91,23,34);
        for(int side:new int[]{-1,1})cornerTower(side);
        terrace();
        ScarletPorch.build(a);
        clockTower();
        // An upper cellar surrounds the deep, separately generated library.
        basement();
        serviceStair();
        ScarletGardens.build(a);
    }
    private void shell(int x1,int z1,int x2,int z2) {
        a.box(x1,0,z1,x2,2,z2,STONE);a.box(x1,3,z1,x2,33,z2,BRICK);
        a.box(x1+1,4,z1+1,x2-1,32,z2-1,AIR);
        for(int floor:new int[]{3,13,23,33})a.box(x1,floor,z1,x2,floor,z2,DARK);
        for(int y:new int[]{3,12,22,32}) {
            a.box(x1-1,y,z1-1,x2+1,y,z1-1,TRIM);a.box(x1-1,y,z2+1,x2+1,y,z2+1,TRIM);
            a.box(x1-1,y,z1-1,x1-1,y,z2+1,TRIM);a.box(x2+1,y,z1-1,x2+1,y,z2+1,TRIM);
        }
        for(int floor:new int[]{3,13,23}) {
            for(int x=x1+5;x<x2-3;x+=8)for(int z:new int[]{z1,z2}) {
                a.box(x-1,floor+2,z,x+1,floor+7,z,GLASS);
                a.box(x-2,floor+1,z-1,x+2,floor+1,z+1,TRIM);
                a.box(x-2,floor+8,z,x+2,floor+8,z,TRIM);
                a.box(x,floor+2,z,x,floor+7,z,Blocks.IRON_BARS.getDefaultState());
            }
            for(int z=z1+5;z<z2-3;z+=8)for(int x:new int[]{x1,x2}) {
                a.box(x,floor+2,z-1,x,floor+7,z+1,GLASS);
                a.box(x-1,floor+1,z-2,x+1,floor+1,z+2,TRIM);
            }
        }
    }
    private void wing(int side,int floor) {
        int min=side<0?-87:30,max=side<0?-30:87;
        a.box(min,floor+1,-25,max,floor+5,-21,AIR);
        for(int z:new int[]{-26,-20,-5,5})a.box(min,floor+1,z,max,floor+8,z,WHITE);
        for(int i=0;i<3;i++) {
            int x1=min+1+i*19,x2=x1+17,mx=(x1+x2)/2;
            for(int z:new int[]{-26,-20,-5,5})a.openZ(mx,z,floor,1,3);
            if(i>0)a.box(x1-1,floor+1,-47,x1-1,floor+8,19,WHITE);
            a.box(x1-1,floor+1,-25,x1-1,floor+5,-21,AIR);
            a.box(x1-1,floor+1,-4,x1-1,floor+6,4,AIR);
            if(floor==3) {
                if(side<0 && i==0)ScarletRooms.storage(a,x1,-18,x2,-8,floor,"备品间");
                else if(side<0)ScarletRooms.kitchen(a,x1,-18,x2,-8,floor,i==1?"厨房":"茶水间");
                else ScarletRooms.dining(a,x1,-18,x2,-8,floor,i==0?"餐具间":"小餐室");
                ScarletRooms.lounge(a,x1,8,x2,17,floor,side<0?"接待室":"休息室");
                if(side<0 && i==0)continue; // the west service staircase occupies this rear room
                if(side<0)ScarletRooms.storage(a,x1,-45,x2,-29,floor,"食品与器具库");
                else ScarletRooms.library(a,x1,-45,x2,-29,floor,"藏品陈列室");
            } else {
                ScarletRooms.bedroom(a,x1,8,x2,17,floor,floor==13?"客房":"女仆寝室",false);
                ScarletRooms.bedroom(a,x1,-45,x2,-29,floor,side<0?"居住室":"客房",false);
                if(i%2==0)ScarletRooms.library(a,x1,-18,x2,-8,floor,"书房");
                else ScarletRooms.lounge(a,x1,-18,x2,-8,floor,"起居间");
            }
        }
    }
    private void mansard(int x1,int z1,int x2,int z2,int base) {
        for(int level=0;level<9;level++) {
            int inset=level<4?level:4+(level-4)*2;
            if(x1+inset>x2-inset || z1+inset>z2-inset)break;
            a.box(x1+inset,base+level,z1+inset,x2-inset,base+level,z2-inset,TILE);
        }
        for(int x=x1+8;x<x2-7;x+=14) {
            a.box(x-2,base+1,z2-2,x+2,base+5,z2-2,TRIM);
            a.box(x-1,base+2,z2-1,x+1,base+4,z2-1,GLASS);
            a.box(x-3,base+6,z2-3,x+3,base+6,z2,TILE);
        }
    }
    private void basement() {
        a.box(-105,-11,-78,104,-3,55,STONE);a.box(-103,-10,-76,102,-4,53,AIR);
        a.box(-103,-10,-76,102,-10,53,DARK);
        for(int z:new int[]{-55,-43,-14,0,29})a.box(-102,-9,z,101,-4,z,BRICK);
        for(int x:new int[]{-58,-15,28,64})a.box(x,-9,-75,x,-4,52,BRICK);
        for(int x:new int[]{-58,-15,28,64})for(int z:new int[]{-49,-7,36})a.openX(x,z,-10,1,3);
        for(int z:new int[]{-55,-43,-14,0,29})for(int x:new int[]{-80,-35,7,46,84})a.openZ(x,z,-10,1,3);
        for(int x:new int[]{-80,-35,7,46,84})for(int z:new int[]{-66,14,44}) {
            if(x==-80 && z==-66)ScarletRooms.kitchen(a,x-12,z-6,x+12,z+6,-10,"地下茶室");
            else if(x==84 && z==14)ScarletRooms.bedroom(a,x-13,z-9,x+13,z+9,-10,"地下寝室",true);
            else if(z==44)ScarletRooms.storage(a,x-11,z-6,x+11,z+6,-10,"地下收藏室");
            else ScarletRooms.lounge(a,x-11,z-6,x+11,z+6,-10,"地下起居室");
        }
        ScarletRooms.kitchen(a,-101,-27,-62,-17,-10,"洗涤与备餐间");
        ScarletRooms.storage(a,-55,-40,-18,-17,-10,"食品地窖");
        for(int x=-48;x<=-24;x+=8) {
            a.box(x,-9,-38,x+2,-8,-22,Blocks.HAY_BLOCK.getDefaultState());
            a.box(x,-7,-38,x+2,-7,-22,Blocks.WOODEN_SLAB.getDefaultState());
        }
        ScarletRooms.library(a,-12,-40,25,-17,-10,"魔法研究室");
        for(int x:new int[]{-7,17})for(int z:new int[]{-34,-24}) {
            a.table(x,-10,z,3);a.box(x,-8,z,x+2,-8,z,Blocks.WOODEN_SLAB.getStateFromMeta(8));
            a.block(x+1,-7,z,dev.lostfantasy.ModBlocks.ARMILLARY.getDefaultState());
            a.block(x+3,-9,z,Blocks.CAULDRON.getStateFromMeta(3));
        }
        ScarletRooms.storage(a,31,-40,61,-17,-10,"布草间");
        for(int x=37;x<=53;x+=8) {
            a.box(x,-9,-37,x+2,-7,-23,Blocks.WOOL.getStateFromMeta(x%3));
            a.box(x,-6,-37,x+2,-6,-23,Blocks.WOODEN_SLAB.getDefaultState());
        }
        ScarletRooms.music(a,67,-40,100,-17,-10);
        for(int x=-96;x<=96;x+=12)for(int z:new int[]{-49,-7,36})ScarletRooms.ceilingLamp(a,x,-5,z);
    }
    private void cornerTower(int side) {
        int cx=side*86,cz=24;
        for(int dx=-9;dx<=9;dx++)for(int dz=-9;dz<=9;dz++) {
            if(Math.abs(dx)+Math.abs(dz)>14)continue;
            boolean wall=Math.abs(dx)==9 || Math.abs(dz)==9 || Math.abs(dx)+Math.abs(dz)==14;
            a.box(cx+dx,0,cz+dz,cx+dx,2,cz+dz,STONE);
            a.box(cx+dx,3,cz+dz,cx+dx,33,cz+dz,wall?BRICK:AIR);
            for(int floor:new int[]{3,13,23,33})a.block(cx+dx,floor,cz+dz,DARK);
            if(wall)for(int band:new int[]{12,22,32})a.block(cx+dx,band,cz+dz,TRIM);
        }
        for(int floor:new int[]{3,13,23}) {
            a.box(cx-2,floor+2,cz+9,cx+2,floor+7,cz+9,GLASS);
            a.box(cx+side*9,floor+2,cz-2,cx+side*9,floor+7,cz+2,GLASS);
            int door=cx-side*9;
            a.box(door-2,floor+1,cz-4,door+2,floor+5,cz-1,AIR);
            a.room((side<0?"西":"东")+"侧眺望室",cx,floor,cz);
            a.table(cx-2,floor,cz+5,3);ScarletRooms.ceilingLamp(a,cx,floor+8,cz);
        }
        for(int level=0;level<24;level++) {
            int r=Math.max(0,10-level/2);
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)if(Math.abs(dx)+Math.abs(dz)<=r*3/2)
                a.block(cx+dx,34+level,cz+dz,TILE);
        }
        a.box(cx,58,cz,cx,61,cz,Blocks.IRON_BARS.getDefaultState());
    }
    private void terrace() {
        for(int x:new int[]{-13,9}) {
            a.table(x,33,18,3);
            for(int dx=0;dx<3;dx++) {
                a.block(x+dx,34,16,Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
                a.block(x+dx,34,20,Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
            }
        }
        for(int x:new int[]{-25,25}) {
            a.box(x-1,34,23,x+1,34,25,TRIM);a.box(x-1,35,23,x+1,36,25,Blocks.LEAVES.getStateFromMeta(4));
            a.lamp(x,34,1);
        }
    }
    private void serviceStair() {
        a.box(-88,-50,-50,-61,3,-29,STONE);a.box(-86,-49,-49,-62,9,-30,AIR);
        a.box(-86,-49,-49,-62,-49,-30,DARK);
        for(int flight=0;flight<4;flight++) {
            int bottom=-49+13*flight,left=flight%2==0?-83:-70;
            boolean north=flight%2==0;
            for(int r=1;r<=13;r++) {
                int z=north?-33-r:-47+r;
                if(r>1)a.box(left,bottom+1,z,left+3,bottom+r-1,z,STONE);
                a.box(left,bottom+r,z,left+3,bottom+r,z,Blocks.STONE_BRICK_STAIRS.getDefaultState()
                        .withProperty(BlockStairs.FACING,north?EnumFacing.NORTH:EnumFacing.SOUTH));
            }
            int z=north?-48:-33;
            a.box(-84,bottom+13,z,-66,bottom+13,z+1,DARK);
            ScarletRooms.ceilingLamp(a,-75,bottom+10,-31);
        }
        // Upper service entrance and intermediate cellar door.
        a.box(-87,3,-34,-57,3,-30,DARK);a.box(-88,4,-34,-57,8,-30,AIR);
        a.box(-80,4,-29,-76,9,-20,AIR);a.box(-80,3,-29,-76,3,-20,DARK);
        a.box(-66,-10,-49,-57,-10,-46,DARK);a.box(-66,-9,-49,-57,-5,-46,AIR);
        // Lowest landing joins the old library's cleared west entry aisle.
        a.box(-66,-49,-39,-34,-49,-33,DARK);a.box(-66,-48,-39,-34,-44,-33,AIR);
        a.room("地下一层楼梯厅",-70,-10,-47);
        a.room("图书馆入口",-56,-49,-36);
    }
    private void clockTower() {
        a.box(-10,33,-41,10,63,-21,BRICK);a.box(-8,34,-39,8,62,-23,AIR);
        a.box(-8,33,-39,8,33,-23,DARK);
        // Roof-level corridor through the mansard to the tower entrance.
        a.box(-3,33,-22,3,33,4,DARK);a.box(-3,34,-22,3,38,4,AIR);
        a.openZ(0,-21,33,2,4);
        for(int i=0;i<24;i++) {
            int side=i/6,t=i%6,x=side==0?-6+t*2:side==1?6:side==2?6-t*2:-6;
            int z=side==0?-37:side==1?-37+t*2:side==2?-25:-25-t*2;
            // Broad blocks overlap each previous step by one block at the turns.
            a.box(x-1,34+i,z-1,x+1,34+i,z+1,Blocks.DARK_OAK_STAIRS.getDefaultState()
                    .withProperty(BlockStairs.FACING,new EnumFacing[]{EnumFacing.EAST,EnumFacing.SOUTH,EnumFacing.WEST,EnumFacing.NORTH}[side]));
        }
        a.box(-8,58,-39,8,58,-23,DARK);a.box(-8,58,-37,-5,58,-24,AIR);
        a.room("钟楼机械间",4,58,-31);
        a.box(-1,59,-32,1,61,-30,Blocks.IRON_BLOCK.getDefaultState());
        // Static clockwork, an accessible workbench and a spare-parts chest beside the landing.
        a.box(0,46,-31,0,58,-31,Blocks.IRON_BARS.getDefaultState());
        a.box(-2,44,-32,2,45,-30,TRIM);
        a.box(-3,60,-31,3,60,-31,Blocks.IRON_BARS.getDefaultState());
        a.block(3,59,-37,Blocks.CRAFTING_TABLE.getDefaultState());
        a.chest(6,58,-37,"kourindou_tools");
        a.box(3,59,-24,6,59,-24,DARK);a.block(4,60,-24,dev.lostfantasy.ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.block(6,60,-24,dev.lostfantasy.ModBlocks.LIBRARY_LAMP.getDefaultState());
        for(int x:new int[]{-10,10})a.box(x,34,-22,x,63,-22,TRIM);
        for(int y=47;y<=61;y++)for(int x=-7;x<=7;x++)if(x*x+(y-54)*(y-54)<=50) {
            a.block(x,y,-20,Blocks.QUARTZ_BLOCK.getDefaultState());
            if(x==0 && y>=54 && y<=59 || y==54 && x>=-4 && x<=0
                    || x==0 && (y==47 || y==61) || Math.abs(x)==7 && y==54)a.block(x,y,-19,TILE);
        }
        for(int level=0;level<15;level++) {
            int r=Math.max(0,11-level*3/4);
            a.box(-r,64+level,-31-r,r,64+level,-31+r,TILE);
        }
        a.box(0,79,-31,0,82,-31,Blocks.IRON_BARS.getDefaultState());
    }
}
