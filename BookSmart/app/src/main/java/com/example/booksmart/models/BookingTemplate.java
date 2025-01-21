package com.example.booksmart.models;

/**
 * Model class representing a booking template.
 * Templates allow users to save and reuse common booking configurations.
 */
public class BookingTemplate {
    private String templateId;
    private String userId;
    private String templateName;
    private String spaceType;
    private String startTime;
    private String endTime;

    /**
     * Required empty constructor for Firebase deserialization
     */
    public BookingTemplate() {
    }

    /**
     * Creates a new booking template with specified parameters
     * 
     * @param userId       User who created the template
     * @param templateName Name of the template
     * @param spaceType    Type of space to book
     * @param startTime    Booking start time
     * @param endTime      Booking end time
     */
    public BookingTemplate(String userId, String templateName, String spaceType,
            String startTime, String endTime) {
        this.userId = userId;
        this.templateName = templateName;
        this.spaceType = spaceType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getSpaceType() {
        return spaceType;
    }

    public void setSpaceType(String spaceType) {
        this.spaceType = spaceType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}