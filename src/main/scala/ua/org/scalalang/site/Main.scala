package ua.org.scalalang.site

import java.nio.file._
import java.nio.file.attribute._
import java.util.EnumSet

object Main
{

  def main(args:Array[String]):Unit =
  {
    val markdownPages = MarkdownPart.process()
    generateSite(markdownPages)
  }

  def generateSite(markdownPages: Seq[MarkdownCompiledPage]):Unit =
  {
    copyResources()
    //generatePages()
  }

  def copyResources(): Unit =
  {
    val in = FileSystems.getDefault().getPath("src","main","resources")
    // getPath is a variadic fucntion with two parameters, call one througth array is not trivial
    val outParts = configuration.outputDir.split("/");
    val out = if (outParts.length==1) {
                    FileSystems.getDefault().getPath(outParts(0));
              } else {
                    FileSystems.getDefault().getPath(outParts(0), outParts.toSeq.drop(1) :_* );
              }
    Files.walkFileTree(in, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Int.MaxValue, 
                       new SimpleFileVisitor[Path] {

                          override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult =
                          {
                           val outdir = out resolve (in relativize dir)
                           try {
                             Files.copy(dir, outdir)
                           } catch {
                             case ex: FileAlreadyExistsException =>
                               if (!Files.isDirectory(outdir)) {
                                  throw ex
                               }
                           }
                           FileVisitResult.CONTINUE
                          }
  

                          override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult =
                          {
                            Files.copy(file, out.resolve(in relativize file), StandardCopyOption.REPLACE_EXISTING)
                            FileVisitResult.CONTINUE
                          }

                       }
                      )
                      
  }

  lazy val configuration = new Configuration

}
