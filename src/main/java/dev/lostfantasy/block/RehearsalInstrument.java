package dev.lostfantasy.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Baked furniture with manual note playback; no ticking entity or client render state. */
public final class RehearsalInstrument extends LibraryDecoration {
    public enum Voice { STRINGS, BRASS, KEYS }
    private static final int[] SCALE={0,2,4,5,7,9,11,12};
    private final Voice voice;
    public RehearsalInstrument(Voice voice) {super(Kind.INSTRUMENT);this.voice=voice;}
    @Override public net.minecraft.util.math.AxisAlignedBB getBoundingBox(IBlockState state,net.minecraft.world.IBlockAccess world,BlockPos pos) {
        if(voice!=Voice.KEYS)return super.getBoundingBox(state,world,pos);
        return state.getValue(FACING).getAxis()==EnumFacing.Axis.X
                ?new net.minecraft.util.math.AxisAlignedBB(.15,.51,.02,.85,.84,.98)
                :new net.minecraft.util.math.AxisAlignedBB(.02,.51,.15,.98,.84,.85);
    }
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,
            EnumHand hand,EnumFacing side,float hitX,float hitY,float hitZ) {
        if(hand!=EnumHand.MAIN_HAND)return false;
        if(!world.isRemote)world.playSound(null,pos,sound(),SoundCategory.RECORDS,.65f,pitch(state.getValue(FACING),hitX,hitZ));
        return true;
    }
    private SoundEvent sound() {
        return voice==Voice.BRASS?SoundEvents.BLOCK_NOTE_FLUTE:voice==Voice.STRINGS?SoundEvents.BLOCK_NOTE_GUITAR:SoundEvents.BLOCK_NOTE_HARP;
    }
    static float pitch(EnumFacing facing,float hitX,float hitZ) {
        float key=facing==EnumFacing.NORTH?1-hitX:facing==EnumFacing.SOUTH?hitX:facing==EnumFacing.EAST?1-hitZ:hitZ;
        int index=Math.max(0,Math.min(7,(int)(key*8)));
        return (float)Math.pow(2,(SCALE[index]-12)/12.0);
    }
}
