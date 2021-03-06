package com.oath.micro.server.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.oath.micro.server.rest.jackson.JacksonUtil;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cyclops.reactive.collections.mutable.MapX;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;

public class RequestTypes<T> {
    @Getter
    private final Map<String, RequestsBeingExecuted> map = new ConcurrentHashMap<>();;

    private final EventBus bus;

    public RequestTypes(EventBus bus, boolean queryCapture) {
        this.bus = bus;
        if (queryCapture)
            bus.register(this);

    }

    @Override
    public String toString() {
        return JacksonUtil.serializeToJson(toMap());
    }

    public Map toMap() {
        return MapX.fromMap(map)
                   .bimap(k -> k, v -> v.toMap());
    }

    @Subscribe
    public void finished(RemoveQuery<T> data) {
        String key = data.getData().type;
        map.computeIfAbsent(key, k -> new RequestsBeingExecuted<T>(bus, k)).events.finished(buildId(data.getData()));

    }

    @Subscribe
    public void processing(AddQuery<T> data) {
        String id = buildId(data.getData());
        String key = data.getData().type;
        map.computeIfAbsent(key, k -> new RequestsBeingExecuted<T>(bus, k)).events.active(id, data.getData());

    }

    private String buildId(RequestData data) {
        return data.correlationId;
    }

    public static class AddQuery<T> extends AddEvent<RequestData<T>> {

        public AddQuery(RequestData<T> data) {
            super(data);
        }

    }

    public static class RemoveQuery<T> extends RemoveEvent<RequestData<T>> {

        public RemoveQuery(RequestData data) {
            super(data);
        }

    }

    public static class AddLabelledQuery<T> extends AddEvent<RequestData<T>> {

        public AddLabelledQuery(RequestData<T> data) {
            super(data);
        }

    }

    public static class RemoveLabelledQuery<T> extends RemoveEvent<RequestData<T>> {

        public RemoveLabelledQuery(RequestData data) {
            super(data);
        }

    }

    @AllArgsConstructor
    @Builder
    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    public static class RequestData<T> extends BaseEventInfo {

        private final String correlationId;

        private final T query;

        private final String type;
        private final Object additionalData;
    }
}
