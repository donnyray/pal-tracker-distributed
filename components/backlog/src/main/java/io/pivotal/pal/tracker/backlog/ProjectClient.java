package io.pivotal.pal.tracker.backlog;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.web.client.RestOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProjectClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RestOperations restOperations;
    private final String endpoint;
    private ConcurrentMap<Long, ProjectInfo> projectInfoConcurrentMap = new ConcurrentHashMap<>();

    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint) {
        this.restOperations = restOperations;
        this.endpoint = registrationServerEndpoint;
    }

    @HystrixCommand(fallbackMethod = "getProjectFromCache")
    public ProjectInfo getProject(long projectId) {

        ProjectInfo projectInfo = restOperations.getForObject(endpoint + "/projects/" + projectId, ProjectInfo.class);
        projectInfoConcurrentMap.put(projectId, projectInfo);
        return projectInfo;
    }


    public ProjectInfo getProjectFromCache(long projectId) {

        if (projectInfoConcurrentMap.containsKey(projectId)) {
            logger.info("Getting project with id {} from cache", projectId);
            return projectInfoConcurrentMap.get(projectId);
        }
        return null;
    }
}
