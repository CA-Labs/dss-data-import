package com.calabs.dss.dataimport

import org.json4s.jackson.Serialization
import scopt.OptionParser

import scala.util.{Failure, Try, Success}
import Config._
import Mapping._

import org.json4s._

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 5/12/14
 */

object DSSDataImport {

  case class Config(resourceType: String, config: String, mapping: String, out: String)

  def outputToJSON(result: Try[Map[String, Any]]) : String = {
    // Used for JSON serialization
    implicit val formats = DefaultFormats
    result match {
      case Success(r) => Serialization.write(r)
      case Failure(e) => Serialization.write(List(("exception" -> true), ("reason", e.getMessage)).toMap)
    }
  }

  def main(args: Array[String]) : Unit = {
    val parser = new OptionParser[Config]("dss-data-import") {
      head("DSS Data Import tool", "0.0.1")
      opt[String]('t', "resource-type") required() action { (x, c) =>
        c.copy(resourceType = x)} text("Resource type (value between json, jsonAPI, xml, xmlAPI, xlsx)")
      opt[String]('c', "config") required() action { (x, c) =>
        c.copy(config = x)} text("Absolute path to configuration file")
      opt[String]('m', "mapping") required() action { (x,c) =>
        c.copy(mapping = x)} text("Absolute path to mapping file")
    }
    parser.parse(args, Config("resource-type", "config", "mapping", "output")) map {

      import ResourceType._

      config => {
        val (dssConfig, dssMapping) = config.resourceType match {
          case ResourceType.JSON => (jsonResourceConfig.load(config.config), jsonResourceMapping.load(config.mapping))
          case ResourceType.JSON_API => (jsonApiResourceConfig.load(config.config), jsonApiResourceMapping.load(config.mapping))
          case ResourceType.XML => (xmlResourceConfig.load(config.config), xmlResourceMapping.load(config.mapping))
          case ResourceType.XML_API => (xmlApiResourceConfig.load(config.config), xmlApiResourceMapping.load(config.mapping))
          case ResourceType.XLSX => (xlsxResourceConfig.load(config.config), xlsxResourceMapping.load(config.mapping))
        }

        val result = Try(
          (dssConfig, dssMapping) match {
            case (Success(c), Success(m)) => {
              val drc = DataResourceConfig(c)
              val drm = DataResourceMapping(m)
              c.productElement(1) match {
                case JSON => JSONResource(drc, drm).extractMetrics.get
                case JSON_API => JSONAPIResource(drc, drm).extractMetrics.get
                case XML => XMLResource(drc, drm).extractMetrics.get
                case XML_API => XMLAPIResource(drc, drm).extractMetrics.get
                case XLSX => XLSXResource(drc, drm).extractMetrics.get
              }
            }
            case (Failure(c), Success(m)) => {
              throw new IllegalArgumentException(s"Some error occurred while trying to load the resource config file: ${c.getMessage}.")
            }
            case (Success(c), Failure(m)) => {
              throw new IllegalArgumentException(s"Some error occurred while trying to load the resource mapping file: ${m.getMessage}.")
            }
            case _ => {
              throw new IllegalArgumentException("Neither the resource config file nor the mapping file could be loaded.")
            }
          }
        )

        println(outputToJSON(result))

      }
    } getOrElse {
      println("Missing options: config and mapping options are required.")
    }
  }

}
