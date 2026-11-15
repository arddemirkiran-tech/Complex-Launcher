package com.lubv.agent;

/**
 * GameProfileTransformer'in Hook'ladigi render metodundan FPS degerini yakalayip
 * FpsOverlay'e geciren statik yardimci. Oyun icinden yansima (reflection) olmadan,
 * bytecode enjeksiyonuyla cagrilir — bu yuzden class dosyasinin Java 8 uyumlu olmasi
 * yeterli.
 */
public final class OverlayRenderer {
    private static long lastFrame = System.nanoTime();
    private static int fps = -1;
    private static int frames = 0;

    private OverlayRenderer() {
    }

    /** Hook'tan her karede cagrilir: (Object gameRenderer, float partialTicks). */
    public static void render(Object renderer, float partialTicks) {
        try {
            long now = System.nanoTime();
            ++frames;
            long elapsed = now - lastFrame;
            if (elapsed >= 500_000_000L) {
                fps = (int) (frames * 1_000_000_000L / Math.max(1L, elapsed));
                frames = 0;
                lastFrame = now;
                FpsOverlay.setFps(fps);
            }
        } catch (Throwable ignore) {
            // hicbir durumda oyunu dusuremez
        }
    }
}
