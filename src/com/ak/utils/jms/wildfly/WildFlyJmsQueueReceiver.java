package com.ak.utils.jms.wildfly;

import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import java.util.Date;
import java.util.Enumeration;


/**
 * @author akozak
 */
public class WildFlyJmsQueueReceiver extends WildFlyJmsQueueUtil
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("versionString: " + versionString);

        InitialContext initialContext = null;
        QueueBrowser queueBrowser = null;
        QueueReceiver queueReceiver = null;
        QueueSession queueSession = null;
        QueueConnection queueConnection = null;
        try
        {
            int limit = 1;
            boolean isGet = false;
            String queueName = "ShipOutQueue";
            String hostname = "localhost";

            for (int i = 0; i < args.length; i++)
            {
                String arg = args[i];
                arg = arg.trim();
                int argLen = arg.length();
                String cmd = "-b";
                String nbr = "1";

                if (2 <= argLen)
                {
                    if ('-' != arg.charAt(0))
                    {
                        queueName = arg;
                    }
                    else if ("-b".equals(arg.substring(0, 2)))
                    {
                        isGet = false;
                        cmd = "-b";
                        if (2 < argLen)
                        {
                            nbr = arg.substring(2);
                            limit = Integer.valueOf(nbr);
                        }
                    }
                    else if ("-h".equals(arg.substring(0, 2)))
                    {
                        if (2 < argLen)
                        {
                            hostname = arg.substring(2);
                        }
                    }
                    else if ("-r".equals(arg.substring(0, 2)))
                    {
                        isGet = true;
                        cmd = "-r";
                        if (2 < argLen)
                        {
                            nbr = arg.substring(2);
                            limit = Integer.valueOf(nbr);
                        }
                    }
                    else
                    {
                        queueName = arg;
                    }
                }
                else
                {
                    queueName = arg;
                }

//                System.out.println("i: " + i);
//                System.out.println("arg: " + arg);
//                System.out.println("cmd: " + cmd);
//                System.out.println("nbr: " + nbr);
//                System.out.println("hostname: " + hostname);
//                System.out.println("queueName: " + queueName);
//                System.out.println("limit: " + limit);
//                System.out.println("isGet: " + isGet);
            }
            System.out.println("hostname: " + hostname);
            System.out.println("queueName: " + queueName);
            System.out.println("limit: " + limit);
            System.out.println("isGet: " + isGet);

            // Step 1. Create an initial context to perform the JNDI lookup.
            initialContext = getInitialContext(hostname);

            // Step 2. Perfom a lookup on the queue
            Queue queue = (Queue) initialContext.lookup(JMS_QUEUE_JNDI_PREFIX + queueName);

            // Step 3. Perform a lookup on the Connection Factory
            //          you could alternatively instantiate the connection directly
            //          ConnectionFactory cf = new ActiveMQConnectionFactory(); // this would accept the broker URI as well
            QueueConnectionFactory qcf = (QueueConnectionFactory) initialContext.lookup(JMS_CONNECTION_FACTORY_JNDI);
            //ConnectionFactory cf = (ConnectionFactory) initialContext.lookup(JMS_CONNECTION_FACTORY_JNDI);

            // Step 4. Create a JMS Connection
            queueConnection = qcf.createQueueConnection(
                    JMS_USERNAME_OVERRIDE != null ? JMS_USERNAME_OVERRIDE : JMS_USERNAME,
                    JMS_PASSWORD_OVERRIDE != null ? JMS_PASSWORD_OVERRIDE : JMS_PASSWORD);
            //connection = qcf.createConnection();

            // Step 5. Create a JMS Session
            queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            //Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Step 6.  Create a Browser or Consumer to retrieve messages
            if (isGet)
            {
                System.out.println("JMS Ready To Receive Messages ...");

                // Step A. Create the JMS QueueReceiver
                queueReceiver = queueSession.createReceiver(queue);

                System.out.println("queue: " + queue.toString());
                System.out.println("queueReceiver: " + queueReceiver.toString());

                // Step B. Start the Connection
                queueConnection.start();

                // Step B. Read the messages on the queue
                // Reading a queue will remove read messages from the queue
                for (int cnt = 1; cnt <= limit; cnt++)
                {
                    long timeoutMillseconds = 1000;
                    System.out.println("... about to receive a message (timeoutMillseconds = " + timeoutMillseconds + ") ...");
                    //System.out.println("... about to receive a message (blocking) ...");
                    //Object obj = (Object) queueReceiver.receive();
                    Object obj = queueReceiver.receive(timeoutMillseconds);
                    System.out.println("obj is " + ((null != obj) ? "not" : "") + " null");
                    //TextMessage textMessage = (TextMessage) queueReceiver.receive(timeoutMillseconds);
                    //TextMessage textMessage = (TextMessage) queueReceiver.receiveNoWait();
                    if (null != obj)
                    {
                        System.out.println("obj.class: " + obj.getClass().getName());
                        TextMessage textMessage = (TextMessage) obj;
                        printMessage("Receive", cnt, textMessage);
                    }
                    else
                    {
                        System.out.println("... null received, no more messages ...");
                        break;
                    }
                }
                Date now = new Date();
                System.out.println("done browsing at: " + now.toString());
            }
            else
            {
                System.out.println("JMS Ready To Browse Messages");
                // Step A. Create the JMS QueueBrowser
                queueBrowser = queueSession.createBrowser(queue);

                System.out.println("queue: " + queue.toString());
                System.out.println("queueBrowser: " + queueBrowser.toString());
                // Step B. Browse the messages on the queue
                // Browsing a queue does not remove the messages from the queue
                Enumeration messageEnum = queueBrowser.getEnumeration();
                int cnt = 0;
                while ((cnt < limit) && messageEnum.hasMoreElements())
                {
                    cnt += 1;
                    TextMessage textMessage = (TextMessage) messageEnum.nextElement();
                    printMessage("Browse", cnt, textMessage);
                }
                Date now = new Date();
                System.out.println("done browsing at: " + now.toString());
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            // Step 15. Be sure to close our JMS resources!
            if (queueReceiver != null)
            {
                queueReceiver.close();
            }
            if (queueBrowser != null)
            {
                queueBrowser.close();
            }
            if (queueSession != null)
            {
                queueSession.close();
            }
            if (queueConnection != null)
            {
                queueConnection.close();
            }
            if (initialContext != null)
            {
                initialContext.close();
            }
        }
    }

    private static void printMessage(String action, int cnt, TextMessage textMessage) throws Exception
    {
        System.out.println(action + "[" + cnt + "] -------------------------- ");
        System.out.println(textMessage.getText());
        System.out.println("    properties ...");
        Enumeration propertyNameEnum = textMessage.getPropertyNames();
        while (propertyNameEnum.hasMoreElements())
        {
            String propertyName = (String) propertyNameEnum.nextElement();
            String propertyValue = textMessage.getStringProperty(propertyName);
            System.out.println("  " + propertyName + ": " + propertyValue);
        }
    }

}
