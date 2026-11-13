# Complex Launcher v54

## Türkçe karakterler düzeltildi

Launcher'ın paketlediği yazı tipleri Google Fonts'un **latin** ve **latin-ext**
alt kümeleri olarak ayrı dosyalardaydı ve iki dosya aynı aile adını
paylaşıyordu. Java bu durumda latin dosyasını seçiyor, içinde `ş`, `ğ`, `İ`,
`ı` glifleri olmadığı için bu harfler eksik glif kutusu (ekranda `?`) olarak
çiziliyordu.

- Space Grotesk ve Bricolage Grotesque artık **tek dosyada birleşik**:
  720 ve 476 glif, tam Latin + Latin Extended kapsamı.
- Font ailesine özel ad verildi (`Complex Space Grotesk`, `Complex Bricolage`);
  böylece sistemde kurulu aynı adlı bir font araya giremiyor.
- `BrandFonts` artık bir yüzü kaydetmeden önce ASCII ve `ÇĞİÖŞÜçğıöşü`
  karakterlerini çizebildiğini doğruluyor; kapsamıyorsa hiç yüklenmiyor ve
  güvenli sistem fontuna düşülüyor.

## Ana sayfa dengelendi

- Seçili instance **hero** kartı eskiden dikey alanın tamamını (`weighty = 1.0`)
  kaplıyordu; pencere büyüdükçe kartın içi boş mor bir alana dönüşüyor,
  altındaki "Son instance'lar" ve "Oyun ayarları" kartları ekranın dışına
  itiliyordu. Hero artık doğal yüksekliğinde (yaklaşık 240-270 px) kalıyor.
- Kalan dikey alan oyun ayarları kartına verildi; sol sütun ile sağ sütun
  (hesap, istatistikler, konsol) aynı hizada bitiyor.

## Oyun açılışı hızlandırıldı

Hazırlık aşamasındaki gereksiz ağ ve disk işleri kaldırıldı. Kurulu bir
instance'ın ikinci ve sonraki açılışlarında launcher tarafı artık ~1 saniye
civarında tamamlanıyor; konsola `[Hız] Launcher hazırlığı ... ms` satırı yazılır.

- **Sürüm/loader JSON'ları önce yerelden:** Minecraft sürüm JSON'u, Fabric
  loader listesi ve profili, Forge promotions ve NeoForge metadata artık her
  açılışta ağdan çekilmiyor; disk cache varsa doğrudan okunuyor.
- **Kalıcı Java seçimi:** seçilen Java ve tespit edilen sürüm
  `java-select.properties` dosyasında saklanıyor; dosya hâlâ geçerliyse
  `java -version` süreci açılmıyor.
- **Kütüphane doğrulaması önbelleği:** ~400 kütüphane JAR'ının SHA-1'i yerine
  dosya adı + boyut + mtime parmak izi `versions/<sürüm>/.libs-ready`
  dosyasında tutuluyor.
- **Asset önbelleği:** binlerce asset'in tek tek SHA-1 kontrolü yerine
  `assets/indexes/<index>.ready` işareti kullanılıyor.
- **Native çıkarma:** `.natives-ready` kontrolü SHA-1'den önce yapılıyor.

## Notlar

- İlk açılış (veya sürüm/loader değişikliği) doğrulama ve indirme yüzünden
  normalden uzun sürebilir; sonraki açılışlar hızlı yoldan gider.
- Kütüphane veya asset dosyaları elle bozulursa ilgili `.ready` dosyasını
  silmek tam doğrulamayı yeniden tetikler.

## Sürüm

`UpdateManager.CURRENT_VERSION = "54"`.
