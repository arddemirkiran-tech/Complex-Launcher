package com.lubv.launcher.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** Küçük vektör ikon seti (font/resource gerektirmez, her DPI'da keskin). */
public final class Icons {
    private Icons() {}

    public enum Kind { HOME, GRID, MODS, SHADERS, IMAGE, PACK, GLOBE, SERVER, USER, GEAR, PLAY, PLUS, STOP, TERMINAL, CHEVRON, FULLSCREEN }

    /** 24x24 tasarım ızgarasında çizer; size piksele ölçeklenir. */
    public static void paint(Graphics2D g0, Kind k, float x, float y, float size, Color c) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.translate(x, y);
        float s = size / 24f;
        g.scale(s, s);
        g.setColor(c);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (k) {
            case HOME: {
                Path2D.Double p = new Path2D.Double();
                p.moveTo(3.5, 11); p.lineTo(12, 3.8); p.lineTo(20.5, 11);
                p.moveTo(5.5, 9.6); p.lineTo(5.5, 20); p.lineTo(18.5, 20); p.lineTo(18.5, 9.6);
                g.draw(p);
                g.draw(new Line2D.Float(10, 20, 10, 14.5f));
                g.draw(new Line2D.Float(14, 20, 14, 14.5f));
                g.draw(new Line2D.Float(10, 14.5f, 14, 14.5f));
                break;
            }
            case GRID:
                g.draw(new RoundRectangle2D.Float(4, 4, 6.5f, 6.5f, 2, 2));
                g.draw(new RoundRectangle2D.Float(13.5f, 4, 6.5f, 6.5f, 2, 2));
                g.draw(new RoundRectangle2D.Float(4, 13.5f, 6.5f, 6.5f, 2, 2));
                g.draw(new RoundRectangle2D.Float(13.5f, 13.5f, 6.5f, 6.5f, 2, 2));
                break;
            case MODS: {
                Path2D.Double p = new Path2D.Double();
                p.moveTo(12, 3); p.lineTo(20, 7.5); p.lineTo(20, 16.5); p.lineTo(12, 21); p.lineTo(4, 16.5); p.lineTo(4, 7.5); p.closePath();
                p.moveTo(4, 7.5); p.lineTo(12, 12); p.lineTo(20, 7.5);
                p.moveTo(12, 12); p.lineTo(12, 21);
                g.draw(p);
                break;
            }
            case SHADERS: {
                g.draw(new Ellipse2D.Float(8, 8, 8, 8));
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    g.draw(new Line2D.Double(12 + Math.cos(a) * 7.2, 12 + Math.sin(a) * 7.2, 12 + Math.cos(a) * 9.6, 12 + Math.sin(a) * 9.6));
                }
                break;
            }
            case IMAGE: {
                g.draw(new RoundRectangle2D.Float(3.5f, 4.5f, 17, 15, 3, 3));
                g.fill(new Ellipse2D.Float(7.2f, 8.2f, 3, 3));
                Path2D.Double p = new Path2D.Double();
                p.moveTo(4, 17); p.lineTo(9.5, 12.5); p.lineTo(13, 15.5); p.lineTo(16, 12.5); p.lineTo(20, 16.5);
                g.draw(p);
                break;
            }
            case PACK: {
                g.draw(new RoundRectangle2D.Float(3.5f, 8, 17, 12, 2.5f, 2.5f));
                g.draw(new RoundRectangle2D.Float(5.5f, 4, 13, 4, 1.5f, 1.5f));
                g.draw(new Line2D.Float(10, 13, 14, 13));
                break;
            }
            case GLOBE: {
                g.draw(new Ellipse2D.Float(3.5f, 3.5f, 17, 17));
                g.draw(new Ellipse2D.Float(8, 3.5f, 8, 17));
                g.draw(new Line2D.Float(3.5f, 12, 20.5f, 12));
                break;
            }
            case SERVER: {
                g.draw(new RoundRectangle2D.Float(3.5f, 4, 17, 6.5f, 2.5f, 2.5f));
                g.draw(new RoundRectangle2D.Float(3.5f, 13.5f, 17, 6.5f, 2.5f, 2.5f));
                g.fill(new Ellipse2D.Float(6.5f, 6.1f, 2.2f, 2.2f));
                g.fill(new Ellipse2D.Float(6.5f, 15.6f, 2.2f, 2.2f));
                break;
            }
            case USER: {
                g.draw(new Ellipse2D.Float(8, 3.8f, 8, 8));
                g.draw(new Arc2D.Float(4.5f, 14, 15, 13, 0, 180, Arc2D.OPEN));
                break;
            }
            case GEAR: {
                g.draw(new Ellipse2D.Float(8.5f, 8.5f, 7, 7));
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    g.draw(new Line2D.Double(12 + Math.cos(a) * 6.6, 12 + Math.sin(a) * 6.6, 12 + Math.cos(a) * 9.4, 12 + Math.sin(a) * 9.4));
                }
                break;
            }
            case PLAY: {
                Path2D.Double p = new Path2D.Double();
                p.moveTo(7, 4.5); p.lineTo(19.5, 12); p.lineTo(7, 19.5); p.closePath();
                g.fill(p);
                break;
            }
            case STOP:
                g.fill(new RoundRectangle2D.Float(6, 6, 12, 12, 3, 3));
                break;
            case PLUS:
                g.draw(new Line2D.Float(12, 5, 12, 19));
                g.draw(new Line2D.Float(5, 12, 19, 12));
                break;
            case TERMINAL: {
                g.draw(new RoundRectangle2D.Float(3.5f, 4.5f, 17, 15, 3, 3));
                Path2D.Double p = new Path2D.Double();
                p.moveTo(7.5, 9.5); p.lineTo(11, 12); p.lineTo(7.5, 14.5);
                g.draw(p);
                g.draw(new Line2D.Float(12.5f, 15, 16.5f, 15));
                break;
            }
            case CHEVRON: {
                Path2D.Double p = new Path2D.Double();
                p.moveTo(7, 9.5); p.lineTo(12, 14.5); p.lineTo(17, 9.5);
                g.draw(p);
                break;
            }
            case FULLSCREEN: {
                Path2D.Double p = new Path2D.Double();
                p.moveTo(4, 9); p.lineTo(4, 4); p.lineTo(9, 4);
                p.moveTo(15, 4); p.lineTo(20, 4); p.lineTo(20, 9);
                p.moveTo(20, 15); p.lineTo(20, 20); p.lineTo(15, 20);
                p.moveTo(9, 20); p.lineTo(4, 20); p.lineTo(4, 15);
                g.draw(p);
                break;
            }
        }
        g.dispose();
    }

    public static javax.swing.Icon icon(Kind k, int size, java.util.function.Supplier<Color> color) {
        return new javax.swing.Icon() {
            @Override public void paintIcon(java.awt.Component comp, java.awt.Graphics gr, int x, int y) {
                Icons.paint((Graphics2D) gr, k, x, y, size, color.get());
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }
}
