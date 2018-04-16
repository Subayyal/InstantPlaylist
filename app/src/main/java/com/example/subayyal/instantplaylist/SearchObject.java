package com.example.subayyal.instantplaylist;

/**
 * Created by subayyal on 4/15/2018.
 */

public class SearchObject {
    private String query;
    private String sortBy;
    private String uploadDate;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }
}
