package example

import D3._
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSName, JSExport}
import org.scalajs.dom
import D3.Layout.{GraphNode, GraphLink}

// It's important to specify a type for object when defining a JS object.
// Otherwise this causes exception like the following.
// "Uncaught java.lang.ClassCastException: 1 is not an instance of scala.runtime.Nothing$"
trait LinkData extends GraphLink {
  var value: Double = js.native
}

trait NodeData extends GraphNode {
  var group: Int = js.native
}

trait GraphData extends js.Object {
  var nodes: js.Array[NodeData] = js.native
  var links: js.Array[LinkData] = js.native
}

//Nesting @JSExport annotation may cause EmptyScope.enter error.
@JSExport
object D3DrawingExamples extends js.JSApp {

  def main(): Unit = {
    graph1()
    Graph2.draw()
//    Graph3.draw()
    Graph4.draw()
  }


  // Adapted from http://bl.ocks.org/mbostock/4062045
  def graph1() {
    val d3 = D3Obj.d3
    val color: D3.Scale.OrdinalScale = d3.scale.category20()

    val width: Double = 960
    val height: Double = 500


    val force = d3.layout.force()
      .charge(-120)
      .linkDistance(30)
      .size(js.Array(width, height))

    val svg = d3.select("body").append("svg")
      .attr("width", width)
      .attr("height", height)

    d3.json("miserables.json", (error: Any, g: js.Any) => {
      dom.console.log("Loaded!!",g)
      val graph = g.asInstanceOf[GraphData]
      force
        .nodes(graph.nodes)
        .links(graph.links)
        .start()

      dom.console.log("Force made")

      val link: Selection = svg.selectAll(".link")
        .data(graph.links)
        .enter().append("line")
        .attr("class", "link")
        .style("stroke-width",(d: LinkData, i: Double) => {
        val w = scala.math.sqrt(d.value)
        w.asInstanceOf[js.Dynamic]
      })

      dom.console.log("Link made")


      val node: D3.Selection = svg.selectAll(".node")
        .data(graph.nodes)
        .enter().append("circle")
        .attr("class", "node")
        .attr("r", 5)
        .style("fill", (d: NodeData, i: Double) => {
        color(d.group)
      })
        .call(force.drag())

      dom.console.log("Node made")

      node.append("title")
        .text((a: NodeData, _: Double) => a.name)

      dom.console.log("Title made")

      def tickFunc = () => {
        link.attr("x1", (d: LinkData) => d.source.x)
          .attr("y1", (d: LinkData) => d.source.y)
          .attr("x2", (d: LinkData) => d.target.x)
          .attr("y2", (d: LinkData) => d.target.y)

        node.attr("cx", (d: GraphNode) => d.x)
          .attr("cy", (d: GraphNode) =>  d.y)
        ()
      }

      force.on("tick", tickFunc)
      ()
    })
  }
}
