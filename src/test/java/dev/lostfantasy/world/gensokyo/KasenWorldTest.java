package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import java.util.function.BiFunction;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class KasenWorldTest {
    private static final GensokyoAtlas SITE=GensokyoAtlas.KASEN;
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void occupiedStoreysGardenAndExitAreConnected() {
        Generated g=new Generated(new KasenGenerator(null,12345)::primer);Set<BlockPos> reached=walk(g,local(0,1,47));
        List<String> missing=new ArrayList<>();GensokyoBlueprint plan=KasenHouse.create();assertEquals(16,plan.rooms().size());
        for(GensokyoBlueprint.Room room:plan.rooms())if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x-SITE.x,room.y-SITE.y,room.z-SITE.z));
        assertTrue(missing.toString(),missing.isEmpty());assertTrue(reached.contains(local(0,1,61)));
        for(int floor:new int[]{3,11,19})for(int x:new int[]{-1,1})assertTrue(g.at(local(x,floor,1)).isFullCube());
        // Lower eaves must not climb in front of the next storey's round windows.
        for(int[] window:new int[][]{{9,11,10,17},{6,19,7,14}})for(int z=window[2]+1;z<=window[3];z++)
            for(int y=window[1]+2;y<=window[1]+6;y++)assertSame(Blocks.AIR,g.at(local(window[0],y,z)).getBlock());
    }
    @Test public void bedsChestsAndLampsHaveSpaceAndSupport() {
        Generated g=new Generated(new KasenGenerator(null,12345)::primer);Set<BlockPos> reached=walk(g,local(0,1,47));
        int beds=0,chests=0,lamps=0;
        for(int x=-34;x<=34;x++)for(int z=-20;z<=14;z++)for(int y=1;y<=32;y++) {
            BlockPos p=local(x,y,z);IBlockState s=g.at(p);
            if(s.getBlock()==Blocks.BED || s.getBlock()==Blocks.CHEST) {
                assertTrue("support "+p,g.at(p.down()).isFullCube());assertFalse("headroom "+p,g.solid(p.up()));
                boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("access "+p,access);
                if(s.getBlock()==Blocks.CHEST)chests++;
                else {beds++;EnumFacing face=s.getValue(BlockBed.FACING);IBlockState pair=g.at(p.offset(s.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?face.getOpposite():face));assertSame(Blocks.BED,pair.getBlock());assertNotEquals(s.getValue(BlockBed.PART),pair.getValue(BlockBed.PART));}
            } else if(s.getBlock()==ModBlocks.RED_LANTERN) {lamps++;assertTrue("suspended lamp "+p,g.at(p.up()).isFullCube());}
        }
        assertEquals(4,beds);assertEquals(12,chests);assertEquals(14,lamps);
    }
    @Test public void mountainApproachIsWalkableAndEveryRouteStepLeadsToTheEntrance() {
        Generated g=new Generated(new GensokyoGenerator(null,12345)::primer);Set<BlockPos> reached=walk(g,local(0,1,67));
        NBTTagCompound progress=new NBTTagCompound();long tick=0;int entries=0;
        assertFalse(HermitPath.step(progress,0,1,62,tick++,true,true));
        for(int i=1;i<HermitPath.POINTS.length;i++) {
            int[] a=HermitPath.POINTS[i-1],b=HermitPath.POINTS[i];int n=(int)Math.ceil(Math.hypot(b[0]-a[0],b[1]-a[1])*3);
            for(int j=1;j<=n;j++) {
                double x=a[0]+(b[0]-a[0])*j/(double)n,z=a[1]+(b[1]-a[1])*j/(double)n;
                assertTrue("path "+x+","+z,reached.contains(local((int)Math.round(x),1,(int)Math.round(z))));
                if(HermitPath.step(progress,x,1,z,tick++,true,true)) {entries++;break;}
            }
        }
        assertEquals(1,entries);assertEquals(HermitPath.POINTS.length,progress.getInteger("next"));
        assertSame(Blocks.LOG,g.at(local(5,14,13)).getBlock());
        assertSame(Blocks.AIR,g.at(local(0,4,9)).getBlock());
    }
    @Test public void shortcutsFlyingMissingTicksAndWrongHeightResetTheRoute() {
        for(int kind=0;kind<5;kind++) {
            NBTTagCompound state=new NBTTagCompound();HermitPath.step(state,0,1,62,10,true,true);assertEquals(1,state.getInteger("next"));
            switch(kind) {
                case 0:assertFalse(HermitPath.step(state,-5,1,-23,11,true,true));break;
                case 1:assertFalse(HermitPath.step(state,0,1,62,11,false,true));break;
                case 2:assertFalse(HermitPath.step(state,-5,1,59,40,true,true));break;
                case 3:assertFalse(HermitPath.step(state,0,8,62,11,true,true));break;
                default:assertFalse(HermitPath.step(state,5,1,60,11,true,true));
            }
            assertFalse("stale progress "+kind,state.hasKey("next"));
        }
        NBTTagCompound airborne=new NBTTagCompound();assertFalse(HermitPath.step(airborne,0,1,62,1,true,false));assertFalse(airborne.hasKey("next"));
        NBTTagCompound first=new NBTTagCompound(),second=new NBTTagCompound();HermitPath.step(first,0,1,62,1,true,true);
        assertFalse(HermitPath.step(second,-20,1,50,1,true,true));assertFalse(second.hasKey("next"));assertEquals(1,first.getInteger("next"));
    }
    @Test public void terrainStaysDeterministicAcrossSlicesAndSeeds() {
        for(long seed:new long[]{0,19,Long.MIN_VALUE}) {
            KasenGenerator g=new KasenGenerator(null,seed);
            for(int[] c:new int[][]{{0,0},{-4,-2},{7,8},{-40,10}}) {
                int cx=(SITE.x>>4)+c[0],cz=(SITE.z>>4)+c[1];ChunkPrimer a=g.primer(cx,cz);g.primer(cx+1,cz-1);ChunkPrimer b=g.primer(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<256;y++)assertEquals(a.getBlockState(x,y,z),b.getBlockState(x,y,z));
            }
            for(int[] p:new int[][]{{0,47},{0,61},{-34,-23},{40,21}})assertEquals(SITE.y,g.ground(SITE.x+p[0],SITE.z+p[1]));
        }
    }
    @Test public void riverHasClosedBanksAndGardenBambooHasGroundSupport() {
        for(long seed:new long[]{0,19,12345}) {
            Generated g=new Generated(new KasenGenerator(null,seed)::primer);int water=0;
            for(int x=-175;x<=-95;x++)for(int z=-80;z<=80;z+=2) {
                BlockPos p=local(x,132-SITE.y,z);if(g.at(p).getMaterial()!=Material.WATER)continue;
                water++;assertTrue(g.at(p.down(5)).isFullCube());
                for(EnumFacing f:EnumFacing.HORIZONTALS)assertTrue("open river bank "+p,g.at(p.offset(f)).getMaterial()==Material.WATER || g.at(p.offset(f)).isFullCube());
            }
            assertTrue(water>800);
        }
        Generated g=new Generated(new KasenGenerator(null,12345)::primer);int bamboo=0;
        for(int x=-20;x<=20;x++)for(int z=-35;z<=-27;z++)if(g.at(local(x,1,z)).getBlock()==ModBlocks.BAMBOO_STEM) {
            bamboo++;assertTrue(g.at(local(x,0,z)).isFullCube());
        }
        assertEquals(20,bamboo);
    }
    @Test public void booksSuppliesAndDirectionsUseSavedOrdinaryContainers() {
        String[] names={"kasen_books","kasen_supplies","hermit_path"};int[][] positions={{-5,4,-16},{16,4,-16},{11,2,57}};
        LootTableManager manager=new LootTableManager(null);
        for(int i=0;i<names.length;i++) {
            BlockPos p=local(positions[i][0],positions[i][1],positions[i][2]);Chunk c=i<2?
                    new KasenGenerator(new TestWorld(),12345).generateChunk(p.getX()>>4,p.getZ()>>4):new GensokyoGenerator(new TestWorld(),12345).generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntityChest chest=(TileEntityChest)c.getTileEntityMap().get(p);assertNotNull(chest);
            NBTTagCompound nbt=chest.writeToNBT(new NBTTagCompound());ResourceLocation id=new ResourceLocation("lostfantasy","chests/"+names[i]);assertEquals(id.toString(),nbt.getString("LootTable"));
            TileEntityChest restored=new TileEntityChest();restored.readFromNBT(nbt);assertEquals(nbt,restored.writeToNBT(new NBTTagCompound()));
            LootTable loot=manager.getLootTableFromLocation(id);assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);
            List<ItemStack> items=loot.generateLootForPools(new Random(1),new LootContext(0,null,manager,null,null,null));assertFalse(items.isEmpty());
            if(i==2) {ItemStack book=items.get(0);assertSame(Items.WRITTEN_BOOK,book.getItem());assertEquals("山中手记",book.getTagCompound().getString("title"));
                ITextComponent text=ITextComponent.Serializer.jsonToComponent(book.getTagCompound().getTagList("pages",8).getStringTagAt(0));assertTrue(text.getUnformattedText().contains("大榉树"));}
        }
    }
    private static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    private static Set<BlockPos> walk(Generated g,BlockPos start) {
        Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();assertTrue("start "+start,g.walkable(start));reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);
                if(n.getX()<SITE.x-46 || n.getX()>SITE.x+46 || n.getZ()<SITE.z-36 || n.getZ()>SITE.z+68 || n.getY()<SITE.y || n.getY()>SITE.y+33 || reached.contains(n))continue;
                if(!g.walkable(n) || dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        return reached;
    }
    private static final class Generated {
        private final BiFunction<Integer,Integer,ChunkPrimer> generator;private final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(BiFunction<Integer,Integer,ChunkPrimer> generator) {this.generator=generator;}
        IBlockState at(BlockPos p) {return chunks.computeIfAbsent(GensokyoAtlas.key(p.getX()>>4,p.getZ()>>4),k->generator.apply(p.getX()>>4,p.getZ()>>4)).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER;}
    }
}
