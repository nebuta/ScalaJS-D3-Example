package example

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import D3.{D3Obj, Nest}
import org.scalajs.dom
import D3.Svg.AreaObject

import scala.collection.mutable

// Ported from http://bl.ocks.org/mbostock/4062085

@JSExport
object Graph2 {

  def draw() {
    val d3 = D3Obj.d3

    case class Margin(top: Double, right: Double, bottom: Double, left: Double)

    val margin = Margin(20,40,30,20)
    val width = 960 - margin.left - margin.right
    val height = 500 - margin.top - margin.bottom
    val barWidth = Math.floor(width / 19) - 1

    val x = d3.scale.linear()
      .range(Array(barWidth / 2, width - barWidth / 2))

    val y = d3.scale.linear()
      .range(Array(height, 0))

    val yAxis = d3.svg.axis()
      .scale(y)
      .orient("right")
      .tickSize(-width)
      .tickFormat((d: Double) => { Math.round(d / 1e6).toString + "M" })

    // An SVG element with a bottom-right origin.
    val svg: D3.Selection = d3.select("body").append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")")

    // A sliding container to hold the bars by birthyear.
    var birthyears = svg.append("g")
      .attr("class", "birthyears")

    // A label for the current year.
    var title: D3.Selection = svg.append("text")
      .attr("class", "title")
      .attr("dy", ".71em")
      .text(2000.toString)

    case class Population2(val people: Int, val year: Int, val age: Int, val sex: Int)

    d3.csv("population.csv", (error: js.Any, dataOriginal: js.Array[Population]) => {

      // Convert strings to numbers.
      val data: Array[Population2] = for(d: Population <- dataOriginal) yield {
        val people = d.people.toInt
        val year = d.year.toInt
        val age = d.age.toInt
        val sex = d.sex.toInt
        Population2(people,year,age,sex)
      }

      //To print out scala object, you need to use println(), not dom.console.log()
      // println(data.mkString(", "))

      // Compute the extent of the data set in age and years.
      val age1 = data.map(_.age).max
      val year0 = data.map(_.year).min
      val year1 = data.map(_.year).max
      var year = year1

      // Update the scale domains.
      x.domain(Array(year1 - age1, year1).map(_.toDouble))
      y.domain(Array(0, d3.max(data, (d: Population2) => d.people)).map(_.toDouble))


      // Produce a map from year and birthyear to [male, female].
      import scala.collection.mutable
      val data2: Map[Int,Map[Int,Array[Population2]]] = {
        val a: Map[Int,Array[Population2]] = data.groupBy(_.year)
        val aa: Map[Int,Map[Int,Array[Population2]]] = (for((k,v)<- a) yield {
          (k,v.groupBy(p => p.year - p.age))
        }).toMap
        aa
      }

      val data3 = data2.mapValues(d => {
        d.mapValues(_.map(_.people).mkString(","))
      })

//      println(data3)

      // Add an axis to show the population values.
      svg.append("g")
        .attr("class", "y axis")
        .attr("transform", "translate(" + width + ",0)")
        .call(yAxis)
        .selectAll("g")
        .filter((value: Any, i: js.Number) => value != null)
        .classed("zero", true)

      // Add labeled rects for each birthyear (so that no enter or exit is required).
      val birthyear = birthyears.selectAll(".birthyear")
        .data(d3.range(year0 - age1, year1 + 1, 5))
        .enter().append("g")
        .attr("class", "birthyear")
        .attr("transform", (birthyear: Double) => "translate(" + x(birthyear) + ",0)")

      birthyear.selectAll("rect")
        .data((birthyear: js.Number, i: Int) => {
        val a: Array[Int] = data2.get(year).flatMap(_.get(birthyear.toInt)).map(_.map(_.people))
          .getOrElse(Array(0,0))

        //.asInstanceOf[js.Dynamic]
      //  dom.console.log(js.Array(a))
        js.Array(a(0),a(1))
      })
        .enter().append("rect")
        .attr("x", -barWidth / 2)
        .attr("width", barWidth)
        .attr("y", y)
        .attr("height", (value: Double) => {
        height - y(value)
      })

      // Add labels to show birthyear.
      birthyear.append("text")
        .attr("y", height - 4)
        .text((birthyear: Double,i: Double) => birthyear.toString)

      // Add labels to show age (separate; not animated).
      svg.selectAll(".age")
        .data(d3.range(0, age1 + 1, 5))
        .enter().append("text")
        .attr("class", "age")
        .attr("x", (age: Int) => x(year - age))
        .attr("y", height + 4)
        .attr("dy", ".71em")
        .text((age: Int, _: js.Number) => { age.toString })

      // Allow the arrow keys to change the displayed year.
      dom.window.focus()
      d3.select(dom.window).on("keydown", (_: js.Any, _: js.Number) => {
        year = d3.event.keyCode match {
          case 37 => Math.max(year0, year - 10)
          case 39 => Math.min(year1, year + 10)
          case _ => year
        }
        update()
        1.asInstanceOf[js.Any]
      })

      def update() {
        if (data(year) == null) return
        title.text(year.toString)

        birthyears.transition()
          .duration(750)
          .attr("transform", "translate(" + (x(year1) - x(year)) + ",0)")

        birthyear.selectAll("rect")
          .data((birthyear: Double, i: Int) => {
          val a: Array[Int] = data2.get(year).flatMap(_.get(birthyear.toInt)).map(_.map(_.people))
            .getOrElse(Array(0,0))

          //.asInstanceOf[js.Dynamic]
          dom.console.log(js.Array(a))
          js.Array(a(0),a(1))
        })
          .transition()
          .duration(750)
          .attr("y", y)
          .attr("height", (value: Double) => height - y(value))
      }
      ()
    })
  }
}

