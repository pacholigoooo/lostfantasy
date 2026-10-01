package dev.lostfantasy.world;

import dev.lostfantasy.LostFantasy;
import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.block.RuinedBookcase;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.world.storage.loot.LootTableList;

/** Shared Voile hall geometry and loot used by the Scarlet Mansion library. */
public final class LibraryStructure {
    private LibraryStructure() {}
    public static final ResourceLocation COMMON_LOOT = new ResourceLocation(LostFantasy.ID, "chests/library_common");
    public static final ResourceLocation RESEARCH_LOOT = new ResourceLocation(LostFantasy.ID, "chests/library_research");
    public static final ResourceLocation CORE_LOOT = new ResourceLocation(LostFantasy.ID, "chests/library_core");
    public static final ResourceLocation SECRET_LOOT = new ResourceLocation(LostFantasy.ID, "chests/library_secret");

    private static final int OUTER_X = 52;
    private static final int OUTER_Z = 61;
    private static final int WALL_INNER_X = 51;
    private static final int WALL_INNER_Z = 60;
    private static final int SHELF_X = 49;
    private static final int SHELF_Z = 58;
    private static final int SECRET_MIN_X = 12;
    private static final int SECRET_MAX_X = 24;
    private static final int SECRET_MIN_Z = 53;
    private static final int SECRET_MAX_Z = 66;
    private static final IBlockState AIR = Blocks.AIR.getDefaultState();
    private static final IBlockState DARK_PLANKS = Blocks.PLANKS.getStateFromMeta(5);
    private static final IBlockState RED_CARPET = Blocks.CARPET.getStateFromMeta(14);
    private static final IBlockState DARK_SLAB = Blocks.WOODEN_SLAB.getStateFromMeta(5);
    private static final IBlockState[][] BOOKCASES = bookcaseStates();

    public static void register() {
        LootTableList.register(COMMON_LOOT);
        LootTableList.register(RESEARCH_LOOT);
        LootTableList.register(CORE_LOOT);
        LootTableList.register(SECRET_LOOT);
    }

    static int columnMaxY(int x, int z) {
        if (insideRounded(x, z, OUTER_X, OUTER_Z, 12)) return LibraryRuinLayout.HEIGHT;
        if (x >= SECRET_MIN_X && x <= SECRET_MAX_X && z >= SECRET_MIN_Z && z <= SECRET_MAX_Z) return 9;
        return -1;
    }

    static IBlockState stateAt(LibraryRuinLayout.Site site, int x, int y, int z) {
        return stateAt(site, columnAt(x, z), y);
    }

    static Column columnAt(int x, int z) {
        if (x < -OUTER_X || x > OUTER_X || z < -OUTER_Z || z > SECRET_MAX_Z) return new Column(x, z);
        return Columns.ALL[(x + OUTER_X) * Columns.DEPTH + z + OUTER_Z];
    }

    private static final class Columns {
        static final int DEPTH = OUTER_Z + SECRET_MAX_Z + 1;
        static final Column[] ALL = create();

        private static Column[] create() {
            Column[] columns = new Column[(OUTER_X * 2 + 1) * DEPTH];
            for (int x = -OUTER_X; x <= OUTER_X; x++) {
                for (int z = -OUTER_Z; z <= SECRET_MAX_Z; z++) {
                    columns[(x + OUTER_X) * DEPTH + z + OUTER_Z] = new Column(x, z);
                }
            }
            return columns;
        }
    }

    static IBlockState stateAt(LibraryRuinLayout.Site site, Column column, int y) {
        int x = column.x, z = column.z;
        IBlockState secret = column.secret ? secretRoomState(site, x, y, z) : null;
        if (secret != null) return secret;
        if (!column.inside) return null;

        if (y == 0) return ruinedStone(site, x, y, z);
        if (y == 1) return DARK_PLANKS;
        if (y == 2) return LibraryInterior.floor(x, z, site.structureSeed);
        if (y == LibraryRuinLayout.HEIGHT) return roofState(site, x, z);
        if (y < 2 || y > LibraryRuinLayout.HEIGHT) return null;

        if (column.wall) return ruinedStone(site, x, y, z);

        IBlockState feature = featureState(site, column, y);
        return feature == null ? AIR : feature;
    }

    static final class Column {
        final int x, z, maxY;
        final boolean inside, secret, wall, shelfRing;
        final EnumFacing facing;
        final IBlockState[] interior;

