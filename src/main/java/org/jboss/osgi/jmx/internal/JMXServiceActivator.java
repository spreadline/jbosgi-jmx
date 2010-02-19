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

import static org.jboss.osgi.jmx.Constants.REMOTE_JMX_HOST;
import static org.jboss.osgi.jmx.Constants.REMOTE_JMX_RMI_ADAPTOR;
import static org.jboss.osgi.jmx.Constants.REMOTE_JMX_RMI_PORT;

import java.io.IOException;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXServiceURL;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.management.ManagedFramework;
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
   private String rmiAdaptorPath;
   private MBeanServer mbeanServer;
   private ManagedFrameworkImpl managedFramework;

   public void start(BundleContext context)
   {
      // Register the MBeanServer 
      MBeanServerService service = new MBeanServerService(context);
      mbeanServer = service.registerMBeanServer();

      // Get the system BundleContext
      BundleContext sysContext = context.getBundle(0).getBundleContext();

      // Register the ManagedFramework 
      managedFramework = new ManagedFrameworkImpl(sysContext, mbeanServer);
      context.registerService(ManagedFramework.class.getName(), managedFramework, null);
      managedFramework.start();

      jmxHost = context.getProperty(REMOTE_JMX_HOST);
      if (jmxHost == null)
         jmxHost = "localhost";

      jmxRmiPort = context.getProperty(REMOTE_JMX_RMI_PORT);
      if (jmxRmiPort == null)
         jmxRmiPort = "1098";

      rmiAdaptorPath = context.getProperty(REMOTE_JMX_RMI_ADAPTOR);
      if (rmiAdaptorPath == null)
         rmiAdaptorPath = "jmx/invoker/RMIAdaptor";

      // Start tracking the NamingService
      InitialContextTracker tracker = new InitialContextTracker(context, rmiAdaptorPath);
      tracker.open();
   }

   public void stop(BundleContext context)
   {
      // Unregister the managed framework
      managedFramework.stop();

      if (jmxConnector != null)
      {
         jmxConnector.stop();
         jmxConnector = null;
      }
   }

   class InitialContextTracker extends ServiceTracker
   {
      private String rmiAdaptorPath;
      private boolean rmiAdaptorBound;

      public InitialContextTracker(BundleContext context, String rmiAdaptorPath)
      {
         super(context, InitialContext.class.getName(), null);
         this.rmiAdaptorPath = rmiAdaptorPath;
      }

      @Override
      public Object addingService(ServiceReference reference)
      {
         InitialContext iniCtx = (InitialContext)super.addingService(reference);

         JMXServiceURL serviceURL = JMXConnectorService.getServiceURL(jmxHost, Integer.parseInt(jmxRmiPort));
         try
         {
            // Try to start the JMXConnector, this should fail if it is already running
            // [TODO] is there a better way to check whether the connector is already running?
            jmxConnector = new JMXConnectorService(context, mbeanServer, jmxHost, Integer.parseInt(jmxRmiPort));
            jmxConnector.start();
         }
         catch (IOException ex)
         {
            // Assume that the JMXConnector is already running if we cannot start it 
            log.debug("Assume JMXConnectorServer already running on: " + serviceURL);
         }

         try
         {
            // Check if the RMIAdaptor is already bound
            iniCtx.lookup(rmiAdaptorPath);
         }
         catch (NamingException lookupEx)
         {
            // Bind the RMIAdaptor
            try
            {
               iniCtx.createSubcontext("jmx").createSubcontext("invoker");
               StringRefAddr addr = new StringRefAddr(JMXServiceURL.class.getName(), serviceURL.toString());
               Reference ref = new Reference(MBeanServerConnection.class.getName(), addr, RMIAdaptorFactory.class.getName(), null);
               iniCtx.bind(rmiAdaptorPath, ref);
               rmiAdaptorBound = true;

               log.info("MBeanServerConnection bound to: " + rmiAdaptorPath);
            }
            catch (NamingException ex)
            {
               log.error("Cannot bind RMIAdaptor", ex);
            }
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
               iniCtx.unbind(rmiAdaptorPath);
               log.info("MBeanServerConnection unbound from: " + rmiAdaptorPath);
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