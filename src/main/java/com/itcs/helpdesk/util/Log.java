package com.itcs.helpdesk.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.apache.log4j.Logger;
//import org.apache.log4j.Priority;

/**
 *
 * @author danilomoya
 */
public class Log
{

    private static Map<String, Log> logMap;
    private final Logger logger;

    private Log(String className)
    {
        logger = Logger.getLogger(className);
    }
    
    public void log(Level level, String string, Throwable thrwbl)
    {
        logger.log(level, string, thrwbl);
//        logger.log(levelToPriority(level), string, thrwbl);
    }
    
//    private Priority levelToPriority(Level level)
//    {
//        if(level.equals(Level.ALL))
//            return Priority.INFO;
//        if(level.equals(Level.CONFIG))
//            return Priority.INFO;
//        if(level.equals(Level.FINE))
//            return Priority.INFO;
//        if(level.equals(Level.FINER))
//            return Priority.INFO;
//        if(level.equals(Level.FINEST))
//            return Priority.INFO;
//        if(level.equals(Level.INFO))
//            return Priority.INFO;
//        if(level.equals(Level.OFF))
//            return Priority.FATAL;
//        if(level.equals(Level.SEVERE))
//            return Priority.ERROR;
//        if(level.equals(Level.WARNING))
//            return Priority.DEBUG;
//        return Priority.DEBUG;
//    }

    /**
     * @return the logMap
     */
    public static synchronized Map<String, Log> getLogMap()
    {
        if(logMap == null)
        {
            logMap = new ConcurrentHashMap<String, Log>();
        }
        return logMap;
    }

    public static Log createLogger(String className)
    {
        Log logger = getLogMap().get(className);
        if(logger == null)
        {
            logger = new Log(className);
            getLogMap().put(className, logger);
        }
        return logger;
    }

    public void logSevere(Object mensaje)
    {
        if(mensaje != null){
             logger.severe(mensaje.toString());
        }else{
             logger.severe("null");
        }
       

        //logger.error(mensaje);
    }

    public void logInfo(Object mensaje)
    {
        logger.info(mensaje.toString());
        //logger.info(mensaje);
    }
    
      public void logDebug(Object mensaje)
    {
        logger.log(Level.CONFIG, mensaje.toString());
        //logger.info(mensaje);
    }

    public void logWarning(Object mensaje)
    {
        logger.severe(mensaje.toString());
        //logger.warn(mensaje);
    }
}
