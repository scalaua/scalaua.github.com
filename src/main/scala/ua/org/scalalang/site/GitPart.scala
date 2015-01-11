package ua.org.scalalang.site

import java.io.File
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api._

/**
 * publish site
 */
object GitPart
{

  def publishGenerated():Unit =
  {
    val configuration = Main.configuration
    import configuration._
    val repoDir = new File(tmpRepoDir)
    FileUtils.forceMkdir(repoDir)
    val repo = Git.cloneRepository().setURI(repoUrl).setBranch(siteBranch).setDirectory(repoDir).call()
    try {
       FileUtils.copyDirectory(new File(outputDir),repoDir)
       repo.add().addFilepattern(".").call()
       // TODO: ask and set author and command string.
       repo.commit().setMessage("automatic publishing").call()
       repo.push().call()
    } finally {
       repo.close()
    }
    FileUtils.deleteDirectory(repoDir)
  }

}
