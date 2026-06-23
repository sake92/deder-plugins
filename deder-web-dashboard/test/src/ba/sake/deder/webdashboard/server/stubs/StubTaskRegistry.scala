package ba.sake.deder.webdashboard.server.stubs

import ba.sake.deder.*
import ba.sake.deder.config.DederProject

class StubTaskRegistry(
    val tasks: Seq[TaskInfo] = Seq.empty
) extends TasksRegistryApi:

  def allTasks: Seq[TaskInfo] = tasks
  def tasksFor(moduleType: DederProject.ModuleType): Seq[TaskInfo] = Seq.empty
