import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.specs2.mutable._
import play.test.WithBrowser

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {

  "Application" should {

    "work from within a browser" in new WithBrowser {

      browser.goTo("http://localhost:" + port)

      browser.pageSource must contain("shouts out")
    }
  }
}
