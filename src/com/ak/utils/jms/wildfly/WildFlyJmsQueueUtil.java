package com.ak.utils.jms.wildfly;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * JMS Send Receive Utility base class .
 * Created by Andrew.Kozak on 2/21/2019.
 */
public abstract class WildFlyJmsQueueUtil
{
    protected final static String versionString = "ak: 2018-07-16";

    protected final static String JMS_CONNECTION_FACTORY_JNDI = "jms/RemoteConnectionFactory";
    protected final static String JMS_QUEUE_JNDI_PREFIX = "jms/queue/";

    protected final static String JMS_USERNAME = "jmsUser"; // The role for this user is "guest" in ApplicationRealm
    protected final static String JMS_PASSWORD = "drowssap";

    protected static String JMS_USERNAME_OVERRIDE = null;
    protected static String JMS_PASSWORD_OVERRIDE = null;

    protected final static String WILDFLY_REMOTING_URL = "http-remoting://localhost";

    protected static InitialContext getInitialContext(String hostname) throws NamingException
    {
        InitialContext context = null;
        try
        {
            String url = WILDFLY_REMOTING_URL.replace("localhost", hostname);

            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
            props.put(Context.PROVIDER_URL, url);   // NOTICE: "http-remoting" and port "8080"

            // Check for an optional credentials file (jms_credentials.propreties)
            Properties credProps = new Properties();
            String sCredentials = readFile("jms_credentials.properties");
            if (sCredentials != null)
            {
                try
                {
                    credProps.load(new FileInputStream("jms_credentials.properties"));

                    if (credProps.getProperty("jms_username") != null)
                    {
                        JMS_USERNAME_OVERRIDE = credProps.getProperty("jms_username").trim();
                        System.out.println("Override JMS User to: " + JMS_USERNAME_OVERRIDE);
                    }

                    if (credProps.getProperty("jms_password") != null)
                    {
                        JMS_PASSWORD_OVERRIDE = credProps.getProperty("jms_password").trim();
                        System.out.println("Override JMS User Password.");
                    }
                }
                catch (Exception e)
                {
                    System.out.println("\n\tFailed to load jms_credentials.properties. " + e.getMessage());
                    e.printStackTrace();
                }
            }

            props.put(Context.SECURITY_PRINCIPAL, JMS_USERNAME_OVERRIDE != null ? JMS_USERNAME_OVERRIDE : JMS_USERNAME);
            props.put(Context.SECURITY_CREDENTIALS, JMS_PASSWORD_OVERRIDE != null ? JMS_PASSWORD_OVERRIDE : JMS_PASSWORD);
            //props.put("jboss.naming.client.ejb.context", true);
            context = new InitialContext(props);
            System.out.println("\n\tGot initial Context: " + context);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return context;
    }

    protected static String readFile(String fn)
    {
        String contents;
        StringBuilder sb = new StringBuilder(4096);
        BufferedReader br = null;
        FileReader fr;
        try
        {
            fr = new FileReader(fn);
            br = new BufferedReader(fr);
            String s;
            while (null != (s = br.readLine()))
            {
                sb.append(s).append(System.getProperty("line.separator"));
            }
            contents = sb.toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            contents = null;
        }
        finally
        {
            if (null != br)
            {
                try
                {
                    br.close();
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        return contents;
    }
}
