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

//$Id: ServiceStateTestCase.java 103562 2010-04-06 10:25:15Z thomas.diesler@jboss.com $

import static org.jboss.osgi.jmx.JMXConstantsExt.REMOTE_JMX_RMI_ADAPTOR_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.InitialContext;

import org.jboss.osgi.jmx.ObjectNameFactory;
import org.junit.Test;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * Test JSR160 remote connectivity.
 * 
 * @author thomas.diesler@jboss.com
 * @since 06-Apr-2010
 */
public class JSR160TestCase extends AbstractJMXTestCase
{
   @Test
   public void testJMXConnector() throws Exception
   {
      // The address of the connector server
      JMXServiceURL address = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:1098/jmxconnector");

      // The environment map, null in this case
      Map<String, ?> environment = null;

      // Create the JMXCconnectorServer
      JMXConnector cntor = JMXConnectorFactory.connect(address, environment);

      // Obtain a "stub" for the remote MBeanServer
      MBeanServerConnection mbsc = cntor.getMBeanServerConnection();

      // Call the remote MBeanServer
      String domain = mbsc.getDefaultDomain();
      assertEquals("DefaultDomain", domain);
      
      ObjectName fwkName = ObjectNameFactory.create(FrameworkMBean.OBJECTNAME);
      ObjectName bndName = ObjectNameFactory.create(BundleStateMBean.OBJECTNAME);
      ObjectName srvName = ObjectNameFactory.create(ServiceStateMBean.OBJECTNAME);

      assertTrue(isMBeanRegistered(mbsc, fwkName, true));
      assertTrue(isMBeanRegistered(mbsc, bndName, true));
      assertTrue(isMBeanRegistered(mbsc, srvName, true));
   }

   @Test
   public void testRMIAdaptor() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      MBeanServerConnection mbsc = (MBeanServerConnection)iniCtx.lookup(REMOTE_JMX_RMI_ADAPTOR_NAME);

      // Call the remote MBeanServer
      String domain = mbsc.getDefaultDomain();
      assertEquals("DefaultDomain", domain);
      
      ObjectName fwkName = ObjectNameFactory.create(FrameworkMBean.OBJECTNAME);
      ObjectName bndName = ObjectNameFactory.create(BundleStateMBean.OBJECTNAME);
      ObjectName srvName = ObjectNameFactory.create(ServiceStateMBean.OBJECTNAME);

      assertTrue(isMBeanRegistered(mbsc, fwkName, true));
      assertTrue(isMBeanRegistered(mbsc, bndName, true));
      assertTrue(isMBeanRegistered(mbsc, srvName, true));
   }
}