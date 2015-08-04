/**
 * Copyright 2015-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.mobileconnectors.s3.transferutility;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Provides methods to access database through which applications can interact
 * with transfer tasks.
 */
class TransferDBBase {

    private static final int TRANSFERS = 10;
    private static final int TRANSFER_ID = 20;
    private static final int TRANSFER_PART = 30;
    private static final int TRANSFER_STATE = 40;
    private static final String BASE_PATH = "transfers";
    private final Context context;
    private final Uri contentUri;
    private final UriMatcher uriMatcher;
    private final TransferDatabaseHelper databaseHelper;

    /**
     * Constructs TransferDBBase with the given Context.
     *
     * @param context A Context instance.
     */
    public TransferDBBase(Context context) {
        this.context = context;
        String mAuthority = context.getApplicationContext().getPackageName();
        databaseHelper = new TransferDatabaseHelper(this.context);
        contentUri = Uri.parse("content://" + mAuthority + "/" + BASE_PATH);
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*
         * The Uri of TRANSFERS is for all records in the table.
         */
        uriMatcher.addURI(mAuthority, BASE_PATH, TRANSFERS);

        /*
         * The Uri of TRANSFER_ID is for a single transfer record.
         */
        uriMatcher.addURI(mAuthority, BASE_PATH + "/#", TRANSFER_ID);

        /*
         * The Uri of TRANSFER_PART is for part records of a multipart upload.
         */
        uriMatcher.addURI(mAuthority, BASE_PATH + "/part/#", TRANSFER_PART);

        /*
         * The Uri of TRANSFER_STATE is for records with a specific state.
         */
        uriMatcher.addURI(mAuthority, BASE_PATH + "/state/*", TRANSFER_STATE);
    }

    /**
     * Closes the database helper.
     */
    public void closeDBHelper() {
        databaseHelper.close();
    }

    /**
     * Gets the Uri for the table.
     *
     * @return The Uri for the table.
     */
    public Uri getContentUri() {
        return contentUri;
    }

    /**
     * Inserts a record to the table.
     *
     * @param uri The Uri of a table.
     * @param values The values of a record.
     * @return The Uri of the inserted record.
     */
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case TRANSFERS:
                id = db.insert(TransferTable.TABLE_TRANSFER, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        context.getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    /**
     * Query records from the database.
     *
     * @param uri A Uri indicating which part of data to query.
     * @param projection The projection of columns.
     * @param selection The "where" clause of sql.
     * @param selectionArgs Strings in the "where" clause.
     * @param sortOrder Sorting order of the query.
     * @param type Type of transfers to query.
     * @return A Cursor pointing to records.
     */
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        // TODO: currently all methods calling this pass null to projection.
        // In the future we want to update projection to be more specific for
        // performance and must handle that here.
        queryBuilder.setTables(TransferTable.TABLE_TRANSFER);
        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case TRANSFERS:
                queryBuilder.appendWhere(TransferTable.COLUMN_PART_NUM + "=" + 0);
                break;
            case TRANSFER_ID:
                queryBuilder.appendWhere(TransferTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            case TRANSFER_PART:
                queryBuilder.appendWhere(TransferTable.COLUMN_MAIN_UPLOAD_ID + "="
                        + uri.getLastPathSegment());
                break;
            case TRANSFER_STATE:
                queryBuilder.appendWhere(TransferTable.COLUMN_STATE + "=");
                queryBuilder.appendWhereEscapeString(uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null,
                sortOrder);
        return cursor;
    }

    /**
     * Updates records in the table synchronously.
     *
     * @param uri A Uri of the specific record.
     * @param values The values to update.
     * @param whereClause The "where" clause of sql.
     * @param whereArgs Strings in the "where" clause.
     * @return Number of rows updated.
     */
    public synchronized int update(Uri uri, ContentValues values, String whereClause,
            String[] whereArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case TRANSFERS:
                rowsUpdated = db.update(TransferTable.TABLE_TRANSFER, values, whereClause,
                        whereArgs);
                break;
            case TRANSFER_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(whereClause)) {
                    rowsUpdated = db.update(TransferTable.TABLE_TRANSFER, values,
                            TransferTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = db
                            .update(TransferTable.TABLE_TRANSFER, values, TransferTable.COLUMN_ID
                                    + "=" + id + " and " + whereClause, whereArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (rowsUpdated > 0) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    /**
     * Deletes a record in the table.
     *
     * @param uri A Uri of the specific record.
     * @param selection The "where" clause of sql.
     * @param selectionArgs Strings in the "where" clause.
     * @return Number of rows deleted.
     */
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case TRANSFERS:
                rowsDeleted = db.delete(TransferTable.TABLE_TRANSFER, selection, selectionArgs);
                break;
            case TRANSFER_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(TransferTable.TABLE_TRANSFER,
                            TransferTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = db
                            .delete(TransferTable.TABLE_TRANSFER, TransferTable.COLUMN_ID + "="
                                    + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        context.getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    /**
     * @param uri The Uri of a table.
     * @param valuesArray A array of values to insert.
     * @return Number of rows inserted.
     */
    public int bulkInsert(Uri uri, ContentValues[] valuesArray) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int mainUploadId = 0;
        switch (uriType) {
            case TRANSFERS:
                try {
                    db.beginTransaction();
                    mainUploadId = (int) db.insertOrThrow(TransferTable.TABLE_TRANSFER, null,
                            valuesArray[0]);
                    for (int i = 1; i < valuesArray.length; i++) {
                        valuesArray[i].put(TransferTable.COLUMN_MAIN_UPLOAD_ID, mainUploadId);
                        db.insertOrThrow(TransferTable.TABLE_TRANSFER, null, valuesArray[i]);
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    Log.e(TransferDBBase.class.getSimpleName(),
                            "bulkInsert error : " + e.getMessage());
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        context.getContentResolver().notifyChange(uri, null);
        return mainUploadId;
    }
}
