package com.calabs.dss.dataimport

import org.scalatest.FunSpec

import scala.io.Source

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 4/12/14
 */
class DataResourceUtilsSpec extends FunSpec {

  import Config._
  import Mapping._
  import TypeAliases._

  describe("Data Resource Utils") {

    /*********************************************************************************
     *************************************** JSON ************************************
     ********************************************************************************/

    it("should correctly parse and load JSON resource config files"){
      val correctConfig = Source.fromFile(getClass.getResource("/json/file/example-file.ok.config").getPath).getLines
      val incorrectConfig = Source.fromFile(getClass.getResource("/json/file/example-file.ko.config").getPath).getLines
      try {
        jsonResourceConfig.check(parseConfigLines(correctConfig))
        val config = jsonResourceConfig.load(getClass.getResource("/json/file/example-file.ok.config").getPath)
        assert(config.isSuccess)
        config.get match {
          case (source: DataSource, resourceType: ResourceType) => {
            assert(source == "example-file.json")
            assert(resourceType == ResourceType.JSON)
          }
          case _ => fail("Wrong number of configuration parameters: only source and resourceType are expected for JSON resources.")
        }
      } catch {
        case e: Throwable => fail(e.getMessage)
      }
      intercept[IllegalArgumentException] {
        jsonResourceConfig.check(parseConfigLines(incorrectConfig))
      }
    }

    it("should correctly parse and load JSON API resource config files"){
      val correctConfig = Source.fromFile(getClass.getResource("/json/api/example-api.ok.config").getPath).getLines
      val incorrectConfig = Source.fromFile(getClass.getResource("/json/api/example-api.ko.config").getPath).getLines
      try {
        jsonApiResourceConfig.check(parseConfigLines(correctConfig))
        val config = jsonApiResourceConfig.load(getClass.getResource("/json/api/example-api.ok.config").getPath)
        assert(config.isSuccess)
        config.get match {
          case (source: DataSource, resourceType: ResourceType, headers: HTTPHeaders) => {
            assert(source == "https://api.github.com/users/jarandaf")
            assert(resourceType == ResourceType.JSON_API)
            assert(headers.get("accept") == Some("*"))
          }
          case _ => fail("Wrong number of configuration parameters: only source, resourceType and headers are expected for JSON API resources.")
        }
      } catch {
        case e: Throwable => fail(e.getMessage)
      }
      intercept[IllegalArgumentException] {
        jsonApiResourceConfig.check(parseConfigLines(incorrectConfig))
      }
    }

    it("should correctly parse and load JSON resource mapping files"){
      val correctMapping = Source.fromFile(getClass.getResource("/json/file/example-file.ok.map").getPath).getLines
      val incorrectMapping = Source.fromFile(getClass.getResource("/json/file/example-file.ko.map").getPath).getLines
      try {
        val metrics = parseMappingLines(correctMapping)
        assert(metrics.get("metric1") == Some("$.foo.bar[0].foo"))
        assert(metrics.get("metric2") == Some("$.bar"))
        assert(metrics.get("metric3") == Some("$.baz.baz"))
      } catch {
        case e: IllegalArgumentException => fail(e.getMessage)
      }
      intercept[IllegalArgumentException] {
        parseMappingLines(incorrectMapping)
      }
    }

    it("should correctly parse and load JSON API resource mapping files"){
      val correctMapping = Source.fromFile(getClass.getResource("/json/api/example-api.ok.map").getPath).getLines
      val incorrectMapping = Source.fromFile(getClass.getResource("/json/api/example-api.ko.map").getPath).getLines
      try {
        val metrics = parseMappingLines(correctMapping)
        assert(metrics.get("login") == Some("$.login"))
        assert(metrics.get("url") == Some("$.url"))
        assert(metrics.get("email") == Some("$.email"))
      } catch {
        case e: IllegalArgumentException => fail(e.getMessage)
      }
      intercept[IllegalArgumentException] {
        parseMappingLines(incorrectMapping)
      }
    }

    /*********************************************************************************
      *************************************** XML ************************************
      ********************************************************************************/

    it("should correctly parse and load XML resource config files"){
      val correctConfig = Source.fromFile(getClass.getResource("/xml/file/example-file.ok.config").getPath).getLines
      val incorrectConfig = Source.fromFile(getClass.getResource("/xml/file/example-file.ko.config").getPath).getLines
      try {
        xmlResourceConfig.check(parseConfigLines(correctConfig))
        val config = xmlResourceConfig.load(getClass.getResource("/xml/file/example-file.ok.config").getPath)
        assert(config.isSuccess)
        config.get match {
          case (source: DataSource, resourceType: ResourceType) => {
            assert(source == "example-file.xml")
            assert(resourceType == ResourceType.XML)
          }
        }
      } catch {
        case e: Throwable => fail(e.getMessage)
      }
      intercept[IllegalArgumentException] {
        xmlResourceConfig.check(parseConfigLines(incorrectConfig))
      }
    }

    it("should correctly parse and load XML API resource config files"){
      val correctConfig = Source.fromFile(getClass.getResource("/xml/api/example-api.ok.config").getPath).getLines
      val incorrectConfig = Source.fromFile(getClass.getResource("/xml/api/example-api.ko.config").getPath).getLines
      try {
        xmlApiResourceConfig.check(parseConfigLines(correctConfig))
        val config = xmlApiResourceConfig.load(getClass.getResource("/xml/api/example-api.ok.config").getPath)
        assert(config.isSuccess)
        config.get match {
          case (source: DataSource, resourceType: ResourceType, headers: HTTPHeaders) => {
            assert(source == "http://api.openweathermap.org/data/2.5/weather?q=London&mode=xml")
            assert(resourceType == ResourceType.XML_API)
            assert(headers.get("accept") == Some("*"))
          }
        }
      } catch {
        case e: Throwable => fail(e.getMessage)
      }
      intercept[IllegalArgumentException] {
        xmlApiResourceConfig.check(parseConfigLines(incorrectConfig))
      }
    }

    it("should correctly parse and load XML resource mapping files"){
      val correctMapping = Source.fromFile(getClass.getResource("/xml/file/example-file.ok.map").getPath).getLines
      val incorrectMapping = Source.fromFile(getClass.getResource("/xml/file/example-file.ko.map").getPath).getLines
      try {
        val metrics = parseMappingLines(correctMapping)
        assert(metrics.get("metric1") == Some("string(/bookstore/book[1]/title)"))
        assert(metrics.get("metric2") == Some("string(/bookstore/book[1]/author)"))
        assert(metrics.get("metric3") == Some("number(/bookstore/book[1]/year)"))
        assert(metrics.get("metric4") == Some("number(/bookstore/book[1]/price)"))
      } catch {
        case e: IllegalArgumentException => fail(e.getMessage)
      }
      intercept[IllegalArgumentException] {
        parseMappingLines(incorrectMapping)
      }
    }

    it("should correctly parse and load XML API resource mapping files"){
      val correctMapping = Source.fromFile(getClass.getResource("/xml/api/example-api.ok.map").getPath).getLines
      val incorrectMapping = Source.fromFile(getClass.getResource("/xml/api/example-api.ko.map").getPath).getLines
      try {
        val metrics = parseMappingLines(correctMapping)
        assert(metrics.get("name") == Some("string(/current/city/@name)"))
        assert(metrics.get("country") == Some("string(/current/city/country)"))
      } catch {
        case e: IllegalArgumentException => fail(e.getMessage)
      }
      intercept[IllegalArgumentException] {
        parseMappingLines(incorrectMapping)
      }
    }

  }

}
