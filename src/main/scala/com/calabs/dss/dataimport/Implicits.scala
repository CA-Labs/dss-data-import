package com.calabs.dss.dataimport

import org.json4s.DefaultFormats
import org.json4s.Xml._

import scala.xml.XML
import scala.collection.JavaConverters._

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 14/1/15
 */

object Implicits {

  implicit def javaListToScalaSeq[A](list: java.util.List[A]) = list.asScala.toSeq
  implicit def javaMapToScalaMap[A](map: java.util.Map[String, A]) = map.asScala
  implicit def javaSetToScalaSet[A](set: java.util.Set[A]) = set.asScala

  /**
   * Converts Any values to valid Scala types recursively.
   * @param value The value to convert.
   * @return
   */
  def asScalaRecursive(value: Any) : Any = {
    implicit val formats = DefaultFormats
    value match {
      // Java collections
      case javaList: java.util.List[Any] => if(javaList.length == 0) javaList.asScala.toList else if (javaList.length == 1) asScalaRecursive(javaList.asScala.toList(0)) else asScalaRecursive(javaList.asScala.toList)
      case javaMap: java.util.Map[String, Any] => javaMap.asScala.mapValues(value => asScalaRecursive(value))
      case javaSet: java.util.Set[Any] => if(javaSet.size == 0) javaSet.asScala else if (javaSet.size == 1) asScalaRecursive(javaSet.asScala(0)) else javaSet.asScala.map(element => asScalaRecursive(element))
      // Scala collections
      case seq: Seq[Any] => if(seq.length == 0) seq else if (seq.length == 1) asScalaRecursive(seq(0)) else seq.map(element => asScalaRecursive(element))
      case map: Map[String, Any] => map.mapValues(value => asScalaRecursive(value))
      case set: Set[Any] => if(set.size == 0) set else if (set.size == 1) asScalaRecursive(set(0)) else set.map(element => asScalaRecursive(element))
      // Base case for XML nodes
      case node: org.dom4j.Node => toJson(XML.loadString(node.asXML)).extract[Map[String,Any]]  // Don't do this kids!
      // Primitive types should match here
      case any: Any => any
    }
  }

}
