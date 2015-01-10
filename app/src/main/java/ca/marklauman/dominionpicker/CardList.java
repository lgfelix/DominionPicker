/* Copyright (c) 2014 Mark Christopher Lauman
 * 
 * Licensed under the The MIT License (MIT)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.                                                                  */
package ca.marklauman.dominionpicker;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/** This is the card database. All card information is stored
 *  here, and it is all retrieved from here.
 *  @author Mark Lauman                                    */
public class CardList extends ContentProvider {
	
	/** URI used to access this ContentProvider */
	public static final Uri URI = Uri.parse("content://ca.marklauman.dominionpicker/cards");
	/** Name of the table containing all cards */
	public static final String TABLE_CARDS = "cards";
	/** MIME type for cards */
	public static final String MIME = "ca.marklauman.dominionpicker.card";
	/** Column for the id id number of the card (internal). */
	public static final String _ID = "_id";
	/** Column for the name of a card. */
	public static final String _NAME = "name";
	/** Column for the description of a card. */
	public static final String _DESC = "description";
	/** Column for the cost of a card (in coppers). */
	public static final String _COST = "cost";
	/** Column for the potion cost of a card (# needed to buy). */
	public static final String _POTION = "potion";
	/** Column for the categories of the card
	 *  (action, reaction, etc)               */
	public static final String _CATEGORY = "category";
	/** Column for the expansion set the card belongs to */
	public static final String _EXP = "expansion";
	/** Column for +1 buys this card provides */
	public static final String _BUY = "buy";
	/** Column for +1 actions this card provides. */
	public static final String _ACTION = "act";
	/** Column for +1 cards this card provides. */
	public static final String _DRAW = "draw";
	/** Column for +1 buying power this card provides. */
	public static final String _GOLD = "gold";
	/** Column for the value of this card in victory points. */
	public static final String _VICTORY = "victory";
	/** Column indicating if this card is a curser */
	public static final String _CURSER = "curser";
	
	/** Array of all column names in {@link #TABLE_CARDS}. */
	public static final String[] COLS = {_ID, _NAME, _DESC,
		_COST, _POTION, _CATEGORY, _EXP,
		_BUY, _ACTION, _DRAW, _GOLD, _VICTORY, _CURSER};
	
	/** Create Table sql statement for {@link #TABLE_CARDS}. */
	public static final String CREATE_TABLE =
			"CREATE TABLE "+ TABLE_CARDS + " ("
					+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ _NAME + " TEXT, "
					+ _DESC + " TEXT, "
					+ _COST + " TEXT, "
					+ _POTION + " INTEGER, "
					+ _CATEGORY + " TEXT, "
					+ _EXP + " TEXT, "
					+ _BUY + " TEXT, "
					+ _ACTION + " TEXT, "
					+ _DRAW + " TEXT, "
					+ _GOLD + " TEXT, "
					+ _VICTORY + " TEXT, "
					+ _CURSER + " TEXT"
					+ ");";
	
	/** The _ID value of the Black Market card. */
	public static final long ID_BLACK_MARKET = 1L;
	/** The ID value of the Young Witch card */
	public static final long ID_YOUNG_WITCH = 161L;
	
	/** Handle to the sql database. */
	private DBHandler dbhandle;
	
	@Override
	public boolean onCreate() {
		dbhandle = new DBHandler(getContext());
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
		return MIME;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection,
				String selection, String[] selectionArgs,
				String sortOrder) {
		SQLiteDatabase db = dbhandle.getReadableDatabase();
		if(sortOrder == null)
			sortOrder = _EXP + ", " + _NAME;
		return db.query(TABLE_CARDS, projection,
						selection, selectionArgs,
						null, null, sortOrder);
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Do not allow insertions
		return null;
	}
	
	@Override
	public int update(Uri uri, ContentValues values,
				String selection, String[] selectionArgs) {
		// Do not allow updates
		return 0;
	}
	
	@Override
	public int delete(Uri uri, String selection,
					  String[] selectionArgs) {
		// Do not allow deletions
		return 0;
	}
	
	/** Handles the sql database. */
	private static class DBHandler extends SQLiteOpenHelper {
		Context context;
		
		public DBHandler(Context c) {
			super(c, "cards.db", null,
					c.getResources()
					 .getInteger(R.integer.db_version));
			context = c;
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE);
			
			Resources r = context.getResources();
			addCards(db, r.getStringArray(R.array.cards_promo));
			addCards(db, r.getStringArray(R.array.cards_base));
			addCards(db, r.getStringArray(R.array.cards_alchemy));
			addCards(db, r.getStringArray(R.array.cards_intrigue));
			addCards(db, r.getStringArray(R.array.cards_prosperity));
			addCards(db, r.getStringArray(R.array.cards_seaside));
			addCards(db, r.getStringArray(R.array.cards_dark_ages));
			addCards(db, r.getStringArray(R.array.cards_cornucopia));
			addCards(db, r.getStringArray(R.array.cards_guilds));
			addCards(db, r.getStringArray(R.array.cards_hinterlands));
			addCards(db, r.getString(R.string.card_prince));
		}
		
		private void addCards(SQLiteDatabase db, String... cards) {
			db.beginTransaction();
			for(String card : cards) {
				String[] in_data = card.split(";");
				ContentValues values = new ContentValues();
				for(int i = 0; i < in_data.length; i++) {
					values.put(COLS[i+1], in_data[i]);
				}
				db.insertWithOnConflict(TABLE_CARDS,
							null,
							values,
							SQLiteDatabase.CONFLICT_IGNORE);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db,
						int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARDS);
			onCreate(db);
		}
		
		@Override
		public void onDowngrade(SQLiteDatabase db,
				int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARDS);
			onCreate(db);
		}
	}
}