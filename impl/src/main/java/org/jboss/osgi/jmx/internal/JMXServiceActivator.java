/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.osgi.jmx.internal;

//$Id$

import static org.jboss.osgi.jmx.JMXConstantsExt.DEFAULT_JMX_RMI_ADAPTOR;
import static org.jboss.osgi.jmx.JMXConstantsExt.DEFAULT_REMOTE_JMX_HOST;
import static org.jboss.osgi.jmx.JMXConstantsExt.DEFAULT_REMOTE_JMX_RMI_PORT;
import static org.jboss.osgi.jmx.JMXConstantsExt.DEFAULT_REMOTE_JMX_RMI_REGISTRY_PORT;
import static org.jboss.osgi.jmx.JMXConstantsExt.REMOTE_JMX_HOST;
import static org.jboss.osgi.jmx.JMXConstantsExt.REMOTE_JMX_RMI_ADAPTOR;
import static org.jboss.osgi.jmx.JMXConstantsExt.REMOTE_JMX_RMI_PORT;
import static org.jboss.osgi.jmx.JMXConstantsExt.REMOTE_JMX_RMI_REGISTRY_PORT;

import java.io.IOException;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.logging.Logger;
import org.jboss.osgi.jmx.JMXServiceURLFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A BundleActivator for the MBeanServer related services
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class JMXServiceActivator implements BundleActivator
{
   // Provide logging
   private static final Logger log = Logger.getLogger(JMXServiceActivator.class);

   private JMXConnectorService jmxConnector;
   private String jmxHost;
   private String jmxRmiPort;
   private String rmiRegistryPort;
   private String rmiAdaptorName;
   private MBeanServer mbeanServer;
   private FrameworkStateExt frameworkState;
   private ServiceStateExt serviceState;
   private BundleStateExt bundleState;
   private PackageStateExt packageState;

   public void start(BundleContext context)
   {
      // Register the MBeanServer 
      MBeanServerService service = new MBeanServerService(context);
      mbeanServer = service.registerMBeanServer();

      // Get the system BundleContext
      BundleContext sysContext = context.getBundle(0).getBundleContext();

      // Register the FrameworkMBean
      frameworkState = new FrameworkStateExt(sysContext, mbeanServer);
      frameworkState.start();

      // Register the ServiceStateMBean 
      serviceState = new ServiceStateExt(sysContext, mbeanServer);
      serviceState.start();

      // Register the BundleStateMBean 
      bundleState = new BundleStateExt(sysContext, mbeanServer);
      bundleState.start();

      // Register the PackageStateMBean 
      packageState = new PackageStateExt(sysContext, mbeanServer);
      packageState.start();

      jmxHost = context.getProperty(REMOTE_JMX_HOST);
      if (jmxHost == null)
         jmxHost = DEFAULT_REMOTE_JMX_HOST;

      jmxRmiPort = context.getProperty(REMOTE_JMX_RMI_PORT);
      if (jmxRmiPort == null)
         jmxRmiPort = DEFAULT_REMOTE_JMX_RMI_PORT;

      rmiRegistryPort = context.getProperty(REMOTE_JMX_RMI_REGISTRY_PORT);
      if (rmiRegistryPort == null)
         rmiRegistryPort = DEFAULT_REMOTE_JMX_RMI_REGISTRY_PORT;

      rmiAdaptorName = context.getProperty(REMOTE_JMX_RMI_ADAPTOR);
      if (rmiAdaptorName == null)
         rmiAdaptorName = DEFAULT_JMX_RMI_ADAPTOR;

      // Start tracking the NamingService
      InitialContextTracker tracker = new InitialContextTracker(context);
      tracker.open();
   }

   public void stop(BundleContext context)
   {
      // Unregister the FrameworkMBean
      frameworkState.stop();

      // Unregister the ServiceStateMBean
      serviceState.stop();

      // Unregister the BundleStateMBean 
      bundleState.stop();

      // Unregister the PackageStateMBean 
      packageState.stop();

      if (jmxConnector != null)
      {
         jmxConnector.stop();
         jmxConnector = null;
      }
   }

   class InitialContextTracker extends ServiceTracker
   {
      private boolean rmiAdaptorBound;

      public InitialContextTracker(BundleContext context)
      {
         super(context, InitialContext.class.getName(), null);
      }

      @Override
      public Object addingService(ServiceReference reference)
      {
         InitialContext iniCtx = (InitialContext)super.addingService(reference);
         
         int conPort = Integer.parseInt(jmxRmiPort);
         int regPort = Integer.parseInt(rmiRegistryPort);
         JMXServiceURL serviceURL = JMXServiceURLFactory.getServiceURL(jmxHost, conPort, regPort);
         try
         {
            jmxConnector = new JMXConnectorService(serviceURL, regPort);
            jmxConnector.start(mbeanServer);
         }
         catch (IOException ex)
         {
            log.error("Cannot start JMXConnectorServer on: " + serviceURL, ex);
            return iniCtx;
         }

         // Bind the RMIAdaptor
         try
         {
            String[] tokens = rmiAdaptorName.split("/");
            Context ctx = iniCtx;
            for (int i = 0; i < tokens.length - 1; i++)
            {
               String token = tokens[i];
               ctx = ctx.createSubcontext(token);
            }
            StringRefAddr addr = new StringRefAddr(JMXServiceURL.class.getName(), serviceURL.toString());
            Reference ref = new Reference(MBeanServerConnection.class.getName(), addr, RMIAdaptorFactory.class.getName(), null);
            iniCtx.bind(rmiAdaptorName, ref);
            rmiAdaptorBound = true;

            log.debug("MBeanServerConnection bound to: " + rmiAdaptorName);
         }
         catch (NamingException ex)
         {
            log.error("Cannot bind RMIAdaptor", ex);
            return iniCtx;
         }
         
         return iniCtx;
      }

      @Override
      public void removedService(ServiceReference reference, Object service)
      {
         InitialContext iniCtx = (InitialContext)service;

         // Stop JMXConnectorService
         if (jmxConnector != null)
         {
            jmxConnector.stop();
            jmxConnector = null;
         }

         // Unbind the RMIAdaptor
         if (rmiAdaptorBound == true)
         {
            try
            {
               iniCtx.unbind(rmiAdaptorName);
               log.info("MBeanServerConnection unbound from: " + rmiAdaptorName);
            }
            catch (NamingException ex)
            {
               log.error("Cannot unbind RMIAdaptor", ex);
            }
         }

         super.removedService(reference, service);
      }
   }
}