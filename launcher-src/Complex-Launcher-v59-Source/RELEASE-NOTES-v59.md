# Complex Launcher v59

## Modlar sayfasinda fare gezdirince olusan bozulma/ciftlenme giderildi

- **Kok neden:** kurulu mod listesi ve arama listesi, her `mouseMoved` olayinda `setToolTipText(...)` cagiriyordu. Swing'in `ToolTipManager`'i her cagrida tooltip penceresini yeniden kurar; modlar varken fare satirlar arasinda gezindiginde ust uste binen, ekranda asili kalan tooltip pencereleri olusuyor ve liste "dupeleniyor / bozuluyor" gibi gorunuyordu. Mod yoksa satir da olmadigi icin sorun yalnizca modlar varken ortaya cikiyordu.
- **Cozum:** programatik goster/gizle kaldirildi. Tooltip artik Swing'in kendi mekanizmasiyla, `JList.getToolTipText(MouseEvent)` uzerinden fare konumuna gore uretiliyor (yeni `ListTips` yardimcisi). Boylece tooltip penceresi tek bir kez yonetilir; satirlar arasinda gezinmek liste cizimini etkilemez.
- Bos alan ve "Henuz mod yok" yer tutucusu icin tooltip dondurulmuyor (ekranda asili kalan bos kutu olusmuyor).

## Notlar
- Davranis ayni: satirin ustunde mod adi, surum ve dosya adi gorunur; arama sonuclarinda ad + kisa aciklama.
- Arayuz dili ve tema destegi degismedi.
