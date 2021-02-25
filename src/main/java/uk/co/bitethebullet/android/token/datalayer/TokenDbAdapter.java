/*
 * Copyright Mark McAvoy - www.bitethebullet.co.uk 2009 - 2020
 * 
 * This file is part of Android Token.
 *
 * Android Token is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Android Token is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Android Token.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package uk.co.bitethebullet.android.token.datalayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Performs the CRUD database actions for the Android Token application
 */
public class TokenDbAdapter {
	//const holding the table field names
	public static final String KEY_TOKEN_ROWID = "_id";
	public static final String KEY_TOKEN_NAME = "name";
	public static final String KEY_TOKEN_SERIAL = "serial";
	public static final String KEY_TOKEN_SEED = "seed";
	public static final String KEY_TOKEN_COUNT = "eventcount";
	public static final String KEY_TOKEN_TYPE = "tokentype";
	public static final String KEY_TOKEN_OTP_LENGTH = "otplength";
	public static final String KEY_TOKEN_TIME_STEP = "timestep";
	public static final String KEY_TOKEN_NAME_SORT = "namesort";
	public static final String KEY_TOKEN_ORGANISATION = "organisation";
	
	public static final String KEY_PIN_ROWID = "_id";
	public static final String KEY_PIN_HASH = "pinhash";
	
	//const define the different token type
	public static final int TOKEN_TYPE_EVENT = 0;
	public static final int TOKEN_TYPE_TIME = 1;
	
	public static final String TAG = "TokenDbAdapter";
	
	//const database tables, version
	private static final String DATABASE_NAME = "androidtoken.db";
    private static final String DATABASE_TOKEN_TABLE = "token";
    private static final String DATABASE_PIN_TABLE = "pin";
    private static final int DATABASE_VERSION = 2;
    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mContext;
    
    private static final String DATABASE_CREATE_TOKEN = 
    			"create table token (_id integer primary key autoincrement,"
                + " name text not null,"
                + " serial text,"
                + " seed text,"
                + " eventcount integer,"
                + " tokentype integer,"
                + " otplength integer,"
                + " timestep integer,"
                + " namesort text,"
				+ " organisation text);";
    
    private static final String DATABASE_CREATE_PIN =
                "create table pin(_id integer primary key autoincrement,"
                + " pinhash text);";
    
    private static final String DATABASE_DROP_TOKEN = "DROP TABLE IF EXISTS token;";
    private static final String DATABASE_DROP_PIN = "DROP TABLE IF EXISTS pin;";

