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
package org.jboss.wsf.common;

// $Id$

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.HashSet;

import org.jboss.logging.Logger;

/** Java utilities
 *
 * @author Thomas.Diesler@jboss.org
 * @since 22-Dec-2004
 */
public class JavaUtils
{
   // provide logging
   private static final Logger log = Logger.getLogger(JavaUtils.class);

   private static HashMap<String, Class> primitiveNames = new HashMap<String, Class>(8);
   private static HashMap<String, String> primitiveNameDescriptors = new HashMap<String, String>(8);
   private static HashSet<String> reservedKeywords = new HashSet<String>(50);

   static
   {
      primitiveNames.put("int", int.class);
      primitiveNames.put("short", short.class);
      primitiveNames.put("boolean", boolean.class);
      primitiveNames.put("byte", byte.class);
      primitiveNames.put("long", long.class);
      primitiveNames.put("double", double.class);
      primitiveNames.put("float", float.class);
      primitiveNames.put("char", char.class);

      primitiveNameDescriptors.put("int", "I");
      primitiveNameDescriptors.put("short", "S");
      primitiveNameDescriptors.put("boolean", "Z");
      primitiveNameDescriptors.put("byte", "B");
      primitiveNameDescriptors.put("long", "J");
      primitiveNameDescriptors.put("double", "D");
      primitiveNameDescriptors.put("float", "F");
      primitiveNameDescriptors.put("char", "C");

      reservedKeywords.add("abstract");
      reservedKeywords.add("continue");
      reservedKeywords.add("for");
      reservedKeywords.add("new");
      reservedKeywords.add("switch");
      reservedKeywords.add("assert");
      reservedKeywords.add("default");
      reservedKeywords.add("if");
      reservedKeywords.add("package");
      reservedKeywords.add("synchronized");
      reservedKeywords.add("boolean");
      reservedKeywords.add("do");
      reservedKeywords.add("goto");
      reservedKeywords.add("private");
      reservedKeywords.add("this");
      reservedKeywords.add("break");
      reservedKeywords.add("double");
      reservedKeywords.add("implements");
      reservedKeywords.add("protected");
      reservedKeywords.add("throw");
      reservedKeywords.add("byte");
      reservedKeywords.add("else");
      reservedKeywords.add("import");
      reservedKeywords.add("public");
      reservedKeywords.add("throws");
      reservedKeywords.add("case");
      reservedKeywords.add("enum");
      reservedKeywords.add("instanceof");
      reservedKeywords.add("return");
      reservedKeywords.add("transient");
      reservedKeywords.add("catch");
      reservedKeywords.add("extends");
      reservedKeywords.add("int");
      reservedKeywords.add("short");
      reservedKeywords.add("try");
      reservedKeywords.add("char");
      reservedKeywords.add("final");
      reservedKeywords.add("interface");
      reservedKeywords.add("static");
      reservedKeywords.add("void");
      reservedKeywords.add("class");
      reservedKeywords.add("finally");
      reservedKeywords.add("long");
      reservedKeywords.add("strictfp");
      reservedKeywords.add("volatile");
      reservedKeywords.add("const");
      reservedKeywords.add("float");
      reservedKeywords.add("native");
      reservedKeywords.add("super");
      reservedKeywords.add("while");
   }

   /**
    * Load a Java type from a given class loader.
    *
    * @param typeName maybe the source notation of a primitve, class name, array of both
    */
   public static Class loadJavaType(String typeName) throws ClassNotFoundException
   {
      return loadJavaType(typeName, null);
   }

   /**
    * Load a Java type from a given class loader.
    *
    * @param typeName maybe the source notation of a primitve, class name, array of both
    */
   public static Class loadJavaType(String typeName, ClassLoader classLoader) throws ClassNotFoundException
   {
      if (classLoader == null)
         classLoader = Thread.currentThread().getContextClassLoader();

      Class javaType = primitiveNames.get(typeName);
      if (javaType == null)
         javaType = getArray(typeName, classLoader);

      if (javaType == null)
         javaType = classLoader.loadClass(typeName);

      return javaType;
   }

   /**
    * True if the given type name is the source notation of a primitive or array of which.
    */
   public static boolean isPrimitive(String javaType)
   {
      return getPrimitiveType(javaType) != null;
   }

