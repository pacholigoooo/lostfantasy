package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Domestic, literary and animal-care uses, without inventing extra resident bedrooms. */
final class ChireidenRooms {
    enum Use { BOOKS, WRITING, BED, BATH, WARDROBE, LOUNGE, PETS, FEED, KITCHEN, DINING, TOOLS, LINEN }
    private ChireidenRooms() {}
    static void furnish(GensokyoArchitecture a,int x1,int x2,int z1,int z2,int f,Use use,String name) {
        int mx=(x1+x2)/2,mz=(z1+z2)/2;
        a.room(name,mx,f,mz);
        // Book rooms and living rooms keep an inset rug; service rooms use coloured stone tiles.
        boolean living=use==Use.BOOKS || use==Use.WRITING || use==Use.BED || use==Use.LOUNGE;
        if(living)InteriorFinishes.rug(a,x1+2,z1+3,x2-2,z2-3,f,use==Use.BED?10:14);
        else {
            for(int z=z1+2;z<z2;z+=4)a.box(x1+1,f,z,x2-1,f,z,Blocks.STONE.getStateFromMeta(6));
            for(int x=x1+2;x<x2;x+=4)a.box(x,f,z1+1,x,f,z2-1,Blocks.STONE.getStateFromMeta(6));
        }
        for(int x:new int[]{x1+4,x2-4})for(int z:new int[]{z1+7,z2-7})lamp(a,x,f+8,z);
        a.box(mx-3,f+1,mz-5,mx+3,f+1,mz+5,Blocks.CARPET.getStateFromMeta(use==Use.BED?10:use==Use.PETS?13:14));
        for(int z:new int[]{mz-4,mz,mz+4})a.block(mx,f,z,LIGHT);
        // High wall bands and shallow ceiling beams give the large rooms their own scale.
        for(int z:new int[]{z1,z2})a.box(x1,f+10,z,x2,f+10,z,DARK);
        for(int z:new int[]{z1+11,z2-11})a.box(x1,f+11,z,x2,f+11,z,DARK);
        for(int z:new int[]{z1+1,z2-1}) {
            a.box(x1+1,f+9,z,x2-1,f+9,z,Blocks.QUARTZ_BLOCK.getDefaultState());
            for(int x=x1+3;x<=x2-3;x+=5)a.block(x,f+8,z,Blocks.QUARTZ_STAIRS.getDefaultState()
                    .withProperty(BlockStairs.FACING,z==z1+1?EnumFacing.NORTH:EnumFacing.SOUTH)
                    .withProperty(BlockStairs.HALF,BlockStairs.EnumHalf.TOP));
        }
        if(use==Use.BOOKS || use==Use.WRITING) {
            for(int x:new int[]{x1,x2}) {
                a.box(x,f+1,z1+2,x,f+5,mz-4,Blocks.BOOKSHELF.getDefaultState());
                a.box(x,f+1,mz+4,x,f+5,z2-2,Blocks.BOOKSHELF.getDefaultState());
            }
            for(int z:new int[]{z1+4,z2-4}) {
                a.table(x1+4,f,z,5);a.block(x1+5,f+3,z,ModBlocks.RESEARCH_NOTES.getDefaultState());
                a.block(x2-4,f+1,z,ModBlocks.WRITING_DESK.getDefaultState());
                seats(a,x1+4,x1+8,f,z+2,EnumFacing.NORTH);
            }
            a.chest(x2-2,f,z1,"palace_books");a.chest(x2-2,f,z2,"palace_books");
            if(use==Use.WRITING) {
                a.block(x1+2,f+1,z1,Blocks.CRAFTING_TABLE.getDefaultState());a.box(x1+3,f+1,z1,x1+7,f+2,z1,WOOD);
                for(int z:new int[]{z1+10,z2-10}) {
                    a.table(x2-7,f,z,4);a.block(x2-6,f+3,z,ModBlocks.RESEARCH_NOTES.getDefaultState());
                    seats(a,x2-7,x2-4,f,z+2,EnumFacing.NORTH);
                }
            } else for(int z:new int[]{z1+8,z1+12,z2-12,z2-8}) {
                a.box(x1+3,f+1,z,x1+7,f+3,z,Blocks.BOOKSHELF.getDefaultState());
                a.box(x2-6,f+1,z,x2-3,f+3,z,Blocks.BOOKSHELF.getDefaultState());
            }
        } else if(use==Use.BED) {
            int bx=x2-5,bz=z1+8;
            a.bed(bx,f,bz);
            for(int x:new int[]{bx-2,bx+2})a.box(x,f+1,bz-3,x,f+5,bz+1,DARK);
            a.box(bx-2,f+6,bz-3,bx+2,f+6,bz+1,Blocks.WOOL.getStateFromMeta(name.contains("恋")?13:10));
            a.chest(x2-2,f,z1,"palace_household");VillageJoinery.cabinet(a,x1+3,f,z1+3,EnumFacing.SOUTH);
            a.box(x1,f+1,z1+2,x1,f+4,z1+8,Blocks.BOOKSHELF.getDefaultState());
            wardrobe(a,x1+2,f,z2-1,5);sitting(a,x2-7,f,z2-6);
            a.box(x1,f+1,mz+5,x1+7,f+4,mz+5,DARK);
            a.box(x1+1,f+1,mz+6,x1+6,f+3,mz+6,Blocks.BOOKSHELF.getDefaultState());
            a.box(bx-3,f+1,bz-4,bx+3,f+1,bz+2,Blocks.CARPET.getStateFromMeta(name.contains("恋")?13:10));
            // Keep bed halves and canopy feet above the rug.
            a.bed(bx,f,bz);
            for(int x:new int[]{bx-2,bx+2})for(int z:new int[]{bz-3,bz+1})a.block(x,f+1,z,DARK);
        } else if(use==Use.BATH) {
            // Raised, sealed tubs keep all bathing water inside the room footprint.
            for(int z:new int[]{z1+5,z2-7}) {
                a.box(x2-7,f+1,z,x2-1,f+1,z+5,STONE);
                a.box(x2-7,f+2,z,x2-1,f+2,z+5,STONE);
                a.box(x2-6,f+2,z+1,x2-2,f+2,z+4,Blocks.WATER.getDefaultState());
                seats(a,x2-6,x2-2,f,z-1,EnumFacing.SOUTH);
            }
            wardrobe(a,x1+1,f,z1+1,5);a.chest(x1+2,f,z2-2,"palace_household");
            for(int z=z1+11;z<=z2-9;z+=5)a.block(x1+2,f+1,z,ModBlocks.WASHSTAND.getDefaultState()
                    .withProperty(BlockHorizontal.FACING,EnumFacing.EAST));
        } else if(use==Use.WARDROBE || use==Use.LINEN) {
            for(int z:new int[]{z1+1,z2-1}) {wardrobe(a,x1+2,f,z,6);wardrobe(a,x2-7,f,z,6);}
            for(int z:new int[]{z1+8,z2-8})for(int x:new int[]{x1+1,x2-1})a.chest(x,f,z,"palace_household");
            a.table(x1+3,f,z2-6,6);a.block(x2-2,f+1,z1+7,Blocks.CRAFTING_TABLE.getDefaultState());
        } else if(use==Use.PETS) {
            for(int z:new int[]{z1+5,z2-5}) {
                for(int x=x1+2;x<=x1+7;x+=2)a.block(x,f+1,z,Blocks.CARPET.getStateFromMeta(1));
                a.box(x2-6,f+1,z-2,x2-6,f+4,z+2,LOG);
                a.box(x2-8,f+4,z-2,x2-4,f+4,z+2,WOOD);
                a.box(x2-6,f+7,z-1,x2-6,f+7,z+1,WOOD);
                a.box(x2-6,f+5,z,x2-6,f+6,z,LOG);
                a.block(x1+3,f+1,z-2,ModBlocks.LACQUER_BOWL.getDefaultState());
            }
            a.box(x1+1,f+1,z1+1,x1+7,f+1,z1+1,Blocks.HAY_BLOCK.getDefaultState());
            a.chest(x2-2,f,z1,"palace_pet_care");a.chest(x2-2,f,z2,"palace_pet_care");
            a.box(x1+2,f+1,z2-2,x1+7,f+1,z2-1,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        } else if(use==Use.FEED || use==Use.TOOLS) {
            for(int z:new int[]{z1+3,z1+9,z2-9,z2-3})for(int x:new int[]{x1+1,x2-1}) {
                a.chest(x,f,z,use==Use.FEED?"palace_pet_care":"palace_household");
                a.box(x-1,f+4,z-1,x+1,f+4,z+1,Blocks.WOODEN_SLAB.getDefaultState());
            }
            a.table(mx-3,f,z1+3,6);a.block(x1+4,f+1,z2-2,Blocks.CRAFTING_TABLE.getDefaultState());
            if(use==Use.TOOLS)a.block(x2-4,f+1,z2-2,Blocks.ANVIL.getDefaultState());
            else a.box(x1+4,f+1,z2-3,x2-4,f+2,z2-1,Blocks.HAY_BLOCK.getDefaultState());
        } else if(use==Use.KITCHEN) {
            for(int z=z1+2;z<=z2-2;z+=4) {
                a.block(x1,f+1,z,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.EAST));
                a.chest(x2-1,f,z,"old_hell_pantry");
            }
            a.table(mx-4,f,z1+6,8);a.table(mx-4,f,z2-6,8);
            a.block(x1+3,f+1,z1,Blocks.CRAFTING_TABLE.getDefaultState());a.block(x1+5,f+1,z1,Blocks.CAULDRON.getStateFromMeta(3));
            for(int x=x1+8;x<x2-2;x+=3)a.block(x,f+1,z1,ModBlocks.KITCHEN_SHELF.getDefaultState()
                    .withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        } else if(use==Use.DINING) {
            for(int z:new int[]{z1+7,z2-7}) {
                a.table(x1+4,f,z,x2-x1-7);
                seats(a,x1+4,x2-4,f,z-2,EnumFacing.SOUTH);seats(a,x1+4,x2-4,f,z+2,EnumFacing.NORTH);
                a.block(mx,f+3,z,ModBlocks.LACQUER_BOWL.getDefaultState());
            }
            a.chest(x2-1,f,z1,"old_hell_pantry");wardrobe(a,x1+1,f,z2,6);
        } else {
            sitting(a,x1+5,f,z1+7);sitting(a,x2-7,f,z2-7);
            a.box(x1,f+1,z1,x1+6,f+4,z1,Blocks.BOOKSHELF.getDefaultState());
            a.chest(x2-1,f,z1,"palace_household");
        }
    }
    private static void wardrobe(GensokyoArchitecture a,int x,int f,int z,int length) {
        a.box(x,f+1,z,x+length-1,f+4,z,DARK);
        for(int u=1;u<length;u+=2)a.block(x+u,f+2,z-1,Blocks.WOODEN_BUTTON.getDefaultState().withProperty(net.minecraft.block.BlockButton.FACING,EnumFacing.NORTH));
    }
    private static void seats(GensokyoArchitecture a,int x1,int x2,int f,int z,EnumFacing facing) {
        a.box(x1,f+1,z,x2,f+1,z,Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,facing));
    }
    private static void sitting(GensokyoArchitecture a,int x,int f,int z) {
        for(int dx:new int[]{0,3}) {
            a.block(x+dx,f+1,z,ModBlocks.TEA_TABLE.getDefaultState());
            a.block(x+dx,f+1,z-2,ModBlocks.UPHOLSTERED_CHAIR.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
            a.block(x+dx,f+1,z+2,ModBlocks.UPHOLSTERED_CHAIR.getDefaultState());
        }
    }
    static void lamp(GensokyoArchitecture a,int x,int y,int z) {
        a.box(x,y+1,z,x,y+3,z,Blocks.IRON_BARS.getDefaultState());a.block(x,y,z,ModBlocks.LIBRARY_LAMP.getDefaultState());
    }
}
