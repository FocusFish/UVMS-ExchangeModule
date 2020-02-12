/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.exchange.service.dao;

import eu.europa.ec.fisheries.schema.exchange.v1.ExchangeHistoryListQuery;
import eu.europa.ec.fisheries.schema.exchange.v1.TypeRefType;
import eu.europa.ec.fisheries.uvms.exchange.service.entity.exchangelog.ExchangeLog;
import eu.europa.ec.fisheries.uvms.exchange.service.entity.exchangelog.ExchangeLogStatus;
import eu.europa.ec.fisheries.uvms.exchange.service.search.ExchangeSearchField;
import eu.europa.ec.fisheries.uvms.exchange.service.search.SearchFieldMapper;
import eu.europa.ec.fisheries.uvms.exchange.service.search.SearchValue;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.util.*;

@Stateless
public class ExchangeLogDaoBean extends AbstractDao {

    private final static Logger LOG = LoggerFactory.getLogger(ExchangeLogDaoBean.class);

    public List<ExchangeLogStatus> getExchangeLogStatusHistory(String sql, ExchangeHistoryListQuery searchQuery) {
        LOG.debug("SQL query for status history " + sql);
        TypedQuery<ExchangeLogStatus> query = em.createQuery(sql, ExchangeLogStatus.class);
        if (searchQuery.getStatus() != null && !searchQuery.getStatus().isEmpty()) {
            query.setParameter("status", searchQuery.getStatus());
        }
        if (searchQuery.getType() != null && !searchQuery.getType().isEmpty()) {
            query.setParameter("type", searchQuery.getType());
        }
        if (searchQuery.getTypeRefDateFrom() != null) {
            Instant from = searchQuery.getTypeRefDateFrom().toInstant();
            query.setParameter("from", from);
        }
        if (searchQuery.getTypeRefDateTo() != null) {
            Instant to = searchQuery.getTypeRefDateTo().toInstant();
            query.setParameter("to", to);
        }
        return query.getResultList();
    }

    public List<ExchangeLog> getExchangeLogListPaginated(Integer page, Integer listSize, String sql, List<SearchValue> searchKeyValues) {
        LOG.debug("SQL QUERY IN LIST PAGINATED: " + sql);
        TypedQuery<ExchangeLog> query = em.createQuery(sql, ExchangeLog.class);
        HashMap<ExchangeSearchField, List<SearchValue>> orderedValues = SearchFieldMapper.combineSearchFields(searchKeyValues);
        setQueryParameters(query, orderedValues);
        query.setFirstResult(listSize * (page - 1));
        query.setMaxResults(listSize);
        return query.getResultList();
    }

    public Long getExchangeLogListSearchCount(String countSql, List<SearchValue> searchKeyValues) {
        LOG.debug("SQL QUERY IN LIST COUNT: " + countSql);
        TypedQuery<Long> query = em.createQuery(countSql, Long.class);
        HashMap<ExchangeSearchField, List<SearchValue>> orderedValues = SearchFieldMapper.combineSearchFields(searchKeyValues);
        setQueryParameters(query, orderedValues);
        return query.getSingleResult();
    }

    public ExchangeLog getExchangeLogByGuid(UUID logGuid) {
        return em.find(ExchangeLog.class, logGuid);
    }

    private void setQueryParameters(Query query, HashMap<ExchangeSearchField, List<SearchValue>> orderedValues) {
        for (Map.Entry<ExchangeSearchField, List<SearchValue>> criteria : orderedValues.entrySet()) {
            if (criteria.getValue().size() > 1) {
                query.setParameter(criteria.getKey().getSQLReplacementToken(), criteria.getValue());
            } else {
                query.setParameter(criteria.getKey().getSQLReplacementToken(), SearchFieldMapper.buildValueFromClassType(criteria.getValue().get(0), criteria.getKey().getClazz()));
            }
        }
    }

    public ExchangeLog createLog(ExchangeLog log) {
        em.persist(log);
        return log;
    }

    public ExchangeLog getExchangeLogByGuid(UUID logGuid, TypeRefType typeRefType) {
        try {
            TypedQuery<ExchangeLog> query = em.createNamedQuery(ExchangeLog.LOG_BY_GUID, ExchangeLog.class);
            query.setParameter("typeRefType", typeRefType);
            query.setParameter("guid", logGuid);
            return query.getSingleResult();
        } catch (NoResultException ignored) {
            // Don't need to actually do anything when no entity was found!
            LOG.error("Error when getting log by id: {} and type: {}", logGuid, typeRefType);
        }
        return null;
    }

    public List<ExchangeLog> getExchangeLogByRangeOfRefGuids(List<UUID> logGuids) {
        if (CollectionUtils.isEmpty(logGuids)) {
            return new ArrayList<>();
        }
        TypedQuery<ExchangeLog> query = em.createNamedQuery(ExchangeLog.LOG_BY_TYPE_RANGE_OF_REF_GUIDS, ExchangeLog.class);
        query.setParameter("refGuids", logGuids);
        return query.getResultList();
    }


    public List<ExchangeLog> getExchangeLogByTypesRefAndGuid(UUID typeRefGuid, List<TypeRefType> types) {
        try {
            TypedQuery<ExchangeLog> namedQuery = em.createNamedQuery(ExchangeLog.LOG_BY_TYPE_REF_AND_GUID, ExchangeLog.class);
            namedQuery.setParameter("typeRefGuid", typeRefGuid);
            namedQuery.setParameter("typeRefTypes", types);
            return namedQuery.getResultList();
        } catch (NoResultException e) {
            // Don't need to actually do anything when no entity was found!
        }
        return null;
    }

    public ExchangeLog updateLog(ExchangeLog entity) {
        em.merge(entity);
        em.flush();
        return entity;
    }

    public ExchangeLog getLatestLog() {
        TypedQuery<ExchangeLog> namedQuery = em.createNamedQuery(ExchangeLog.LATEST_LOG, ExchangeLog.class);
        namedQuery.setMaxResults(1);
        return namedQuery.getResultList().get(0);
    }
}