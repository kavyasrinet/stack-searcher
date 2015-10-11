package edu.cmu.lti.search;


public class RetrievalResult  {

    private String docID, text ;

    private int rank = -1;
    private float probability;

    public float getProbability() {
        return probability;
    }

    public void setProbability(float probability) {
        this.probability = probability;
    }
    private String queryString, url;

    public RetrievalResult() {
        super();
    }

    public RetrievalResult(String docID, float score, String queryString) {
        super();
        this.docID = docID;
        this.probability = score;
        this.queryString = queryString;
    }

    public RetrievalResult(String docID , float score, String queryString,
                           String  text , String url ) {
        super();
        this.docID = docID;
        this.probability = score;
        this.queryString = queryString;
        this.text = text;
        this.url = url;
    }

    public String getQueryString() {
        return this.queryString;
    }

    public String getDocID() {
        return this.docID;
    }

    public int getRank() {
        return this.rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getUrl() {
        return url;
    }
    public String toString() {
        return "[" + docID + " " + url + "]" ; // \n " + text + "]";
    }
    public String getText(){
        return text;
    }
    public void setText( String text ){
        this.text = text;
    }


}
