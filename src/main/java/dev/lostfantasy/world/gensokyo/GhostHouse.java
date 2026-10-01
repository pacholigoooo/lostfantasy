package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.entity.EntityHouseSpirit;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** The forest ruin used as a kappa storehouse, distinct from the Prismriver estate. */
final class GhostHouse {
    private static final IBlockState PLASTER=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(8),
            TRIM=Blocks.SANDSTONE.getStateFromMeta(2),BRICK=Blocks.BRICK_BLOCK.getDefaultState();
    static final int[][] SPIRITS={{0,6,10},{-15,12,-10},{15,5,-13}};
    private GhostHouse() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.GHOST_HOUSE);
        surroundings(a);shell(a);interior(a);wear(a);
        a.room("门前石径",0,0,30);a.room("楼梯厅",0,2,12);a.room("上层回廊",-6,9,0);
        a.room("西侧收存",-13,2,-10);a.room("旧书与分拣",-13,2,7);
        a.room("零件收存",13,2,-10);a.room("装卸与修补",13,2,7);
        a.room("西侧旧寝间",-13,9,-8);a.room("东侧旧寝间",13,9,-8);
        a.room("西侧寝床",-20,9,-7);a.room("东侧寝床",20,9,-7);
        a.room("封存书物",-13,9,7);a.room("旧器具间",13,9,7);a.room("侧门",28,0,7);
    }
    private static void shell(GensokyoArchitecture a) {
        a.box(-24,0,-20,24,1,18,STONE);a.box(-24,2,-20,24,16,18,PLASTER);
        a.box(-23,3,-19,23,15,17,AIR);
        for(int y:new int[]{2,9,16})a.box(-23,y,-19,23,y,17,WOOD);
        // The hall opens through both storeys; galleries run around three sides of the stairs.
        a.box(-7,9,2,7,9,10,AIR);
        for(int y:new int[]{2,9,16}) {
            a.box(-25,y,-21,25,y,-21,TRIM);a.box(-25,y,19,25,y,19,TRIM);
            a.box(-25,y,-20,-25,y,18,TRIM);a.box(25,y,-20,25,y,18,TRIM);
        }
        for(int x:new int[]{-24,-9,9,24})for(int z:new int[]{-20,18})a.box(x,3,z,x,15,z,BRICK);
        for(int f:new int[]{2,9}) {
            for(int x:new int[]{-17,0,17}) {window(a,x,f,-20,false);window(a,x,f,18,false);}
            for(int z:new int[]{-12,7}) {window(a,-24,f,z,true);window(a,24,f,z,true);}
        }
        a.gable(-27,-23,27,21,17);
        // A forward gable and stone porch interrupt the long roof and frame the entry.
        a.box(-5,0,19,5,1,23,STONE);a.box(-5,2,19,5,2,23,TRIM);
        for(int x:new int[]{-5,5})a.box(x,3,22,x,7,22,TRIM);
        a.box(-6,8,18,6,8,24,WOOD);a.gableZ(-7,17,7,25,9);
        a.box(-2,3,18,2,7,19,AIR);a.stairsSouth(0,24,0,2,3);
        // A second entrance lets the kappa carry supplies without crossing the stairs.
        a.box(24,3,5,25,6,9,AIR);
        for(int x=26;x<=27;x++) {
            int h=28-x;if(h>1)a.box(x,1,5,x,h-1,9,STONE);
            a.box(x,h,5,x,h,9,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.WEST));
        }
    }
    private static void window(GensokyoArchitecture a,int x,int f,int z,boolean side) {
        for(int u=-2;u<=2;u++)for(int v=1;v<=5;v++) {
            IBlockState s=Math.abs(u)==2 || v==1 || v==5 && Math.abs(u)>0?TRIM:Blocks.GLASS_PANE.getDefaultState();
            a.block(x+(side?0:u),f+v,z+(side?u:0),s);
        }
    }
    private static void interior(GensokyoArchitecture a) {
        for(int f:new int[]{2,9})for(int x:new int[]{-9,9}) {
            a.box(x,f+1,-19,x,f+6,17,PLASTER);a.openX(x,-9,f,1,3);a.openX(x,7,f,1,3);
        }
        for(int f:new int[]{2,9})for(int[] side:new int[][]{{-23,-10,-14},{10,23,14}}) {
            a.box(side[0],f+1,-3,side[1],f+6,-3,PLASTER);a.openZ(side[2],-3,f,1,3);
        }
        a.box(-2,3,2,2,12,8,AIR);a.stairsSouth(0,2,2,9,2);
        a.box(-7,10,10,7,10,10,Blocks.SPRUCE_FENCE.getDefaultState());
        for(int x:new int[]{-7,7})a.box(x,10,2,x,10,9,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(-2,10,1,2,12,1,AIR);
        // Stored parts use low racks with a clear approach to every chest.
        rack(a,-22,-18,2,"kappa_tools");rack(a,-16,-18,2,"kappa_parts");rack(a,-22,-7,2,"kappa_parts");
        rack(a,11,-18,2,"kappa_parts");rack(a,18,-18,2,"kappa_parts");rack(a,18,-7,2,"kappa_tools");
        rack(a,-21,-12,2,"kappa_parts");rack(a,17,-12,2,"kappa_parts");
        a.box(12,3,-7,14,4,-6,Blocks.IRON_BLOCK.getDefaultState());
        a.block(13,5,-7,ModBlocks.KAPPA_PIPE.getDefaultState());
        a.box(-22,3,0,-22,6,7,Blocks.BOOKSHELF.getDefaultState());
        a.box(-21,3,13,-15,4,14,DARK);a.block(-18,5,13,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.chest(-11,2,15,"ghost_storehouse");a.chest(-11,2,0,"forest_books");
        a.box(-20,3,7,-17,3,8,WOOD);a.block(-18,4,7,Blocks.FLOWER_POT.getDefaultState());
        a.box(17,3,14,22,3,15,STONE);a.block(18,3,13,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(21,3,13,Blocks.FURNACE.getDefaultState());a.block(12,3,13,Blocks.ANVIL.getDefaultState());
        a.chest(21,2,1,"kappa_tools");a.box(11,3,0,14,4,1,WOOD);a.block(12,5,0,ModBlocks.OUTSIDE_TELEVISION.getDefaultState());
        for(int hand:new int[]{-1,1}) {
            int bx=hand*19;
            a.box(hand*16,10,-19,hand*16,15,-4,PLASTER);a.openX(hand*16,-7,9,1,3);
            a.bed(bx,9,-10);a.chest(hand*11,9,-17,"ghost_storehouse");
            a.box(hand<0?-22:18,10,-18,hand<0?-18:22,12,-18,DARK);
            VillageJoinery.lowDesk(a,hand<0?-14:12,9,-14,3);
            a.box(hand<0?-23:23,10,-8,hand<0?-23:23,13,-5,Blocks.BOOKSHELF.getDefaultState());
            a.block(hand*12,10,-5,Blocks.CAULDRON.getDefaultState());a.block(hand*14,10,-5,DARK);
        }
        a.box(-22,10,0,-20,12,1,Blocks.BOOKSHELF.getDefaultState());rack(a,-22,12,9,"forest_books");
        a.box(-16,10,0,-13,11,1,WOOD);a.chest(-11,9,15,"ghost_storehouse");
        a.box(-22,10,6,-18,12,6,Blocks.BOOKSHELF.getDefaultState());VillageJoinery.lowDesk(a,-17,9,10,4);
        a.box(17,10,13,22,11,14,DARK);a.block(19,12,13,ModBlocks.GRAMOPHONE.getDefaultState());
        a.box(12,10,0,15,11,1,Blocks.WOOL.getStateFromMeta(0));a.chest(21,9,1,"kappa_parts");
        bench(a,18,21,9,5,EnumFacing.SOUTH);bench(a,18,21,9,9,EnumFacing.NORTH);
        a.box(18,10,7,20,10,7,WOOD);a.block(21,10,7,Blocks.FLOWER_POT.getDefaultState());
        // The broad back of the hall is still useful: sorting below, old seats above.
        a.box(-3,3,-12,3,3,-10,DARK);a.block(0,4,-11,ModBlocks.RESEARCH_NOTES.getDefaultState());
        bench(a,-3,3,2,-14,EnumFacing.SOUTH);bench(a,-3,3,2,-8,EnumFacing.NORTH);
        a.box(-4,10,-13,4,10,-5,Blocks.CARPET.getStateFromMeta(7));
        a.box(-1,10,-10,1,10,-8,DARK);
        for(int z=-11;z<=-7;z++)for(int hand:new int[]{-1,1})a.block(hand*4,10,z,Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,hand<0?EnumFacing.EAST:EnumFacing.WEST));
        // Old fireplaces and recently hung lamps illuminate the working lanes with block light.
        for(int f:new int[]{2,9}) {
            a.box(-3,f+1,-19,3,f+4,-17,STONE);a.box(-1,f+1,-16,1,f+2,-16,Blocks.IRON_BARS.getDefaultState());
            a.box(-1,f+1,-17,1,f+2,-17,LIGHT);
            for(int[] p:new int[][]{{-14,-11},{14,-11},{-14,8},{14,8}})a.block(p[0],f+6,p[1],ModBlocks.RED_LANTERN.getDefaultState());
        }
        a.box(-2,17,-19,2,29,-17,BRICK);a.box(-3,29,-20,3,29,-16,SLAB);
        // A narrow chandelier leaves the central stairs clear.
        a.box(0,13,11,0,15,11,Blocks.IRON_BARS.getDefaultState());a.block(0,12,11,ModBlocks.RED_LANTERN.getDefaultState());
    }
    private static void bench(GensokyoArchitecture a,int x1,int x2,int floor,int z,EnumFacing facing) {
        a.box(x1,floor+1,z,x2,floor+1,z,Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,facing));
    }
    private static void rack(GensokyoArchitecture a,int x,int z,int f,String loot) {
        a.box(x,f+1,z,x,f+4,z,LOG);a.box(x+4,f+1,z,x+4,f+4,z,LOG);
        a.box(x,f+4,z,x+4,f+4,z,WOOD);a.chest(x+1,f,z,loot);
        a.box(x+3,f+1,z,x+3,f+2,z,WOOD);
    }
    private static void surroundings(GensokyoArchitecture a) {
        for(int[] p:new int[][]{{-32,-22,23,7},{31,-22,26,7},{-31,19,22,6},{30,25,19,6}})ForestDetails.tree(a,p[0],p[1],p[2],p[3]);
        a.box(-3,0,25,3,0,39,Blocks.GRAVEL.getDefaultState());a.box(26,0,5,34,0,9,Blocks.GRAVEL.getDefaultState());
        for(int x=-21;x<=20;x++)for(int z=26;z<=32;z++) {
            if(Math.abs(x)<5 || Math.floorMod(x*7+z*13,9)<3)continue;
            a.block(x,0,z,Math.floorMod(x+z,3)==0?Blocks.MOSSY_COBBLESTONE.getDefaultState():Blocks.STONEBRICK.getStateFromMeta(2));
        }
        for(int x:new int[]{-8,8}) {a.box(x,0,32,x,3,32,STONE);a.block(x,4,32,SLAB);}
        for(int x=-20;x<=20;x++)if(Math.abs(x)>8 && Math.floorMod(x,7)!=0)a.block(x,1,32,Blocks.IRON_BARS.getDefaultState());
        for(int[] p:new int[][]{{-30,-4},{30,-10},{-17,28},{22,29}}) {
            a.block(p[0],0,p[1],Blocks.MYCELIUM.getDefaultState());a.block(p[0],1,p[1],Blocks.BROWN_MUSHROOM.getDefaultState());
        }
    }
    private static void wear(GensokyoArchitecture a) {
        // Local decay spares the circulation routes and the storehouse's usable floors.
        for(int[] p:new int[][]{{-24,4,-17},{24,5,-5},{-11,11,18},{10,4,-20},{23,12,-20}})a.block(p[0],p[1],p[2],Blocks.STONEBRICK.getStateFromMeta(2));
        for(int[] w:new int[][]{{-17,6,18},{17,13,18},{24,7,-12},{-24,13,7}})a.block(w[0],w[1],w[2],AIR);
        a.box(-19,5,19,-15,5,19,DARK);a.box(25,12,5,25,12,9,DARK);
        // One roof scar is visible from the former upstairs room, without removing its floor.
        a.box(15,16,-15,18,30,-12,AIR);a.box(14,16,-15,14,16,-12,LOG);a.box(19,16,-15,19,16,-12,LOG);
        a.block(17,17,-16,Blocks.WEB.getDefaultState());
        for(int[] p:new int[][]{{-22,15,-18},{22,8,-18},{-22,8,16},{21,15,16}})a.block(p[0],p[1],p[2],Blocks.WEB.getDefaultState());
        ForestDetails.vinesZ(a,-23,-20,19,4,15,EnumFacing.NORTH);
        ForestDetails.vinesZ(a,20,23,-21,4,15,EnumFacing.SOUTH);
    }
    static void installSpirits(Chunk chunk) {
        GensokyoAtlas s=GensokyoAtlas.GHOST_HOUSE;
        if(Math.abs((chunk.x<<4)-s.x)>48 || Math.abs((chunk.z<<4)-s.z)>48)return;
        for(int i=0;i<SPIRITS.length;i++) {
            int[] p=SPIRITS[i];BlockPos at=new BlockPos(s.x+p[0],s.y+p[1],s.z+p[2]);
            if(at.getX()>>4!=chunk.x || at.getZ()>>4!=chunk.z)continue;
            EntityHouseSpirit spirit=new EntityHouseSpirit(chunk.getWorld());spirit.anchor(at,i*2.1);chunk.addEntity(spirit);
        }
    }
}
