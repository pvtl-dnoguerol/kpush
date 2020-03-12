package com.whizzosoftware.kpush.listener;

import com.whizzosoftware.kpush.event.UpdateDeploymentEvent;
import com.whizzosoftware.kpush.manager.DeploymentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class UpdateDeploymentListener implements ApplicationListener<UpdateDeploymentEvent> {
    private final Logger logger = LoggerFactory.getLogger(UpdateDeploymentListener.class);

    private DeploymentManager deploymentManager;

    public UpdateDeploymentListener(DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    @Override
    public void onApplicationEvent(UpdateDeploymentEvent event) {
        logger.trace("Received update deployment event: {}", event);

        deploymentManager.updateDeployment(event.getDeployment());
    }
}
