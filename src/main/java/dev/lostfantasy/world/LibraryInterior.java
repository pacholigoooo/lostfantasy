package dev.lostfantasy.world;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.block.RuinedBookcase;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

/** Block layout measured from the C13 scene; X/Y in Blender become X/Z here. */
final class LibraryInterior {
    private static final int HALF_X = 50;
    private static final int HALF_Z = 59;
    private static final int WIDTH = HALF_X * 2 + 1;
    private static final IBlockState WOOD = Blocks.PLANKS.getStateFromMeta(5);
    private static final IBlockState POST = Blocks.LOG2.getStateFromMeta(1);
    private static final IBlockState SLAB = Blocks.WOODEN_SLAB.getStateFromMeta(5);
    private static final IBlockState FENCE = Blocks.DARK_OAK_FENCE.getDefaultState();
    private static final IBlockState PURPLE = Blocks.WOOL.getStateFromMeta(10);

    // Empty columns stay null. The plan is built once, not once per ruin or chunk.
    private final IBlockState[][] columns = new IBlockState[WIDTH * (HALF_Z * 2 + 1)][];

    private static final class Holder {
        static final LibraryInterior PLAN = new LibraryInterior();
    }

    static IBlockState[] column(int x, int z) {
        if (Math.abs(x) > HALF_X || Math.abs(z) > HALF_Z) return null;
        return Holder.PLAN.columns[index(x, z)];
    }

    private LibraryInterior() {
        bookcases();
        wallTrim();
        centralStudy();
        studyCorners();
        readingBays();
        ceiling();
        fallenMasonry();
        passageLights();
    }

    private void bookcases() {
        for (int segment = 0; segment < 12; segment++) {
            double angle = Math.toRadians(15 + segment * 30);
            shelfBay(24 * Math.cos(angle), 24 * Math.sin(angle), angle, 12);
        }
        for (int side : new int[]{-1, 1}) {
            for (int row = 0; row < 8; row++) {
                int z = -44 + row * 13;
                shelfBay(side * 32.1, z, Math.PI / 2, 18);
                // The source omits this bay to leave the south-west entry clear.
                if (side > 0 || row != 0) shelfBay(side * 39.9, z, Math.PI / 2, 18);
                floorLamp(side * 29, z - 3);
            }
            for (int x : new int[]{-18, -6, 6, 18}) {
                shelfBay(x, side * 43, Math.PI / 2, 12);
            }
        }
    }

    private void shelfBay(double centerX, double centerZ, double normalAngle, int height) {
        double normalX = Math.cos(normalAngle), normalZ = Math.sin(normalAngle);
        for (int x = (int) Math.floor(centerX - 5); x <= Math.ceil(centerX + 5); x++) {
            for (int z = (int) Math.floor(centerZ - 5); z <= Math.ceil(centerZ + 5); z++) {
                double dx = x - centerX, dz = z - centerZ;
                double across = -dx * normalZ + dz * normalX;
                double depth = dx * normalX + dz * normalZ;
                if (Math.abs(across) > 3.9 || Math.abs(depth) > 1.05) continue;
                EnumFacing facing = EnumFacing.getFacingFromVector(
                        (float) (depth < 0 ? -normalX : normalX), 0,
                        (float) (depth < 0 ? -normalZ : normalZ));
                boolean post = Math.abs(across) > 2.8;
                IBlockState books = shelf(facing);
                for (int y = 3; y < 3 + height; y++) {
                    boolean rail = (y - 3) % 6 == 0 || y == height + 2;
                    put(x, y, z, post ? POST : rail ? WOOD : books);
                }
                put(x, height + 3, z, SLAB);
            }
        }
        if (Math.floorMod((int) Math.round(centerX + centerZ), 3) == 0) {
            int x = (int) Math.round(centerX - normalZ * 4.6);
            int z = (int) Math.round(centerZ + normalX * 4.6);
            if (Math.abs(x) > 2 && Math.abs(z) > 2) {
                putIfEmpty(x, 4, z, Blocks.WEB.getDefaultState());
                putIfEmpty(x, 5, z, Blocks.WEB.getDefaultState());
            }
        }
    }

