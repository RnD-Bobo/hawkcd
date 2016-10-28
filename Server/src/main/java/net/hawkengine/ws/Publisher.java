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

package net.hawkengine.ws;

import net.hawkengine.db.redis.RedisManager;
import redis.clients.jedis.Jedis;

public class Publisher {
    private Jedis publisherJedis = RedisManager.getJedisPool().getResource();

    public Publisher() {
        this.publisherJedis = RedisManager.getJedisPool().getResource();
    }

    public void publish(String channelName, Object object) {
        this.publisherJedis.publish(channelName, "lololol");
    }
}
