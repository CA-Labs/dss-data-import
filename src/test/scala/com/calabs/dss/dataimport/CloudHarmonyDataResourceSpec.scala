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

class CloudHarmonyDataResourceSpec extends FunSpec {

  describe("A Cloud Harmony Data Resource extractor"){

    it("should correctly extract metrics from a JSON data resource (resourceType=jsonAPI)"){
      val config = DataResourceUtils.loadConfig(getClass.getResource("/json/api/cloud-harmony/cloud-harmony.aws.ec2.1.config").getPath)
      val mapping = DataResourceUtils.loadMapping(getClass.getResource("/json/api/cloud-harmony/cloud-harmony.aws.ec2.1.map").getPath)
      (config, mapping) match {
        case (Success(c), Success(m)) => {
          val jsonResource = JSONResource(DataResourceConfig(c), DataResourceMapping(m))
          val metrics = jsonResource.extractMetrics
          assert(metrics.get("beta") == false)
          assert(metrics.get("regions") == "eu")
        }
        case (Failure(c), Success(m)) => {
          fail(s"Some error occurred while trying to load the JSON resource config file: ${c.getMessage}.")
        }
        case (Success(c), Failure(m)) => {
          fail(s"Some error occurred while trying to load the JSON resource mapping file: ${m.getMessage}.")
        }
        case _ => {
          fail("Neither the JSON resource config file nor the mapping file could be loaded.")
        }
      }
    }

  }

}
