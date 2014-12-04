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

    it("should correctly check config files"){
      val correctConfig = Source.fromFile(getClass.getResource("/json/example.ok.config").getPath).getLines
      val incorrectConfig = Source.fromFile(getClass.getResource("/json/example.ko.config").getPath).getLines
      assert(DataResourceUtils.parseConfigLines(correctConfig).map(DataResourceUtils.checkConfigParams(_)).isSuccess)
      assert(DataResourceUtils.parseConfigLines(incorrectConfig).map(DataResourceUtils.checkConfigParams(_)).isFailure)
    }

    it("should correctly parse mapping files"){
      val correctMapping = Source.fromFile(getClass.getResource("/json/example.ok.map").getPath).getLines
      val incorrectMapping = Source.fromFile(getClass.getResource("/json/example.ko.map").getPath).getLines
      assert(DataResourceUtils.parseMappingLines(correctMapping).isSuccess)
      assert(DataResourceUtils.parseMappingLines(incorrectMapping).isFailure)
    }

    it("should correctly load config files"){
      val config = DataResourceUtils.loadConfig(getClass.getResource("/json/example.ok.config").getPath)
      assert(config.isSuccess)
      assert(config.get._1 == "example.json")
      assert(config.get._2 == "json")
      assert(config.get._3 == "utf-8")
    }

    it("should correctly load mapping files"){
      val mapping = DataResourceUtils.loadMapping(getClass.getResource("/json/example.ok.map").getPath)
      assert(mapping.isSuccess)
      assert(mapping.get.get("metric1") == Some("$.foo.bar[0].foo"))
      assert(mapping.get.get("metric2") == Some("$.bar"))
      assert(mapping.get.get("metric3") == Some("$.baz.baz"))
    }

  }

}
