package com.calabs.dss.dataimport

import com.calabs.dss.dataimport.TypeAliases._

import scala.annotation.tailrec
import scala.collection.mutable.{Map => MutableMap}

/**
 * Created by Jordi Aranda
 * <jordi.aranda@bsc.es>
 * 13/1/15
 */

// Base trait which represents a database entity
sealed trait Element {
  def props: Map[String, Any]
  def isVertex: Boolean
  def isEdge: Boolean
  def isDocument: Boolean
}

// Database entity in NoSQL document stores
sealed trait Document extends Element {
  override def isVertex: Boolean = false
  override def isEdge: Boolean = false
  override def isDocument: Boolean = true
}

// Database entity (vertex) in NoSQL graph databases
case class Vertex(props: Map[String, Any]) extends Document {
  override def isVertex: Boolean = true
  override def isEdge: Boolean = false
}

// Database entity (edge) in NoSQL graph databases
case class Edge(props: Map[String, Any]) extends Document {
  override def isVertex: Boolean = false
  override def isEdge: Boolean = true
}

private[dataimport] object Parsing {

  type Chunk = List[String]

  object Tags {
    val PROPS_SEPARATOR = "\n"
    val KEY_VALUE_SEPARATOR = "::"
    val MAP_KEY_VALUE_SEPARATOR = "=>"
    val MAP_SEPARATOR = ","
    val SEARCHABLE_CRITERIA = "__"
    val FROM = SEARCHABLE_CRITERIA + "from"
    val TO = SEARCHABLE_CRITERIA + "to"
  }

  /**
   * Tries to parse a line representing a map of keys and values.
   * For example: "a=>1,b=>2,c=>3".
   * @param line The input line to parse.
   * @return The map extracted.
   */
  def stringToMap(line: String) : Map[String, String] = {
    line.split(Tags.MAP_SEPARATOR).map(mapValue => {
      val keyValue = mapValue.split(Tags.MAP_KEY_VALUE_SEPARATOR)
      if (keyValue.length != 2) throw new IllegalArgumentException(s"Wrong number of parameters in key/value $line")
      else (keyValue(0), keyValue(1))
    }).toMap
  }

  /**
   * Determines whether a key is a searchable criteria or not.
   * @param key The key to check it is a searchable criteria
   * @return
   */
  def isSearchableCriteria(key: String) : Boolean = key.startsWith(Tags.SEARCHABLE_CRITERIA) && !key.startsWith(Tags.FROM) && !key.startsWith(Tags.TO)

  /**
   * Extracts a document resource configuration, which is in turn a simple keys/values map.
   * @param lines The configuration input (raw format, [[List]] of [[String]]
   * @return A map of configuration keys and values.
   */
  def extractConfig(lines: List[String]) : Map[ConfigKey, ConfigValue] = {
    def parseLine(line: String) : (ConfigKey, ConfigValue) = {
      val params = line.split(Tags.KEY_VALUE_SEPARATOR)
      if(params.length != 2) throw new IllegalArgumentException(s"Wrong number of parameters for line $line")
      else (params.head, params.tail.head)
    }
    lines.map(line => parseLine(line)).toList.groupBy(_._1).map{ case(k,v) => (k, v.head._2) }
  }

  /**
   * Finds out whether a document is a vertex or an edge depending on its property keys.
   * If the properties map contains keys starting with [[Tags.FROM]] and [[Tags.TO]], this
   * document is considered to be an Edge. If not, it is considered a Vertex. For edges, [[Tags.FROM]]
   * and [[Tags.TO]] values must be searchable criteria in a [[Map]] fashion.
   * @param props The key/values map associated to this element.
   * @return The element extracted.
   */
  def extractDocument(props: Map[String, Any]) : Document = {
    if (props.contains(Tags.FROM) && props.contains(Tags.FROM)) {
      val from = stringToMap(props.get(Tags.FROM).get.toString)
      val to = stringToMap(props.get(Tags.TO).get.toString)

      // Create new properties map for new from/to extracted property maps
      val mutableMap = MutableMap[String,Any]()
      props.foreach{ case(k,v) => {
        if (k == Tags.FROM) mutableMap.update(k, from)
        else if (k == Tags.TO) mutableMap.update(k, to)
        else mutableMap.update(k,v)
      }}

      (from, to) match {
        case (f: Map[String, String], t: Map[String, String]) => Edge(mutableMap.toMap)
        case (f: Map[String,String], _) => throw new IllegalArgumentException(s"${Tags.TO} key must contain searchable criteria, ie must contain key/values.")
        case (_, t: Map[String,String]) => throw new IllegalArgumentException(s"${Tags.FROM} key must contain searchable criteria, ie must contain key/values.")
        case (_, _) => throw new IllegalArgumentException(s"${Tags.FROM} and ${Tags.TO} keys must contain searchable criteria, ie must contain key/values.")
      }
    } else Vertex(props)
  }

  /**
   * Extracts documents mappings, which are in turn simple keys/values.
   * @param lines The mapping input (raw format, [[List]] of [[String]]).
   * @return A pair of vertices and edges mappings.
   */
  def extractMappings(lines: List[String]) : (List[Map[Metric, MetricPath]]) = {

    /**
     * Extracts a chunk of lines (it assumes every chunk of data is separated by a blank line).
     * @param lines The total lines to read.
     * @return A pair of chunk of lines read and lines left to be read.
     */
    def extractChunk(lines: List[String], acc: Chunk) : (Chunk, List[String]) = lines match {
      case head :: tail if(head.isEmpty) => (acc, tail)
      case head :: tail if(!head.isEmpty) => extractChunk(tail, head :: acc)
      case _ => (acc, List.empty)
    }

    /**
     * Extracts chunks of lines (it assumes every chunk of data is separated by a blank line).
     * @param lines The input lines from where chunks should be extracted.
     * @param acc The current number of chunks extracted.
     * @return A list of extracted chunks.
     */
    @tailrec
    def extractChunks(lines: List[String], acc: List[Chunk]) : List[Chunk] = {
      if (lines.isEmpty) acc
      else {
        val nextChunk = extractChunk(lines, List.empty)
        extractChunks(nextChunk._2, nextChunk._1 :: acc)
      }
    }

    val mappings = extractChunks(lines, List.empty).map(props => {
        // Create the props map from key/values
        val propsMap = props.map(prop => {
          val keyValue = prop.split(Tags.KEY_VALUE_SEPARATOR)
          if (keyValue.length != 2) throw new IllegalArgumentException(s"Wrong key/value in mapping $prop.")
          else (keyValue(0), keyValue(1))
        }).toMap
        propsMap
    })

    mappings

  }

}
