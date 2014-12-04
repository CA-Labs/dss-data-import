package com.calabs.dss.dataimport

import javax.xml.xpath.XPathConstants

import com.fasterxml.jackson.databind.ObjectMapper
import io.gatling.jsonpath.JsonPath
import org.dom4j.{Node, DocumentHelper}

import scala.io.Source
import scala.util.{Failure, Success, Try}
import util.parsing.json.{JSON}
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
  type ConfigValue = String
}

case class DataResourceConfig(config: Map[String, String])

object ResourceType {
  val WEBSITE = "website"
  val JSON = "json"
  val JSON_API = "jsonAPI"
  val XML = "xml"
  val XML_API = "xmlAPI"
}

case class DataResourceMapping(mapping: Map[String, String])

sealed trait DataResource {
  def config: DataResourceConfig
  def mapping: DataResourceMapping
  def checkResourceConfig: Try[(DataSource, ResourceType, Codec)] = {
    // Resource config check (mandatory properties are: source, resourceType and codec)
    val resourceConfig = config.config
    val source = resourceConfig.get("source") match {
      case Some(source) => source
      case None => throw new NoSuchElementException("Missing source parameter in resource config file.")
    }
    val resourceType = resourceConfig.get("resourceType") match {
      case Some(resourceType) => resourceType
      case None => throw new NoSuchElementException("Missing resource type parameter in resource config file.")
    }
    val codec = resourceConfig.get("codec") match {
      case Some(codec) => codec
      case None => throw new NoSuchElementException("Missing code parameter in resource config file.")
    }
    Success(source, resourceType, codec)
  }
}

trait DataResourceExtractor {
  def extractMetrics: Try[Map[String, Any]]
}

//case class JSONResource(config: DataResourceConfig, mapping: DataResourceMapping) extends DataResource with DataResourceExtractor {
//
//  override def extractMetrics: Try[Map[String, Any]] = {
//
//    // Check resource config
//    checkResourceConfig.map {
//      case (dataSource, resourceType, codec) => {
//
//        // Potential result
//        val mutableMap = MutableMap[String, Any]()
//
//        // Metrics mapping
//        val metricJsonPathMapping = mapping.mapping
//
//        // Load the JSON resource
//        val jsonFile = resourceType match {
//          case ResourceType.JSON => Source.fromFile(dataSource, codec)
//          case ResourceType.JSON_API => Source.fromURL(dataSource, codec)
//          case _ => throw new IllegalArgumentException(s"Wrong resource type, must be either ${ResourceType.JSON} or ${ResourceType.JSON_API} for JSON data resources.")
//        }
//        val jsonInput = jsonFile.mkString
//        val json = JSONResource.parseJson(jsonInput)
//        jsonFile.close()
//
//        metricJsonPathMapping.foreach {
//          case (metric, key) => {
//            val metricRawValue = JsonPath.query(key, json)
//            val metricValue = metricRawValue match {
//              case Left(error) => throw new IllegalArgumentException(s"Some error occurred when looking up metric $metric: ${error.reason}.")
//              case Right(value) => Right(value.next())
//            }
//            mutableMap.update(metric, metricValue)
//          }
//        }
//
//        mutableMap.toMap
//
//      }
//    }
//  }
//
//}

object JSONResource {
  lazy val mapper = new ObjectMapper
  def parseJson(s: String) = mapper.readValue(s, classOf[Object])
}

//case class XMLResource(mapping: DataResourceMapping) extends DataResource with DataResourceExtractor {
//  override def extractMetrics: Try[Map[String, Either[String, Any]]] = {
//    val mutableMap = MutableMap[String, Either[String, Any]]()
//    val metricXPathMapping = mapping.mapping
//    val xmlFile = Source.fromFile(source)
//    val xmlInput = xmlFile.mkString
//    val xml = DocumentHelper.parseText(xmlInput)
//    metricXPathMapping.foreach{case (metric, key) =>
//        val metricRawValue = xml.selectObject(key)
//        mutableMap.update(metric, Right(metricRawValue))
//    }
//    mutableMap.toMap
//  }
//}