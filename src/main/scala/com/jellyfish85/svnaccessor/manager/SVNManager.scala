package com.jellyfish85.svnaccessor.manager

import org.tmatesoft.svn.core.io.{SVNRepositoryFactory, SVNRepository}
import java.util.Properties
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager
import org.tmatesoft.svn.core.wc.{SVNClientManager, SVNDiffClient, SVNWCUtil}
import org.tmatesoft.svn.core.{SVNException, SVNURL}
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl

class SVNManager {

  /**
   * == Over View ==
   *
   * return subversion repository object
   *
   * @throws org.tmatesoft.svn.core.SVNException
   * @return subversion repository object
   */
  @throws(classOf[SVNException])
  def repository: SVNRepository = {
    var username: String = ""
    var password: String = ""
    var baseUrl:  String = ""

    val property: Properties = new Properties()
    try {
      if (System.getProperty("app.env.name") == "development") {
        property.load(getClass().getResourceAsStream("/properties/dev-subversion.properties"))

      } else {
        property.load(getClass().getResourceAsStream("/properties/subversion.properties"))
      }

      username = property.getProperty("username")
      password = property.getProperty("password")
      baseUrl  = new String(property.getProperty("baseUrl").getBytes("UTF-8"))

    } catch  {
      case e:Exception =>
        e.printStackTrace()
    }

    DAVRepositoryFactory.setup()

    var repository: SVNRepository = null

    val authManager: ISVNAuthenticationManager = SVNWCUtil.createDefaultAuthenticationManager(username, password)
    repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(baseUrl))
    repository.setAuthenticationManager(authManager)

    repository
  }

  /**
   *
   *
   * @return
   */
  def diffClient: SVNDiffClient = {
    var username: String = ""
    var password: String = ""
    var baseUrl:  String = ""

    val property: Properties = new Properties()
    try {
      if (System.getProperty("app.env.name") == "development") {
        property.load(getClass().getResourceAsStream("/properties/dev-subversion.properties"))

      } else {
        property.load(getClass().getResourceAsStream("/properties/subversion.properties"))
      }

      username = property.getProperty("username")
      password = property.getProperty("password")
      baseUrl  = new String(property.getProperty("baseUrl").getBytes("UTF-8"))

    } catch  {
      case e:Exception =>
        e.printStackTrace()
    }

    SVNRepositoryFactoryImpl.setup();
    DAVRepositoryFactory.setup()

    val clientManager: SVNClientManager = SVNClientManager.newInstance(
      SVNWCUtil.createDefaultOptions(true)
      , username
      , password);


    val diffClient: SVNDiffClient = clientManager.getDiffClient()
    diffClient
  }
}