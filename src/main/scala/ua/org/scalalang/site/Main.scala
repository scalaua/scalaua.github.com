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
    generatePages(markdownPages)
  }

  def generatePages(markdownPages: Seq[MarkdownCompiledPage]):Unit =
  {
    val grouped = markdownPages.groupBy{ page =>
      if (page.path.length > 1) {
          page.path(0)
      } else {
          "toplevel"
      }
    }
    mkdir(configuration.outputDir+"/articles");

    for(articles <- grouped.get("articles");
        article <- articles) {
          val html = templates.html.article(article);
          Console.println(s"html for ${article.path}");
          Console.println(html);
    }
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

  def mkdir(path:String): Unit =
  {
    val f = new java.io.File(path);
    if (!f.exists()) {
        f.mkdir();
    } else if (!f.isDirectory()) {
        throw new IllegalStateException(s"file ${path} is not a directory");
    }
  }

  lazy val configuration = new Configuration

}
