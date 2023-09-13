package io.quarkus.status;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.status.github.GitHubService;
import io.quarkus.status.model.Label;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class LabelsService {

    private static final String QUARKUS_IO_ORG = "quarkusio";
    private static final String MAIN_REPOSITORY = "quarkus";
    public static final String BUG_LABEL = "kind/bug";
    public static final String ENHANCEMENT_LABEL = "kind/enhancement";

    @ConfigProperty(name = "status.labels.subset", defaultValue = "false")
    boolean subsetOnly;

    @Inject
    GitHubService gitHubService;

    private volatile List<Label> bugsLabels;
    private volatile List<Label> enhancementsLabels;

    @Scheduled(every = "2m")
    public void updateStatus() throws Exception {
        bugsLabels = buildLabelsStats(BUG_LABEL);
        enhancementsLabels = buildLabelsStats(ENHANCEMENT_LABEL);
    }

    public List<Label> getBugsLabels() throws Exception {
        List<Label> localLabels = bugsLabels;
        if (localLabels == null) {
            synchronized (this) {
                localLabels = bugsLabels;
                if (localLabels == null) {
                    bugsLabels = localLabels = buildLabelsStats(BUG_LABEL);
                }
            }
        }
        return localLabels;
    }

    public List<Label> getEnhancementsLabels() throws Exception {
        List<Label> localLabels = enhancementsLabels;
        if (localLabels == null) {
            synchronized (this) {
                localLabels = enhancementsLabels;
                if (localLabels == null) {
                    enhancementsLabels = localLabels = buildLabelsStats(ENHANCEMENT_LABEL);
                }
            }
        }
        return localLabels;
    }

    private List<Label> buildLabelsStats(String mainLabel) throws Exception {
        return gitHubService.labelsStats(QUARKUS_IO_ORG, MAIN_REPOSITORY, mainLabel, subsetOnly);
    }

}