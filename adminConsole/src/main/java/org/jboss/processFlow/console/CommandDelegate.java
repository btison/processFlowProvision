/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.processFlow.console;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.drools.definition.process.Process;
import org.jboss.processFlow.bam.IBAMService;
import org.jboss.processFlow.knowledgeService.IKnowledgeSessionService;
import org.jbpm.process.audit.ProcessInstanceLog;


/**
 *  14 Nov 2011 :  what is the proper way to forward a user friendly message to the gwt javascript during exception handling ?
 *      -- will throw RuntimeExcetions until this is figured out
 */ 
public class CommandDelegate {

    private IKnowledgeSessionService ksessionProxy = null;
    private IBAMService bamProxy = null;

    public CommandDelegate() {
        Context ksessionContext = null;
        Context bamContext = null;
        try {
            Properties jndiProps = new Properties();
            jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.JndiLoginInitialContextFactory");
            jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
            jndiProps.put(Context.SECURITY_PRINCIPAL, "admin");
            jndiProps.put(Context.SECURITY_CREDENTIALS, "admin");
            jndiProps.put(Context.PROVIDER_URL, System.getProperty(IKnowledgeSessionService.KNOWLEDGE_SERVICE_PROVIDER_URL));
            ksessionContext = new InitialContext(jndiProps);
            ksessionProxy = (IKnowledgeSessionService)ksessionContext.lookup(IKnowledgeSessionService.KNOWLEDGE_SESSION_SERVICE_JNDI);

            jndiProps.put(Context.PROVIDER_URL, System.getProperty(IBAMService.BAM_SERVICE_PROVIDER_URL));
            bamContext = new InitialContext(jndiProps);
            bamProxy = (IBAMService)bamContext.lookup(IBAMService.BAM_SERVICE_JNDI);
        } catch(Exception x) {
            throw new RuntimeException(x);
        } finally {
            try {
                if(ksessionContext != null)
                    ksessionContext.close();
                if(bamContext != null)
                    bamContext.close();
            } catch(Exception y){
                throw new RuntimeException(y);
            }
        }
    }
    
    public List<Process> getProcesses() {
        try {
            return ksessionProxy.retrieveProcesses();
        } catch(RuntimeException x) {
            throw x;
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }
    
    public Process getProcess(String processId) {
        try {
            return ksessionProxy.getProcess(processId);
        } catch(RuntimeException x) {
            throw x;
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }
    
    public Process getProcessByName(String name) {
        try {
            return ksessionProxy.getProcessByName(name);
        } catch(RuntimeException x) {
            throw x;
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }

    public void removeProcess(String processId) {
        throw new UnsupportedOperationException();
    }

    public ProcessInstanceLog getProcessInstanceLog(String processId) {
        try {
            List<ProcessInstanceLog> pInstanceLogs = bamProxy.getProcessInstanceLogsByProcessId(processId);
            if(pInstanceLogs.size() > 1) {
                throw new RuntimeException("getProcessInstanceLog() following # of processInstances found for processId = "+processId+" : "+pInstanceLogs.size());
            }
            return pInstanceLogs.get(0);
        } catch(RuntimeException x) {
            throw x;
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * Get the process instance logs from history
     * 
     * @param processId
     * @return
     */
    public List<ProcessInstanceLog> getProcessInstanceLogsByProcessId(String processId) {
        try {
            return bamProxy.getProcessInstanceLogsByProcessId(processId);
        } catch(RuntimeException x) {
            throw x;
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * Get the active process instance logs at runtime 
     * @param processId
     * @return
     */
    public List<ProcessInstanceLog> getActiveProcessInstanceLogsByProcessId(String processId) {
        try {
            //XXX what if there are thousands of active process instances? pagination could solve the issue, but need change the API
            return bamProxy.getActiveProcessInstanceLogsByProcessId(processId);
        } catch(RuntimeException x) {
            throw x;
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }

    /*
     * startProcess
     *  jbpm5 documentation (secion 8.1) mentions :  
     *     "in most cases where information about the current execution state of process instances is required, the use of a history log is mostyl recommended"
     *  due to the async nature of populating the history log in processFlowProvision, will now query the jbpm5 core engine for the newly created process instance  
     */ 
    public ProcessInstanceLog startProcess(String processId, Map<String, Object> parameters) {
        try {
            Map<String, Object> returnMap = ksessionProxy.startProcessAndReturnId(processId, parameters);

            Long pInstanceId = (Long)returnMap.get(IKnowledgeSessionService.PROCESS_INSTANCE_ID);
            ProcessInstanceLog pInstanceLog = new ProcessInstanceLog(pInstanceId, processId);
            pInstanceLog.setStart(new Date());

            //return bamProxy.getProcessInstanceLog((Long)returnMap.get(IKnowledgeSessionService.PROCESS_INSTANCE_ID));
            return pInstanceLog;
        } catch(RuntimeException x) {
            throw x;
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }
    
    public void abortProcessInstance(String processInstanceId) {
        try {
            ksessionProxy.abortProcessInstance(Long.valueOf(processInstanceId), null);
        } catch(RuntimeException x) {
            throw x;
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }
    
    public Map<String, Object> getProcessInstanceVariables(String processInstanceId) {
        try {
            return ksessionProxy.getActiveProcessInstanceVariables(Long.valueOf(processInstanceId), null);
        } catch(RuntimeException x) {
            throw x;
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }
    /**
     * This method the variables provided in the map to the instance.
     * NOTE: the map will be added not replaced
     * @param processInstanceId
     * @param variables
     */
    public void setProcessInstanceVariables(String processInstanceId, Map<String, Object> variables) {
        try {
            ksessionProxy.setProcessInstanceVariables(Long.valueOf(processInstanceId), variables, null);
        } catch(RuntimeException x) {
            throw x;
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }
    
    public void signalExecution(String executionId, String signal) {
        throw new RuntimeException("signalExecution() method not yet implemented");
    }
}
