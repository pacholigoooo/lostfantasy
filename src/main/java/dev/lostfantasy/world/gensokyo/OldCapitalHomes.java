package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;
import static dev.lostfantasy.world.gensokyo.OldCapitalJoinery.*;

/** Street-facing plots, back lanes and five inhabited house plans. */
final class OldCapitalHomes {
    static final int SHOP=0,TAVERN=1,WORKSHOP=2,ROW_HOME=3,COURTYARD=4;
    static final List<Lot> LOTS=lots();
    private static final String[] TRADES={"布庄","纸墨铺","器物铺","粮铺"};
    private OldCapitalHomes() {}

    static void build(GensokyoBlueprint plan) {
        for(Lot lot:LOTS) {
            String name=lot.type==SHOP?TRADES[lot.id%4]:lot.type==TAVERN?"酒肆":lot.type==WORKSHOP?"修作坊":lot.type==ROW_HOME?"长屋":"合院";
            GensokyoArchitecture a=new GensokyoArchitecture(plan,OldHellWorld.ORIGIN,lot.x,lot.z,"旧都·"+name+(lot.id+1),lot.rotation);
            // A dry stone apron meets the public lane beyond each projecting shop eave.
            a.box(-lot.h-1,0,lot.d+1,lot.h+1,0,lot.d+lot.apron,STONE);
            switch(lot.type) {
                case SHOP: shop(a,lot,name);break;
                case TAVERN: tavern(a,lot);break;
                case WORKSHOP: workshop(a,lot);break;
                case ROW_HOME: rowHome(a,lot);break;
                default: courtyard(a,lot);break;
            }
            frontageDetails(a,lot);
        }
    }

    private static void frontageDetails(GensokyoArchitecture a,Lot l) {
        // Goods occupy the edge of each plot, never the public street or centre doorway.
        if(l.type==SHOP || l.type==TAVERN) {
            int x=l.h-3,z=l.d+2;
            a.box(x,1,z,x+1,2,z+1,Blocks.LOG.getStateFromMeta(1));
            a.box(x,3,z,x+1,3,z+1,Blocks.WOODEN_SLAB.getStateFromMeta(13));
            a.block(x,4,z,l.type==TAVERN?ModBlocks.LACQUER_BOWL.getDefaultState():Blocks.FLOWER_POT.getDefaultState());
            if(l.type==TAVERN) {
                a.box(-l.h+2,4,-l.d+6,-3,4,-l.d+6,Blocks.WOODEN_SLAB.getStateFromMeta(13));
                for(int u=-l.h+3;u<=-3;u+=3)a.block(u,5,-l.d+6,ModBlocks.LACQUER_BOWL.getDefaultState());
            }
        }
        if(l.type!=ROW_HOME)InteriorFinishes.tatami(a,-l.h+2,-l.d+2,1,-l.d+7,8);
        // Stone joinery at the threshold gives the blue-tiled street a warm, solid footing.
        for(int x=-l.h+1;x<l.h;x+=4)a.block(x,0,l.d+1,Blocks.STONEBRICK.getStateFromMeta(3));
    }

    private static void shop(GensokyoArchitecture a,Lot l,String name) {
        shell(a,-l.h,-l.d,l.h,l.d,2,l.id);stair(a,l.h,-l.d);
        entrance(a,0,l.d,2);shopfront(a,l.h,l.d,l.id,name);
        backDoor(a,l.d);
        kitchen(a,-l.h+2,-l.d+2,2);a.chest(1,2,-l.d+2,"old_hell_pantry");
        a.table(-l.h+3,2,2,5);a.chest(l.h-2,2,l.d-3,"old_hell_trade");
        int kind=l.id%4;
        if(kind==0) {
            for(int z=-2;z<=4;z+=3)a.box(-l.h+1,3,z,-l.h+1,5,z+1,Blocks.WOOL.getStateFromMeta(z+2==0?11:14));
        } else if(kind==1) {
            a.box(-l.h+1,3,-2,-l.h+1,5,5,Blocks.BOOKSHELF.getDefaultState());
            a.block(-l.h+4,3,5,ModBlocks.WRITING_DESK.getDefaultState());
        } else if(kind==2) {
            a.block(-l.h+5,5,2,ModBlocks.LACQUER_BOWL.getDefaultState());
            a.block(l.h-2,3,2,Blocks.ANVIL.getDefaultState());
        } else a.box(-l.h+1,3,-2,-l.h+1,4,0,Blocks.HAY_BLOCK.getDefaultState());
        bench(a,-l.h+3,l.d-3,2,4,EnumFacing.NORTH);
        upperHome(a,l,l.d);light(a,-4,-4,2);light(a,4,l.d-4,2);
        a.room("店堂",0,2,l.d-4);
    }

