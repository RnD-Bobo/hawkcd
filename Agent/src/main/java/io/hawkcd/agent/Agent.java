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

package io.hawkcd.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.hawkcd.agent.components.jobexecutor.IJobExecutor;
import io.hawkcd.agent.components.jobexecutor.JobExecutor;
import io.hawkcd.agent.constants.MessageConstants;
import io.hawkcd.agent.enums.JobStatus;
import io.hawkcd.agent.interfaces.IAgent;
import io.hawkcd.agent.models.TaskDefinition;
import io.hawkcd.agent.models.payload.WorkInfo;
import io.hawkcd.agent.utilities.deserializers.MaterialDefinitionAdapter;
import io.hawkcd.agent.utilities.deserializers.TaskDefinitionAdapter;
import io.hawkcd.agent.models.MaterialDefinition;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.ws.rs.core.Response.Status;
import java.util.Timer;
import java.util.TimerTask;

/// <summary>
/// The class represents the HawkServer agent instance running on a machine. It's designed to be a singleton.
/// Upon initialization the class creates timers that issue http requests on a regular time interval to the server's http endpoints.
/// The two main methods the communication with the server goes through are: ReportToServer - Report status to the server for a job being executed;
/// CheckForWork - In response, receives information if any job is being dispatched for execution.
/// </summary>
public class Agent implements IAgent {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private Timer checkForWorkTimer;
    private Timer reportTimer;
    private Timer reportAgentTimer;
    private IJobExecutor jobExecutor;
    private Client restClient;
    private Gson jsonConverter;

