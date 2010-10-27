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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.management.MBeanServer;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.jboss.osgi.jmx.ServiceStateMBeanExt;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.JmxConstants;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * A test that excercises the ServiceStateMBean
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Feb-2010
 */
public class ServiceStateTestCase extends AbstractJMXTestCase
{
   @Test
   public void listServices() throws Exception
   {
      BundleContext systemContext = getFramework().getBundleContext();
      ServiceReference[] srefs = systemContext.getServiceReferences(null, null);
      
      ServiceStateMBean serviceState = getServiceStateMBean();
      TabularData data = serviceState.listServices();
      assertEquals("Number of services", srefs.length, data.size());
   }

   @Test
   public void getService() throws Exception
   {
      ServiceStateMBeanExt serviceState = (ServiceStateMBeanExt)getServiceStateMBean();
      CompositeData serviceData = serviceState.getService(MBeanServer.class.getName());
      assertNotNull("MBeanServer service not null", serviceData);
      
      Long serviceId = (Long)serviceData.get(ServiceStateMBean.IDENTIFIER);
      assertNotNull("service.id not null", serviceId);
      
      TabularData props = serviceState.getProperties(serviceId);
      assertNotNull("Properties not null", props);
      
      CompositeData idData = props.get(new Object[] { Constants.SERVICE_ID });
      assertEquals(serviceId.toString(), idData.get(JmxConstants.VALUE));
   }

   @Test
   public void getServices() throws Exception
   {
      BundleContext systemContext = getFramework().getBundleContext();
      ServiceReference sref = systemContext.getServiceReference(MBeanServer.class.getName());
      Long serviceID = (Long)sref.getProperty(Constants.SERVICE_ID);
      
      ServiceStateMBeanExt serviceState = (ServiceStateMBeanExt)getServiceStateMBean();
      TabularData data = serviceState.getServices(null, "(service.id=" + serviceID + ")");
      assertEquals("MBeanServer service not null", 1, data.size());
   }
}