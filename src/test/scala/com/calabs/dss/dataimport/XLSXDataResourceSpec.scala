package com.calabs.dss.dataimport

import org.json4s.JsonAST.{JDouble, JBool, JInt}
import org.scalatest.FunSpec

import scala.collection.mutable.{Map => MutableMap}
import scala.util.{Failure, Success}

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 21/11/14
 */

class XLSXDataResourceSpec extends FunSpec {

  import Config._

  val resourceMapper = DataResourceMapper()

  describe("An XLSX Data Resource extractor"){

    it("should correctly extract metrics from an XLSX data resource (resourceType=xlsx)"){
      val config = xlsxResourceConfig.load(getClass.getResource("/xlsx/example-xlsx.ok.config").getPath)
      val mapping = resourceMapper.load(getClass.getResource("/xlsx/example-xlsx.ok.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => {
          // How to test files that are loaded from a path property read from a file? This will be different between machines!
          // Overwrite data source path by now with one existing relative to resources folder
          val newConfig = DataResourceConfig(Map[String,Any]("source" -> (getClass.getResource("/xlsx/example.xlsx").getPath), "resourceType" -> ResourceType.XLSX, "sheet" -> c.get("sheet").get))
          val xlsxResource = XLSXResource(newConfig, DataResourceMapping(m))
          val documents = xlsxResource.extractDocuments
          documents match {
            case Success(docs) => {
              val (vertices, edges) = (docs._1, docs._2)
              assert(!vertices.isEmpty)
              assert(edges.isEmpty)
              assert(vertices(0).isVertex)
              assert(vertices(0).props.get("metric1") == Some(JDouble(1.0)))
              assert(vertices(0).props.get("metric2") == Some(JBool(true)))
              assert(vertices(0).props.get("metric3") == Some(JBool(false)))
            }
            case Failure(e) => fail(s"Some error occured while trying to extract documents from the XLSX" +
              s" resource: ${e.getMessage}.")
          }
        }
        case (Failure(c), Success(m)) => {
          fail(s"Some error occurred while trying to load the XLSX resource config file: ${c.getMessage}.")
        }
        case (Success(c), Failure(m)) => {
          fail(s"Some error occurred while trying to load the XLSX resource mapping file: ${m.getMessage}.")
        }
        case _ => {
          fail("Neither the XLSX resource config file nor the mapping file could be loaded.")
        }
      }
    }

    it("should fail when trying to extract metrics from an XLSX data resource (resourceType=xlsx) with invalid config or mapping files") {
      val config = xlsxResourceConfig.load(getClass.getResource("/xlsx/example-xlsx.ko.config").getPath)
      val mapping = resourceMapper.load(getClass.getResource("/xlsx/example-xlsx.ko.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => fail("Unexpected correct loading of config/mapping files: they are wrong!")
        case _ => assert(true)
      }
    }

  }

}
