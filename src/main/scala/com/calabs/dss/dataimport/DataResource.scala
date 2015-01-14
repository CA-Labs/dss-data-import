package com.calabs.dss.dataimport

import java.net.URL

import com.fasterxml.jackson.databind.ObjectMapper
import io.gatling.jsonpath.JsonPath
import org.apache.poi.ss.usermodel.{Cell, Sheet, WorkbookFactory}
import org.dom4j.{DocumentHelper}
import org.json4s.DefaultFormats

import scala.io.{BufferedSource, Source}
import scala.util.{Try}
import scala.collection.mutable.{Map => MutableMap}
import TypeAliases._
import scala.collection.JavaConverters._
import org.json4s.Xml.{toJson}

import scala.xml.XML

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 20/11/14
 */

object TypeAliases {
  type DataSource = String
  type ResourceType = String
  type Codec = String
  type Metric = String
  type MetricPath = String
  type MetricValue = Any
  type ConfigKey = String
  type ConfigValue = Any
  type HeaderKey = String
  type HeaderValue = Any
  type HTTPHeaders = Map[HeaderKey, HeaderValue]
  type XLSXSheet = String
}

object ResourceType {
  val WEBSITE = "website"
  val JSON = "json"
  val JSON_API = "jsonAPI"
  val XML = "xml"
  val XML_API = "xmlAPI"
  val XLSX = "xlsx"
}

case class DataResourceConfig(config: Product)
case class DataResourceMapping(mapping: List[Map[Metric,MetricPath]])

trait DataResourceExtractor {
  def extractDocuments: Try[(List[Document], List[Document])]
}

sealed trait DataResource {
  self: DataResourceExtractor =>
  def config: DataResourceConfig
  def mapping: DataResourceMapping
}

trait APIConnection {
  def prepareConnection(dataSource: DataSource, headers: HTTPHeaders): BufferedSource = {
    val connection = new URL(dataSource).openConnection()
    headers.foreach{ case (k,v) => connection.setRequestProperty(k,v.toString)}
    Source.fromInputStream(connection.getInputStream)
  }
}

trait JSONResourceBase extends DataResource with DataResourceExtractor {
  lazy val mapper = new ObjectMapper
  def parseJson(s: String) = mapper.readValue(s, classOf[Object])
}

trait XMLResourceBase extends DataResource with DataResourceExtractor

//trait XLSXResourceBase extends DataResource with DataResourceExtractor {
//  val truthyValues = List("X", "x", "Y", "y", "yes")
//  val falsyValues = List("N", "n", "no")
//
//  def openSheet(path: String, sheet: XLSXSheet) : Sheet = WorkbookFactory.create(new File(path)).getSheet(sheet)
//}

case class JSONResource(config: DataResourceConfig, mapping: DataResourceMapping) extends JSONResourceBase {

  import Implicits._

  override def extractDocuments: Try[(List[Document], List[Document])] = {
    val c = config.config
    Try(c match {
      case (dataSource: DataSource, resourceType: ResourceType) => {
        val m = mapping.mapping
        // Load the JSON resource
        val jsonFile = resourceType match {
          case ResourceType.JSON => Source.fromFile(dataSource, "utf-8")
          case _ => throw new IllegalArgumentException(s"Wrong resource type, must be ${ResourceType.JSON} for JSON data resources.")
        }
        val jsonInput = jsonFile.mkString
        val json = parseJson(jsonInput)
        val documents = m.map(documentMapping => {
          // Potential result
          val propsMap = MutableMap[Metric, MetricValue]()
          documentMapping.foreach {
            case (metric, key) => {
              val metricRawValue = JsonPath.query(key, json)
              val metricValue = metricRawValue match {
                case Left(error) => throw new IllegalArgumentException(s"Some error occurred when looking up metric $metric: ${error.reason}.")
                case Right(value) => asScalaRecursive(value.next())
              }
              propsMap.update(metric,metricValue)
            }
          }
          Parsing.extractDocument(propsMap.toMap)
        })
        (documents.filter(_.isVertex), documents.filter(_.isEdge))
      }
      case _ => throw new IllegalArgumentException(s"Wrong number of parameters expected in JSON resource configuration file.")
    })
  }

}

case class JSONAPIResource(config: DataResourceConfig, mapping: DataResourceMapping) extends JSONResourceBase with APIConnection {

  import Implicits._

  override def extractDocuments: Try[(List[Document], List[Document])] = {
    val c = config.config
    Try(c match {
      case (dataSource: DataSource, resourceType: ResourceType, headers: HTTPHeaders) => {
        val m = mapping.mapping
        // Load the JSON resource
        val jsonFile = resourceType match {
          case ResourceType.JSON_API => prepareConnection(dataSource, headers)
          case _ => throw new IllegalArgumentException(s"Wrong resource type, must be ${ResourceType.JSON_API} for JSON API data resources.")
        }
        val jsonInput = jsonFile.mkString
        val json = parseJson(jsonInput)
        val documents = m.map(documentMapping => {
          // Potential result
          val propsMap = MutableMap[Metric, MetricValue]()
          documentMapping.foreach {
            case (metric, key) => {
              val metricRawValue = JsonPath.query(key, json)
              val metricValue = metricRawValue match {
                case Left(error) => throw new IllegalArgumentException(s"Some error occurred when looking up metric $metric: ${error.reason}.")
                case Right(value) => asScalaRecursive(value.next())
              }
              propsMap.update(metric,metricValue)
            }
          }
          Parsing.extractDocument(propsMap.toMap)
        })
        (documents.filter(_.isVertex), documents.filter(_.isEdge))
      }
      case _ => throw new IllegalArgumentException(s"Wrong number of parameters expected in JSON API resource configuration file.")
    })
  }

}

