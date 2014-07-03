package example

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import D3.Nest
import org.scalajs.dom

// Ported from http://bl.ocks.org/mbostock/4062085

@JSExport
object Graph2 {

  val d3 = D3.D3Obj.d3
    def draw() {

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

      class Population extends js.Object {
        var people: String = ???
        var year: String = ???
        var age: String = ???
        var sex: String = ???
      }

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

        println(data3)

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
          dom.console.log(js.Array(a))
          js.Array(a(0),a(1))
          })
          .enter().append("rect")
          .attr("x", -barWidth / 2)
          .attr("width", barWidth)
          .attr("y", y)
          .attr("height", (value: Double) => {
          dom.console.log(value)
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
