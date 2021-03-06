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
package fish.focus.uvms.exchange.service.entity.serviceregistry;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "service_setting")
@NamedQueries({
        @NamedQuery(name = ServiceSetting.SETTING_FIND_BY_SERVICE, query = "SELECT s FROM ServiceSetting s where s.service.serviceClassName =:serviceClassName ") })

public class ServiceSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String SETTING_FIND_BY_SERVICE = "ServiceSetting.findByServiceId";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "servset_id")
    private UUID id;

    @NotNull
    @Column(name = "servset_updatetime")
    private Instant updatedTime;

    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "servset_upuser")
    private String updatedBy;

    @Size(max = 500)
    @Column(name = "servset_setting")
    private String setting;

    @JsonbTransient
    @JoinColumn(name = "servset_serv_id", referencedColumnName = "serv_id")
    @ManyToOne
    private Service service;

    @Size(max = 4000)
    @Column(name = "servset_value")
    private String value;

    public ServiceSetting() {
    }

    @PrePersist
    @PreUpdate
    public void preUpdate(){
        updatedTime = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Instant updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getUser() {
        return updatedBy;
    }

    public void setUser(String user) {
        this.updatedBy = user;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

	public String getSetting() {
		return setting;
	}

	public void setSetting(String setting) {
		this.setting = setting;
	}

}