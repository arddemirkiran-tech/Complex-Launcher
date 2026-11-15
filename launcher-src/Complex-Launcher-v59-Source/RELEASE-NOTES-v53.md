# Complex Launcher v53

## Ana sayfa dengelendi

- Seçili instance **hero** kartı eskiden dikey alanın tamamını (`weighty = 1.0`)
  kaplıyordu; pencere büyüdükçe kartın içi boş mor bir alana dönüşüyor,
  altındaki "Son instance'lar" ve "Oyun ayarları" kartları ekranın dışına
  itiliyordu. Hero artık doğal yüksekliğinde (yaklaşık 240-270 px) kalıyor.
- Kalan dikey alan oyun ayarları kartına verildi; sol sütun ile sağ sütun
  (hesap, istatistikler, konsol) aynı hizada bitiyor. Boşluklar ve iç kenar
  boşlukları da biraz sıkılaştırıldı.

## Oyun açılışı hızlandırıldı

Hazırlık aşamasındaki gereksiz ağ ve disk işleri kaldırıldı. Kurulu bir
instance'ın ikinci ve sonraki açılışlarında launcher tarafı artık ~1 saniye
civarında tamamlanıyor; konsola `[Hız] Launcher hazırlığı ... ms` satırı yazılır.

- **Sürüm/loader JSON'ları önce yerelden:** Minecraft sürüm JSON'u, Fabric
  loader listesi ve profili, Forge promotions ve NeoForge metadata artık her
  açılışta ağdan çekilmiyor; disk cache varsa doğrudan okunuyor.
- **Kalıcı Java seçimi:** `findInstalled` her açılışta tüm Java adayları için
  `java -version` süreci açıyordu (1-3 sn). Seçilen Java ve tespit edilen
  sürüm `java-select.properties` dosyasında saklanıyor; dosya hâlâ geçerliyse
  süreç açılmıyor.
- **Kütüphane doğrulaması önbelleği:** ~400 kütüphane JAR'ının SHA-1'i her
  açılışta yeniden hesaplanıyordu. Dosya adı + boyut + mtime parmak izi
  `versions/<sürüm>/.libs-ready` dosyasına yazılıyor; iz değişmediyse hash
  ve indirme kontrolü tamamen atlanıyor.
- **Asset önbelleği:** binlerce asset'in tek tek SHA-1 kontrolü yerine
  `assets/indexes/<index>.ready` işareti kullanılıyor; kaynaklar eksiksiz
  indirildiyse sonraki açılışlar doğrulamayı atlıyor. Isıtma işçileri de
  hazır index için boşuna çalışmıyor.
- **Native çıkarma:** `.natives-ready` kontrolü artık SHA-1'den önce, sadece
  dosya boyutu/mtime ile yapılıyor; native JAR'lar gereksiz yere okunmuyor.

## Notlar

- İlk açılış (veya sürüm/loader değişikliği) doğrulama ve indirme yüzünden
  normalden uzun sürebilir; sonraki açılışlar hızlı yoldan gider.
- Kütüphane veya asset dosyaları elle bozulursa ilgili `.ready` dosyasını
  silmek tam doğrulamayı yeniden tetikler.

## Sürüm

`UpdateManager.CURRENT_VERSION = "53"`.
