SWEET

2010-08-09

Tests:

# generate list of sweetAll.owl's imported ontologies in worlspace/sweet/importedOnts.txt:
$ mvn -e exec:java -Dexec.args="Sweet --listImported importedOnts.txt --ws workspace/sweet"
Sweet] Listing 177 imported sweet ontology URIs to workspace/sweet/importedOnts.txt

# Register ALL those ontologies
$ mvn -e exec:java -Dexec.args="Sweet  --ws workspace/sweet --register @importedOnts.txt --password XXXXXX"
...
OK for the most part (about 163 ontologies).  What happened with the others? ... AH! The ones below where 
not detected as in UTF-8 for some reason.  But firefox and jEdit seem to open them in UTF-8 just fine!

http://sweet.jpl.nasa.gov/2.0/biolPlant.owl (9494)
    verifyUtf8: Detected charset: windows-1252
    
http://sweet.jpl.nasa.gov/2.0/geolTectonics.owl (20447)
    verifyUtf8: Detected charset: windows-1252
    
http://sweet.jpl.nasa.gov/2.0/geolPetrology.owl (10776)
    verifyUtf8: Probable charsets: [EUC-KR, windows-1252, EUC-JP, Shift_JIS, GB2312, UTF-8, GB18030, x-euc-tw, Big5]
    
http://sweet.jpl.nasa.gov/2.0/geolVolcano.owl (13982)
    verifyUtf8: Probable charsets: [EUC-KR, windows-1252, EUC-JP, Shift_JIS, GB2312, UTF-8, GB18030, x-euc-tw, Big5]
    
http://sweet.jpl.nasa.gov/2.0/cryo.owl (9776)
     verifyUtf8: Detected charset: windows-1252   
     
http://sweet.jpl.nasa.gov/2.0/astroStar.owl (6384)
     verifyUtf8: Probable charsets: [EUC-KR, windows-1252, EUC-JP, Shift_JIS, GB2312, UTF-8, GB18030, x-euc-tw, Big5] 

http://sweet.jpl.nasa.gov/2.0/oceanCirculation.owl (11843)
     verifyUtf8: Detected charset: windows-1252

http://sweet.jpl.nasa.gov/2.0/physFluidInstability.owl (9377)
    verifyUtf8: Detected charset: windows-1252

http://sweet.jpl.nasa.gov/2.0/geol.owl (13880)
    verifyUtf8: Detected charset: windows-1252

http://sweet.jpl.nasa.gov/2.0/landLandform.owl (8811)
    verifyUtf8: Probable charsets: [windows-1252, Shift_JIS]

http://sweet.jpl.nasa.gov/2.0/geolMineral.owl (6198)
     verifyUtf8: Probable charsets: [windows-1252, Shift_JIS]

http://sweet.jpl.nasa.gov/2.0/geolEarthquake.owl (7797)
     verifyUtf8: Detected charset: windows-1252

http://sweet.jpl.nasa.gov/2.0/chemProcess.owl (12026)
    verifyUtf8: Detected charset: windows-1252

