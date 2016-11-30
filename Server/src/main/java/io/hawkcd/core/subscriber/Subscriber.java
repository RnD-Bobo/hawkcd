/*
 *   Copyright (C) 2016 R&D Solutions Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *
 */

package io.hawkcd.core.subscriber;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.hawkcd.core.Message;
import io.hawkcd.core.MessageDispatcher;
import io.hawkcd.model.MaterialDefinition;
import io.hawkcd.model.TaskDefinition;
import io.hawkcd.utilities.deserializers.MaterialDefinitionAdapter;
import io.hawkcd.utilities.deserializers.TaskDefinitionAdapter;
import org.apache.log4j.Logger;
import redis.clients.jedis.JedisPubSub;

/**
 * Represents an application process(when running in separate VM)
 * or thread ( when running in the same VM) that receives messages broadcast by Publishers.
 */
public class Subscriber extends JedisPubSub {
    private static final Logger LOGGER = Logger.getLogger(Subscriber.class);

    private Gson jsonConverter;

    public Subscriber() {
        this.jsonConverter = new GsonBuilder()
                .registerTypeAdapter(Envelopе.class, new EnvelopeAdapter())
                .registerTypeAdapter(TaskDefinition.class, new TaskDefinitionAdapter())
                .registerTypeAdapter(MaterialDefinition.class, new MaterialDefinitionAdapter())
                .create();
    }

    @Override
    public void onMessage(String channel, String msg) {
        LOGGER.debug(msg);
        Message message = this.jsonConverter.fromJson(msg, Message.class);
        MessageDispatcher.dispatchOutgoingMessage(message);
    }
}
