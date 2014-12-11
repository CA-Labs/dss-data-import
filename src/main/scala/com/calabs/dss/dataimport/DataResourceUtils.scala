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

trait Mapping[A] {
  def load(path: String) : Try[Map[Metric, MetricPath]]
}

object Mapping {

  val FIELD_SEPARATOR = "::"

  def parseMappingLines(lines: Iterator[String]) : Map[Metric, MetricPath] = {
    def parseLine(line: String) : (Metric, MetricPath) = {
      val params = line.split(FIELD_SEPARATOR)
      if(params.length != 2) throw new IllegalArgumentException(s"Wrong number of parameters for line $line")
      else (params.head, params.tail.head)
    }
    lines.map(line => parseLine(line)).toList.groupBy(_._1).map{ case(k,v) => (k, v.head._2) }
  }

  implicit val jsonResourceMapping: Mapping[JSONResource] = new Mapping[JSONResource] {
    // JSON resource mapping load
    override def load(path: String): Try[Map[Metric, MetricPath]] = {
      val sourceFile = Source.fromFile(path)
      val sourceContent = sourceFile.getLines()
      Try(parseMappingLines(sourceContent))
    }
  }

  implicit val jsonApiResourceMapping: Mapping[JSONAPIResource] = new Mapping[JSONAPIResource] {
    // JSON API resource mapping load
    override def load(path: String): Try[Map[Metric, MetricPath]] = {
      val sourceFile = Source.fromFile(path)
      val sourceContent = sourceFile.getLines()
      Try(parseMappingLines(sourceContent))
    }
  }

  implicit val xmlResourceMapping : Mapping[XMLResource] = new Mapping[XMLResource] {
    // XML resource mapping load
    override def load(path: String): Try[Map[Metric, MetricPath]] = {
      val sourceFile = Source.fromFile(path)
      val sourceContent = sourceFile.getLines()
      Try(parseMappingLines(sourceContent))
    }
  }

  implicit val xmlApiResourceMapping : Mapping[XMLAPIResource] = new Mapping[XMLAPIResource] {
    // XML API resource mapping load
    override def load(path: String): Try[Map[Metric, MetricPath]] = {
      val sourceFile = Source.fromFile(path)
      val sourceContent = sourceFile.getLines()
      Try(parseMappingLines(sourceContent))
    }
  }

}

trait Config[A] {
  def load(path: String) : Try[Product]
  def check(config: Map[ConfigKey, ConfigValue]) : Product
}

object Config {

  val FIELD_SEPARATOR = "::"
  val HEADERS_SEPARATOR = ","
  val HEADER_SEPARATOR = "=>"

  def parseConfigLines(lines: Iterator[String]) : Map[ConfigKey, ConfigValue] = {
    def parseLine(line: String) : (ConfigKey, ConfigValue) = {
      val params = line.split(FIELD_SEPARATOR)
      if(params.length != 2) throw new IllegalArgumentException(s"Wrong number of parameters for line $line")
      else (params.head, params.tail.head)
    }
    lines.map(line => parseLine(line)).toList.groupBy(_._1).map{ case(k,v) => (k, v.head._2) }
  }

  implicit val jsonResourceConfig : Config[JSONResource] = new Config[JSONResource]{

    override def load(path: String): Try[Product] = {
      // JSON resource load
      val sourceFile = Source.fromFile(path)
      val sourceContent = sourceFile.getLines()
      Try(check(parseConfigLines(sourceContent)))
    }

    override def check(config: Map[ConfigKey, ConfigValue]) : Product = {
      // Resource config check (mandatory properties are: source, resourceType and codec)
      val source = config.get("source") match {
        case Some(source) => source.toString
        case None => throw new NoSuchElementException("Missing source parameter in resource config file.")
      }
      val resourceType = config.get("resourceType") match {
        case Some(resourceType) => resourceType.toString
        case None => throw new NoSuchElementException("Missing resource type parameter in resource config file.")
      }
      (source, resourceType)
    }

  }

  implicit val jsonApiResourceConfig : Config[JSONAPIResource] = new Config[JSONAPIResource]{

    override def load(path: String): Try[Product] = {
      // JSON API resource load
      val sourceFile = Source.fromFile(path)
      val sourceContent = sourceFile.getLines()
      Try(check(parseConfigLines(sourceContent)))
    }

    override def check(config: Map[ConfigKey, ConfigValue]) : Product = {
      // JSON API resource config check (mandatory properties are: source, resourceType and headers)
      val source = config.get("source") match {
        case Some(source) => source.toString
        case None => throw new NoSuchElementException("Missing source parameter in resource config file.")
      }
      val resourceType = config.get("resourceType") match {
        case Some(resourceType) => resourceType.toString
        case None => throw new NoSuchElementException("Missing resource type parameter in resource config file.")
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
      (source, resourceType, headers)
    }

  }

  implicit val xmlResourceConfig : Config[XMLResource] = new Config[XMLResource]{

    override def load(path: String): Try[Product] = {
      // XML resource load
      val sourceFile = Source.fromFile(path)
      val sourceContent = sourceFile.getLines()
      Try(check(parseConfigLines(sourceContent)))
    }

    override def check(config: Map[ConfigKey, ConfigValue]) : Product = {
      // XML resource config check (mandatory properties are: source, resourceType)
      val source = config.get("source") match {
        case Some(source) => source.toString
        case None => throw new NoSuchElementException("Missing source parameter in resource config file.")
      }
      val resourceType = config.get("resourceType") match {
        case Some(resourceType) => resourceType.toString
        case None => throw new NoSuchElementException("Missing resource type parameter in resource config file.")
      }
      (source, resourceType)
    }

  }

  implicit val xmlApiResourceConfig : Config[XMLAPIResource] = new Config[XMLAPIResource]{

    override def load(path: String): Try[Product] = {
      // XML API resource load
      val sourceFile = Source.fromFile(path)
      val sourceContent = sourceFile.getLines()
      Try(check(parseConfigLines(sourceContent)))
    }

    override def check(config: Map[ConfigKey, ConfigValue]) : Product = {
      // XML API resource config check (mandatory properties are: source, resourceType and headers)
      val source = config.get("source") match {
        case Some(source) => source.toString
        case None => throw new NoSuchElementException("Missing source parameter in resource config file.")
      }
      val resourceType = config.get("resourceType") match {
        case Some(resourceType) => resourceType.toString
        case None => throw new NoSuchElementException("Missing resource type parameter in resource config file.")
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
      (source, resourceType, headers)
    }
  }

}