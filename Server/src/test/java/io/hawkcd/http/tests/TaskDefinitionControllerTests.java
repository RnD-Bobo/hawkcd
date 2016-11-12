package io.hawkcd.http.tests;

import io.hawkcd.Config;
import io.hawkcd.http.TaskDefinitionController;
import io.hawkcd.model.ExecTask;
import io.hawkcd.model.ServiceResult;
import io.hawkcd.model.enums.NotificationType;
import io.hawkcd.model.JobDefinition;
import io.hawkcd.model.enums.RunIf;
import io.hawkcd.model.enums.TaskType;
import io.hawkcd.services.TaskDefinitionService;
import io.hawkcd.services.interfaces.ITaskDefinitionService;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import io.hawkcd.model.PipelineDefinition;
import io.hawkcd.model.StageDefinition;
import io.hawkcd.model.TaskDefinition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TaskDefinitionControllerTests extends JerseyTest {
    private TaskDefinitionController taskDefinitionController;
    private ITaskDefinitionService taskDefinitionService;
    private TaskDefinition taskDefinition;
    private ServiceResult serviceResult;

    @BeforeClass
    public static void setUpClass() {
        Config.configure();
    }

    @Override
    public Application configure() {
        this.taskDefinitionService = Mockito.mock(TaskDefinitionService.class);
        this.taskDefinitionController = new TaskDefinitionController(this.taskDefinitionService);
        this.serviceResult = new ServiceResult();
        return new ResourceConfig().register(this.taskDefinitionController);
    }

    @Test
    public void TaskDefinitionController_constructorTest_notNull() {

        TaskDefinitionController taskDefinitionController = new TaskDefinitionController();

        assertNotNull(taskDefinitionController);
    }

    @Test
    public void getTaksDefinitions_nonExistingObjects_emptyList() {
        //Arrange
        List<TaskDefinition> expectedResult = new ArrayList<>();
        this.serviceResult.setObject(expectedResult);
        Mockito.when(this.taskDefinitionService.getAll()).thenReturn(this.serviceResult);

        //Act
        Response response = target("/task-definitions").request().get();
        List<TaskDefinition> actualResult = response.readEntity(List.class);

        //Assert
        assertEquals(200, response.getStatus());
        assertEquals(expectedResult.size(), actualResult.size());

    }

    @Test
    public void getAllTaskDefinitions_existingObjects_twoObjects() {
        //Arrange
        List<TaskDefinition> expectedResult = new ArrayList<>();
        this.taskDefinition = new ExecTask();
        expectedResult.add(this.taskDefinition);
        expectedResult.add(this.taskDefinition);
        this.serviceResult.setObject(expectedResult);
        Mockito.when(this.taskDefinitionService.getAll()).thenReturn(this.serviceResult);

        //Act
        Response response = target("/task-definitions").request().get();
        List<PipelineDefinition> actualResult = response.readEntity(List.class);

        //Assert
        assertEquals(200, response.getStatus());
        assertEquals(expectedResult.size(), actualResult.size());
    }

    @Test
    public void getTaskDefinitionById_existingObject_correctObject() {
        ///Arrange
        this.taskDefinition = new ExecTask();
        this.serviceResult.setObject(this.taskDefinition);
        Mockito.when(this.taskDefinitionService.getById(Mockito.anyString())).thenReturn(this.serviceResult);
        TaskDefinition expectedResult = this.taskDefinition;

        //Act
        Response response = target("/task-definitions/" + this.taskDefinition.getId()).request().get();
        TaskDefinition actualResult = response.readEntity(TaskDefinition.class);

        //Assert
        assertEquals(200, response.getStatus());
        assertEquals(expectedResult.getId(), actualResult.getId());
    }

    @Test
    public void getTaskDefinitionById_nonExistingObject_properErrorMessage() {
        //Arrange
        String expectedResult = "TaskDefinition not found.";
        this.serviceResult.setMessage(expectedResult);
        this.serviceResult.setNotificationType(NotificationType.ERROR);
        this.serviceResult.setObject(null);
        Mockito.when(this.taskDefinitionService.getById(Mockito.any())).thenReturn(this.serviceResult);

        //Act
        Response response = target("/task-definitions/wrongId").request().get();
        String actualResult = response.readEntity(String.class);

        //Assert
        assertEquals(404, response.getStatus());
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void addTaskDefinition_oneObject_successMessage() {
        //Arrange
        this.prepareTaskDefinition();
        this.serviceResult.setObject(this.taskDefinition);
        Mockito.when(this.taskDefinitionService.addTask(Mockito.anyObject())).thenReturn(this.serviceResult);
        Entity entity = Entity.entity(this.taskDefinition, "application/json");
        TaskDefinition expectedResult = this.taskDefinition;

        //Act
        Response response = target("/task-definitions").request().post(entity);
        TaskDefinition actualResult = response.readEntity(TaskDefinition.class);

        //Assert
        assertEquals(201, response.getStatus());
        assertEquals(expectedResult.getId(), actualResult.getId());
    }

    @Test
    public void addTaskDefinition_invalidField_properErrorMessage() {
        //Arrange
        this.prepareTaskDefinition();
        this.taskDefinition.setName(null);
        String expectedResult = "ERROR: TASK DEFINITION NAME IS NULL.";
        this.serviceResult.setMessage(expectedResult);
        this.serviceResult.setNotificationType(NotificationType.ERROR);
        this.serviceResult.setObject(this.taskDefinition);
        Mockito.when(this.taskDefinitionService.addTask(Mockito.anyObject())).thenReturn(this.serviceResult);
        Entity entity = Entity.entity(this.taskDefinition, "application/json");

        //Act
        Response response = target("/task-definitions").request().post(entity);
        String actualResult = response.readEntity(String.class);

        //Assert
        assertEquals(400, response.getStatus());
        assertEquals(expectedResult, actualResult);
    }

    /*
    TODO: service that checks for object duplication to be implemented.
    @Test
    public void addTaskDefinition_existingObject_properErrorMessage() {
        //Arrange
        this.prepareTaskDefinition();
        String expectedResult = "TaskDefinition already exists.";
        this.serviceResult.setError(true);
        this.serviceResult.setMessage(expectedResult);
        Mockito.when(this.taskDefinitionService.addTask(Mockito.anyObject())).thenReturn(this.serviceResult);
        Entity entity = Entity.entity(this.taskDefinition, "application/json");

        //Act
        Response response = target("/task-definitions").request().post(entity);
        String actualResult = response.readEntity(String.class);

        //Assert
        assertEquals(400, response.getStatus());
        assertEquals(expectedResult, actualResult);
    }

    TODO: service that checks for name collision to be implemented.
    @Test
    public void addStageDefinition_withSameName_properErrorMessage() {
        //Arrange
        this.prepareTaskDefinition();
        this.serviceResult.setObject(null);
        this.serviceResult.setError(true);
        String expectedResult = "TaskDefinition with that name already exists.";
        this.serviceResult.setMessage(expectedResult);
        Mockito.when(this.taskDefinitionService.addTask(Mockito.anyObject())).thenReturn(this.serviceResult);
        Entity entity = Entity.entity(this.taskDefinition, "application/json");

        //Act
        Response response = target("/task-definitions/").request().post(entity);
        String actualResult = response.readEntity(String.class);

        //Assert
        assertEquals(400, response.getStatus());
        assertEquals(expectedResult, actualResult);
    }
*/
    @Test
    public void updateTaskDefinition_existingTaskDefinition_updatedTaskDefinition() {
        //Arrange
        this.prepareTaskDefinition();
        this.serviceResult.setObject(this.taskDefinition);
        this.taskDefinition.setName("name-updated");
        Mockito.when(this.taskDefinitionService.updateTask(Mockito.anyObject())).thenReturn(this.serviceResult);
        Entity entity = Entity.entity(this.taskDefinition, "application/json");
        TaskDefinition expectedResult = this.taskDefinition;

        //Act
        Response response = target("task-definitions/").request().put(entity);
        TaskDefinition actualResult = response.readEntity(TaskDefinition.class);

        //Assert
        assertEquals(200, response.getStatus());
        assertEquals(expectedResult.getName(), actualResult.getName());
    }

    @Test
    public void updateTaskDefinition_nonExistingTaskDefinition_properErrorMessage() {
        //Arrange
        this.prepareTaskDefinition();
        String expectedMessage = "TaskDefinition not found.";
        this.serviceResult.setNotificationType(NotificationType.ERROR);
        this.serviceResult.setMessage(expectedMessage);
        Mockito.when(this.taskDefinitionService.updateTask(Mockito.anyObject())).thenReturn(this.serviceResult);
        Entity entity = Entity.entity(this.taskDefinition, "application/json");

        //Act
        Response response = target("/task-definitions/").request().put(entity);
        String actualMessage = response.readEntity(String.class);

        //Assert
        assertEquals(400, response.getStatus());
        assertEquals(expectedMessage, actualMessage);
    }

    /*
        TODO: service that checks for name collision to be implemented.
        @Test
        public void updateTaskDefinition_withSameName_properErrorMessage() {
            //Arrange
            this.prepareTaskDefinition();
            String expectedMessage = "TaskDefinition with that name already exists";
            this.serviceResult.setError(true);
            this.serviceResult.setMessage(expectedMessage);
            Mockito.when(this.taskDefinitionService.updateTask(Mockito.anyObject())).thenReturn(this.serviceResult);
            Entity entity = Entity.entity(this.taskDefinition, "application/json");

            //Act
            Response response = target("/task-definitions/").request().put(entity);
            String actualMessage = response.readEntity(String.class);


            //Assert
            assertEquals(400, response.getStatus());
            assertEquals(expectedMessage, actualMessage);
        }
    */
    @Test
    public void updateTaskDefinition_invalidField_properErrorMessage() {
        //Arrange
        this.prepareTaskDefinition();
        String expectedResult = "ERROR: TASK DEFINITION NAME IS NULL.";
        this.serviceResult.setMessage(expectedResult);
        this.serviceResult.setNotificationType(NotificationType.ERROR);
        this.taskDefinition.setName(null);
        Mockito.when(this.taskDefinitionService.updateTask(Mockito.anyObject())).thenReturn(this.serviceResult);
        Entity entity = Entity.entity(this.taskDefinition, "application/json");

        //Act
        Response response = target("/task-definitions").request().put(entity);
        String actualResult = response.readEntity(String.class);

        //Assert
        assertEquals(400, response.getStatus());
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void deleteTaskDefinition_taskDefinition_successMessage() {
        //Arrange
        this.prepareTaskDefinition();
        Mockito.when(this.taskDefinitionService.delete(Mockito.anyString())).thenReturn(this.serviceResult);

        //Act
        Response response = target("/task-definitions/" + this.taskDefinition.getId()).request().delete();

        //Assert
        assertEquals(204, response.getStatus());
    }

    @Test
    public void deleteTaskDefinition_nonExistingTaskDefinition_errorMessage() {
        //Arrange
        String expectedMessage = "TaskDefinition not found.";
        this.serviceResult.setNotificationType(NotificationType.ERROR);
        this.serviceResult.setMessage(expectedMessage);
        Mockito.when(this.taskDefinitionService.delete(Mockito.anyString())).thenReturn(this.serviceResult);

        //Act
        Response response = target("/task-definitions/wrongId").request().delete();
        String actualMessage = response.readEntity(String.class);

        //Assert
        assertEquals(400, response.getStatus());
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void deleteTaskDefinition_lastTaskDefinition_errorMessage() {
        //Arrange
        this.prepareTaskDefinition();
        String expectedMessage = this.taskDefinition.getId() + " cannot delete the last task definition";
        this.serviceResult.setNotificationType(NotificationType.ERROR);
        this.serviceResult.setMessage(expectedMessage);
        Mockito.when(this.taskDefinitionService.delete(Mockito.anyString())).thenReturn(this.serviceResult);

        //Act
        Response response = target("/task-definitions/" + this.taskDefinition.getId()).request().delete();
        String actualMessage = response.readEntity(String.class);

        //Assert
        assertEquals(400, response.getStatus());
        assertEquals(expectedMessage, actualMessage);
    }

    private void prepareTaskDefinition() {
        PipelineDefinition pipelineDefinition = new PipelineDefinition();
        pipelineDefinition.setName("pipelineDefinition");
        StageDefinition stageDefinition = new StageDefinition();
        stageDefinition.setName("stageDefinition");
        stageDefinition.setPipelineDefinitionId(pipelineDefinition.getId());
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setName("jobDefinition");
        jobDefinition.setStageDefinitionId(stageDefinition.getId());
        ExecTask execTask = new ExecTask();
        execTask.setJobDefinitionId(jobDefinition.getId());
        execTask.setName("execTask");
        execTask.setCommand("taskCommand");
        String arguments = new String("argument");
        execTask.setArguments(arguments);
        execTask.setRunIfCondition(RunIf.PASSED);
        execTask.setType(TaskType.EXEC);
        this.taskDefinition = execTask;
        List<TaskDefinition> taskDefinitions = new ArrayList<>();
        taskDefinitions.add(this.taskDefinition);
        List<JobDefinition> jobDefinitions = new ArrayList<>();
        jobDefinition.setTaskDefinitions(taskDefinitions);
        jobDefinitions.add(jobDefinition);
        List<StageDefinition> stageDefinitions = new ArrayList<>();
        stageDefinition.setJobDefinitions(jobDefinitions);
        stageDefinitions.add(stageDefinition);
        pipelineDefinition.setStageDefinitions(stageDefinitions);
        taskDefinitionService.addTask(taskDefinition);
    }
}
