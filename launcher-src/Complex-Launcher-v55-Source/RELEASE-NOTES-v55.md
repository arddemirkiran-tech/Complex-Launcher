# Complex Launcher v55

## Maks Performans modu — yeni kontroller

Panelin altında artık canlı bir durum satırı ve üç kontrol var:

- **Durum:** `Açık - 42/48 mod kurulu` / `Kapalı - bu moda ait 42 mod kurulu` gibi kurulu seti gösterir.
- **Yeniden kur:** Performans mod setini bu instance için baştan kurar.
- **Modları kaldır:** Bu modun kurduğu jar'ları (registry kaydıyla birlikte) siler.
- **JVM bayrakları:** Modun eklediği GC/JIT bayraklarını ve hangi RAM/Java sürümü için üretildiğini gösterir.

Kurulan modlar `.maxperf-set.json` işaret dosyasına yazılır; durum satırı ve "Modları kaldır" bu listeyi kullanır, böylece elle kurulmuş modlara dokunulmaz.

## Açma animasyonu ve indirme yüzdesi

Maks Performans'ı açtığınızda arayüzün üzerinde yumuşak bir geçişle bir katman belirir: büyük yüzde göstergesi, animasyonlu ilerleme çubuğu ve o an kurulan modun adı (`[7/48] FerriteCore`) görünür. Kurulum bitince yüzde 100'e tamamlanır ve katman kendiliğinden sönümlenir.

## Instance değişince otomatik kapanma

Maks Performans artık instance'a bağlıdır. Başka bir instance'a geçince (liste, hızlı geçiş, yeniden adlandırma veya instance silme) mod otomatik olarak **KAPALI** konumuna döner ve ayar kaydedilir; böylece bir instance'ın performans mod seti diğerine taşınmaz, aç/kapa yapmanız gerekmez. Devam eden kurulum da iptal edilir.

## Kalan "?" işaretleri

Yazı tiplerinin çizemediği karakterler (emoji ve bazı oklar) ekranda kutu/soru işareti olarak çıkıyordu. Bu sürümde:

- Geri bildirim kategorileri, çökme mesajı ve konsol uyarısındaki simgeler çizilebilir metinle değiştirildi.
- Modrinth'ten gelen mod/shader/doku paketi adları-açıklamaları ve sunucu MOTD'si, gömülü fontların çizemediği karakterlerden arındırılıyor; Türkçe karakterler aynen korunuyor.
