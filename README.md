# GaunCraftPack

Resource pack'i oyuncular sunucuya girince otomatik gonderen Paper eklentisi.

## Kurulum
1. Bu repo **public** olmali (private olursa oyuncular pack'i indiremez).
2. `main` dalina push edince GitHub Actions calisir ve jar'i derler.
3. Repo > **Releases** > `GaunCraft Pack (son surum)` icinden `GaunCraftPack.jar` indir.
4. Sunucudaki `plugins/` klasorune at, sunucuyu yeniden baslat.

## Pack'i guncelleme
`pack/GaunCraft-Pack.zip` dosyasini yenisiyle degistirip commit et. Actions yeniden derler,
sha1 otomatik guncellenir, oyuncular yeni pack'i indirir.
Zip'in icinde `pack.mcmeta` dogrudan en ustte olmali.

## Komutlar
- `/gcpack reload`
- `/gcpack send [oyuncu]`
Yetki: `gauncraftpack.admin`
