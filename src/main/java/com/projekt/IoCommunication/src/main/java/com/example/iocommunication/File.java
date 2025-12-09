package com.example.iocommunication;

import jakarta.persistence.*;

@Entity
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;
    private long size;

    @Lob
    private byte[] content;

    @ManyToOne
    private User uploadedBy;

    public File() {}

    public File(String fileName, String fileType, long size, byte[] content, User uploadedBy) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.size = size;
        this.content = content;
        this.uploadedBy = uploadedBy;
    }

    public Long getId() {
        return id;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileType() {
        return fileType;
    }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public long getSize() {
        return size;
    }
    public void setSize(long size) {
        this.size = size;
    }
    public byte[] getContent() {
        return content;
    }
    public void setContent(byte[] content) {
        this.content = content;
    }
    public User getUploadedBy() {
        return uploadedBy;
    }
    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
}