    private void wallTrim() {
        for (int side : new int[]{-1, 1}) {
            for (int z = -48; z <= 48; z += 6) {
                fill(side * 45, 3, z, side * 45, 32, z, POST);
            }
            for (int x = -42; x <= 42; x += 6) {
                fill(x, 3, side * 54, x, 32, side * 54, POST);
            }
            for (int y : new int[]{11, 21, 32}) {
                fill(side * 44, y, -50, side * 45, y, 50, WOOD);
                fill(-43, y, side * 53, 43, y, side * 54, WOOD);
            }
            for (int z : new int[]{-36, 36}) {
                for (int y = 3; y <= 10; y++) {
                    put(side * 44, y, z, Blocks.LADDER.getDefaultState()
                            .withProperty(BlockLadder.FACING, side < 0 ? EnumFacing.EAST : EnumFacing.WEST));
                }
            }
        }
    }

    private void centralStudy() {
        for (int x = -9; x <= 9; x++) for (int z = -9; z <= 9; z++) {
            int radiusSquared = x * x + z * z;
            if (radiusSquared < 54 || radiusSquared > 82 || Math.abs(x) < 3 || Math.abs(z) < 3) continue;
            EnumFacing facing = EnumFacing.getFacingFromVector(-x, 0, -z);
            IBlockState openShelf = shelf(facing).withProperty(RuinedBookcase.DAMAGE, RuinedBookcase.Damage.DOUBLE_SIDED);
            put(x, 3, z, openShelf);
            put(x, 4, z, dev.lostfantasy.core.ResearchCatalog.indexAt(x,4,z)>=4
                    ? facing(ModBlocks.ARCHIVE_CATALOG.getDefaultState(),facing) : openShelf);
            put(x, 5, z, SLAB);
        }
        for (int x = -3; x <= 2; x++) for (int z = -3; z <= 3; z++) {
            double outer = square((x + .5) / 2.9) + square(z / 3.4);
            double cutout = square((x - 1.6) / 2.0) + square(z / 2.5);
            if (outer > 1 || cutout < 1) continue;
            put(x, 4, z, Blocks.STONE_SLAB.getStateFromMeta(15));
            if ((x == -2 && Math.abs(z) == 1) || (x == 0 && Math.abs(z) == 3)) put(x, 3, z, FENCE);
        }
        put(-2, 5, 0, facing(ModBlocks.EMERALD_STUDY.getDefaultState(), EnumFacing.EAST));
        put(-1, 5, 2, ModBlocks.ARMILLARY.getDefaultState());
        put(-1, 5, -2, ModBlocks.LIBRARY_LAMP.getDefaultState());
        chair(1, 0, EnumFacing.WEST);
    }

    private void studyCorners() {
        desk(-12, -10, 2, 1);
        put(-13, 5, -10, ModBlocks.RESEARCH_NOTES.getDefaultState());
        put(-11, 5, -10, ModBlocks.RESEARCH_NOTES.getDefaultState());
        put(-14, 5, -11, ModBlocks.LIBRARY_LAMP.getDefaultState());
        put(-10, 5, -11, ModBlocks.LIBRARY_LAMP.getDefaultState());
        chair(-13, -13, EnumFacing.SOUTH);
        chair(-10, -13, EnumFacing.SOUTH);

        desk(-12, 11, 2, 1);
        put(-13, 5, 11, ModBlocks.ARMILLARY.getDefaultState());
        put(-11, 5, 11, ModBlocks.RESEARCH_NOTES.getDefaultState());
        put(-14, 5, 10, ModBlocks.LIBRARY_LAMP.getDefaultState());
        fill(-9, 3, 12, -8, 6, 12, shelf(EnumFacing.NORTH));
        put(-9, 7, 12, SLAB);
        put(-8, 7, 12, SLAB);

        desk(12, 10, 1, 1);
        put(12, 5, 10, ModBlocks.RESEARCH_NOTES.getDefaultState());
        fill(10, 3, 14, 13, 5, 14, WOOD);
        fill(10, 4, 14, 13, 4, 14, ModBlocks.ARCHIVE_CATALOG.getDefaultState());
        fill(10, 6, 14, 13, 6, 14, SLAB);
        floorLamp(9, 12);

        fill(11, 3, -7, 14, 3, -7, PURPLE);
        fill(11, 3, -6, 14, 4, -6, PURPLE);
        fill(10, 3, -7, 10, 4, -6, WOOD);
        fill(15, 3, -7, 15, 4, -6, WOOD);
        chair(16, -10, EnumFacing.WEST);
        put(13, 3, -10, FENCE);
        put(13, 4, -10, WOOD);
        put(13, 5, -10, ModBlocks.RESEARCH_NOTES.getDefaultState());
        floorLamp(16, -6);
    }

