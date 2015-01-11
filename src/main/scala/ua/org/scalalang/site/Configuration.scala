package ua.org.scalalang.site

class Configuration
{

   def markdownDir: String = "src/main/markdown" 

   def outputDir: String = "target/site"

   def embeddedServerPort = 9000


   def tmpRepoDir = "target/tmp1"
   def repoUrl = "git@github.com:scalaua/scalaua.github.com.git"
   def repoName = "scalaua.github.com"
   def siteBranch = "master"


}
