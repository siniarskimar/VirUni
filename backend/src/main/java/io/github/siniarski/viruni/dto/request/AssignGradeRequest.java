package io.github.siniarski.viruni.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class AssignGradeRequest {
    @JsonProperty("subject")
    @NotNull(message = "grade must reference a subject id")
    private Long subjectId;

    @JsonProperty("student")
    @NotNull(message = "grade must reference a student id")
    private Long studentId;

    @JsonProperty("teacher")
    @NotNull(message = "grade must reference a teacher id")
    private Long teacherId;

    @NotNull(message = "grade must have a value")
    private Float value;

    public Long getSubjectId() {
        return subjectId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public Float getValue() {
        return value;
    }

    public Long getStudentId() {
        return studentId;
    }
}
