package lib

import scala.collection.JavaConverters._
import scala.collection.mutable

// https://github.com/roundrop/facebook4j/blob/master/facebook4j-core/src/main/java/facebook4j/internal/util/z_F4JLRUCache.java
case class LRUCache[K, V](maxSize: Int) {

  private[this] val cache: mutable.Map[K, V] = new java.util.LinkedHashMap[K, V](maxSize, 0.75f, true) {
    override def removeEldestEntry(eldest: java.util.Map.Entry[K, V]) = size > maxSize
  }.asScala

  def getOrElse(key: K)(fn: => V): V = {
    get(key).getOrElse {
      val result = fn
      put(key, result)
      result
    }
  }

  def get(key: K): Option[V] = synchronized {
    cache.get(key)
  }

  def put(key: K, value: V) = synchronized {
    cache.put(key, value)
  }

}
