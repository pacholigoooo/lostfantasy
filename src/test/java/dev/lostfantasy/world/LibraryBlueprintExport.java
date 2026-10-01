package dev.lostfantasy.world;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.EnumFacing;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

/** Exports generator states for an architectural preview; does not create a game world. */
public final class LibraryBlueprintExport {
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        LibraryRuinLayout.Site site = new LibraryRuinLayout.Site(0, 0, 24, 0, 0x5eedL);
        Path output = Paths.get("build/library/blocks.tsv");
        Files.createDirectories(output.getParent());
        Map<String, Integer> counts = new TreeMap<>();
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            for (int x = -52; x <= 52; x++) for (int z = -61; z <= 66; z++) {
                LibraryStructure.Column column = LibraryStructure.columnAt(x, z);
                for (int y = 0; y <= column.maxY; y++) {
                    IBlockState state = LibraryStructure.stateAt(site, column, y);
                    if (y == 3) {
                        EnumFacing chest = chestAt(x, z);
                        if (chest != null) state = Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, chest);
                    }
                    if (state == null || state.getBlock() == Blocks.AIR) continue;
                    String id = state.getBlock().getRegistryName().toString();
                    writer.write(x + "\t" + y + "\t" + z + "\t" + id + "\t"
                            + state.getBlock().getMetaFromState(state));
                    writer.newLine();
                    counts.merge(id, 1, Integer::sum);
                }
            }
        }
        int shelves = counts.getOrDefault(ModBlocks.RUINED_BOOKCASE.getRegistryName().toString(), 0)
                + counts.getOrDefault("minecraft:bookshelf", 0);
        String report = "Fixed blueprint seed: 0x5eed\nBookshelf blocks: " + shelves
                + "\nNon-air blocks: " + counts.values().stream().mapToInt(Integer::intValue).sum()
                + "\n" + counts + "\n";
        Files.write(output.resolveSibling("counts.txt"), report.getBytes(StandardCharsets.UTF_8));
        System.out.print(report);
    }

    private static EnumFacing chestAt(int x, int z) {
        if (Math.abs(x) == 38 && Math.abs(z) == 36) return z < 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
        if (x == -9 && z == 13) return EnumFacing.WEST;
        if (x == 9 && z == 13) return EnumFacing.EAST;
        if (x == 14 && z == -10) return EnumFacing.NORTH;
        if (x == 18 && z == 63) return EnumFacing.SOUTH;
        return null;
    }
}
