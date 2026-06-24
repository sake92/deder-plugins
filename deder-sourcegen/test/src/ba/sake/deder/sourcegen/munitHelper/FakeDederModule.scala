package ba.sake.deder.sourcegen.munitHelper

import ba.sake.deder.config.DederProject
import ba.sake.deder.config.DederProject.DederModule

import java.util.List as JList

class FakeDederModule(id: String, root: String, mt: DederProject.ModuleType)
    extends DederModule(id, root, JList.of(), JList.of(), true, mt)
