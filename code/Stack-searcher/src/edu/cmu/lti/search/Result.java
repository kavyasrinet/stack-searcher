package edu.cmu.lti.search;

import java.io.Serializable;

public class Result implements Serializable {

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    private static final long serialVersionUID = 1L;


    private String docText;


    String query;


    String url;
    int rank , score;

    public Result(String docText, String query, String url, int rank) {
        this.setDocText(docText);
        this.query = query;
        this.url = url;
        this.rank = rank;
    }

    public String getURL() {
        return url;
    }

    public void setScore ( int score ) {
        this.score = score;
    }

    public String toString() {
        return "[Result " + rank + " " + url + "\n" + getDocText() + "]";
    }

    public String getAnswer() {
        return getDocText();
    }

    public String getDocID() {
        return rank + "";
    }

    public float getScore() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getQuery() {
        return query;
    }

    public String getDocText() {
        return docText;
    }

    public void setDocText(String docText) {
        this.docText = docText;
    }

}
