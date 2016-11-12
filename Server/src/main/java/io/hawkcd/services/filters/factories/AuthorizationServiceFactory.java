/*
 * Copyright (C) 2016 R&D Solutions Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.hawkcd.services.filters.factories;

import io.hawkcd.services.filters.MaterialAuthorizationService;
import io.hawkcd.services.filters.PipelineDefinitionAuthorizationService;
import io.hawkcd.services.filters.PipelineGroupAuthorizationService;
import io.hawkcd.services.filters.TaskDefinitionAuthorizationService;
import io.hawkcd.services.filters.UserAuthorizationService;
import io.hawkcd.services.filters.interfaces.IAuthorizationService;
import io.hawkcd.services.filters.AgentAuthorizationService;
import io.hawkcd.services.filters.JobDefinitionAuthorizationService;
import io.hawkcd.services.filters.PipelineAuthorizationService;
import io.hawkcd.services.filters.StageDefinitionAuthorizationService;
import io.hawkcd.services.filters.UserGroupAuthorizationService;

public class AuthorizationServiceFactory {
    private static IAuthorizationService authorizationService;

    public static IAuthorizationService create(String service) {
        switch (service) {
            case "PipelineDefinitionService":
                authorizationService = new PipelineDefinitionAuthorizationService();
                return authorizationService;
            case "StageDefinitionService":
                authorizationService = new StageDefinitionAuthorizationService();
                return authorizationService;
            case "JobDefinitionService":
                authorizationService = new JobDefinitionAuthorizationService();
                return authorizationService;
            case "TaskDefinitionService":
                authorizationService = new TaskDefinitionAuthorizationService();
                return authorizationService;
            case "AgentService":
                authorizationService = new AgentAuthorizationService();
                return authorizationService;
            case "PipelineService":
            case "StageService":
            case "JobService":
                authorizationService = new PipelineAuthorizationService();
                return authorizationService;
            case "PipelineGroupService":
                authorizationService = new PipelineGroupAuthorizationService();
                return authorizationService;
            case "UserGroupService":
                authorizationService = new UserGroupAuthorizationService();
                return authorizationService;
            case "UserService":
                authorizationService = new UserAuthorizationService();
                return authorizationService;
            case "MaterialDefinitionService":
                authorizationService = new MaterialAuthorizationService();
                return authorizationService;
            default:
                return null;
        }
    }
}
