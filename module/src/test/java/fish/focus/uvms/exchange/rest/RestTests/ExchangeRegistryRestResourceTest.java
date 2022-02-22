package fish.focus.uvms.exchange.rest.RestTests;

import fish.focus.schema.exchange.plugin.types.v1.PluginType;
import fish.focus.schema.exchange.plugin.v1.ExchangePluginMethod;
import fish.focus.schema.exchange.plugin.v1.StartRequest;
import fish.focus.schema.exchange.plugin.v1.StopRequest;
import fish.focus.schema.exchange.service.v1.CapabilityTypeType;
import fish.focus.uvms.exchange.rest.BuildExchangeRestTestDeployment;
import fish.focus.uvms.exchange.rest.JMSHelper;
import fish.focus.uvms.exchange.rest.RestHelper;
import fish.focus.uvms.exchange.model.mapper.JAXBMarshaller;
import fish.focus.uvms.exchange.rest.dto.Plugin;
import fish.focus.uvms.exchange.rest.filter.AppError;
import fish.focus.uvms.exchange.service.dao.ServiceRegistryDaoBean;
import fish.focus.uvms.exchange.service.entity.serviceregistry.Service;
import fish.focus.uvms.exchange.service.entity.serviceregistry.ServiceCapability;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.TextMessage;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class ExchangeRegistryRestResourceTest extends BuildExchangeRestTestDeployment {

    @Inject
    ServiceRegistryDaoBean serviceRegistryDao;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Test
    @OperateOnDeployment("exchangeservice")
    public void getServiceListTest() throws Exception {

        List<Plugin> responseDto = getWebTarget()
                .path("plugin")
                .path("list")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<Plugin>>() {});

        assertNotNull(responseDto);
        assertFalse(responseDto.isEmpty());
        assertEquals("STARTED", responseDto.get(0).getStatus());
        assertEquals("ManualMovement", responseDto.get(0).getName());
        assertEquals("ManualMovement", responseDto.get(0).getServiceClassName());
    }

    @Test
    @OperateOnDeployment("exchangeservice")
    public void getServiceByCapabilityTest() {
        List<Plugin> before = getWebTarget()
                .path("plugin")
                .path("capability/SEND_REPORT")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<>() {});

        Service service = RestHelper.createBasicService("Test Service Name " + UUID.randomUUID(), "Service Class Name " + UUID.randomUUID(), PluginType.OTHER);
        ServiceCapability capability = new ServiceCapability();
        capability.setService(service);
        capability.setUpdatedBy("Exchange Tests");
        capability.setUpdatedTime(Instant.now());
        capability.setCapability(CapabilityTypeType.SEND_REPORT);
        capability.setValue(true);
        service.getServiceCapabilityList().add(capability);
        service = serviceRegistryDao.createEntity(service);

        List<Plugin> after = getWebTarget()
                .path("plugin")
                .path("capability/SEND_REPORT")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<>() {});

        assertEquals(before.size() + 1, after.size());
        Plugin plugin = after.get(after.size()-1);
        assertEquals(service.getName(), plugin.getName());
    }

    @Test
    @OperateOnDeployment("exchangeservice")
    public void serviceStopTest() throws Exception {
        JMSHelper jmsHelper = new JMSHelper(connectionFactory);
        String serviceClassName = "Service Class Name " + UUID.randomUUID().toString();

        Service s = RestHelper.createBasicService("Test Service Name " + UUID.randomUUID().toString(), serviceClassName, PluginType.OTHER);
        s = serviceRegistryDao.createEntity(s);

        jmsHelper.registerSubscriber("ServiceName = '" + serviceClassName + "'");
        String stringResponse = getWebTarget()
                .path("plugin/stop")
                .path(serviceClassName)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(s), String.class);

        assertNotNull(stringResponse);
        assertTrue(stringResponse.contains("true"));
        TextMessage message = (TextMessage)jmsHelper.listenOnEventBus("ServiceName = '" + serviceClassName + "'", 5000l);
        StopRequest response = JAXBMarshaller.unmarshallTextMessage(message, StopRequest.class);
        assertEquals(ExchangePluginMethod.STOP, response.getMethod());

    }

    @Test
    @OperateOnDeployment("exchangeservice")
    public void serviceStartTest() throws Exception {
        JMSHelper jmsHelper = new JMSHelper(connectionFactory);
        String serviceClassName = "Service Class Name " + UUID.randomUUID().toString();

        Service s = RestHelper.createBasicService("Test Service Name " + UUID.randomUUID().toString(), serviceClassName, PluginType.OTHER);
        s = serviceRegistryDao.createEntity(s);

        jmsHelper.registerSubscriber("ServiceName = '" + serviceClassName + "'");
        String stringResponse = getWebTarget()
                .path("plugin/start")
                .path(serviceClassName)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(s), String.class);

        assertNotNull(stringResponse);
        assertTrue(stringResponse.contains("true"));
        TextMessage message = (TextMessage)jmsHelper.listenOnEventBus("ServiceName = '" + serviceClassName + "'", 5000l);
        StartRequest response = JAXBMarshaller.unmarshallTextMessage(message, StartRequest.class);
        assertEquals(ExchangePluginMethod.START, response.getMethod());

    }

    @Test
    @OperateOnDeployment("exchangeservice")
    public void serviceStopAndStartNonexistantServiceTest() {
        Response response = getWebTarget()
                .path("plugin/stop")
                .path("Non-valid service")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json("test"), Response.class);

        assertEquals(200, response.getStatus());
        AppError appError = response.readEntity(AppError.class);
        assertEquals(500, appError.code.intValue());

        String stringResponse = appError.description;
        assertNotNull(stringResponse);
        assertTrue(stringResponse.contains("Service with service class name: Non-valid service does not exist"));

        response = getWebTarget()
                .path("plugin/start")
                .path("Non-valid service")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json("test"), Response.class);

        assertEquals(200, response.getStatus());
        appError = response.readEntity(AppError.class);
        assertEquals(500, appError.code.intValue());
        stringResponse = appError.description;

        assertNotNull(stringResponse);
        assertTrue(stringResponse.contains("Service with service class name: Non-valid service does not exist"));
    }
}
