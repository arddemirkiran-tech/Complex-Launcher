# Complex Launcher v56

## Açılış hazırlığı: her loader için ~1 saniye yerine ~50 ms

Ölçüm (bu makinede, 1.20.1, önbellek sıcak):

| Aşama | Forge | Fabric |
|---|---|---|
| Sürüm JSON'u | 31 ms | 33 ms |
| Java kontrolü | 6 ms | 5 ms |
| Loader profili | 5 ms | 15 ms |
| Natives | 4 ms | 3 ms |
| Kaynaklar | 3 ms | 2 ms |
| **Toplam** | **49 ms** | **58 ms** |

Bunun için kaldırılan tekrarlar:

- **Loader'a özel kütüphane işareti** — `.libs-ready` tek dosyaydı; aynı MC sürümünde Vanilla ↔ Fabric ↔ Forge geçişinde parmak izi değişip ~60 MB / 79 JAR her seferinde yeniden SHA-1 ile doğrulanıyordu (ölçülen: 108 ms + disk). Artık `.libs-ready-<loader>` ayrı tutuluyor, geçişte doğrulama atlanıyor.
- **İstemci JAR işareti** — 23 MB'lık `1.20.1.jar` her açılışta baştan hash'leniyordu (ölçülen: 16-67 ms). Boyut+mtime parmak izi `.client-ready` dosyasında; iz aynıysa hash yok.
- **Uyumluluk taraması önbelleği** — Maks Performans açıkken her açılışta tüm mod JAR'ları tek tek açılıp metadata okunuyordu (22 mod: 51 ms soğuk; 100+ modda saniyeler). `mods/.compat-ready` parmak izi değişmediyse tarama tamamen atlanıyor.
- **Aşama süreleri loglanıyor** — konsolda `[Hız] Sürüm bilgisi: 31 ms`, `[Hız] Loader (Forge): 5 ms`, `[Hız] Kütüphaneler: ...` ve en sonda toplam süre görünür; hangi loader'da ne kadar beklediğiniz ölçülebilir.

Not: launcher tarafındaki hazırlık artık ~0,05 saniye; Minecraft penceresinin açılmasındaki geri kalan süre oyunun kendi JVM/LWJGL açılışıdır (mod sayısına bağlıdır, launcher bunu kısaltamaz).
