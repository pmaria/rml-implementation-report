package oeg.dia.fi.upm.es;

import com.taxonic.carml.engine.RmlMapper;
import oeg.dia.fi.upm.es.CARML.TestCaseCARML;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestCaseRML {

    private final static Logger LOG = Logger.getLogger("oeg.dia.fi.upm.es.TestCaseRML");
    private PrintWriter pwResults, pwErrors, pwMetadata;
    private File[] directories;
    private String platform;

    public TestCaseRML(String testPath, String platform){
        directories = new File(testPath).listFiles();
        this.platform = platform;
        try {
            pwResults = new PrintWriter("./output/"+this.platform+"/results.csv", "UTF-8");
            pwErrors = new PrintWriter("./output/"+this.platform+"/errors.csv", "UTF-8");
            pwMetadata = new PrintWriter("./output/"+this.platform+"/metadata.csv", "UTF-8");
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error open the writers: "+e.getMessage());
        }

        pwResults.println("testid,result");
        pwErrors.println("id,error");
        pwMetadata.println("tester,platform,testid");
    }

    public void run(){
        for (int i = 0 ; i<directories.length; i++) {
            File testDir = directories[i];
            runUnitTestCase(testDir);
        }
    }

    public void runUnitTestCase(File testDir) {
        File[] directories = testDir.listFiles();
        File mappingFile = null, outputFile = null; Model result=null; String test,error="";
        File output = new File(testDir.getAbsolutePath() + "/" + platform + "Output.ttl");
        boolean comparator,errorFlag=false;
        try {

            for (int i = 0; i < directories.length; i++) {
                if (directories[i].getName().matches(".*mapping.*")) {
                    mappingFile = directories[i];
                } else if (directories[i].getName().matches("output\\.ttl") || directories[i].getName().matches("output\\.nq")) {
                    outputFile = directories[i];
                }
            }
            if(platform.equals("carml")){
                pwMetadata.println("dchaves.oeg-upm.net,https://github.com/carml/carml," + testDir.getName());
                TestCaseCARML testCaseCARML = new TestCaseCARML();
                if(testDir.getName().equals("RMLTC0007b-JSON")){
                    System.out.printf("Hola");
                }
                result = testCaseCARML.runUnitTestCaseCARML(testDir,mappingFile,output);
                error = testCaseCARML.getError();
            }
            //ToDo Else if equals RMLMapper
            //Todo else if equals Ontario
            //...
            FileInputStream input =new FileInputStream(outputFile);
            Model expected = Rio.parse(input, "", RDFFormat.TURTLE);
            comparator = Models.isomorphic(result,expected);
        }catch (Exception e){
            LOG.log(Level.WARNING,"Error "+e.getLocalizedMessage());
            comparator = Utils.checkExpectedError(testDir.getName());
            if(!comparator){
               errorFlag=true;
               if(error.equals("")){
                   error = e.getLocalizedMessage();
               }
            }
        }
        if(testDir.getName().matches(".*SQLServer|.*SPARQL|.*PostgreSQL|.*MySQL") && platform.equals("carml")){
            test = "inapplicable";
        }
        else {
            if(errorFlag){
                pwErrors.println(testDir.getName()+",\""+error+"\"");
            }
            if (comparator) {
                test = "passed";
            } else {
                test = "failed";
            }
        }
        pwResults.println(testDir.getName()+","+test);
    }

    public void close(){
        pwResults.close();pwErrors.close();pwMetadata.close();
    }
}