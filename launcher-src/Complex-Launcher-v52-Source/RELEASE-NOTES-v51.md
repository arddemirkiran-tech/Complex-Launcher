# Complex Launcher v51

## Crash fix: "Incompatible mods found"

v50'te Maks Performans, Modrinth'in proje filtresine güveniyordu: "bu jar bu MC
sürümü için var mı?" Evet diyordu, ama "bu jar diğer modlarla birlikte yüklenir
mi?" sorusunu kimse sormuyordu. Senin aldığın hata tam olarak buydu:

```
More Culling 1.0.11 requires cloth-config >= 15.0.0 (missing)
More Culling 1.0.11 is incompatible with Sodium <= 0.6.13 (0.6.13 installed)
Particle Core 0.3.3 requires fzzy_config >= 0.7.5 (missing)
```

Üç ayrı kök neden vardı ve üçü de düzeltildi:

1. **Yanlış loader jar'ları.** O Fabric instance'ında
   `cloth-config-15.0.140-neoforge.jar`, `lithium-neoforge`,
   `entityculling-neoforge`, `fzzy_config-...-neoforge` duruyordu. Fabric bu
   jar'ları görmez, ama diğer modlar onları bağımlılık olarak listeler — bu
   yüzden "cloth-config eksik" diyordu, oysa dosya oradaydı.
   Artık sürüm seçimi **loader zorunlu**: `ModrinthApi.pickBestVersion` aynı MC
   sürümü için fabric ve neoforge build'i varsa yalnızca instance'ın loader'ına
   uyanı seçer.

2. **Jar içi bağımlılık doğrulaması yoktu.** Yeni `ModCompatibility`, her jar'ın
   içindeki `fabric.mod.json` dosyasını okuyup `depends` / `breaks` alanlarını
   kurulu jar'larla karşılaştırır (sürüm aralıkları dahil: `>=`, `<=`, `~`, `^`,
   `x`). Fabric API gibi bundle jar'ların `META-INF/jars/` altındaki 49 alt
   modülünü de "sağlanan" olarak sayar, böylece uydurma hata üretmez.

3. **Uyumsuz set oyunu açmıyordu.** Oyun başlamadan önce (ve Maks Performans
   kurulumunun sonunda) kontrol çalışır; uyumsuz jar'lar **silinmez**, `.disabled`
   olarak yeniden adlandırılır ve Modlar sekmesinden geri açılabilir.

Doğrulama, senin bozuk instance'ının bir kopyası üzerinde yapıldı:
`10 problem` tespit edildi → `9 jar` devre dışı → kalan sette `0 problem`.

## Kaldırıldı: Vulcan Mode

Vulkan Modu tamamen silindi (sınıf, ayar alanı, ayarlar sekmesindeki bölüm ve
launch çağrıları). Launcher artık kendi başına hiçbir renderer/mod indirmiyor.

## GUI: hover glitch'i

Tooltip her `mouseMoved` olayında yeniden kuruluyordu; Swing her çağrıda yeni bir
tooltip penceresi açtığı için imleç gezerken tooltipler üst üste binip satırlar
"dupeleniyor" gibi görünüyordu. Artık tooltip yalnızca **satır değişince**
güncellenir, boş alanda ve listeden çıkışta temizlenir. Mods, Modpacks, Shaders
ve Resource Packs panellerinde uygulandı.

## Sürüm

`-Dminecraft.launcher.version=51.0.0`, `UpdateManager.CURRENT_VERSION = "51"`.

## Bilinen sınırlar

- Forge/NeoForge jar'ları uyumluluk denetiminde **doğrulanmaz** (metadata TOML
  formatında); yalnızca loader uyuşmazlığı kontrol edilir.
- Hover düzeltmesi derlendi ve kod incelemesiyle doğrulandı; fare etkileşimi
  gerektiği için otomatik görsel test yapılmadı.
- Linux paketinde Rust çekirdeği için ikili yok (Windows'tan cross-compile
  edilmedi); Java yolu kullanılır.
