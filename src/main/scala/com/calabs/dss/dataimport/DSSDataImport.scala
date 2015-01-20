package com.calabs.dss.dataimport

import org.json4s.jackson.Serialization
import scopt.OptionParser

import scala.io.Source
import scala.util.{Failure, Try, Success}
import Config._

import org.json4s._

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 5/12/14
 */

object DSSDataImport {

  case class Config(config: String, mapping: String)

  def outputToJSON(result: Try[(List[Document], List[Document])]) : String = {
    // Used for JSON serialization
    implicit val formats = DefaultFormats
    result match {
      case Success(r) => Serialization.write(List(("vertices" -> r._1.map(_.props)), ("edges" -> r._2.map(_.props))).toMap)
      case Failure(e) => Serialization.write(List(("error" -> true), ("reason" -> e.getMessage)).toMap)
    }
  }

  def main(args: Array[String]) : Unit = {
    val parser = new OptionParser[Config]("dss-data-import") {
      head("DSS Data Import tool", "0.0.1")
      opt[String]('c', "config") required() action { (x, c) =>
        c.copy(config = x)} text("Absolute path to configuration file")
      opt[String]('m', "mapping") required() action { (x,c) =>
        c.copy(mapping = x)} text("Absolute path to mapping file")
    }
    parser.parse(args, Config("config", "mapping")) map {

      import ResourceType._

      val resourceMapper = DataResourceMapper()

      config => {

        val sourceFile = Try(Source.fromFile(config.config))

        sourceFile match {
          case Success(file) => {

            val sourceContent = file.getLines().toList
            val configParams = Try(Parsing.extractConfig(sourceContent))

            configParams match {
              case Success(params) => {

                val (dssConfig, dssMapping, resourceType) = params.get("resourceType") match {
                  case Some(resourceType) => resourceType match {
                    case ResourceType.JSON => (jsonResourceConfig.load(config.config), resourceMapper.load(config.mapping), resourceType)
                    case ResourceType.JSON_API => (jsonApiResourceConfig.load(config.config), resourceMapper.load(config.mapping), resourceType)
                    case ResourceType.XML => (xmlResourceConfig.load(config.config), resourceMapper.load(config.mapping), resourceType)
                    case ResourceType.XML_API => (xmlApiResourceConfig.load(config.config), resourceMapper.load(config.mapping), resourceType)
                    case _ => throw new IllegalArgumentException(s"Resource type $resourceType is not supported (supported types are ${ResourceType.JSON}, ${ResourceType.JSON_API}, ${ResourceType.XML}, ${ResourceType.XML_API}")
                  }
                  case None => throw new NoSuchElementException(s"resourceType key not found in configuration file located at ${config.config}")
                }

                val result = Try(
                  (dssConfig, dssMapping, resourceType) match {
                    case (Success(c), Success(m), resourceType) => {
                      val drc = DataResourceConfig(c)
                      val drm = DataResourceMapping(m)
                      resourceType match {
                        case ResourceType.JSON => JSONResource(drc, drm).extractDocuments.get
                        case ResourceType.JSON_API => JSONAPIResource(drc, drm).extractDocuments.get
                        case ResourceType.XML => XMLResource(drc, drm).extractDocuments.get
                        case ResourceType.XML_API => XMLAPIResource(drc, drm).extractDocuments.get
                      }
                    }
                    case (Failure(c), Success(m), _) => {
                      throw new IllegalArgumentException(s"Some error occurred while trying to load the resource config file: ${c.getMessage}.")
                    }
                    case (Success(c), Failure(m), _) => {
                      throw new IllegalArgumentException(s"Some error occurred while trying to load the resource mapping file: ${m.getMessage}.")
                    }
                    case (Failure(c), Failure(m), _) => {
                      throw new IllegalArgumentException(s"Neither the resource config file nor the mapping file could be loaded: ${c.getMessage}, ${m.getMessage}")
                    }
                  }
                )

                println(outputToJSON(result))

              }
              case Failure(e) => throw new IllegalArgumentException(s"Wrong configuration file was provided")
            }

          }
          case Failure(e) => throw new IllegalArgumentException(s"Configuration file located at ${config.config} could not be loaded")
        }

      }
    } getOrElse {
      println("Missing options: config and mapping are required.")
    }
  }

}
