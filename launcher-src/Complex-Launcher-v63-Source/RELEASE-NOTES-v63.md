# Complex Launcher v63

Oynama suresi kaybolma hatasi giderildi.

## Duzeltilen hatalar

- **Oynama suresi hic kaydedilmiyordu.** Sure sadece oyun kapaninca yaziliyordu;
  kullanici launcher'i oyun acikken kapatirsa (en yaygin kullanim) `onGameExit`
  hic cagrilmiyor ve sure kayboluyordu. Instance kartlari sonsuza kadar
  "Henuz oynanmadi" gosteriyordu.

  Cozum: oyun baslarken instance klasorune `.play_session` oturum isaretcisi
  yaziliyor. Launcher acilirken bu isaretciler taranip gecen sure instance'a
  ekleniyor (oturum kapanmadan kapanan launcher icin kurtarma). Launcher
  kapanirken de yarim kalan oturumun suresi yaziliyor. Sure hesaplamasi
  oyun cikisinda olculen deger ile isaretciden hesaplanan degerin buyugunu
  aliyor.

- **Instance kartinda kisa sure "0h 46m" diye gosteriliyordu.** Artik 1 saatten
  kisa sureler "46m" olarak gosteriliyor.

## Teknik

- Yeni `core/PlayTimeTracker.java` (begin/end/elapsedSeconds/recoverAll/
  finishRunningSessions).
- `MainWindow`: constructor'da recoverAll + windowClosing hook; launch
  thread'inde begin; handleGameExit icinde PlayTimeTracker.end.
