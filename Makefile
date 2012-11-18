

icons: drawable/launcher.png drawable-hdpi/launcher.png drawable-xhdpi/launcher.png

drawable/launcher.png: launcher.svg
	mkdir -p res/drawable
	convert -background Transparent launcher.svg -resize 62x62 -bordercolor Transparent -border 4x6 \( +clone -background Transparent -shadow 60x1+0+2 \) +swap -layers merge +repage res/drawable/launcher.png
	convert -extract 72x72+0+0 +repage res/drawable/launcher.png res/drawable/launcher.png

drawable-hdpi/launcher.png: launcher.svg
	mkdir -p res/drawable-hdpi
	convert -background Transparent launcher.svg -resize 93x93 -bordercolor Transparent -border 6x9 \( +clone -background Transparent -shadow 80x1+0+2 \) +swap -layers merge +repage res/drawable-hdpi/launcher.png
	convert -extract 108x108+0+0 +repage res/drawable-hdpi/launcher.png res/drawable-hdpi/launcher.png

drawable-xhdpi/launcher.png: launcher.svg
	mkdir -p res/drawable-xhdpi
	convert -background Transparent launcher.svg -resize 126x126 -bordercolor Transparent -border 8x12 \( +clone -background Transparent -shadow 80x2+0+4 \) +swap -layers merge +repage res/drawable-xhdpi/launcher.png
	convert -extract 144x144+0+0 +repage res/drawable-xhdpi/launcher.png res/drawable-xhdpi/launcher.png