        Column(int x, int z) {
            this.x = x;
            this.z = z;
            inside = insideRounded(x, z, OUTER_X, OUTER_Z, 12);
            secret = x >= SECRET_MIN_X && x <= SECRET_MAX_X && z >= SECRET_MIN_Z && z <= SECRET_MAX_Z;
            maxY = inside ? LibraryRuinLayout.HEIGHT : secret ? 9 : -1;
            wall = inside && !insideRounded(x, z, WALL_INNER_X, WALL_INNER_Z, 11);
            shelfRing = insideRounded(x, z, SHELF_X, SHELF_Z, 9)
                    && !insideRounded(x, z, SHELF_X - 4, SHELF_Z - 4, 5);
            facing = inwardFacing(x, z);
            interior = inside ? LibraryInterior.column(x, z) : null;
        }
    }

    private static IBlockState secretRoomState(LibraryRuinLayout.Site site, int x, int y, int z) {
        if (x < SECRET_MIN_X || x > SECRET_MAX_X || z < SECRET_MIN_Z || z > SECRET_MAX_Z || y < 0 || y > 9) {
            return null;
        }
        if (y == 0 || y == 9) return ruinedStone(site, x, y, z);
        if (y == 1) return DARK_PLANKS;
        if (y == 2) return solidFloor(secretFloorCover(site, x, z));

        boolean doorway = z == SECRET_MIN_Z && x >= 17 && x <= 19 && y <= 5;
        boolean wall = x == SECRET_MIN_X || x == SECRET_MAX_X || z == SECRET_MIN_Z || z == SECRET_MAX_Z;
        if (wall) return doorway ? AIR : ruinedStone(site, x, y, z);
        if ((x == SECRET_MIN_X + 1 || x == SECRET_MAX_X - 1) && y >= 3 && y <= 6) {
            return ruinedShelf(site, x, y, z, x < 18 ? EnumFacing.EAST : EnumFacing.WEST, false);
        }
        if (y == 3 && z == 61 && (x == 15 || x == 21)) return Blocks.SEA_LANTERN.getDefaultState();
        return AIR;
    }

    private static IBlockState secretFloorCover(LibraryRuinLayout.Site site, int x, int z) {
        return Math.floorMod(LibraryRuinLayout.blockNoise(site.structureSeed, x, 2, z), 9) == 0 ? AIR : RED_CARPET;
    }

    private static IBlockState solidFloor(IBlockState cover) {
        // Keep the carpet pattern, but its top must meet furniture at local Y=3.
        return cover.getBlock() == Blocks.CARPET
                ? Blocks.WOOL.getStateFromMeta(Blocks.CARPET.getMetaFromState(cover)) : DARK_PLANKS;
    }

    private static IBlockState featureState(LibraryRuinLayout.Site site, Column column, int y) {
        int x = column.x, z = column.z;
        if (z == 52 && x >= 17 && x <= 19 && y >= 3 && y <= 5) {
            return bookcase(EnumFacing.SOUTH, RuinedBookcase.Damage.EMPTY);
        }

        IBlockState interior = column.interior == null ? null : column.interior[y];
        if (interior != null) {
            if (interior == DARK_SLAB && LibraryInterior.brokenCornice(x, y, z, site.structureSeed)) return AIR;
            if (interior.getBlock() == ModBlocks.RUINED_BOOKCASE
                    && interior.getValue(RuinedBookcase.DAMAGE) != RuinedBookcase.Damage.DOUBLE_SIDED) {
                // Keep the cabinet silhouette intact; only its contents are damaged.
                long noise = LibraryRuinLayout.blockNoise(site.structureSeed, x, y, z);
                RuinedBookcase.Damage damage = y == 3 && mod(noise, 9) == 0
                        ? RuinedBookcase.Damage.COLLAPSED : mod(noise, 5) == 0
                        ? RuinedBookcase.Damage.EMPTY : RuinedBookcase.Damage.SPARSE;
                return bookcase(interior.getValue(BlockHorizontal.FACING), damage);
            }
            return interior;
        }

        if (column.shelfRing && Math.abs(x) > 2 && Math.abs(z) > 2 && y >= 3 && y <= 32) {
            if (isShelfFrame(y)) return DARK_PLANKS;
            return ruinedShelf(site, x, y, z, column.facing, true);
        }

        if (y >= 3 && y <= 8 && (Math.abs(x) > 42 || Math.abs(z) > 50)
                && Math.floorMod(LibraryRuinLayout.blockNoise(site.structureSeed, x, y, z), 1300) == 0) {
            return Blocks.WEB.getDefaultState();
        }
        return null;
    }

