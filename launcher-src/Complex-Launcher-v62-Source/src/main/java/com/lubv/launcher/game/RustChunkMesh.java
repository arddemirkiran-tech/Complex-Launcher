package com.lubv.launcher.game;

/**
 * v51: bridge to the Rust chunk-section mesher ({@code native/clsx-chunk}).
 *
 * <p>The native library is optional: when it is missing (Linux build without the
 * binary, unsupported platform, antivirus quarantine) {@link #available()} is
 * false and callers must fall back to their own Java mesher. Nothing here
 * changes vanilla behaviour on its own - the Fabric mod calls this class.
 *
 * <p>Section layout: 4096 bytes, {@code index = (y * 16 + z) * 16 + x}, {@code 0}
 * means air, any non-zero value means solid.
 */
public final class RustChunkMesh {
    /** Native library name (clsx_chunk.dll / libclsx_chunk.so). */
    private static final String LIBRARY = "clsx_chunk";

    private static final boolean AVAILABLE;
    private static final String LOAD_ERROR;

    static {
        boolean ok = false;
        String err = "";
        try {
            System.loadLibrary(LIBRARY);
            ok = true;
        } catch (Throwable t) {
            err = t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        AVAILABLE = ok;
        LOAD_ERROR = err;
    }

    private RustChunkMesh() {
    }

    public static boolean available() {
        return AVAILABLE;
    }

    public static String loadError() {
        return LOAD_ERROR;
    }

    /**
     * Native mesher. Returns {@code (quads << 32) | vertices}, or {@code -1} on
     * refusal. Not meant to be called before checking {@link #available()}.
     */
    private static native long meshSection(byte[] blocks);

    /**
     * Meshes one section, or returns {@code -1} when the native library is not
     * loaded so the caller can use the Java path.
     */
    public static long mesh(byte[] blocks) {
        if (!AVAILABLE) {
            return -1L;
        }
        if (blocks == null || blocks.length != 4096) {
            throw new IllegalArgumentException("section must be exactly 4096 bytes");
        }
        return meshSection(blocks);
    }

    public static int quads(long packed) {
        return (int) (packed >>> 32);
    }

    public static int vertices(long packed) {
        return (int) packed;
    }
}
