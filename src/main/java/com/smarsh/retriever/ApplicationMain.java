package com.smarsh.retriever;


import com.smarsh.retriever.service.CachedWebContentRetriever;
import com.smarsh.retriever.service.ContentRetriever;
import com.smarsh.retriever.service.Request;
import com.smarsh.retriever.service.Response;

import java.util.logging.Logger;

public class ApplicationMain {
    // Just for the simplicity  of the assignment I use JCL logging. Otherwise I would use SLF4J + logback
    private static final Logger logger = Logger.getLogger(ApplicationMain.class.getCanonicalName());
    static void main() {
        logger.info("Starting application...");

        IO.println("Hello and welcome!");

        String url = IO.readln("Press enter URL...");
        ContentRetriever retriever = new CachedWebContentRetriever();
        Response response = retriever.retrieve(Request.of(url));
        IO.println(response.url());
        IO.println(response.createdAt());
        IO.println(new String(response.content()));
        logger.info("Application finished.");
    }
}
