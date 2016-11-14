package com.example.julisarrelli.findmycar;


        import java.util.Date;
        import java.util.List;

/**
 * Created by Angie on 16/10/2016.
 */
public class Question {

    private String question;
    private Date publishedAt;
    private String url;
    private List<Choice> choices;

    public Question(String question, Date publishedAt, String url, List<Choice> choices) {
        this.question = question;
        this.publishedAt = publishedAt;
        this.url = url;
        this.choices = choices;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    @Override
    public String toString() {
        return "Question{" +
                "question='" + question + '\'' +
                ", publishedAt=" + publishedAt +
                ", url='" + url + '\'' +
                ", choices=" + choices +
                '}';
    }
}
