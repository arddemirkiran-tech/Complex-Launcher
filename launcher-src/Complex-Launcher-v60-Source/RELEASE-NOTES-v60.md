# Complex Launcher v60

Bu surum, v59'daki tooltip duzeltmesinin diger sayfalara uygulanmasi ve kod
taramasinda bulunan diger hatalarin giderilmesidir.

## Duzeltilen hatalar

- **Shader / Doku Paketi / Modpack arama listelerinde fare gezinirken tooltip yigilmasi.**
  v59'da modlar listesinde duzeltilen sorun bu uc sayfada da aynen vardi: her
  `mouseMoved` olayinda `setToolTipText` cagriliyor, Swing her cagrida tooltip
  penceresini yeniden kuruyordu (ust uste binen / asili kalan kutular, liste
  "ciftleniyor" gorunumu). Dinleyiciler tamamen kaldirildi; tooltip artik
  Swing'in kendi `getToolTipText(MouseEvent)` mekanizmasiyla, ortak `ListTips`
  uzerinden uretiliyor. Shader ve doku paketi sonuclari icin de baslik +
  aciklama tooltip'i eklendi.

- **Mod ac/kapat komutu yanlis dosya adi hesapliyordu.** Dosya adi zaten
  `.disabled` ile bitiyorsa ikinci kez `.disabled` ekleniyor (`x.jar.disabled.disabled`),
  dosya bulunamadigi icin hicbir sey yeniden adlandirilmiyor ama kayit "devre
  disi" yaziliyordu: mod oyunda yuklenmeye devam ediyor, launcher kapali
  gosteriyordu. Ayrica `renameTo` sonucu yok sayildigi icin dosya kilitliyken
  (oyun acik) de ayni yanlis durum kaydediliyordu. Artik ad normalize ediliyor,
  gerekirse `Files.move` ile tekrar deneniyor ve basarisiz olursa kullaniciya
  "dosya kullanimda" uyarisi gosteriliyor.

- **ZIP ice aktarma yol kontrolu kardes klasore izin veriyordu.** `safeResolve`
  yalnizca `startsWith(basePath)` bakiyordu; `/instances/foo` oneki
  `/instances/foo-bar` ile de eslestigi icin arsivdeki `../foo-bar/...` yolu
  baska bir instance klasorune yazabiliyordu. Kanonik yol + ayirici karakter
  kontrolune gecildi (instance import ve modpack kurulumu).

- **Guncelleme surum karsilastirmasi cokebiliyordu.** `"1..2"` gibi surum
  etiketlerinde bos parca `NumberFormatException` atiyordu; surum parcalari
  artik guvenli ayrıştiriliyor.

## Dogrulama

- Derleme temiz (`javac --release 17`), uygulama aciliyor, log'da exception yok.
- Modlar sayfasi mod yukluyken fare gezdirme testi: ekran goruntuleri arasinda
  ciftlenme / bozulma yok.
- ZIP yol kontrolu birim testi: normal yollar kabul, `../` ve kardes klasor
  yollari reddediliyor.
