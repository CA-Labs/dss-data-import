package com.calabs.dss.dataimport

import java.util

import org.scalatest.FunSpec
import scala.collection.mutable.{Map => MutableMap}
import scala.io.Source
import scala.util.Success

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 21/11/14
 */

class DataResourceSpec extends FunSpec {

  describe("A data resource extractor"){

    it("should correctly extract metrics from a JSON data resource (resourceType=file)"){
      val config = DataResourceUtils.loadConfig(getClass.getResource("/json/example.ok.config").getPath)
      val mapping = DataResourceUtils.loadMapping(getClass.getResource("/json/example.ok.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => {
          // How to test files that are loaded from a path property read from a file? This will be different between machines!
          // Overwrite data source path by now with one existing relative to resources folder
          val newConfig = DataResourceConfig(getClass.getResource("/json/example.json").getPath, c._2, c._3)
          val jsonResource = JSONResource(newConfig, DataResourceMapping(m))
          val metrics = jsonResource.extractMetrics
          assert(metrics.isSuccess)
          assert(metrics.get.get("metric1") == Some("bar"))
          assert(metrics.get.get("metric2") == Some(2))
          assert(metrics.get.get("metric3") == Some(new util.ArrayList(util.Arrays.asList(1,2,3))))
        }
        case _ => assert(false)
      }
    }

    it("should fail when trying to extract metrics from a JSON data resource (resourceType=file) with invalid config or mapping files"){
      val config = DataResourceUtils.loadConfig(getClass.getResource("/json/example.ko.config").getPath)
      val mapping = DataResourceUtils.loadMapping(getClass.getResource("/json/example.ko.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => assert(false)
        case _ => assert(true)
      }
    }

    ignore("should correctly parse an XML data resource"){
      ???
    }

  }

}
