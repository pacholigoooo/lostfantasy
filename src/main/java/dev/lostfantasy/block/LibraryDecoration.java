package dev.lostfantasy.block;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Static furniture: baked block models, without ticking or tile entities. */
public class LibraryDecoration extends BlockHorizontal {
    public enum Kind {
        CATALOG(FULL_BLOCK_AABB, true),
        STORAGE_CHEST(new AxisAlignedBB(.0625,0,.0625,.9375,.875,.9375),false),
        LAMP(new AxisAlignedBB(.15, 0, .25, .85, .875, .8), false),
        HANGING_LAMP(new AxisAlignedBB(.1875,.125,.1875,.8125,1,.8125),false),
        SAISEN_BOX(new AxisAlignedBB(.03125,0,.0625,.96875,.78125,.9375),false),
        TEA_TABLE(new AxisAlignedBB(0,0,.0625,1,.625,.9375),false),
        FLOOR_CUSHION(new AxisAlignedBB(.08,0,.08,.92,.15,.92),false),
        DRAWER_CABINET(new AxisAlignedBB(.03125,0,.0625,.96875,.9375,.9375),false),
        KITCHEN_SHELF(new AxisAlignedBB(.0625,0,.125,.9375,1,.875),false),
        WASHSTAND(new AxisAlignedBB(.0625,0,.05,.9375,.78125,.9375),false),
        UPHOLSTERED_CHAIR(new AxisAlignedBB(.0625,0,.125,.9375,.96875,.90625),false),
        NOTES(new AxisAlignedBB(.0625, 0, .125, .9375, .1875, .875), false),
        ARMILLARY(new AxisAlignedBB(.125, 0, .125, .875, 1, .875), false),
        DOLL(new AxisAlignedBB(.25, 0, .25, .75, 1, .75), false),
        RED_LANTERN(new AxisAlignedBB(.1875, 0, .1875, .8125, 1, .8125), false),
        TELEVISION(new AxisAlignedBB(.0625, 0, .125, .9375, .875, .875), false),
        WRITING_DESK(new AxisAlignedBB(0, 0, .0625, 1, .5, .9375), false),
        GRAMOPHONE(new AxisAlignedBB(.125, 0, .125, .875, 1, .875), false),
        JIZO(new AxisAlignedBB(.1875, 0, .1875, .8125, 1, .8125), false,Material.ROCK),
        MEDICINE_TRAY(new AxisAlignedBB(.0625,0,.125,.9375,.5,.875),false),
        SHRINE_ROPE(new AxisAlignedBB(0,.25,.25,1,.75,.75),false),
        SHIDE(new AxisAlignedBB(.3125,0,.4375,.6875,1.25,.5625),false),
        PRESS(new AxisAlignedBB(.0625,0,.0625,.9375,1,.9375),false,Material.IRON),
        CAMERA(new AxisAlignedBB(.125,0,.125,.875,1,.875),false,Material.IRON),
        GAMING_TABLE(new AxisAlignedBB(0,0,0,1,.75,1),false),
        INSTRUMENT(new AxisAlignedBB(.0625,0,.125,.9375,1,.875),false),
        LACQUER_BOWL(new AxisAlignedBB(.125,0,.125,.875,1,.875),false),
        DRAGON_PIPE(new AxisAlignedBB(.0625,.125,.3125,.9375,1,.6875),false,Material.IRON);

        final AxisAlignedBB bounds;
        final boolean fullCube;
        final Material material;

        Kind(AxisAlignedBB bounds, boolean fullCube) {
            this(bounds,fullCube,Material.WOOD);
        }
        Kind(AxisAlignedBB bounds,boolean fullCube,Material material) {
            this.bounds = bounds;
            this.fullCube = fullCube;
            this.material=material;
        }
    }

    private final Kind kind;

    public LibraryDecoration(Kind kind) {
        super(kind.material);
        this.kind = kind;
        fullBlock = kind.fullCube;
        setHardness(1.5f);
        setResistance(4);
        setSoundType(kind.material==Material.ROCK?SoundType.STONE:kind.material==Material.IRON?SoundType.METAL:SoundType.WOOD);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        setLightOpacity(kind.fullCube ? 255 : 0);
        if (kind == Kind.LAMP || kind == Kind.RED_LANTERN || kind == Kind.HANGING_LAMP) setLightLevel(1);
    }

    @Override protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
    }

    @Override public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return withRotation(state, mirror.toRotation(state.getValue(FACING)));
    }

    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side,
                                                     float hitX, float hitY, float hitZ, int meta,
                                                     EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override public boolean isFullCube(IBlockState state) {
        return kind != null && kind.fullCube;
    }

    @Override public boolean isOpaqueCube(IBlockState state) {
        return isFullCube(state);
    }

    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        AxisAlignedBB bounds = kind.bounds;
        return state.getValue(FACING).getAxis() == EnumFacing.Axis.X
                ? new AxisAlignedBB(bounds.minZ, bounds.minY, bounds.minX, bounds.maxZ, bounds.maxY, bounds.maxX)
                : bounds;
    }
}
