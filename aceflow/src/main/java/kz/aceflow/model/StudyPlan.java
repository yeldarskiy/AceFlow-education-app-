package kz.aceflow.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * A study plan created by a user for a specific exam.
 */
public class StudyPlan {

    private int planId;
    private int userId;
    private String examName;
    private String examType;
    private LocalDate examDate;
    private LocalDate startDate;
    private String status;

    public StudyPlan() {}

    /** Returns the number of days remaining until the exam. */
    public long getDaysRemaining() {
        if (examDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), examDate);
    }

    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
