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
  import TypeAliases._

  describe("Data Resource Utils") {

    /*********************************************************************************
     *************************************** JSON ************************************
     ********************************************************************************/

    it("should correctly parse and load JSON resource config files"){
      val correctConfig = Source.fromFile(getClass.getResource("/json/file/example-file.ok.config").getPath).getLines.toList
      val incorrectConfig = Source.fromFile(getClass.getResource("/json/file/example-file.ko.config").getPath).getLines.toList
      try {
        jsonResourceConfig.check(Parsing.extractConfig((correctConfig)))
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
        jsonResourceConfig.check(Parsing.extractConfig(incorrectConfig))
      }
    }

    it("should correctly parse and load JSON API resource config files"){
      val correctConfig = Source.fromFile(getClass.getResource("/json/api/example-api.ok.config").getPath).getLines.toList
      val incorrectConfig = Source.fromFile(getClass.getResource("/json/api/example-api.ko.config").getPath).getLines.toList
      try {
        jsonApiResourceConfig.check(Parsing.extractConfig(correctConfig))
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
        jsonApiResourceConfig.check(Parsing.extractConfig((incorrectConfig)))
      }
    }

    /*********************************************************************************
      *************************************** XML ************************************
      ********************************************************************************/

    it("should correctly parse and load XML resource config files"){
      val correctConfig = Source.fromFile(getClass.getResource("/xml/file/example-file.ok.config").getPath).getLines.toList
      val incorrectConfig = Source.fromFile(getClass.getResource("/xml/file/example-file.ko.config").getPath).getLines.toList
      try {
        xmlResourceConfig.check(Parsing.extractConfig((correctConfig)))
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
        xmlResourceConfig.check(Parsing.extractConfig((incorrectConfig)))
      }
    }

    it("should correctly parse and load XML API resource config files"){
      val correctConfig = Source.fromFile(getClass.getResource("/xml/api/example-api.ok.config").getPath).getLines.toList
      val incorrectConfig = Source.fromFile(getClass.getResource("/xml/api/example-api.ko.config").getPath).getLines.toList
      try {
        xmlApiResourceConfig.check(Parsing.extractConfig(correctConfig))
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
        xmlApiResourceConfig.check(Parsing.extractConfig(incorrectConfig))
      }
    }

    /*********************************************************************************
      ************************************** XLSX ************************************
      ********************************************************************************/

    ignore("should correctly parse and load XLSX resource config files"){
//      val correctConfig = Source.fromFile(getClass.getResource("/xlsx/example-xlsx.ok.config").getPath).getLines
//      val incorrectConfig = Source.fromFile(getClass.getResource("/xlsx/example-xlsx.ko.config").getPath).getLines
//      try {
//        xlsxResourceConfig.check(parseConfigLines(correctConfig))
//        val config = xlsxResourceConfig.load(getClass.getResource("/xlsx/example-xlsx.ok.config").getPath)
//        assert(config.isSuccess)
//        config.get match {
//          case (source: DataSource, resourceType: ResourceType, sheet: XLSXSheet) => {
//            assert(source == "example.xlsx")
//            assert(resourceType == ResourceType.XLSX)
//            assert(sheet == "test")
//          }
//        }
//      } catch {
//        case e: Throwable => fail(e.getMessage)
//      }
//      intercept[IllegalArgumentException] {
//        xlsxResourceConfig.check(parseConfigLines(incorrectConfig))
//      }
    }

  }

}
