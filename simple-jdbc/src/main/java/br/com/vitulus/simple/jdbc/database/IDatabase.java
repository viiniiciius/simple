package br.com.vitulus.simple.jdbc.database;

import java.awt.Cursor;
import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public interface IDatabase {

	/**
	 * Begins a transaction. Transactions can be nested. When the outer transaction is ended all of
	 * the work done in that transaction and all of the nested transactions will be committed or
	 * rolled back. The changes will be rolled back if any transaction is ended without being
	 * marked as clean (by calling setTransactionSuccessful). Otherwise they will be committed.
	 *
	 * <p>Here is the standard idiom for transactions:
	 *
	 * <pre>
	 *   db.beginTransaction();
	 *   try {
	 *     ...
	 *     db.setTransactionSuccessful();
	 *   } finally {
	 *     db.endTransaction();
	 *   }
	 * </pre>
	 */
	public abstract void beginTransaction();

	/**
	 * Begins a transaction. Transactions can be nested. When the outer transaction is ended all of
	 * the work done in that transaction and all of the nested transactions will be committed or
	 * rolled back. The changes will be rolled back if any transaction is ended without being
	 * marked as clean (by calling setTransactionSuccessful). Otherwise they will be committed.
	 *
	 * <p>Here is the standard idiom for transactions:
	 *
	 * <pre>
	 *   db.beginTransactionWithListener(listener);
	 *   try {
	 *     ...
	 *     db.setTransactionSuccessful();
	 *   } finally {
	 *     db.endTransaction();
	 *   }
	 * </pre>
	 * @param transactionListener listener that should be notified when the transaction begins,
	 * commits, or is rolled back, either explicitly or by a call to
	 * {@link #yieldIfContendedSafely}.
	 */
	public abstract void beginTransactionWithListener(TransactionListener transactionListener);

	/**
	 * End a transaction. See beginTransaction for notes about how to use this and when transactions
	 * are committed and rolled back.
	 */
	public abstract void endTransaction();

	/**
	 * Marks the current transaction as successful. Do not do any more database work between
	 * calling this and calling endTransaction. Do as little non-database work as possible in that
	 * situation too. If any errors are encountered between this and endTransaction the transaction
	 * will still be committed.
	 *
	 * @throws IllegalStateException if the current thread is not in a transaction or the
	 * transaction is already marked as successful.
	 */
	public abstract void setTransactionSuccessful();

	/**
	 * return true if there is a transaction pending
	 */
	public abstract boolean inTransaction();

	/**
	 * Close the database.
	 */
	public abstract void close();

	/**
	 * Query the given URL, returning a {@link Cursor} over the result set.
	 *
	 * @param distinct true if you want each row to be unique, false otherwise.
	 * @param table The table name to compile the query against.
	 * @param columns A list of which columns to return. Passing null will
	 *            return all columns, which is discouraged to prevent reading
	 *            data from storage that isn't going to be used.
	 * @param selection A filter declaring which rows to return, formatted as an
	 *            SQL WHERE clause (excluding the WHERE itself). Passing null
	 *            will return all rows for the given table.
	 * @param selectionArgs You may include ?s in selection, which will be
	 *         replaced by the values from selectionArgs, in order that they
	 *         appear in the selection. The values will be bound as Strings.
	 * @param groupBy A filter declaring how to group rows, formatted as an SQL
	 *            GROUP BY clause (excluding the GROUP BY itself). Passing null
	 *            will cause the rows to not be grouped.
	 * @param having A filter declare which row groups to include in the cursor,
	 *            if row grouping is being used, formatted as an SQL HAVING
	 *            clause (excluding the HAVING itself). Passing null will cause
	 *            all row groups to be included, and is required when row
	 *            grouping is not being used.
	 * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause
	 *            (excluding the ORDER BY itself). Passing null will use the
	 *            default sort order, which may be unordered.
	 * @param limit Limits the number of rows returned by the query,
	 *            formatted as LIMIT clause. Passing null denotes no LIMIT clause.
	 * @return A {@link Cursor} object, which is positioned before the first entry. Note that
	 * {@link Cursor}s are not synchronized, see the documentation for more details.
	 * @see Cursor
	 */
	public abstract ResultSet query(boolean distinct, String table,
			String[] columns, String selection, Object[] selectionArgs,
			String groupBy, String having, String orderBy, String limit);

	/**
	 * Query the given table, returning a {@link Cursor} over the result set.
	 *
	 * @param table The table name to compile the query against.
	 * @param columns A list of which columns to return. Passing null will
	 *            return all columns, which is discouraged to prevent reading
	 *            data from storage that isn't going to be used.
	 * @param selection A filter declaring which rows to return, formatted as an
	 *            SQL WHERE clause (excluding the WHERE itself). Passing null
	 *            will return all rows for the given table.
	 * @param selectionArgs You may include ?s in selection, which will be
	 *         replaced by the values from selectionArgs, in order that they
	 *         appear in the selection. The values will be bound as Strings.
	 * @param groupBy A filter declaring how to group rows, formatted as an SQL
	 *            GROUP BY clause (excluding the GROUP BY itself). Passing null
	 *            will cause the rows to not be grouped.
	 * @param having A filter declare which row groups to include in the cursor,
	 *            if row grouping is being used, formatted as an SQL HAVING
	 *            clause (excluding the HAVING itself). Passing null will cause
	 *            all row groups to be included, and is required when row
	 *            grouping is not being used.
	 * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause
	 *            (excluding the ORDER BY itself). Passing null will use the
	 *            default sort order, which may be unordered.
	 * @return A {@link Cursor} object, which is positioned before the first entry. Note that
	 * {@link Cursor}s are not synchronized, see the documentation for more details.
	 * @see Cursor
	 */
	public abstract ResultSet query(String table, String[] columns,
			String selection, Object[] selectionArgs, String groupBy,
			String having, String orderBy);

	/**
	 * Query the given table, returning a {@link Cursor} over the result set.
	 *
	 * @param table The table name to compile the query against.
	 * @param columns A list of which columns to return. Passing null will
	 *            return all columns, which is discouraged to prevent reading
	 *            data from storage that isn't going to be used.
	 * @param selection A filter declaring which rows to return, formatted as an
	 *            SQL WHERE clause (excluding the WHERE itself). Passing null
	 *            will return all rows for the given table.
	 * @param selectionArgs You may include ?s in selection, which will be
	 *         replaced by the values from selectionArgs, in order that they
	 *         appear in the selection. The values will be bound as Strings.
	 * @param groupBy A filter declaring how to group rows, formatted as an SQL
	 *            GROUP BY clause (excluding the GROUP BY itself). Passing null
	 *            will cause the rows to not be grouped.
	 * @param having A filter declare which row groups to include in the cursor,
	 *            if row grouping is being used, formatted as an SQL HAVING
	 *            clause (excluding the HAVING itself). Passing null will cause
	 *            all row groups to be included, and is required when row
	 *            grouping is not being used.
	 * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause
	 *            (excluding the ORDER BY itself). Passing null will use the
	 *            default sort order, which may be unordered.
	 * @param limit Limits the number of rows returned by the query,
	 *            formatted as LIMIT clause. Passing null denotes no LIMIT clause.
	 * @return A {@link Cursor} object, which is positioned before the first entry. Note that
	 * {@link Cursor}s are not synchronized, see the documentation for more details.
	 * @see Cursor
	 */
	public abstract ResultSet query(String table, String[] columns,
			String selection, Object[] selectionArgs, String groupBy,
			String having, String orderBy, String limit);

	/**
	 * Runs the provided SQL and returns a {@link Cursor} over the result set.
	 *
	 * @param sql the SQL query. The SQL string must not be ; terminated
	 * @param selectionArgs You may include ?s in where clause in the query,
	 *     which will be replaced by the values from selectionArgs. The
	 *     values will be bound as Strings.
	 * @return A {@link Cursor} object, which is positioned before the first entry. Note that
	 * {@link Cursor}s are not synchronized, see the documentation for more details.
	 * @throws SQLException 
	 */
	public abstract ResultSet rawQuery(String sql, Object... selectionArgs) throws SQLException;

	/**
	 * Convenience method for inserting a row into the database.
	 *
	 * @param table the table to insert the row into
	 * @param nullColumnHack optional; may be <code>null</code>.
	 *            SQL doesn't allow inserting a completely empty row without
	 *            naming at least one column name.  If your provided <code>values</code> is
	 *            empty, no column names are known and an empty row can't be inserted.
	 *            If not set to null, the <code>nullColumnHack</code> parameter
	 *            provides the name of nullable column name to explicitly insert a NULL into
	 *            in the case where your <code>values</code> is empty.
	 * @param values this map contains the initial column values for the
	 *            row. The keys should be the column names and the values the
	 *            column values
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public abstract long insert(String table, String nullColumnHack, ContentValues values);

	/**
	 * Convenience method for inserting a row into the database.
	 *
	 * @param table the table to insert the row into
	 * @param nullColumnHack optional; may be <code>null</code>.
	 *            SQL doesn't allow inserting a completely empty row without
	 *            naming at least one column name.  If your provided <code>values</code> is
	 *            empty, no column names are known and an empty row can't be inserted.
	 *            If not set to null, the <code>nullColumnHack</code> parameter
	 *            provides the name of nullable column name to explicitly insert a NULL into
	 *            in the case where your <code>values</code> is empty.
	 * @param values this map contains the initial column values for the
	 *            row. The keys should be the column names and the values the
	 *            column values
	 * @throws SQLException
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public abstract long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws SQLException;

	/**
	 * Convenience method for deleting rows in the database.
	 *
	 * @param table the table to delete from
	 * @param whereClause the optional WHERE clause to apply when deleting.
	 *            Passing null will delete all rows.
	 * @return the number of rows affected if a whereClause is passed in, 0
	 *         otherwise. To remove all rows and get a count pass "1" as the
	 *         whereClause.
	 */
	public abstract int delete(String table, String whereClause, Object[] whereArgs);

	/**
	 * Convenience method for updating rows in the database.
	 *
	 * @param table the table to update in
	 * @param values a map from column names to new column values. null is a
	 *            valid value that will be translated to NULL.
	 * @param whereClause the optional WHERE clause to apply when updating.
	 *            Passing null will update all rows.
	 * @return the number of rows affected
	 */
	public abstract int update(String table, ContentValues values, String whereClause, Object[] whereArgs);

	/**
	 * Execute a single SQL statement that is not a query. For example, CREATE
	 * TABLE, DELETE, INSERT, etc. Multiple statements separated by semicolons are not
	 * supported.  Takes a write lock.
	 *
	 * @throws SQLException if the SQL string is invalid
	 */
	public abstract int execSQL(String sql) throws SQLException;

	/**
	 * Execute a single SQL statement that is not a query. For example, CREATE
	 * TABLE, DELETE, INSERT, etc. Multiple statements separated by semicolons are not
	 * supported.  Takes a write lock.
	 *
	 * @param sql
	 * @param bindArgs only byte[], String, Long and Double are supported in bindArgs.
	 * @throws SQLException if the SQL string is invalid
	 */
	public abstract int execSQL(String sql, Object... bindArgs)	throws SQLException;

	/**
	 * @return true if the DB is currently open (has not been closed)
	 */
	public abstract boolean isOpen();
	
	public IDatabase initialize(DatabaseContext context);	
	
	public class DatabaseContext{
		Map<String,Object> properties = new HashMap<String, Object>();		
		public DatabaseContext() {
		}		
		@SuppressWarnings("unchecked")
		public <T> T getProperty(String key){
			return (T) properties.get(key);
		}
		public void setProperty(String key,Object value){
			properties.put(key, value);
		}
	}	
	
	public class DatabaseFactory{
		static Map<String,DatabaseContext> map = new HashMap<String, DatabaseContext>();
		public static void registerSchema(String schema,DatabaseContext context){
			map.put(schema, context);
		}
		@SuppressWarnings("unchecked")
		public static <T extends IDatabase> T getDatabase(String schema){
			try {
				DatabaseContext context = map.get(schema);
				Class<? extends IDatabase> classe = context.getProperty("class");
				Constructor<? extends IDatabase> c = classe.getDeclaredConstructor();
				c.setAccessible(true);
				IDatabase db = c.newInstance();
				db.initialize(context);
				return (T) db;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
}