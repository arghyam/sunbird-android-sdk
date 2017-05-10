package org.ekstep.genieservices.content.db.model;

import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.db.contract.ContentEntry;
import org.ekstep.genieservices.commons.db.core.ContentValues;
import org.ekstep.genieservices.commons.db.core.ICleanable;
import org.ekstep.genieservices.commons.db.core.IReadable;
import org.ekstep.genieservices.commons.db.core.IResultSet;
import org.ekstep.genieservices.commons.db.core.IUpdatable;
import org.ekstep.genieservices.commons.db.core.IWritable;
import org.ekstep.genieservices.commons.db.operations.IDBSession;
import org.ekstep.genieservices.commons.utils.ArrayUtil;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.ekstep.genieservices.content.ContentConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.valueOf;

/**
 * Created on 5/3/2017.
 *
 * @author anil
 */
public class ContentModel implements IWritable, IUpdatable, IReadable, ICleanable {

    public static final String IDENTIFIER_KEY = "identifier";
    public static final String CONTENT_TYPE_KEY = "contentType";

    private static final String MIME_TYPE_KEY = "mimeType";
    private static final String VISIBILITY_KEY = "visibility";
    private static final String LAST_UPDATED_ON_KEY = "lastUpdatedOn";
    private static final String PRE_REQUISITES_KEY = "pre_requisites";
    private static final String CHILDREN_KEY = "children";

    public static int minCompatibilityLevel = 1;
    public static int maxCompatibilityLevel = 2;
    // TODO: 02-03-2017 : We can remove this later after few release
    public static int defaultCompatibilityLevel = 1;

    private IDBSession mDBSession;

    private Long id = -1L;
    private String identifier;
    private Map<String, Object> serverData;
    private Map<String, Object> localData;
    private String mimeType;
    private String path;
    private String visibility;
    //The content reference count default value will be 1. Don't change this value.
    private int refCount = 1;
    private int contentState;
    private String contentType;
    private String manifestVersion;
    private String localLastUpdatedTime;

    private boolean isExternalContent;

    private long lastModified;

    private ContentModel() {
    }

    private ContentModel(IDBSession dbSession, String identifier) {
        this.mDBSession = dbSession;
        this.identifier = identifier;
    }

    private ContentModel(IDBSession dbSession, Map data, String manifestVersion, boolean isLocalData) {
        if (data == null) {
            return;
        }

        this.mDBSession = dbSession;

        if (isLocalData) {
            this.manifestVersion = manifestVersion;
            this.localData = data;
        } else {
            this.serverData = data;
        }

        if (data.containsKey(IDENTIFIER_KEY)) {
            identifier = (String) data.get(IDENTIFIER_KEY);
        }

        if (data.containsKey(MIME_TYPE_KEY)) {
            mimeType = (String) data.get(MIME_TYPE_KEY);
        }

        if (data.containsKey(CONTENT_TYPE_KEY)) {
            contentType = (String) data.get(CONTENT_TYPE_KEY);
            if (!StringUtil.isNullOrEmpty(contentType)) {
                contentType = contentType.toLowerCase();
            }
        }

        visibility = data.containsKey(VISIBILITY_KEY) ? (String) data.get(VISIBILITY_KEY) : ContentConstants.Visibility.DEFAULT;
    }

    public static ContentModel find(IDBSession dbSession, Object identifier) {
        ContentModel content = new ContentModel(dbSession, valueOf(identifier));
        dbSession.read(content);

        if (content.id == -1L) {
            return null;
        } else {
            return content;
        }
    }

    public static ContentModel build() {
        return new ContentModel();
    }

    public static ContentModel build(IDBSession dbSession, Map data, String manifestVersion, boolean isLocal) {
        ContentModel contentModel = new ContentModel(dbSession, data, manifestVersion, isLocal);
        return contentModel;
    }

    public Void save() {
        mDBSession.create(this);
        return null;
    }

    public Void update() {
        mDBSession.update(this);
        return null;
    }

    public boolean hasPreRequisites() {
        return (localData != null) && (localData.get(PRE_REQUISITES_KEY) != null);
    }

    public List<String> getPreRequisitesIdentifiers() {
        List<Map> children = (List) localData.get(PRE_REQUISITES_KEY);

        List<String> childIdentifiers = new ArrayList<>();
        for (Map child : children) {
            String childIdentifier = valueOf(child.get(IDENTIFIER_KEY));
            childIdentifiers.add(childIdentifier);
        }

        // Return the pre_requisites in DB
        return childIdentifiers;
    }

    public boolean hasChildren() {
        return (localData != null) && (localData.get(CHILDREN_KEY) != null);
    }

