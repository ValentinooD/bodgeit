package com.thebodgeitstore.search;

//Author: Doug Logan
//Website: https://www.CyberNinjas.com


import com.thebodgeitstore.util.AES;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AdvancedSearch {
    private HttpServletRequest request = null;
    private HttpSession session = null;
    private Connection connection = null;
    private Map<String,String> parameters = new LinkedHashMap<>();
    LinkedList<SearchResult> results = new LinkedList<>();
    private String encryptKey = null;
    private LinkedList<String> debugOutput = new LinkedList<>();
    private String output = "";
    private static String jsonPrequal = "[";
    private static String htmlPrequal = "<TABLE border=\"1\">\n" + "<TR><TD>Product</TD><TD>Description</TD><TD>Type</TD><TD>Price</TD></TR>\n";
    private static String jsonPostqual = "]";
    private static String htmlPostqual = "</TABLE>";
    private static String jsonEmpty = "[]";
    private static String htmlEmpty = "<div><b>No Results Found</b></div>";

    //Constructor
    public AdvancedSearch(HttpServletRequest req, HttpSession sess, Connection conn){
        this.request = req;
        this.session = sess;
        this.connection = conn;
        try {
            this.encryptKey = sess.getAttribute("key").toString();
            this.getParameters();
            this.setResults();
            this.setScores();
        } catch (Exception e){
            this.encryptKey = UUID.randomUUID().toString().substring(0, 16);
            this.session.setAttribute("key", this.encryptKey);
        }
        this.parameters.put("key", this.encryptKey);
    }
    
    //Returns whether its an Ajax request, or a normal request based on param "Ajax" in req.
    public boolean isAjax(){
        try {
            return "true".equalsIgnoreCase(this.parameters.get("ajax"));
        } catch (Exception e){
            return false;
        }
    }
    
    //Returns whether the "Debug" flag was sent, and therefore debugging information should be returned.
    public boolean isDebug(){
        return "true".equals(this.request.getParameter("debug"));
    }
    
    //Gets the current encryption key to be output to the page.
    public String getEncryptKey(){
        return this.encryptKey;
    }
    
    //Returns true if its a search request, or false if just a regular page load
    public boolean isSearchRequest(){
        return this.parameters.size() > 1;
    }
    
    //Gets the Debug Output for outputting to the page
    public String getDebugOutput(){
        String debugOut = "";
        for(String dbg : this.debugOutput){
            debugOut = debugOut.concat(dbg).concat("\n\n");
        }
        return debugOut;
    }
    
    //Gets the appropriate output depending on wheter it is an Ajax or normal request
    public String getResultsOutput(){
        if(!results.isEmpty())
            return this.output;
        else if (this.isAjax())
            return jsonEmpty;
        else
            return htmlEmpty;
    }
    
    //Gets the query string used for "You searched for:
    public String getQueryString(){
        String queryString = "";
        for (Map.Entry<String, String> entry : parameters.entrySet()){
            if(!"key".equals(entry.getKey())){
                queryString = queryString.concat(" ").concat(entry.getKey()).concat(":").concat(entry.getValue());
            }
            
        }
        return queryString;
    }
    
    //Checks payloads and other details to see if any items that need to be scored
    //are present, and therefore properly score them.
    private void setScores(){
        try {
            //Cycle Through all Parameters and look for the XSS Payload, and if so score it
            for (Map.Entry<String, String> entry : parameters.entrySet()){
                if(entry.getValue().replaceAll("\\s", "").toLowerCase().indexOf("<script>alert(\"h@ckeda3s\")</script>") > -1){
                    try (Statement statement = this.connection.createStatement()) {
                        statement.execute("UPDATE Score SET status = 1 WHERE task = 'AES_XSS'");
                    }
                }
            }

            //Cycle Through All of the Results & Look for the the arbitrary Table name created, so 
            //that however its taked into the results it can be scored
            String tableName = "f0ecfb32e56d3845f140e5c81a81363ce61d9d50";
            for(SearchResult result : this.results){
                if(result.checkIfValExists(tableName)) {
                    try (Statement statement = this.connection.createStatement()) {
                        statement.execute("UPDATE Score SET status = 1 WHERE task = 'AES_SQLI'");
                    }
                }
            }
            
            if(this.isDebug()){
                try (Statement statement = this.connection.createStatement()) {
                    statement.execute("UPDATE Score SET status = 1 WHERE task = 'HIDDEN_DEBUG'");
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.debugOutput.add(sw.toString());
        }
    }
    
    //Extracts the parameters out of the encrypted string and stores them
    //In the this.parameters to be used by other methods.
    private void getParameters(){
        String queryString = "";
        try {
            if (this.request.getMethod().equals("POST")){
                AES enc = new AES();
                enc.setCrtKey(this.encryptKey);
                String eQuery = this.request.getParameter("q");
                queryString = enc.decryptCrt(eQuery);
                queryString = queryString.replaceAll("[^\\p{ASCII}]", "");
                for(String param : queryString.split("\\|")){
                    String[] keyPair = param.split(":", 2);
                    if(keyPair.length == 2){
                        this.parameters.put(keyPair[0].toLowerCase(), keyPair[1]);
                    }                
                }    
            } 
        } catch (Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.debugOutput.add(sw.toString());
        }
    }
    
    //Uses the this.parameters field to pull the appropriate results and store
    //them into this.results. Also sets the appropriate output in this.output
    //depending on whether its an HTML or Ajax (e.g. JSON Request).
    private void setResults(){
        try (Statement stmt = this.connection.createStatement()) {
            ResultSet rs = null;
           
            String sql = "SELECT PRODUCT, DESC, TYPE, TYPEID, PRICE " +
                         "FROM PRODUCTS AS a JOIN PRODUCTTYPES AS b " +
                         "ON a.TYPEID = b.TYPEID " +
                         "WHERE PRODUCT LIKE '%{PRODUCT}%' AND " + 
                         "DESC LIKE '%{DESC}%' AND PRICE LIKE '%{PRICE}%' " +
                         "AND TYPE LIKE '%{TYPE}%'";
            this.debugOutput.add("SQL Statement Before:".concat(sql));

            for(Map.Entry<String, String> es : this.parameters.entrySet()){
                String find = "\\{".concat(es.getKey().toUpperCase()).concat("\\}");
                sql = sql.replaceAll(find, es.getValue());
            }
            sql = sql.replaceAll("%\\{[^\\}]+\\}", "");
            this.debugOutput.add("SQL Statement After:".concat(sql));
            rs = stmt.executeQuery(sql);
            
            this.output = (this.isAjax()) ? jsonPrequal : htmlPrequal;
            
            while(rs.next()){
                SearchResult result = new SearchResult();
                result.setProduct(rs.getString("PRODUCT"));
                result.setDesc(rs.getString("DESC"));
                result.setType(rs.getString("TYPE"));
                result.setPrice(rs.getString("PRICE"));
                this.results.add(result);
                
                this.output = this.output.concat(this.isAjax() ? result.getJSON().concat(", ") : result.getTrHTML());
            }
            //The negative 2 removes the extra space and , from making valid JSON
            this.output = (this.isAjax()) ? this.output.substring(0, this.output.length() - 2).concat(jsonPostqual)
                                          : this.output.concat(htmlPostqual);
            
        } catch (Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.debugOutput.add(sw.toString());
        }
    }
    
    
}
