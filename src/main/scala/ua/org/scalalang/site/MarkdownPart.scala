package ua.org.scalalang.site


import com.tristanhunt.knockoff._
import com.tristanhunt.knockoff.DefaultDiscounter._
import java.io.File

case class MarkdownCompiledPage(attributes: Map[String,String], val path: Seq[String]) 


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
      val out = processFile(f, prefix )
      Console.println("processed:"+out)
      List(out)
   } else {
      System.err.println(s"skippong file ${f.getCanonicalPath()} [extension is not .md]");
      Nil
   }
  }
 
  def processFile(f: File, prefix: Seq[String]): MarkdownCompiledPage =
  {
     val (attributes, blocks) = parseFileAttributes(io.Source.fromFile(f), mkName(prefix) )
     generateHtml(blocks, prefix)
     MarkdownCompiledPage(attributes, prefix)
  }

  /**
   * source is markdown file where before markdown we have set of name-value pairs
   * (i.e. attribute:value) separated by horisontal lines from markdown.
   * Tupical file content looks like:
   * <pre>
   *-----------------
   * title: "document title"
   * updated: "20-12-2012"
   *----------------
   * markdown document here.
   * </pre>
   *
   * here we parse header and ret§
   */
  def parseFileAttributes(source: io.Source, fname: String) : (Map[String,String], Seq[Block]) =
  {
   var i = 0;
   var secondDashFound=false
   var withoutAttributes=false
   var attributes = Map[String,String]()
   var rest = IndexedSeq[String]()
   val it = source.getLines()
   while(it.hasNext && !withoutAttributes) {
     val line = it.next
     Console.println(s"${i}:${line}")
     if (i==0)  {
       if (!isDash(line)) {
         Console.println(s"first dash not found for ${fname}, assuming this is plain markdown")
         withoutAttributes = true 
       }
     }  else if (secondDashFound) {
         rest :+= line
     }  else if (isDash(line)) {
         secondDashFound = true
     } else {
         val p = line.indexOf(":")
         if (p == -1) {
            Console.println("${fname}:${i} invalid attributr:value pair, assuming this is plain markdown")
            withoutAttributes = true
         } else {
            val name = line.substring(0,p).trim
            val value = line.substring(p+1).trim
            attributes = attributes.updated(name,value)
         }
     }
     i += 1 
   }
   Console.println("rest="+rest)
   if (!secondDashFound) {
     Console.println(s"second dash not found for ${fname}, assuming this is plain markdown")
     withoutAttributes = true
   }
   val blocks = if (withoutAttributes) {
                   knockoff(source.mkString)
                } else {
                   knockoff(rest.mkString)
                }
   attributes = attributes.updated("body",toXHTML(blocks).toString)
   (attributes, blocks)
  }


  def mkName(name: Seq[String]):String =
       name mkString "/"

  def generateHtml(blocks:Seq[Block], prefix:Seq[String])
  {
    Console.println("html generation is not implemented yet")
  }

  def createOutputDir(prefix: Seq[String]):Unit =
  {
    Console.println(s"create outpud dir: ${prefix mkString "/"} ")
  }

  def isDash(s:String):Boolean =
    s.matches("^-+$")

}
