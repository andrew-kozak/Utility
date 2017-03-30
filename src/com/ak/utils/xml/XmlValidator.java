package com.ak.utils.xml;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

/**
 * Created by Andrew.Kozak on 3/29/2017.
 */
public class XmlValidator
{
    /**
     *
     * @param a_asArgs
     */
    public static void main(String[] a_asArgs)
    {
        Source xmlFile = null;

        try
        {
            // NOTE: Could the URL too
//            URL schemaFile = new URL("http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd");
            File schemaFile = new File(a_asArgs[0]);

            String sFile = a_asArgs[1];

            // Create Schema
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaFile);

            // Create XML File
            xmlFile = new StreamSource(new File(sFile));

            boolean bValid = validateXmlFile(schema, xmlFile);

            System.out.println(xmlFile.getSystemId() + " is:" + (bValid ? " valid." : " NOT valid."));

        }
        catch (Exception e)
        {
            System.out.println(xmlFile.getSystemId() + " is NOT valid reason:" + e);
        }
    }

    /**
     * Validate the given XML File with the Schema
     *
     * @param a_oSchema
     * @param a_oXmlFile
     * @return
     */
    public static boolean validateXmlFile(Schema a_oSchema, Source a_oXmlFile)
    {
        boolean bReturn = false;

        // Validate
        Validator validator = a_oSchema.newValidator();

        try
        {
            validator.validate(a_oXmlFile);
            bReturn = true;
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return bReturn;
    }
}
