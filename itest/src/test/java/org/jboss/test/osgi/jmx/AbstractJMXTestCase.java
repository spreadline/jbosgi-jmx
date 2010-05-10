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
package org.jboss.test.osgi.jmx;

//$Id$

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.osgi.jmx.ObjectNameFactory;
import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * An abstract JMX test case.
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Feb-2010
 */
public abstract class AbstractJMXTestCase extends OSGiFrameworkTest
{
   // Provide logging
   private static final Logger log = Logger.getLogger(AbstractJMXTestCase.class);
   
   private MBeanServer server;
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      // Install/Start the jboss-osgi-jmx bundle
      String bundleName = "jboss-osgi-jmx-" + System.getProperty("project.version");
      URL bundleURL = new File("../bundle/target/" + bundleName + ".jar").toURI().toURL();
      
      Framework framework = createFramework();
      framework.start();
      
      BundleContext systemContext = framework.getBundleContext();
      Bundle bundle = systemContext.installBundle(bundleURL.toExternalForm());
      bundle.start();
   }

   @Before
   public void setUp() throws Exception
   {
      super.setUp();
      assertMBeanRegistration(true);
   }

   protected MBeanServer getMBeanServer()
   {
      if (server == null)
      {
         ArrayList<MBeanServer> serverArr = MBeanServerFactory.findMBeanServer(null);
         if (serverArr.size() > 1)
            throw new IllegalStateException("Multiple MBeanServer instances not supported");

         if (serverArr.size() == 1)
            server = serverArr.get(0);

         if (server == null)
            server = MBeanServerFactory.createMBeanServer();
      }
      return server;
   }

   private void assertMBeanRegistration(boolean state) throws IOException
   {
      log.debug("assertMBeanRegistration: " + state);

      MBeanServer server = (MBeanServer)getMBeanServer();
      ObjectName fwkName = ObjectNameFactory.create(FrameworkMBean.OBJECTNAME);
      ObjectName bndName = ObjectNameFactory.create(BundleStateMBean.OBJECTNAME);
      ObjectName srvName = ObjectNameFactory.create(ServiceStateMBean.OBJECTNAME);

      int timeout = 5000;
      while (0 < (timeout -= 200))
      {
         boolean fwkCheck = isMBeanRegistered(server, fwkName, state);
         boolean bndCheck = isMBeanRegistered(server, bndName, state);
         boolean srvCheck = isMBeanRegistered(server, srvName, state);
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

      if (isMBeanRegistered(server, fwkName, state) == false)
         log.warn("FrameworkMBean " + (state ? "not" : "still") + " registered");
      if (isMBeanRegistered(server, bndName, state) == false)
         log.warn("BundleStateMBean " + (state ? "not" : "still") + " registered");
      if (isMBeanRegistered(server, srvName, state) == false)
         log.warn("ServiceStateMBean " + (state ? "not" : "still") + " registered");
   }

   protected boolean isMBeanRegistered(MBeanServerConnection server, ObjectName oname, boolean state) throws IOException
   {
      boolean registered = server.isRegistered(oname);
      log.debug(oname + " registered: " + registered);
      return registered == state;
   }
}