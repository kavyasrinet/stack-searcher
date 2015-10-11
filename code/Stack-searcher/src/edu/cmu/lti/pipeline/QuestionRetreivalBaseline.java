package edu.cmu.lti.pipeline;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import edu.cmu.lti.search.BingSearchAgent;
import edu.cmu.lti.search.RetrievalResult;
/**
 * @author Kavya Srinet.
 */
public class QuestionRetreivalBaseline {
    private static final int ArrayList = 0;
    private static final int String = 0;
    public static void main(String[] args) throws URISyntaxException, IOException {

        //Step 1: read input file, load training questions
   //     BufferedReader reader = new BufferedReader(new FileReader(new File("dataset_sample/datasetX.txt")));
      //datasetY
        String line = null;
        String accountKey = "74mM12fgn5KdVok+J7bKHkPybKZjBHh8asx+91JkwdI";
        BingSearchAgent bsa = new BingSearchAgent();
        bsa.initialize(accountKey);
        bsa.setResultSetSize(20);
        String qid ="";
        String question="";
        String ln2 = null;
        int j=0;

        ArrayList<RetrievalResult> results = new ArrayList<>();
     //   while(j<=64){
         //   line = "";
          //  String[] i = line.split("\t");
          String[] i = new String[3];
          i[0] = "28";
          i[1] = "Cheapest mobile operator in Europe";
          i[2] = "What are the best ways to avoid data roaming fees when travelling abroad?Is where any default cheap mobile provider across Europe?\nI want to buy sim-card in one country (East or Nord Europe), and use it in other (West Europe). Is it possible?";
          qid = i[0];
            String title = i[1];
            String body = i[2];

            results.addAll(bsa.retrieveDocuments(qid, "site:travel.stackexchange.com "+title));
            System.out.println(results);
  //          j=j+1;
    //    }
        
  
    }
}
