package org.ekstep.genieservices.commons.bean;

import org.ekstep.genieservices.commons.utils.StringUtil;

import java.util.List;

/**
 * This class accepts,
 * - isChildContent - if the child contents are required while importing
 * - destinationFolder - where the contents are to be stored
 * - contentIds - list of contentIds to be imported
 * - correlationData - list of correlationData
 * - status - list of status i.e. "Live", "Draft"
 * - contentType - List of contentType. "Story", "Worksheet", "Collection", "Game", "TextBook", "Course", "LessonPlan".
 */
public class ContentImportRequest {

    private boolean isChildContent;
    private String destinationFolder;
    private List<String> contentIds;
    private List<CorrelationData> correlationData;

    private ContentImportRequest(boolean isChildContent, String destinationFolder, List<String> contentIds, List<CorrelationData> correlationData) {
        this.isChildContent = isChildContent;
        this.destinationFolder = destinationFolder;
        this.contentIds = contentIds;
        this.correlationData = correlationData;
    }

    public boolean isChildContent() {
        return isChildContent;
    }

    public String getDestinationFolder() {
        return destinationFolder;
    }

    public List<String> getContentIds() {
        return contentIds;
    }

    public List<CorrelationData> getCorrelationData() {
        return correlationData;
    }

    public static class Builder {
        private boolean isChildContent;
        private String destinationFolder;
        private List<String> contentIds;
        private List<CorrelationData> correlationData;

        /**
         * Method to indicate that the file being imported is a child content
         */
        public Builder childContent() {
            this.isChildContent = true;
            return this;
        }

        /**
         * Destination folder where content will import.
         */
        public Builder toFolder(String toFolder) {
            if (StringUtil.isNullOrEmpty(toFolder)) {
                throw new IllegalArgumentException("Illegal toFolder, should not be null or empty.");
            }
            this.destinationFolder = toFolder;
            return this;
        }

        /**
         * List of content identifier which needs to import.
         */
        public Builder contentIds(List<String> contentIds) {
            this.contentIds = contentIds;
            return this;
        }

        /**
         * CorrelationData of content.
         */
        public Builder correlationData(List<CorrelationData> correlationData) {
            this.correlationData = correlationData;
            return this;
        }

        public ContentImportRequest build() {
            if (StringUtil.isNullOrEmpty(destinationFolder)) {
                throw new IllegalStateException("To folder required.");
            }

            if (contentIds == null || contentIds.size() == 0) {
                throw new IllegalStateException("ContentIds required.");
            }

            return new ContentImportRequest(isChildContent, destinationFolder, contentIds, correlationData);
        }
    }
}
