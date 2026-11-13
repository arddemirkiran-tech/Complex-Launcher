# Complex Launcher v57

## Kesilen yazilar duzeltildi (sonuna "..." eklenen metinler)

Panel dar oldugunda yazilar kesiliyor ve Swing etiketin sonuna "..." ekliyordu. Artik:

- **Maks Performans paneli yeniden duzendi.** Baslik solda, ACIK/KAPALI anahtari sagda kendi olcusunde (yazinin genisligine gore hesaplaniyor, "KAPALI" de artik sigiyor). Aciklama metni sabit satir sonlari yerine panel genisligine gore sariliyor. Durum satiri ("Acik - 22/48 mod kurulu") kendi satirinda ve tam genislikte. "Yeniden kur / Modlari kaldir / JVM bayraklari" butonlari satir atlayan yerlesime gecti; dar pencerelerde ust uste binmiyor.
- **Sunucu kartlarindaki butonlar:** "Oyna / Modlar / Duzenle / Sil" sabit 50-70 px yerine yazi olcusune gore boyutlandigi icin "Duzenle" gibi uzun etiketler kesilmiyor.
- **Yerel Ice Aktar aciklamasi** (Modpack'ler sekmesi) sabit genislikli HTML yerine kartin genisligine gore sariliyor.

Uzun aciklamalar icin ortak bir `WrapLabel` bileseni eklendi; ileride eklenecek metinler de kesilmeden sarilir.

## Not
Metinlerin sonundaki "..." artik yalnizca gercek "devam ediyor" anlaminda kullaniliyor (Yukleniyor gibi); kesilme kaynakli "..." olusmuyor.
