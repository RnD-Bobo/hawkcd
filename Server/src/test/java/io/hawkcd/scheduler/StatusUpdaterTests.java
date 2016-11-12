package io.hawkcd.scheduler;

import com.fiftyonred.mock_jedis.MockJedisPool;

import io.hawkcd.Config;
import io.hawkcd.db.IDbRepository;
import io.hawkcd.db.redis.RedisRepository;
import io.hawkcd.model.enums.JobStatus;
import io.hawkcd.model.enums.PipelineStatus;
import io.hawkcd.model.Agent;
import io.hawkcd.model.enums.StageStatus;
import io.hawkcd.services.AgentService;
import io.hawkcd.services.MaterialDefinitionService;
import io.hawkcd.services.PipelineDefinitionService;
import io.hawkcd.services.PipelineService;
import io.hawkcd.services.interfaces.IAgentService;
import io.hawkcd.model.Stage;
import io.hawkcd.services.interfaces.IMaterialDefinitionService;
import io.hawkcd.services.interfaces.IPipelineDefinitionService;
import io.hawkcd.services.interfaces.IPipelineService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.hawkcd.model.Job;
import io.hawkcd.model.MaterialDefinition;
import io.hawkcd.model.Pipeline;
import io.hawkcd.model.PipelineDefinition;
import redis.clients.jedis.JedisPoolConfig;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class StatusUpdaterTests {
    private IPipelineService pipelineService;
    private IAgentService agentService;
    private StatusUpdaterService statusUpdaterService;
    private IPipelineDefinitionService pipelineDefinitionService;
    private IMaterialDefinitionService materialDefinitionService;
    private PipelineDefinition expectedPipelineDefinition;
    private PipelinePreparer pipelinePreparer;

    @BeforeClass
    public static void setUpClass() {
        Config.configure();
    }

    @Before
    public void setUp() {
        MockJedisPool mockedPool = new MockJedisPool(new JedisPoolConfig(), "testStatusUpdater");
        IDbRepository pipelineRepo = new RedisRepository(Pipeline.class, mockedPool);
        IDbRepository agentRepo = new RedisRepository(Agent.class, mockedPool);
        IDbRepository pipelineDefintionRepo = new RedisRepository(PipelineDefinition.class, mockedPool);
        IDbRepository materialDefinitionRepo = new RedisRepository(MaterialDefinition.class, mockedPool);
        this.pipelineDefinitionService = new PipelineDefinitionService(pipelineDefintionRepo);
        this.materialDefinitionService = new MaterialDefinitionService(materialDefinitionRepo, this.pipelineDefinitionService);
        this.pipelineService = new PipelineService(pipelineRepo, this.pipelineDefinitionService, this.materialDefinitionService);
        this.agentService = new AgentService(agentRepo, this.pipelineService);
        this.pipelinePreparer = new PipelinePreparer(this.pipelineService, this.pipelineDefinitionService);
        this.statusUpdaterService = new StatusUpdaterService(this.agentService ,this.pipelineService);
        this.expectedPipelineDefinition = new PipelineDefinition();
        this.pipelineDefinitionService.add(this.expectedPipelineDefinition);
    }

    @Test
    public void statusUpdater_updateStatusesWithNoStages_inProgressStatuses(){
        List<Pipeline> expectedPipelines = this.injectDataForTestingStatusUpdater();

        this.statusUpdaterService.updateStatuses();

        List<Pipeline> actualPipelines = (List<Pipeline>) this.pipelineService.getAll().getObject();

        for (Pipeline actualPipeline: actualPipelines) {
            Assert.assertEquals(PipelineStatus.IN_PROGRESS, actualPipeline.getStatus());
        }
    }

    @Test
    public void statusUpdater_failedJob_failedPipeline() {
        List<Pipeline> expectedPipelines = this.injectDataForTestingStatusUpdater();
        for (Pipeline expectedPipelineObject : expectedPipelines) {
            this.statusUpdaterService.updateAllStatuses(expectedPipelineObject);
            List<Stage> stages = expectedPipelineObject.getStages();
            for (Stage stage : stages) {
                List<Job> jobs = stage.getJobs();
                jobs.stream().filter(job -> job.getStatus() == JobStatus.FAILED).forEach(job -> {
                    String pipelineId = expectedPipelineObject.getId();
                    Pipeline actualPipeline = (Pipeline) this.pipelineService.getById(pipelineId).getObject();
                    Assert.assertNotEquals(PipelineStatus.FAILED, actualPipeline.getStatus());
                });
            }
        }
    }

    @Test
    public void statusUpdater_allPassedJobs_passedPipeline() {
        List<Pipeline> expectedPipelines = this.injectDataForTestingStatusUpdater();
        int passedJobsIterator = 0;
        for (Pipeline expectedPipelineObject : expectedPipelines) {
            this.statusUpdaterService.updateAllStatuses(expectedPipelineObject);
            List<Stage> stages = expectedPipelineObject.getStages();
            for (Stage stage : stages) {
                List<Job> jobs = stage.getJobs();
                passedJobsIterator = 0;
                for (Job job : jobs) {
                    if (job.getStatus() == JobStatus.PASSED) {
                        passedJobsIterator++;
                    }
                }
                if (passedJobsIterator == jobs.size()) {
                    this.pipelineService.update(expectedPipelineObject);
                    String pipelineId = expectedPipelineObject.getId();
                    Pipeline actualPipeline = (Pipeline) this.pipelineService.getById(pipelineId).getObject();
                    Assert.assertEquals(PipelineStatus.PASSED, actualPipeline.getStatus());
                }
            }
        }
    }

    @Test
    public void statusUpdater_awaitingJobStatus_notModifiedPipelineStatus() {
        List<Pipeline> expectedPipelines = this.injectDataForTestingStatusUpdater();
        int failedJobsIterator = 0;
        for (Pipeline expectedPipelineObject : expectedPipelines) {
            this.statusUpdaterService.updateAllStatuses(expectedPipelineObject);
            List<Stage> stages = expectedPipelineObject.getStages();
            for (Stage stage : stages) {
                List<Job> jobs = stage.getJobs();
                failedJobsIterator = 0;
                for (Job job : jobs) {
                    if (job.getStatus() == JobStatus.PASSED) {
                        failedJobsIterator++;
                    }
                }
                if (failedJobsIterator == 0) {
                    String pipelineId = expectedPipelineObject.getId();
                    Pipeline actualPipeline = (Pipeline) this.pipelineService.getById(pipelineId).getObject();
                    Assert.assertEquals(expectedPipelineObject.getStatus(), actualPipeline.getStatus());
                }
            }
        }
    }

//    @Test
//    public void runStatusUpdater_interruptedThread_throwInterruptedException() {
//        InterruptedException interrupt = new InterruptedException();
//        try {
//            Thread.currentThread().interrupt();
//            this.statusUpdaterService.();
//        } catch (IllegalStateException e) {
//            Assert.assertEquals(interrupt, e.getCause());
//        }
//    }

    @Test
    public void statusUpdater_updateStageStatusesInSequenceWithFirstPassedStage_updatedStatuses(){
        Stage firstExpectedStage= new Stage();
        firstExpectedStage.setStatus(StageStatus.PASSED);

        Stage secondExpectedStage = new Stage();

        Stage thirdExpectedStage = new Stage();

        List<Stage> expectedStages = new ArrayList<>();
        expectedStages.add(firstExpectedStage);
        expectedStages.add(secondExpectedStage);
        expectedStages.add(thirdExpectedStage);

        this.statusUpdaterService.updateStageStatusesInSequence(expectedStages);

        Assert.assertEquals(StageStatus.IN_PROGRESS, secondExpectedStage.getStatus());
        Assert.assertEquals(StageStatus.NOT_RUN, thirdExpectedStage.getStatus());
    }

    @Test
    public void statusUpdater_updateStageStatusesInSequenceWithSecondPassedStage_updatedStatuses(){
        Stage firstExpectedStage= new Stage();
        firstExpectedStage.setStatus(StageStatus.PASSED);

        Stage secondExpectedStage = new Stage();
        secondExpectedStage.setStatus(StageStatus.FAILED);

        Stage thirdExpectedStage = new Stage();

        List<Stage> expectedStages = new ArrayList<>();
        expectedStages.add(firstExpectedStage);
        expectedStages.add(secondExpectedStage);
        expectedStages.add(thirdExpectedStage);

        this.statusUpdaterService.updateStageStatusesInSequence(expectedStages);

        Assert.assertEquals(StageStatus.NOT_RUN, thirdExpectedStage.getStatus());
    }

    @Test
    public void statusUpdater_updateStageStatusesInSequenceWithNotRunStages_updatedFirstStageStatus(){
        Stage firstExpectedStage= new Stage();

        Stage secondExpectedStage = new Stage();

        Stage thirdExpectedStage = new Stage();

        List<Stage> expectedStages = new ArrayList<>();
        expectedStages.add(firstExpectedStage);
        expectedStages.add(secondExpectedStage);
        expectedStages.add(thirdExpectedStage);

        this.statusUpdaterService.updateStageStatusesInSequence(expectedStages);

        Assert.assertEquals(StageStatus.IN_PROGRESS, firstExpectedStage.getStatus());
        Assert.assertEquals(StageStatus.NOT_RUN, secondExpectedStage.getStatus());
        Assert.assertEquals(StageStatus.NOT_RUN, thirdExpectedStage.getStatus());
    }

    @Test
    public void statusUpdater_updateStageStatusWithPassedJobs_passed() {
        Job firstExpectedJob = new Job();
        firstExpectedJob.setStatus(JobStatus.PASSED);

        Job secondExpectedJob = new Job();
        secondExpectedJob.setStatus(JobStatus.PASSED);

        List<Job> expextedJobs = new ArrayList<>();
        expextedJobs.add(firstExpectedJob);
        expextedJobs.add(secondExpectedJob);

        Stage expectedStage = new Stage();
        expectedStage.setJobs(expextedJobs);

        this.statusUpdaterService.updateStageStatus(expectedStage);

        Assert.assertEquals(StageStatus.PASSED, expectedStage.getStatus());
    }

    @Test
    public void statusUpdater_updateStageStatusWithFailedJob_failed() {
        Job firstExpectedJob = new Job();
        firstExpectedJob.setStatus(JobStatus.PASSED);

        Job secondExpectedJob = new Job();
        secondExpectedJob.setStatus(JobStatus.FAILED);

        List<Job> expextedJobs = new ArrayList<>();
        expextedJobs.add(firstExpectedJob);
        expextedJobs.add(secondExpectedJob);

        Stage expectedStage = new Stage();
        expectedStage.setJobs(expextedJobs);

        this.statusUpdaterService.updateStageStatus(expectedStage);

        Assert.assertEquals(StageStatus.FAILED, expectedStage.getStatus());
    }

    @Test
    public void statusUpdater_updatePipelineStatusWithPassedStages_passed() {
        Stage firstExpectedStage = new Stage();
        firstExpectedStage.setStatus(StageStatus.PASSED);

        Stage secondExpectedStaage = new Stage();
        secondExpectedStaage.setStatus(StageStatus.PASSED);

        List<Stage> expextedStages = new ArrayList<>();
        expextedStages.add(firstExpectedStage);
        expextedStages.add(secondExpectedStaage);

        Pipeline expectedPipeline = new Pipeline();
        expectedPipeline.setStages(expextedStages);

        this.statusUpdaterService.updatePipelineStatus(expectedPipeline);

        Assert.assertEquals(PipelineStatus.PASSED, expectedPipeline.getStatus());
    }

    @Test
    public void statusUpdater_updatePipelineStatusWithFailedStage_failed() {
        Stage firstExpectedStage = new Stage();
        firstExpectedStage.setStatus(StageStatus.PASSED);

        Stage secondExpectedStaage = new Stage();
        secondExpectedStaage.setStatus(StageStatus.FAILED);

        List<Stage> expextedStages = new ArrayList<>();
        expextedStages.add(firstExpectedStage);
        expextedStages.add(secondExpectedStaage);

        Pipeline expectedPipeline = new Pipeline();
        expectedPipeline.setStages(expextedStages);

        this.statusUpdaterService.updatePipelineStatus(expectedPipeline);

        Assert.assertEquals(PipelineStatus.FAILED, expectedPipeline.getStatus());
    }

    @Test
    public void statusUpdater_areAllPassedWithListOfPassed_true() {
        List<JobStatus> expectedJobStatuses = new ArrayList<>();
        expectedJobStatuses.add(JobStatus.PASSED);
        expectedJobStatuses.add(JobStatus.PASSED);

        boolean actualResult = this.statusUpdaterService.areAllPassed(expectedJobStatuses);

        Assert.assertTrue(actualResult);
    }

    @Test
    public void statusUpdater_areAllPassedWithListWithFailed_false() {
        List<JobStatus> expectedJobStatuses = new ArrayList<>();
        expectedJobStatuses.add(JobStatus.PASSED);
        expectedJobStatuses.add(JobStatus.FAILED);

        boolean actualResult = this.statusUpdaterService.areAllPassed(expectedJobStatuses);

        Assert.assertFalse(actualResult);
    }

    @Test
    public void updateAgentStatus_agentToBeDisconnected_agentSetToDisconnected() {
        // Arrange
        Agent agent = new Agent();
        agent.setConnected(true);
        agent.setLastReportedTime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime().minusSeconds(20));

        // Act
        Agent actualResult = this.statusUpdaterService.updateAgentStatus(agent);

        // Assert
        Assert.assertFalse(actualResult.isConnected());
    }

    @Test
    public void updateAgentStatus_agentNotToBeDisconnected_agentIsStillConnected() {
        // Arrange
        Agent agent = new Agent();
        agent.setConnected(true);
        agent.setLastReportedTime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());

        // Act
        Agent actualResult = this.statusUpdaterService.updateAgentStatus(agent);

        // Assert
        Assert.assertTrue(actualResult.isConnected());
    }

    private List<Pipeline> injectDataForTestingStatusUpdater() {
        List<Pipeline> pipelines = new ArrayList<>();
        List<Job> jobsToAdd = new ArrayList<>();
        List<Stage> stagesToAdd = new ArrayList<>();

        Pipeline firstPipeline = new Pipeline();
        firstPipeline.setPipelineDefinitionId(this.expectedPipelineDefinition.getId());

        Stage stage = new Stage();

        Job firstJob = new Job();
        firstJob.setStatus(JobStatus.PASSED);

        Job secondJob = new Job();
        secondJob.setStatus(JobStatus.PASSED);

        jobsToAdd.add(firstJob);
        jobsToAdd.add(secondJob);

        stage.setJobs(jobsToAdd);
        stagesToAdd.add(stage);

        firstPipeline.setStages(stagesToAdd);
        pipelines.add(firstPipeline);
        this.pipelineService.add(firstPipeline);

        Pipeline secondPipeline = new Pipeline();
        secondPipeline.setPipelineDefinitionId(this.expectedPipelineDefinition.getId());
        jobsToAdd = new ArrayList<>();
        stagesToAdd = new ArrayList<>();

        firstJob.setStatus(JobStatus.PASSED);

        secondJob.setStatus(JobStatus.PASSED);

        jobsToAdd.add(firstJob);
        jobsToAdd.add(secondJob);

        stage.setJobs(jobsToAdd);
        stagesToAdd.add(stage);

        secondPipeline.setStages(stagesToAdd);
        pipelines.add(secondPipeline);
        this.pipelineService.add(secondPipeline);

        Pipeline thirdPipeline = new Pipeline();
        thirdPipeline.setPipelineDefinitionId(this.expectedPipelineDefinition.getId());

        firstJob.setStatus(JobStatus.PASSED);

        secondJob.setStatus(JobStatus.PASSED);

        jobsToAdd = new ArrayList<>();
        stagesToAdd = new ArrayList<>();
        jobsToAdd.add(firstJob);
        jobsToAdd.add(secondJob);

        stage.setJobs(jobsToAdd);
        stagesToAdd.add(stage);

        thirdPipeline.setStages(stagesToAdd);
        pipelines.add(thirdPipeline);
        this.pipelineService.add(thirdPipeline);

        return pipelines;
    }
}
