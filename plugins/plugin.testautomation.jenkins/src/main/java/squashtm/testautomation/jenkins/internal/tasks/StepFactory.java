package squashtm.testautomation.jenkins.internal.tasks;

public interface StepFactory {

	RemoteBuildStep<?> getNextStepFor(RemoteBuildStep<?> previousStep);
	
	int getMillisecondDelayFor(RemoteBuildStep<?> previousStep);
	
}