    private void readingBays() {
        for (int x : new int[]{-35, 35}) for (int z : new int[]{-25, 15}) {
            desk(x, z, 1, 0);
            put(x - 1, 5, z, ModBlocks.LIBRARY_LAMP.getDefaultState());
            put(x, 5, z, ModBlocks.RESEARCH_NOTES.getDefaultState());
            chair(x, z + 2, EnumFacing.NORTH);
        }
    }

    private void desk(int x, int z, int halfWidth, int halfDepth) {
        fill(x - halfWidth, 4, z - halfDepth, x + halfWidth, 4, z + halfDepth, WOOD);
        for (int dx : new int[]{-halfWidth, halfWidth}) for (int dz : new int[]{-halfDepth, halfDepth}) {
            put(x + dx, 3, z + dz, FENCE);
        }
    }

    private void chair(int x, int z, EnumFacing facing) {
        put(x, 3, z, PURPLE);
        int backX = x - facing.getXOffset(), backZ = z - facing.getZOffset();
        put(backX, 3, backZ, WOOD);
        put(backX, 4, backZ, PURPLE);
        put(backX, 5, backZ, SLAB);
    }

    private void floorLamp(int x, int z) {
        put(x, 3, z, FENCE);
        put(x, 4, z, FENCE);
        put(x, 5, z, ModBlocks.LIBRARY_LAMP.getDefaultState());
    }

    private void passageLights() {
        // Low wall lights reach the walking level; the original chandeliers stay above the shelves.
        for (int side : new int[]{-1, 1}) {
            for (int z = -48; z <= 48; z += 12) {
                // Keep the four rolling-ladder columns clear.
                wallLight(side * 44, Math.abs(z) == 36 ? z - Integer.signum(z) * 6 : z);
            }
            for (int x = -36; x <= 36; x += 12) {
                int z = side > 0 && (x == 12 || x == 24) ? 52 : side * 53;
                wallLight(x, z);
            }
            for (int z = -38; z <= 40; z += 26) glowstoneLamp(side * 36, z);
            for (int z = -34; z <= 44; z += 26) glowstoneLamp(side * 27, z);
            for (int z : new int[]{-16, 16}) glowstoneLamp(side * 16, z);
            for (int z : new int[]{-34, 34}) glowstoneLamp(side * 12, z);
        }
        // A small pendant lights the central desk without putting a post in its access path.
        fill(0, 9, 0, 0, 33, 0, Blocks.IRON_BARS.getDefaultState());
        for (int[] arm : new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
            put(arm[0], 9, arm[1], Blocks.GLOWSTONE.getDefaultState());
            put(arm[0], 10, arm[1], SLAB);
        }
    }

    private void wallLight(int x, int z) {
        put(x, 6, z, Blocks.GLOWSTONE.getDefaultState());
        put(x, 7, z, SLAB);
    }

    private void glowstoneLamp(int x, int z) {
        put(x, 3, z, FENCE);
        put(x, 4, z, FENCE);
        put(x, 5, z, Blocks.GLOWSTONE.getDefaultState());
        put(x, 6, z, SLAB);
    }

