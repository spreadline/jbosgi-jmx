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
package org.jboss.osgi.jmx;

//$Id$

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.capability.Capability;
import org.jboss.osgi.spi.capability.CompendiumCapability;
import org.jboss.osgi.testing.OSGiRuntime;
import org.osgi.framework.BundleException;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * Adds the JMX capability to the {@link OSGiRuntime}
 * under test. 
 * 
 * It is ignored if the {@link MBeanServer} is already registered.
 * 
 * Installed bundles: jboss-osgi-jmx.jar
 * 
 * Default properties set by this capability
 * 
 * <table>
 * <tr><th>Property</th><th>Value</th></tr> 
 * <tr><td>org.jboss.osgi.jmx.host</td><td>${jboss.bind.address}</td></tr> 
 * <tr><td>org.jboss.osgi.jmx.rmi.port</td><td>1198</td></tr> 
 * </table>
 *  
 * @author thomas.diesler@jboss.com
 * @since 05-May-2009
 */
public class JMXCapability extends Capability
{
   // Provide logging
   private static final Logger log = Logger.getLogger(JMXCapability.class);

   public JMXCapability()
   {
      super(MBeanServer.class.getName());

      addSystemProperty("org.jboss.osgi.jmx.host", System.getProperty("jboss.bind.address", "localhost"));
      addSystemProperty("org.jboss.osgi.jmx.rmi.port", "1198");

      addDependency(new CompendiumCapability());

      addBundle("bundles/jboss-osgi-jmx.jar");
      addBundle("bundles/org.apache.aries.jmx.jar");
   }

   @Override
   public void start(OSGiRuntime runtime) throws BundleException
   {
      // Explicitly create the MBeanServer, so we don't get into
      // a race condition with jboss-osgi-jmx also creating one
      runtime.getMBeanServer();

      super.start(runtime);
      assertMBeanRegistration(runtime, true);
   }

   @Override
   public void stop(OSGiRuntime runtime)
   {
      super.stop(runtime);
      assertMBeanRegistration(runtime, false);
   }

   private void assertMBeanRegistration(OSGiRuntime runtime, boolean state)
   {
      log.debug("assertMBeanRegistration: " + state);

      MBeanServer server = (MBeanServer)runtime.getMBeanServer();
      ObjectName fwkName = ObjectNameFactory.create(FrameworkMBean.OBJECTNAME);
      ObjectName bndName = ObjectNameFactory.create(BundleStateMBean.OBJECTNAME);
      ObjectName srvName = ObjectNameFactory.create(ServiceStateMBean.OBJECTNAME);

      int timeout = 5000;
      while (0 < (timeout -= 200))
      {
         boolean fwkCheck = checkMBean(server, fwkName, state);
         boolean bndCheck = checkMBean(server, bndName, state);
         boolean srvCheck = checkMBean(server, srvName, state);
         if (fwkCheck == true && bndCheck == true && srvCheck == true)
            break;
         
         try
         {
            Thread.sleep(200);
         }
         catch (InterruptedException e)
         {
            // ignore
         }
      }

      if (checkMBean(server, fwkName, state) == false)
         log.warn("FrameworkMBean " + (state ? "not" : "still") + " registered");
      if (checkMBean(server, bndName, state) == false)
         log.warn("BundleStateMBean " + (state ? "not" : "still") + " registered");
      if (checkMBean(server, srvName, state) == false)
         log.warn("ServiceStateMBean " + (state ? "not" : "still") + " registered");
   }

   protected boolean checkMBean(MBeanServer server, ObjectName oname, boolean state)
   {
      boolean registered = server.isRegistered(oname);
      log.debug(oname + " registered: " + registered);
      return registered == state;
   }
}