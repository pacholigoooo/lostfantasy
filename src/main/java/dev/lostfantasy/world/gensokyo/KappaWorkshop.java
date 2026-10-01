package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.init.Blocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Dry hillside workshops, shared facilities and homes along both banks. */
final class KappaWorkshop {
    private static final IBlockState BASALT=ModBlocks.COLUMNAR_BASALT.getDefaultState(),
            IRON=Blocks.IRON_BLOCK.getDefaultState(),GLASS=Blocks.GLASS.getDefaultState();
    private KappaWorkshop() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.KAPPA);
        rockShell(a);interior(a);upperRooms(a);court(a);pumpHouse(a);pipework(a);KappaHomes.build(plan);
    }
    private static void rockShell(GensokyoArchitecture a) {
        // The columns form a stepped rock cap, leaving the open watercourse to the east.
        a.box(-66,-2,-66,17,21,-7,BASALT);
        for(int x=-69;x<=18;x+=7)for(int z=-68;z<=-7;z+=7) {
            int height=capHeight(x,z);
            for(int dz=-3;dz<=3;dz++) {
                int half=Math.abs(dz)==3?1:Math.abs(dz)==2?2:3;
                a.box(x-half,0,z+dz,x+half,height,z+dz,BASALT);
                a.box(x-half,height+1,z+dz,x+half,height+1,z+dz,Math.floorMod(x*17+z*11,5)==0?Blocks.GRAVEL.getDefaultState():Blocks.GRASS.getDefaultState());
            }
        }
        for(int[] tree:new int[][]{{-55,-54},{-20,-61},{1,-26}}) {
            int x=tree[0],z=tree[1],floor=capHeight(x,z)+1;
            a.box(x,floor+1,z,x,floor+7,z,Blocks.LOG.getDefaultState());
            for(int y=4;y<=9;y++) {
                int radius=y>=8?2:3;
                for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)if(dx*dx+dz*dz<=radius*radius+1)
                    if(dx!=0 || dz!=0 || y>7)a.block(x+dx,floor+y,z+dz,Blocks.LEAVES.getStateFromMeta(4));
            }
        }
        a.box(-60,0,-53,14,0,-6,STONE);a.box(-58,1,-51,12,8,-8,AIR);
        a.box(-58,9,-51,12,9,-8,STONE);a.box(-58,10,-51,12,10,-8,WOOD);
        a.box(-58,11,-51,12,18,-8,AIR);a.box(-58,19,-51,12,19,-8,DARK);
        // Recessed masonry and windows sit in front of the natural rock, not on top of it.
        a.box(-60,1,-8,14,19,-7,STONE);
        for(int x=-54;x<=6;x+=12) {
            a.box(x,3,-8,x+7,6,-7,GLASS);a.box(x,13,-8,x+7,16,-7,GLASS);
        }
        for(int x:new int[]{-36,0})a.box(x-2,1,-9,x+2,4,-2,AIR);
        a.box(-62,0,-6,17,0,6,STONE);a.box(-62,1,-6,17,21,6,AIR);
        for(int x:new int[]{-60,-20,14})a.box(x,1,5,x,7,5,IRON);
        a.box(-63,8,-6,18,8,7,STONE);
        a.room("岸边装配廊",-27,0,1);
    }
    private static void interior(GensokyoArchitecture a) {
        for(int x:new int[]{-19,-9})a.box(x,1,-51,x,8,-9,STONE);
        for(int x:new int[]{-19,-9})for(int z:new int[]{-41,-14})a.openX(x,z,0,2,3);
        a.box(-58,1,-29,-20,8,-29,STONE);a.openZ(-27,-29,0,2,3);
        // Furnaces, anvils, crafting surfaces and their supply containers remain usable vanilla blocks.
        for(int x:new int[]{-51,-38}) {
            a.box(x-2,1,-45,x+3,1,-40,STONE);a.box(x-1,2,-44,x+2,2,-41,IRON);
            a.block(x,3,-43,Blocks.ANVIL.getDefaultState());
            a.block(x+3,2,-40,Blocks.CRAFTING_TABLE.getDefaultState());
        }
        for(int x:new int[]{-55,-47,-39})a.block(x,1,-50,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        for(int x:new int[]{-55,-47,-39})a.chest(x,0,-32,"kappa_tools");
        a.box(-32,1,-48,-23,1,-44,WOOD);a.block(-28,2,-47,Blocks.CRAFTING_TABLE.getDefaultState());
        a.chest(-54,0,-36,"kappa_parts");a.chest(-47,0,-36,"kappa_parts");a.block(-51,1,-36,Blocks.CRAFTING_TABLE.getDefaultState());
        a.box(-56,1,-14,-36,1,-12,WOOD);a.block(-51,2,-13,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(-43,2,-13,Blocks.ANVIL.getDefaultState());
        for(int x:new int[]{-54,-46,-38})a.chest(x,0,-26,"kappa_parts");
        a.box(-6,1,-31,11,8,-31,STONE);a.openZ(2,-31,0,1,3);
        a.box(-5,1,-47,8,1,-45,WOOD);a.block(-2,2,-46,ModBlocks.OUTSIDE_TELEVISION.getDefaultState());
        a.block(5,2,-46,Blocks.NOTEBLOCK.getDefaultState());a.chest(10,0,-48,"kappa_parts");
        a.box(-6,1,-38,-4,2,-34,IRON);a.block(-5,3,-36,lever());
        a.box(7,1,-27,10,1,-12,WOOD);
        for(int z:new int[]{-24,-17})a.block(8,2,z,Blocks.CRAFTING_TABLE.getDefaultState());
        a.chest(-6,0,-27,"kappa_tools");a.block(-5,1,-12,Blocks.CAULDRON.getStateFromMeta(3));
        for(int x:new int[]{-48,-30,-14,2})for(int z:new int[]{-44,-21})a.block(x,8,z,Blocks.SEA_LANTERN.getDefaultState());
        for(int rise=1;rise<=10;rise++) {
            int z=-10-rise;
            a.box(-16,rise,z,-12,rise,z,Blocks.STONE_BRICK_STAIRS.getDefaultState()
                    .withProperty(net.minecraft.block.BlockStairs.FACING,EnumFacing.NORTH));
            a.box(-16,rise+1,z,-12,rise+3,z,AIR);
        }
        a.room("金工作业",-29,0,-39);a.room("拆修工作间",-28,0,-19);
        a.room("电器调试",2,0,-38);a.room("防水装配",1,0,-19);
    }
    private static void upperRooms(GensokyoArchitecture a) {
        for(int x:new int[]{-19,-9})a.box(x,11,-51,x,18,-9,WHITE);
        for(int x:new int[]{-19,-9})for(int z:new int[]{-42,-24})a.openX(x,z,10,1,3);
        a.box(-58,11,-31,-20,18,-31,WHITE);a.box(-39,11,-50,-39,18,-32,WHITE);
        for(int x:new int[]{-48,-29})a.openZ(x,-31,10,1,3);
        a.box(-57,11,-50,-41,14,-50,Blocks.BOOKSHELF.getDefaultState());
        a.box(-57,11,-49,-57,13,-43,Blocks.BOOKSHELF.getDefaultState());
        a.chest(-54,10,-48,"school_supplies");VillageJoinery.lowDesk(a,-53,10,-38,9);
        a.block(-47,11,-47,DARK);a.block(-47,12,-47,ModBlocks.LIBRARY_LAMP.getDefaultState());
        a.box(-36,11,-49,-24,11,-46,WOOD);a.block(-32,12,-47,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(-27,12,-47,ModBlocks.OUTSIDE_TELEVISION.getDefaultState());a.chest(-22,10,-48,"kappa_parts");
        a.box(-37,11,-40,-21,15,-40,GLASS);a.openZ(-29,-40,10,2,3);
        a.box(-36,11,-36,-33,11,-34,WOOD);a.block(-35,12,-35,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.room("资料阅览",-47,10,-35);a.room("样机维护",-27,10,-35);
        a.box(-56,11,-25,-53,11,-12,WOOD);a.block(-54,12,-24,Blocks.CAULDRON.getStateFromMeta(3));
        a.block(-56,11,-17,Blocks.FURNACE.getDefaultState());a.chest(-55,10,-10,"village_pantry");
        a.box(-48,11,-30,-48,18,-9,WHITE);a.openX(-48,-20,10,1,3);
        VillageJoinery.lowDesk(a,-44,10,-22,10);a.chest(-43,10,-11,"village_pantry");
        a.box(-30,11,-18,-20,18,-18,WHITE);a.box(-30,11,-17,-30,18,-9,WHITE);a.openZ(-25,-18,10,1,3);
        a.block(-27,11,-12,Blocks.CAULDRON.getStateFromMeta(3));a.block(-23,11,-12,Blocks.CAULDRON.getStateFromMeta(3));
        a.box(-29,13,-10,-21,13,-10,Blocks.IRON_BARS.getDefaultState());
        a.block(-25,18,-13,Blocks.SEA_LANTERN.getDefaultState());
        a.box(-8,11,-30,12,18,-30,WHITE);a.openZ(2,-30,10,1,3);
        a.box(11,11,-49,11,14,-34,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.lowDesk(a,-4,10,-43,10);a.chest(-6,10,-48,"school_supplies");
        a.box(-5,11,-25,7,11,-24,WOOD);a.block(1,12,-24,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.chest(10,10,-12,"kappa_parts");a.chest(10,10,-19,"kappa_tools");
        for(int x:new int[]{-48,-29,2})for(int z:new int[]{-40,-18})a.block(x,18,z,Blocks.SEA_LANTERN.getDefaultState());
        a.room("茶饭间",-34,10,-16);a.room("厨房",-51,10,-20);a.room("洗涤间",-25,10,-15);
        a.room("图纸记录",3,10,-36);a.room("器材收存",2,10,-18);
        a.room("楼上连廊",-14,10,-24);
    }
    private static void court(GensokyoArchitecture a) {
        // Dry decks surround a sunken, river-connected water-testing basin.
        a.box(-62,-6,7,17,0,48,STONE);a.box(-62,1,7,17,5,48,AIR);
        a.box(-56,-5,12,9,-3,42,Blocks.WATER.getDefaultState());a.box(-56,-2,12,9,0,42,AIR);
        a.box(10,-5,20,24,-3,25,Blocks.WATER.getDefaultState());a.box(10,-2,20,24,0,25,AIR);
        a.box(-4,0,7,4,0,58,WOOD);a.box(-4,1,8,-4,1,42,Blocks.IRON_BARS.getDefaultState());a.box(4,1,8,4,1,42,Blocks.IRON_BARS.getDefaultState());
        a.box(-56,1,11,9,1,11,Blocks.IRON_BARS.getDefaultState());a.box(-56,1,43,9,1,43,Blocks.IRON_BARS.getDefaultState());
        a.box(-57,1,12,-57,1,42,Blocks.IRON_BARS.getDefaultState());a.box(10,1,12,10,1,42,Blocks.IRON_BARS.getDefaultState());
        a.box(-3,1,11,3,2,11,AIR);a.box(-3,1,43,3,2,43,AIR);
        for(int z:new int[]{-1,30}) {
            a.box(12,0,z-3,50,0,z+3,WOOD);
            for(int edge:new int[]{z-3,z+3})a.box(18,1,edge,46,1,edge,Blocks.IRON_BARS.getDefaultState());
        }
        a.box(-10,0,49,10,0,62,Blocks.GRAVEL.getDefaultState());
        for(int x:new int[]{-60,14})for(int z:new int[]{8,46})a.lamp(x,1,z);
        a.box(-59,1,16,-59,1,36,WOOD);a.block(-59,2,26,Blocks.CRAFTING_TABLE.getDefaultState());
        a.chest(-60,0,39,"kappa_tools");a.chest(-60,0,11,"kappa_parts");
        a.room("试水池西岸",-60,0,29);a.room("试水池栈桥",0,0,28);a.room("管线过河桥",31,0,-1);
    }
    private static void pumpHouse(GensokyoArchitecture a) {
        a.box(46,-5,-28,68,0,39,STONE);a.box(47,1,-27,67,10,38,AIR);
        a.box(48,1,-25,65,7,15,STONE);a.box(49,1,-24,64,6,14,AIR);a.box(46,8,-27,67,8,17,STONE);
        a.openX(48,-1,0,2,3);a.openZ(56,15,0,2,3);
        a.box(65,2,-20,65,5,9,GLASS);
        for(int z:new int[]{-17,-6,6}) {
            a.box(56,1,z-2,59,1,z+2,STONE);a.box(57,2,z-1,58,3,z+1,IRON);
            a.block(56,2,z+2,lever());a.block(56,5,z,Blocks.SEA_LANTERN.getDefaultState());
        }
        a.chest(51,0,-22,"kappa_parts");a.chest(62,0,-24,"kappa_tools");
        a.block(62,1,12,Blocks.CRAFTING_TABLE.getDefaultState());a.block(51,1,12,Blocks.FURNACE.getDefaultState());
        a.box(48,1,36,65,1,36,Blocks.IRON_BARS.getDefaultState());
        a.room("岸边泵房",52,0,4);a.room("泵房前台",56,0,25);
    }
    private static void pipework(GensokyoArchitecture a) {
        IBlockState x=ModBlocks.KAPPA_PIPE.getDefaultState().withProperty(BlockRotatedPillar.AXIS,EnumFacing.Axis.X),
                y=ModBlocks.KAPPA_PIPE.getDefaultState(),z=ModBlocks.KAPPA_PIPE.getDefaultState().withProperty(BlockRotatedPillar.AXIS,EnumFacing.Axis.Z);
        a.box(-56,7,-6,60,7,-6,x);a.box(-56,6,-6,-56,7,-6,IRON);a.box(60,5,-6,60,7,-6,y);
        a.box(-55,20,-6,9,20,-6,x);a.box(9,7,-6,9,20,-6,y);
        for(int zz:new int[]{-17,6}) {
            a.box(57,4,zz,62,4,zz,x);a.box(62,4,zz,62,6,zz,y);
        }
        a.box(62,6,-22,62,6,10,z);a.box(62,-3,-22,62,6,-22,y);
        for(int xx:new int[]{-56,9,60})a.block(xx,7,-6,IRON);
        // A short intake hangs over the river, while the main conduits stay above walking headroom.
        a.box(42,4,-18,51,4,-18,x);a.box(42,-3,-18,42,4,-18,y);
        a.block(42,4,-18,IRON);a.block(51,4,-18,IRON);
    }
    private static IBlockState lever() {return Blocks.LEVER.getDefaultState().withProperty(net.minecraft.block.BlockLever.FACING,net.minecraft.block.BlockLever.EnumOrientation.UP_X);}
    private static int capHeight(int x,int z) {
        double ridge=Math.exp(-Math.pow((x+25)/43.0,2)-Math.pow((z+36)/32.0,2));
        return 22+(int)Math.round(10*ridge)+Math.floorMod((x*73428767)^(z*912931),4);
    }
}
