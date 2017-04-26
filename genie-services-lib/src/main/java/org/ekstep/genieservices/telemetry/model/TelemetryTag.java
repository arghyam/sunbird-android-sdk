package org.ekstep.genieservices.telemetry.model;

import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.db.BaseColumns;
import org.ekstep.genieservices.commons.db.core.ContentValues;
import org.ekstep.genieservices.commons.db.core.ICleanable;
import org.ekstep.genieservices.commons.db.core.IReadable;
import org.ekstep.genieservices.commons.db.core.IResultSet;
import org.ekstep.genieservices.commons.db.core.IWritable;
import org.ekstep.genieservices.telemetry.db.contract.TelemetryTagEntry;

/**
 * Created by swayangjit on 26/4/17.
 */

public class TelemetryTag implements IReadable, IWritable, ICleanable {

    private String name;
    private String hash;
    private String description;
    private String startDate;
    private String endDate;
    private Long id;
    private ContentValues contentValues;
    private AppContext mAppContext;

    private TelemetryTag(AppContext appContext) {
        this.mAppContext = appContext;
        this.contentValues = new ContentValues();
    }

    private TelemetryTag(AppContext appContext, String name, String hash, String description,
                         String startDate, String endDate) {
        this.mAppContext = appContext;
        this.name = name;
        this.hash = hash;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.contentValues = new ContentValues();
    }

    public static TelemetryTag build(AppContext appContext, String name, String hash, String description,
                                     String startDate, String endDate) {
        return new TelemetryTag(appContext, name, hash, description, startDate, endDate);
    }

    public static TelemetryTag find(AppContext appContext) {
        TelemetryTag telemetryTag = new TelemetryTag(appContext);
        appContext.getDBSession().read(telemetryTag);
        return telemetryTag;
    }

    @Override
    public IReadable read(IResultSet resultSet) {
        if (resultSet != null && resultSet.moveToFirst()) {
            id = resultSet.getLong(resultSet.getColumnIndex(BaseColumns._ID));
            name = resultSet.getString(resultSet.getColumnIndex(TelemetryTagEntry.COLUMN_NAME_NAME));
            hash = resultSet.getString(resultSet.getColumnIndex(TelemetryTagEntry.COLUMN_NAME_HASH));
            description = resultSet.getString(resultSet.getColumnIndex(TelemetryTagEntry.COLUMN_NAME_DESCRIPTION));
            startDate = resultSet.getString(resultSet.getColumnIndex(TelemetryTagEntry.COLUMN_NAME_START_DATE));
            endDate = resultSet.getString(resultSet.getColumnIndex(TelemetryTagEntry.COLUMN_NAME_END_DATE));
        }
        return this;
    }

    @Override
    public ContentValues getContentValues() {
        contentValues.clear();
        contentValues.put(TelemetryTagEntry._ID, id);
        contentValues.put(TelemetryTagEntry.COLUMN_NAME_NAME, name);
        contentValues.put(TelemetryTagEntry.COLUMN_NAME_HASH, hash);
        contentValues.put(TelemetryTagEntry.COLUMN_NAME_DESCRIPTION, description);
        contentValues.put(TelemetryTagEntry.COLUMN_NAME_START_DATE, startDate);
        contentValues.put(TelemetryTagEntry.COLUMN_NAME_END_DATE, endDate);
        return contentValues;
    }

    @Override
    public void updateId(long id) {
        this.id = id;
    }

    @Override
    public String getTableName() {
        return TelemetryTagEntry.TABLE_NAME;
    }

    @Override
    public void beforeWrite(AppContext context) {

    }

    @Override
    public String orderBy() {
        return TelemetryTagEntry.COLUMN_NAME_NAME;
    }

    @Override
    public String filterForRead() {
        return "";
    }

    @Override
    public String[] selectionArgsForFilter() {
        return null;
    }

    @Override
    public String limitBy() {
        return "";
    }

    @Override
    public void clean() {

    }

    @Override
    public String selectionToClean() {
        return "";
    }

    public String name() {
        return name;
    }

    public String tagHash() {
        return hash;
    }

    public String description() {
        return description;
    }

    public String startDate() {
        return startDate;
    }

    public String endDate() {
        return endDate;
    }
}
