/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ws.common.sort;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.jboss.ws.api.util.BundleUtils;
import org.jboss.wsf.spi.deployment.DeploymentAspect;

/**
 * Implements <a href="http://en.wikipedia.org/wiki/Topological_sorting">topological sorting</a> for acyclic graphs.
 * The algorithm complexity is <b>O(m+n)</b>, where <b>m</b> is count of vertices and <b>n</b> is count of edges.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class DeploymentAspectSorter 
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(DeploymentAspectSorter.class);
    
   private static final DeploymentAspectSorter INSTANCE = new DeploymentAspectSorter();

   private DeploymentAspectSorter()
   {
       // forbidden inheritance
   }
   
   public static DeploymentAspectSorter getInstance()
   {
      return INSTANCE;
   }
    
   public List<DeploymentAspect> sort(final List<DeploymentAspect> aspects)
   {
      final DeploymentAspect lastAspect = getLastAspect(aspects);
      final List<DeploymentAspect> sortedAspects = createOrientedGraph(aspects).sort();
      sortedAspects.add(lastAspect);
      return sortedAspects;
   }
   
   private DeploymentAspect getLastAspect(final List<DeploymentAspect> aspects)
   {
      final Iterator<DeploymentAspect> i = aspects.iterator();
      DeploymentAspect aspect;
      while (i.hasNext())
      {
         aspect = i.next();
         if (aspect.isLast())
         {
            i.remove();
            return aspect;
         }
      }
      
      throw new IllegalStateException(BundleUtils.getMessage(bundle, "NO_DEPLOYMENT_ASPECT_FOUND"));
   }

   private Graph createOrientedGraph(final List<DeploymentAspect> aspects)
   {
      final Graph graph = new Graph();

      for (final DeploymentAspect aspect : aspects)
      {
         graph.addVertex(aspect);
      }

      graph.createEdges();

      return graph;
   }

   private static class Graph
   {
      private Map<String, Dependency> dependencies = new HashMap<String, Dependency>();
      private Set<Vertex> vertices = new HashSet<Vertex>();

      public void addVertex(final DeploymentAspect aspect)
      {
         // create disjunct sets
         final Set<String> inputs = new HashSet<String>();
         inputs.addAll(aspect.getRequiresAsSet());
         final Set<String> outputs = new HashSet<String>();
         outputs.addAll(aspect.getProvidesAsSet());
         final Set<String> intersection = this.getIntersection(inputs, outputs);

         // register vertex
         final Vertex vertex = new Vertex(aspect);
         this.vertices.add(vertex);

         // register dependencies
         Dependency dependency;
         for (final String in : inputs)
         {
            dependency = this.getDependency(in);
            dependency.consumers.add(vertex);
         }

         for (final String inOut : intersection)
         {
            dependency = this.getDependency(inOut);
            dependency.modifiers.add(vertex);
         }

         for (final String out : outputs)
         {
            dependency = this.getDependency(out);
            dependency.producers.add(vertex);
         }
      }

      public List<DeploymentAspect> sort()
      {
         // L ← Empty list that will contain the sorted elements
         List<DeploymentAspect> retVal = new LinkedList<DeploymentAspect>();
         // S ← Set of all nodes with no incoming edges
         List<Vertex> roots = this.getRoots();

         // while S is non-empty do
         Vertex root;
         List<Vertex> nextLevel;
         while(!roots.isEmpty())
         {
            // remove a node n from S
            root = roots.remove(0);
            // insert n into L
            retVal.add(root.getAspect());

            // for each node m with an edge e from n to m do
            if (root.hasConsumers())
            {
               nextLevel = new LinkedList<Vertex>();
               for(final Vertex consumer : root.consumers)
               {
                  // remove edge e from the graph
                  consumer.decrementDegree();
                  // if m has no other incoming edges then insert m into S
                  if (!consumer.hasProducers())
                  {
                     this.remove(consumer);
                     nextLevel.add(consumer);
                  }
               }

               // append to the end of list in sorted order
               roots.addAll(nextLevel);
            }
         }

         if (this.vertices.size() > 0)
         {
            // if graph has edges then graph has at least one cycle
            throw new IllegalStateException(BundleUtils.getMessage(bundle, "CYCLE_DETECTED_IN_SUBGRAPH",  this.vertices));
         }
         else
         {
            // topologically sorted order
            return retVal;
         }
      }

      private Set<String> getIntersection(final Set<String> inputs, final Set<String> outputs)
      {
         final Set<String> intersection = new HashSet<String>();

         for (final String input : inputs)
            for (final String output : outputs)
               if (input.equals(output)) 
                  intersection.add(input);

         inputs.removeAll(intersection);
         outputs.removeAll(intersection);

         return intersection;
      }

      private Dependency getDependency(final String name)
      {
         if (this.dependencies.containsKey(name))
         {
            return this.dependencies.get(name);
         }
         else
         {
            final Dependency newDependency = new Dependency();
            this.dependencies.put(name, newDependency);
            return newDependency;
         }
      }

      private void createEdges()
      {
         Dependency dependency;
         boolean hasModifiers;

         for (final String dependencyName : this.dependencies.keySet())
         {
            dependency = this.dependencies.get(dependencyName);
            hasModifiers = dependency.modifiers.size() > 0;

            if (hasModifiers)
            {
               this.createEdges(dependency.producers, dependency.modifiers);
               this.createEdges(dependency.modifiers, dependency.consumers);
            }
            else
            {
               this.createEdges(dependency.producers, dependency.consumers);
            }
         }
      }

      private void createEdges(final List<Vertex> producers, final List<Vertex> consumers)
      {
         for (final Vertex producer : producers)
            for (final Vertex consumer : consumers)
            {
               producer.addConsumer(consumer);
               consumer.incrementDegree();
            }
      }

      private List<Vertex> getRoots()
      {
         final List<Vertex> retVal = new LinkedList<Vertex>();

         Vertex current;
         for (final Iterator<Vertex> i = this.vertices.iterator(); i.hasNext(); )
         {
            current = i.next();
            if (!current.hasProducers())
            {
               retVal.add(current);
               i.remove();
            }
         }

         return retVal;
      }

      private void remove(final Vertex v)
      {
         this.vertices.remove(v);
      }

      private static class Vertex
      {
         // Wrapped aspect
         private DeploymentAspect aspect;
         // Incoming edges
         private int inDegree;
         // Outgoing edges
         private List<Vertex> consumers = new LinkedList<Vertex>();

         public Vertex(final DeploymentAspect aspect)
         {
            this.aspect = aspect;
         }

         public void incrementDegree()
         {
            this.inDegree++;
         }

         public void decrementDegree()
         {
            this.inDegree--;
         }

         public boolean hasProducers()
         {
            return this.inDegree > 0;
         }

         public void addConsumer(final Vertex v)
         {
            this.consumers.add(v);
         }

         public boolean hasConsumers()
         {
            return this.consumers.size() > 0;
         }

         public DeploymentAspect getAspect()
         {
            return this.aspect;
         }

         public String toString()
         {
            return this.aspect.toString();
         }
      }

      private static class Dependency
      {
         // aspects creating this dependency
         private List<Vertex> producers = new LinkedList<Vertex>();
         // aspects modifying this dependency
         private List<Vertex> modifiers = new LinkedList<Vertex>();
         // aspects consuming this dependency
         private List<Vertex> consumers = new LinkedList<Vertex>();
      }

   }

}
