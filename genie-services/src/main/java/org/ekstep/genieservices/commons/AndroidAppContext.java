package org.ekstep.genieservices.commons;

import android.content.Context;

import org.ekstep.genieservices.commons.db.SummarizerDBContext;
import org.ekstep.genieservices.commons.db.operations.IDBSession;
import org.ekstep.genieservices.commons.db.operations.impl.SQLiteSession;
import org.ekstep.genieservices.commons.network.AndroidHttpClient;
import org.ekstep.genieservices.commons.network.AndroidNetworkConnectivity;
import org.ekstep.genieservices.commons.network.IConnectionInfo;
import org.ekstep.genieservices.commons.network.IHttpClient;
import org.ekstep.genieservices.commons.network.auth.BasicAuthenticator;

/**
 * Created on 18/4/17.
 */
public class AndroidAppContext extends AppContext<Context, AndroidLogger> {

    private IDBSession mDBSession;
    private IDBSession mSummarizerDBSession;
    private IConnectionInfo mConnectionInfo;
    private IHttpClient mHttpClient;

    private AndroidAppContext(Context context, String appPackage, String key, AndroidLogger logger) {
        super(context, appPackage, key, logger);
    }

    public static AppContext buildAppContext(Context context, String appPackage, String key, AndroidLogger logger) {
        AndroidAppContext appContext = new AndroidAppContext(context, appPackage, key, logger);
        appContext.setDBSession(new SQLiteSession(appContext));
        appContext.setSummarizerDBSession(new SQLiteSession(appContext, new SummarizerDBContext()));
        appContext.setConnectionInfo(new AndroidNetworkConnectivity(appContext));
        appContext.setHttpClient(new AndroidHttpClient(new BasicAuthenticator()));
        return appContext;
    }

    @Override
    public IDBSession getDBSession() {
        return mDBSession;
    }

    private void setDBSession(IDBSession dbSession) {
        this.mDBSession = dbSession;
    }

    @Override
    public IDBSession getSummarizerDBSession() {
        return mSummarizerDBSession;
    }

    private void setSummarizerDBSession(IDBSession dbSession) {
        this.mSummarizerDBSession = dbSession;
    }

    @Override
    public IConnectionInfo getConnectionInfo() {
        return mConnectionInfo;
    }

    private void setConnectionInfo(IConnectionInfo connectionInfo) {
        this.mConnectionInfo = connectionInfo;
    }

    @Override
    public IHttpClient getHttpClient() {
        return mHttpClient;
    }

    private void setHttpClient(IHttpClient client) {
        this.mHttpClient = client;
    }

}