   /**
    * True if the given class is a primitive or array of which.
    */
   public static boolean isPrimitive(Class javaType)
   {
      return javaType.isPrimitive() || (javaType.isArray() && isPrimitive(javaType.getComponentType()));
   }

   public static Class getPrimitiveType(String javaType)
   {
      Class type = primitiveNames.get(javaType);
      if (type != null)
         return type;

      try
      {
         // null loader = primitive only
         type = getArray(javaType, null);
      }
      catch (ClassNotFoundException e)
      {
         // This will actually never be thrown since is null
      }

      return type;
   }

   private static Class getArray(String javaType, ClassLoader loader) throws ClassNotFoundException
   {
      if (javaType.charAt(0) == '[')
         return getArrayFromJVMName(javaType, loader);

      if (javaType.endsWith("[]"))
         return getArrayFromSourceName(javaType, loader);

      return null;
   }

   private static Class getArrayFromJVMName(String javaType, ClassLoader loader) throws ClassNotFoundException
   {
      Class componentType;
      int componentStart = javaType.lastIndexOf('[') + 1;
      switch (javaType.charAt(componentStart))
      {
         case 'I': componentType = int.class; break;
         case 'S': componentType = short.class; break;
         case 'Z': componentType = boolean.class; break;
         case 'B': componentType = byte.class; break;
         case 'J': componentType = long.class; break;
         case 'D': componentType = double.class; break;
         case 'F': componentType = float.class; break;
         case 'C': componentType = char.class; break;
         case 'L':
            if (loader == null)
               return null;
            String name = javaType.substring(componentStart + 1, javaType.length() - 1);
            componentType = loader.loadClass(name);
            break;
         default:
            throw new IllegalArgumentException("Invalid binary component for array: " + javaType.charAt(componentStart));
      }

      // componentStart doubles as the number of '['s which is the number of dimensions
      return Array.newInstance(componentType, new int[componentStart]).getClass();
   }

   private static Class getArrayFromSourceName(String javaType, ClassLoader loader) throws ClassNotFoundException
   {
      int arrayStart = javaType.indexOf('[');
      String componentName = javaType.substring(0, arrayStart);

      Class componentType = primitiveNames.get(componentName);
      if (componentType == null)
      {
         if (loader == null)
            return null;

         componentType = loader.loadClass(componentName);
      }

      // [][][][] divided by 2
      int dimensions = (javaType.length() - arrayStart) >> 1;

      return Array.newInstance(componentType, new int[dimensions]).getClass();
   }

   /**
    * Get the corresponding primitive for a give wrapper type.
    * Also handles arrays of which.
    */
   public static Class getPrimitiveType(Class javaType)
   {
      if (javaType == Integer.class)
         return int.class;
      if (javaType == Short.class)
         return short.class;
      if (javaType == Boolean.class)
         return boolean.class;
      if (javaType == Byte.class)
         return byte.class;
      if (javaType == Long.class)
         return long.class;
      if (javaType == Double.class)
         return double.class;
      if (javaType == Float.class)
         return float.class;
      if (javaType == Character.class)
         return char.class;

      if (javaType == Integer[].class)
         return int[].class;
      if (javaType == Short[].class)
         return short[].class;
      if (javaType == Boolean[].class)
         return boolean[].class;
      if (javaType == Byte[].class)
         return byte[].class;
      if (javaType == Long[].class)
         return long[].class;
      if (javaType == Double[].class)
         return double[].class;
      if (javaType == Float[].class)
         return float[].class;
      if (javaType == Character[].class)
         return char[].class;

      if (javaType.isArray() && javaType.getComponentType().isArray())
      {
         Class compType = getPrimitiveType(javaType.getComponentType());
         return Array.newInstance(compType, 0).getClass();
      }

      return javaType;
   }

   /**
    * Converts an n-dimensional array of wrapper types to primitive types
    */
   public static Object getPrimitiveValueArray(Object value)
   {
      if (value == null)
         return null;

      Class javaType = value.getClass();
      if (javaType.isArray())
      {
         int length = Array.getLength(value);
         Object destArr = Array.newInstance(getPrimitiveType(javaType.getComponentType()), length);
         for (int i = 0; i < length; i++)
         {
            Object srcObj = Array.get(value, i);
            Array.set(destArr, i, getPrimitiveValueArray(srcObj));
         }
         return destArr;
      }

      return value;
   }

