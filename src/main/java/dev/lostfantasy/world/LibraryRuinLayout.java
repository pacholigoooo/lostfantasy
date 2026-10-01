package dev.lostfantasy.world;

/** Library bounds, rotations and deterministic furnishing variation. */
public final class LibraryRuinLayout {
    public static final int HALF_WIDTH = 52;
    public static final int NORTH_EXTENT = 66;
    public static final int SOUTH_EXTENT = 61;
    public static final int HEIGHT = 36;

    private LibraryRuinLayout() {}

    public static boolean intersectsChunk(Site site, int chunkX, int chunkZ) {
        return intersectsChunk(site, chunkX, chunkZ, 0);
    }

    static boolean intersectsChunk(Site site, int chunkX, int chunkZ, int padding) {
        return intersectsArea(site, chunkX << 4, chunkZ << 4, padding);
    }

    private static boolean intersectsArea(Site site, int minX, int minZ, int padding) {
        int minOffsetX;
        int maxOffsetX;
        int minOffsetZ;
        int maxOffsetZ;
        switch (site.turns & 3) {
            case 1:
                minOffsetX = -NORTH_EXTENT;
                maxOffsetX = SOUTH_EXTENT;
                minOffsetZ = -HALF_WIDTH;
                maxOffsetZ = HALF_WIDTH;
                break;
            case 2:
                minOffsetX = -HALF_WIDTH;
                maxOffsetX = HALF_WIDTH;
                minOffsetZ = -NORTH_EXTENT;
                maxOffsetZ = SOUTH_EXTENT;
                break;
            case 3:
                minOffsetX = -SOUTH_EXTENT;
                maxOffsetX = NORTH_EXTENT;
                minOffsetZ = -HALF_WIDTH;
                maxOffsetZ = HALF_WIDTH;
                break;
            default:
                minOffsetX = -HALF_WIDTH;
                maxOffsetX = HALF_WIDTH;
                minOffsetZ = -SOUTH_EXTENT;
                maxOffsetZ = NORTH_EXTENT;
        }
        int maxX = minX + 15;
        int maxZ = minZ + 15;
        return maxX >= site.centerX() + minOffsetX - padding && minX <= site.centerX() + maxOffsetX + padding
                && maxZ >= site.centerZ() + minOffsetZ - padding && minZ <= site.centerZ() + maxOffsetZ + padding;
    }

    public static int[] toWorld(int localX, int localZ, int turns) {
        int[] result = new int[2];
        rotateInto(localX, localZ, turns, result);
        return result;
    }

    private static void rotateInto(int x, int z, int turns, int[] result) {
        switch (turns & 3) {
            case 1:
                result[0] = -z;
                result[1] = x;
                break;
            case 2:
                result[0] = -x;
                result[1] = -z;
                break;
            case 3:
                result[0] = z;
                result[1] = -x;
                break;
            default:
                result[0] = x;
                result[1] = z;
        }
    }

    public static int[] toLocal(int worldOffsetX, int worldOffsetZ, int turns) {
        return toWorld(worldOffsetX, worldOffsetZ, 4 - (turns & 3));
    }

    static void toLocal(int worldOffsetX, int worldOffsetZ, int turns, int[] result) {
        rotateInto(worldOffsetX, worldOffsetZ, 4 - (turns & 3), result);
    }

    public static long blockNoise(long structureSeed, int x, int y, int z) {
        long value = structureSeed;
        value ^= x * 0x632be59bd9b4e019L;
        value ^= y * 0x9e3779b97f4a7c15L;
        value ^= z * 0x94d049bb133111ebL;
        value ^= value >>> 30;
        value *= 0xbf58476d1ce4e5b9L;
        value ^= value >>> 27;
        value *= 0x94d049bb133111ebL;
        return value ^ value >>> 31;
    }

    public static final class Site {
        public final int chunkX;
        public final int chunkZ;
        public final int baseY;
        public final int turns;
        public final long structureSeed;
        private volatile int[] roofOpenings;

        Site(int chunkX, int chunkZ, int baseY, int turns, long structureSeed) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.baseY = baseY;
            this.turns = turns;
            this.structureSeed = structureSeed;
        }

        public int centerX() {
            return (chunkX << 4) + 8;
        }

        public int centerZ() {
            return (chunkZ << 4) + 8;
        }

        public long distanceSquared(int x, int z) {
            long dx = (long) centerX() - x;
            long dz = (long) centerZ() - z;
            return dx * dx + dz * dz;
        }

        boolean roofOpenAt(int x, int z) {
            int[] openings = roofOpenings;
            if (openings == null) {
                int count = 3 + (int) Math.floorMod(structureSeed, 3L);
                openings = new int[count * 3];
                for (int i = 0; i < count; i++) {
                    long noise = blockNoise(structureSeed, i, 91, -i);
                    int radius = 4 + (int) Math.floorMod(noise >>> 33, 5L);
                    openings[i * 3] = -38 + (int) Math.floorMod(noise, 77L);
                    openings[i * 3 + 1] = -45 + (int) Math.floorMod(noise >>> 17, 91L);
                    openings[i * 3 + 2] = radius * radius;
                }
                // Most placement candidates are rejected without ever needing roof geometry.
                roofOpenings = openings;
            }
            for (int i = 0; i < openings.length; i += 3) {
                int dx = x - openings[i], dz = z - openings[i + 1];
                if (dx * dx + dz * dz <= openings[i + 2]) return true;
            }
            return false;
        }
    }
}
