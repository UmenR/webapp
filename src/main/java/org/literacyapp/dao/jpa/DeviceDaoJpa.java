package org.literacyapp.dao.jpa;

import javax.persistence.NoResultException;
import org.literacyapp.dao.DeviceDao;

import org.springframework.dao.DataAccessException;

import org.literacyapp.model.Device;

public class DeviceDaoJpa extends GenericDaoJpa<Device> implements DeviceDao {

    @Override
    public Device read(String deviceId) throws DataAccessException {
        try {
            return (Device) em.createQuery(
                "SELECT d " +
                "FROM Device d " +
                "WHERE d.deviceId = :deviceId")
                .setParameter("deviceId", deviceId)
                .getSingleResult();
        } catch (NoResultException e) {
            logger.warn("Device \"" + deviceId + "\" was not found");
            return null;
        }
    }
}