   /**
    * Get the corresponding wrapper type for a give primitive.
    * Also handles arrays of which.
    */
   public static Class getWrapperType(Class javaType)
   {
      if (javaType == int.class)
         return Integer.class;
      if (javaType == short.class)
         return Short.class;
      if (javaType == boolean.class)
         return Boolean.class;
      if (javaType == byte.class)
         return Byte.class;
      if (javaType == long.class)
         return Long.class;
      if (javaType == double.class)
         return Double.class;
      if (javaType == float.class)
         return Float.class;
      if (javaType == char.class)
         return Character.class;

      if (javaType == int[].class)
         return Integer[].class;
      if (javaType == short[].class)
         return Short[].class;
      if (javaType == boolean[].class)
         return Boolean[].class;
      if (javaType == byte[].class)
         return Byte[].class;
      if (javaType == long[].class)
         return Long[].class;
      if (javaType == double[].class)
         return Double[].class;
      if (javaType == float[].class)
         return Float[].class;
      if (javaType == char[].class)
         return Character[].class;

      if (javaType.isArray() && javaType.getComponentType().isArray())
      {
         Class compType = getWrapperType(javaType.getComponentType());
         return Array.newInstance(compType, 0).getClass();
      }

      return javaType;
   }

   /**
    * Converts an n-dimensional array of primitive types to wrapper types
    */
   public static Object getWrapperValueArray(Object value)
   {
      if (value == null)
         return null;

      Class javaType = value.getClass();
      if (javaType.isArray())
      {
         int length = Array.getLength(value);
         Object destArr = Array.newInstance(getWrapperType(javaType.getComponentType()), length);
         for (int i = 0; i < length; i++)
         {
            Object srcObj = Array.get(value, i);
            Array.set(destArr, i, getWrapperValueArray(srcObj));
         }
         return destArr;
      }

      return value;
   }

   public static Object syncArray(Object array, Class target)
   {
      return (JavaUtils.isPrimitive(target)) ? JavaUtils.getPrimitiveValueArray(array) : JavaUtils.getWrapperValueArray(array);
   }

   /**
    * Return true if the dest class is assignable from the src.
    * Also handles arrays and primitives.
    */
   public static boolean isAssignableFrom(Class dest, Class src)
   {
      if (dest == null)
         throw new IllegalArgumentException("Destination class cannot be null");
      if (src == null)
         throw new IllegalArgumentException("Source class cannot be null");

      boolean isAssignable = dest.isAssignableFrom(src);
      if (isAssignable == false && dest.getName().equals(src.getName()))
      {
         ClassLoader destLoader = dest.getClassLoader();
         ClassLoader srcLoader = src.getClassLoader();
         if(log.isDebugEnabled()) log.debug("Not assignable because of conflicting class loaders:\ndstLoader=" + destLoader + "\nsrcLoader=" + srcLoader);
      }

      if (isAssignable == false && isPrimitive(dest))
      {
         dest = getWrapperType(dest);
         isAssignable = dest.isAssignableFrom(src);
      }
      if (isAssignable == false && isPrimitive(src))
      {
         src = getWrapperType(src);
         isAssignable = dest.isAssignableFrom(src);
      }
      return isAssignable;
   }

   public static String convertJVMNameToSourceName(String typeName, ClassLoader loader)
   {
      // TODO Don't use a ClassLoader for this, we need to just convert it
      try
      {
         Class javaType = loadJavaType(typeName, loader);
         typeName = getSourceName(javaType);
      }
      catch (Exception e)
      {
      }

      return typeName;
   }

   /**
    * Converts a JVM external name to a JVM signature name. An external name is
    * that which is returned from {@link Class#getName()} A signature name is
    * the name in class file format.
    * <p>
    * For example:
    * <p>
    * [java.lang.Object
    * <p>
    * becomes:
    * <p>
    * [Ljava/lang/Object;
    *
    * @param externalName
    * @return
    */
   public static String toSignature(String externalName)
   {
      if (externalName == null)
         return null;

      String ret = primitiveNameDescriptors.get(externalName);
      if (ret != null)
         return ret;

      ret = externalName.replace('.', '/');
      return (ret.charAt(0) == '[') ? ret : "L" + ret + ";";
   }

