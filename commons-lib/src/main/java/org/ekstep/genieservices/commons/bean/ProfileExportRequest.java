package org.ekstep.genieservices.commons.bean;

import org.ekstep.genieservices.commons.utils.StringUtil;

import java.util.List;

/**
 * This class accepts userIds as {@link List<String>}, and destinationFolder when requesting to export user profiles.
 */
public class ProfileExportRequest {

    private List<String> userIds;
    private String destinationFolder;

    private ProfileExportRequest(List<String> userIds, String destinationFolder) {
        this.userIds = userIds;
        this.destinationFolder = destinationFolder;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public String getDestinationFolder() {
        return destinationFolder;
    }

    public static class Builder {
        private List<String> userIds;
        private String destinationFolder;

        /**
         * List of user which needs to export.
         */
        public Builder exportUsers(List<String> userIds) {
            this.userIds = userIds;
            return this;
        }

        /**
         * Absolute path of destination folder where content will import.
         */
        public Builder toFolder(String toFolder) {
            if (StringUtil.isNullOrEmpty(toFolder)) {
                throw new IllegalArgumentException("Illegal toFolder, should not be null or empty.");
            }
            this.destinationFolder = toFolder;
            return this;
        }

        public ProfileExportRequest build() {
            if (StringUtil.isNullOrEmpty(destinationFolder)) {
                throw new IllegalStateException("To folder required.");
            }
            return new ProfileExportRequest(userIds, destinationFolder);
        }
    }
}