    public List<String> getChildContentsIdentifiers() {
        List<Map> children = (List) localData.get(CHILDREN_KEY);

        List<String> childIdentifiers = new ArrayList<>();
        for (Map child : children) {
            String childIdentifier = valueOf(child.get(IDENTIFIER_KEY));
            childIdentifiers.add(childIdentifier);
        }

        // Return the childrenInDB
        return childIdentifiers;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();

        with(contentValues, ContentEntry.COLUMN_NAME_MANIFEST_VERSION, manifestVersion);
        with(contentValues, ContentEntry.COLUMN_NAME_IDENTIFIER, identifier);
        with(contentValues, ContentEntry.COLUMN_NAME_SERVER_DATA, serverData);
        with(contentValues, ContentEntry.COLUMN_NAME_LOCAL_DATA, localData);
        with(contentValues, ContentEntry.COLUMN_NAME_MIME_TYPE, mimeType);
        with(contentValues, ContentEntry.COLUMN_NAME_PATH, path);
        with(contentValues, ContentEntry.COLUMN_NAME_VISIBILITY, visibility);
        with(contentValues, ContentEntry.COLUMN_NAME_CONTENT_TYPE, contentType);
        with(contentValues, ContentEntry.COLUMN_NAME_LOCAL_LAST_UPDATED_ON, localLastUpdatedOn());
        with(contentValues, ContentEntry.COLUMN_NAME_SERVER_LAST_UPDATED_ON, serverLastUpdatedOn());
        with(contentValues, ContentEntry.COLUMN_NAME_INDEX, searchIndex());

        contentValues.put(ContentEntry.COLUMN_NAME_REF_COUNT, refCount);
        contentValues.put(ContentEntry.COLUMN_NAME_CONTENT_STATE, contentState);

        return contentValues;
    }

    @Override
    public void updateId(long id) {
        this.id = id;
    }

    @Override
    public IReadable read(IResultSet resultSet) {
        if (resultSet != null && resultSet.moveToFirst()) {
            readWithoutMoving(resultSet);
        }

        return this;
    }

    @Override
    public ContentValues getFieldsToUpdate() {
        ContentValues contentValues = new ContentValues();

        with(contentValues, ContentEntry.COLUMN_NAME_MANIFEST_VERSION, manifestVersion);
        with(contentValues, ContentEntry.COLUMN_NAME_IDENTIFIER, identifier);
        with(contentValues, ContentEntry.COLUMN_NAME_SERVER_DATA, serverData);
        with(contentValues, ContentEntry.COLUMN_NAME_LOCAL_DATA, localData);
        with(contentValues, ContentEntry.COLUMN_NAME_MIME_TYPE, mimeType);
        with(contentValues, ContentEntry.COLUMN_NAME_PATH, path);
        with(contentValues, ContentEntry.COLUMN_NAME_VISIBILITY, visibility);
        with(contentValues, ContentEntry.COLUMN_NAME_CONTENT_TYPE, contentType);
        with(contentValues, ContentEntry.COLUMN_NAME_LOCAL_LAST_UPDATED_ON, localLastUpdatedOn());
        with(contentValues, ContentEntry.COLUMN_NAME_SERVER_LAST_UPDATED_ON, serverLastUpdatedOn());
        with(contentValues, ContentEntry.COLUMN_NAME_INDEX, searchIndex());

        contentValues.put(ContentEntry.COLUMN_NAME_REF_COUNT, refCount);
        contentValues.put(ContentEntry.COLUMN_NAME_CONTENT_STATE, contentState);

        return contentValues;
    }

    @Override
    public String getTableName() {
        return ContentEntry.TABLE_NAME;
    }

    @Override
    public void clean() {
        id = -1L;
    }

    @Override
    public String selectionToClean() {
        return String.format(Locale.US, "where %s = '%s'", ContentEntry.COLUMN_NAME_IDENTIFIER, identifier);
    }

    @Override
    public String updateBy() {
        return String.format(Locale.US, "%s = '%s'", ContentEntry.COLUMN_NAME_IDENTIFIER, identifier);
    }

    @Override
    public String orderBy() {
        return "";
    }

    @Override
    public String filterForRead() {
        return String.format(Locale.US, "where %s = ?", ContentEntry.COLUMN_NAME_IDENTIFIER);
    }

    @Override
    public String[] selectionArgsForFilter() {
        return new String[]{identifier};
    }

    @Override
    public String limitBy() {
        return "limit 1";
    }

    @Override
    public void beforeWrite(AppContext context) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentModel content = (ContentModel) o;

        if (serverData != null ? !serverData.equals(content.serverData) : content.serverData != null) {
            return false;
        }

        if (localData != null ? !localData.equals(content.localData) : content.localData != null) {
            return false;
        }

        if (!identifier.equals(content.identifier)) {
            return false;
        }

        if (mimeType != null ? !mimeType.equals(content.mimeType) : content.mimeType != null) {
            return false;
        }

        if (path != null ? !path.equals(content.path) : content.path != null) {
            return false;
        }

