package com.dynamo.bob.pipeline;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**

.tmx:
<map version="1.5" tiledversion="1.7.2" orientation="orthogonal" renderorder="right-down" width="100" height="100" tilewidth="16" tileheight="16" infinite="0" nextlayerid="2" nextobjectid="1">
 <tileset firstgid="1" source="grottoescape.tsx"/>
 <layer id="1" name="Tile Layer 1" width="100" height="100">
  <data encoding="csv">


.tsx:
<tileset version="1.5" tiledversion="1.7.2" name="grottoescape" tilewidth="16" tileheight="16" tilecount="256" columns="16">
 <grid orientation="orthogonal" width="16" height="16"/>
 <image source="tiles.png" width="128" height="128"/>
*/
public class Tmx {

    private static Element parse(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        return builder.parse(is).getDocumentElement();
    }

    private static Element getFirstDescendant(Element e, String name) {
        return (Element)e.getElementsByTagName(name).item(0);
    }

    private static String getElementValue(Element e) {
        return e.getChildNodes().item(0).getNodeValue();
    }

    private static int getMapAttributeAsInt(String xml, String attribute) throws IOException {
        try {
            Element map = parse(xml);
            String value = map.getAttribute(attribute);
            return Integer.parseInt(value);
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    private static String getMapAttributeAsString(String xml, String attribute) throws IOException {
        try {
            Element map = parse(xml);
            return map.getAttribute(attribute);
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public static String getTilesetSource(String xml) throws IOException {
        try {
            Element map = parse(xml);
            Element tileset = getFirstDescendant(map, "tileset");
            String tilesetSource = tileset.getAttribute("source");
            return tilesetSource;
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public static String getLayerData(String xml, int index) throws IOException {
        try {
            Element map = parse(xml);
            NodeList layers = map.getElementsByTagName("layer");
            for (int i = 0; i < layers.getLength(); i++) {
                Element layer = (Element)layers.item(i);
                Element data = getFirstDescendant(layer, "data");
                String csv = getElementValue(data);
                if (i == index) {
                    return csv;
                }
            }
            return null;
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public static String getImagePath(String xml) throws IOException {
        try {
            Element tileset = parse(xml);
            Element image = getFirstDescendant(tileset, "image");
            return image.getAttribute("source");
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public static boolean isInfinite(String xml) throws IOException {
        return getMapAttributeAsInt(xml, "infinite") != 0;
    }

    public static String getRenderOrder(String xml) throws IOException {
        return getMapAttributeAsString(xml, "renderorder");
    }
    public static String getOrientation(String xml) throws IOException {
        return getMapAttributeAsString(xml, "orientation");
    }
    public static int getWidth(String xml) throws IOException {
        return getMapAttributeAsInt(xml, "width");
    }
    public static int getHeight(String xml) throws IOException {
        return getMapAttributeAsInt(xml, "height");
    }

    public static int getTileWidth(String xml) throws IOException {
        return getMapAttributeAsInt(xml, "tilewidth");
    }
    public static int getTileHeight(String xml) throws IOException {
        return getMapAttributeAsInt(xml, "tileheight");
    }

    public static String getGreeting() {
        return "Hello";
    }
}
