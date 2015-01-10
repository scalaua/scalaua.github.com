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
    ResourcesPart.process()
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

    articleDir("articles");
    articleDir("meetups");

    def articleDir(dirname:String): Unit =
    {
     mkdir(configuration.outputDir+"/"+dirname)
     for(articles <- grouped.get(dirname);
        article <- articles) {
          val internalHtml = templates.html.article(article);
          val extHtml = templates.html.default(article("title"),internalHtml.body,"..")
          writeToFile(configuration.outputDir + "/" + article("localUrl"),extHtml.body)
     }
    }

  }

  def writeToFile(fname:String, content: String): Unit =
  {
    val writer = new java.io.PrintWriter(new java.io.File(fname))
    try {
        writer.print(content);
    } finally {
        writer.close()
    }
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