case class XMLResource(config: DataResourceConfig, mapping: DataResourceMapping) extends XMLResourceBase {

  import Implicits._

  override def extractDocuments: Try[(List[Document], List[Document])] = {
    val c = config.config
    Try(c match {
      case (dataSource: DataSource, resourceType: ResourceType) => {
        val m = mapping.mapping
        // Load the XML resource
        val xmlFile = resourceType match {
          case ResourceType.XML => Source.fromFile(dataSource, "utf-8")
          case _ => throw new IllegalArgumentException(s"Wrong resource type, must be ${ResourceType.XML} for XML data resources.")
        }
        val xmlInput = xmlFile.mkString
        val xml = DocumentHelper.parseText(xmlInput)
        val documents = m.map(documentMapping => {
          // Potential result
          val propsMap = MutableMap[Metric, MetricValue]()
          documentMapping.foreach {case (metric, key) => propsMap.update(metric, asScalaRecursive(xml.selectNodes(key)))}
          Parsing.extractDocument(propsMap.toMap)
        })
        (documents.filter(_.isVertex), documents.filter(_.isEdge))
      }
      case _ => throw new IllegalArgumentException(s"Wrong number of parameters expected in XML resource configuration file.")
    })
  }

}

case class XMLAPIResource(config: DataResourceConfig, mapping: DataResourceMapping) extends XMLResourceBase with APIConnection {

  import Implicits._

  override def extractDocuments: Try[(List[Document], List[Document])] = {
    val c = config.config
    // Potential result
    val mutableMap = MutableMap[Metric, MetricValue]()
    Try(c match {
      case (dataSource: DataSource, resourceType: ResourceType, headers: HTTPHeaders) => {
        val m = mapping.mapping
        // Load the XML resource
        val xmlFile = resourceType match {
          case ResourceType.XML_API => prepareConnection(dataSource, headers)
          case _ => throw new IllegalArgumentException(s"Wrong resource type, must be ${ResourceType.XML_API} for XML data resources.")
        }
        val xmlInput = xmlFile.mkString
        val xml = DocumentHelper.parseText(xmlInput)
        val documents = m.map(documentMapping => {
          // Potential result
          val propsMap = MutableMap[Metric, MetricValue]()
          documentMapping.foreach {case (metric, key) => propsMap.update(metric, asScalaRecursive(xml.selectNodes(key)))}
          Parsing.extractDocument(propsMap.toMap)
        })
        (documents.filter(_.isVertex), documents.filter(_.isEdge))
      }
      case _ => throw new IllegalArgumentException(s"Wrong number of parameters expected in XML API resource configuration file.")
    })
  }

}

//case class XLSXResource(config: DataResourceConfig, mapping: DataResourceMapping) extends XLSXResourceBase {
//  override def extractMetrics: Try[Map[Metric, MetricValue]] = {
//    val c = config.config
//    // Potential result
//    val mutableMap = MutableMap[Metric, MetricValue]()
//    Try(c match {
//      case (dataSource: DataSource, resourceType: ResourceType, sheet: XLSXSheet) => {
//        val m = mapping.mapping
//        val s = resourceType match {
//          case ResourceType.XLSX => openSheet(dataSource, sheet)
//          case _ => throw new IllegalArgumentException(s"Wrong resource type, must be ${ResourceType.XLSX} for XLSX data resources.")
//        }
//        m.foreach{case (metric, rowColumn) => {
//          val metricRawValue = rowColumn.split(",")
//          if (metricRawValue.length != 2) throw new IllegalArgumentException(s"Wrong metric path for metric $metric ($rowColumn): value must be two numbers (separated by comma) indicating row/cell position respectively.")
//          else {
//            val (row, column) = (metricRawValue.head.toInt, metricRawValue.tail.head.toInt)
//            val cell = s.getRow(row).getCell(column)
//            val cellValue = cell.getCellType match {
//              case Cell.CELL_TYPE_BOOLEAN => cell.getBooleanCellValue
//              case Cell.CELL_TYPE_NUMERIC => cell.getNumericCellValue
//              case Cell.CELL_TYPE_STRING => {
//                val cellValue = cell.getStringCellValue
//                if (truthyValues.contains(cellValue)) true
//                else if (falsyValues.contains(cellValue)) false
//                else cellValue
//              }
//              case _ => throw new IllegalArgumentException(s"Cell located in sheet ${s.getSheetName} ($row,$column) is empty or contains an invalid value.")
//            }
//            mutableMap.update(metric, cellValue)
//          }
//        }}
//        mutableMap.toMap
//      }
//      case _ => throw new IllegalArgumentException(s"Wrong number of parameters expected in XLSX resource configuration file.")
//    })
//  }
//}