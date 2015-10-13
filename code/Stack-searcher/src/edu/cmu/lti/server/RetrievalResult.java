package edu.cmu.lti.server;

/**
 * Class that represents a search result
 */
public class RetrievalResult implements Comparable<RetrievalResult> {
  public String getDataSource() {
    return dataSource;
  }

  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }

  public void setDocID(String docID) {
    this.docID = docID;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

  public void setQueryString(String queryString) {
    this.queryString = queryString;
  }

  public void setUrl(String urlString){
	  this.url = urlString;
  }
  
  public String dataSource;

  private String docID;
  
  private String url;

  private double score;

  private String text;

  private int rank;

  private String queryString;

  private double rerankScore;

  /**
   * Constructor
   * 
   * @param docID
   *          document identifier
   * @param score
   *          score assigned to search result
   * @param text
   *          text of result
   * @param rank
   *          rank of result
   * @param queryString
   *          query string that was used to find result
   */
  public RetrievalResult(String docID, double score, String text, int rank, String queryString, String url) {
    this.docID = docID;
    this.score = score;
    this.text = text;
    this.rank = rank;
    this.queryString = queryString;
    this.url = url;
  }
  
  public RetrievalResult() {
  }

  public boolean equals(Object o) {
    try {
      // Attempt to cast object and compare docIDs (used by
      // Collections.contains operator, only compares document ID)
      if (o != null) {
        return ((RetrievalResult) o).getDocID().equals(this.docID);
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

  public String toString() {
    return this.text;
  }

  /**
   * Get query string
   * 
   * @return query string used to retrieve document
   */
  public String getQueryString() {
    return this.queryString;
  }

  public String getUrl(){
	  return this.url;
  }
  
  /**
   * Get document ID
   * 
   * @return document identifier
   */
  public String getDocID() {
    return this.docID;
  }

  /**
   * Get result score
   * 
   * @return result score
   */
  public double getScore() {
    return this.score;
  }

  /**
   * Get text of document/search result
   * 
   * @return text of result
   */
  public String getText() {
    return this.text;
  }

  /**
   * Get rank of document/search result
   * 
   * @return result rank
   */
  public int getRank() {
    return this.rank;
  }

  /**
   * Get rerank of document/search result
   * 
   * @return result rerank score
   */
  public double getRerankScore() {
    return this.rerankScore;
  }

  /**
   * Set rerank score of document/search result
   * 
   */
  public void setRerankScore(double rerankScore) {
    this.rerankScore = rerankScore;
  }

  @Override
  public int compareTo(RetrievalResult o) {
    if (this.rerankScore - o.rerankScore > 0) {
      return 1;
    } else if (this.rerankScore - o.rerankScore < 0) {
      return -1;
    } else {
      return 0;
    }
  }
}
