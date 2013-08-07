package org.jboss.processFlow.services.remote.cdi;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import org.jbpm.kie.services.api.DeploymentService;
import org.jbpm.kie.services.api.DeploymentUnit;
import org.jbpm.kie.services.api.DeploymentUnit.RuntimeStrategy;
import org.jbpm.kie.services.api.Vfs;
import org.jbpm.kie.services.impl.VFSDeploymentUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Triggers org.jbpm.kie.services.impl.event.Deploy &  org.jbpm.kie.services.impl.event.UnDeploy events.
 * These events are captured in org.kie.services.remote.cdi.RuntimeManagerManager.
 */
@Singleton
@Startup
public class RESTApplicationStartup {
    
    public static final String DEPLOYMENT_ID = "org.jboss.processFlow.cdi.deployment.id";
    public static final String VFS_PATH = "org.jboss.processFlow.cdi.vfs.path";
    public static final String KSESSION_STRATEGY = "org.jboss.processFlow.cdi.ksession.strategy";
    
    @Inject
    @Vfs
    private DeploymentService deploymentService;
    
    private List<DeploymentUnit> units = new ArrayList<DeploymentUnit>();
    
    private String deploymentId = "general";
    private String vfsPath = "/tmp/bpms/processes/";
    private RuntimeStrategy ksessionStrategy = DeploymentUnit.RuntimeStrategy.PER_PROCESS_INSTANCE;
    
    private Logger log = LoggerFactory.getLogger("RESTApplicationStartup");
    
    @PostConstruct
    public void start() {
        deploymentId = System.getProperty(DEPLOYMENT_ID, deploymentId);
        if(deploymentService.getDeployedUnit(deploymentId) == null){
            vfsPath = System.getProperty(VFS_PATH, vfsPath);
            ksessionStrategy = RuntimeStrategy.valueOf(System.getProperty(this.KSESSION_STRATEGY, this.ksessionStrategy.toString()));
            StringBuilder sBuilder = new StringBuilder();
            sBuilder.append("start() creating deploymentUnit with \n\tdeploymentId = ");
            sBuilder.append(deploymentId);
            sBuilder.append("\n\tvfsPath = ");
            sBuilder.append(vfsPath);
            sBuilder.append("\n\tksessionStrategy = ");
            sBuilder.append(ksessionStrategy.toString());
            log.info(sBuilder.toString());
            VFSDeploymentUnit deploymentUnit = new VFSDeploymentUnit(deploymentId, "", vfsPath+deploymentId);
            deploymentUnit.setStrategy(ksessionStrategy);
            deploymentService.deploy(deploymentUnit);
            units.add(deploymentUnit);
        }else{
            log.info("start() deployedUnit with following deploymentId already exists : {}", this.deploymentId);
        }
    }
    
    @PreDestroy
    public void stop() {
        for (DeploymentUnit unit : units) {
            log.info("stop() about to stop following deployment unit : {}", unit.getIdentifier());
            deploymentService.undeploy(unit);
        }
    }
}