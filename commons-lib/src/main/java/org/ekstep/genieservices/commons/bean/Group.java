package org.ekstep.genieservices.commons.bean;

/**
 * This class holds all the data related to a group.
 */
public class Group {

    private String gid;
    private String name;
    private String[] syllabus;
    private String[] grade;
    private Long createdAt;
    private Long updatedAt;

    public Group(String name) {
        this.name = name;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getSyllabus() {
        return syllabus;
    }

    public void setSyllabus(String[] syllabus) {
        this.syllabus = syllabus;
    }

    public String[] getGrade() {
        return grade;
    }

    public void setGrade(String[] grade) {
        this.grade = grade;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }


}