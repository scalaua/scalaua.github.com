package ua.org.scalalang.site

import java.nio.file._
import java.nio.file.attribute._
import java.util.EnumSet

object ResourcesPart
{

  def process(): Unit =
  {
    copyTree("src/main/resources",Main.configuration.outputDir)
  }

  def copyTree(inDir:String, outDir:String): Unit =
  {
    val in = getPath(inDir)
    val out = getPath(outDir)

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

  def getPath(path:String): Path =
  {
    val arr = path.split("/")
    // nio getPath is a variadic function with two parameters, call one througth array is not trivial
    if (arr.length==0) {
           throw new IllegalArgumentException("empty path is not supported")
    } else if (arr.length==1) {
          FileSystems.getDefault().getPath(arr(0));
    } else {
          FileSystems.getDefault().getPath(arr(0), arr.toSeq.drop(1) :_* );
    }
  }


}
