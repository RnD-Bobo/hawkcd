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

package io.hawkcd.agent.components.taskexecutor.executors;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.hawkcd.agent.AgentConfiguration;
import io.hawkcd.agent.components.taskexecutor.TaskExecutor;
import io.hawkcd.agent.constants.ConfigConstants;
import io.hawkcd.agent.enums.TaskStatus;
import io.hawkcd.agent.models.FetchArtifactTask;
import io.hawkcd.agent.models.Task;
import io.hawkcd.agent.models.payload.WorkInfo;
import io.hawkcd.agent.services.FileManagementService;
import io.hawkcd.agent.services.interfaces.IFileManagementService;
import io.hawkcd.agent.utilities.ReportAppender;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

public class FetchArtifactExecutor extends TaskExecutor {

    private Client restClient;
    private IFileManagementService fileManagementService;

    public FetchArtifactExecutor() {
        this.restClient = Client.create();
        this.fileManagementService = new FileManagementService();
    }

    public FetchArtifactExecutor(Client client, IFileManagementService fileManagementService) {
        this.restClient = client.create();
        this.fileManagementService = fileManagementService;
    }

    @Override
    public Task executeTask(Task task, StringBuilder report, WorkInfo workInfo) {
        FetchArtifactTask taskDefinition = (FetchArtifactTask) task.getTaskDefinition();
        super.updateTask(task, TaskStatus.PASSED, LocalDateTime.now(), null);

        if ((taskDefinition.getDesignatedPipelineExecutionId() == null) || (taskDefinition.getDesignatedPipelineExecutionId().isEmpty())) {
            return this.nullProcessing(report, task, "Error occurred in getting pipeline execution ID!");
        }
        if ((taskDefinition.getDesignatedPipelineDefinitionName() == null) || (taskDefinition.getDesignatedPipelineDefinitionName().isEmpty())) {
            return this.nullProcessing(report, task, "Error occurred in getting pipeline name!");
        }

        String fetchingMessage = String.format("Start fetching artifact source: " +
                taskDefinition.getDesignatedPipelineDefinitionName() +
                File.separator +
                taskDefinition.getDesignatedPipelineExecutionId() +
                File.separator +
                taskDefinition.getSource());
        LOGGER.debug(fetchingMessage);
        ReportAppender.appendInfoMessage(fetchingMessage, report);

        String folderPath = String.format(ConfigConstants.SERVER_CREATE_ARTIFACT_API_ADDRESS, workInfo.getPipelineDefinitionName(), workInfo.getPipelineExecutionID());
        AgentConfiguration.getInstallInfo().setCreateArtifactApiAddress(String.format("%s/%s", AgentConfiguration.getInstallInfo().getServerAddress(), folderPath));

        String requestSource = this.fileManagementService.urlCombine(AgentConfiguration.getInstallInfo().getCreateArtifactApiAddress()) + "/fetch-artifact";
        WebResource webResource = this.restClient.resource(requestSource);
        String source = taskDefinition.getDesignatedPipelineDefinitionName() + File.separator + taskDefinition.getDesignatedPipelineExecutionId() + File.separator + taskDefinition.getSource();

        ClientResponse response = webResource.type("application/json").post(ClientResponse.class, source);

        if ((response.getStatus() != 200)) {
            return this.nullProcessing(report, task, String.format("Could not get resource. TaskStatus code %s", response.getStatus()));
        }

        if (response.getEntityInputStream() == null) {
            return this.nullProcessing(report, task, "Could not get resource. Input stream is null");
        }

        String filePath = Paths.get(AgentConfiguration.getInstallInfo().getAgentTempDirectoryPath(), UUID.randomUUID() + ".zip").toString();
        File fetchArtifactDir = new File(filePath);
        this.fileManagementService.generateDirectory(fetchArtifactDir);

        String errorMessage;
        errorMessage = this.fileManagementService.initiateFile(fetchArtifactDir, response.getEntityInputStream(), filePath);
        if (errorMessage != null) {
            return this.nullProcessing(report, task, "Error occurred in creating the artifact!");
        }
        String destination;
        if (taskDefinition.getDestination() != null) {
            destination = String.valueOf(Paths.get(AgentConfiguration.getInstallInfo().getAgentPipelinesDir() + File.separator + taskDefinition.getDesignatedPipelineDefinitionName(), taskDefinition.getDestination()));
        } else {
            destination = String.valueOf(Paths.get(AgentConfiguration.getInstallInfo().getAgentPipelinesDir() + File.separator + taskDefinition.getDesignatedPipelineDefinitionName()));
        }
        errorMessage = this.fileManagementService.unzipFile(filePath, destination);
        filePath = Paths.get(AgentConfiguration.getInstallInfo().getAgentTempDirectoryPath()).toString();
        String deleteMessage = this.fileManagementService.deleteFilesInDirectory(filePath);

        if (errorMessage != null) {
            return this.nullProcessing(report, task, "Error occurred in unzipping files!");
        }

        if (deleteMessage != null) {
            return this.nullProcessing(report, task, "Error occurred in deleting files!");
        }

        super.updateTask(task, TaskStatus.PASSED, null, LocalDateTime.now());

        String fetchedMessage = String.format("Saved artifact to %s after verifying the integrity of its contents.", destination);
        LOGGER.debug(fetchedMessage);
        ReportAppender.appendInfoMessage(fetchedMessage, report);

        return task;
    }
}