	private static final String DATABASE_ALTER_TOKEN_1 = "ALTER TABLE "
			+ DATABASE_TOKEN_TABLE + " ADD COLUMN " + KEY_TOKEN_ORGANISATION + " string;";
	
    
    private static class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context){
			super(context,DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_TOKEN);
			db.execSQL(DATABASE_CREATE_PIN);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			if (oldVersion < 2) {
				db.execSQL(DATABASE_ALTER_TOKEN_1);
			}
		}    	
    }
    
    public TokenDbAdapter(Context context){
    	this.mContext = context;
    }
    
    public TokenDbAdapter open() throws SQLException{
    	mDbHelper = new DatabaseHelper(mContext);
    	mDb = mDbHelper.getWritableDatabase();
    	return this;
    }
    
    public void close(){
    	mDbHelper.close();
    }
    
    
    
    
    // Data Access Methods
    ////////////////////////////////
    
    
    //TOKEN TABLE
    public long createToken(String name, String serial, String seed,
							int tokenType, int otpLength, int timeStep,
							String organisation){
    	ContentValues values = new ContentValues();
    	values.put(KEY_TOKEN_NAME, name);
    	values.put(KEY_TOKEN_SERIAL, serial);
    	values.put(KEY_TOKEN_SEED, seed);
    	values.put(KEY_TOKEN_TYPE, tokenType);
    	values.put(KEY_TOKEN_OTP_LENGTH, otpLength);
    	values.put(KEY_TOKEN_TIME_STEP, timeStep);
    	values.put(KEY_TOKEN_COUNT, 0);
    	values.put(KEY_TOKEN_NAME_SORT, name.toLowerCase());
    	values.put(KEY_TOKEN_ORGANISATION, organisation);
    	
    	return mDb.insert(DATABASE_TOKEN_TABLE, null, values);
    }
    
    public boolean deleteToken(long tokenId){
    	return mDb.delete(DATABASE_TOKEN_TABLE, KEY_TOKEN_ROWID + "=" + tokenId, null) > 0;
    }
    
    public boolean renameToken(long tokenId, String name){
    	ContentValues values = new 	ContentValues();
    	values.put(KEY_TOKEN_NAME, name);
    	
    	return mDb.update(DATABASE_TOKEN_TABLE, values, KEY_TOKEN_ROWID + "=" + tokenId, null) > 0;
    }
    
    public void incrementTokenCount(long tokenId){
    	mDb.execSQL("UPDATE token SET eventcount = eventcount + 1 WHERE _id = " + tokenId);
    }
    
    public void setTokenCounter(long tokenId, int eventCounter){
    	mDb.execSQL("UPDATE token SET eventcount = " + eventCounter + "  WHERE _id = " + tokenId);
    }
    
    public Cursor fetchToken(long tokenId){
    	Cursor c = mDb.query(DATABASE_TOKEN_TABLE,
    						 new String[] {KEY_TOKEN_ROWID, KEY_TOKEN_NAME, KEY_TOKEN_SERIAL, KEY_TOKEN_SEED, 
    										KEY_TOKEN_COUNT, KEY_TOKEN_TYPE, KEY_TOKEN_OTP_LENGTH, 
    										KEY_TOKEN_TIME_STEP, KEY_TOKEN_ORGANISATION},
    						 KEY_TOKEN_ROWID + "=" + tokenId, 
    						 null,
    						 null, 
    						 null, 
    						 null);
    	
    	if(c != null)
    		c.moveToFirst();
    	
    	return c;
    }
    
    public Cursor fetchAllTokens(){
    	return mDb.query(DATABASE_TOKEN_TABLE, 
		    			new String[] {KEY_TOKEN_ROWID, KEY_TOKEN_NAME, KEY_TOKEN_SERIAL, KEY_TOKEN_SEED, 
										KEY_TOKEN_COUNT, KEY_TOKEN_TYPE, KEY_TOKEN_OTP_LENGTH, 
										KEY_TOKEN_TIME_STEP, KEY_TOKEN_ORGANISATION},
    				     null, 
    				     null, 
    				     null, 
    				     null, 
    				     KEY_TOKEN_NAME_SORT + " ASC");
    }
    
    
    //PIN TABLE
    public boolean createOrUpdatePin(String pinHash){
    	
    	boolean result = false;
    	
    	ContentValues values = new 	ContentValues();
    	values.put(KEY_PIN_HASH, pinHash);
    	
    	Cursor c = fetchPin();
    	
    	if(c.getCount() == 0){
    		//no pin set, insert new row
    		result = mDb.insert(DATABASE_PIN_TABLE, null, values) > 0;    		
    	}else{
    		//pin already set update existing
    		result = mDb.update(DATABASE_PIN_TABLE, values, null, null) > 0;
    	}
    	
    	c.close();
    	
    	return result;
    }
    
    public void deletePin(){
    	mDb.delete(DATABASE_PIN_TABLE, null, null);
    }
    
    public Cursor fetchPin(){
    	Cursor c = mDb.query(DATABASE_PIN_TABLE,
    					     new String[]{KEY_PIN_HASH}, 
    					     null, 
    					     null, 
    					     null, 
    					     null, 
    					     null,
    					     "1");
    	
    	if(c != null)
    		c.moveToFirst();
    	
    	return c;
    }
    
}
