# Complex Launcher v51 — Yeni Tasarım (UI yenileme)

Değişen/yeni dosyalar (hepsi `src/main/java/com/lubv/launcher/` altında):
- `ui/Nav.java`      (YENİ) sol gezinme çubuğu + hesap kartı
- `ui/Icons.java`    (YENİ) vektör ikon seti (font/resim gerektirmez)
- `ui/Cards.java`    (YENİ) kart / chip bileşenleri
- `ui/Theme.java`    yeni varsayılan "Complex" teması + modern UIManager ayarları
- `ui/MainWindow.java` kabuk: yatay sekmeler gizlendi, sidebar + üst çubuk; Ana Sayfa kontrol paneline çevrildi
- `update/UpdatePanel.java` uzun hata metni kesiliyor (başlığın üstüne binmiyor)
- `core/Settings.java`, `Launcher.java` varsayılan tema "Complex"

Mantık (başlatma, hesap, mod, güncelleme) değişmedi; `JTabbedPane` içerik modeli olarak duruyor.
Derleme: JDK 17+, classpath = Complex-Launcher.jar (+ namechanger.jar, ASM için):
  javac --release 17 -encoding UTF-8 -cp Complex-Launcher.jar:namechanger.jar -d out $(find src/main/java -name '*.java')
Eski temalar Ayarlar > Theme'dan seçilebilir (Nebula vb.).
