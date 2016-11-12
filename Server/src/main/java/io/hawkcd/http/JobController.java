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

package io.hawkcd.http;

import io.hawkcd.model.ServiceResult;
import io.hawkcd.model.enums.NotificationType;
import io.hawkcd.services.JobService;
import io.hawkcd.services.interfaces.IJobService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/jobs")
public class JobController {
    private IJobService jobService;

    public JobController() {
        this.jobService = new JobService();
    }

    public JobController(IJobService jobService) {
        this.jobService = jobService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllJobs() {
        ServiceResult response = this.jobService.getAll();
        return Response.status(Status.OK).entity(response.getObject()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{jobId}")
    public Response getJobById(@PathParam("jobId") String stageId) {
        ServiceResult response = this.jobService.getById(stageId);
        if (response.getNotificationType() == NotificationType.ERROR) {
            return Response.status(Status.NOT_FOUND).entity(response.getMessage()).build();
        }
        return Response.status(Status.OK).entity(response.getObject()).build();
    }
/*
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{jobId}/latest")
    public Response getLatest(@PathParam("jobId") String stageId) {
        // TODO: service to be implemented.
        return Response.noContent().build();
    }
    */
}
