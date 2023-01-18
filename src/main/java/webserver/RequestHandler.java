package webserver;

import java.io.*;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.controller.Controller;
import webserver.domain.StatusCodes;
import webserver.domain.request.Request;
import webserver.domain.response.Response;
import webserver.utils.HttpRequestUtils;
import webserver.utils.HttpResponseWriter;

public class RequestHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private Socket connection;
    private HandlerMapping handlerMapping;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
        this.handlerMapping = new HandlerMapping();
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            Request req = HttpRequestUtils.parseHttpRequest(in);
            Response res = new Response();

            handlerMapping.getStaticHandler().chain(req, res);  //get static controller to handle this request
            if(res.getStatusCode() == StatusCodes.NOT_FOUND){  // request is not precessed by static controller
                Controller controller = handlerMapping.getHandler(req);  //get the controller to handle request
                controller.chain(req, res);
            }

            HttpResponseWriter.of(res, out); //write http response and send it back to client side


        } catch (IOException e) {
            logger.debug(e.getMessage());
        }
    }

}
