package com.calabs.dss.dataimport

import org.scalatest.FunSpec

import scala.io.Source

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 4/12/14
 */
class DataResourceUtilsSpec extends FunSpec {

  describe("Data Resource Utils") {

    it("should correctly parse config files"){
      val correctConfig = Source.fromFile(getClass.getResource("/json/file/example-file.ok.config").getPath).getLines
      val incorrectConfig = Source.fromFile(getClass.getResource("/json/file/example-file.ko.config").getPath).getLines
      try {
        DataResourceUtils.checkConfigParams(DataResourceUtils.parseConfigLines(correctConfig))
      } catch {
        case e: Throwable => fail(e.getMessage)
      }
      intercept[IllegalArgumentException] {
        DataResourceUtils.checkConfigParams(DataResourceUtils.parseConfigLines(incorrectConfig))
      }
    }

    it("should correctly parse mapping files"){
      val correctMapping = Source.fromFile(getClass.getResource("/json/file/example-file.ok.map").getPath).getLines
      val incorrectMapping = Source.fromFile(getClass.getResource("/json/file/example-file.ko.map").getPath).getLines
      try {
        DataResourceUtils.parseMappingLines(correctMapping)
      } catch {
        case e: IllegalArgumentException => fail(e.getMessage)
        case _: Throwable => fail("Unexpected exception while parsing the mapping file.")
      }
      intercept[IllegalArgumentException] {
        DataResourceUtils.parseMappingLines(incorrectMapping)
      }
    }

    it("should correctly load config files"){
      val config = DataResourceUtils.loadConfig(getClass.getResource("/json/file/example-file.ok.config").getPath)
      assert(config.isSuccess)
      assert(config.get._1 == "example-file.json")
      assert(config.get._2 == "json")
      assert(config.get._3 == "utf-8")
    }

    it("should correctly load mapping files"){
      val mapping = DataResourceUtils.loadMapping(getClass.getResource("/json/file/example-file.ok.map").getPath)
      assert(mapping.isSuccess)
      assert(mapping.get.get("metric1") == Some("$.foo.bar[0].foo"))
      assert(mapping.get.get("metric2") == Some("$.bar"))
      assert(mapping.get.get("metric3") == Some("$.baz.baz"))
    }

  }

}