    private static void tavern(GensokyoArchitecture a,Lot l) {
        shell(a,-l.h,-l.d,l.h,l.d,2,l.id);stair(a,l.h,-l.d);
        entrance(a,0,l.d,2);shopfront(a,l.h,l.d,l.id,"酒肆");
        backDoor(a,l.d);
        kitchen(a,-l.h+2,-l.d+2,2);a.chest(1,2,-l.d+2,"old_hell_pantry");
        a.box(-l.h+1,3,-l.d+6,1,3,-l.d+6,DARK);
        for(int x:new int[]{-l.h+3,3})for(int z:new int[]{1,l.d-5}) {
            a.table(x,2,z,4);bench(a,x,z+2,2,4,EnumFacing.NORTH);
            a.block(x+1,5,z,ModBlocks.LACQUER_BOWL.getDefaultState());
        }
        a.chest(l.h-2,2,l.d-2,"old_hell_pantry");
        upperHome(a,l,l.d);light(a,-4,-4,2);light(a,4,l.d-4,2);
        // An upstairs gallery gives selected corner taverns a different street silhouette.
        if(l.id%2==0) {
            a.box(-l.h+2,8,l.d+1,l.h-2,8,l.d+3,WOOD);
            a.box(-l.h+2,9,l.d+3,l.h-2,9,l.d+3,Blocks.DARK_OAK_FENCE.getDefaultState());
            for(int x:new int[]{-l.h+2,l.h-2})a.box(x,9,l.d+1,x,9,l.d+3,Blocks.DARK_OAK_FENCE.getDefaultState());
            a.openZ(0,l.d,8,1,3);
        }
        a.room("酒席",0,2,l.d-3);
    }

    private static void workshop(GensokyoArchitecture a,Lot l) {
        shell(a,-l.h,-l.d,l.h,2,2,l.id);
        shell(a,-l.h,3,-3,l.d,1,0);
        a.box(-l.h+1,9,1,-4,13,1,AIR);
        a.box(-2,2,3,l.h,2,l.d,STONE);
        stair(a,l.h,-l.d);a.openZ(4,2,2,2,4);
        entrance(a,4,l.d,2);a.openX(-3,(l.d+3)/2,2,1,3);
        kitchen(a,-l.h+2,-l.d+2,2);a.chest(1,2,-l.d+2,"old_hell_pantry");
        a.table(-l.h+3,2,-2,5);a.block(-l.h+5,3,1,Blocks.ANVIL.getDefaultState());
        a.block(-l.h+2,3,6,Blocks.CRAFTING_TABLE.getDefaultState());
        a.chest(-l.h+2,2,l.d-2,"old_hell_trade");
        a.box(l.h-1,3,5,l.h-1,4,7,LOG);a.box(l.h-1,3,9,l.h-1,3,l.d-1,LOG);
        a.box(0,7,l.d+1,l.h-1,7,l.d+1,DARK);
        a.box(0,1,l.d+1,0,6,l.d+1,LOG);a.box(l.h-1,1,l.d+1,l.h-1,6,l.d+1,LOG);
        a.block(1,6,l.d+1,ModBlocks.RED_LANTERN.getDefaultState());
        a.sign(4,7,l.d+2,EnumFacing.SOUTH,"修作坊","");
        upperHome(a,l,2);light(a,-4,-4,2);light(a,-l.h+3,l.d-3,2);
        a.room("作业间",0,2,-1);a.room("工棚",-6,2,7);a.room("装卸院",4,2,l.d-3);
    }

    private static void rowHome(GensokyoArchitecture a,Lot l) {
        shell(a,-l.h,-l.d,l.h,l.d,1,l.id);
        a.box(0,3,-l.d+1,0,7,l.d-1,WHITE);
        for(int side:new int[]{-1,1}) {
            int left=side<0?-l.h:1,right=side<0?-1:l.h,mid=(left+right)/2;
            entrance(a,mid,l.d,1);
            a.box(left+1,3,-2,right-1,7,-2,WHITE);a.openZ(mid,-2,2,0,3);
            a.bed(left+3,2,-l.d+5);a.chest(right-1,2,-l.d+2,"old_hell_household");
            kitchen(a,left+2,1,2);
            a.table(left+3,2,l.d-5,3);bench(a,left+3,l.d-3,2,3,EnumFacing.NORTH);
            light(a,mid,-l.d+3,2);light(a,mid,l.d-5,2);
            a.room("起居"+side,mid,2,4);a.room("寝间"+side,mid,2,-l.d+8);
        }
    }

