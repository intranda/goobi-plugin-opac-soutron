goobi_opac.xml configuration:
each document type has to be mapped as a <mapping> element:

        <type isContainedWork="false" isMultiVolume="false" isPeriodical="false" rulesetType="Monograph" tifHeaderType="Monographie" title="monograph">
            <label language="de">Monographie</label>
            <label language="en">Monograph</label>
            <mapping>Monograph</mapping>
        </type>

catalogue:

    <catalogue title="Wiener">
        <config description="Wiener Library" address="http://wiener.soutron.net/Library/WebServices/SoutronAPI.svc/GetCatalogue" port="80" database="2.1" iktlist="IKTLIST-GBV.xml" ucnf="XPNOFF=1" opacType="SoutronImport" />
    </catalogue>
    
    
/opt/digiverso/goobi/config/config_soutron.xml
    
    