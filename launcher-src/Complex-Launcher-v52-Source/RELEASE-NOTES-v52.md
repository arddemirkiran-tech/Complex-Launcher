# Complex Launcher v52

## v52.1 ek düzeltme: ana sayfa donması ve konsol duplike görüntüsü

Önceki v52 yapısında ana sayfa donuk kalıyor, konsolda yazı seçerken veya
log akarken alt kısım duplike görünüyordu. İki kök neden vardı:

- Konsol `JTextArea`'sı **tamamen şeffaf** (alpha 0) arka planla `opaque`
  bırakılmıştı. Swing böyle bir bileşende eski pikselleri temizlemediği için
  seçim vurgusu ve yeni log satırları eskilerin üzerine biniyordu. Konsol artık
  kartla aynı **opak** renkte (`Theme.BG_ELEVATED`) çiziliyor.
- Konsolun kaydırma/viewport zinciri tamamen şeffaf olduğu için her seçim ve
  otomatik kaydırma **tüm pencereyi** yeniden çiziyordu; bu da ana sayfayı
  donduruyordu. Konsolun etrafına opak bir sarmalayıcı eklendi (FX modunda
  şeffaflaştırılmıyor), böylece yeniden çizim yalnızca konsol alanında kalıyor.
- Ana sayfa için 30 FPS hızlı tazeleme geri getirildi. FX tasarım modunda
  `BackgroundFx` zaten root pane'i 30 FPS yenilediği için ikinci timer
  çalışmıyor; çift timer kaynaklı tearing/glitch oluşmuyor.

## Sekme tıklaması: tek tık yeterli

Sol menüdeki sekmeler `mouseClicked` ile dinleniyordu; Swing bu olayı yalnızca
basma ve bırakma aynı bileşende biterse tetikler. Satır üzerinde hafif kayma,
satır arasındaki 2 px boşluk veya pencere ilk kez aktifleşirken gelen tıklama
kayboluyor, bazı sekmeler "iki kez basmadan" açılmıyordu.

- Seçim artık **basma anında** (`mousePressed`) yapılıyor.
- Bırakma yalnızca basma kaçırıldıysa yedek yol olarak çalışıyor; çift geçiş yok.
- Aynı davranış sol alttaki hesap kartına da uygulandı.

## Ana sayfa glitch düzeltmesi

- Ana sayfa kök panelinde, yeni tasarımdan sonra hiçbir şey boyamayan bir
  25 FPS `repaint()` timer'ı kalmıştı; tüm sayfa (hero, kartlar, konsol) boş
  yere saniyede 25 kez yeniden çiziliyor ve tıklama animasyonlarıyla
  çakışınca görüntü glitchleniyordu. Timer tamamen kaldırıldı.
- Hero chip'leri 500 ms'de bir koşulsuz `setText` ile güncelleniyordu; her
  çağrı `revalidate()` tetikleyip ana sayfayı yeniden yerleştiriyordu. Artık
  metin gerçekten değişmedikçe güncellenmiyor.
- `UiFx` accent/ghost/danger/launch butonlarında basınca yalnızca zemin
  küçülüyor, yazı tam boyutta kalıp butonun dışına taşıyordu. Metin de aynı
  ölçekte çiziliyor; butonlar basınca bütün olarak küçülüyor.

## Tema Oluşturucu iyileştirmeleri

- Hazır tema listesi artık `Theme.Preset`'ten geliyor (22 tema). Eski sabit
  liste gerçek presetlerle uyuşmuyordu (`Royal`, `Nord`, `Sakura` yoktu ve
  seçim sessizce Nebula'ya düşüyordu); Minecraft temaları da listede yoktu.
- 13 rengin her biri artık **yazılabilir hex alanı** ile elle girilebiliyor
  (`#8B5CF6`; Enter/odak kaybında uygulanır, geçersiz değer eski renge döner).
- Tema adı yazarken canlı bağlanıyor; renk seçmek yazılan adı silmiyor.
- **Rastgele** kullanıcının tema adını ezmiyor.
- **Kapat** düğmesi eklendi: kaydetmeden kapatınca önceki temaya dönüyor.
- "Yağan fotoğraflar" bölümü yanlış konteynere eklendiği için hiç
  görünmüyordu; artık diyaloğun altında görünüyor.
- Kaydetme başarısız olursa sahte "kaydedildi" mesajı gösterilmiyor; font
  seçimi Randomize/İçe Aktar sonrası doğru seçeneğe dönüyor. Canlı önizleme
  başlığında tema adı görünüyor.

## Kurulum EXE'sinde seçilen dil launcher'ı belirler

Setup EXE'si dil seçimini `%LOCALAPPDATA%\ComplexLauncher\setup-lang.txt`
dosyasına yazıyordu ama launcher bunu okumuyordu.

- Launcher ilk açılışta bu dosyayı (portable kurulumda kurulum klasöründe,
  sistem kurulumunda `%LOCALAPPDATA%\ComplexLauncher` altında) okur ve
  `lang.txt` yoksa seçilen dili uygular.
- Launcher içinden sonradan yapılan dil seçimi (`lang.txt`) daha yeni olduğu
  için korunur.
- Setup yeniden çalıştırılıp başka bir dil seçilirse `setup-lang.txt` daha
  yeni olur ve yeni seçim uygulanır.

## Sürüm

`UpdateManager.CURRENT_VERSION = "52"`.
