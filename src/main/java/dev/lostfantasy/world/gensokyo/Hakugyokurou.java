package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A courtyard residence, with service rooms and living quarters opening onto connected verandas. */
final class Hakugyokurou {
    private Hakugyokurou() {}
    static GensokyoBlueprint create() {
        GensokyoBlueprint p=new GensokyoBlueprint();GensokyoArchitecture a=new GensokyoArchitecture(p,GensokyoAtlas.NETHER_GATE,0,0,"白玉楼");
        a.box(-120,-4,-123,120,0,139,STONE);a.box(-120,1,-123,120,36,139,AIR);
        a.box(-119,0,-122,119,0,138,Blocks.GRASS.getDefaultState());
        for(int x:new int[]{-120,120}) {a.box(x,1,-123,x,5,139,WHITE);a.box(x-1,6,-124,x+1,6,140,ROOF);}
        for(int z:new int[]{-123,139}) {a.box(-120,1,z,120,5,z,WHITE);a.box(-121,6,z-1,121,6,z+1,ROOF);}
        a.box(-7,1,138,7,7,140,AIR);a.box(103,1,-124,113,7,-122,AIR);
        a.box(-8,0,48,8,0,140,STONE);a.box(104,0,-211,112,0,127,STONE);a.box(-106,0,116,112,0,126,STONE);
        a.box(-85,1,-79,85,3,61,STONE);a.box(-85,4,-79,85,4,61,WOOD);
        // The open courtyard retains ground level; verandas run along its three sides.
        a.box(-42,1,-34,42,4,62,AIR);a.box(-42,0,-34,42,0,61,Blocks.GRAVEL.getDefaultState());
        VillageJoinery.house(a,-80,-75,80,-44,4,1);
        for(int x:new int[]{-48,-16,16,48})VillageJoinery.wallX(a,x,-74,-45,4,-50);
        String[] back={"旧卷与幽灵记录","幽幽子书斋","接客间","茶食间","衣物与器物"};
        for(int i=0;i<5;i++) {
            int x=-64+i*32;a.openZ(x,-44,4,3,3);furnish(a,x,-60,back[i],i);
            a.room(back[i],x,4,-48);
        }
        for(int side:new int[]{-1,1}) {
            int x1=side<0?-80:48,x2=side<0?-48:80,cx=(x1+x2)/2;
            VillageJoinery.house(a,x1,-35,x2,57,4,1);
            for(int z:new int[]{-12,11,34})VillageJoinery.wallZ(a,x1+1,x2-1,z,4,cx);
            String[] names=side<0?new String[]{"幽幽子起居","客房","庭师寝居","庭师用房"}:new String[]{"配膳与厨房","储藏室","裁缝与修缮","洗漱间"};
            for(int i=0;i<4;i++) {
                int z=-24+i*23;a.openX(side<0?-48:48,z,4,2,3);
                furnish(a,cx,z,names[i],side<0?5+i:9+i);a.room(names[i],side<0?-51:51,4,z);
            }
        }
        // Slim veranda roof and timber posts, leaving a view from each inner door.
        a.box(-85,9,-43,85,9,-36,ROOF);
        for(int side:new int[]{-1,1}) {
            a.box(side<0?-47:43,9,-35,side<0?-43:47,9,62,ROOF);
            for(int z=-32;z<=60;z+=12)a.box(side*43,5,z,side*43,8,z,LOG);
            a.stairsSouth(side*63,62,0,4,10);
        }
        for(int x=-84;x<=84;x+=12)a.box(x,5,-36,x,8,-36,LOG);
        a.stairsSouth(0,-35,0,4,7);
        a.box(-42,1,61,-10,2,61,WHITE);a.box(10,1,61,42,2,61,WHITE);
        dryGarden(a);gate(a);SaigyouAyakashi.build(a);NetherworldEntrance.approach(a);gardenDetails(a);
        a.room("枯山水庭",0,0,36);a.room("赏樱庭",0,0,100);a.room("西行妖观景道",108,0,-207);
        p.seal();return p;
    }
    private static void furnish(GensokyoArchitecture a,int x,int z,String name,int use) {
        InteriorFinishes.tatami(a,x-13,z-9,x+13,z+7,4);
        a.box(x-10,5,z-7,x+10,5,z+5,Blocks.CARPET.getStateFromMeta(use==2?14:13));
        a.box(x-11,5,z-10,x-7,7,z-10,DARK);a.block(x-9,8,z-10,ModBlocks.LACQUER_BOWL.getDefaultState());
        for(int dx:new int[]{-8,8})VillageJoinery.lantern(a,x+dx,4,z);
        if(use==5 || use==6 || use==7) {
            VillageJoinery.wallZ(a,x-15,x+15,z-3,4,x+9);
            a.box(x-14,5,z-9,x+14,5,z-4,AIR);
            for(int dx:new int[]{-9,-4})a.bed(x+dx,4,z-5);
            a.chest(x+11,4,z-8,"palace_household");VillageJoinery.lowDesk(a,x-7,4,z+3,6);
        } else if(use==9) {
            for(int dx:new int[]{-10,-7,-4})a.block(x+dx,5,z-7,Blocks.FURNACE.getDefaultState());
            a.block(x+5,5,z-8,Blocks.CRAFTING_TABLE.getDefaultState());
            a.block(x+10,5,z-7,Blocks.CAULDRON.getDefaultState());
            a.chest(x+10,4,z,"village_pantry");a.chest(x+10,4,z+4,"village_pantry");
            a.table(x-8,4,z+3,11);
        } else if(use==10 || use==4) {
            for(int dx:new int[]{-11,-5,3,10})a.chest(x+dx,4,z-7,use==10?"village_pantry":"palace_household");
            a.box(x-12,5,z+5,x+12,7,z+5,DARK);a.table(x-5,4,z,8);
        } else if(use==8 || use==11) {
            for(int dx:new int[]{-10,-6,7})a.chest(x+dx,4,z-7,"castle_crafts");
            a.block(x+10,5,z+3,Blocks.CRAFTING_TABLE.getDefaultState());a.block(x+6,5,z+3,Blocks.ANVIL.getDefaultState());
            VillageJoinery.lowDesk(a,x-9,4,z+3,8);int shelf=x+(x<0?-13:13);a.box(shelf,5,z-4,shelf,7,z+3,Blocks.BOOKSHELF.getDefaultState());
        } else if(use==12) {
            for(int dx:new int[]{-9,-2,5})a.block(x+dx,5,z-7,Blocks.CAULDRON.getDefaultState());
            a.box(x-11,5,z+4,x+6,5,z+4,Blocks.SPRUCE_STAIRS.getDefaultState());a.chest(x+11,4,z+3,"palace_household");
        } else {
            VillageJoinery.lowDesk(a,x-5,4,z,10);
            if(use==0 || use==1) {
                for(int dx:new int[]{-13,13})a.box(x+dx,5,z-7,x+dx,8,z+5,Blocks.BOOKSHELF.getDefaultState());
                for(int dx:new int[]{-6,0,6})a.chest(x+dx,4,z-9,"hieda_records");
            } else {
                for(int dx:new int[]{-11,11})a.chest(x+dx,4,z+6,use==3?"village_pantry":"palace_household");
                for(int dx:new int[]{-4,0,4})a.block(x+dx,6,z,ModBlocks.LACQUER_BOWL.getDefaultState());
            }
        }
    }
    private static void dryGarden(GensokyoArchitecture a) {
        int[][] stones={{-24,-9},{26,23},{-28,40}};
        for(int z=-27;z<=55;z++)for(int x=-37;x<=37;x++) {
            if(Math.abs(x)<7)continue;
            double nearest=100;
            for(int[] pos:stones)nearest=Math.min(nearest,Math.hypot((x-pos[0])*.85,z-pos[1]));
            double ripple=nearest<16?nearest:z+32+2*Math.sin(x*.12);
            if(Math.floorMod((int)Math.round(ripple),5)==0)a.block(x,0,z,Blocks.SAND.getDefaultState());
        }
        a.box(-6,0,-30,6,0,61,STONE);
        for(int[] pos:stones) {
            int x=pos[0],z=pos[1],side=x<0?1:-1;
            GardenScenery.rocks(a,x,z,side);
            GardenScenery.pine(a,x+side*4,z+5,side);
        }
        for(int x:new int[]{-38,38})for(int z:new int[]{-25,52,112})a.lamp(x,1,z);
        for(int x:new int[]{-94,94})for(int z:new int[]{-90,-20,80})a.lamp(x,1,z);
        a.box(-112,0,-112,112,0,-100,STONE);
        a.box(35,0,-212,108,0,-204,STONE);
    }
    private static void gate(GensokyoArchitecture a) {
        for(int x:new int[]{-10,10})a.box(x,1,137,x,13,141,LOG);
        a.box(-11,11,136,11,13,142,DARK);a.gable(-16,131,16,147,14);
        a.sign(0,12,143,EnumFacing.SOUTH,"白玉楼","");
    }
    private static void gardenDetails(GensokyoArchitecture a) {
        // Cherry-viewing seats line the southern garden, beyond the dry garden's enclosure.
        for(int side:new int[]{-1,1}) {
            int x=side*68;
            GardenScenery.arbour(a,x,99,9,5);
            a.box(Math.min(0,x),0,109,Math.max(0,x),0,112,Blocks.GRAVEL.getDefaultState());
            a.box(x-2,0,105,x+2,0,112,Blocks.GRAVEL.getDefaultState());
            GardenScenery.plantedBed(a,side*91,94,8,6,3);
            a.room(side<0?"西赏樱席":"东赏樱席",x,0,99);
        }
        for(int z=-26;z<=50;z+=8)for(int x:new int[]{-40,40})
            a.box(x,0,z,x,0,z+3,Blocks.STONE_SLAB.getStateFromMeta(5));
        for(int z:new int[]{-21,2,25,48})for(int side:new int[]{-1,1})
            a.block(side*45,8,z,ModBlocks.RED_LANTERN.getDefaultState());
    }
}
