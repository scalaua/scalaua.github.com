package ua.org.scalalang.site


object Main
{

  def main(args:Array[String]):Unit =
  {
    val markdownPages = MarkdownPart.process()
    scalatexGen()
  }

  def scalatexGen():Unit =
  {
  //TODO
  }

  lazy val configuration = new Configuration

}
