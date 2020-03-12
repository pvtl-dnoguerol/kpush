package com.whizzosoftware.kpush.listener;

import com.whizzosoftware.kpush.event.CreateDeploymentEvent;
import com.whizzosoftware.kpush.manager.DeploymentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class CreateDeploymentListener implements ApplicationListener<CreateDeploymentEvent> {
    private final Logger logger = LoggerFactory.getLogger(CreateDeploymentListener.class);

    private DeploymentManager deploymentManager;

    public CreateDeploymentListener(DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    @Override
    public void onApplicationEvent(CreateDeploymentEvent event) {
        logger.trace("Received create deployment event: {}", event);

        deploymentManager.createDeployment(event.getDeployment());
    }
}
