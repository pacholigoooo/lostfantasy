package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A forest trading base with a residential lane, tool stores and material yards. */
final class YamawaroBase {
    private YamawaroBase() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.SECRET_CLIFF);
        a.box(-148,0,-35,145,0,35,Blocks.GRASS.getDefaultState());
        a.box(-147,1,-34,144,24,34,AIR);
        a.box(-4,0,-7,4,0,40,Blocks.GRAVEL.getDefaultState());
        a.box(-144,0,-3,139,0,4,Blocks.GRAVEL.getDefaultState());
        a.box(-145,0,32,139,0,34,Blocks.GRAVEL.getDefaultState());
        workshop(a);store(a);YamawaroHomes.build(plan);materialStore(a);
        canopy(a,-35,15);canopy(a,35,15);
        for(int x:new int[]{-21,21}) {
            a.box(x-6,0,14,x+6,0,24,STONE);
            a.box(x-5,1,16,x-3,1,22,WOOD);a.box(x+3,1,16,x+5,1,22,WOOD);
            a.block(x,1,14,Blocks.CRAFTING_TABLE.getDefaultState());
            a.chest(x,0,24,"yamawaro_trade");
            a.lamp(x,0,10);
        }
        // A low retaining wall leaves the front lane open toward the graded mountain road.
        for(int x=-147;x<=144;x++)if(Math.abs(x)>6)a.block(x,1,35,Blocks.COBBLESTONE_WALL.getDefaultState());
        a.box(-147,1,-34,-32,2,-34,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        a.box(32,1,-34,77,2,-34,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        for(int[] p:new int[][]{{-36,-19},{36,-18},{-8,25},{8,25}})a.lamp(p[0],0,p[1]);
        workYard(a);
        a.room("前场",0,0,29);a.room("西交易庭",-21,0,19);a.room("东交易庭",21,0,19);
    }
    private static void workshop(GensokyoArchitecture a) {
        VillageJoinery.house(a,-29,-29,29,-9,1,2);
        a.openZ(0,-9,1,3,4);entry(a,-4,4,-8);
        for(int floor:new int[]{1,6}) {
            VillageJoinery.wallX(a,-10,-28,-10,floor,-17);VillageJoinery.wallX(a,10,-28,-10,floor,-17);
        }
        a.box(-2,6,-24,2,9,-18,AIR);a.stairsSouth(0,-24,1,6,1);
        a.box(-3,7,-23,-3,7,-19,Blocks.SPRUCE_FENCE.getDefaultState());a.box(3,7,-23,3,7,-19,Blocks.SPRUCE_FENCE.getDefaultState());
        // Ground floor: counting counter and a separate fitting shop.
        VillageJoinery.lowDesk(a,-25,1,-16,8);a.chest(-26,1,-24,"yamawaro_trade");a.chest(-14,1,-24,"school_supplies");
        VillageJoinery.wallZ(a,-28,-11,-21,1,-19);
        rack(a,-23,1,-26,7);a.box(-27,2,-12,-14,2,-12,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        a.box(-17,2,-19,-17,2,-16,WOOD);
        a.box(15,2,-25,24,2,-24,WOOD);a.block(17,3,-24,Blocks.CRAFTING_TABLE.getDefaultState());a.block(23,3,-24,Blocks.ANVIL.getDefaultState());
        for(int z:new int[]{-20,-16})a.block(27,2,z,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.WEST));
        a.chest(14,1,-13,"yamawaro_tools");a.chest(24,1,-13,"kappa_parts");
        a.box(14,2,-19,16,2,-18,WOOD);a.block(15,3,-19,ModBlocks.WRITING_DESK.getDefaultState());
        a.box(25,2,-27,27,4,-26,Blocks.BOOKSHELF.getDefaultState());
        // Upstairs: agreements, calculation and component drawings.
        a.box(-27,7,-27,-13,9,-27,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.wallX(a,-20,-26,-22,6,-24);
        for(int x:new int[]{-26,-18}) {VillageJoinery.lowDesk(a,x,6,-23,3);VillageJoinery.lowDesk(a,x,6,-17,3);}
        a.box(-27,7,-19,-27,8,-16,Blocks.BOOKSHELF.getDefaultState());
        a.chest(-26,6,-12,"school_supplies");a.chest(-13,6,-12,"yamawaro_trade");
        VillageJoinery.lowDesk(a,14,6,-23,4);VillageJoinery.lowDesk(a,22,6,-23,4);a.box(14,7,-12,26,7,-12,WOOD);
        rack(a,18,6,-27,8);a.box(26,7,-19,27,8,-17,Blocks.BOOKSHELF.getDefaultState());
        a.block(24,8,-12,ModBlocks.OUTSIDE_TELEVISION.getDefaultState());a.chest(15,6,-16,"kappa_parts");
        for(int x:new int[]{-7,7})a.box(x,2,-17,x,2,-11,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,x<0?EnumFacing.EAST:EnumFacing.WEST));
        VillageJoinery.lowDesk(a,5,6,-14,3);a.box(-8,7,-27,-7,9,-24,Blocks.BOOKSHELF.getDefaultState());
        for(int floor:new int[]{1,6})for(int x:new int[]{-19,19})VillageJoinery.lantern(a,x,floor,-21);
        VillageJoinery.lantern(a,6,1,-14);VillageJoinery.lantern(a,0,6,-27);
        for(int floor:new int[]{1,6}) {
            a.room(floor==1?"交易柜台":"账册与往来文书",-19,floor,-20);
            a.room(floor==1?"配件装配":"器具图纸",19,floor,-18);
        }
        a.room("楼梯上层",0,6,-26);a.room("工房门厅",0,1,-14);
        a.room("交易后库",-19,1,-24);
    }
    private static void store(GensokyoArchitecture a) {
        VillageJoinery.house(a,44,-29,74,-9,1,1);a.openZ(59,-9,1,2,3);entry(a,56,62,-8);
        VillageJoinery.wallX(a,60,-28,-10,1,-18);
        for(int z:new int[]{-26,-22,-14}) {
            a.chest(47,1,z,"yamawaro_tools");a.chest(71,1,z,"yamawaro_trade");
        }
        a.box(52,2,-26,57,3,-24,Blocks.LOG.getStateFromMeta(12));a.box(65,2,-26,67,2,-23,Blocks.IRON_BLOCK.getDefaultState());
        a.box(52,2,-13,57,2,-12,WOOD);a.block(55,3,-12,Blocks.CRAFTING_TABLE.getDefaultState());
        rack(a,51,1,-20,7);rack(a,64,1,-13,5);
        // The warehouse uses dark timber panels and a low green roof, distinct from the homes.
        for(int x=45;x<74;x++)if(x%5!=4)a.block(x,2,-29,DARK);
        for(int z=-31;z<=-7;z++)a.box(42,7+(12-Math.abs(z+19))/2,z,76,7+(12-Math.abs(z+19))/2,z,Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(13));
        VillageJoinery.lantern(a,54,1,-20);VillageJoinery.lantern(a,66,1,-18);
        a.room("木料与工具",54,1,-18);a.room("货品收存",66,1,-19);
    }
    private static void materialStore(GensokyoArchitecture a) {
        a.box(87,0,-28,139,0,28,STONE);
        a.box(92,1,-25,135,6,-10,WOOD);a.box(93,2,-24,134,5,-11,AIR);
        a.box(92,1,-25,135,1,-10,WOOD);a.box(92,6,-25,135,6,-10,WOOD);
        a.openZ(112,-10,1,3,3);entry(a,109,115,-9);
        YamawaroHomes.roof(a,92,-25,135,-10,7,false);
        for(int x:new int[]{96,105,120,130})a.chest(x,1,-22,"yamawaro_tools");
        a.box(95,2,-14,103,2,-13,WOOD);a.block(98,3,-13,Blocks.CRAFTING_TABLE.getDefaultState());
        a.box(122,2,-14,132,2,-13,WOOD);a.block(127,3,-13,Blocks.ANVIL.getDefaultState());
        VillageJoinery.lantern(a,102,1,-18);VillageJoinery.lantern(a,124,1,-18);
        for(int z:new int[]{10,16,22})a.box(97,1,z,110,2,z+1,Blocks.LOG.getStateFromMeta(12));
        a.box(124,1,12,137,1,13,WOOD);a.block(129,2,12,Blocks.CRAFTING_TABLE.getDefaultState());
        a.room("共用器材库",112,1,-17);a.room("林料场",119,0,18);
    }
    private static void canopy(GensokyoArchitecture a,int x,int z) {
        a.box(x-5,0,z-7,x+5,0,z+7,WOOD);
        for(int dx:new int[]{-5,5})for(int dz:new int[]{-7,7})a.box(x+dx,1,z+dz,x+dx,5,z+dz,LOG);
        a.box(x-6,6,z-8,x+6,6,z+8,Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(13));
        a.box(x-4,1,z-5,x+3,1,z-4,WOOD);a.block(x-2,2,z-4,Blocks.CRAFTING_TABLE.getDefaultState());
        a.chest(x+3,0,z+5,"yamawaro_trade");a.block(x,5,z,ModBlocks.RED_LANTERN.getDefaultState());
        a.room(x<0?"西侧摆货棚":"东侧摆货棚",x,0,z);
    }
    private static void entry(GensokyoArchitecture a,int x1,int x2,int z) {
        for(int x=x1;x<=x2;x++)a.block(x,1,z,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
    }
    private static void rack(GensokyoArchitecture a,int x,int floor,int z,int length) {
        a.box(x,floor+1,z,x+length-1,floor+1,z,WOOD);a.box(x,floor+3,z,x+length-1,floor+3,z,Blocks.WOODEN_SLAB.getStateFromMeta(1));
        a.box(x,floor+2,z,x,floor+3,z,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(x+length-1,floor+2,z,x+length-1,floor+3,z,Blocks.SPRUCE_FENCE.getDefaultState());
        for(int i=2;i<length-1;i+=3)a.block(x+i,floor+2,z,Blocks.BOOKSHELF.getDefaultState());
    }
    private static void workYard(GensokyoArchitecture a) {
        a.box(48,0,9,66,0,25,Blocks.DIRT.getStateFromMeta(1));
        for(int z:new int[]{11,13})a.box(50,1,z,59,2,z,Blocks.LOG.getStateFromMeta(12));
        a.box(54,1,21,61,1,22,WOOD);a.block(56,2,21,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(60,2,21,Blocks.ANVIL.getDefaultState());
        a.box(65,0,16,67,1,22,STONE);a.box(66,1,17,66,1,21,Blocks.WATER.getDefaultState());
        for(int[] tree:new int[][]{{72,9},{70,28},{-9,30}}) {
            a.box(tree[0],1,tree[1],tree[0],7,tree[1],LOG);
            for(int y=4;y<=10;y++) {
                int r=y<8?3:2;
                for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)if(x*x+z*z<=r*r+1 && (x!=0 || z!=0 || y>7))
                    a.block(tree[0]+x,y,tree[1]+z,Blocks.LEAVES.getStateFromMeta(5));
            }
        }
        a.room("露天修配场",57,0,17);
    }
}
