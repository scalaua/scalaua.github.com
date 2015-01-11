package ua.org.scalalang.site



import java.io.File
import java.io.StringWriter

case class MarkdownCompiledPage(attributes: Map[String,String], val path: Seq[String]) 
{

  def apply(name:String):String =
   attributes.get(name).getOrElse{
      Console.println(s"attribute ${name} not found in template ${path.mkString("/")}");
      s"[missing ${name}]"
   }


  def get(name:String):Option[String] = 
   attributes.get(name)

}


object MarkdownPart
{

  def process():List[MarkdownCompiledPage] =
   processDirOrFile(new File(Main.configuration.markdownDir),Seq())

  def processDirOrFile(f: File, prefix: Seq[String]): List[MarkdownCompiledPage] =
  {
   Console.println(s"processing ${f.getCanonicalPath()}")
   if (f.isDirectory) {
      for(cf <- f.listFiles.toList;
          page <- processDirOrFile(cf,prefix :+ cf.getName) ) yield page
   } else if (f.getName.endsWith(".md")) {
      val out = processFile(f, prefix )
      List(out)
   } else {
      System.err.println(s"skippong file ${f.getCanonicalPath()} [extension is not .md]");
      Nil
   }
  }
 
  def processFile(f: File, prefix: Seq[String]): MarkdownCompiledPage =
  {
     val attributes = parseFileAttributes(io.Source.fromFile(f), mkName(prefix) )
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
   * markdown is passed to
   * here we parse header and ret§
   */
  def parseFileAttributes(source: io.Source, fname: String) : Map[String,String] =
  {
   var i = 0;
   var secondDashFound=false
   var withoutAttributes=false
   var attributes = Map[String,String]()
   var rest = IndexedSeq[String]()
   val it = source.getLines()
   while(it.hasNext && !withoutAttributes) {
     val line = it.next
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
            if (!line.isEmpty) {
              Console.println("${fname}:${i} invalid attributr:value pair, assuming this is plain markdown")
              withoutAttributes = true
            }
         } else {
            val name = line.substring(0,p).trim
            val value = line.substring(p+1).trim
            attributes = attributes.updated(name,value)
         }
     }
     i += 1 
   }
   if (!secondDashFound) {
     Console.println(s"second dash not found for ${fname}, assuming this is plain markdown")
     withoutAttributes = true
   }

   val markdownLines = if (withoutAttributes) source else rest

   attributes = attributes.updated("body",markdownToHtml(markdownLines mkString "\n"))


   for(t <- timeInFilename(fname)) {
       attributes = attributes.updated("ftime",t)
       if (attributes.get("updated").isEmpty) {
           attributes = attributes.updated("updated",t)
       }
   }

   attributes = attributes.updated("localUrl",fname.substring(0,fname.length-2)+"html")

   attributes
  }

  def markdownToHtml(markdown:String):String =
  {
   import scala.xml._
   import com.tristanhunt.knockoff.DefaultDiscounter._
   val blocks = knockoff(markdown)
   val xml = toXHTML(blocks)
   val sw = new StringWriter
   XML.write( sw, xml, "UTF-8", false, null )
   sw.toString
   //val pegdown = new org.pegdown.PegDownProcessor();
   //pegdown.markdownToHtml(markdownLines.mkString)
  }

  def timeInFilename(fname:String):Option[String] =
  {
    val lastPart = fname.split("/").last
    val date ="""(\d\d\d\d)-(\d\d)-(\d\d)-.*""".r
    lastPart match {
          case date(year,month,day) => Some(s"${year}-${month}-${day}")
          case _ => None
    }
  }  

  def mkName(name: Seq[String]):String =
       name mkString "/"


  def isDash(s:String):Boolean =
    s.matches("^-+$")

}
