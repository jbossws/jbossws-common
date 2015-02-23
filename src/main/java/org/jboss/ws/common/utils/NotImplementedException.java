package org.jboss.ws.common.utils;

public class NotImplementedException extends RuntimeException
{
   private static final long serialVersionUID = 1366726477562496595L;

   /**
    * Construct a <tt>NotImplementedException</tt> with a detail message.
    *
    * @param msg  Detail message.
    */
   public NotImplementedException(final String msg)
   {
      super(msg);
   }

   /**
    * Construct a <tt>NotImplementedException</tt> with no detail.
    */
   public NotImplementedException()
   {
      super();
   }
}