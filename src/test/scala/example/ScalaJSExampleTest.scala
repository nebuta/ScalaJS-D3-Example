package example

import scala.scalajs.js
import js.Dynamic.{ global => g }
import scala.scalajs.test.JasmineTest

object ScalaJSExampleTest extends JasmineTest {

  describe("ScalaJSExample") {

    it("should draw svg") {
      import example._
//      D3DrawingExamples.graph1()
//      Graph2.draw()
    }
  }
}
