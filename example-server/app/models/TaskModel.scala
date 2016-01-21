package models

import scala.concurrent.Future
import shared.Task
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

object TaskModel {
  val store: TaskStore = TaskSlickStore
}

trait TaskStore {
  def all: Future[Seq[Task]]
  def create(txt: String, done: Boolean): Future[Task]
  def update(task: Task): Future[Boolean]
  def delete(ids: Long*): Future[Boolean]
  def clearCompletedTasks: Future[Int]
}

object TaskSlickStore extends TaskStore {

  import play.api.db.DB
  import slick.driver.H2Driver.api._

  //H2 always uses all upper case. That's annoying!!!
  class Tasks(tag: Tag) extends Table[Task](tag, "TASKS"){
    def id   = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
    def txt  = column[String]("TXT")
    def done = column[Boolean]("DONE")
    def * = (id, txt, done) <> (Task.tupled, Task.unapply)
  }

  private def db: Database = Database.forDataSource(DB.getDataSource())

  val tasks = TableQuery[Tasks]

  override def all(): Future[Seq[Task]] = {
    db.run(tasks.sortBy(_.id.desc).result)
  }

  override def create(txt: String, done: Boolean): Future[Task] = {
    db.run{
      (tasks returning tasks.map(_.id) into ((task,id) => task.copy(id=id))) += Task(None, txt, done)
    }
  }

  override def update(task: Task): Future[Boolean] = {
    db.run{
      val q = for { t <- tasks if t.id === task.id } yield (t.txt, t.done)
      q.update(task.txt, task.done)
    }.map(_ == 1)
  }

  override def delete(ids: Long*): Future[Boolean] = {
    Future.sequence(for(id <- ids) yield { db.run(tasks.filter(_.id === id).delete).map(_==1)}).map{
      _.find(i => i == false) == None
    }
  }

  override def clearCompletedTasks: Future[Int] = {
    db.run{
      tasks.filter(_.done === true).delete
    }
  }
}

