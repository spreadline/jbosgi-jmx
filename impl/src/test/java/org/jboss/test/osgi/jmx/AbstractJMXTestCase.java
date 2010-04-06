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
import java.net.URL;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.osgi.jmx.FrameworkMBeanExt;
import org.jboss.osgi.jmx.MBeanProxy;
import org.jboss.osgi.jmx.ObjectNameFactory;
import org.jboss.osgi.jmx.ServiceStateMBeanExt;
import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.osgi.testing.OSGiTestHelper;
import org.junit.BeforeClass;
import org.osgi.jmx.framework.BundleStateMBean;

/**
 * An abstract JMX test case.
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Feb-2010
 */
public abstract class AbstractJMXTestCase extends OSGiFrameworkTest
{
   private MBeanServer server;
   
   @BeforeClass
   public static void setUpClass() throws Exception
   {
      String bundleName = System.getProperty("project.build.finalName");
      URL bundleURL = new File("target/" + bundleName + ".jar").toURI().toURL();
      systemContext.installBundle(bundleURL.toExternalForm());
   }

   protected FrameworkMBeanExt getFrameworkMBean() throws Exception
   {
      ObjectName oname = ObjectNameFactory.create(FrameworkMBeanExt.OBJECTNAME);
      return MBeanProxy.get(getMBeanServer(), oname, FrameworkMBeanExt.class);
   }
   
   protected BundleStateMBean getBundleStateMBean() throws Exception
   {
      ObjectName oname = ObjectNameFactory.create(BundleStateMBean.OBJECTNAME);
      return MBeanProxy.get(getMBeanServer(), oname, BundleStateMBean.class);
   }
   
   protected ServiceStateMBeanExt getServiceStateMBean() throws Exception
   {
      ObjectName oname = ObjectNameFactory.create(ServiceStateMBeanExt.OBJECTNAME);
      return MBeanProxy.get(getMBeanServer(), oname, ServiceStateMBeanExt.class);
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
}