package com.calabs.dss.dataimport

import javax.xml.xpath.XPathConstants

import com.fasterxml.jackson.databind.ObjectMapper
import io.gatling.jsonpath.JsonPath
import org.dom4j.{Node, DocumentHelper}

import scala.io.Source
import util.parsing.json.{JSON}
import scala.collection.mutable.{Map => MutableMap}

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 20/11/14
 */

case class DataResourceMapping(mapping: Map[String, String])
sealed trait DataResource {
  def mapping: DataResourceMapping
}
trait DataResourceExtractor {
  def extractMetrics: Map[String, Either[String,Any]]
}

case class JSONResource(mapping: DataResourceMapping, source: String) extends DataResource with DataResourceExtractor {
  override def extractMetrics: Map[String, Either[String, Any]] = {
    val mutableMap = MutableMap[String, Either[String, Any]]()
    val metricJsonPathMapping = mapping.mapping
    val jsonFile = Source.fromFile(source)
    val jsonInput = jsonFile.mkString
    val json = JSONResource.parseJson(jsonInput)
    jsonFile.close()
    metricJsonPathMapping.foreach{case (metric,key) => {
      val metricRawValue = JsonPath.query(key, json)
      val metricValue = metricRawValue match {
        case Left(error) => Left(error.reason)
        case Right(value) => Right(value.next())
      }
      mutableMap.update(metric, metricValue)
    }}
    mutableMap.toMap
  }
}

object JSONResource {
  lazy val mapper = new ObjectMapper
  def parseJson(s: String) = mapper.readValue(s, classOf[Object])
}

case class XMLResource(mapping: DataResourceMapping, source: String) extends DataResource with DataResourceExtractor {
  override def extractMetrics: Map[String, Either[String, Any]] = {
    val mutableMap = MutableMap[String, Either[String, Any]]()
    val metricXPathMapping = mapping.mapping
    val xmlFile = Source.fromFile(source)
    val xmlInput = xmlFile.mkString
    val xml = DocumentHelper.parseText(xmlInput)
    metricXPathMapping.foreach{case (metric, key) =>
        val metricRawValue = xml.selectObject(key)
        mutableMap.update(metric, Right(metricRawValue))
    }
    mutableMap.toMap
  }
}