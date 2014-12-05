package com.calabs.dss.dataimport

import java.util

import org.scalatest.FunSpec

import scala.collection.mutable.{Map => MutableMap}
import scala.util.{Failure, Success}

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 21/11/14
 */

class XMLDataResourceSpec extends FunSpec {

  describe("An XML Data Resource extractor"){

    it("should correctly extract metrics from an XML data resource (resourceType=xml)"){
      val config = DataResourceUtils.loadConfig(getClass.getResource("/xml/file/example-file.ok.config").getPath)
      val mapping = DataResourceUtils.loadMapping(getClass.getResource("/xml/file/example-file.ok.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => {
          // How to test files that are loaded from a path property read from a file? This will be different between machines!
          // Overwrite data source path by now with one existing relative to resources folder
          val newConfig = DataResourceConfig(getClass.getResource("/xml/file/example-file.xml").getPath, c._2, c._3)
          val xmlResource = XMLResource(newConfig, DataResourceMapping(m))
          val metrics = xmlResource.extractMetrics
          assert(metrics.get.get("metric1") == Some("Everyday Italian"))
          assert(metrics.get.get("metric2") == Some("Giada De Laurentiis"))
          assert(metrics.get.get("metric3") == Some(2005))
          assert(metrics.get.get("metric4") == Some(30))
        }
        case (Failure(c), Success(m)) => {
          fail(s"Some error occurred while trying to load the XML resource config file: ${c.getMessage}.")
        }
        case (Success(c), Failure(m)) => {
          fail(s"Some error occurred while trying to load the XML resource mapping file: ${m.getMessage}.")
        }
        case _ => {
          fail("Neither the JSON resource config file nor the mapping file could be loaded.")
        }
      }
    }

    it("should correctly extract metrics from an XML data resource (resourceType=xmlAPI)"){
      val config = DataResourceUtils.loadConfig(getClass.getResource("/xml/api/example-api.ok.config").getPath)
      val mapping = DataResourceUtils.loadMapping(getClass.getResource("/xml/api/example-api.ok.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => {
          val xmlResource = XMLResource(DataResourceConfig(c), DataResourceMapping(m))
          val metrics = xmlResource.extractMetrics
          assert(metrics.get.get("name") == Some("London"))
          assert(metrics.get.get("country") == Some("GB"))
        }
        case (Failure(c), Success(m)) => {
          fail(s"Some error occurred while trying to load the XML resource config file: ${c.getMessage}.")
        }
        case (Success(c), Failure(m)) => {
          fail(s"Some error occurred while trying to load the XML resource mapping file: ${m.getMessage}.")
        }
        case _ => {
          fail("Neither the JSON resource config file nor the mapping file could be loaded.")
        }
      }
    }

    it("should fail when trying to extract metrics from an XML data resource (resourceType=xml) with invalid config or mapping files"){
      val config = DataResourceUtils.loadConfig(getClass.getResource("/xml/file/example-file.ko.config").getPath)
      val mapping = DataResourceUtils.loadMapping(getClass.getResource("/xml/file/example-file.ko.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => fail("Unexpected correct loading of config/mapping files: they are wrong!")
        case _ => assert(true)
      }
    }

    it("should fail when trying to extract metrics from an XML data resource (resourceType=xmlAPI) with invalid config or mapping files"){
      val config = DataResourceUtils.loadConfig(getClass.getResource("/xml/api/example-api.ko.config").getPath)
      val mapping = DataResourceUtils.loadMapping(getClass.getResource("/xml/api/example-api.ko.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => fail("Unexpected correct loading of config/mapping files: they are wrong!")
        case _ => assert(true)
      }
    }

  }

}
