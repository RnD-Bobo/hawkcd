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
import junit.framework.Assert;
import io.hawkcd.agent.AgentConfiguration;
import io.hawkcd.agent.base.TestBase;
import io.hawkcd.agent.enums.TaskStatus;
import io.hawkcd.agent.enums.TaskType;
import io.hawkcd.agent.models.FetchArtifactTask;
import io.hawkcd.agent.models.Job;
import io.hawkcd.agent.models.Task;
import io.hawkcd.agent.models.payload.WorkInfo;
import io.hawkcd.agent.services.FileManagementService;
import io.hawkcd.agent.services.interfaces.IFileManagementService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.InputStream;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Client.class, WebResource.Builder.class})
public class FetchArtifactExecutorTest extends TestBase {

    private Task correctFetchArtifactTask;
    private Client mockedClient;
    private WebResource mockedResource;
    private WebResource.Builder mockedBuilder;
    private ClientResponse mockedResponse;
    private IFileManagementService mockedFileManagementService;
    private FetchArtifactTask fetchArtifactTaskDefinition;
    private File mockedFile;
    private WorkInfo workInfo;
    private FetchArtifactExecutor fetchArtifactExecutor;
    private StringBuilder report;
    private InputStream mockedInputStream;
    private Job fetchArtifactJob;
    private String source;

