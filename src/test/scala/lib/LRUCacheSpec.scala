package lib

import org.scalatest.FunSpec

class LRUCacheSpec extends FunSpec {

  it("LRUCache") {
    val cache = new LRUCache[Int, Int](100)
    cache.put(1, 2)
    assert(cache.getOrElse(1)(3) == 2)
  }

}
