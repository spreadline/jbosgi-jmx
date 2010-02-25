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

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.jboss.logging.Logger;
import org.jboss.osgi.jmx.MBeanProxy;
import org.jboss.osgi.jmx.ObjectNameFactory;
import org.osgi.framework.BundleContext;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * An extension to {@link BundleStateMBean}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Feb-2010
 */
abstract class AbstractState
{
   // Provide logging
   private static final Logger log = Logger.getLogger(AbstractState.class);
   
   protected MBeanServer mbeanServer;
   protected BundleContext context;

   AbstractState(BundleContext context, MBeanServer mbeanServer)
   {
      if (context == null)
         throw new IllegalArgumentException("Null BundleContext");
      if (mbeanServer == null)
         throw new IllegalArgumentException("Null MBeanServer");

      if (context.getBundle().getBundleId() != 0)
         throw new IllegalArgumentException("Not the system bundle context: " + context);

      this.context = context;
      this.mbeanServer = mbeanServer;
   }
   
   void start()
   {
      ObjectName objectName = getObjectName();
      try
      {
         log.debug("Register: " + objectName);
         mbeanServer.registerMBean(getStandardMBean(), objectName);
      }
      catch (JMException ex)
      {
         log.warn("Cannot register: " + objectName);
      }
   }

   void stop()
   {
      ObjectName objectName = getObjectName();
      try
      {
         if (mbeanServer.isRegistered(objectName))
         {
            log.debug("Unregister: " + objectName);
            mbeanServer.unregisterMBean(objectName);
         }
      }
      catch (JMException ex)
      {
         log.warn("Cannot unregister: " + objectName);
      }
   }

   abstract StandardMBean getStandardMBean() throws NotCompliantMBeanException;

   abstract ObjectName getObjectName();

   FrameworkMBean getFrameworkMBean()
   {
      ObjectName objectName = ObjectNameFactory.create(FrameworkMBean.OBJECTNAME);
      return MBeanProxy.get(mbeanServer, objectName, FrameworkMBean.class);
   }

   BundleStateMBean getBundleStateMBean()
   {
      ObjectName objectName = ObjectNameFactory.create(BundleStateMBean.OBJECTNAME);
      return MBeanProxy.get(mbeanServer, objectName, BundleStateMBean.class);
   }

   ServiceStateMBean getServiceStateMBean()
   {
      ObjectName objectName = ObjectNameFactory.create(ServiceStateMBean.OBJECTNAME);
      return MBeanProxy.get(mbeanServer, objectName, ServiceStateMBean.class);
   }
}