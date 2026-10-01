package dev.lostfantasy.client;

import java.util.*;
import net.minecraft.client.resources.*;

/** Held scythe and paddle; the character uses vanilla Alex geometry. */
final class KomachiMesh implements IResourceManagerReloadListener {
    static final String[] PARTS = {"scythe", "oar"};
    static final KomachiMesh INSTANCE = new KomachiMesh();
    private final Map<String, TexturedMesh> parts = new HashMap<>();
    private KomachiMesh() {
        for (String key : PARTS) parts.put(key, new TexturedMesh("komachi/" + key));
    }
    @Override public void onResourceManagerReload(IResourceManager manager) {
        for (TexturedMesh mesh : parts.values()) mesh.onResourceManagerReload(manager);
    }
    void draw(String key, float opacity) {
        TexturedMesh mesh = parts.get(key);
        if (mesh != null) mesh.draw(opacity);
    }
}
