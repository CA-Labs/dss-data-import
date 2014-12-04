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

    ignore("should correctly check config files"){
      assert(false)
    }

    it("should correctly parse mapping files"){
      val correctMapping = Source.fromFile(getClass.getResource("/json/example.ok.map").getPath).getLines()
      val incorrectMapping = Source.fromFile(getClass.getResource("/json/example.ko.map").getPath).getLines()
      assert(DataResourceUtils.parseMappingLines(correctMapping).isSuccess)
      assert(DataResourceUtils.parseMappingLines(incorrectMapping).isFailure)
    }

    ignore("should correctly load config files"){
      assert(false)
    }

    ignore("should correctly load mapping files"){
      assert(false)
    }

  }

}
