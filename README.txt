Ovo je mali isecak koda od drugog domaceg zadatka.

Ukoliko niste vicni maven projektima a zelite da iskoristite kod predlazem da iskopirate klase koje se nalazi na putanji
src/main/java

Da bi ovaj primer proradio moracete da skinete biblioteku koja se zove GSON. Ukoliko koristite maven, maven ce ovu biblioteku skinuti za vas.
Download mozete naci na https://search.maven.org/artifact/com.google.code.gson/gson/2.8.5/jar

Paket model sluzi za modele koji ce se koristiti pri komunikaciji. Ako pravite dve odvojene aplikacije onda cete hteti da vam se ove klase nalaze na oba mesta.

Paket server treba da radi nezavisno od paketa client.

Paket client ima sitnu logiku za okrestraciju gde za svaku novu musteriju kreira novi socket.