    private void ceiling() {
        for (int z = -48; z <= 48; z += 12) {
            fill(-44, 34, z, 44, 35, z, WOOD);
        }
        for (int x : new int[]{-44, -24, 24, 44}) fill(x, 34, -53, x, 35, 53, WOOD);
        // A smaller gridded lightwell matches the source instead of a broad purple roof.
        fill(-3, 31, 2, 3, 31, 9, WOOD);
        for (int x : new int[]{-3, 3}) for (int z : new int[]{2, 9}) {
            fill(x, 32, z, x, 35, z, Blocks.IRON_BARS.getDefaultState());
        }
        for (int x = -2; x <= 2; x++) for (int z = 3; z <= 8; z++) {
            put(x, 31, z, x % 2 == 0 || z % 2 == 0
                    ? Blocks.IRON_BARS.getDefaultState() : Blocks.STAINED_GLASS.getStateFromMeta(10));
        }
        for (int[] center : new int[][]{{-20,-24},{20,-24},{-20,24},{20,24},{0,-44},{0,44}}) {
            int x = center[0], z = center[1];
            fill(x, 26, z, x, 33, z, Blocks.IRON_BARS.getDefaultState());
            fill(x - 2, 25, z, x + 2, 25, z, Blocks.NETHER_BRICK_FENCE.getDefaultState());
            fill(x, 25, z - 2, x, 25, z + 2, Blocks.NETHER_BRICK_FENCE.getDefaultState());
            for (int[] arm : new int[][]{{-2,0},{2,0},{0,-2},{0,2}}) {
                put(x + arm[0], 26, z + arm[1], Blocks.GLOWSTONE.getDefaultState());
                put(x + arm[0], 27, z + arm[1], SLAB);
            }
        }
    }

    static IBlockState floor(int x, int z, long seed) {
        int radiusSquared = x * x + z * z;
        if (Math.abs(x) > 43 || Math.abs(z) > 50) return WOOD;
        // Patch wear exposes boards and cracked masonry without leaving a floor-height gap.
        long patch = LibraryRuinLayout.blockNoise(seed, Math.floorDiv(x, 3), 2, Math.floorDiv(z, 3));
        int wear = (int) Math.floorMod(patch, 100);
        if (radiusSquared > 49 && wear < 13
                && Math.floorMod(LibraryRuinLayout.blockNoise(seed, x, 73, z), 5) != 0) {
            return wear < 8 ? WOOD : Blocks.STONEBRICK.getStateFromMeta(2);
        }
        return Blocks.WOOL.getStateFromMeta(x >= 9 && x <= 17 && z >= -13 && z <= -5 ? 10 : 14);
    }

    static boolean brokenCornice(int x, int y, int z, long seed) {
        // Only trim is removed: books, table supports and walkways are preserved.
        return (y == 5 || y == 15 || y == 21)
                && Math.floorMod(LibraryRuinLayout.blockNoise(seed, Math.floorDiv(x, 3), 74,
                Math.floorDiv(z, 3)), 7) == 0;
    }

    private void fallenMasonry() {
        for (int[] patch : new int[][]{{-27,31},{27,-32},{3,48}}) {
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                if (dx == dz || dx == 0) {
                    putIfEmpty(patch[0] + dx, 3, patch[1] + dz, Blocks.STONE_SLAB.getStateFromMeta(3));
                }
            }
        }
    }

    private static IBlockState shelf(EnumFacing facing) {
        return facing(ModBlocks.RUINED_BOOKCASE.getDefaultState(), facing)
                .withProperty(RuinedBookcase.DAMAGE, RuinedBookcase.Damage.SPARSE);
    }

    private static IBlockState facing(IBlockState state, EnumFacing facing) {
        return state.withProperty(BlockHorizontal.FACING, facing);
    }

    private void fill(int x1, int y1, int z1, int x2, int y2, int z2, IBlockState state) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++)
                for (int y = y1; y <= y2; y++) put(x, y, z, state);
    }

    private void put(int x, int y, int z, IBlockState state) {
        int index = index(x, z);
        if (columns[index] == null) columns[index] = new IBlockState[LibraryRuinLayout.HEIGHT];
        columns[index][y] = state;
    }

    private void putIfEmpty(int x, int y, int z, IBlockState state) {
        IBlockState[] column = columns[index(x, z)];
        if (column == null || column[y] == null) put(x, y, z, state);
    }

    private static int index(int x, int z) { return (z + HALF_Z) * WIDTH + x + HALF_X; }
    private static double square(double value) { return value * value; }
}
