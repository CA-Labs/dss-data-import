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

case class DataResourceConfig(config: (DataSource, ResourceType, Codec))

object ResourceType {
  val WEBSITE = "website"
  val JSON = "json"
  val JSON_API = "jsonAPI"
  val XML = "xml"
  val XML_API = "xmlAPI"
}

case class DataResourceMapping(mapping: Map[Metric, MetricPath])

sealed trait DataResource {
  def config: DataResourceConfig
  def mapping: DataResourceMapping
}

trait DataResourceExtractor {
  def extractMetrics: Try[Map[Metric, MetricValue]]
}

case class JSONResource(config: DataResourceConfig, mapping: DataResourceMapping) extends DataResource with DataResourceExtractor {

  override def extractMetrics: Try[Map[Metric, MetricValue]] = {

    val c = config.config

    // Potential result
    val mutableMap = MutableMap[Metric, MetricValue]()

    Try(c match {

      case (dataSource, resourceType, codec) => {

        val m = mapping.mapping

        // Load the JSON resource
        val jsonFile = resourceType match {
          case ResourceType.JSON => Source.fromFile(dataSource, codec)
          case ResourceType.JSON_API => Source.fromURL(dataSource, codec)
          case _ => throw new IllegalArgumentException(s"Wrong resource type, must be either ${ResourceType.JSON} or ${ResourceType.JSON_API} for JSON data resources.")
        }
        val jsonInput = jsonFile.mkString
        val json = JSONResource.parseJson(jsonInput)

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

    })

  }

}

object JSONResource {
  lazy val mapper = new ObjectMapper
  def parseJson(s: String) = mapper.readValue(s, classOf[Object])
}

case class XMLResource(config: DataResourceConfig, mapping: DataResourceMapping) extends DataResource with DataResourceExtractor {

  override def extractMetrics: Try[Map[Metric, MetricValue]] = {

    val c = config.config

    // Potential result
    val mutableMap = MutableMap[Metric, MetricValue]()

    Try(c match {

      case (dataSource, resourceType, codec) => {

        val m = mapping.mapping

        // Load the XML resource
        val xmlFile = resourceType match {
          case ResourceType.XML => Source.fromFile(dataSource, codec)
          case ResourceType.XML_API => Source.fromURL(dataSource, codec)
          case _ => throw new IllegalArgumentException(s"Wrong resource type, must be either ${ResourceType.XML} or ${ResourceType.XML_API} for XML data resources.")
        }
        val xmlInput = xmlFile.mkString
        val xml = DocumentHelper.parseText(xmlInput)

        m.foreach{case (metric, key) =>
          val metricRawValue = xml.selectObject(key)
          mutableMap.update(metric, metricRawValue)
        }

        mutableMap.toMap

      }

    })

  }

}