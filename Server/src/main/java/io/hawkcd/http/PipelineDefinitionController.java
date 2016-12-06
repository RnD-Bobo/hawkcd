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

import io.hawkcd.utilities.SchemaValidator;
import io.hawkcd.model.PipelineDefinition;
import io.hawkcd.model.ServiceResult;
import io.hawkcd.model.enums.NotificationType;
import io.hawkcd.services.PipelineDefinitionService;
import io.hawkcd.services.interfaces.IPipelineDefinitionService;
import io.swagger.annotations.Api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Consumes("application/json")
@Produces("application/json")
@Path("/pipeline-definitions")
@Api(value = "/pipeline-definitions", description = "Web Services to browse entities")
public class PipelineDefinitionController {
    private IPipelineDefinitionService pipelineDefinitionService;
    private SchemaValidator schemaValidator;

    public PipelineDefinitionController() {
        this.pipelineDefinitionService = new PipelineDefinitionService();
        this.schemaValidator = new SchemaValidator();
    }

    public PipelineDefinitionController(IPipelineDefinitionService pipelineDefinitionService) {
        this.pipelineDefinitionService = pipelineDefinitionService;
        this.schemaValidator = new SchemaValidator();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPipelineDefinitions() {
        ServiceResult result = this.pipelineDefinitionService.getAll();
        return Response.status(Status.OK)
                .entity(result.getEntity())
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{pipelineDefinitionId}")
    public Response getPipelineDefinitionById(@PathParam("pipelineDefinitionId")
                                                      String pipelineDefinitionId) {
        ServiceResult result = this.pipelineDefinitionService.getById(pipelineDefinitionId);
        if (result.getNotificationType() == NotificationType.ERROR) {
            return Response.status(Status.NOT_FOUND)
                    .entity(result.getMessage())
                    .type(MediaType.TEXT_HTML)
                    .build();
        }
        return Response.status(Status.OK)
                .entity(result.getEntity())
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewPipeline(PipelineDefinition pipelineDefinition) {
        String isValid = this.schemaValidator.validate(pipelineDefinition);
        if (isValid.equals("OK")) {
            ServiceResult result = this.pipelineDefinitionService.add(pipelineDefinition);
            if (result.getNotificationType() == NotificationType.ERROR) {
                return Response.status(Status.BAD_REQUEST)
                        .entity(result.getMessage())
                        .type(MediaType.TEXT_HTML)
                        .build();
            }
            return Response.status(Status.CREATED)
                    .entity(result.getEntity())
                    .build();
        } else {
            return Response.status(Status.BAD_REQUEST)
                    .entity(isValid)
                    .type(MediaType.TEXT_HTML)
                    .build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePipeline(PipelineDefinition pipelineDefinition) {
        String isValid = this.schemaValidator.validate(pipelineDefinition);
        if (isValid.equals("OK")) {
            ServiceResult result = this.pipelineDefinitionService.update(pipelineDefinition);
            if (result.getNotificationType() == NotificationType.ERROR) {
                return Response.status(Status.BAD_REQUEST)
                        .entity(result.getMessage())
                        .type(MediaType.TEXT_HTML)
                        .build();
            }
            return Response.status(Status.OK)
                    .entity(result.getEntity())
                    .build();

        } else {
            return Response.status(Status.BAD_REQUEST)
                    .entity(isValid)
                    .type(MediaType.TEXT_HTML)
                    .build();
        }
    }

//    @DELETE
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Path("/{pipelineDefinitionId}")
//    public Response deletePipeline(@PathParam("pipelineDefinitionId")
//                                           String pipelineDefinitionId) {
//        ServiceResult result = this.pipelineDefinitionService.delete(pipelineDefinitionId);
//        if (result.getNotificationType() == NotificationType.ERROR) {
//            return Response.status(Status.BAD_REQUEST)
//                    .entity(result.getMessage())
//                    .type(MediaType.TEXT_HTML)
//                    .build();
//        }
//
//        return Response.status(Status.NO_CONTENT).build();
//    }
}