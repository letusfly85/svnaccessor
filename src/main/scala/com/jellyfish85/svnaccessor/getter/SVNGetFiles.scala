package com.jellyfish85.svnaccessor.getter

import com.jellyfish85.svnaccessor.manager.SVNManager
import com.jellyfish85.svnaccessor.bean.SVNRequestBean
import java.io.{FileOutputStream, ByteArrayOutputStream, File}
import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.{SVNException, SVNProperties}
import org.apache.commons.io.FileUtils

class SVNGetFiles {

  @throws(classOf[SVNException])
  def simpleGetFiles(list: List[SVNRequestBean], folder: File) {
    FileUtils.cleanDirectory(folder)

    val manager   : SVNManager     = new SVNManager
    val repository: SVNRepository  = manager.repository

    list.foreach {entity: SVNRequestBean =>
      val out: ByteArrayOutputStream = new ByteArrayOutputStream()
      println(entity.path)
      repository.getFile(
        entity.path,
        entity.revision,
        SVNProperties.wrap(java.util.Collections.EMPTY_MAP),
        out
      )

      val data: Array[scala.Byte] = out.toByteArray
      if (!(folder).exists()) {
        FileUtils.forceMkdir(folder)
      }

      val fos: FileOutputStream = new FileOutputStream(new File(folder.getPath, entity.fileName))
      fos.write(data)
      fos.close()

    }
  }
}
