package io.hawkcd.services.tests;

import com.fiftyonred.mock_jedis.MockJedisPool;

import io.hawkcd.Config;
import io.hawkcd.db.IDbRepository;
import io.hawkcd.db.redis.RedisRepository;
import io.hawkcd.model.ServiceResult;
import io.hawkcd.model.Stage;
import io.hawkcd.model.enums.NotificationType;
import io.hawkcd.model.enums.StageStatus;
import io.hawkcd.services.MaterialDefinitionService;
import io.hawkcd.services.PipelineDefinitionService;
import io.hawkcd.services.PipelineService;
import io.hawkcd.services.StageService;
import io.hawkcd.services.interfaces.IMaterialDefinitionService;
import io.hawkcd.services.interfaces.IPipelineDefinitionService;
import io.hawkcd.services.interfaces.IPipelineService;
import io.hawkcd.services.interfaces.IStageService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.hawkcd.model.MaterialDefinition;
import io.hawkcd.model.Pipeline;
import io.hawkcd.model.PipelineDefinition;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class StageServiceTests {
    private IPipelineService pipelineService;
    private IStageService stageService;
    private IPipelineDefinitionService pipelineDefinitionService;
    private IMaterialDefinitionService materialDefinitionService;
    private Pipeline pipeline;
    private PipelineDefinition pipelineDefinition;
    private Stage stage;

    @BeforeClass
    public static void setUpClass() {
        Config.configure();
    }

    @Before
    public void setUp() {
        MockJedisPool mockJedisPool = new MockJedisPool(new JedisPoolConfig(), "testStageService");
        IDbRepository pipelineRepo = new RedisRepository(Pipeline.class, mockJedisPool);
        IDbRepository pipelineDefinitionRepo = new RedisRepository(PipelineDefinition.class, mockJedisPool);
        IDbRepository materialDefinitionRepo = new RedisRepository(MaterialDefinition.class, mockJedisPool);
        this.pipelineDefinitionService = new PipelineDefinitionService(pipelineDefinitionRepo);
        this.materialDefinitionService = new MaterialDefinitionService(materialDefinitionRepo, this.pipelineDefinitionService);
        this.pipelineService = new PipelineService(pipelineRepo, this.pipelineDefinitionService, this.materialDefinitionService);
        this.stageService = new StageService(this.pipelineService);
    }

    @Test
    public void getAll_receiveEmptyList() {
        //Arrange
        final String expectedMessage = "Stages retrieved successfully.";
        List<Stage> stageList = new ArrayList<>();

        //Act
        ServiceResult actualResult = this.stageService.getAll();

        //Assert
        Assert.assertEquals(NotificationType.SUCCESS, actualResult.getNotificationType());
        assertNotNull(actualResult.getObject());
        assertEquals(actualResult.getMessage(), expectedMessage);
        assertEquals(actualResult.getObject(), stageList);
    }

    @Test
    public void getAll_setOneObject_getOneObject() {
        //Arrange
        final String expectedMessage = "Stages retrieved successfully.";
        List<Stage> stageList = new ArrayList<>();
        this.insertIntoDb();
        this.stageService.add(this.stage);
        stageList.add(this.stage);

        //Act
        ServiceResult actualResult = this.stageService.getAll();
        List<Stage> resultList = (List<Stage>) actualResult.getObject();
        Stage actualObject = resultList.stream().findFirst().get();

        //Assert
        Assert.assertEquals(NotificationType.SUCCESS, actualResult.getNotificationType());
        assertNotNull(actualResult.getObject());
        assertEquals(actualResult.getMessage(), expectedMessage);
        assertEquals(1, resultList.size());
        assertEquals(this.stage.getId(), actualObject.getId());
    }

    @Test
    public void getById_returnsOneObject() {
        //Arrange
        this.insertIntoDb();
        this.stageService.add(this.stage);
        String expectedMessage = "Stage " + this.stage.getId() + " retrieved successfully.";

        //Act
        ServiceResult actualResult = this.stageService.getById(this.stage.getId());
        Stage actualStage = (Stage) actualResult.getObject();


        //Assert
        Assert.assertEquals(NotificationType.SUCCESS, actualResult.getNotificationType());
        assertNotNull(actualResult.getObject());
        assertEquals(expectedMessage, actualResult.getMessage());
        assertEquals(this.stage.getId(), ((Stage) actualResult.getObject()).getId());
    }

    @Test
    public void getById_wrongId_returnsError() {
        //Arrange
        UUID randomId = UUID.randomUUID();
        String expectedMessage = "Stage not found.";

        //Act
        ServiceResult actualResult = this.stageService.getById(randomId.toString());

        //Assert
        assertEquals(NotificationType.ERROR, actualResult.getNotificationType());
        assertNull(actualResult.getObject());
        assertEquals(expectedMessage, actualResult.getMessage());
    }

    @Test
    public void addOneObject_returnsSuccessResult() {
        //Arrange
        this.insertIntoDb();

        //Act
        ServiceResult actualResult = this.stageService.add(this.stage);
        Stage actualStatus = (Stage) actualResult.getObject();
        String expectedMessage = actualResult.getObject().getClass().getSimpleName() +
                " " + actualStatus.getId() + " created successfully.";

        //Assert
        Assert.assertEquals(NotificationType.SUCCESS, actualResult.getNotificationType());
        assertNotNull(actualResult.getObject());
        assertEquals(expectedMessage, actualResult.getMessage());
        assertEquals(this.stage.getId(), ((Stage) actualResult.getObject()).getId());

    }

    @Test
    public void updateSingleObject_returnsSuccessResult() {
        //Arrange
        this.insertIntoDb();
        this.stageService.add(this.stage);

        //Act
        this.stage.setStatus(StageStatus.PASSED);
        ServiceResult actualResult = this.stageService.update(this.stage);
        Stage actualStatus = (Stage) actualResult.getObject();
        String expectedMessage = actualResult.getObject().getClass().getSimpleName() +
                " " + actualStatus.getId() + " updated successfully.";

        //Assert
        Assert.assertEquals(NotificationType.SUCCESS, actualResult.getNotificationType());
        assertNotNull(actualResult.getObject());
        assertEquals(expectedMessage, actualResult.getMessage());
        assertEquals(StageStatus.PASSED, actualStatus.getStatus());
    }

    @Test
    public void delete_oneStage_successMessage() {
        //Arrange
        this.insertIntoDb();
        Stage stageTwo = new Stage();
        stageTwo.setPipelineId(this.pipeline.getId());
        this.stageService.add(stageTwo);
        this.stageService.add(this.stage);
        String expectedMessage = "Stage " + this.stage.getId() + " deleted successfully.";

        //Act
        ServiceResult actualResult = this.stageService.delete(this.stage.getId());

        //Assert
        Assert.assertEquals(NotificationType.SUCCESS, actualResult.getNotificationType());
        assertNotNull(actualResult.getObject());
        assertEquals(expectedMessage, actualResult.getMessage());
    }

    private void insertIntoDb() {
        this.pipelineDefinition = new PipelineDefinition();
        this.pipelineDefinition.setName("pipelinedefinition");
        this.pipelineDefinitionService.add(this.pipelineDefinition);
        this.pipeline = new Pipeline();
        this.pipeline.setPipelineDefinitionName(this.pipelineDefinition.getName());
        this.pipeline.setPipelineDefinitionId(this.pipelineDefinition.getId());
        this.pipelineService.add(this.pipeline);
        this.stage = new Stage();
        this.stage.setPipelineId(this.pipeline.getId());
    }
}
