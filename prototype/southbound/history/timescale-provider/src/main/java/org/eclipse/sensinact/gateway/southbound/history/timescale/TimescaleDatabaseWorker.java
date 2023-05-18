/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.timescale;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.southbound.history.api.HistoricalQueries;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.typedevent.TypedEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TimescaleDatabaseWorker implements TypedEventHandler<ResourceDataNotification>, HistoricalQueries {

    private static final String INSERT_TEMPLATE = "INSERT INTO %s ( time, model, provider, service, resource, data ) values ( ?, ?, ?, ?, ?, %s );";

    private static final String SINGLE_TEMPLATE = "SELECT time, num, text, geo FROM ( "
            + "( SELECT time, data AS num, NULL AS text, NULL AS geo FROM sensinact.numeric_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? ORDER BY time DESC LIMIT 1 ) "
            + "UNION ALL "
            + "( SELECT time, NULL AS num, data AS text, NULL AS geo FROM sensinact.text_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? ORDER BY time DESC LIMIT 1 ) "
            + "UNION ALL "
            + "( SELECT time, NULL AS num, NULL AS text, ST_AsGeoJSON(data) AS geo FROM sensinact.geo_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? ORDER BY time DESC LIMIT 1 ) "
            + ") results ORDER BY time DESC LIMIT 1;";
    private static final String SINGLE_TEMPLATE_WITHOUT_TIME = "SELECT time, num, text, geo FROM ( "
            + "( SELECT time, data AS num, NULL AS text, NULL AS geo FROM sensinact.numeric_data WHERE provider = ? AND service = ? AND resource = ? ORDER BY time ASC LIMIT 1 ) "
            + "UNION ALL "
            + "( SELECT time, NULL AS num, data AS text, NULL AS geo FROM sensinact.text_data WHERE provider = ? AND service = ? AND resource = ? ORDER BY time ASC LIMIT 1 ) "
            + "UNION ALL "
            + "( SELECT time, NULL AS num, NULL AS text, ST_AsGeoJSON(data) AS geo FROM sensinact.geo_data WHERE provider = ? AND service = ? AND resource = ? ORDER BY time ASC LIMIT 1 ) "
            + ") results ORDER BY time DESC LIMIT 1;";

    private static final String RANGE_TEMPLATE = "SELECT time, num, text, geo FROM ( "
            + "( SELECT time, data AS num, NULL AS text, NULL AS geo FROM sensinact.numeric_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? AND time >= ? ORDER BY time ASC ) "
            + "UNION ALL "
            + "( SELECT time, NULL AS num, data AS text, NULL AS geo FROM sensinact.text_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? AND time >= ? ORDER BY time ASC ) "
            + "UNION ALL "
            + "( SELECT time, NULL AS num, NULL AS text, ST_AsGeoJSON(data) AS geo FROM sensinact.geo_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? AND time >= ? ORDER BY time ASC ) "
            + ") results ORDER BY time ASC OFFSET ? LIMIT 501;";
    private static final String RANGE_TEMPLATE_WITHOUT_LIMIT = "SELECT time, num, text, geo FROM ( "
            + "( SELECT time, data AS num, NULL AS text, NULL AS geo FROM sensinact.numeric_data WHERE provider = ? AND service = ? AND resource = ? AND time >= ? ORDER BY time ASC ) "
            + "UNION ALL "
            + "( SELECT time, NULL AS num, data AS text, NULL AS geo FROM sensinact.text_data WHERE provider = ? AND service = ? AND resource = ? AND time >= ? ORDER BY time ASC ) "
            + "UNION ALL "
            + "( SELECT time, NULL AS num, NULL AS text, ST_AsGeoJSON(data) AS geo FROM sensinact.geo_data WHERE provider = ? AND service = ? AND resource = ? AND time >= ? ORDER BY time ASC ) "
            + ") results ORDER BY time ASC OFFSET ? LIMIT 501;";
    private static final String RANGE_TEMPLATE_WITHOUT_START = "SELECT reverse.* from ( SELECT time, num, text, geo FROM ( "
            + "( SELECT time, data AS num, NULL AS text, NULL AS geo FROM sensinact.numeric_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? ORDER BY time DESC ) "
            + "UNION ALL "
            + "( SELECT time, NULL AS num, data AS text, NULL AS geo FROM sensinact.text_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? ORDER BY time DESC ) "
            + "UNION ALL "
            + "( SELECT time, NULL AS num, NULL AS text, ST_AsGeoJSON(data) AS geo FROM sensinact.geo_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? ORDER BY time DESC ) "
            + ") results ORDER BY time DESC OFFSET ? LIMIT 500 ) reverse ORDER BY time ASC;";
    private static final String RANGE_TEMPLATE_WITHOUT_START_OR_LIMIT = "SELECT reverse.* from ( SELECT time, num, text, geo FROM ( "
            + "( SELECT time, data AS num, NULL AS text, NULL AS geo FROM sensinact.numeric_data WHERE provider = ? AND service = ? AND resource = ? ORDER BY time DESC) "
            + "UNION ALL "
            + "( SELECT time, NULL AS num, data AS text, NULL AS geo FROM sensinact.text_data WHERE provider = ? AND service = ? AND resource = ? ORDER BY time DESC ) "
            + "UNION ALL "
            + "( SELECT time, NULL AS num, NULL AS text, ST_AsGeoJSON(data) AS geo FROM sensinact.geo_data WHERE provider = ? AND service = ? AND resource = ? ORDER BY time DESC ) "
            + ") results ORDER BY time DESC OFFSET ? LIMIT 500 ) reverse ORDER BY time ASC;";

    private static final String COUNT_TEMPLATE = "SELECT SUM(c) FROM ( "
            + "( SELECT COUNT(time) as c FROM sensinact.numeric_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? AND time >= ? ) "
            + "UNION ALL "
            + "( SELECT COUNT(time) as c FROM sensinact.text_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? AND time >= ? ) "
            + "UNION ALL "
            + "( SELECT COUNT(time) as c FROM sensinact.geo_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? AND time >= ? ) "
            + ") results;";
    private static final String COUNT_TEMPLATE_WITHOUT_LIMIT = "SELECT SUM(c) FROM ( "
            + "( SELECT COUNT(time) as c FROM sensinact.numeric_data WHERE provider = ? AND service = ? AND resource = ? AND time >= ? ) "
            + "UNION ALL "
            + "( SELECT COUNT(time) as c FROM sensinact.text_data WHERE provider = ? AND service = ? AND resource = ? AND time >= ? ) "
            + "UNION ALL "
            + "( SELECT COUNT(time) as c FROM sensinact.geo_data WHERE provider = ? AND service = ? AND resource = ? AND time >= ? ) "
            + ") results;";
    private static final String COUNT_TEMPLATE_WITHOUT_START = "SELECT SUM(c) FROM ( "
            + "( SELECT COUNT(time) as c FROM sensinact.numeric_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? ) "
            + "UNION ALL "
            + "( SELECT COUNT(time) as c FROM sensinact.text_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? ) "
            + "UNION ALL "
            + "( SELECT COUNT(time) as c FROM sensinact.geo_data WHERE provider = ? AND service = ? AND resource = ? AND time <= ? ) "
            + ") results;";
    private static final String COUNT_TEMPLATE_WITHOUT_START_OR_LIMIT = "SELECT SUM(c) FROM ( "
            + "( SELECT COUNT(time) as c FROM sensinact.numeric_data WHERE provider = ? AND service = ? AND resource = ? ) "
            + "UNION ALL "
            + "( SELECT COUNT(time) as c FROM sensinact.text_data WHERE provider = ? AND service = ? AND resource = ? ) "
            + "UNION ALL "
            + "( SELECT COUNT(time) as c FROM sensinact.geo_data WHERE provider = ? AND service = ? AND resource = ? ) "
            + ") results;";

    private static final Logger logger = LoggerFactory.getLogger(TimescaleDatabaseWorker.class);

    private final TransactionControl txControl;

    private final Supplier<Connection> connectionSupplier;

    private final ObjectMapper mapper = new ObjectMapper();

    public TimescaleDatabaseWorker(TransactionControl txControl, Supplier<Connection> connectionSupplier) {
        super();
        this.txControl = txControl;
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public void notify(String topic, ResourceDataNotification event) {

        if (logger.isDebugEnabled()) {
            logger.debug("Update received for topic {} and the data will be stored", topic);
        }

        String command;
        Object value;

        if (isGeographic(event)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Event is geographic");
            }
            command = String.format(INSERT_TEMPLATE, "sensinact.geo_data",
                    "(SELECT ST_GeomFromGeoJSON( ? )::geography)");
            String tmpValue;
            if (event.newValue == null) {
                tmpValue = "{\"type\":\"Point\", \"coordinates\":[]}";
            } else if (event.newValue instanceof GeoJsonObject) {
                try {
                    tmpValue = mapper.writeValueAsString(event.newValue);
                } catch (JsonProcessingException e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unable to serialize geographic data for {}", topic, e);
                    }
                    return;
                }
            } else {
                tmpValue = event.newValue.toString();
            }
            value = tmpValue;
        } else if (isNumber(event.type)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Event is numeric");
            }
            command = String.format(INSERT_TEMPLATE, "sensinact.numeric_data", "?");
            value = event.newValue;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Event is being treated as text");
            }
            command = String.format(INSERT_TEMPLATE, "sensinact.text_data", "?");
            value = event.newValue == null ? null : event.newValue.toString();
        }
        Connection conn = connectionSupplier.get();

        try {
            txControl.required(() -> {

                PreparedStatement ps = conn.prepareStatement(command);
                ps.setTimestamp(1, Timestamp.from(event.timestamp));
                ps.setString(2, event.model);
                ps.setString(3, event.provider);
                ps.setString(4, event.service);
                ps.setString(5, event.resource);
                ps.setObject(6, value);

                return ps.execute();
            });
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Unable to store data for {}", topic, e);
            }
        }

    }

    private boolean isGeographic(ResourceDataNotification event) {
        return GeoJsonObject.class.isAssignableFrom(event.type);
    }

    private static final Set<Class<?>> primitiveNumbers = Set.of(byte.class, short.class, int.class, long.class,
            float.class, double.class);

    private boolean isNumber(Class<?> type) {
        return primitiveNumbers.contains(type) || Number.class.isAssignableFrom(type);
    }

    @Override
    public TimedValue<?> getSingleValue(String provider, String service, String resource, ZonedDateTime time) {

        Connection conn = connectionSupplier.get();

        try {
            return txControl.required(() -> {

                PreparedStatement ps;
                if (time == null) {
                    ps = conn.prepareStatement(SINGLE_TEMPLATE_WITHOUT_TIME);
                    setVariables(ps, provider, service, resource);
                } else {
                    ps = conn.prepareStatement(SINGLE_TEMPLATE);
                    setVariables(ps, provider, service, resource,
                            Timestamp.from(time == null ? Instant.now() : time.toInstant()));
                }

                ResultSet rs = ps.executeQuery();

                TimedValue<?> result;
                if (rs.next()) {
                    result = toTimedValue(rs);
                } else {
                    result = new TimedValueImpl<>(null, null);
                }
                return result;
            });
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Unable to locate data for {} {} {}", provider, service, resource, e);
            }
            throw new RuntimeException(e);
        }
    }

    private void setVariables(PreparedStatement ps, Object... variables) throws SQLException {
        int idx = 1;
        for (int i = 0; i < 3; i++) {
            for (Object o : variables) {
                ps.setObject(idx++, o);
            }
        }
    }

    private TimedValue<?> toTimedValue(ResultSet rs) throws Exception {
        Object value = null;
        Instant dataTime = rs.getTimestamp("time").toInstant();
        BigDecimal num = rs.getBigDecimal("num");
        if (num != null) {
            if (num.scale() <= 0) {
                value = num.longValueExact();
            } else {
                value = num.doubleValue();
            }
        } else {
            String text = rs.getString("text");
            if (text != null) {
                value = text;
            } else {
                String geo = rs.getString("geo");
                if (geo != null) {
                    value = mapper.readValue(geo, GeoJsonObject.class);
                }
            }
        }
        return new TimedValueImpl<>(value, dataTime);
    }

    @Override
    public List<TimedValue<?>> getValueRange(String provider, String service, String resource, ZonedDateTime fromTime,
            ZonedDateTime toTime, Integer skip) {
        Integer toSkip = skip == null ? Integer.valueOf(0) : skip;
        Connection conn = connectionSupplier.get();

        try {
            return txControl.required(() -> {

                List<TimedValue<?>> list = new ArrayList<>(501);

                PreparedStatement ps;
                if (toTime == null) {
                    if (fromTime == null) {
                        ps = conn.prepareStatement(RANGE_TEMPLATE_WITHOUT_START_OR_LIMIT);
                        setVariables(ps, provider, service, resource);
                        ps.setInt(10, toSkip);
                    } else {
                        ps = conn.prepareStatement(RANGE_TEMPLATE_WITHOUT_LIMIT);
                        setVariables(ps, provider, service, resource, Timestamp.from(fromTime.toInstant()));
                        ps.setInt(13, toSkip);
                    }
                } else {
                    if (fromTime == null) {
                        ps = conn.prepareStatement(RANGE_TEMPLATE_WITHOUT_START);
                        setVariables(ps, provider, service, resource, Timestamp.from(toTime.toInstant()));
                        ps.setInt(13, toSkip);
                    } else {
                        ps = conn.prepareStatement(RANGE_TEMPLATE);
                        setVariables(ps, provider, service, resource, Timestamp.from(toTime.toInstant()),
                                Timestamp.from(fromTime.toInstant()));
                        ps.setInt(16, toSkip);
                    }
                }

                ResultSet rs = ps.executeQuery();

                for (int i = 0; i < 500; i++) {
                    if (rs.next()) {
                        list.add(toTimedValue(rs));
                    } else {
                        break;
                    }
                }
                if (rs.next()) {
                    list.add(new TimedValueImpl<>(null, null));
                }

                return list;

            });
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Unable to locate data for {} {} {}", provider, service, resource, e);
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long getStoredValueCount(String provider, String service, String resource, ZonedDateTime fromTime,
            ZonedDateTime toTime) {
        Connection conn = connectionSupplier.get();

        try {
            return txControl.required(() -> {

                PreparedStatement ps;
                if (toTime == null) {
                    if (fromTime == null) {
                        ps = conn.prepareStatement(COUNT_TEMPLATE_WITHOUT_START_OR_LIMIT);
                        setVariables(ps, provider, service, resource);
                    } else {
                        ps = conn.prepareStatement(COUNT_TEMPLATE_WITHOUT_LIMIT);
                        setVariables(ps, provider, service, resource, Timestamp.from(fromTime.toInstant()));
                    }
                } else {
                    if (fromTime == null) {
                        ps = conn.prepareStatement(COUNT_TEMPLATE_WITHOUT_START);
                        setVariables(ps, provider, service, resource, Timestamp.from(toTime.toInstant()));
                    } else {
                        ps = conn.prepareStatement(COUNT_TEMPLATE);
                        setVariables(ps, provider, service, resource, Timestamp.from(toTime.toInstant()),
                                Timestamp.from(fromTime.toInstant()));
                    }
                }

                ResultSet rs = ps.executeQuery();
                rs.next();
                return rs.getLong(1);
            });
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Unable to count data for {} {} {}", provider, service, resource, e);
            }
            throw new RuntimeException(e);
        }
    }

}

class TimedValueImpl<T> implements TimedValue<T> {

    private final Instant timestamp;

    private final T value;

    public TimedValueImpl(final T value) {
        this(value, Instant.now());
    }

    public TimedValueImpl(final T value, Instant instant) {
        this.value = value;
        this.timestamp = instant;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("TimedValue(%s, %s)", getValue(), getTimestamp());
    }
}
