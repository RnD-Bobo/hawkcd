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

package io.hawkcd.core.session;

import io.hawkcd.ws.WSSocket;

import java.util.Set;

/**
 * Created by rado on 11.11.16.
 */
public interface ISessionsPool  {

    Set<WSSocket> getSessions();

    WSSocket getSessionByID(String id);

    void addSession(WSSocket session);

    void removeSession(WSSocket session);

    boolean contains(WSSocket session);

    WSSocket getSessionByUserEmail(String email);

    WSSocket getSessionByUserId(String userId);
}
