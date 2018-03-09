# Import aus dem Soutron Katalog der Wiener Library

## Testaufrufe

Mit den folgenden Testaufrufen kann man den Katalog beispielhaft abfragen:

	http://wiener.soutron.net/Library/WebServices/SoutronAPI.svc/GetCatalogue?id=70561
	http://wiener.soutron.net/Library/WebServices/SoutronAPI.svc/GetCatalogue?id=74797
	http://wiener.soutron.net/Library/WebServices/SoutronAPI.svc/GetCatalogue?id=74798
	http://wiener.soutron.net/Library/WebServices/SoutronAPI.svc/GetCatalogue?id=90073
	http://wiener.soutron.net/Portal/Default/en-GB/RecordView/Index/69950

Einige Beispiele, die Jessica Green einmal herausgesucht hatte:

	CID 86817 (Photo) - http://wiener.soutron.net/Portal/Default/en-GB/RecordView/Index/86817
	CID 87346 (Photo) - http://wiener.soutron.net/Portal/Default/en-GB/RecordView/Index/87346
	CID 88049 (Photo) - http://wiener.soutron.net/Portal/Default/en-GB/RecordView/Index/88049
	CID 88272 (Photo) - http://wiener.soutron.net/Portal/Default/en-GB/RecordView/Index/88272
	CID 66784 (Unpublished memoir) - http://wiener.soutron.net/Portal/Default/en-GB/RecordView/Index/66784
	CID 61228 (Book) - http://wiener.soutron.net/Portal/Default/en-GB/RecordView/Index/61228
	CID 23428 (CD) - http://wiener.soutron.net/Portal/Default/en-GB/RecordView/Index/65696
	CID 65881 (E-resource) - http://wiener.soutron.net/Portal/Default/en-GB/RecordView/Index/65881
	
Ein Testimony Beispiel:

	https://wiener.soutron.net/Portal/Default/en-GB/RecordView/Index/93816
	http://wiener.soutron.net/Library/WebServices/SoutronAPI.svc/GetCatalogue?id=93816
	
Suchanfrage:

	http://wiener.soutron.net/Library/WebServices/SoutronAPI.svc/searchcatalogues?q=law&ctrt=Monograph;Serial;AudioVisual;PhotoCollections

## Konfiguration des Plugins als Katalog

Für den Einsatz des Plugins muss die Opac-Konfiguration geändert werden innerhalb der Datei goobi_opac.xml. Dabei muss jeder Publikationstyp wie folgt (mit <mapping>) konfiguriert werden:

	<type isContainedWork="false" isMultiVolume="false" isPeriodical="false" rulesetType="Monograph" 
		tifHeaderType="Monographie" title="monograph">
		<label language="de">Monographie</label>
		<label language="en">Monograph</label>
		<mapping>Monograph</mapping>
	</type>

Der Katalog selbst wird wie folgt definiert::

	<catalogue title="Wiener">
		<config description="Wiener Library" address="https://wiener.soutron.net/Library/WebServices/SoutronAPI.svc/GetCatalogue"
		port="80" database="2.1" iktlist="IKTLIST-GBV.xml" ucnf="XPNOFF=1" opacType="SoutronImport" />
	</catalogue>
    

## Konfiguration des Mappings 

Sämtliche Mapping-Konfiguration findet in der folgenden Datei statt:
 
	/opt/digiverso/goobi/config/plugin_intranda_opac_soutron.xml
    
    