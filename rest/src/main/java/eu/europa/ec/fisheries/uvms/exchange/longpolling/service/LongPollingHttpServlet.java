package eu.europa.ec.fisheries.uvms.exchange.longpolling.service;

import java.io.IOException;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.europa.ec.fisheries.uvms.exchange.longpolling.constants.LongPollingConstants;
import eu.europa.ec.fisheries.uvms.exchange.service.event.ExchangeLogEvent;
import eu.europa.ec.fisheries.uvms.exchange.service.event.ExchangePluginStatusEvent;
import eu.europa.ec.fisheries.uvms.exchange.service.event.ExchangeSendingQueueEvent;
import eu.europa.ec.fisheries.uvms.notifications.NotificationMessage;

@WebServlet(asyncSupported = true, urlPatterns = { LongPollingConstants.EXCHANGE_LOG_PATH, LongPollingConstants.PLUGIN_STATUS_PATH, LongPollingConstants.SENDING_QUEUE_PATH })
public class LongPollingHttpServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    LongPollingContextHelper asyncContexts;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AsyncContext ctx = req.startAsync(req, resp);
        ctx.setTimeout(LongPollingConstants.ASYNC_TIMEOUT);
        ctx.addListener(new LongPollingAsyncListener() {

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                AsyncContext ctx = event.getAsyncContext();
                asyncContexts.remove(ctx);
                completePoll(ctx, createJsonMessage(null));
            }

        });

        asyncContexts.add(ctx, req.getServletPath());
    }

    public void observeAlarmCreated(@Observes @ExchangeLogEvent NotificationMessage message) throws IOException {
        String guid = (String) message.getProperties().get("guid");
        completePoll(LongPollingConstants.EXCHANGE_LOG_PATH, createJsonMessage(guid));
    }

    public void observePluginStatusEvent(@Observes @ExchangePluginStatusEvent NotificationMessage message) throws IOException {
        String serviceClassName = (String) message.getProperties().get("serviceClassName");
        boolean started = (boolean) message.getProperties().get("started");
        completePoll(LongPollingConstants.PLUGIN_STATUS_PATH, createJsonMessage(serviceClassName, started));
    }

    public void observeSendinqQueueEvent(@Observes @ExchangeSendingQueueEvent NotificationMessage message) throws IOException {
        List<String> messageIdList = (List<String>) message.getProperties().get("messageIds");
        completePoll(LongPollingConstants.SENDING_QUEUE_PATH, createJsonMessageFromList(messageIdList));
    }

    private String createJsonMessageFromList(List<String> guidList) {
    	JsonArrayBuilder array = Json.createArrayBuilder();
        if (guidList != null) {
        	for (String guid : guidList) {
        		array.add(guid);
			}
        }
        return Json.createObjectBuilder().add("ids", array).build().toString();
    }
    
    private String createJsonMessage(String guid) {
        JsonArrayBuilder array = Json.createArrayBuilder();
        if (guid != null) {
            array.add(guid);
        }

        return Json.createObjectBuilder().add("ids", array).build().toString();
    }

    private String createJsonMessage(String className, boolean started) {
        return Json.createObjectBuilder().add(className, started).build().toString();
    }

    private void completePoll(String resourcePath, String message) throws IOException {
        AsyncContext ctx = null;
        while ((ctx = asyncContexts.popContext(resourcePath)) != null) {
            completePoll(ctx, message);
        }
    }

    private void completePoll(AsyncContext ctx, String jsonMessage) throws IOException {
        ctx.getResponse().setContentType("application/json");
        ctx.getResponse().getWriter().write(jsonMessage);
        ctx.complete();
    }

    private abstract static class LongPollingAsyncListener implements AsyncListener {

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            // Do nothing
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
            // Do nothing
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            // Do nothing
        }

    }

}
