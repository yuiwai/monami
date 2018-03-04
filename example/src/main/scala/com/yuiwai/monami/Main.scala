package com.yuiwai.monami

import com.yuiwai.monami.TimeCheck.c
import com.yuiwai.monami.core.GraphDatabase.{Edge, Node, NodeId}
import com.yuiwai.monami.core.{GraphDatabaseBuilder, IndexedDatabaseBuilder}

object Main extends App {
  graph()
  def indexed(): Unit = {
    c("build start")
    val db = new SampleIndexedDBBuilder(
      (1 to 100000)
        .map(i => i -> SampleData(i, (Math.random() * 10).toInt, s"name$i"))
    ).build
    c("initialized")
    db.find(1)
    c("find")
    db.findByIndex('category, 5)
    c("findByIndex")
  }
  def graph(): Unit = {
    val db = new SampleGraphDBBuilder()
      .addNode(Node(NodeId(1), SampleData(1, 1, "test1")))
      .addEdge(Edge(NodeId(1), NodeId(2)))
      .build
    println(db.find(NodeId(1)))
  }
}

object TimeCheck {
  private var t = System.currentTimeMillis()
  def c(msg: String): Unit = {
    val n = System.currentTimeMillis()
    println(s"$msg: ${n - t}")
    t = n
  }
}

class SampleIndexedDBBuilder(val data: Iterable[(Int, SampleData)]) extends IndexedDatabaseBuilder[Int, SampleData] {
  override val keys = Seq('category)
  override def indexKey(row: SampleData, key: Symbol): Int = key match {
    case 'category => row.category
  }
}
class SampleGraphDBBuilder() extends GraphDatabaseBuilder[Int, SampleData] {

}
case class SampleData(id: Int, category: Int, name: String)
