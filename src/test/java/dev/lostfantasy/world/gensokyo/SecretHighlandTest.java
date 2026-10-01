package dev.lostfantasy.world.gensokyo;

import com.mojang.authlib.GameProfile;
import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import java.util.*;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.storage.loot.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class SecretHighlandTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void cliffHomesWorkroomsAndDenHaveWalkableRoutes() {
        int[] counts={45,17};int index=0;
        for(GensokyoAtlas site:new GensokyoAtlas[]{GensokyoAtlas.SECRET_CLIFF,GensokyoAtlas.FALSE_HEAVEN}) {
            Generated g=new Generated(site,12345);Set<BlockPos> reached=walk(g);List<String> missing=new ArrayList<>();int count=0;
            for(GensokyoBlueprint.Room room:GensokyoStructures.create().rooms())if(room.name.startsWith(site.title+"·")) {
                count++;if(!reached.contains(new BlockPos(room.x,room.y,room.z)))missing.add(room.name+" "+new BlockPos(room.x-site.x,room.y-site.y,room.z-site.z));
            }
            assertEquals(counts[index++],count);assertTrue(missing.toString(),missing.isEmpty());
        }
    }
    @Test public void bedsStorageDiceAndHangingFixturesStayUsable() {
        for(GensokyoAtlas site:new GensokyoAtlas[]{GensokyoAtlas.SECRET_CLIFF,GensokyoAtlas.FALSE_HEAVEN}) {
            Generated g=new Generated(site,12345);Set<BlockPos> reached=walk(g);int beds=0,chests=0,lanterns=0,tables=0,pipe=0;
            for(int x=-site.rx;x<=site.rx;x++)for(int z=-site.rz;z<=site.rz;z++)for(int y=1;y<=22;y++) {
                BlockPos p=g.local(x,y,z);IBlockState s=g.at(p);
                if(s.getBlock()==Blocks.BED || s.getBlock()==Blocks.CHEST) {
                    assertTrue("support "+p,g.at(p.down()).isFullCube());assertFalse("lid/head "+p,g.solid(p.up()));
                    boolean access=false;for(EnumFacing f:EnumFacing.HORIZONTALS)access|=reached.contains(p.offset(f));assertTrue("access "+p,access);
                    if(s.getBlock()==Blocks.CHEST)chests++;
                    else {beds++;EnumFacing face=s.getValue(BlockBed.FACING);IBlockState pair=g.at(p.offset(s.getValue(BlockBed.PART)==BlockBed.EnumPartType.HEAD?face.getOpposite():face));assertSame(Blocks.BED,pair.getBlock());assertNotEquals(s.getValue(BlockBed.PART),pair.getValue(BlockBed.PART));}
                }else if(s.getBlock()==ModBlocks.RED_LANTERN || s.getBlock()==ModBlocks.DRAGON_PIPE) {
                    assertTrue("hanging fixture "+p,g.solid(p.up()));if(s.getBlock()==ModBlocks.RED_LANTERN)lanterns++;else pipe++;
                }else if(s.getBlock()==ModBlocks.MOUNTAIN_GAMING_TABLE) {
                    tables++;assertTrue(g.at(p.down()).isFullCube());boolean access=false;
                    for(EnumFacing f:EnumFacing.HORIZONTALS)for(int d=1;d<=2;d++)access|=reached.contains(p.offset(f,d));
                    assertTrue("table access "+p,access);assertFalse(g.solid(p.up()));
                }
            }
            boolean den=site==GensokyoAtlas.FALSE_HEAVEN;
            assertEquals(den?4:24,beds);assertEquals(den?7:39,chests);assertEquals(den?9:28,lanterns);assertEquals(den?5:0,tables);assertEquals(den?1:0,pipe);
        }
    }
    @Test public void dryEscarpmentAndOpenMeadowsHaveHeightAndSupportedFlowers() {
        GensokyoAtlas cliff=GensokyoAtlas.SECRET_CLIFF,shelf=GensokyoAtlas.FALSE_HEAVEN;
        for(long seed:new long[]{0,17,12345,Long.MIN_VALUE}) {
            GensokyoTerrain terrain=new GensokyoTerrain(seed);
            GensokyoTerrain.Column lower=terrain.column(cliff.x+15,cliff.z),upper=terrain.column(cliff.x+15,cliff.z-96);
            assertTrue("cliff height "+seed,upper.ground-lower.ground>=42);assertFalse(lower.wet());assertFalse(upper.wet());
            assertEquals(GensokyoTerrain.Region.FOREST,terrain.region(cliff.x+100,cliff.z+45));
        }
        Generated g=new Generated(shelf,12345);GensokyoTerrain terrain=new GensokyoTerrain(12345);int flowers=0,snow=0,trees=0;
        for(int x=-20;x<=85;x++)for(int z=-90;z<=-48;z++) {
            int wx=shelf.x+x,wz=shelf.z+z;GensokyoTerrain.Column c=terrain.column(wx,wz);
            BlockPos p=new BlockPos(wx,c.ground+1,wz);IBlockState s=g.at(p);
            if(s.getBlock()==ModBlocks.KOMAKUSA) {flowers++;assertSame(Blocks.GRASS,g.at(p.down()).getBlock());}
            if(s.getBlock()==Blocks.SNOW_LAYER) {snow++;assertTrue(g.at(p.down()).isFullCube());}
            if(s.getBlock()==Blocks.LOG)trees++;
        }
        assertTrue("flowers "+flowers,flowers>30);assertTrue("snow "+snow,snow>100);assertEquals(0,trees);
    }
    @Test public void diceRollIsServerSideMainHandAndDoesNotSpendItems() {
        TestWorld world=new TestWorld();DicePlayer player=new DicePlayer(world);player.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(Items.PAPER,3));
        IBlockState state=ModBlocks.MOUNTAIN_GAMING_TABLE.getDefaultState();Set<Integer> seen=new HashSet<>();
        for(int i=0;i<32;i++) {
            assertTrue(state.getBlock().onBlockActivated(world,BlockPos.ORIGIN,state,player,EnumHand.MAIN_HAND,EnumFacing.UP,.5f,.5f,.5f));
            TextComponentTranslation text=(TextComponentTranslation)player.message;assertEquals("message.lostfantasy.dice_roll",text.getKey());
            assertEquals(3,text.getFormatArgs().length);for(Object value:text.getFormatArgs()) {int n=(Integer)value;assertTrue(n>=1 && n<=6);seen.add(n);}
            assertEquals(3,player.getHeldItemMainhand().getCount());
        }
        assertEquals(6,seen.size());assertEquals(32,player.messages);
        assertFalse(state.getBlock().onBlockActivated(world,BlockPos.ORIGIN,state,player,EnumHand.OFF_HAND,EnumFacing.UP,.5f,.5f,.5f));assertEquals(32,player.messages);
        assertFalse(state.getBlock().hasTileEntity(state));assertEquals(0,state.getLightValue());
        for(int i=0;i<4;i++) {IBlockState s=state.getBlock().getStateFromMeta(i);assertEquals(i,state.getBlock().getMetaFromState(s));assertEquals(s,s.withRotation(Rotation.CLOCKWISE_90).withRotation(Rotation.COUNTERCLOCKWISE_90));}
    }
    @Test public void generatedSlicesAndThreeSupplyTablesSurviveSaving() {
        for(GensokyoAtlas site:new GensokyoAtlas[]{GensokyoAtlas.SECRET_CLIFF,GensokyoAtlas.FALSE_HEAVEN}) {
            Generated one=new Generated(site,19),two=new Generated(site,19);
            for(int[] p:new int[][]{{-24,-22},{0,-18},{24,0},{-65,19},{42,25}}) {
                int cx=(site.x+p[0])>>4,cz=(site.z+p[1])>>4;ChunkPrimer expected=one.chunk(cx,cz);two.chunk(cx+1,cz-1);ChunkPrimer actual=two.chunk(cx,cz);
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=site.y-3;y<site.y+24;y++)assertEquals(expected.getBlockState(x,y,z),actual.getBlockState(x,y,z));
            }
        }
        GensokyoAtlas[] sites={GensokyoAtlas.SECRET_CLIFF,GensokyoAtlas.SECRET_CLIFF,GensokyoAtlas.FALSE_HEAVEN};
        int[][] coords={{-26,2,-24},{47,2,-26},{13,2,-10}};String[] names={"yamawaro_trade","yamawaro_tools","komakusa_supplies"};
        LootTableManager manager=new LootTableManager(null);
        for(int i=0;i<sites.length;i++) {
            BlockPos p=new BlockPos(sites[i].x+coords[i][0],sites[i].y+coords[i][1],sites[i].z+coords[i][2]);
            Chunk chunk=new GensokyoGenerator(new TestWorld(),12345).generateChunk(p.getX()>>4,p.getZ()>>4);
            TileEntityChest chest=(TileEntityChest)chunk.getTileEntityMap().get(p);assertNotNull(chest);NBTTagCompound nbt=chest.writeToNBT(new NBTTagCompound());
            ResourceLocation key=new ResourceLocation("lostfantasy","chests/"+names[i]);assertEquals(key.toString(),nbt.getString("LootTable"));
            TileEntityChest restored=new TileEntityChest();restored.readFromNBT(nbt);assertEquals(nbt,restored.writeToNBT(new NBTTagCompound()));
            LootTable loot=manager.getLootTableFromLocation(key);assertNotSame(LootTable.EMPTY_LOOT_TABLE,loot);
            assertFalse(loot.generateLootForPools(new Random(16),new LootContext(0,null,manager,null,null,null)).isEmpty());
        }
    }
    private static Set<BlockPos> walk(Generated g) {
        GensokyoAtlas s=g.site;Set<BlockPos> reached=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();BlockPos start=g.local(0,1,s.rz+5);assertTrue(g.walkable(start));reached.add(start);queue.add(start);
        while(!queue.isEmpty()) {
            BlockPos p=queue.removeFirst();
            for(EnumFacing side:EnumFacing.HORIZONTALS)for(int dy:new int[]{0,1,-1}) {
                BlockPos n=p.offset(side).up(dy);
                if(n.getX()<s.x-s.rx-3 || n.getX()>s.x+s.rx+3 || n.getZ()<s.z-s.rz-3 || n.getZ()>s.z+s.rz+6 || n.getY()<s.y || n.getY()>s.y+22 || reached.contains(n))continue;
                if(!g.walkable(n) || dy>0 && g.solid(p.up(2)) || dy<0 && g.solid(n.up(2)))continue;
                reached.add(n);queue.add(n);break;
            }
        }
        return reached;
    }
    private static final class Generated {
        final GensokyoAtlas site;final GensokyoGenerator generator;final Map<Long,ChunkPrimer> chunks=new HashMap<>();
        Generated(GensokyoAtlas site,long seed) {this.site=site;generator=new GensokyoGenerator(null,seed);}
        BlockPos local(int x,int y,int z) {return new BlockPos(site.x+x,site.y+y,site.z+z);}
        ChunkPrimer chunk(int x,int z) {return chunks.computeIfAbsent(GensokyoAtlas.key(x,z),k->generator.primer(x,z));}
        IBlockState at(BlockPos p) {return chunk(p.getX()>>4,p.getZ()>>4).getBlockState(p.getX()&15,p.getY(),p.getZ()&15);}
        boolean solid(BlockPos p) {IBlockState s=at(p);return s.getMaterial().blocksMovement() && s.getBlock()!=Blocks.CARPET;}
        boolean walkable(BlockPos p) {return solid(p.down()) && !solid(p) && !solid(p.up()) && at(p).getMaterial()!=Material.WATER;}
    }
    private static final class DicePlayer extends EntityPlayer {
        int messages;ITextComponent message;
        DicePlayer(TestWorld world) {super(world,new GameProfile(new UUID(0,7),"DiceTester"));}
        @Override public boolean isSpectator() {return false;}
        @Override public boolean isCreative() {return false;}
        @Override public void sendStatusMessage(ITextComponent text,boolean actionBar) {messages++;message=text;}
    }
}
