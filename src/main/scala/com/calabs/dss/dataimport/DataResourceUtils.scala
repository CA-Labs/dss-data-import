package com.calabs.dss.dataimport

import scala.io.Source
import scala.util.{Failure, Success, Try}
import scala.collection.mutable.{Map => MutableMap}
import TypeAliases._

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 3/12/14
 */

object DataResourceUtils {

  val FIELD_SEPARATOR = "::"
  val HEADERS_SEPARATOR = ","
  val HEADER_SEPARATOR = "=>"

  def loadMapping(path: String) : Try[Map[Metric, MetricPath]] = {
    val sourceFile = Source.fromFile(path)
    val sourceContent = sourceFile.getLines()
    // We should use some library instead of scala.io.Source, using try/catch/finally isn't scalaish code
    // sourceFile.close()
    Try(parseMappingLines(sourceContent))
  }

  def parseMappingLines(lines: Iterator[String]) : Map[Metric, MetricPath] = {
    def parseLine(line: String) : (Metric, MetricPath) = {
      val params = line.split(FIELD_SEPARATOR)
      if(params.length != 2) throw new IllegalArgumentException(s"Wrong number of parameters for line $line")
      else (params.head, params.tail.head)
    }
    lines.map(line => parseLine(line)).toList.groupBy(_._1).map{ case(k,v) => (k, v.head._2) }
  }

  def loadConfig(path: String) : Try[(DataSource, ResourceType, Codec, HTTPHeaders)] = {
    val sourceFile = Source.fromFile(path)
    val sourceContent = sourceFile.getLines()
    // We should use some library instead of scala.io.Source, usting try/catch/finally isnt' scalaish code
    // sourceFile.close()
    Try(checkConfigParams(parseConfigLines(sourceContent)))
  }

  def parseConfigLines(lines: Iterator[String]) : Map[ConfigKey, ConfigValue] = {
    def parseLine(line: String) : (ConfigKey, ConfigValue) = {
      val params = line.split(FIELD_SEPARATOR)
      if(params.length != 2) throw new IllegalArgumentException(s"Wrong number of parameters for line $line")
      else (params.head, params.tail.head)
    }
    lines.map(line => parseLine(line)).toList.groupBy(_._1).map{ case(k,v) => (k, v.head._2) }
  }

  def checkConfigParams(config: Map[ConfigKey, ConfigValue]) : (DataSource, ResourceType, Codec, HTTPHeaders) = {
      // Resource config check (mandatory properties are: source, resourceType and codec)
      val source = config.get("source") match {
        case Some(source) => source.toString
        case None => throw new NoSuchElementException("Missing source parameter in resource config file.")
      }
      val resourceType = config.get("resourceType") match {
        case Some(resourceType) => resourceType.toString
        case None => throw new NoSuchElementException("Missing resource type parameter in resource config file.")
      }
      val codec = config.get("codec") match {
        case Some(codec) => codec.toString
        case None => throw new NoSuchElementException("Missing code parameter in resource config file.")
      }
      val headers = config.get("headers") match {
        case Some(headers) => {
          val httpHeaders = MutableMap[HeaderKey, HeaderValue]()
          val rawHeaders = headers.toString.split(HEADERS_SEPARATOR)
          rawHeaders.foreach(rawHeader => {
            val headerKeyValue = rawHeader.split(HEADER_SEPARATOR)
            if(headerKeyValue.length != 2) throw new IllegalArgumentException("Wrong headers specification in resource config file.")
            else httpHeaders.update(headerKeyValue.head, headerKeyValue.tail.head)
          })
          httpHeaders.toMap
        }
        case None => Map[HeaderKey, HeaderValue]()
      }
      (source, resourceType, codec, headers)
  }

}
