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

import io.hawkcd.model.SessionDetails;
import io.hawkcd.model.dto.WsContractDto;
import io.hawkcd.ws.WSSocket;

import java.util.List;

/**
 * Created by rado on 11.11.16.
 */
public interface ISessionManager {

    void closeSessionById(String sessionId);

    void closeSessionByUserEmail(String email);

    void sendToAllSessions(WsContractDto contractDto);

    void openSession(WSSocket session);

    boolean isUserInSession(WSSocket session, String email);

    void logoutUser(WSSocket session);

    SessionDetails getSessionDetailsBySessionId(String sessionId);

    List<SessionDetails> getAllActiveSessions();

    void sendToAllAuthorizedSession(WsContractDto contractDto);

    WSSocket getSessionByUserId(String id);

    void updateSessionLoggedUser(String... userIds);

    void send(WSSocket session, WsContractDto contractDto);
}
