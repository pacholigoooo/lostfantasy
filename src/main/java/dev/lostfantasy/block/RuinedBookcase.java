package dev.lostfantasy.block;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Random;

/** Decorative archive shelving which cannot turn a ruin into a book farm. */
public final class RuinedBookcase extends BlockHorizontal {
    public static final PropertyEnum<Damage> DAMAGE = PropertyEnum.create("damage", Damage.class);

    public RuinedBookcase() {
        super(Material.WOOD);
        setHardness(1.5f);
        setResistance(4);
        setSoundType(SoundType.WOOD);
        setDefaultState(blockState.getBaseState().withProperty(FACING, net.minecraft.util.EnumFacing.NORTH)
                .withProperty(DAMAGE, Damage.SPARSE));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, DAMAGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        Damage damage = Damage.values()[(meta >> 2) % Damage.values().length];
        return getDefaultState().withProperty(FACING, net.minecraft.util.EnumFacing.byHorizontalIndex(meta & 3))
                .withProperty(DAMAGE, damage);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | state.getValue(DAMAGE).ordinal() << 2;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX,
                                             float hitY, float hitZ, int meta,
                                             EntityLivingBase placer, EnumHand hand) {
        Damage damage = Damage.values()[(meta >> 2) % Damage.values().length];
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite())
                .withProperty(DAMAGE, damage);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        // The intact cabinet sides cover the whole face; the open front does not.
        Damage damage = state.getValue(DAMAGE);
        EnumFacing front = state.getValue(FACING);
        return damage != Damage.COLLAPSED && face != front
                && (damage != Damage.DOUBLE_SIDED || face != front.getOpposite());
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Items.PAPER;
    }

    @Override
    public int quantityDropped(Random random) {
        return random.nextInt(8) == 0 ? 1 : 0;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab != CreativeTabs.SEARCH && tab != ModBlocks.TAB) return;
        for (Damage damage : Damage.values()) {
            IBlockState state = getDefaultState().withProperty(DAMAGE, damage);
            items.add(new ItemStack(this, 1, getMetaFromState(state)));
        }
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, net.minecraft.util.EnumFacing face) {
        return 0;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, net.minecraft.util.EnumFacing face) {
        return 0;
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    public enum Damage implements IStringSerializable {
        SPARSE("sparse"), EMPTY("empty"), COLLAPSED("collapsed"), DOUBLE_SIDED("double_sided");

        private final String name;

        Damage(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
