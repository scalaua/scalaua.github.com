package ua.org.scalalang.site

import java.nio.file._
import java.nio.file.attribute._
import java.util.EnumSet
import java.io.File
import org.apache.commons.io.FileUtils

import play.twirl.api._
import templates._

object Main
{

  def main(args:Array[String]):Unit =
  {
    Console.println("args:"+args.toList)
    val List(runServer, publishSite) = if (args.length > 0) {
                                        List("server","publishSite") map (_ == args(0))
                                       } else List(false,false)
    val markdownPages = MarkdownPart.process()
    generateSite(markdownPages)
    if (runServer) {
       Console.println("starting embedded server on port ${configuration.embeddedServerPort}")
       EmbeddedWebServer.run()
    }
    if (publishSite) {
       Console.println(s"publish site to ${Main.configuration.repoUrl}/${Main.configuration.siteBranch}")
       GitPart.publishGenerated()
    }
  }

  def generateSite(markdownPages: Seq[MarkdownCompiledPage]):Unit =
  {
    copyResources()
    generatePages(markdownPages)
  }

  def  copyResources(): Unit =
  {
    FileUtils.copyDirectory(new File("src/main/resources"),
                            new File(Main.configuration.outputDir))
  } 

  def generatePages(markdownPages: Seq[MarkdownCompiledPage]):Unit =
  {
    val grouped = markdownPages.groupBy{ _.path(0) }

    val meetups = byTime(grouped("meetups"))
    val articles = byTime(grouped("articles"))

    val top10All = topN(meetups ++ articles, 10)

    articleDir("articles")
    articleDir("meetups")

    page("index.html","ScalaUA",html.home(top10All),".")
    page("articles/index.html","ScalaUA",html.articles(articles),"..")
    page("meetups/index.html","ScalaUA",html.articles(meetups),"..")
    page("about/index.html","About",html.page(grouped("about.md").head),"..")


    def articleDir(dirname:String): Unit =
    {
     FileUtils.forceMkdir(new File(Main.configuration.outputDir+"/"+dirname))
     for(articles <- grouped.get(dirname);
        article <- articles) {
          val internalHtml = html.article(article);
          val extHtml = html.default(article("title"),internalHtml.body,"..")
          val localUrl = article.get("localUrl").getOrElse{
                val path = article.path mkString "/"
                Console.println("missing localUrl for $path")
                path+"-missing.html"
          }
          writePage(localUrl,extHtml.body)
     }
    }

    def page(pageName: String, title: String, content: HtmlFormat.Appendable, topLevelPath: String): Unit =
         writePage(pageName,html.default(title,content.body,topLevelPath).body)

  }

  def topN(x:Seq[MarkdownCompiledPage],n:Int):Seq[MarkdownCompiledPage] =
    byTime(x).take(n)

  def byTime(x:Seq[MarkdownCompiledPage]):Seq[MarkdownCompiledPage] =
    x.sortWith(_("updated") > _("updated"))

  def writePage(page:String, content: String): Unit =
  {
      FileUtils.write(new File(configuration.outputDir + "/" +page), content)
  }

  lazy val configuration = new Configuration

}
