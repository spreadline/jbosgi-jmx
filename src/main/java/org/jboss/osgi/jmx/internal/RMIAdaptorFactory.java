/**
 * 
 */
package org.jboss.osgi.jmx.internal;

// $Id$

import java.io.Serializable;
import java.util.Hashtable;

import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public class RMIAdaptorFactory implements ObjectFactory, Serializable
{
   private static final long serialVersionUID = 2560477127430087074L;

   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
   {
      Reference ref = (Reference)obj;
      RefAddr refAddr = ref.get(JMXServiceURL.class.getName());
      String serviceURL = (String)refAddr.getContent();
      JMXServiceURL url = new JMXServiceURL(serviceURL);
      return new RMIAdaptor(url);
   }
}