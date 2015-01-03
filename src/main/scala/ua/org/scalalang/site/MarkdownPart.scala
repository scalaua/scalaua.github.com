package ua.org.scalalang.site


import com.tristanhunt.knockoff._
import com.tristanhunt.knockoff.DefaultDiscounter._
import java.io.File

case class MarkdownCompiledPage(title: String, date: Option[String], val path: Seq[String]) 


object MarkdownPart
{

  def process():List[MarkdownCompiledPage] =
   processDirOrFile(new File(Main.configuration.markdownDir),Seq())

  def processDirOrFile(f: File, prefix: Seq[String]): List[MarkdownCompiledPage] =
  {
   Console.println(s"processing ${f.getCanonicalPath()}")
   if (f.isDirectory) {
      createOutputDir(prefix)
      for(cf <- f.listFiles.toList;
          page <- processDirOrFile(cf,prefix :+ cf.getName) ) yield page
   } else if (f.getName.endsWith(".md")) {
      val out = processFile(f, prefix );
      List(out)
   } else {
      System.err.println(s"skippong file ${f.getCanonicalPath()} [extension is not .md]");
      Nil
   }
  }
 
  def processFile(f: File, prefix: Seq[String]): MarkdownCompiledPage =
  {
     val blocks = knockoff(io.Source.fromFile(f).mkString)
     val (title,date) = determinateTitleAndDate(blocks);
     generateHtml(blocks, prefix);
     MarkdownCompiledPage(title,date,prefix)
  }

  def determinateTitleAndDate(blocks: Seq[Block]):(String,String) =
  {
    Console.println("blocks:"+blocks);
    ("undefined","01-01-1970")
  }
 
  def generateHtml(blocks:Seq[Block], prefix:Seq[String])
  {
    Console.println("html generation is not implemented yet")
  }

  def createOutputDir(prefix: Seq[String]):Unit =
  {
    Console.println(s"create outpud dir: ${prefix mkString "/"} ")
  }


}
