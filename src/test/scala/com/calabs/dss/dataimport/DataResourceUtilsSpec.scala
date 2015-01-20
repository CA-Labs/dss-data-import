package com.calabs.dss.dataimport

import org.scalatest.{TryValues, FunSpec}

import scala.io.Source

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 4/12/14
 */
class DataResourceUtilsSpec extends FunSpec with TryValues {

  import Config._

  describe("Data Resource Utils") {

    /*********************************************************************************
     *************************************** JSON ************************************
     ********************************************************************************/

    it("should correctly parse and load JSON resource config files"){
      val correctConfig = Source.fromFile(getClass.getResource("/json/file/example-file.ok.config").getPath).getLines.toList
      val incorrectConfig = Source.fromFile(getClass.getResource("/json/file/example-file.ko.config").getPath).getLines.toList
      try {
        jsonResourceConfig.check(Parsing.extractConfig((correctConfig)))
        val config = jsonResourceConfig.load(getClass.getResource("/json/file/example-file.ok.config").getPath).success.value
        assert(config.get("source") == Some("example-file.json"))
      } catch {
        case e: Throwable => fail(e.getMessage)
      }
      intercept[NoSuchElementException] {
        jsonResourceConfig.check(Parsing.extractConfig(incorrectConfig))
      }
    }

    it("should correctly parse and load JSON API resource config files"){
      val correctConfig = Source.fromFile(getClass.getResource("/json/api/example-api.ok.config").getPath).getLines.toList
      val incorrectConfig = Source.fromFile(getClass.getResource("/json/api/example-api.ko.config").getPath).getLines.toList
      try {
        jsonApiResourceConfig.check(Parsing.extractConfig(correctConfig))
        val config = jsonApiResourceConfig.load(getClass.getResource("/json/api/example-api.ok.config").getPath).success.value
        assert(config.get("source") == Some("https://api.github.com/users/jarandaf"))
        assert(config.get("headers") == Some(Map[String,Any]("accept" -> "*")))
      } catch {
        case e: Throwable => fail(e.getMessage)
      }
      intercept[NoSuchElementException] {
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
        val config = xmlResourceConfig.load(getClass.getResource("/xml/file/example-file.ok.config").getPath).success.value
        assert(config.get("source") == Some("example-file.xml"))
      } catch {
        case e: Throwable => fail(e.getMessage)
      }
      intercept[NoSuchElementException] {
        xmlResourceConfig.check(Parsing.extractConfig((incorrectConfig)))
      }
    }

    it("should correctly parse and load XML API resource config files"){
      val correctConfig = Source.fromFile(getClass.getResource("/xml/api/example-api.ok.config").getPath).getLines.toList
      val incorrectConfig = Source.fromFile(getClass.getResource("/xml/api/example-api.ko.config").getPath).getLines.toList
      try {
        xmlApiResourceConfig.check(Parsing.extractConfig(correctConfig))
        val config = xmlApiResourceConfig.load(getClass.getResource("/xml/api/example-api.ok.config").getPath).success.value
        assert(config.get("source") == Some("http://api.openweathermap.org/data/2.5/weather?q=London&mode=xml"))
        assert(config.get("headers") == Some(Map[String,Any]("accept" -> "*")))
      } catch {
        case e: Throwable => fail(e.getMessage)
      }
      intercept[NoSuchElementException] {
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
