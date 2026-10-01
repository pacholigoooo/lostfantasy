package dev.lostfantasy.client;

import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

/** Optional captures are available only in Forge's development runtime. */
final class DevelopmentPreviews {
    private static final String[][] ENTRIES = {
            {"komachiPreview", "KomachiPreview"},
            {"fortunePreview", "FortunePreview"},
            {"ribbonArtPreview", "RibbonArtPreview"},
            {"higanArtPreview", "HiganArtPreview"},
            {"higanPreview", "HiganPreview"}
    };

    private DevelopmentPreviews() {}

    static void install() {
        install(FMLLaunchHandler.isDeobfuscatedEnvironment(), Boolean::getBoolean, DevelopmentPreviews::launch);
    }

    static void install(boolean development, Predicate<String> enabled, Consumer<String> launch) {
        if (!development) return;
        for (String[] entry : ENTRIES) {
            if (enabled.test("lostfantasy." + entry[0])) launch.accept("dev.lostfantasy.preview." + entry[1]);
        }
    }

    private static void launch(String name) {
        try {
            Class.forName(name).getMethod("install").invoke(null);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Cannot start development preview " + name, ex);
        }
    }
}
