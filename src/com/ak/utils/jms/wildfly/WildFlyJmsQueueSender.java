package com.ak.utils.jms.wildfly;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Use jboss-client.jar
 * Implementation-Title: WildFly: EJB and JMS client combined jar
 * Implementation-Version: 10.1.0.Final
 *
 * @author tkwong
 */
public class WildFlyJmsQueueSender extends WildFlyJmsQueueUtil
{
    public final static String USAGE = "java -cp <classPath> meow.WildFlyJmsQueueSender"
            + "\n    <appMsgType> <fileName> [ <queueName> [ <hostname> [ <jmsCorrelationId> [ <appMsgVersion> [ <options>* ]]]]]"
            + "\n  <options> can be:"
            + "\n  filename for additional messages to send";

    private QueueConnectionFactory qconFactory;
    private QueueConnection qcon;
    private QueueSession qsession;
    private QueueSender qsender;
    private Queue queue;
    private TextMessage msg;

    public static void main(String[] args) throws Exception
    {
        System.out.println("versionString: " + versionString);

        String appMsgType = "Ping";
        String firstFileName = "ping.msg";
        String queueName = "AdminInQueue";
        String hostname = "localhost:8080";
        String JMSCorrelationId = null;
        String appMsgVersion = "null";
        List<String> fileNames = new ArrayList<>();

        int argsLength = args.length;
        if (0 < argsLength)
        {
            appMsgType = args[0].trim();
        }
        if (1 < argsLength)
        {
            firstFileName = args[1].trim();
        }
        if (2 < argsLength)
        {
            queueName = args[2].trim();
        }
        if (3 < argsLength)
        {
            hostname = args[3].trim();
        }
        if (4 < argsLength)
        {
            JMSCorrelationId = args[4].trim();
        }
        if (5 < argsLength)
        {
            appMsgVersion = args[5].trim();
        }
        fileNames.add(firstFileName);
        if (6 < argsLength)
        {
            for (int i = 6; i < argsLength; i++)
            {
                fileNames.add(args[i].trim());
            }
        }

        try
        {
            InitialContext ic = getInitialContext(hostname);
            WildFlyJmsQueueSender queueSender = new WildFlyJmsQueueSender();
            queueSender.init(ic, JMS_QUEUE_JNDI_PREFIX + queueName);

            for (String fileName : fileNames)
            {
                queueSender.send(appMsgType, fileName, JMSCorrelationId, appMsgVersion);
            }
            queueSender.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    protected void close() throws JMSException
    {
        qsender.close();
        qsession.close();
        qcon.close();
    }

    protected void send(String appMsgType, String fileName, String JMSCorrelationId, String appMsgVersion) throws JMSException
    {
        System.out.println("File: " + fileName + " : appMsgType: " + appMsgType);
        String body = readFile(fileName);
        msg.setStringProperty("appMsgType", appMsgType);
        msg.setStringProperty("appMsgVersion", appMsgVersion);
        msg.setText(body);

        if (JMSCorrelationId != null)
        {
            msg.setJMSCorrelationID(JMSCorrelationId);
        }

        qsender.send(msg);
        Date now = new Date();
        System.out.println("message sent at: " + now.toString());
    }

    protected void init(Context ctx, String queueName) throws NamingException, JMSException
    {
        System.out.println("init with queueName: " + queueName);
        qconFactory = (QueueConnectionFactory) ctx.lookup(JMS_CONNECTION_FACTORY_JNDI);

        //  If you won't pass jms credential here then you will get 
        // [javax.jms.JMSSecurityException: HQ119031: Unable to validate user: null]
        // TODO - Refactor and get from ctx
        qcon = qconFactory.createQueueConnection(JMS_USERNAME_OVERRIDE != null ? JMS_USERNAME_OVERRIDE : JMS_USERNAME,
                JMS_PASSWORD_OVERRIDE != null ? JMS_PASSWORD_OVERRIDE : JMS_PASSWORD);

        qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = (Queue) ctx.lookup(queueName);
        qsender = qsession.createSender(queue);
        msg = qsession.createTextMessage();
        qcon.start();
    }


}