    private static boolean isShelfFrame(int y) {
        return y == 11 || y == 12 || y == 21 || y == 22 || y == 31 || y == 32;
    }

    private static IBlockState ruinedShelf(LibraryRuinLayout.Site site, int x, int y, int z,
                                            EnumFacing facing, boolean allowMissing) {
        long noise = LibraryRuinLayout.blockNoise(site.structureSeed, x, y, z);
        int roll = mod(noise, 100);
        if (allowMissing && roll < 28) return AIR;
        if (Math.floorMod(noise, 401) == 0) return Blocks.BOOKSHELF.getDefaultState();
        RuinedBookcase.Damage damage = roll < 48 ? RuinedBookcase.Damage.EMPTY
                : roll < 72 ? RuinedBookcase.Damage.COLLAPSED : RuinedBookcase.Damage.SPARSE;
        return bookcase(facing, damage);
    }

    private static IBlockState bookcase(EnumFacing facing, RuinedBookcase.Damage damage) {
        return BOOKCASES[facing.getHorizontalIndex()][damage.ordinal()];
    }

    private static IBlockState[][] bookcaseStates() {
        RuinedBookcase.Damage[] variants = RuinedBookcase.Damage.values();
        IBlockState[][] states = new IBlockState[4][variants.length];
        for (int direction = 0; direction < states.length; direction++) {
            for (RuinedBookcase.Damage damage : variants) {
                states[direction][damage.ordinal()] = ModBlocks.RUINED_BOOKCASE.getDefaultState()
                        .withProperty(BlockHorizontal.FACING, EnumFacing.byHorizontalIndex(direction))
                        .withProperty(RuinedBookcase.DAMAGE, damage);
            }
        }
        return states;
    }

    private static IBlockState roofState(LibraryRuinLayout.Site site, int x, int z) {
        if (site.roofOpenAt(x, z)) return AIR;
        if (Math.abs(x) <= 2 && z >= 3 && z <= 8) {
            long noise = LibraryRuinLayout.blockNoise(site.structureSeed, x, LibraryRuinLayout.HEIGHT, z);
            return Math.floorMod(noise, 4) == 0 ? AIR : Blocks.STAINED_GLASS.getStateFromMeta(10);
        }
        return ruinedStone(site, x, LibraryRuinLayout.HEIGHT, z);
    }

    private static IBlockState ruinedStone(LibraryRuinLayout.Site site, int x, int y, int z) {
        int roll = mod(LibraryRuinLayout.blockNoise(site.structureSeed, x, y, z), 100);
        if (roll < 3) return Blocks.MONSTER_EGG.getStateFromMeta(2);
        if (roll < 9) return Blocks.COBBLESTONE.getDefaultState();
        if (roll < 18) return Blocks.STONEBRICK.getStateFromMeta(1);
        if (roll < 30) return Blocks.STONEBRICK.getStateFromMeta(2);
        return Blocks.STONEBRICK.getDefaultState();
    }

    private static EnumFacing inwardFacing(int x, int z) {
        double nx = Math.abs(x / (double) SHELF_X);
        double nz = Math.abs(z / (double) SHELF_Z);
        if (nx > nz) return x > 0 ? EnumFacing.WEST : EnumFacing.EAST;
        return z > 0 ? EnumFacing.NORTH : EnumFacing.SOUTH;
    }

    private static boolean insideRounded(int x, int z, int halfX, int halfZ, int radius) {
        int ax = Math.abs(x);
        int az = Math.abs(z);
        if (ax > halfX || az > halfZ) return false;
        if (ax <= halfX - radius || az <= halfZ - radius) return true;
        int dx = ax - (halfX - radius);
        int dz = az - (halfZ - radius);
        return dx * dx + dz * dz <= radius * radius;
    }

    private static int mod(long value, int modulus) {
        return (int)Math.floorMod(value,(long)modulus);
    }

    static Rotation rotation(int turns) {
        switch (turns & 3) {
            case 1:
                return Rotation.CLOCKWISE_90;
            case 2:
                return Rotation.CLOCKWISE_180;
            case 3:
                return Rotation.COUNTERCLOCKWISE_90;
            default:
                return Rotation.NONE;
        }
    }
}
