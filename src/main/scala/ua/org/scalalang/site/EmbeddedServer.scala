package ua.org.scalalang.site

import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler._


object EmbeddedWebServer
{

   def run()
   {
    val server = new Server(Main.configuration.embeddedServerPort);

    val resource_handler = new ResourceHandler();
    resource_handler.setDirectoriesListed(true);
    resource_handler.setWelcomeFiles(Array[String]("index.html"));

    resource_handler.setResourceBase(Main.configuration.outputDir);

    val handlers = new HandlerList();
    handlers.setHandlers(Array[Handler]( resource_handler, new DefaultHandler() ));
    server.setHandler(handlers);

    server.start();
    server.join();
   }

}
