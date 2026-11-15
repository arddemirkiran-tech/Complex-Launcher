# Complex Launcher v52

## Tema Oluşturucu düzeltmesi (v52.2)

- Diyalog penceresi ve iç kaydırma alanı artık **opak**: tema uygulanırken
altındaki launcher pikselleri sızıp "çift/bozuk" görüntü oluşturmuyor.
Pencere ilk açıldığında ve boyutlandığında tüm ağaç bir kez temiz boyanıyor.
- Tema uygulandıktan sonra hem launcher hem Tema Oluşturucu ağacı tamamen
yeniden boyanıyor; yarım kalan boyama kalıntıları siliniyor.
- Renk satırı hover önizlemesi fare alt bileşene geçince kesilmiyor ve
fare çıkınca hex kutuları da gerçek renge geri dönüyor (önceki sürümde
kutular hover renginde kalabiliyordu).
- **Yazı tipi ve ağırlık hover önizlemesi eklendi:** yazı tipi listesinde bir
ailenin, Başlık/Düğme listesinde Normal/Kalın seçeneğinin üzerine gelince
sağdaki canlı maket anında o yazı tipi/kalınlıkla boyanıyor. Seçim
yapılmadan çıkınca kendiliğinden eski haline dönüyor. Hover sırasında taslak
renkler/font değerleri kalıcı olarak değiştirilmiyor.
- Tema Oluşturucu kapatılırken yarım kalmış hover durumu geri alınıyor.

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

## Instances sayfası yenilendi (v52.3)

- Instance kartları artık geniş (252x112): ikon karosu, ad, loader · sürüm ve toplam oynama süresi.
- Aktif instance yeşil nokta + vurgulu çerçeveyle işaretli; hover'da kart hafifçe yükselir.
- Kartlar pencere genişliğine göre **satır atlıyor** (`WrapLayout` + `ScrollGrid`), yatay kaydırma yok.
- Düzenle düğmesi sağ alt köşede ayar ikonu (font glifi yerine vektör ikon).
- Sayfa içindeki tekrar eden "Instances" başlığı kaldırıldı (üst çubukta zaten var).

## İçerik sayfaları yenilendi (Mods / Shaders / Resource Packs / Modpacks)

- Kartlar: ince çerçeveli, daha yuvarlak (18px) ve daha geniş iç boşluklu yüzeyler.
- Liste satırları (`RowPanel`): yuvarlatılmış satır, seçili satırda vurgu rengi + sol çubuk; aranan/kurulu mod, shader, resource pack ve modpack listelerinin hepsinde ortak.
- Arama düğmeleri kesilmiyor (Shaders/Resource Packs "…" → "Search"); Mods'ta "Sasirt Beni" → "Rastgele/Random".
- Sabit "SansSerif" fontları tema fontuna (`Theme.uiFont`) çevrildi; "⬇" glifi "↓" yapıldı (bazı sistemlerde kutu çıkıyordu).

## Büyük görsel güncelleme (v52.4)

- **Ana Sayfa yeniden tasarlandı:** iki sütun. Solda hero kartı (izometrik blok sanatı), "Son instance'lar" şeridi (tek tıkla instance değiştirir), oyun ayarları. Sağda hesap, istatistik kutuları (toplam süre / instance sayısı / aktif mod) ve konsol.
- **Ctrl+K komut paleti:** sayfalara, instance'lara ve "Oyunu başlat" eylemine hızlı geçiş; kenar çubuğunun üstündeki arama kutusuyla da açılır.
- **Boş durum ekranları:** Mods / Shaders / Resource Packs / Modpacks arama listeleri boşken ikon + yönlendirici metin gösterir (siyah boş kutu yok).
- Yeni dosyalar: `EmptyList`, `CommandPalette`, `RowPanel`, `WrapLayout`, `ScrollGrid`; `Cards`'a `paintCube` ve `StatTile`; `Icons`'a `SEARCH`.

## Ayarlar ve Hesaplar yenilendi (v52.5)

- **Ayarlar:** uzun form artık bölümlere ayrıldı (Java & Performans, Görünüm, Genel, Yedek & Modlar, Araçlar); her bölümün başlığı ve alt açıklaması var. Satır hizaları düzeltildi, sayfa artık yatay kaydırma çıkarmıyor (`TrackWidthPanel`). Ayar arama kutusu eskisi gibi çalışır.
- **Hesaplar:** avatarlı satırlar, Microsoft/Çevrimdışı etiketi, aktif hesapta yeşil halka + "AKTİF" rozeti, hesap yokken boş durum ekranı (`AccountRowRenderer`).

## Minecraft temalı Ana Sayfa + konsol çevirisi (v52.6)

- **Konsol/ilerleme İngilizce:** `LogI18n` sözlüğü; dil İngilizce iken tüm konsol ve ilerleme metinleri (giriş, indirme, Forge/Fabric/OptiFine kurulumu, oyun başlatma, mod/shader/doku paketi işlemleri) çevrilir. Türkçe seçiliyken metinler aynen kalır. Ana sayfadaki "Oynanmamış / Toplam süre / Geçmiş / Snapshot" etiketleri de dile uyar.
- **Minecraft referansları (`McArt`, tamamen kodla çizilir):** hero'da piksel dokulu izometrik bloklar (loader'a göre çimen/elmas cevheri/altın/TNT), RAM'i gösteren **kalp (can) barı**, **XP çubuğu** stilinde ilerleme göstergesi, Son instance'lar için **hotbar slotu** + blok ikonu (aktif olan beyaz çerçeveli), istatistik kutularında elmas/kazma/creeper sprite'ları.
- **Ayarlar:** üstte bölüm atlama çubuğu (Java & Performans / Görünüm / Genel / Yedek & Modlar / Araçlar).

## Blok dokuları düzeltildi (v52.7)

- Elmas/altın cevheri: rastgele kareler yerine elle yerleştirilmiş düzensiz damarlar (açık/ana/gölge tonlu, taşa oturan ince koyu kenar).
- TNT: dinamit çubuklu kırmızı yan yüzler, ortada beyaz bant ve "TNT" harfleri, üst yüzde fitil plakası.
- Taş: orta gri zemin üzerinde seyrek açık/koyu benekler. (Hepsi `McArt` içinde kodla üretilir.)
