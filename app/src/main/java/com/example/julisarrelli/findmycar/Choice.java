package com.example.julisarrelli.findmycar;

/**
 * Created by Angie on 16/10/2016.
 */
public class Choice {

    private String choice;
    private String url;
    private Long votes;

    public Choice(String choice, String url, Long votes) {
        this.choice = choice;
        this.url = url;
        this.votes = votes;
    }

    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getVotes() {
        return votes;
    }

    public void setVotes(Long votes) {
        this.votes = votes;
    }
}
