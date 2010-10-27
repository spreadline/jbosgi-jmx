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


import static org.jboss.osgi.jmx.JMXConstantsExt.DEFAULT_REMOTE_RMI_PORT;
import static org.jboss.osgi.jmx.JMXConstantsExt.DEFAULT_REMOTE_RMI_HOST;
import static org.jboss.osgi.jmx.JMXConstantsExt.REMOTE_RMI_PORT;
import static org.jboss.osgi.jmx.JMXConstantsExt.REMOTE_RMI_HOST;

import java.io.IOException;

import javax.management.MBeanServer;
import javax.management.remote.JMXServiceURL;

import org.jboss.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * A BundleActivator for JMX related services
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class JMXServiceActivator implements BundleActivator
{
   // Provide logging
   private static final Logger log = Logger.getLogger(JMXServiceActivator.class);

   private JMXConnectorService jmxConnector;
   private String rmiHost;
   private String rmiPortStr;
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

      rmiHost = context.getProperty(REMOTE_RMI_HOST);
      if (rmiHost == null)
         rmiHost = DEFAULT_REMOTE_RMI_HOST;

      rmiPortStr = context.getProperty(REMOTE_RMI_PORT);
      if (rmiPortStr == null)
         rmiPortStr = DEFAULT_REMOTE_RMI_PORT;

      int rmiPort = Integer.parseInt(rmiPortStr);

      // Start the JSR160 connector
      String urlString = "service:jmx:rmi://" + rmiHost + ":" + rmiPort + "/jmxrmi";
      try
      {
         JMXServiceURL serviceURL = new JMXServiceURL(urlString);
         jmxConnector = new JMXConnectorService(serviceURL, rmiPort);
         jmxConnector.start(mbeanServer);
      }
      catch (IOException ex)
      {
         log.error("Cannot start JMXConnectorServer", ex);
      }
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
}