   public static String printArray(Object[] val)
   {
      if (val == null)
         return "null";

      StringBuilder out = new StringBuilder("[");
      for (int i = 0; i < val.length; i++)
      {
         if (i > 0)
         {
            out.append(",");
         }
         out.append(val[i].getClass().isArray() ? printArray((Object[])val[i]) : val[i]);
      }
      return out.append("]").toString();
   }

   public static String getSourceName(Class type)
   {
      if (! type.isArray())
         return type.getName();

      String arrayNotation = "";
      Class component = type;
      while(component.isArray())
      {
         component = component.getComponentType();
         arrayNotation += "[]";
      }

      return component.getName() + arrayNotation;
   }

   public static String capitalize(String source)
   {
      if (source == null)
         return null;

      if (source.length() == 0)
         return source;

      if (Character.isUpperCase(source.charAt(0)))
         return source;

      char c = Character.toUpperCase(source.charAt(0));

      return c + source.substring(1);
   }

   public static boolean isLoaded(String className, ClassLoader loader)
   {
      try
      {
         loadJavaType(className, loader);
      }
      catch (ClassNotFoundException e)
      {
         return false;
      }

      return true;
   }

   public static String getPackageName(Class<?> clazz)
   {
      String fullName = clazz.getName();
      return fullName.substring(0, fullName.lastIndexOf("."));
   }

   public static boolean isReservedKeyword(String keyword)
   {
      return reservedKeywords.contains(keyword);
   }

   /**
    * Erases a type according to the JLS type erasure rules
    *
    * @param t type to erase
    * @return erased type
    */
   public static Class erasure(Type type)
   {
      if (type instanceof ParameterizedType)
      {
         return erasure(((ParameterizedType)type).getRawType());
      }
      if (type instanceof TypeVariable)
      {
         return erasure(((TypeVariable)type).getBounds()[0]);
      }
      if (type instanceof WildcardType)
      {
         return erasure(((WildcardType)type).getUpperBounds()[0]);
      }
      if (type instanceof GenericArrayType)
      {
         return Array.newInstance(erasure(((GenericArrayType)type).getGenericComponentType()), 0).getClass();
      }

      // Only type left is class
      return (Class)type;
   }

   public static String[] getRawParameterTypeArguments(ParameterizedType type)
   {
      Type[] arguments = type.getActualTypeArguments();
      String[] ret = new String[arguments.length];
      for (int i = 0; i < arguments.length; i++)
      {
         Class raw = erasure(arguments[i]);
         ret[i] = raw.getName();
      }

      return ret;
   }

   /**
    * This method tests for retro translation by searching for a known problem where Class
    * does not implement Type. If this is true, then code must never cast a Class to a Type.
    *
    * @return true if we are in retro
    */
   public static boolean isRetro14()
   {
      return !(String.class instanceof java.lang.reflect.Type);
   }

   /**
    * Tests if this class loader is a JBoss RepositoryClassLoader
    *
    * @param loader
    * @return
    */
   public static boolean isJBossRepositoryClassLoader(ClassLoader loader)
   {
      Class clazz = loader.getClass();
      while (!clazz.getName().startsWith("java"))
      {
         if ("org.jboss.mx.loading.RepositoryClassLoader".equals(clazz.getName()))
            return true;
         clazz = clazz.getSuperclass();
      }

      return false;
   }

   /**
    * Clears black lists on a JBoss RepositoryClassLoader. This is somewhat of a hack, and
    * could be replaced with an integration module. This is needed when the following order of
    * events occur.
    *
    * <ol>
    *   <li>loadClass() returns not found</li>
    *   <li>Some call to defineClass()</li>
    * <ol>
    *
    * The CNFE triggers a black list addition, which cause the class never again to be found.
    *
    * @param loader the loader to clear black lists for
    */
   public static void clearBlacklists(ClassLoader loader)
   {
      if (isJBossRepositoryClassLoader(loader))
      {
			for(Method m : loader.getClass().getMethods())
			{
				if("clearBlackLists".equalsIgnoreCase(m.getName()))
				{
					try
					{
						m.invoke(loader);
					}
					catch (Exception e)
					{
						if(log.isDebugEnabled()) log.debug("Could not clear blacklists on " + loader);
					}
				}
			}			
      }
   }
}