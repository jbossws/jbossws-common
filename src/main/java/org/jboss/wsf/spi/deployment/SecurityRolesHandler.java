package org.jboss.wsf.spi.deployment;

import org.dom4j.Element;


public interface SecurityRolesHandler
{
   /** Add the roles from ejb-jar.xml to the security roles
    */
   void addSecurityRoles(Element webApp, UnifiedDeploymentInfo udi);
}
