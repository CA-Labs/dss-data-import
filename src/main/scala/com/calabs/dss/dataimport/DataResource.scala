package com.calabs.dss.dataimport

import java.io.File
import java.net.URL

import com.fasterxml.jackson.databind.ObjectMapper
import io.gatling.jsonpath.JsonPath
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.dom4j.{DocumentHelper}

import scala.io.{BufferedSource, Source}
import scala.util.{Try}
import scala.collection.mutable.{Map => MutableMap}
import TypeAliases._

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
}

object ResourceType {
  val WEBSITE = "website"
  val JSON = "json"
  val JSON_API = "jsonAPI"
  val XML = "xml"
  val XML_API = "xmlAPI"
  val FILE = "file"
}

case class DataResourceConfig(config: Product)
case class DataResourceMapping(mapping: Map[Metric, MetricPath])

sealed trait DataResource {
  def config: DataResourceConfig
  def mapping: DataResourceMapping
}

trait DataResourceExtractor {
  def extractMetrics: Try[Map[Metric, MetricValue]]
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

trait ExcelResourceBase extends DataResource with DataResourceExtractor

case class JSONResource(config: DataResourceConfig, mapping: DataResourceMapping) extends JSONResourceBase {

  override def extractMetrics: Try[Map[Metric, MetricValue]] = {
    val c = config.config
    // Potential result
    val mutableMap = MutableMap[Metric, MetricValue]()
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
        m.foreach {
          case (metric, key) => {
            val metricRawValue = JsonPath.query(key, json)
            val metricValue = metricRawValue match {
              case Left(error) => throw new IllegalArgumentException(s"Some error occurred when looking up metric $metric: ${error.reason}.")
              case Right(value) => value.next()
            }
            mutableMap.update(metric, metricValue)
          }
        }
        mutableMap.toMap
      }
      case _ => throw new IllegalArgumentException(s"Wrong number of parameters expected in JSON resource configuration file.")
    })
  }

}

case class JSONAPIResource(config: DataResourceConfig, mapping: DataResourceMapping) extends JSONResourceBase with APIConnection {

  override def extractMetrics: Try[Map[Metric, MetricValue]] = {
    val c = config.config
    // Potential result
    val mutableMap = MutableMap[Metric, MetricValue]()
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
        m.foreach {
          case (metric, key) => {
            val metricRawValue = JsonPath.query(key, json)
            val metricValue = metricRawValue match {
              case Left(error) => throw new IllegalArgumentException(s"Some error occurred when looking up metric $metric: ${error.reason}.")
              case Right(value) => value.next()
            }
            mutableMap.update(metric, metricValue)
          }
        }
        mutableMap.toMap
      }
      case _ => throw new IllegalArgumentException(s"Wrong number of parameters expected in JSON API resource configuration file.")
    })
  }

}

case class XMLResource(config: DataResourceConfig, mapping: DataResourceMapping) extends XMLResourceBase {

  override def extractMetrics: Try[Map[Metric, MetricValue]] = {
    val c = config.config
    // Potential result
    val mutableMap = MutableMap[Metric, MetricValue]()
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
        m.foreach{case (metric, key) =>
          val metricRawValue = xml.selectObject(key)
          mutableMap.update(metric, metricRawValue)
        }
        mutableMap.toMap
      }
      case _ => throw new IllegalArgumentException(s"Wrong number of parameters expected in XML resource configuration file.")
    })
  }

}

case class XMLAPIResource(config: DataResourceConfig, mapping: DataResourceMapping) extends XMLResourceBase with APIConnection {

  override def extractMetrics: Try[Map[Metric, MetricValue]] = {
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
        m.foreach{case (metric, key) =>
          val metricRawValue = xml.selectObject(key)
          mutableMap.update(metric, metricRawValue)
        }
        mutableMap.toMap
      }
      case _ => throw new IllegalArgumentException(s"Wrong number of parameters expected in XML API resource configuration file")
    })
  }

}