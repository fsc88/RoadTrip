package edu.upc.fib.roadtriptest;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DBAdapter 
{
	public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHONE = "mobile";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_DATETIME = "date";
    public static final String KEY_TOTAL = "total";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_COST = "cost";
    public static final String KEY_CONTACTS_ID = "contacts_id";
    public static final String KEY_TRIPS_ID = "trips_id";
    public static final String KEY_ITEMS_ID = "items_id";
    private static final String TAG = "DBAdapter";
    
    private static final String DATABASE_NAME = "roadtrip";
    private static final String TABLE_CONTACTS = "contacts";
    private static final String TABLE_REL_CONTRIPS = "rel_contrips";
    private static final String TABLE_TRIPS = "trips";
    private static final String TABLE_ITEMS = "items";
    private static final String TABLE_REL_TRIPITEMS = "rel_tripitems";
    private static final String TABLE_REL_COSTITEMS = "rel_costitems";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_DB_CONTACTS = 
    		"create table "+TABLE_CONTACTS+" (_id integer primary key autoincrement,"
    		+ "name text not null, mobile text, email text );";
    
    private static final String CREATE_DB_TRIPS = 
    		"create table "+TABLE_TRIPS+" (_id integer primary key autoincrement,"
    		+ "name text not null, date datetime not null, total double); ";
    
    private static final String CREATE_DB_CONTRIP = 
    		"create table "+TABLE_REL_CONTRIPS+" (_id integer primary key autoincrement,"
    		+ "contacts_id int not null, trips_id int not null);";
    
    private static final String CREATE_DB_ITEMS = 
    		"create table "+TABLE_ITEMS+" (_id integer primary key autoincrement,"
    		+ "name text not null, description text, date datetime not null, total double); ";
    
    private static final String CREATE_DB_TRIPITEMS = 
    		"create table "+TABLE_REL_TRIPITEMS+" (_id integer primary key autoincrement,"
    		+ "trips_id int not null, items_id int not null);";
    
    private static final String CREATE_DB_COSTITEMS = 
    		"create table "+TABLE_REL_COSTITEMS+" (_id integer primary key autoincrement,"
    		+ "contacts_id int not null, items_id int not null, cost double not null);";
    
        
    private final Context context;  
    
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) 
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
        
    private static class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
            db.execSQL(CREATE_DB_CONTACTS);
            db.execSQL(CREATE_DB_TRIPS);
            db.execSQL(CREATE_DB_CONTRIP); 
            db.execSQL(CREATE_DB_ITEMS);
            db.execSQL(CREATE_DB_TRIPITEMS);
            db.execSQL(CREATE_DB_COSTITEMS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
                              int newVersion) 
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion 
                  + " to "
                  + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_CONTACTS);
            onCreate(db);
        }
    }    
    
    //---opens the database---
    public DBAdapter open() throws SQLException 
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---    
    public void close() 
    {
        DBHelper.close();
    }
    
    // **************************************
    // * Database Contacts                  *
    // **************************************
    
    //---insert a contact into the database---
    public long insertContact(String name, String mobile, String email) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_PHONE, mobile);
        initialValues.put(KEY_EMAIL, email);
        return db.insert(TABLE_CONTACTS, null, initialValues);
    }

    /*
    //---deletes a particular title---
    public boolean deleteTitle(long rowId) 
    {
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    */
    
    public int countContacts(){
    	SQLiteStatement statement = db.compileStatement("SELECT count(*) FROM "+TABLE_CONTACTS);
    	return (int) statement.simpleQueryForLong();
    }

    
    //---retrieves all the titles---
    public Cursor getAllContacts() 
    {
        return db.query(TABLE_CONTACTS, new String[] {
        		KEY_ROWID, 
        		KEY_NAME, 
        		KEY_PHONE,
        		KEY_EMAIL
        		}, 
                null, 
                null, 
                null, 
                null, 
                KEY_NAME);
    }
    
    // Aixo agafa una llista en particular de contactes
    public Cursor getContactList(String idlist)
    {
        return db.query(TABLE_CONTACTS, new String[] {
        		KEY_ROWID, 
        		KEY_NAME, 
        		KEY_PHONE,
        		KEY_EMAIL
        		}, 
                KEY_ROWID + " IN (" + idlist + ")", 
                null, 
                null, 
                null, 
                KEY_NAME);
    }
    
    public String getContactName(int contId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, TABLE_CONTACTS, new String[] {
                		KEY_ROWID,
                		KEY_NAME, 
                		}, 
                		KEY_ROWID + "='" + contId + "'", 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        try{
            return mCursor.getString(mCursor.getColumnIndex(DBAdapter.KEY_NAME));
        }
        catch(Exception ex) {return "";}
    }
    
    public int getContactId(String name) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, TABLE_CONTACTS, new String[] {
                		KEY_ROWID,
                		KEY_NAME, 
                		KEY_PHONE,
                		KEY_EMAIL
                		}, 
                		KEY_NAME + "='" + name + "'", 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        try{
            return mCursor.getInt(mCursor.getColumnIndex(DBAdapter.KEY_ROWID));
        }
        catch(Exception ex) {return -1;}

    }
    
    public String getContactEmail(int contId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, TABLE_CONTACTS, new String[] {
                		KEY_ROWID,
                		KEY_NAME, 
                		KEY_PHONE,
                		KEY_EMAIL
                		}, 
                		KEY_ROWID + "='" + contId + "'", 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        try{
            return mCursor.getString(mCursor.getColumnIndex(DBAdapter.KEY_EMAIL));
        }
        catch(Exception ex) {return "";}
    }
    
    public String getContactPhone(int contId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, TABLE_CONTACTS, new String[] {
                		KEY_ROWID,
                		KEY_NAME, 
                		KEY_PHONE,
                		KEY_EMAIL
                		}, 
                		KEY_ROWID + "='" + contId + "'", 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        try{
            return mCursor.getString(mCursor.getColumnIndex(DBAdapter.KEY_PHONE));
        }
        catch(Exception ex) {return "";}
    }
    
    public boolean updateContact(String oldName, String newName, String email, String phone){
    	ContentValues args = new ContentValues();
        args.put(KEY_NAME, newName);
        args.put(KEY_EMAIL, email);
        args.put(KEY_PHONE, phone);
        return db.update(TABLE_CONTACTS, args, 
                         KEY_NAME + "=" + "'" + oldName + "'", null) > 0;
    }

    // **************************************
    // * Database ConTrips                  *
    // **************************************
    
    public long insertRelConTrip(int contacts_id, int trips_id)
    {
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CONTACTS_ID,contacts_id);
        initialValues.put(KEY_TRIPS_ID, trips_id);
    	return db.insert(TABLE_REL_CONTRIPS, null, initialValues);
    }
    
    public int countTripContacts(int tripId){
    	SQLiteStatement statement = db.compileStatement("SELECT count(*) FROM "+TABLE_REL_CONTRIPS 
    			+ " WHERE "+KEY_TRIPS_ID+" = '"+tripId+"'");
    	return (int) statement.simpleQueryForLong();
    }
    
    // Aixo agafa els contactes d'un trip en particular
    public Cursor getAllTripContacts(int tripId) 
    {
    	Cursor itemListCur = db.query(TABLE_REL_CONTRIPS, new String[] {
        		KEY_ROWID, 
        		KEY_CONTACTS_ID, 
        		KEY_TRIPS_ID,
        		}, 
        		KEY_TRIPS_ID + "='" + tripId + "'", 
                null, 
                null, 
                null, 
                null);
    	String itemListStr = "";
    	itemListCur.moveToFirst();
		do{
			itemListStr += itemListCur.getString(itemListCur.getColumnIndex(KEY_CONTACTS_ID)) + ",";
		}while(itemListCur.moveToNext());
		itemListCur.close();
		itemListStr = itemListStr.substring(0,itemListStr.length()-1);
    	
        return db.query(TABLE_CONTACTS, new String[] {
        		KEY_ROWID, 
        		KEY_NAME
        		}, 
        		KEY_ROWID + " IN(" + itemListStr + ")", 
                null, 
                null, 
                null, 
                KEY_NAME);
    }
    
    
    // **************************************
    // * Database Trips                     *
    // **************************************
    
    public long insertTrip(String name) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        initialValues.put(KEY_DATETIME, dateFormat.format(date) );
        initialValues.put(KEY_TOTAL, 0);
        return db.insert(TABLE_TRIPS, null, initialValues);
    }
    
    public int countTrips(){
    	SQLiteStatement statement = db.compileStatement("SELECT count(*) FROM "+TABLE_TRIPS);
    	return (int) statement.simpleQueryForLong();
    }
    
    public Cursor getAllTrips() 
    {
        return db.query(TABLE_TRIPS, new String[] {
        		KEY_ROWID, 
        		KEY_NAME, 
        		KEY_DATETIME,
        		KEY_TOTAL
        		}, 
                null, 
                null, 
                null, 
                null, 
                KEY_DATETIME +" DESC");
    }
    
    public int getTripId(String name) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, TABLE_TRIPS, new String[] {
                		KEY_ROWID,
                		KEY_NAME, 
                		KEY_DATETIME,
                		KEY_TOTAL
                		}, 
                		KEY_NAME + "='" + name + "'", 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        try{
            return mCursor.getInt(mCursor.getColumnIndex(DBAdapter.KEY_ROWID));
        }
        catch(Exception ex) {return -1;}

    }
    
    public String getTripName(int tripId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, TABLE_TRIPS, new String[] {
                		KEY_ROWID,
                		KEY_NAME
                		}, 
                		KEY_ROWID + "='" + tripId + "'", 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        try{
            return mCursor.getString(mCursor.getColumnIndex(DBAdapter.KEY_NAME));
        }
        catch(Exception ex) {return "";}

    }
    
    public boolean insertTripTotal(int tripId, double total){
    	ContentValues args = new ContentValues();
		args.put(KEY_TOTAL, total);
		return db.update(TABLE_TRIPS, args, 
                KEY_ROWID + "=" + tripId, null) > 0;
    }
    
    public double getTripTotal(int tripId){
    	
    	Cursor itemListCur = db.query(TABLE_REL_TRIPITEMS, new String[] {
        		KEY_ROWID, 
        		KEY_TRIPS_ID, 
        		KEY_ITEMS_ID,
        		}, 
        		KEY_TRIPS_ID + "='" + tripId + "'", 
                null, 
                null, 
                null, 
                null);
    	
    	String itemListStr = "";
    	itemListCur.moveToFirst();
		do{
			itemListStr += itemListCur.getString(itemListCur.getColumnIndex(KEY_ITEMS_ID)) + ",";
		}while(itemListCur.moveToNext());
		itemListCur.close();
		itemListStr = itemListStr.substring(0,itemListStr.length()-1);
		
		double total = 0;
    	Cursor totalCur = db.query(TABLE_ITEMS, new String[] {
        		KEY_ROWID, 
        		KEY_TOTAL
        		}, 
        		KEY_ROWID + " IN(" + itemListStr + ")", 
                null, 
                null, 
                null, 
                null);
		totalCur.moveToFirst();
		do{
			 total += totalCur.getDouble(totalCur.getColumnIndex(KEY_TOTAL));
		}while(totalCur.moveToNext());
		return total;
    }
    
    public double updateTripTotal(int tripId){
    	
    	double total = getTripTotal(tripId);
		
        insertTripTotal(tripId,total);
		
    	return total;
    }
    
    // **************************************
    // * Database Items                     *
    // **************************************
    
    public long insertItem(String name, String description) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_DESCRIPTION, description);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        initialValues.put(KEY_DATETIME, dateFormat.format(date) );
        initialValues.put(KEY_TOTAL, 0);
        return db.insert(TABLE_ITEMS, null, initialValues);
    }
    
    public String getItemName(int itemId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, TABLE_ITEMS, new String[] {
                		KEY_ROWID,
                		KEY_NAME
                		}, 
                		KEY_ROWID + "='" + itemId + "'", 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        try{
            return mCursor.getString(mCursor.getColumnIndex(DBAdapter.KEY_NAME));
        }
        catch(Exception ex) {return "";}

    }
    
    public String getItemDesc(int itemId) throws SQLException 
    {
    	Cursor mCursor =
                db.query(true, TABLE_ITEMS, new String[] {
                		KEY_ROWID,
                		KEY_DESCRIPTION
                		}, 
                		KEY_ROWID + "='" + itemId + "'", 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        try{
            return mCursor.getString(mCursor.getColumnIndex(DBAdapter.KEY_DESCRIPTION));
        }
        catch(Exception ex) {return "";}

    }
    
    public boolean updateItemNoCost(int itemId, String name, String description) 
    {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_DESCRIPTION, description);
        return db.update(TABLE_ITEMS, args, 
                         KEY_ROWID + "=" + itemId, null) > 0;
    }
    
    public double updateItemTotal(int itemId){
    	double total = 0;
    	Cursor  costCur = db.query(TABLE_REL_COSTITEMS, new String[] {
        		KEY_COST, 
        		KEY_ITEMS_ID,
        		}, 
        		KEY_ITEMS_ID + "='" + itemId + "'", 
                null, 
                null, 
                null, 
                null);
    	costCur.moveToFirst();
		do{
			 total += costCur.getDouble(costCur.getColumnIndex(KEY_COST));
		}while(costCur.moveToNext());
		
        ContentValues args = new ContentValues();
		args.put(KEY_TOTAL, total);
		db.update(TABLE_ITEMS, args, 
                KEY_ROWID + "=" + itemId, null);
		
    	return total;
    }
    
    // **************************************
    // * Database TripItems                 *
    // **************************************
    
    public long insertRelTripItems(int trips_id, int items_id)
    {
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TRIPS_ID, trips_id);
        initialValues.put(KEY_ITEMS_ID,items_id);
    	return db.insert(TABLE_REL_TRIPITEMS, null, initialValues);
    }
    
    public int countTripItems(int tripId){
    	SQLiteStatement statement = db.compileStatement("SELECT count(*) FROM "+TABLE_REL_TRIPITEMS 
    			+ " WHERE "+KEY_TRIPS_ID+" = '"+tripId+"'");
    	return (int) statement.simpleQueryForLong();
    }
    
    public Cursor getAllTripItems(int tripId) 
    {
    	Cursor itemListCur = db.query(TABLE_REL_TRIPITEMS, new String[] {
        		KEY_ROWID, 
        		KEY_TRIPS_ID, 
        		KEY_ITEMS_ID,
        		}, 
        		KEY_TRIPS_ID + "='" + tripId + "'", 
                null, 
                null, 
                null, 
                null);
    	String itemListStr = "";
    	itemListCur.moveToFirst();
		do{
			itemListStr += itemListCur.getString(itemListCur.getColumnIndex(KEY_ITEMS_ID)) + ",";
		}while(itemListCur.moveToNext());
		itemListCur.close();
		itemListStr = itemListStr.substring(0,itemListStr.length()-1);
    	
        return db.query(TABLE_ITEMS, new String[] {
        		KEY_ROWID, 
        		KEY_NAME, 
        		KEY_DESCRIPTION,
        		KEY_DATETIME,
        		KEY_TOTAL
        		}, 
        		KEY_ROWID + " IN(" + itemListStr + ")", 
                null, 
                null, 
                null, 
                KEY_DATETIME +" DESC");
    }
    

    // **************************************
    // * Database CostItems                 *
    // **************************************
    
    public long insertRelCostItems(int contId, int itemId, double cost)
    {
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CONTACTS_ID, contId);
        initialValues.put(KEY_ITEMS_ID,itemId);
        initialValues.put(KEY_COST,cost);
    	return db.insert(TABLE_REL_COSTITEMS, null, initialValues);
    }
    
    public boolean deleteItemContacts(int itemId) 
    {
        return db.delete(TABLE_REL_COSTITEMS, KEY_ITEMS_ID + "=" + itemId, null) > 0;
    }
    
    public boolean updateItemContactCost(int itemId, int contId, double cost) 
    {
        ContentValues args = new ContentValues();
        args.put(KEY_COST, cost);
        return db.update(TABLE_REL_COSTITEMS, args, 
        		KEY_ITEMS_ID + "=" + itemId + " AND " + KEY_CONTACTS_ID + "=" + contId,
        		null) > 0;
    }
    
    public boolean contactInItem(int contId, int itemId){
    	SQLiteStatement statement = db.compileStatement("SELECT count(*) FROM "+TABLE_REL_COSTITEMS 
    			+ " WHERE "+KEY_ITEMS_ID + "='" + itemId + "' AND " + KEY_CONTACTS_ID + "='" + contId+"'");
    	return statement.simpleQueryForLong()>0;
    }
    
    public double getItemContactCost(int contId, int itemId){
    	double cost;
    	Cursor costCur = db.query(TABLE_REL_COSTITEMS, new String[] {
        		KEY_COST, 
        		KEY_CONTACTS_ID, 
        		KEY_ITEMS_ID,
        		}, 
        		KEY_ITEMS_ID + "='" + itemId + "' AND " + KEY_CONTACTS_ID + "='" + contId+"'", 
                null, 
                null, 
                null, 
                null);
    	costCur.moveToFirst();
		do{
			 cost = costCur.getDouble(costCur.getColumnIndex(KEY_COST));
		}while(costCur.moveToNext());
    	return cost;
    }
 }
