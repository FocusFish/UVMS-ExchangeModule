package eu.europa.ec.fisheries.uvms.exchange.service.bean;

import eu.europa.ec.fisheries.schema.exchange.service.v1.ServiceType;
import eu.europa.ec.fisheries.schema.exchange.source.v1.GetLogListByQueryResponse;
import eu.europa.ec.fisheries.schema.exchange.v1.ExchangeListQuery;
import eu.europa.ec.fisheries.schema.exchange.v1.ExchangeLogType;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.exchange.message.constants.DataSourceQueue;
import eu.europa.ec.fisheries.uvms.exchange.message.consumer.ExchangeMessageConsumer;
import eu.europa.ec.fisheries.uvms.exchange.message.exception.ExchangeMessageException;
import eu.europa.ec.fisheries.uvms.exchange.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMapperException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeDataSourceRequestMapper;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeDataSourceResponseMapper;
import eu.europa.ec.fisheries.uvms.exchange.service.ExchangeService;
import eu.europa.ec.fisheries.uvms.exchange.service.ParameterService;
import eu.europa.ec.fisheries.uvms.exchange.service.exception.ExchangeServiceException;

@Stateless
public class ExchangeServiceBean implements ExchangeService {

    final static Logger LOG = LoggerFactory.getLogger(ExchangeServiceBean.class);

    @EJB
    ParameterService parameterService;

    @EJB
    ExchangeMessageConsumer consumer;

    @EJB
    MessageProducer producer;

    /**
     * {@inheritDoc}
     *
     * @param data
     * @throws ExchangeServiceException
     */
    @Override
    public ServiceType registerService(ServiceType data) throws ExchangeServiceException {
        LOG.info("Register service invoked in service layer");
        try {
            String request = ExchangeDataSourceRequestMapper.mapRegisterServiceToString(data);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class);
            return ExchangeDataSourceResponseMapper.mapToRegisterServiceResponse(response);
        } catch (ExchangeModelMapperException | ExchangeMessageException ex) {
            throw new ExchangeServiceException(ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param data
     * @throws ExchangeServiceException
     */
    @Override
    public ServiceType unregisterService(ServiceType data) throws ExchangeServiceException {
        LOG.info("Unregister service invoked in service layer");
        try {
            String request = ExchangeDataSourceRequestMapper.mapUnregisterServiceToString(data);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class);
            return ExchangeDataSourceResponseMapper.mapToUnregisterServiceResponse(response);
        } catch (ExchangeModelMapperException | ExchangeMessageException ex) {
            throw new ExchangeServiceException(ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return
     * @throws ExchangeServiceException
     */
    @Override
    public List<ServiceType> getServiceList() throws ExchangeServiceException {
        LOG.info("Get list invoked in service layer");
        try {
            String request = ExchangeDataSourceRequestMapper.mapGetServiceListToString();
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class);
            return ExchangeDataSourceResponseMapper.mapToServiceTypeListFromResponse(response);
        } catch (ExchangeModelMapperException | ExchangeMessageException e) {
            throw new ExchangeServiceException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param id
     * @return
     * @throws ExchangeServiceException
     */
    @Override
    public ServiceType getById(Long id) throws ExchangeServiceException {
        LOG.info("Get by id invoked in service layer");
        throw new ExchangeServiceException("Get by id not implemented in service layer");
    }

    /**
     * {@inheritDoc}
     *
     * @param data
     * @throws ExchangeServiceException
     */
    @Override
    public ServiceType update(ServiceType data) throws ExchangeServiceException {
        LOG.info("Update invoked in service layer");
        throw new ExchangeServiceException("Update not implemented in service layer");
    }

    @Override
    public ServiceType getService(String serviceId) throws ExchangeServiceException {
        LOG.info("Get list invoked in service layer");
        try {
            String request = ExchangeDataSourceRequestMapper.mapGetServiceToString(serviceId);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class);
            return ExchangeDataSourceResponseMapper.mapToServiceTypeFromGetServiceResponse(response);
        } catch (ExchangeModelMapperException | ExchangeMessageException e) {
            throw new ExchangeServiceException(e.getMessage());
        }
    }

    @Override
    public ExchangeLogType createExchangeLog(ExchangeLogType exchangeLog) throws ExchangeServiceException {
        LOG.info("Create Exchange log invoked in service layer");
        try {
            String request = ExchangeDataSourceRequestMapper.mapCreateExchangeLogToString(exchangeLog);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class);
            return ExchangeDataSourceResponseMapper.mapToExchangeLogTypeFromCreateExchageLogResponse(response);
        } catch (ExchangeModelMapperException | ExchangeMessageException e) {
            throw new ExchangeServiceException(e.getMessage());
        }
    }

    @Override
    public GetLogListByQueryResponse getExchangeLogByQuery(ExchangeListQuery query) throws ExchangeServiceException {
        LOG.info("Create Exchange log invoked in service layer");
        try {
            String request = ExchangeDataSourceRequestMapper.mapGetExchageLogListByQueryToString(query);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class);
            return ExchangeDataSourceResponseMapper.mapToGetLogListByQueryResponse(response);
        } catch (ExchangeModelMapperException | ExchangeMessageException e) {
            throw new ExchangeServiceException(e.getMessage());
        }
    }

}