    @Before
    public void setUp() {
        AgentConfiguration.configure();

        try {
            PowerMockito.whenNew(WebResource.Builder.class).withNoArguments().thenReturn(Mockito.mock(WebResource.Builder.class));
            this.mockedBuilder = Mockito.mock(WebResource.Builder.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        PowerMockito.mockStatic(Client.class);
        this.mockedClient = Mockito.mock(Client.class);
        Mockito.when(Client.create()).thenReturn(this.mockedClient);
        this.mockedInputStream = Mockito.mock(InputStream.class);
        this.mockedResource = Mockito.mock(WebResource.class);
        this.mockedResponse = Mockito.mock(ClientResponse.class);
        this.mockedFileManagementService = Mockito.mock(FileManagementService.class);
        this.mockedFile = new File("pathToFile");
        this.fetchArtifactExecutor = new FetchArtifactExecutor(this.mockedClient, this.mockedFileManagementService);
        this.report = new StringBuilder();
        setupData();
    }

    private void setupData() {
        this.correctFetchArtifactTask = new Task();
        this.correctFetchArtifactTask.setType(TaskType.FETCH_ARTIFACT);

        this.fetchArtifactJob = new Job();
        this.fetchArtifactJob.setJobDefinitionName("jobDefinition");
        this.fetchArtifactJob.setJobDefinitionName("correct");

        this.fetchArtifactTaskDefinition = new FetchArtifactTask();
        this.fetchArtifactTaskDefinition.setName("fetchArtifactTask");
        this.fetchArtifactTaskDefinition.setDesignatedPipelineDefinitionName("pipeline");
        this.fetchArtifactTaskDefinition.setDesignatedPipelineExecutionId("pipeline");
        this.fetchArtifactTaskDefinition.setSource("correctSource");
        this.fetchArtifactTaskDefinition.setDestination("correctDestination");
        this.correctFetchArtifactTask.setTaskDefinition(this.fetchArtifactTaskDefinition);

        this.workInfo = new WorkInfo();
        this.workInfo.setPipelineDefinitionName("correct");
        this.workInfo.setStageDefinitionName("correct");
        this.workInfo.setJob(this.fetchArtifactJob);

        this.source = this.fetchArtifactTaskDefinition.getDesignatedPipelineDefinitionName() + File.separator + this.fetchArtifactTaskDefinition.getDesignatedPipelineExecutionId() + File.separator + this.fetchArtifactTaskDefinition.getSource();

    }

    @Test
    public void fetchArtifactExecutor_instantiated_notNull() {
        FetchArtifactExecutor executor = new FetchArtifactExecutor();
        Assert.assertNotNull(executor);
    }

    @Test
    public void executeTask_artifactFetchedSuccessfully_taskPassed() {
        //Arrange
        Mockito.when(this.mockedFileManagementService.unzipFile(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        Mockito.when(this.mockedFileManagementService.initiateFile(Mockito.any(File.class), Mockito.any(InputStream.class), Mockito.anyString())).thenReturn(null);
        Mockito.when(this.mockedFileManagementService.urlCombine(Mockito.anyString())).thenReturn("sourceForAPI");
        Mockito.when(this.mockedClient.resource("sourceForAPI/fetch-artifact")).thenReturn(this.mockedResource);
        Mockito.when(this.mockedResource.type(Mockito.anyString())).thenReturn(this.mockedBuilder);
        Mockito.when(this.mockedResource.accept(Mockito.anyString())).thenReturn(this.mockedBuilder);
        Mockito.when(this.mockedResource.type("application/json").post(ClientResponse.class, this.source)).thenReturn(this.mockedResponse);
        Mockito.when(this.mockedResponse.getStatus()).thenReturn(200);
        Mockito.when(this.mockedResponse.getEntityInputStream()).thenReturn(this.mockedInputStream);

        //Act
        Task resultTask = this.fetchArtifactExecutor.executeTask(this.correctFetchArtifactTask, this.report, this.workInfo);

        //Assert
        Mockito.verify(this.mockedFileManagementService, Mockito.times(1)).unzipFile(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(this.mockedFileManagementService, Mockito.times(1)).initiateFile(Mockito.any(File.class), Mockito.any(InputStream.class), Mockito.anyString());
        Mockito.verify(this.mockedFileManagementService, Mockito.times(1)).urlCombine(Mockito.anyString());
        Mockito.verify(this.mockedResponse, Mockito.times(1)).getStatus();
        Assert.assertEquals(TaskStatus.PASSED, resultTask.getStatus());
        Assert.assertEquals(this.fetchArtifactTaskDefinition.getName(), resultTask.getTaskDefinition().getName());
    }

    @Test
    public void executeTask_failedToDeleteFiles_taskFailed() {
        //Arrange
        Mockito.when(this.mockedFileManagementService.unzipFile(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        Mockito.when(this.mockedFileManagementService.initiateFile(Mockito.any(File.class), Mockito.any(InputStream.class), Mockito.anyString())).thenReturn(null);
        Mockito.when(this.mockedFileManagementService.urlCombine(Mockito.anyString())).thenReturn("sourceForAPI");
        Mockito.when(this.mockedClient.resource("sourceForAPI/fetch-artifact")).thenReturn(this.mockedResource);
        Mockito.when(this.mockedResource.type(Mockito.anyString())).thenReturn(this.mockedBuilder);
        Mockito.when(this.mockedResource.accept(Mockito.anyString())).thenReturn(this.mockedBuilder);
        Mockito.when(this.mockedResource.type("application/json").post(ClientResponse.class, this.source)).thenReturn(this.mockedResponse);
        Mockito.when(this.mockedResponse.getStatus()).thenReturn(200);
        Mockito.when(this.mockedResponse.getEntityInputStream()).thenReturn(this.mockedInputStream);
        Mockito.when(this.mockedFileManagementService.deleteFilesInDirectory(Mockito.anyString())).thenReturn("Error");

        //Act
        Task resultTask = this.fetchArtifactExecutor.executeTask(this.correctFetchArtifactTask, this.report, this.workInfo);

        //Assert
        Mockito.verify(this.mockedFileManagementService, Mockito.times(1)).unzipFile(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(this.mockedFileManagementService, Mockito.times(1)).initiateFile(Mockito.any(File.class), Mockito.any(InputStream.class), Mockito.anyString());
        Mockito.verify(this.mockedFileManagementService, Mockito.times(1)).urlCombine(Mockito.anyString());
        Mockito.verify(this.mockedFileManagementService, Mockito.times(1)).deleteFilesInDirectory(Mockito.anyString());
        Assert.assertEquals(TaskStatus.FAILED, resultTask.getStatus());
        Assert.assertEquals(this.fetchArtifactTaskDefinition.getName(), resultTask.getTaskDefinition().getName());
    }

    @Test
    public void executeTask_failedToUnzipFiles_taskFailed() {
        //Arrange
        Mockito.when(this.mockedFileManagementService.initiateFile(Mockito.any(File.class), Mockito.any(InputStream.class), Mockito.anyString())).thenReturn(null);
        Mockito.when(this.mockedFileManagementService.urlCombine(Mockito.anyString())).thenReturn("sourceForAPI");
        Mockito.when(this.mockedClient.resource("sourceForAPI/fetch-artifact")).thenReturn(this.mockedResource);
        Mockito.when(this.mockedResource.type(Mockito.anyString())).thenReturn(this.mockedBuilder);
        Mockito.when(this.mockedResource.accept(Mockito.anyString())).thenReturn(this.mockedBuilder);
        Mockito.when(this.mockedResource.type("application/json").post(ClientResponse.class, this.source)).thenReturn(this.mockedResponse);
        Mockito.when(this.mockedResponse.getStatus()).thenReturn(200);
        Mockito.when(this.mockedResponse.getEntityInputStream()).thenReturn(this.mockedInputStream);
        Mockito.when(this.mockedFileManagementService.unzipFile(Mockito.anyString(), Mockito.anyString())).thenReturn("Error");

        //Act
        Task resultTask = this.fetchArtifactExecutor.executeTask(this.correctFetchArtifactTask, this.report, this.workInfo);

        //Assert
        Mockito.verify(this.mockedFileManagementService, Mockito.times(1)).unzipFile(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(this.mockedFileManagementService, Mockito.times(1)).initiateFile(Mockito.any(File.class), Mockito.any(InputStream.class), Mockito.anyString());
        Mockito.verify(this.mockedFileManagementService, Mockito.times(1)).urlCombine(Mockito.anyString());
        Assert.assertEquals(TaskStatus.FAILED, resultTask.getStatus());
        Assert.assertEquals(this.fetchArtifactTaskDefinition.getName(), resultTask.getTaskDefinition().getName());
    }

    @Test
    public void executeTask_failedToCreateFiles_taskFailed() {
        //Arrange
        Mockito.when(this.mockedFileManagementService.initiateFile(Mockito.any(File.class), Mockito.any(InputStream.class), Mockito.anyString())).thenReturn("Error");
        Mockito.when(this.mockedFileManagementService.urlCombine(Mockito.anyString())).thenReturn("sourceForAPI");
        Mockito.when(this.mockedClient.resource("sourceForAPI/fetch-artifact")).thenReturn(this.mockedResource);
        Mockito.when(this.mockedResource.type(Mockito.anyString())).thenReturn(this.mockedBuilder);
        Mockito.when(this.mockedResource.accept(Mockito.anyString())).thenReturn(this.mockedBuilder);
        Mockito.when(this.mockedResource.type("application/json").post(ClientResponse.class, this.source)).thenReturn(this.mockedResponse);
        Mockito.when(this.mockedResponse.getStatus()).thenReturn(200);
        Mockito.when(this.mockedResponse.getEntityInputStream()).thenReturn(this.mockedInputStream);

        //Act
        Task resultTask = this.fetchArtifactExecutor.executeTask(this.correctFetchArtifactTask, this.report, this.workInfo);

        //Assert
        Mockito.verify(this.mockedFileManagementService, Mockito.times(1)).initiateFile(Mockito.any(File.class), Mockito.any(InputStream.class), Mockito.anyString());
        Mockito.verify(this.mockedFileManagementService, Mockito.times(1)).urlCombine(Mockito.anyString());
        Assert.assertEquals(TaskStatus.FAILED, resultTask.getStatus());
        Assert.assertEquals(this.fetchArtifactTaskDefinition.getName(), resultTask.getTaskDefinition().getName());
    }

    @Test
    public void executeTask_responseNotOk_taskFailed() {
        //Arrange
        Mockito.when(this.mockedFileManagementService.urlCombine(Mockito.anyString())).thenReturn("sourceForAPI");
        Mockito.when(this.mockedClient.resource("sourceForAPI/fetch-artifact")).thenReturn(this.mockedResource);
        Mockito.when(this.mockedResource.type(Mockito.anyString())).thenReturn(this.mockedBuilder);
        Mockito.when(this.mockedResource.accept(Mockito.anyString())).thenReturn(this.mockedBuilder);
        Mockito.when(this.mockedResource.type("application/json").post(ClientResponse.class, this.source)).thenReturn(this.mockedResponse);
        Mockito.when(this.mockedResponse.getStatus()).thenReturn(400);
        Mockito.when(this.mockedResponse.getEntityInputStream()).thenReturn(this.mockedInputStream);

        //Act
        Task resultTask = this.fetchArtifactExecutor.executeTask(this.correctFetchArtifactTask, this.report, this.workInfo);

        //Assert
        Mockito.verify(this.mockedFileManagementService, Mockito.times(1)).urlCombine(Mockito.anyString());
        Assert.assertEquals(400, this.mockedResponse.getStatus());
        Assert.assertEquals(TaskStatus.FAILED, resultTask.getStatus());
        Assert.assertEquals(this.fetchArtifactTaskDefinition.getName(), resultTask.getTaskDefinition().getName());
    }

    @Test
    public void executeTask_inputStreamNull_taskFailed() {
        //Arrange
        Mockito.when(this.mockedFileManagementService.urlCombine(Mockito.anyString())).thenReturn("sourceForAPI");
        Mockito.when(this.mockedClient.resource("sourceForAPI/fetch-artifact")).thenReturn(this.mockedResource);
        Mockito.when(this.mockedResource.type(Mockito.anyString())).thenReturn(this.mockedBuilder);
        Mockito.when(this.mockedResource.accept(Mockito.anyString())).thenReturn(this.mockedBuilder);
        Mockito.when(this.mockedResource.type("application/json").post(ClientResponse.class, this.source)).thenReturn(this.mockedResponse);
        Mockito.when(this.mockedResponse.getStatus()).thenReturn(200);
        Mockito.when(this.mockedResponse.getEntityInputStream()).thenReturn(null);

        //Act
        Task resultTask = this.fetchArtifactExecutor.executeTask(this.correctFetchArtifactTask, this.report, this.workInfo);

        //Assert
        Mockito.verify(this.mockedFileManagementService, Mockito.times(1)).urlCombine(Mockito.anyString());
        Assert.assertEquals(200, this.mockedResponse.getStatus());
        Assert.assertEquals(TaskStatus.FAILED, resultTask.getStatus());
        Assert.assertEquals(this.fetchArtifactTaskDefinition.getName(), resultTask.getTaskDefinition().getName());
    }
}