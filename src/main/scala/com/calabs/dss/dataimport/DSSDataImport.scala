package com.calabs.dss.dataimport

import scopt.OptionParser

import scala.util.{Failure, Try, Success}

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 5/12/14
 */

object DSSDataImport {

  case class Config(config: String, mapping: String, out: String)

  def main(args: Array[String]) : Unit = {
    val parser = new OptionParser[Config]("dss-data-import") {
      head("DSS Data Import tool", "0.0.1")
      opt[String]('c', "config") required() action { (x, c) =>
        c.copy(config = x)} text("Absolute path to configuration file")
      opt[String]('m', "mapping") required() action { (x,c) =>
        c.copy(mapping = x)} text("Absolute path to mapping file")
      opt[String]('o', "out") action { (x,c) =>
        c.copy(out = x)} text("Absolute path to output file")
    }
    parser.parse(args, Config("config", "mapping", "output")) map {
      import ResourceType._
      config => {
        val dssConfig = DataResourceUtils.loadConfig(config.config)
        val dssMapping = DataResourceUtils.loadMapping(config.mapping)
        val result = (dssConfig, dssMapping) match {
          case (Success(c), Success(m)) => {
            val drc = DataResourceConfig(c)
            val drm = DataResourceMapping(m)
            c._2 match {
              case JSON => JSONResource(drc, drm).extractMetrics
              case JSON_API => JSONResource(drc, drm).extractMetrics
              case XML => XMLResource(drc, drm).extractMetrics
              case XML_API => XMLResource(drc, drm).extractMetrics
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
