package com.calabs.dss.dataimport

import com.fasterxml.jackson.databind.ObjectMapper
import io.gatling.jsonpath.JsonPath

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
    val metricKeyMapping = mapping.mapping;
    val jsonFile = Source.fromFile(source)
    val jsonInput = jsonFile.mkString
    val json = JSONResource.parseJson(jsonInput)
    jsonFile.close()
    for{ (metricName, metricKey) <- metricKeyMapping } yield {
      val metricRawValue = JsonPath.query(metricKey, json)
      val metricValue = metricRawValue match {
        case Left(error) => Left(error.reason)
        case Right(value) => Right(value.next())
      }
      mutableMap.update(metricName,metricValue)
    }
    mutableMap.toMap
  }
}

object JSONResource {
  lazy val mapper = new ObjectMapper
  def parseJson(s: String) = mapper.readValue(s, classOf[Object])
}

case class XMLResource(mapping: DataResourceMapping, source: String) extends DataResource with DataResourceExtractor {
  override def extractMetrics: Map[String, Either[String, Any]] = ???
}

