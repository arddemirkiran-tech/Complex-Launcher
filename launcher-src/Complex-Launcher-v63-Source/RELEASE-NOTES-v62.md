# Complex Launcher v62

Shaderlar, Doku Paketleri ve Modpacklar sayfalarinin fare ile gezinme testinde
kalan gorsel bozukluklar giderildi.

## Duzeltilen hatalar

- **Modpacklar sayfasi bos aciliyordu.** Sayfa varsayilan olarak CurseForge
  kaynaginda basliyordu ama derlemede gomulu CurseForge API anahtari bos; bu
  yuzden sorgu hata donuyor ve liste sessizce bos kaliyordu. Sayfa artik
  Modrinth ile basliyor (anahtar gerektirmez), acilista populer modpack'lerle
  doluyor. Kullanici CurseForge'a gecip anahtar yoksa launcher tekrar Modrinth'e
  donup bunu bildiriyor (eskiden sessiz bos liste vardi).

- **Modpack aramasinda canli arama ve ESC yoktu.** Yazarken 450 ms sonra
  otomatik arama, ESC ile temizleme ve kaynak degistirince o kaynagin
  populerlerini getirme eklendi (mod/shader/doku sayfalariyla ayni davranis).

- **Liste aciklamalari cumle ortasinda uc noktasiz kesiliyordu.** Yeni
  `EllipsisLabel`, metni satirin gercek genisligine gore "..." ile kirpiyor;
  boylece modpack/shader/doku sonuc listelerinde aciklama yarim kelimede
  bitmiyor. Ayni degisiklik listenin altinda beliren gereksiz **yatay kaydirma
  cubugunu** da kaldirdi (etiketin tercih ettigi genislik viewport'tan buyuktu).

- **Klasor yolu hala bozuk bolunuyordu.** v61'deki sifir genislikli bosluk
  yontemi Swing metin alaninda ise yaramadi: yol `...\shaderpack` + `s` diye
  ikiye ayriliyordu. Bilgi blogu etiketlerle yeniden kuruldu; uzun yol tek
  satirda **ortadan** kirpilip sonu gorunur kaliyor
  (`Klasor: ...\instances\survival\shaderpacks`).

- **Turkce karakterler:** "Secili Modpack'i Kur", "Yerel Ice Aktar",
  "Bilgisayarinizdan ... secerek ... ice aktarabilirsiniz" ve kaynak secici
  ipucu artik dogru Turkce karakterlerle ve Ingilizce secenegiyle gosteriliyor.

## Dogrulama

- Derleme temiz (`javac --release 17`), uygulama **Complex Launcher v62**
  olarak aciliyor, log'da exception yok.
- Modpacklar sayfasi acilista 30 populer modpack ile doluyor (ekran goruntusu).
- Uc sayfada da acilis ve fare gezinme ekran goruntuleri **birebir ayni**
  (md5 esit): hover sirasinda panel yer degistirmiyor, blok sonradan belirmiyor.
- Modpack indirme/kurulum akisi bu ortamda calistirilmadi (arama ve listeleme
  dogrulandi, kurulum degil).
