package com.decisionhelperapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class Question implements Parcelable {
    private String description;
    private String id;
    private int score;
    private String type;
    private String title;
    private List<Answer> answers;

    public Question(String description, String id, int score, String type, String title) {
        this.description = description;
        this.id = id;
        this.score = score;
        this.type = type;
        this.title = title;
        this.answers = new ArrayList<>();
    }

    public Question() {
        // Empty constructor added
        this.answers = new ArrayList<>();
    }

    // Parcelable implementation
    protected Question(Parcel in) {
        description = in.readString();
        id = in.readString();
        score = in.readInt();
        type = in.readString();
        title = in.readString();
        answers = in.createTypedArrayList(Answer.CREATOR);
    }

    public static final Creator<Question> CREATOR = new Creator<>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeString(id);
        dest.writeInt(score);
        dest.writeString(type);
        dest.writeString(title);
        dest.writeTypedList(answers);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void loadAnswersFromDescription() {
        if (description == null || description.isEmpty()) {
            return;
        }
        
        List<Answer> loadedAnswers = new ArrayList<>();
        String currentOptionText = null;
        int currentPercentage = 0;
        
        String[] lines = description.split("\n");
        for (String line : lines) {
            if (line.startsWith("option:")) {
                // If we have a complete option from previous iteration, add it
                if (currentOptionText != null) {
                    loadedAnswers.add(new Answer(currentOptionText, currentPercentage));
                }

                currentOptionText = line.substring("option:".length());
                currentPercentage = 0; // Reset percentage
            } else if (line.startsWith("percentage:") && currentOptionText != null) {
                try {
                    currentPercentage = Integer.parseInt(line.substring("percentage:".length()));
                } catch (NumberFormatException e) {
                    currentPercentage = 0;
                }
            }
        }
        
        // Add the last answer if there is one
        if (currentOptionText != null) {
            loadedAnswers.add(new Answer(currentOptionText, currentPercentage));
        }
        
        this.answers = loadedAnswers;
    }

    // Inner class to represent an answer with percentage
    public static class Answer implements Parcelable {
        private String text;
        private int percentage;

        public Answer() {}
        public Answer(String text, int percentage) {
            this.text = text;
            this.percentage = percentage;
        }

        protected Answer(Parcel in) {
            text = in.readString();
            percentage = in.readInt();
        }

        public static final Creator<Answer> CREATOR = new Creator<>() {
            @Override
            public Answer createFromParcel(Parcel in) {
                return new Answer(in);
            }

            @Override
            public Answer[] newArray(int size) {
                return new Answer[size];
            }
        };

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getPercentage() {
            return percentage;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(text);
            dest.writeInt(percentage);
        }
    }
}