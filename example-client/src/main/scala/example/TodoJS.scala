package example

import common.Framework
import config.Routes
import org.scalajs.dom
import org.scalajs.dom.ext.{Ajax, AjaxException}
import scalatags.JsDom._
import all._
import tags2.section
import rx._
import scala.scalajs.js.annotation.JSExport
import shared._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.concurrent.Future


@JSExport
object TodoJS {

  import Framework._

  object Model {

    import scala.scalajs.js
    import js.Dynamic.{global => g}
    import org.scalajs.jquery.{jQuery => $}
    import upickle.default._
    import common.ExtAjax._

    val tasks = Var(List.empty[Task])
    val done = Rx {
      tasks().count(_.done)
    }
    val notDone = Rx {
      tasks().length - done()
    }
    val editing = Var[Option[Task]](None)
    val filter = Var("All")

    val filters = Map[String, Task => Boolean](
      ("All", t => true)
    )

    def init: Future[Unit] = {
      Ajax.get(Routes.Todos.all).map { r =>
        read[List[Task]](r.responseText)
      }.map { r =>
        tasks() = r
      }
    }

    def all: List[Task] = tasks()

    def create(txt: String, done: Boolean = false) = {
      val json = s"""{"txt": "${txt}", "done": ${done}}"""
      Ajax.postAsJson(Routes.Todos.create, json).map { r =>
        tasks() = read[Task](r.responseText) +: tasks()
      }.recover { case e: AjaxException => dom.alert(e.xhr.responseText) }
    }

    def update(task: Task) = {
      val json = s"""{"txt": "${task.txt}", "done": ${task.done}}"""
      task.id.map { id =>
        Ajax.postAsJson(Routes.Todos.update(id), json).map { r =>
          if (r.ok) {
            val pos = tasks().indexWhere(t => t.id == task.id)
            tasks() = tasks().updated(pos, task)
          }
        }
      }
    }

    def delete(idOp: Option[Long]) = {
      idOp.map { id =>
        Ajax.delete(Routes.Todos.delete(id)).map { r =>
          if (r.ok) tasks() = tasks().filter(_.id != idOp)
        }
      }
    }

    def clearCompletedTasks = {
      Ajax.postAsForm(Routes.Todos.clear).map { r =>
        if (r.ok) tasks() = tasks().filter(!_.done)
      }
    }

  }

  val inputBox = input(
    id := "new-todo",
    placeholder := "Nowe zadanie",
    autofocus := true
  ).render

  def templateHeader = {
    header(id := "header")(
      form(
        inputBox,
        onsubmit := { () =>
          Model.create(inputBox.value)
          inputBox.value = ""
          false
        }
      )
    )
  }

  def templateBody = {
    section(id := "main")(
      input(
        id := "toggle-all",
        `type` := "checkbox",
        cursor := "pointer",
        onclick := { () =>
          val target = Model.tasks().exists(_.done == false)
          //          Var.set(tasks().map(_.done -> target): _*)
        }
      ),
      label(`for` := "toggle-all", "Mark all as complete"),
      partList,
      partControls
    )
  }

  def templateFooter = {
    footer(id := "info")(
      p("Projekt dostpny na GitHubie ", a(href := "https://github.com/Wwarrior1/ScalaProjekt")("tutaj"))
    )
  }

  def partList = Rx {
    ul(id := "todo-list")(
      for (task <- Model.tasks() if Model.filters(Model.filter())(task)) yield {
        val inputRef = input(`class` := "edit", value := task.txt).render

        li(
          `class` := Rx {
            if (task.done) "completed"
            else if (Model.editing() == Some(task)) "editing"
            else ""
          },
          div(`class` := "view")(
            "ondblclick".attr := { () =>
              Model.editing() = Some(task)
            },
            input(`class` := "toggle", `type` := "checkbox", cursor := "pointer", onchange := { () =>
              Model.update(task.copy(done = !task.done))
            }, if (task.done) checked := true else ""
            ),
            label(task.txt),
            button(
              `class` := "destroy",
              cursor := "pointer",
              onclick := { () => Model.delete(task.id) }
            )
          ),
          form(
            onsubmit := { () =>
              Model.update(task.copy(txt = inputRef.value))
              Model.editing() = None
              false
            },
            inputRef
          )
        )
      }
    )
  }

  def partControls = {
    footer(id := "footer")(
      button(
        id := "clear-completed",
        onclick := { () => Model.clearCompletedTasks },
        "Usuń pozostałe (", Model.done, ")"
      )
    )
  }

  @JSExport
  def main(): Unit = {

    Model.init.map { r =>
      dom.document.getElementById("content").appendChild(
        section(id := "todoapp")(
          templateHeader,
          templateBody,
          templateFooter
        ).render
      )
    }
  }


}