package ru.yandex.practicum.service.hub;

import ru.yandex.practicum.model.hub.HubEvent;

public interface HubEventService {

    void collect(HubEvent hubEvent);
}
