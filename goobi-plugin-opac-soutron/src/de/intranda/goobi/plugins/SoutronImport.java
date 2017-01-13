/***************************************************************
 * Copyright notice
 *
 * (c) 2016 Robert Sehr <robert.sehr@intranda.com>
 *
 * All rights reserved
 *
 * This file is part of the Goobi project. The Goobi project is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * The GNU General Public License can be found at
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * This script is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * This copyright notice MUST APPEAR in all copies of this file!
 ***************************************************************/

package de.intranda.goobi.plugins;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IOpacPlugin;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.fileformats.mets.MetsMods;
import de.intranda.goobi.configuration.Mapping;
import de.sub.goobi.helper.exceptions.ImportPluginException;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacCatalogue;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;
import lombok.extern.log4j.Log4j;

@PluginImplementation
@Log4j
public class SoutronImport implements IOpacPlugin {

    private static final String pluginName = "SoutronImport";

    private int numberOfHits = 0;

    private static XPathFactory xFactory = XPathFactory.instance();
    private Map<String, String> docstructMap;
    private Map<String, String> metadataMap;
    private Map<String, String> personMap;

    private String gattung;
    private ConfigOpacCatalogue coc;

    public SoutronImport() {

        docstructMap = Mapping.getInstance().getConfiguredDocstructs();

        metadataMap = Mapping.getInstance().getConfiguredMetadataMapping();

        personMap = Mapping.getInstance().getConfiguredPersonMapping();

    }

    @Override
    public PluginType getType() {
        return PluginType.Opac;
    }

    @Override
    public String getTitle() {
        return pluginName;
    }

    public String getDescription() {
        return pluginName;
    }

    @Override
    public Fileformat search(String inSuchfeld, String inSuchbegriff, ConfigOpacCatalogue coc, Prefs prefs) throws Exception {
        this.coc = coc;
        Client client = ClientBuilder.newClient();
        WebTarget url = client.target(coc.getAddress()).queryParam("id", inSuchbegriff);

        Response response = url.request().get();
        if (response.getStatus() != 200) {
            numberOfHits = 0;
            return null;
        }
        String s = response.readEntity(String.class);
        SAXBuilder sb = new SAXBuilder();
        Document doc = sb.build(new StringReader(s));
        Element root = doc.getRootElement();
        if (!root.getName().equals("soutron")) {
            numberOfHits = 0;
            return null;
        }
        numberOfHits = 1;
        Element element = root.getChild("catalogs_view").getChild("ct");
        String documentType = element.getAttributeValue("name");
        String rulesetType = docstructMap.get(documentType);
        gattung = rulesetType;
        MetsMods mm = null;
        try {
            mm = new MetsMods(prefs);

            DigitalDocument digitalDocument = new DigitalDocument();
            mm.setDigitalDocument(digitalDocument);
            DocStructType logicalType = prefs.getDocStrctTypeByName(rulesetType);
            DocStruct logical = digitalDocument.createDocStruct(logicalType);

            digitalDocument.setLogicalDocStruct(logical);
            DocStructType physicalType = prefs.getDocStrctTypeByName("BoundBook");
            DocStruct physical = digitalDocument.createDocStruct(physicalType);
            digitalDocument.setPhysicalDocStruct(physical);
            Metadata imagePath = new Metadata(prefs.getMetadataTypeByName("pathimagefiles"));
            imagePath.setValue("./images/");
            physical.addMetadata(imagePath);

            for (String xpath : metadataMap.keySet()) {
                XPathExpression<Text> expression = xFactory.compile(xpath, Filters.text(), null);
                List<Text> textList = expression.evaluate(element);
                for (Text text : textList) {
                    String value = text.getText();
                    MetadataType mdt = prefs.getMetadataTypeByName(metadataMap.get(xpath));
                    try {
                        Metadata md = new Metadata(mdt);
                        md.setValue(value);
                        logical.addMetadata(md);
                    } catch (MetadataTypeNotAllowedException | DocStructHasNoTypeException e) {
                        log.error("Cannot add metadata " + mdt.getName() + " to docstruct " + logical.getType().getName());
                    }
                }
            }

            for (String xpath : personMap.keySet()) {
                XPathExpression<Text> expression = xFactory.compile(xpath, Filters.text(), null);
                List<Text> textList = expression.evaluate(element);
                for (Text text : textList) {
                    String value = text.getText();
                    String firstname = "";
                    String lastname = "";
                    if (value.contains(",")) {
                        String[] parts = value.split(",");
                        lastname = parts[0];
                        firstname = parts[1];
                    } else {
                        lastname = value;
                    }
                    MetadataType mdt = prefs.getMetadataTypeByName(personMap.get(xpath));
                    Person person = new Person(mdt);
                    try {
                        person.setLastname(lastname);
                        person.setFirstname(firstname);
                        logical.addPerson(person);
                    } catch (MetadataTypeNotAllowedException | DocStructHasNoTypeException e) {
                        log.error("Cannot add metadata " + mdt.getName() + " to docstruct " + logical.getType().getName());
                    }
                }
            }

        } catch (PreferencesException | MetadataTypeNotAllowedException | TypeNotAllowedForParentException e) {
            throw new ImportPluginException(e);
        }
        return mm;
    }

    @Override
    public int getHitcount() {
        return numberOfHits;
    }

    @Override
    public String getAtstsl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConfigOpacDoctype getOpacDocType() {
        ConfigOpacDoctype cod = null;
        try {
            ConfigOpac co = ConfigOpac.getInstance();
//            ConfigOpac co = new ConfigOpac();
            cod = co.getDoctypeByMapping(this.gattung, this.coc.getTitle());
            if (cod == null) {

                cod = co.getAllDoctypes().get(0);
                this.gattung = cod.getMappings().get(0);

            }
        } catch (Exception e) {
            log.error(e);
        }
        return cod;
    }

    @Override
    public String createAtstsl(String value, String value2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAtstsl(String createAtstsl) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getGattung() {
        return gattung;
    }

}
