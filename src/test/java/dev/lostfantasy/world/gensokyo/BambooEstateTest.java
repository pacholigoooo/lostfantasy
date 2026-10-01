package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class BambooEstateTest {
    private static final GensokyoAtlas[] SITES={GensokyoAtlas.EIENTEI,GensokyoAtlas.MOKOU};
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void clinicPrivateRoomsGardensAndShelterCanBeReachedOnFoot() {
        Generated g=new Generated();GensokyoBlueprint plan=GensokyoStructures.create();
        for(GensokyoAtlas site:SITES) {
            Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
            BlockPos start=new BlockPos(site.x,site.y+1,site.approachZ());queue.add(start);reached.add(start);
            while(!queue.isEmpty()) {
                BlockPos p=queue.removeFirst();
                for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                    BlockPos n=p.offset(side).up(dy);
                    if(!site.contains(n.getX(),n.getZ(),6) || n.getY()<site.y+1 || n.getY()>site.y+12 || reached.contains(n))continue;
                    if(!g.solid(n.down()) || g.solid(n) || g.solid(n.up()))continue;
                    if(dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                    reached.add(n);queue.add(n);break;
                }
            }
            int rooms=0;List<String> missing=new ArrayList<>();
            for(GensokyoBlueprint.Room room:plan.rooms())if(room.name.startsWith(site.title+"·")) {
                rooms++;if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x-site.x,room.y-site.y,room.z-site.z));
            }
            assertTrue(missing.toString(),missing.isEmpty());assertTrue(rooms>=(site==GensokyoAtlas.EIENTEI?24:4));
        }
    }
    @Test public void furnitureLightsBedsAndMedicineStorageRemainUsable() {
        Generated g=new Generated();int beds=0,chests=0,cabinets=0,trays=0;
        for(GensokyoAtlas site:SITES)for(int x=-site.rx;x<=site.rx;x++)for(int z=-site.rz;z<=site.rz;z++)for(int y=1;y<=9;y++) {
            BlockPos p=new BlockPos(site.x+x,site.y+y,site.z+z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.BED) {
                beds++;assertTrue("bed support "+p,g.at(p.down()).isFullCube());assertFalse(g.solid(p.up()));
                EnumFacing direction=s.getValue(BlockBed.FACING);
                assertSame(Blocks.BED,g.at(p.offset(s.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?direction.getOpposite():direction)).getBlock());
            } else if(s.getBlock()==Blocks.CHEST) {
                chests++;assertTrue("chest support "+p,g.at(p.down()).isFullCube());assertFalse("chest lid "+p,g.at(p.up()).isFullCube());
            } else if(s.getBlock()==ModBlocks.MEDICINE_TRAY || s.getBlock()==ModBlocks.WRITING_DESK) {
                if(s.getBlock()==ModBlocks.MEDICINE_TRAY)trays++;
                assertTrue("desk/tray support "+p,g.at(p.down()).isFullCube());assertFalse("desk/tray headroom "+p,g.at(p.up()).isFullCube());
            } else if(s.getBlock()==ModBlocks.PHARMACY_CABINET) {
                cabinets++;assertTrue("cabinet support "+p,g.at(p.down()).isFullCube());
            } else if(s.getBlock()==ModBlocks.RED_LANTERN)assertTrue("lantern support "+p,g.solid(p.up()));
            else if(s.getBlock()==Blocks.WALL_SIGN)assertTrue("sign support "+p,g.at(p.offset(s.getValue(BlockHorizontal.FACING).getOpposite())).isFullCube());
        }
        assertEquals("bed halves",14,beds);assertTrue("chests "+chests,chests>=18);
        assertTrue("medicine drawers "+cabinets,cabinets>=55);assertTrue("medicine trays "+trays,trays>=7);
    }
    @Test public void suppliesGenerateAndHealingPotionsRetainTheirEffect() {
        LootTableManager tables=new LootTableManager(null);boolean healing=false;
        for(String name:new String[]{"eientei_herbs","eientei_clinic","lunar_archive"}) {
            LootTable table=tables.getLootTableFromLocation(new ResourceLocation("lostfantasy","chests/"+name));
            assertNotSame(name,LootTable.EMPTY_LOOT_TABLE,table);
            for(int seed=0;seed<24;seed++) {
                List<ItemStack> stacks=table.generateLootForPools(new Random(seed),new LootContext(0,null,tables,null,null,null));
                assertFalse(name,stacks.isEmpty());
                for(ItemStack stack:stacks)if(stack.hasTagCompound() && "minecraft:healing".equals(stack.getTagCompound().getString("Potion")))healing=true;
            }
        }
        assertTrue("clinic never supplies a healing potion",healing);
    }
    private static final class Generated {
        final GensokyoGenerator generator=new GensokyoGenerator(null,12345);final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        IBlockState at(BlockPos p) {
            int cx=p.getX()>>4,cz=p.getZ()>>4;return chunks.computeIfAbsent(GensokyoAtlas.key(cx,cz),key->generator.primer(cx,cz)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);
        }
        boolean solid(BlockPos p) {
            IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET && s.getBlock()!=Blocks.WALL_SIGN;
        }
    }
}
