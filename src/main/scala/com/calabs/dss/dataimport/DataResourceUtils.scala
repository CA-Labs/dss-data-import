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

  def loadMapping(path: String) : Try[Map[Metric, MetricPath]] = {
    val sourceFile = Source.fromFile(path)
    val sourceContent = sourceFile.getLines()
    sourceFile.close()
    parseMappingLines(sourceContent)
  }

  def parseMappingLines(lines: Iterator[String]) : Try[Map[Metric, MetricPath]] = {
    def parseLine(line: String) : (Metric, MetricPath) = {
      val params = line.split("=")
      if(params.length != 2) throw new IllegalArgumentException(s"Wrong number of parameters for line $line")
      else (params.head, params.tail.head)
    }
    Try(lines.map(line => Try(parseLine(line))).map(_.get).toList)
      .map(metricsAndPaths => metricsAndPaths.groupBy(_._1).map{ case(k,v) => (k, v.head._2)})
  }

  def loadConfig(path: String) : Try[(DataSource, ResourceType, Codec)] = {
    val sourceFile = Source.fromFile(path)
    val sourceContent = sourceFile.getLines()
    sourceFile.close()
    parseConfigLines(sourceContent).flatMap(checkConfigParams(_))
  }

  def parseConfigLines(lines: Iterator[String]) : Try[Map[ConfigKey, ConfigValue]] = {
    def parseLine(line: String) : (ConfigKey, ConfigValue) = {
      val params = line.split("=")
      if(params.length != 2) throw new IllegalArgumentException(s"Wrong number of parameters for line $line")
      else (params.head, params.tail.head)
    }
    Try(lines.map(line => Try(parseLine(line))).map(_.get).toList)
      .map(configKeysAndValues => configKeysAndValues.groupBy(_._1).map{ case (k,v) => (k, v.head._2)})
  }

  def checkConfigParams(config: Map[ConfigKey, ConfigValue]) : Try[(DataSource, ResourceType, Codec)] = {
      // Resource config check (mandatory properties are: source, resourceType and codec)
      val source = config.get("source") match {
        case Some(source) => source
        case None => throw new NoSuchElementException("Missing source parameter in resource config file.")
      }
      val resourceType = config.get("resourceType") match {
        case Some(resourceType) => resourceType
        case None => throw new NoSuchElementException("Missing resource type parameter in resource config file.")
      }
      val codec = config.get("codec") match {
        case Some(codec) => codec
        case None => throw new NoSuchElementException("Missing code parameter in resource config file.")
      }
      Success(source, resourceType, codec)
  }

}
