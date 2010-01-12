/**
 * 
 */
package org.jboss.osgi.jmx.internal;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class RMIAdaptor implements MBeanServerConnection, Serializable
{
   private static final long serialVersionUID = 6066226353118090215L;

   private MBeanServerConnection delegate;

   public RMIAdaptor(JMXServiceURL url) throws IOException
   {
      JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
      delegate = jmxc.getMBeanServerConnection();
   }

   public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
         throws InstanceNotFoundException, IOException
   {
      delegate.addNotificationListener(name, listener, filter, handback);
   }

   public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException,
         IOException
   {
      delegate.addNotificationListener(name, listener, filter, handback);
   }

   public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature) throws ReflectionException,
         InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException
   {
      return delegate.createMBean(className, name, params, signature);
   }

   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException,
         InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException
   {
      return delegate.createMBean(className, name, loaderName, params, signature);
   }

   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException,
         MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException
   {
      return delegate.createMBean(className, name, loaderName);
   }

   public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
         MBeanException, NotCompliantMBeanException, IOException
   {
      return delegate.createMBean(className, name);
   }

   public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException,
         ReflectionException, IOException
   {
      return delegate.getAttribute(name, attribute);
   }

   public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException, IOException
   {
      return delegate.getAttributes(name, attributes);
   }

   public String getDefaultDomain() throws IOException
   {
      return delegate.getDefaultDomain();
   }

   public String[] getDomains() throws IOException
   {
      return delegate.getDomains();
   }

   public Integer getMBeanCount() throws IOException
   {
      return delegate.getMBeanCount();
   }

   public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException
   {
      return delegate.getMBeanInfo(name);
   }

   public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException, IOException
   {
      return delegate.getObjectInstance(name);
   }

   public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException,
         ReflectionException, IOException
   {
      return delegate.invoke(name, operationName, params, signature);
   }

   public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException, IOException
   {
      return delegate.isInstanceOf(name, className);
   }

   public boolean isRegistered(ObjectName name) throws IOException
   {
      return delegate.isRegistered(name);
   }

   @SuppressWarnings({ "rawtypes" })
   public Set queryMBeans(ObjectName name, QueryExp query) throws IOException
   {
      return delegate.queryMBeans(name, query);
   }

   @SuppressWarnings({ "rawtypes" })
   public Set queryNames(ObjectName name, QueryExp query) throws IOException
   {
      return delegate.queryNames(name, query);
   }

   public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
         throws InstanceNotFoundException, ListenerNotFoundException, IOException
   {
      delegate.removeNotificationListener(name, listener, filter, handback);
   }

   public void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException,
         IOException
   {
      delegate.removeNotificationListener(name, listener);
   }

   public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException,
         ListenerNotFoundException, IOException
   {
      delegate.removeNotificationListener(name, listener, filter, handback);
   }

   public void removeNotificationListener(ObjectName name, ObjectName listener) throws InstanceNotFoundException, ListenerNotFoundException, IOException
   {
      delegate.removeNotificationListener(name, listener);
   }

   public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException,
         MBeanException, ReflectionException, IOException
   {
      delegate.setAttribute(name, attribute);
   }

   public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException, IOException
   {
      return delegate.setAttributes(name, attributes);
   }

   public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException, IOException
   {
      delegate.unregisterMBean(name);
   }
}