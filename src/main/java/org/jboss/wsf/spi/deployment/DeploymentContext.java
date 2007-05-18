package org.jboss.wsf.spi.deployment;

public interface DeploymentContext
{
   <T> T getAttachment(Class<T> clazz);

   <T> T addAttachment(Class<T> clazz, Object obj);
}