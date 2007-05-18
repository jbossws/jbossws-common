package org.jboss.wsintegration.spi.deployment;

import org.w3c.dom.Element;

public interface SecurityRolesHandler
{
   /** Add the roles from ejb-jar.xml to the security roles
    */
   void addSecurityRoles(Element webApp, UnifiedDeploymentInfo udi);
}
