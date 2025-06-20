package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {

    @JsonProperty("total_results")
    private Integer totalResults;

    @JsonProperty("results")
    private List<SearchResult> results;

    @JsonProperty("search_query")
    private SearchRequest searchQuery;

    @JsonProperty("search_type_used")
    private String searchTypeUsed; // exact, partial, fuzzy

    @JsonProperty("execution_time_ms")
    private Long executionTimeMs;

    @JsonProperty("suggestions")
    private List<String> suggestions; // Alternative search suggestions

    @JsonProperty("disambiguation_needed")
    private Boolean disambiguationNeeded;

    @JsonProperty("search_tips")
    private List<String> searchTips; // Helpful tips for users

    @JsonProperty("filters_applied")
    private Map<String, String> filtersApplied;

    @JsonProperty("has_more_results")
    private Boolean hasMoreResults; // Indicates if there are more results beyond the limit

    // Constructors
    public SearchResponse() {}

    public SearchResponse(List<SearchResult> results, Integer totalResults) {
        this.results = results;
        this.totalResults = totalResults;
        this.disambiguationNeeded = totalResults > 1;
    }

    // Static factory methods for common scenarios
    public static SearchResponse noResults(SearchRequest query) {
        SearchResponse response = new SearchResponse();
        response.setTotalResults(0);
        response.setResults(new ArrayList<>(Arrays.asList()));
        response.setSearchQuery(query);
        response.setDisambiguationNeeded(false);
        response.setSearchTips(new ArrayList<>(Arrays.asList(
                "Try using partial name matches",
                "Check employer name spelling",
                "Expand date range if searching by employment period",
                "Use composite key search for exact matches"
        )));
        return response;
    }

    public static SearchResponse singleResult(SearchResult result, SearchRequest query) {
        SearchResponse response = new SearchResponse();
        response.setTotalResults(1);
        response.setResults(new ArrayList<>(Arrays.asList(result)));
        response.setSearchQuery(query);
        response.setDisambiguationNeeded(false);
        response.setSearchTypeUsed("exact");
        return response;
    }

    public static SearchResponse multipleResults(List<SearchResult> results, SearchRequest query) {
        SearchResponse response = new SearchResponse();
        response.setTotalResults(results.size());
        response.setResults(results);
        response.setSearchQuery(query);
        response.setDisambiguationNeeded(true);
        response.setSearchTypeUsed("partial");

        // Add disambiguation tips
        response.setSearchTips(new ArrayList<>(Arrays.asList(
                "Multiple matches found - use employment dates to narrow down",
                "Consider department or job title filters",
                "Use composite key search for exact identification"
        )));

        return response;
    }

    public static SearchResponse fuzzyResults(List<SearchResult> results, SearchRequest query, List<String> suggestions) {
        SearchResponse response = new SearchResponse();
        response.setTotalResults(results.size());
        response.setResults(results);
        response.setSearchQuery(query);
        response.setDisambiguationNeeded(true);
        response.setSearchTypeUsed("fuzzy");
        response.setSuggestions(suggestions);

        response.setSearchTips(new ArrayList<>(Arrays.asList(
                "Fuzzy matching used - results may not be exact",
                "Check suggestions for alternative spellings",
                "Use exact search for more precise results"
        )));

        return response;
    }

    // Helper methods
    public void addSearchTip(String tip) {
        if (searchTips == null) {
            searchTips = new java.util.ArrayList<>();
        }
        searchTips.add(tip);
    }

    public void addSuggestion(String suggestion) {
        if (suggestions == null) {
            suggestions = new java.util.ArrayList<>();
        }
        suggestions.add(suggestion);
    }

    public boolean hasResults() {
        return totalResults != null && totalResults > 0;
    }

    public boolean isSingleResult() {
        return totalResults != null && totalResults == 1;
    }

    public boolean needsDisambiguation() {
        return disambiguationNeeded != null && disambiguationNeeded;
    }

    // Getters and Setters
    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
        this.disambiguationNeeded = totalResults != null && totalResults > 1;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public void setResults(List<SearchResult> results) {
        this.results = results;
    }

    public SearchRequest getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(SearchRequest searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getSearchTypeUsed() {
        return searchTypeUsed;
    }

    public void setSearchTypeUsed(String searchTypeUsed) {
        this.searchTypeUsed = searchTypeUsed;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public Boolean getDisambiguationNeeded() {
        return disambiguationNeeded;
    }

    public void setDisambiguationNeeded(Boolean disambiguationNeeded) {
        this.disambiguationNeeded = disambiguationNeeded;
    }

    public List<String> getSearchTips() {
        return searchTips;
    }

    public void setSearchTips(List<String> searchTips) {
        this.searchTips = searchTips;
    }

    public Map<String, String> getFiltersApplied() {
        return filtersApplied;
    }

    public void setFiltersApplied(Map<String, String> filtersApplied) {
        this.filtersApplied = filtersApplied;
    }

    public Boolean getHasMoreResults() {
        return hasMoreResults;
    }

    public void setHasMoreResults(Boolean hasMoreResults) {
        this.hasMoreResults = hasMoreResults;
    }
}