class Population extends js.Object {
   var people: String = ???
   var year: String = ???
   var age: String = ???
   var sex: String = ???
}

// http://bl.ocks.org/mbostock/4060954

@JSExport
object Graph3 {
  import scala.collection.mutable.ArrayBuffer
  val d3 = D3.Base

  def bumpLayer(n: Int): js.Array[js.Object] = {

    def bump(a: ArrayBuffer[Double]) = {
      val x = 1 / (.1 + Math.random())
      val y = 2 * Math.random() - .5
      val z = 10 / (.1 + Math.random())
      for(i <- 0 until n) {
        val w = (i / n - y) * z
        a(i) += x * Math.exp(-w * w)
      }
    }
    import scala.collection.mutable

    val a: ArrayBuffer[Double] = ArrayBuffer.fill(n)(0)
    for (i <- 0 until 5) bump(a)
    val b: mutable.Seq[js.Object] = mutable.Seq(a.zipWithIndex.map((v: (Double, Int)) => js.Dynamic.literal("x" -> v._2, "y" -> Math.max(0, v._1)) ).toSeq :_*)
    val r = js.Array(b :_*)
//    dom.console.log(r)
    r
  }

  def draw() {
    val n = 20 // number of layers
    val m = 200 // number of samples per layer
    val stack = d3.layout.stack().offset("wiggle")
    val layers0: js.Array[js.Object] = js.Array(stack(Array.range(0,n).map(_ => bumpLayer(m))) :_*)
    val layers1: js.Array[js.Object] = js.Array(stack(Array.range(0,n).map(_ => bumpLayer(m))) :_*)

    dom.console.log("Graph3.draw()")
    dom.console.log(layers0,layers1)

    val width = 960.0
    val height = 500.0

    val x = d3.scale.linear()
      .domain(Array(0, m - 1).map(_.toDouble))
      .range(Array(0.0, width))

//    val mx: Double = Array((layers0++layers1) :_*).map((layer: AreaObject) => {
//      d.y0 + d.y
//    }).max

    val mx = 100
    val y = d3.scale.linear()
      .domain(Array(0.0, mx))
      .range(Array(height, 0))

    val color = d3.scale.linear()
      .range(Array("#aad", "#556").asInstanceOf[js.Array[js.Any]])

    val area = d3.svg.area()
      .x(d => x(d.x))
      .y0(d => y(d.y0))
      .y1(d => y(d.y0+d.y))

    val svg = d3.select("body").append("svg")
      .attr("width", width)
      .attr("height", height)

    dom.console.log(area,area(layers0))

    svg.selectAll("path")
      .data(layers0)
      .enter().append("path")
      .attr("d", area)
      .style("fill", (obj: js.Object, a: Double) => color(Math.random()))

//    def transition() {
//      d3.selectAll("path")
//        .data(() => {
//        var d = layers1
//        layers1 = layers0
//        return layers0 = d
//      })
//        .transition()
//        .duration(2500)
//        .attr("d", area)
//    }

  }
}

@JSExport
object Graph4 {
  val d3 = D3.Base
  trait MySelection extends D3.Selection {
    def size(x: Int, y: Int): MySelection = {
      this.attr("x", x).attr("y", y).asInstanceOf[MySelection]
    }
  }

  def size2(sel: D3.Selection, x: Int, y: Int) = {
    sel.attr("width",x).attr("height",y)
  }
  def draw(){
    val values: js.Array[js.Number] = js.Array((0 until 1000).map(_ => d3.random.bates(10)()) :_*)

    // A formatter for counts.
    val formatCount = d3.format(",.0f")

    case class Margin(top: Double, right: Double, bottom: Double, left: Double)
    val margin = Margin(10,30,30,30)

    val width = 960 - margin.left - margin.right
    val height = 500 - margin.top - margin.bottom

    val x: D3.Scale.QuantitiveScale = d3.scale.linear()
      .domain(Array(0d, 1))
      .range(Array(0d, width))

    // Generate a histogram using twenty uniformly-spaced bins.
    val data: mutable.Seq[D3.Layout.Bin] = d3.layout.histogram()
      .bins(x.ticks(20))(values)

    var y = d3.scale.linear()
      .domain(Array(0d, data.map(_.y).max))
      .range(Array(height, 0))

    var xAxis = d3.svg.axis()
      .scale(x)
      .orient("bottom")

    val svg = d3.select("body").append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")")

    val bar = svg.selectAll(".bar")
      .data(js.Array(data :_*))
      .enter().append("g")
      .attr("class", "bar")
      .attr("transform", (d: D3.Layout.Bin) => "translate(" + x(d.x) + "," + y(d.y) + ")")

    bar.append("rect")
      .attr("x", 1)
      .attr("width", x(data(0).dx) - 1)
      .attr("height", (d: D3.Layout.Bin) => height - y(d.y))

    bar.append("text")
      .attr("dy", ".75em")
      .attr("y", 6)
      .attr("x", x(data(0).dx / 2))
      .attr("text-anchor", "middle")
      .text((d: D3.Layout.Bin, i: Double) => formatCount(d.y))

    svg.append("g")
      .attr("class", "x axis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis)

  }
}

trait XYObject extends js.Object {
  var x: Double = ???
  var y: Double = ???
}

