# Complex Launcher v61

Shaderlar, Doku Paketleri ve Modpacklar sayfalarinin fare ile gezinme testi
sonucu bulunan gorsel hatalar giderildi.

## Duzeltilen hatalar

- **Sag panelin alt blogu (bilgi + Kaldir / Klasoru Ac) ilk acilista gorunmuyordu.**
  Shader ve Doku Paketleri sayfalarinda sag panel bir `JLayeredPane` icinde
  duruyor ve cocuklarinin `bounds` degeri yalnizca `componentResized` olayinda
  veriliyordu. Bu yuzden sayfa acildiginda alt blok yerlesmiyor, ancak bir fare
  hareketi repaint tetikleyince ortaya cikiyordu (sayfa "sonradan kayiyor" gibi
  gorunuyordu). Katmanli pane artik `doLayout()` icinde cocuklarini her
  yerlesimde kapliyor; blok sayfa acilir acilmaz dogru yerde.

- **Bilgi metni dolduktan sonra layout tazelenmiyordu.** Ayni bloktaki klasor
  yolu/metin sonradan yazildigi icin panelin tercih edilen yuksekligi degisiyor
  ama `revalidate` cagrilmiyordu; metin set edildikten sonra `revalidate()`
  eklendi. ShaderPanel'de bilgi paneli hem ana panele hem alt sarmalayiciya
  ekleniyordu (mukerrer ekleme) - temizlendi.

- **Uzun klasor yolu kelime ortasindan kiriliyordu.** Tooltip'te
  `...\instances\survival\shaderpacks` gibi bosluksuz bir yol
  `shaderpac / ks` seklinde bolunuyordu. Yeni `TextWrap.breakPath` yardimcisi
  yol ayiricilarindan sonra sifir genislikli bosluk koyuyor; satir artik klasor
  sinirlarinda kiriliyor.

- **Yazim hatasi:** "Otomatik kurulacak shader ayarlanmamis" -> "ayarlanmamış"
  (shader ve doku paketi panellerinde).

## Dogrulama

- Derleme temiz, uygulama aciliyor, log'da exception yok.
- Shaderlar ve Doku Paketleri sayfalari acilir acilmaz alinan ekran goruntusu
  ile fare listelerin uzerinde gezdirildikten sonraki goruntu **birebir ayni**
  (md5 esit): artik hover sirasinda panel yer degistirmiyor / blok sonradan
  belirmiyor.
- Modpacklar sayfasi icin arama sonucu listesi bu ortamda dolmadigi icin
  (CurseForge API anahtari yok) yalnizca bos durum ve yerel ice aktarma karti
  dogrulandi; listede ayni tooltip mekanizmasi kullaniliyor.