    public Agent() {
        AgentConfiguration.configure();
        this.jobExecutor = new JobExecutor();
        this.jsonConverter = new GsonBuilder()
                .registerTypeAdapter(TaskDefinition.class, new TaskDefinitionAdapter())
                .registerTypeAdapter(MaterialDefinition.class, new MaterialDefinitionAdapter())
                .create();
        this.restClient = Client.create();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    //Using IoC container
    public Agent(IJobExecutor jobExecutor) {
        this.jobExecutor = jobExecutor;
    }

    @Override
    public void reportJobToServer() {
        //String report = prepareReportForServer();
        WebResource webResource = this.restClient.resource(AgentConfiguration.getInstallInfo().getReportJobApiAddress());
        ClientResponse response = null;
        try {
            String jobAsString = this.jsonConverter.toJson(this.jobExecutor.getCurrentJob());
            response = webResource.type("application/json").put(ClientResponse.class, jobAsString);
        } catch (Exception e) {
//            this.logger.info(MessageConstants.AGENT_COULD_NOT_CONNECT);
        }

        if ((response != null) && (response.getStatus() == Status.OK.getStatusCode())) {
//            this.logger.info(MessageConstants.JOB_REPORT_SENT);

            if ((this.jobExecutor.getCurrentJob() != null) && ((this.jobExecutor.getCurrentJob().getStatus() == JobStatus.PASSED) || (this.jobExecutor.getCurrentJob().getStatus() == JobStatus.FAILED))) {
                try {
                    //report = prepareReportForServer();
                    String jobAsString = this.jsonConverter.toJson(this.jobExecutor.getCurrentJob());
                    response = webResource.type("application/json").put(ClientResponse.class, jobAsString);
                } catch (Exception e) {
//                    this.logger.info(MessageConstants.AGENT_COULD_NOT_CONNECT);
                }

                if ((response != null) && (response.getStatus() == Status.OK.getStatusCode())) {
                    String jobAsString = this.jsonConverter.toJson(this.jobExecutor.getCurrentJob());
                    webResource.type("application/json").put(ClientResponse.class, jobAsString);
                    this.logger.info(MessageConstants.JOB_REPORT_SENT);
                    AgentConfiguration.getAgentInfo().setRunning(false);
                    this.jobExecutor.setCurrentJob(null);
                }
            }
        }
    }

    @Override
    public void reportAgentToServer() {
        WebResource webResource = this.restClient.resource(AgentConfiguration.getInstallInfo().getReportAgentApiAddress());
        ClientResponse response = null;

        try {
            String agentAsString = this.jsonConverter.toJson(AgentConfiguration.getAgentInfo());
//            this.logger.info(MessageConstants.AGENT_REPORT_SENT);
            response = webResource.type("application/json").put(ClientResponse.class, agentAsString);
        } catch (Exception e) {
//            this.logger.info(MessageConstants.AGENT_COULD_NOT_CONNECT);
        }
    }

    @Override
    public void checkForWork() {
//        this.logger.info(MessageConstants.AGENT_CHECKING_FOR_WORK);
        boolean isRunning = AgentConfiguration.getAgentInfo().isRunning();
        if (!isRunning) {
            WebResource webResource = this.restClient.resource(AgentConfiguration.getInstallInfo().getCheckForWorkApiAddress());
            ClientResponse response = null;

            try {
                response = webResource.accept("application/json").get(ClientResponse.class);
            } catch (Exception e) {
//                this.logger.info(MessageConstants.AGENT_COULD_NOT_CONNECT);
            }

            WorkInfo workInfo = null;
            if ((response != null) && (response.getStatus() == Status.OK.getStatusCode())) {
                String responseEntity = response.getEntity(String.class);

                workInfo = this.jsonConverter.fromJson(responseEntity, WorkInfo.class);
            } else if ((response != null) && (response.getStatus() != Status.OK.getStatusCode())) {
                this.logger.info(response.getEntity(String.class));
            }

            if (workInfo != null) {
                AgentConfiguration.getAgentInfo().setRunning(true);
                this.logger.info(MessageConstants.AGENT_WORK_FOUND);
                this.jobExecutor.initJobExecutionInfo(workInfo);
                this.jobExecutor.executeJob(workInfo);
            }
        }
    }

    @Override
    public void start() {
        this.startReportJobTimer();
        this.startReportAgentTimer();
        this.startCheckForWorkTimer();
        this.logger.info(MessageConstants.AGENT_STARTED);
    }

    @Override
    public void stop() {
        this.stopReportJobTimer();
        this.stopReportAgentTimer();
        this.stopCheckForWorkTimer();
        this.logger.info(MessageConstants.AGENT_STOPPED);
    }

    private void startReportJobTimer() {

        TimerTask reportTask = new TimerTask() {
            @Override
            public void run() {
                Agent.this.reportJobToServer();
            }
        };
        this.reportTimer = new Timer();
        this.reportTimer.schedule(reportTask, 0, 4000);
    }

    private void startReportAgentTimer() {

        TimerTask reportAgentTask = new TimerTask() {
            @Override
            public void run() {
                Agent.this.reportAgentToServer();
            }
        };
        this.reportAgentTimer = new Timer();
        this.reportAgentTimer.schedule(reportAgentTask, 0, 4000);
    }

    private void startCheckForWorkTimer() {

        TimerTask checkForWorkTask = new TimerTask() {
            @Override
            public void run() {
                Agent.this.checkForWork();
            }
        };

        this.checkForWorkTimer = new Timer();
        this.checkForWorkTimer.schedule(checkForWorkTask, 2000, 4000);
    }

    private void stopReportJobTimer() {

        this.reportTimer.cancel();
    }

    private void stopReportAgentTimer() {

        this.reportAgentTimer.cancel();
    }

    private void stopCheckForWorkTimer() {

        this.checkForWorkTimer.cancel();
    }

    //TODO: To be removed
//    private String prepareReportForServer() {
//        AgentStatusReport report = new AgentStatusReport();
//        report.setAgentInfo(AgentConfiguration.getAgentInfo());
//        report.setEnvironmentInfo(AgentConfiguration.getEnvironmentInfo());
//        report.setJobExecutionInfo(this.jobExecutor.getCurrentJob());
//
//        String reportToServer = this.jsonConverter.toJson(report);
//        return reportToServer;
//    }
}
