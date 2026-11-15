package com.lubv.launcher.ui;

import com.lubv.launcher.mods.ModManager;
import com.lubv.launcher.mods.ModSearchHit;
import com.lubv.launcher.mods.ModrinthApi;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.JList;

/**
 * Liste satiri tooltip'i Swing'in kendi mekanizmasiyla ({@code getToolTipText})
 * uretilir.
 *
 * <p>ONCEDEN bir mouse-moved dinleyicisi her satir degisiminde
 * {@code setToolTipText} cagiriyordu. ToolTipManager her cagrida pencereyi
 * yeniden kurdugu icin fare listede gezerken ust uste binen / ekranda asili
 * kalan tooltip pencereleri olusuyor, liste "dupeleniyor, bozuluyor" gibi
 * gorunuyordu. Swing zaten fare konumuna gore tooltip sorar; dinleyiciye ve
 * programatik goster/gizle'ye gerek yok.
 */
final class ListTips {
    private ListTips() {
    }

    /** Fare konumundaki satirin tooltip metni; satir yoksa null. */
    static String forEvent(JList<?> list, MouseEvent event) {
        if (list == null || event == null) {
            return null;
        }
        int row = list.locationToIndex(event.getPoint());
        if (row < 0 || row >= list.getModel().getSize()) {
            return null;
        }
        Rectangle bounds = list.getCellBounds(row, row);
        // locationToIndex son satirin altindaki bos alanda da en yakin satiri
        // dondurur: imlec gercekten hucrenin icinde mi diye bakilir.
        if (bounds == null || !bounds.contains(event.getPoint())) {
            return null;
        }
        Object item = list.getModel().getElementAt(row);
        if (item instanceof ModSearchHit) {
            ModSearchHit hit = (ModSearchHit) item;
            String desc = hit.description == null ? "" : hit.description;
            if (desc.length() > 160) {
                desc = desc.substring(0, 157) + "\u2026";
            }
            return "<html><b>" + esc(hit.title) + "</b><br>" + esc(desc) + "</html>";
        }
        if (item instanceof ModrinthApi.ModResult) {
            ModrinthApi.ModResult result = (ModrinthApi.ModResult) item;
            String desc = result.description == null ? "" : result.description;
            if (desc.length() > 160) {
                desc = desc.substring(0, 157) + "…";
            }
            return "<html><b>" + esc(result.title) + "</b><br>" + esc(desc) + "</html>";
        }
        if (item instanceof ModManager.InstalledMod) {
            ModManager.InstalledMod mod = (ModManager.InstalledMod) item;
            if (mod.fileName == null || mod.fileName.isBlank()) {
                return null; // "Henuz mod yok" yer tutucusu
            }
            String name = mod.projectTitle != null ? mod.projectTitle : mod.fileName;
            String ver = mod.versionNumber == null ? "" : mod.versionNumber;
            return "<html><b>" + esc(name) + "</b><br>" + esc(ver) + " \u00b7 " + esc(mod.fileName) + "</html>";
        }
        return null;
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
