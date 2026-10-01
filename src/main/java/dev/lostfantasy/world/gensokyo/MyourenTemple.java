package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A worship hall and working monastery, with the cemetery reached around the east side. */
final class MyourenTemple {
    private MyourenTemple() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.MYOUREN);
        grounds(a);mainHall(a);westWing(a);eastWing(a);gate(a);bell(a);cemetery(a);
        gardenDetails(a);
    }
    private static void gardenDetails(GensokyoArchitecture a) {
        // The west strip between the scripture wing and dining hall becomes a quiet moss court.
        a.box(-86,0,18,-46,0,24,Blocks.GRAVEL.getDefaultState());
        GardenScenery.plantedBed(a,-80,5,6,8,8);
        GardenScenery.bench(a,-84,0,20,8,EnumFacing.NORTH);
        for(int z=-7;z<=15;z+=4) {
            a.box(-84,0,z,-82,0,z+1,STONE);a.box(-84,1,z,-82,2,z+1,AIR);
        }
        GardenScenery.plantedBed(a,30,-67,8,4,8);
        InteriorFinishes.tatami(a,-29,-25,29,-8,4);
        InteriorFinishes.tatami(a,-84,-37,-61,-26,4);
        for(int z:new int[]{-50,-33,-16,1,17,31})
            InteriorFinishes.tatami(a,62,z-3,76,z+4,4);
        for(int z:new int[]{34,40,46})for(int x:new int[]{-81,-77,-73})
            a.block(x,4,z,ModBlocks.LACQUER_BOWL.getDefaultState());
        a.room("经堂苔庭",-78,0,20);
    }
    private static void grounds(GensokyoArchitecture a) {
        a.box(-99,0,-87,99,0,85,Blocks.GRASS.getDefaultState());
        a.box(-98,1,-87,98,3,-87,WHITE);a.box(-99,1,-86,-99,3,85,WHITE);a.box(99,1,-86,99,3,85,WHITE);
        a.box(-99,4,-88,99,4,-86,SLAB);a.box(-100,4,-87,-98,4,85,SLAB);a.box(98,4,-87,100,4,85,SLAB);
        for(int[] edge:new int[][]{{-99,-18},{18,99}}) {a.box(edge[0],1,85,edge[1],3,85,WHITE);a.box(edge[0],4,84,edge[1],4,86,SLAB);}
        a.box(-15,0,10,15,0,96,Blocks.GRAVEL.getDefaultState());a.box(-6,0,10,6,0,96,STONE);
        a.box(-73,0,12,76,0,22,Blocks.GRAVEL.getDefaultState());
        a.box(84,0,-79,90,0,61,STONE);a.box(2,0,-79,90,0,-73,STONE);
        a.box(7,0,55,87,0,61,STONE);a.box(-86,0,55,-7,0,61,STONE);
        for(int z:new int[]{28,47,68,92})for(int x:new int[]{-12,12})a.lamp(x,1,z);
        for(int x:new int[]{-31,31})for(int z:new int[]{32,63})cherry(a,x,z);
        a.room("参拜道",0,0,59);a.room("东侧墓地通路",87,0,12);
    }
    private static void mainHall(GensokyoArchitecture a) {
        a.hall(-34,-37,34,-3,4,8,LOG);
        a.box(-33,12,-36,33,12,-4,WOOD);
        for(int x=-30;x<=30;x+=10)a.box(x,12,-36,x,12,-4,DARK);
        a.box(-40,13,-43,40,32,3,AIR);hipRoof(a,-40,-43,40,3,13);
        // Deep veranda, three entrances and a broad stair preserve the temple silhouette.
        a.box(-37,0,-2,37,3,9,STONE);a.box(-37,4,-2,37,4,9,WOOD);
        for(int x:new int[]{-30,-18,18,30})a.box(x,5,8,x,11,8,LOG);
        a.box(-39,12,1,39,12,11,ROOF);a.box(-39,13,11,39,13,11,SLAB);
        for(int x:new int[]{-21,0,21})a.openZ(x,-3,4,3,5);
        a.stairsSouth(0,10,0,4,9);
        for(int side:new int[]{-1,1}) {
            int x=side*36;a.box(x,5,0,x,5,8,Blocks.SPRUCE_FENCE.getDefaultState());
            a.box(side<0?-36:10,5,9,side<0?-10:36,5,9,Blocks.SPRUCE_FENCE.getDefaultState());
        }
        // An altar with a small sculptural icon, lamps, incense and ritual storage.
        a.box(-13,5,-32,13,6,-28,DARK);a.box(-9,7,-33,9,7,-30,WOOD);
        a.box(-3,8,-32,3,8,-30,Blocks.GOLD_BLOCK.getDefaultState());
        a.box(-2,9,-32,2,11,-31,Blocks.GOLD_BLOCK.getDefaultState());
        a.block(0,12,-31,Blocks.GOLD_BLOCK.getDefaultState());
        for(int x:new int[]{-11,11})a.block(x,7,-29,ModBlocks.LIBRARY_LAMP.getDefaultState());
        a.box(-3,5,-23,3,5,-22,WOOD);a.block(0,6,-22,Blocks.CAULDRON.getDefaultState());
        for(int x:new int[]{-22,-12,12,22})for(int z:new int[]{-16,-10})a.box(x-2,5,z-1,x+2,5,z+1,Blocks.CARPET.getStateFromMeta(13));
        a.box(-28,5,-35,-18,7,-35,Blocks.BOOKSHELF.getDefaultState());a.chest(27,4,-34,"school_supplies");
        a.chest(30,4,-34,"hieda_records");a.block(25,5,-30,Blocks.NOTEBLOCK.getDefaultState());
        for(int x:new int[]{-18,18})for(int z:new int[]{-25,-10}) {
            a.box(x,10,z,x,11,z,Blocks.OAK_FENCE.getDefaultState());a.block(x,9,z,ModBlocks.RED_LANTERN.getDefaultState());
        }
        a.box(-3,10,-2,3,11,-2,DARK);a.sign(0,10,-1,EnumFacing.SOUTH,"命莲寺","");
        for(int x:new int[]{-16,16})flag(a,x,11);
        a.room("大殿",0,4,-13);a.room("佛前供桌",0,4,-26);a.room("仪具收存",28,4,-27);
        // The rear doors connect to daily-use wings at the same floor level.
        a.openX(-34,-28,4,2,4);a.openX(34,-28,4,2,4);
        corridor(a,-48,-31,-34,-25,4);corridor(a,34,-31,48,-25,4);
    }
    private static void westWing(GensokyoArchitecture a) {
        VillageJoinery.house(a,-87,-59,-48,-11,4,1);
        a.openX(-48,-28,4,2,3);a.openZ(-54,-11,4,1,3);
        a.box(-89,4,-10,-46,4,-6,WOOD);a.stairsSouth(-54,-5,0,4,3);
        // East corridor serves a scripture room, copying desks and a private living room.
        VillageJoinery.wallX(a,-59,-58,-12,4,-48);a.openX(-59,-31,4,1,3);a.openX(-59,-18,4,1,3);
        VillageJoinery.wallZ(a,-86,-60,-39,4,-63);VillageJoinery.wallZ(a,-86,-60,-24,4,-63);
        for(int x:new int[]{-84,-77,-70})a.box(x,5,-57,x,7,-43,Blocks.BOOKSHELF.getDefaultState());
        a.chest(-63,4,-56,"school_supplies");a.room("经藏",-64,4,-47);
        VillageJoinery.lowDesk(a,-82,4,-33,7);a.chest(-84,4,-37,"bookbinding");a.room("抄经室",-64,4,-30);
        a.bed(-82,4,-16);VillageJoinery.lowDesk(a,-73,4,-18,5);a.chest(-84,4,-22,"hieda_records");a.room("住持起居",-64,4,-16);
        for(int z:new int[]{-48,-31,-18})VillageJoinery.lantern(a,-63,4,z);
        VillageJoinery.house(a,-87,29,-47,51,2,1);
        a.openZ(-54,51,2,2,3);a.box(-89,2,52,-45,2,55,WOOD);a.stairsSouth(-54,56,0,2,3);
        VillageJoinery.wallX(a,-65,30,50,2,40);
        for(int z:new int[]{34,40,46})VillageJoinery.lowDesk(a,-83,2,z,10);
        a.box(-62,3,32,-50,3,33,WOOD);a.block(-53,4,32,Blocks.CAULDRON.getStateFromMeta(3));
        a.block(-49,3,38,Blocks.FURNACE.getDefaultState());a.block(-49,3,42,Blocks.FURNACE.getDefaultState());
        a.chest(-62,2,48,"village_pantry");a.chest(-58,2,48,"village_pantry");
        a.room("斋堂",-68,2,44);a.room("厨房",-54,2,43);
        VillageJoinery.lantern(a,-69,2,43);VillageJoinery.lantern(a,-54,2,37);
    }
    private static void eastWing(GensokyoArchitecture a) {
        VillageJoinery.house(a,48,-59,79,37,4,1);
        a.openX(48,-28,4,2,3);a.openZ(54,37,4,1,3);
        a.box(46,4,38,81,4,42,WOOD);a.stairsSouth(54,43,0,4,3);
        a.box(59,5,-58,59,8,36,WHITE);
        int[] divisions={-42,-25,-8,9,24};
        for(int z:divisions)a.box(60,5,z,78,8,z,WHITE);
        int[] doors={-50,-33,-16,1,17,31};
        for(int i=0;i<doors.length;i++) {
            int z=doors[i];a.openX(59,z,4,1,3);a.bed(73,4,z);a.chest(76,4,z-4,"village_pantry");
            VillageJoinery.lowDesk(a,62,4,z-3,4);VillageJoinery.lantern(a,68,4,z);
            a.room("僧舍"+(i+1),68,4,z+3);
        }
        a.room("僧舍长廊",54,4,-47);
        // Small repair and laundry house has a ground-level entrance from the east walk.
        VillageJoinery.house(a,46,63,79,77,1,1);a.openZ(56,77,1,2,3);a.stairsSouth(56,78,0,1,3);
        a.box(51,0,80,61,0,83,Blocks.GRAVEL.getDefaultState());a.box(59,0,58,64,0,83,Blocks.GRAVEL.getDefaultState());
        a.block(49,2,66,Blocks.CRAFTING_TABLE.getDefaultState());a.block(53,2,66,Blocks.ANVIL.getDefaultState());
        for(int x:new int[]{69,73,77})a.block(x,2,66,Blocks.CAULDRON.getStateFromMeta(3));
        a.chest(49,1,74,"kourindou_tools");a.chest(76,1,74,"doll_materials");
        VillageJoinery.lantern(a,58,1,70);VillageJoinery.lantern(a,72,1,70);a.room("修补与洗涤",63,1,71);
    }
    private static void gate(GensokyoArchitecture a) {
        a.box(-19,0,74,19,0,86,STONE);
        for(int x:new int[]{-16,-8,8,16})for(int z:new int[]{76,84})a.box(x,1,z,x,9,z,LOG);
        a.box(-18,9,75,18,10,85,DARK);a.gable(-22,72,22,88,11);
        a.box(-16,1,77,-9,1,83,WOOD);a.box(9,1,77,16,1,83,WOOD);
        for(int x:new int[]{-12,12})jizo(a,x,2,80);
        a.box(-3,8,85,3,10,85,DARK);a.sign(0,9,86,EnumFacing.SOUTH,"命莲寺","");
        a.room("山门",0,0,80);
        for(int x:new int[]{-22,22})for(int z:new int[]{74,82})jizo(a,x,1,z);
    }
    private static void bell(GensokyoArchitecture a) {
        int x=-59,z=7;
        a.box(x-9,0,z-8,x+9,1,z+8,STONE);a.box(x-9,2,z-8,x+9,2,z+8,WOOD);
        for(int dx:new int[]{-6,6})for(int dz:new int[]{-5,5})a.box(x+dx,3,z+dz,x+dx,12,z+dz,LOG);
        a.box(x-7,12,z-6,x+7,13,z+6,DARK);hipRoof(a,x-11,z-10,x+11,z+10,14);
        a.box(x,10,z,x,11,z,Blocks.IRON_BARS.getDefaultState());
        // Hollow bronze bell and its suspended timber striker are built from ordinary blocks.
        for(int y=5;y<=9;y++) {
            int r=y>=8?1:2;
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)
                if(Math.abs(dx)==r || Math.abs(dz)==r || y==9)
                    a.block(x+dx,y,z+dz,Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(9));
        }
        a.box(x+3,6,z,x+7,6,z,Blocks.LOG.getStateFromMeta(4));
        for(int dx:new int[]{3,7})a.box(x+dx,7,z,x+dx,11,z,Blocks.OAK_FENCE.getDefaultState());
        a.stairsSouth(x,z+9,0,2,3);a.box(x-3,0,z+11,x+3,0,21,Blocks.GRAVEL.getDefaultState());
        a.room("钟楼",x-5,2,z+3);
    }
    private static void cemetery(GensokyoArchitecture a) {
        a.box(-87,0,-83,79,0,-65,Blocks.GRAVEL.getDefaultState());
        a.box(-89,0,-79,90,0,-75,STONE);
        for(int x=-81;x<=74;x+=13) {
            a.box(x-2,1,-72,x+2,1,-68,STONE);a.box(x-1,2,-71,x+1,3,-69,STONE);a.block(x,4,-70,STONE);
            a.block(x-2,2,-68,Blocks.FLOWER_POT.getDefaultState());
            if(x%2==0)jizo(a,x,1,-82);
        }
        a.room("墓地",0,0,-77);a.lamp(-89,1,-77);a.lamp(91,1,-77);
    }
    private static void jizo(GensokyoArchitecture a,int x,int y,int z) {
        a.block(x,y-1,z,STONE);a.block(x,y,z,ModBlocks.JIZO.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
    }
    private static void flag(GensokyoArchitecture a,int x,int z) {
        a.box(x,1,z,x,12,z,Blocks.OAK_FENCE.getDefaultState());a.box(x,12,z,x+3,12,z,Blocks.OAK_FENCE.getDefaultState());
        a.box(x+1,5,z,x+3,11,z,Blocks.WOOL.getStateFromMeta(14));
        a.sign(x+2,9,z+1,EnumFacing.SOUTH,"毘沙门天王","");
    }
    private static void corridor(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int floor) {
        a.box(x1,0,z1,x2,floor-1,z2,STONE);a.box(x1,floor,z1,x2,floor,z2,WOOD);
        for(int x=x1+3;x<x2;x+=6)for(int z:new int[]{z1,z2})a.box(x,floor+1,z,x,floor+4,z,LOG);
        a.gable(x1+1,z1-1,x2-1,z2+1,floor+5);
    }
    private static void hipRoof(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int y) {
        int depth=Math.min(x2-x1,z2-z1)/2;
        for(int i=0;i<=depth;i++) {
            int h=y+i/2;
            a.box(x1+i,h,z1+i,x2-i,h,z1+i,ROOF);a.box(x1+i,h,z2-i,x2-i,h,z2-i,ROOF);
            a.box(x1+i,h,z1+i,x1+i,h,z2-i,ROOF);a.box(x2-i,h,z1+i,x2-i,h,z2-i,ROOF);
        }
        a.box(x1-1,y+1,z1,x2+1,y+1,z1,SLAB);a.box(x1-1,y+1,z2,x2+1,y+1,z2,SLAB);
        a.box(x1+depth,y+depth/2+1,(z1+z2)/2,x2-depth,y+depth/2+1,(z1+z2)/2,SLAB);
    }
    private static void cherry(GensokyoArchitecture a,int x,int z) {
        a.box(x,1,z,x+1,9,z+1,LOG);
        for(int dx=-7;dx<=7;dx++)for(int dz=-7;dz<=7;dz++)for(int dy=-2;dy<=2;dy++)
            if(dx*dx+dz*dz+dy*dy*5<48 && Math.floorMod(dx*7+dz*13+dy,11)!=0)
                a.block(x+dx,10+dy,z+dz,ModBlocks.CHERRY_LEAVES.getDefaultState());
        a.box(x-4,7,z,x+5,7,z,Blocks.LOG.getStateFromMeta(4));
    }
}
