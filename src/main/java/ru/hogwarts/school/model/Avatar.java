package ru.hogwarts.school.model;

import jakarta.persistence.*;

import java.util.Arrays;
import java.util.Objects;

@Entity
public class Avatar {


    @Id
    @GeneratedValue
    private Long id;

    private String filePath;
    private Long fileSize;

    private String mediaType;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(nullable = false)
    private byte[] preview;

    @OneToOne
    private Student student;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public byte[] getPreview() {
        return preview;
    }

    public void setPreview(byte[] data) {
        this.preview =data;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, filePath, fileSize, mediaType, Arrays.hashCode(preview), student);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Avatar avatar = (Avatar) object;
        return Objects.equals(id, avatar.id) && Objects.equals(filePath, avatar.filePath) && Objects.equals(fileSize, avatar.fileSize) && Objects.equals(mediaType, avatar.mediaType) && Objects.deepEquals(preview, avatar.preview) && Objects.equals(student, avatar.student);
    }

    @Override
    public String toString() {
        return "Avatar{" +
                "id=" + id +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", mediatype='" + mediaType + '\'' +
                ", data=" + Arrays.toString(preview) +
                ", student=" + student +
                '}';
    }
}
