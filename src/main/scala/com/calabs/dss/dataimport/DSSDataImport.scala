package com.calabs.dss.dataimport

import scopt.OptionParser

import scala.util.{Failure, Try, Success}
import Config._
import Mapping._

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 5/12/14
 */

object DSSDataImport {

  case class Config(resourceType: String, config: String, mapping: String, out: String)

  def main(args: Array[String]) : Unit = {
    val parser = new OptionParser[Config]("dss-data-import") {
      head("DSS Data Import tool", "0.0.1")
      opt[String]('t', "resource-type") required() action { (x, c) =>
        c.copy(resourceType = x)} text("Resource type (value between json, jsonAPI, xml, xmlAPI)")
      opt[String]('c', "config") required() action { (x, c) =>
        c.copy(config = x)} text("Absolute path to configuration file")
      opt[String]('m', "mapping") required() action { (x,c) =>
        c.copy(mapping = x)} text("Absolute path to mapping file")
      opt[String]('o', "out") action { (x,c) =>
        c.copy(out = x)} text("Absolute path to output file")
    }
    parser.parse(args, Config("resource-type", "config", "mapping", "output")) map {

      import ResourceType._

      config => {
        val (dssConfig, dssMapping) = config.resourceType match {
          case ResourceType.JSON => (jsonResourceConfig.load(config.config), jsonResourceMapping.load(config.mapping))
          case ResourceType.JSON_API => (jsonApiResourceConfig.load(config.config), jsonApiResourceMapping.load(config.mapping))
          case ResourceType.XML => (xmlResourceConfig.load(config.config), xmlResourceMapping.load(config.mapping))
          case ResourceType.XML_API => (xmlApiResourceConfig.load(config.config), xmlApiResourceMapping.load(config.mapping))
        }

        val result = (dssConfig, dssMapping) match {
          case (Success(c), Success(m)) => {
            val drc = DataResourceConfig(c)
            val drm = DataResourceMapping(m)
            c.productElement(1) match {
              case JSON => JSONResource(drc, drm).extractMetrics
              case JSON_API => JSONAPIResource(drc, drm).extractMetrics
              case XML => XMLResource(drc, drm).extractMetrics
              case XML_API => XMLAPIResource(drc, drm).extractMetrics
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
        result match {
          case Success(r) => println("OK => " + result.toString)
          case Failure(e) => println("KO => " + e.getMessage)
        }
      }
    } getOrElse {
      println("Missing options: config, mapping and output options are required.")
    }
  }

}