    private static void courtyard(GensokyoArchitecture a,Lot l) {
        shell(a,-l.h,-l.d,l.h,0,2,l.id);
        shell(a,-l.h,4,-4,l.d,1,0);
        a.box(-3,2,1,l.h,2,l.d,STONE);
        // The two-block passage behind the service wing joins the shared courtyard.
        a.box(-l.h,2,1,-4,2,3,STONE);
        stair(a,l.h,-l.d);a.openZ(0,0,2,1,4);a.openX(-4,(l.d+4)/2,2,1,3);
        a.box(-l.h,3,l.d,l.h,4,l.d,Blocks.DARK_OAK_FENCE.getDefaultState());entrance(a,0,l.d,1);
        kitchen(a,-l.h+2,-l.d+2,2);a.chest(1,2,-l.d+2,"old_hell_pantry");
        a.table(-l.h+3,2,-3,5);a.chest(-l.h+2,2,l.d-2,"old_hell_household");
        a.block(-l.h+2,3,6,Blocks.CAULDRON.getStateFromMeta(3));
        bench(a,3,l.d-3,2,4,EnumFacing.NORTH);
        a.box(l.h-2,3,3,l.h,3,5,STONE);a.box(l.h-1,4,4,l.h-1,4,4,Blocks.DIRT.getDefaultState());
        a.block(l.h-1,5,4,Blocks.RED_FLOWER.getStateFromMeta(0));
        a.box(-2,7,l.d+1,2,7,l.d+1,DARK);
        for(int x:new int[]{-2,2})a.box(x,1,l.d+1,x,6,l.d+1,LOG);
        a.block(0,6,l.d+1,ModBlocks.RED_LANTERN.getDefaultState());
        upperHome(a,l,0);light(a,-4,-4,2);light(a,-l.h+3,l.d-3,2);
        a.room("家居",0,2,-2);a.room("洗衣库房",-6,2,7);a.room("院子",0,2,l.d-3);
    }

    private static void upperHome(GensokyoArchitecture a,Lot l,int front) {
        int partition=-l.d+9;
        a.box(-l.h+1,9,partition,2,13,partition,WHITE);a.openZ(0,partition,8,1,3);
        a.bed(-l.h+4,8,-l.d+6);a.bed(-l.h+9,8,-l.d+6);
        a.chest(-l.h+2,8,-l.d+2,"old_hell_household");
        a.box(-l.h+1,9,-l.d+1,1,10,-l.d+1,Blocks.BOOKSHELF.getDefaultState());
        a.table(-l.h+3,8,front<=2?front-2:front-4,5);bench(a,-l.h+3,front-1,8,5,EnumFacing.NORTH);
        a.block(3,9,front-2,ModBlocks.WRITING_DESK.getDefaultState());
        light(a,-4,-l.d+3,8);light(a,4,front-3,8);
        a.room("楼上起居",0,8,front-1);a.room("寝间",0,8,-l.d+7);
    }

    private static void backDoor(GensokyoArchitecture a,int depth) {
        a.openZ(0,-depth,2,1,3);
        for(int step=0;step<2;step++)a.box(-1,1+step,-depth-2+step,1,1+step,-depth-2+step,
                Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(net.minecraft.block.BlockStairs.FACING,EnumFacing.SOUTH));
    }

    private static List<Lot> lots() {
        List<Lot> out=new ArrayList<>();
        int[] centers={27,58,110,145,197,233},widths={12,11,13,12,14,12};
        int[][] types={{SHOP,TAVERN,SHOP,SHOP,TAVERN,SHOP},
                {WORKSHOP,ROW_HOME,SHOP,ROW_HOME,WORKSHOP,COURTYARD},
                {ROW_HOME,COURTYARD,ROW_HOME,WORKSHOP,COURTYARD,ROW_HOME},
                {TAVERN,SHOP,WORKSHOP,COURTYARD,SHOP,TAVERN},
                {COURTYARD,ROW_HOME,WORKSHOP,ROW_HOME,SHOP,COURTYARD},
                {ROW_HOME,WORKSHOP,COURTYARD,ROW_HOME,WORKSHOP,COURTYARD}};
        for(int side:new int[]{-1,1})for(int row=0;row<6;row++)for(int j=0;j<centers.length;j++) {
            int id=out.size(),depth=row==1 || row==2?12+id%3:row==3 || row==4?16+id%4:13+id%5;
            boolean facesIn=row!=2 && row!=4;
            int facade=row==0?16:row==1?87:row==2?161:row==3?182:row==4?300:326;
            int x=side*(facade+(facesIn?depth:-depth));
            boolean west=side>0?facesIn:!facesIn;
            Rotation rotation=west?Rotation.CLOCKWISE_90:Rotation.COUNTERCLOCKWISE_90;
            out.add(new Lot(id,x,centers[j]+id%3-1,widths[j]-id%2,depth,types[row][(j+(side<0?0:1))%6],row==0?8:row==5?13:8,rotation));
        }
        return Collections.unmodifiableList(out);
    }

    static final class Lot {
        final int id,x,z,h,d,type,apron;final Rotation rotation;
        Lot(int id,int x,int z,int h,int d,int type,int apron,Rotation rotation) {
            this.id=id;this.x=x;this.z=z;this.h=h;this.d=d;this.type=type;this.apron=apron;this.rotation=rotation;
        }
        BlockPos world(int lx,int y,int lz) {
            BlockPos p=new BlockPos(lx,y,lz).rotate(rotation);
            return OldHellWorld.local(x+p.getX(),p.getY(),z+p.getZ());
        }
    }
}
