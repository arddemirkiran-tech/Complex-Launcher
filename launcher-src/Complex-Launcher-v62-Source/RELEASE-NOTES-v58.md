# Complex Launcher v58

## Instance'lar sekmesi

- **Kartlar cogalmiyor / ust uste binmiyor.** Kart izgarasi bazi yerlerden (modpack kurulumu, maks performans, instance silme/olusturma) arka plan thread'inden yenileniyordu; Swing bilesenleri EDT disinda degistirilince hayalet kartlar kaliyordu. Artik tazeleme her zaman arayuz thread'inde ve arka arkaya gelen istekler tek bir yenilemede birlestirilerek yapiliyor.
- **Ayni instance iki kez listelenmiyor:** isimler tekillestirilerek ekleniyor.
- **Hover animasyonu sizintisi giderildi.** Her fare girisinde yeni bir Swing `Timer` yaratiliyor, liste yenilendiginde eski kartlarin timer'lari durdurulmadigi icin gorunmeyen kartlar saniyede 60 kez yeniden ciziliyordu (donma/titreme). Artik kart basina tek timer kullaniliyor.

## Modlar sekmesi

- **Cift satir yok.** Ayni jar icin birden fazla kayit (eski kurulumlardan kalan) ve diskte olmayan "hayalet" kayitlar listeden eleniyor.
- **Secim korunuyor.** Liste her tazelendiginde secim sifirlaniyor, "Ac/Kapat" yanlis moda uygulanabiliyordu; artik secili modlar dosya adina gore geri secilir.
- **Titreme azaltildi:** kurulu mod listesi degismediyse model bosaltilip yeniden doldurulmuyor.
- Bos liste yer tutucusu ("Henuz mod yok") artik gercek bir mod gibi kaldirilamiyor/acilip kapatilamiyor.

## Modlu oyunlarda JSON/TOML uyumluluk kontrolu (Forge + Fabric)

- **Forge/NeoForge jar'lari da denetleniyor.** Eskiden yalnizca Fabric'in `fabric.mod.json` dosyasi okunuyordu; Forge/NeoForge jarlari tamamen atlaniyordu. Artik `META-INF/mods.toml` ve `META-INF/neoforge.mods.toml` icindeki mod kimligi, surum ve **zorunlu bagimliliklar** okunuyor.
- **Surum araliklari Forge bicimiyle de dogru karsilastiriliyor:** `[1.20,1.21)`, `[1.20,)`, `(,1.21]`, `[1.20.1]` ve `"[46,47.1.3),[47.1.43,)"` gibi coklu araliklar; `1.20.1-14.4.4` gibi MC-onekli surumler ve `${file.jarVersion}` gibi cozumlenemeyen surumler yanlis pozitif uretmiyor.
- **Yanlis yukleyici tespiti:** Fabric instance'inda Forge/NeoForge jari veya Forge instance'inda Fabric jari varsa raporlanir (yukleyici onlari goremedigi icin diger modlara "bagimlilik eksik" gibi gorunur).
- **Kontrol artik yalnizca Maks Performans acikken degil, MODLU her acilista** (Fabric / Forge / NeoForge / Quilt) oyun baslamadan once calisir; sonuc konsola yazilir, uyumsuz jar silinmez sadece `.disabled` yapilir (Modlar sekmesinden geri acilabilir).
- Kontrol her acilista jar'lari bastan okumaz: `mods/.compat-ready` parmak izi ayniysa tarama atlanir.

Gercek bir Forge instance'inda (14 jar) test edildi: 14/14 jar'in metadatasi okundu, yanlis uyumsuzluk raporlanmadi.