        if (visibility != null ? !visibility.equals(content.visibility) : content.visibility != null) {
            return false;
        }

        if (!id.equals(content.id)) {
            return false;
        }

        return !(manifestVersion != null ? !manifestVersion.equals(content.manifestVersion) : content.manifestVersion != null);
    }

    @Override
    public int hashCode() {
        int result = serverData != null ? serverData.hashCode() : 0;
        result = 31 * result + (localData != null ? localData.hashCode() : 0);
        result = 31 * result + identifier.hashCode();
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (visibility != null ? visibility.hashCode() : 0);
        result = 31 * result + id.hashCode();
        result = 31 * result + (manifestVersion != null ? manifestVersion.hashCode() : 0);
        result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
        // TODO: add hashCode for contentState
        return result;
    }

    public void readWithoutMoving(IResultSet resultSet) {
        id = resultSet.getLong(0);
        identifier = resultSet.getString(resultSet.getColumnIndex(ContentEntry.COLUMN_NAME_IDENTIFIER));
        serverData = GsonUtil.fromJson(resultSet.getString(resultSet.getColumnIndex(ContentEntry.COLUMN_NAME_SERVER_DATA)), Map.class);
        localData = GsonUtil.fromJson(resultSet.getString(resultSet.getColumnIndex(ContentEntry.COLUMN_NAME_LOCAL_DATA)), Map.class);
        mimeType = resultSet.getString(resultSet.getColumnIndex(ContentEntry.COLUMN_NAME_MIME_TYPE));
        path = resultSet.getString(resultSet.getColumnIndex(ContentEntry.COLUMN_NAME_PATH));
        visibility = resultSet.getString(resultSet.getColumnIndex(ContentEntry.COLUMN_NAME_VISIBILITY));
        refCount = resultSet.getInt(resultSet.getColumnIndex(ContentEntry.COLUMN_NAME_REF_COUNT));
        contentState = resultSet.getInt(resultSet.getColumnIndex(ContentEntry.COLUMN_NAME_CONTENT_STATE));
        contentType = resultSet.getString(resultSet.getColumnIndex(ContentEntry.COLUMN_NAME_CONTENT_TYPE));
        localLastUpdatedTime = resultSet.getString(resultSet.getColumnIndex(ContentEntry.COLUMN_NAME_LOCAL_LAST_UPDATED_ON));

        isExternalContent = isExternalContent(path);

        // TODO: add feedback in content service if required
//        feedback = addContentFeedback(identifier);
    }

    private boolean isExternalContent(String path) {
        return (!StringUtil.isNullOrEmpty(path)) && path.contains(ContentConstants.GENIE_EXTRACTED_ECAR_FOLDER_PATH);
    }

    private void with(ContentValues contentValues, String key, String value) {
        if (value != null) {
            contentValues.put(key, value);
        }
    }

    private void with(ContentValues contentValues, String key, Map<String, Object> value) {
        if (value != null) {
            contentValues.put(key, GsonUtil.toJson(value));
        }
    }

    private String localLastUpdatedOn() {
        String lastUpdatedTime = null;

        if (localData != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(ContentConstants.ISO_DATE_TIME_PATTERN, Locale.US);
            lastUpdatedTime = dateFormat.format(new Date());
        }

        if (isExternalContent(path)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(ContentConstants.ISO_DATE_TIME_PATTERN, Locale.US);
            lastUpdatedTime = dateFormat.format(new Date(lastModified));
        }

        return lastUpdatedTime;
    }

    private String serverLastUpdatedOn() {
        return serverData != null
                ? (String) serverData.get(LAST_UPDATED_ON_KEY)
                : null;
    }

    /**
     * Search index is the comma separated string of serverData/localData.
     *
     * @return comma separated string.
     */
    private String searchIndex() {
        if (ContentConstants.MimeType.APPLICATION.equals(mimeType)) {
            if (serverData != null) {
                return ArrayUtil.mapToCommaSeparatedString(serverData.values());
            }
        } else if (localData != null) {
            return ArrayUtil.mapToCommaSeparatedString(localData.values());
        } else if (serverData != null) {
            return ArrayUtil.mapToCommaSeparatedString(serverData.values());
        }

        return null;
    }

    public int getRefCount() {
        return refCount;
    }

    public void addOrUpdateRefCount(int refCount) {
        if (refCount < 0) {
            refCount = 0;
        }
        this.refCount = refCount;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, Object> getServerData() {
        return serverData;
    }

    public Map<String, Object> getLocalData() {
        return localData;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getPath() {
        return path;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public int getContentState() {
        return contentState;
    }

    public void addOrUpdateContentState(int contentState) {
        this.contentState = contentState;
    }

    public String getContentType() {
        return contentType;
    }

    public String getLocalLastUpdatedTime() {
        return localLastUpdatedTime;
    }

    public boolean isExternalContent() {
        return isExternalContent;
    }

    public void setFileLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}