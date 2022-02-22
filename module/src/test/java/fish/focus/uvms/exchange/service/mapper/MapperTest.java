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
package fish.focus.uvms.exchange.service.mapper;

import fish.focus.schema.exchange.service.v1.CapabilityListType;
import fish.focus.schema.exchange.service.v1.ServiceType;
import fish.focus.schema.exchange.service.v1.SettingListType;
import fish.focus.uvms.exchange.service.MockData;
import fish.focus.uvms.exchange.service.entity.serviceregistry.Service;
import fish.focus.uvms.exchange.service.entity.serviceregistry.ServiceCapability;
import fish.focus.uvms.exchange.service.entity.serviceregistry.ServiceSetting;
import fish.focus.uvms.exchange.service.mapper.ServiceMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class MapperTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testEntityToModel() {
        UUID id = UUID.randomUUID();
        Service entity = MockData.getEntity(id);
        List<ServiceCapability> capabilityList = new ArrayList<>();
        entity.setServiceCapabilityList(capabilityList);
        List<ServiceSetting> settingList = new ArrayList<>();
        entity.setServiceSettingList(settingList);
        // mockDaoToEntity();
        ServiceType result = ServiceMapper.toServiceModel(entity);

        assertSame(entity.getName(), result.getName());
        assertSame(entity.getServiceClassName(), result.getServiceClassName());
    }

    @Test
    public void testModelToEntity() {
        int id = 1;
        ServiceType model = MockData.getModel(id);
        CapabilityListType capabilityListType = MockData.getCapabilityList();
        SettingListType settingListType = MockData.getSettingList();
        // mockDaoToEntity();

        Service result = ServiceMapper.toServiceEntity(model, capabilityListType, settingListType, "TEST");

        assertSame(model.getName(), result.getName());
        assertSame(model.getServiceClassName(), result.getServiceClassName());
    }

    @Test
    public void testEntityAndModelToEntity() {
        UUID id = UUID.randomUUID();
        Service entity = MockData.getEntity(id);
        ServiceType service = MockData.getModel(1);
        CapabilityListType capabilityListType = MockData.getCapabilityList();
        SettingListType settingListType = MockData.getSettingList();
        // mockDaoToEntity();

        Service result = ServiceMapper.toServiceEntity(entity, service, capabilityListType, settingListType, "TEST");

        assertSame(entity.getName(), result.getName());
        assertSame(entity.getServiceClassName(), result.getServiceClassName());
    }

    @Test
    public void testEntityAndModelToModel() {
        Service entity = MockData.getEntity(UUID.randomUUID());
        List<ServiceCapability> capabilityList = new ArrayList<>();
        entity.setServiceCapabilityList(capabilityList);
        List<ServiceSetting> settingList = new ArrayList<>();
        entity.setServiceSettingList(settingList);
        // mockDaoToEntity();
        ServiceType result = ServiceMapper.toServiceModel(entity);

        assertSame(entity.getName(), result.getName());
        assertSame(entity.getServiceClassName(), result.getServiceClassName());
    }

    @Test
    public void testUpsert() {
        String newValue = "NEW_VALUE";

        Service entity = MockData.getEntity(UUID.randomUUID());
        entity.setServiceCapabilityList(MockData.getEntityCapabilities(entity));
        entity.setServiceSettingList(MockData.getEntitySettings(entity));

        List<ServiceSetting> updateSettings = new ArrayList<>();
        ServiceSetting updateSetting = new ServiceSetting();
        updateSetting.setSetting(MockData.SETTING_KEY);
        updateSetting.setValue(newValue);
        updateSettings.add(updateSetting);
        List<ServiceSetting> list = ServiceMapper.mapSettingsList(entity, updateSettings, "TEST");

        assertFalse(list.isEmpty());
        for (ServiceSetting setting : list) {
            assertSame(setting.getValue(), newValue);
        }

        List<ServiceSetting> newSettings = new ArrayList<>();
        ServiceSetting newSetting = new ServiceSetting();
        newSetting.setSetting("NEW.KEY");
        newSetting.setValue("NEW.VALUE");
        newSettings.add(newSetting);
        list = ServiceMapper.mapSettingsList(entity, newSettings, "TEST");

        assertEquals(1, list.size());
    }
}
