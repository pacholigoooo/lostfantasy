package dev.lostfantasy.block;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.TestWorld;
import dev.lostfantasy.world.gensokyo.GensokyoTestBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class RehearsalInstrumentTest {
    @BeforeClass public static void bootstrap() {GensokyoTestBlocks.register();}
    @Test public void oneMainHandClickPlaysOneLocalNoteWithoutInventoryOrTicks() {
        RecordingWorld world=new RecordingWorld();BlockPos pos=new BlockPos(4,80,5);
        Block[] blocks={ModBlocks.VIOLIN_STAND,ModBlocks.TRUMPET_STAND,ModBlocks.REHEARSAL_KEYBOARD};
        SoundEvent[] sounds={SoundEvents.BLOCK_NOTE_GUITAR,SoundEvents.BLOCK_NOTE_FLUTE,SoundEvents.BLOCK_NOTE_HARP};
        for(int i=0;i<blocks.length;i++) {
            IBlockState s=blocks[i].getDefaultState();world.count=0;
            assertFalse(blocks[i].onBlockActivated(world,pos,s,null,EnumHand.OFF_HAND,EnumFacing.UP,.5f,1,.5f));assertEquals(0,world.count);
            assertTrue(blocks[i].onBlockActivated(world,pos,s,null,EnumHand.MAIN_HAND,EnumFacing.UP,.5f,1,.5f));
            assertEquals(1,world.count);assertEquals(pos,world.position);assertSame(sounds[i],world.sound);assertSame(SoundCategory.RECORDS,world.category);
            assertTrue(world.pitch>=.5f && world.pitch<=1);assertEquals(.65f,world.volume,0);assertTrue(world.blocks.isEmpty());
            assertFalse(blocks[i].hasTileEntity(s));assertFalse(blocks[i].getTickRandomly());
        }
    }
    @Test public void notesTrackTheInstrumentKeysInEveryFacing() {
        for(int i=0;i<8;i++) {
            float x=(i+.5f)/8,p=RehearsalInstrument.pitch(EnumFacing.NORTH,1-x,.5f);
            assertEquals(p,RehearsalInstrument.pitch(EnumFacing.SOUTH,x,.5f),0);
            assertEquals(p,RehearsalInstrument.pitch(EnumFacing.EAST,.5f,1-x),0);
            assertEquals(p,RehearsalInstrument.pitch(EnumFacing.WEST,.5f,x),0);
            if(i>0)assertTrue(p>RehearsalInstrument.pitch(EnumFacing.NORTH,1-(i-.5f)/8,.5f));
        }
        for(Block block:new Block[]{ModBlocks.VIOLIN_STAND,ModBlocks.TRUMPET_STAND,ModBlocks.REHEARSAL_KEYBOARD})for(EnumFacing f:EnumFacing.HORIZONTALS) {
            IBlockState s=block.getDefaultState().withProperty(BlockHorizontal.FACING,f);
            assertEquals(s,block.getStateFromMeta(block.getMetaFromState(s)));
            assertEquals(f.rotateY(),s.withRotation(Rotation.CLOCKWISE_90).getValue(BlockHorizontal.FACING));
        }
    }
    private static final class RecordingWorld extends TestWorld {
        int count;BlockPos position;SoundEvent sound;SoundCategory category;float pitch,volume;
        @Override public void playSound(EntityPlayer player,BlockPos pos,SoundEvent event,SoundCategory group,float loudness,float frequency) {
            count++;position=pos;sound=event;category=group;volume=loudness;pitch=frequency;
        }
    }
}
