package test

import dataClasses.Elevation
import dataClasses.Location
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

fun parseGPX(path: String): List<Location> {
    val locations = mutableListOf<Location>()
    fun readXml(): Document {
        val xmlFile = File(path)
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val xmlInput = InputSource(StringReader(xmlFile.readText().trim().replaceFirst("^([\\W]+)<", "<")))
        val doc = dBuilder.parse(xmlInput)
        return doc
    }

    val doc = readXml()

    /*
    <?xml version="1.0"?>
    <gpx version="1.0" creator="Viking -- http://viking.sf.net/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns="http://www.topografix.com/GPX/1/0"
    xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd">
    <trk hidden="hidden">
    <name>Labe - Lysa - Mismatch</name>
    <trkseg>
    <trkpt lat="50.168174149203963" lon="14.855387187347411">
    </trkpt>
    */

    val locationNodeAtributes = listOf("rtept","trkpt")

    var atributesCount = 0
    while(locations.isEmpty()) {
        val nodeListTrckpt: NodeList = doc.getElementsByTagName(locationNodeAtributes[atributesCount])//trkpt
        for (i in 0 until nodeListTrckpt.length) {
            val node = nodeListTrckpt.item(i)
            val nodeElem = node as Element
            val latS = nodeElem.getAttribute("lat")
            val lonS = nodeElem.getAttribute("lon")
            val eleElem = nodeElem.getElementsByTagName("ele")
            val eleNode = eleElem.item(0);
            val eleS = eleNode.textContent
            val eleD = eleS.toDouble()
            val latD = latS.toDouble()
            val lonD = lonS.toDouble()
            locations.add(Location(latD, lonD, Elevation.Value(eleD)))
        }
        atributesCount ++
    }
    return locations
}