/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.spikes.mongo;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.AggregationQuery;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.AggregationQuery.Bucket;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.AggregationQuery.Function;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryPage;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.PruneRequest;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.SortOrder;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ValueFilter;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ValueKind;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.TimeSeriesGranularity;
import com.mongodb.client.model.TimeSeriesOptions;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Candidate C: MongoDB time-series collection. Numbers carry a parallel
 * Decimal128 field for query/aggregation pushdown plus the canonical string
 * for exact reconstruction (Decimal128 alone caps at 34 significant digits).
 * Structured values (GeoJSON/objects) round-trip as canonical JSON strings in
 * this spike; native BSON storage is possible but changes nothing for the
 * contract. Known platform limits (documented in EVALUATION_NOTES.md): BSON
 * Date is millisecond-precision; time-series updates are metaField-only.
 */
public class MongoHistoryStorage implements HistoryStorage {

    private static final String COLLECTION = "history";

    private final MongoClient client;
    private final MongoCollection<Document> collection;
    private final ObjectMapper mapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS).build();

    public MongoHistoryStorage(MongoClient client, String databaseName) {
        this.client = client;
        MongoDatabase database = client.getDatabase(databaseName);
        database.getCollection(COLLECTION).drop();
        database.createCollection(COLLECTION, new CreateCollectionOptions().timeSeriesOptions(
                new TimeSeriesOptions("t").metaField("meta").granularity(TimeSeriesGranularity.SECONDS)));
        this.collection = database.getCollection(COLLECTION);
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.AGGREGATION, Capability.VALUE_FILTERING);
    }

    @Override
    public int maxPageSize() {
        return 10_000;
    }

    @Override
    public void store(List<HistoricalRecord> records) {
        List<Document> documents = records.stream().map(this::toDocument).toList();
        collection.insertMany(documents);
    }

    private Document toDocument(HistoricalRecord record) {
        Document meta = new Document("p", record.path().provider()).append("s", record.path().service())
                .append("r", record.path().resource());
        Document document = new Document("t", Date.from(record.timestamp())).append("meta", meta)
                .append("kind", record.kind().ordinal());
        if (record.value() == null) {
            return document;
        }
        document.append("jt", record.value().getClass().getName());
        switch (record.kind()) {
        case NUMBER -> {
            document.append("raw", numericLiteral(record.value()));
            Decimal128 pushdownValue = toDecimal128(record.value());
            if (pushdownValue != null) {
                document.append("num", pushdownValue);
            }
        }
        case BOOLEAN -> document.append("val", record.value());
        case STRING -> document.append("val", record.value());
        case GEOJSON, OBJECT -> document.append("json", mapper.writeValueAsString(record.value()));
        }
        return document;
    }

    private static String numericLiteral(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd.toPlainString();
        }
        return value.toString();
    }

    private static Decimal128 toDecimal128(Object value) {
        try {
            if (value instanceof Double d) {
                if (d.isNaN()) {
                    return Decimal128.NaN;
                }
                if (d.isInfinite()) {
                    return d > 0 ? Decimal128.POSITIVE_INFINITY : Decimal128.NEGATIVE_INFINITY;
                }
                return new Decimal128(BigDecimal.valueOf(d));
            }
            if (value instanceof Float f) {
                return new Decimal128(new BigDecimal(f.toString()));
            }
            if (value instanceof BigDecimal bd) {
                return new Decimal128(bd);
            }
            if (value instanceof java.math.BigInteger bi) {
                return new Decimal128(new BigDecimal(bi));
            }
            if (value instanceof Number n) {
                return new Decimal128(n.longValue());
            }
        } catch (NumberFormatException | ArithmeticException e) {
            // exceeds Decimal128 (34 digits): no pushdown field, raw string still reconstructs
        }
        return null;
    }

    private Object reconstruct(Document document) {
        String javaType = document.getString("jt");
        if (javaType == null) {
            return null;
        }
        ValueKind kind = ValueKind.values()[document.getInteger("kind")];
        return switch (kind) {
        case NUMBER -> reconstructNumber(document.getString("raw"), javaType);
        case BOOLEAN, STRING -> document.get("val");
        case GEOJSON -> mapper.readValue(document.getString("json"), GeoJsonObject.class);
        case OBJECT -> mapper.readValue(document.getString("json"), Object.class);
        };
    }

    private static Object reconstructNumber(String literal, String javaType) {
        return switch (javaType) {
        case "java.lang.Long" -> Long.valueOf(literal);
        case "java.lang.Integer" -> Integer.valueOf(literal);
        case "java.lang.Short" -> Short.valueOf(literal);
        case "java.lang.Byte" -> Byte.valueOf(literal);
        case "java.lang.Double" -> Double.valueOf(literal);
        case "java.lang.Float" -> Float.valueOf(literal);
        case "java.math.BigInteger" -> new java.math.BigInteger(literal);
        default -> new BigDecimal(literal);
        };
    }

    private static Bson pathFilter(ResourcePath path) {
        return Filters.and(Filters.eq("meta.p", path.provider()), Filters.eq("meta.s", path.service()),
                Filters.eq("meta.r", path.resource()));
    }

    private static Bson withRange(Bson filter, TimeRange range) {
        List<Bson> parts = new ArrayList<>(List.of(filter));
        if (range.from() != null) {
            parts.add(range.fromInclusive() ? Filters.gte("t", Date.from(range.from()))
                    : Filters.gt("t", Date.from(range.from())));
        }
        if (range.to() != null) {
            parts.add(range.toInclusive() ? Filters.lte("t", Date.from(range.to()))
                    : Filters.lt("t", Date.from(range.to())));
        }
        return Filters.and(parts);
    }

    private static Bson withValueFilter(Bson filter, ValueFilter valueFilter) {
        if (valueFilter == null) {
            return filter;
        }
        List<Bson> parts = new ArrayList<>(List.of(filter));
        for (ValueFilter.Condition condition : valueFilter.conditions()) {
            Decimal128 literal = toDecimal128(condition.literal());
            parts.add(switch (condition.op()) {
            case EQ -> Filters.eq("num", literal);
            case NE -> Filters.ne("num", literal);
            case LT -> Filters.lt("num", literal);
            case LE -> Filters.lte("num", literal);
            case GT -> Filters.gt("num", literal);
            case GE -> Filters.gte("num", literal);
            });
        }
        return Filters.and(parts);
    }

    private Optional<TimedValue<?>> single(Bson filter, Bson sort) {
        Document document = collection.find(filter).sort(sort).limit(1).first();
        return document == null ? Optional.empty()
                : Optional.of(new DefaultTimedValue<>(reconstruct(document),
                        document.getDate("t").toInstant()));
    }

    @Override
    public Optional<TimedValue<?>> valueAt(ResourcePath path, Instant at) {
        return single(Filters.and(pathFilter(path), Filters.lte("t", Date.from(at))), Sorts.descending("t"));
    }

    @Override
    public Optional<TimedValue<?>> firstValue(ResourcePath path) {
        return single(pathFilter(path), Sorts.ascending("t"));
    }

    @Override
    public Optional<TimedValue<?>> latestValue(ResourcePath path) {
        return single(pathFilter(path), Sorts.descending("t"));
    }

    @Override
    public long count(ResourcePath path, TimeRange range) {
        return collection.countDocuments(withRange(pathFilter(path), range));
    }

    @Override
    public HistoryPage values(HistoryQuery query) {
        int limit = Math.min(query.limit(), maxPageSize());
        Bson filter = withValueFilter(withRange(pathFilter(query.path()), query.range()), query.valueFilter());
        Bson sort = query.order() == SortOrder.DESCENDING ? Sorts.descending("t") : Sorts.ascending("t");

        List<TimedValue<?>> values = new ArrayList<>();
        boolean hasMore = false;
        for (Document document : collection.find(filter).sort(sort).skip((int) query.offset()).limit(limit + 1)) {
            if (values.size() == limit) {
                hasMore = true;
                break;
            }
            values.add(new DefaultTimedValue<>(reconstruct(document), document.getDate("t").toInstant()));
        }
        return new HistoryPage(values, query.offset(), hasMore);
    }

    @Override
    public List<Bucket> aggregate(AggregationQuery query) {
        Bson filter = withRange(pathFilter(query.path()), query.range());
        Document dateTrunc = new Document("$dateTrunc", truncSpec(query.bucketSize()));

        List<Bson> pipeline = List.of(
                new Document("$match", filter),
                new Document("$group", new Document("_id", dateTrunc)
                        .append("cnt", new Document("$sum", 1L))
                        .append("mn", new Document("$min", "$num"))
                        .append("mx", new Document("$max", "$num"))
                        .append("av", new Document("$avg", "$num"))
                        .append("sm", new Document("$sum", "$num"))),
                new Document("$sort", new Document("_id", 1)));

        List<Bucket> buckets = new ArrayList<>();
        for (Document document : collection.aggregate(pipeline)) {
            Map<Function, Object> results = new EnumMap<>(Function.class);
            for (Function fn : query.functions()) {
                results.put(fn, switch (fn) {
                case COUNT -> document.getLong("cnt");
                case MIN -> toBigDecimal(document.get("mn"));
                case MAX -> toBigDecimal(document.get("mx"));
                case AVG -> toBigDecimal(document.get("av"));
                case SUM -> toBigDecimal(document.get("sm"));
                });
            }
            buckets.add(new Bucket(document.getDate("_id").toInstant(), results));
        }
        return buckets;
    }

    private static Document truncSpec(Duration bucketSize) {
        long seconds = bucketSize.toSeconds();
        String unit;
        long binSize;
        if (seconds % 3600 == 0) {
            unit = "hour";
            binSize = seconds / 3600;
        } else if (seconds % 60 == 0) {
            unit = "minute";
            binSize = seconds / 60;
        } else {
            unit = "second";
            binSize = seconds;
        }
        return new Document("date", "$t").append("unit", unit).append("binSize", binSize);
    }

    private static BigDecimal toBigDecimal(Object aggregate) {
        return aggregate instanceof Decimal128 d ? d.bigDecimalValue() : null;
    }

    @Override
    public long prune(PruneRequest request) {
        Bson scope = request.scope() == null ? Filters.empty() : pathFilter(request.scope());
        long deleted = 0;
        if (request.olderThan() != null) {
            deleted += collection
                    .deleteMany(Filters.and(scope, Filters.lt("t", Date.from(request.olderThan()))))
                    .getDeletedCount();
        }
        if (request.keepLatestPerResource() != null) {
            for (Document meta : collection.distinct("meta", scope, Document.class)) {
                List<Object> beyondKeep = new ArrayList<>();
                collection.find(Filters.eq("meta", meta)).sort(Sorts.descending("t"))
                        .skip(request.keepLatestPerResource().intValue())
                        .forEach(document -> beyondKeep.add(document.get("_id")));
                if (!beyondKeep.isEmpty()) {
                    deleted += collection.deleteMany(Filters.in("_id", beyondKeep)).getDeletedCount();
                }
            }
        }
        return deleted;
    }

    @Override
    public void close() {
        client.close();
    }
}
