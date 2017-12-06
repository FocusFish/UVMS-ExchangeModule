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
package eu.europa.ec.fisheries.uvms.exchange.dao;

import java.util.List;

import javax.ejb.Local;

import eu.europa.ec.fisheries.uvms.exchange.entity.unsent.UnsentMessage;
import eu.europa.ec.fisheries.uvms.exchange.exception.ExchangeDaoException;
import eu.europa.ec.fisheries.uvms.exchange.exception.NoEntityFoundException;

@Local
public interface UnsentMessageDao {

	public UnsentMessage create(UnsentMessage unsentMessage) throws ExchangeDaoException;
	
	public UnsentMessage remove(UnsentMessage unsentMessage) throws ExchangeDaoException;
	
	public UnsentMessage getByGuid(String guid) throws NoEntityFoundException;
	
	public List<UnsentMessage> getAll() throws ExchangeDaoException;
}