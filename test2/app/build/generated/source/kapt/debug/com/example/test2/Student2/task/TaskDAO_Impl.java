package com.example.test2.Student2.task;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TaskDAO_Impl implements TaskDAO {
  private final RoomDatabase __db;

  private final EntityDeletionOrUpdateAdapter<Task> __deletionAdapterOfTask;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatus;

  private final EntityUpsertionAdapter<Task> __upsertionAdapterOfTask;

  public TaskDAO_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__deletionAdapterOfTask = new EntityDeletionOrUpdateAdapter<Task>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `task` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Task entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM Task";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE task SET status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfTask = new EntityUpsertionAdapter<Task>(new EntityInsertionAdapter<Task>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `task` (`id`,`name`,`description`,`categoryId`,`recurring`,`repeatInterval`,`repeatUnit`,`repeatStart`,`repeatEnd`,`executeAt`,`difficulty`,`importance`,`status`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Task entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDescription());
        }
        statement.bindLong(4, entity.getCategoryId());
        final int _tmp = entity.isRecurring() ? 1 : 0;
        statement.bindLong(5, _tmp);
        if (entity.getRepeatInterval() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getRepeatInterval());
        }
        if (entity.getRepeatUnit() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, __RepeatUnit_enumToString(entity.getRepeatUnit()));
        }
        if (entity.getRepeatStart() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getRepeatStart());
        }
        if (entity.getRepeatEnd() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getRepeatEnd());
        }
        if (entity.getExecuteAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getExecuteAt());
        }
        if (entity.getDifficulty() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, __Difficulty_enumToString(entity.getDifficulty()));
        }
        if (entity.getImportance() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, __Importance_enumToString(entity.getImportance()));
        }
        if (entity.getStatus() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, __Status_enumToString(entity.getStatus()));
        }
      }
    }, new EntityDeletionOrUpdateAdapter<Task>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `task` SET `id` = ?,`name` = ?,`description` = ?,`categoryId` = ?,`recurring` = ?,`repeatInterval` = ?,`repeatUnit` = ?,`repeatStart` = ?,`repeatEnd` = ?,`executeAt` = ?,`difficulty` = ?,`importance` = ?,`status` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Task entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDescription());
        }
        statement.bindLong(4, entity.getCategoryId());
        final int _tmp = entity.isRecurring() ? 1 : 0;
        statement.bindLong(5, _tmp);
        if (entity.getRepeatInterval() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getRepeatInterval());
        }
        if (entity.getRepeatUnit() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, __RepeatUnit_enumToString(entity.getRepeatUnit()));
        }
        if (entity.getRepeatStart() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getRepeatStart());
        }
        if (entity.getRepeatEnd() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getRepeatEnd());
        }
        if (entity.getExecuteAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getExecuteAt());
        }
        if (entity.getDifficulty() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, __Difficulty_enumToString(entity.getDifficulty()));
        }
        if (entity.getImportance() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, __Importance_enumToString(entity.getImportance()));
        }
        if (entity.getStatus() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, __Status_enumToString(entity.getStatus()));
        }
        statement.bindLong(14, entity.getId());
      }
    });
  }

  @Override
  public void delete(final Task task) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfTask.handle(task);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public void updateStatus(final int id, final Task.Status status) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatus.acquire();
    int _argIndex = 1;
    if (status == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, __Status_enumToString(status));
    }
    _argIndex = 2;
    _stmt.bindLong(_argIndex, id);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfUpdateStatus.release(_stmt);
    }
  }

  @Override
  public void upsert(final Task task) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __upsertionAdapterOfTask.upsert(task);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Task> getAll() {
    final String _sql = "SELECT * FROM Task";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
      final int _cursorIndexOfRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "recurring");
      final int _cursorIndexOfRepeatInterval = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatInterval");
      final int _cursorIndexOfRepeatUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatUnit");
      final int _cursorIndexOfRepeatStart = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatStart");
      final int _cursorIndexOfRepeatEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatEnd");
      final int _cursorIndexOfExecuteAt = CursorUtil.getColumnIndexOrThrow(_cursor, "executeAt");
      final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
      final int _cursorIndexOfImportance = CursorUtil.getColumnIndexOrThrow(_cursor, "importance");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final List<Task> _result = new ArrayList<Task>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Task _item;
        _item = new Task();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _item.setName(_tmpName);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        _item.setDescription(_tmpDescription);
        final int _tmpCategoryId;
        _tmpCategoryId = _cursor.getInt(_cursorIndexOfCategoryId);
        _item.setCategoryId(_tmpCategoryId);
        final boolean _tmpRecurring;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfRecurring);
        _tmpRecurring = _tmp != 0;
        _item.setRecurring(_tmpRecurring);
        final Integer _tmpRepeatInterval;
        if (_cursor.isNull(_cursorIndexOfRepeatInterval)) {
          _tmpRepeatInterval = null;
        } else {
          _tmpRepeatInterval = _cursor.getInt(_cursorIndexOfRepeatInterval);
        }
        _item.setRepeatInterval(_tmpRepeatInterval);
        final Task.RepeatUnit _tmpRepeatUnit;
        if (_cursor.isNull(_cursorIndexOfRepeatUnit)) {
          _tmpRepeatUnit = null;
        } else {
          _tmpRepeatUnit = __RepeatUnit_stringToEnum(_cursor.getString(_cursorIndexOfRepeatUnit));
        }
        _item.setRepeatUnit(_tmpRepeatUnit);
        final String _tmpRepeatStart;
        if (_cursor.isNull(_cursorIndexOfRepeatStart)) {
          _tmpRepeatStart = null;
        } else {
          _tmpRepeatStart = _cursor.getString(_cursorIndexOfRepeatStart);
        }
        _item.setRepeatStart(_tmpRepeatStart);
        final String _tmpRepeatEnd;
        if (_cursor.isNull(_cursorIndexOfRepeatEnd)) {
          _tmpRepeatEnd = null;
        } else {
          _tmpRepeatEnd = _cursor.getString(_cursorIndexOfRepeatEnd);
        }
        _item.setRepeatEnd(_tmpRepeatEnd);
        final Long _tmpExecuteAt;
        if (_cursor.isNull(_cursorIndexOfExecuteAt)) {
          _tmpExecuteAt = null;
        } else {
          _tmpExecuteAt = _cursor.getLong(_cursorIndexOfExecuteAt);
        }
        _item.setExecuteAt(_tmpExecuteAt);
        final Task.Difficulty _tmpDifficulty;
        if (_cursor.isNull(_cursorIndexOfDifficulty)) {
          _tmpDifficulty = null;
        } else {
          _tmpDifficulty = __Difficulty_stringToEnum(_cursor.getString(_cursorIndexOfDifficulty));
        }
        _item.setDifficulty(_tmpDifficulty);
        final Task.Importance _tmpImportance;
        if (_cursor.isNull(_cursorIndexOfImportance)) {
          _tmpImportance = null;
        } else {
          _tmpImportance = __Importance_stringToEnum(_cursor.getString(_cursorIndexOfImportance));
        }
        _item.setImportance(_tmpImportance);
        final Task.Status _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = __Status_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
        }
        _item.setStatus(_tmpStatus);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Task getById(final int id) {
    final String _sql = "SELECT * FROM Task WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
      final int _cursorIndexOfRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "recurring");
      final int _cursorIndexOfRepeatInterval = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatInterval");
      final int _cursorIndexOfRepeatUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatUnit");
      final int _cursorIndexOfRepeatStart = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatStart");
      final int _cursorIndexOfRepeatEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatEnd");
      final int _cursorIndexOfExecuteAt = CursorUtil.getColumnIndexOrThrow(_cursor, "executeAt");
      final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
      final int _cursorIndexOfImportance = CursorUtil.getColumnIndexOrThrow(_cursor, "importance");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final Task _result;
      if (_cursor.moveToFirst()) {
        _result = new Task();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _result.setId(_tmpId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _result.setName(_tmpName);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        _result.setDescription(_tmpDescription);
        final int _tmpCategoryId;
        _tmpCategoryId = _cursor.getInt(_cursorIndexOfCategoryId);
        _result.setCategoryId(_tmpCategoryId);
        final boolean _tmpRecurring;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfRecurring);
        _tmpRecurring = _tmp != 0;
        _result.setRecurring(_tmpRecurring);
        final Integer _tmpRepeatInterval;
        if (_cursor.isNull(_cursorIndexOfRepeatInterval)) {
          _tmpRepeatInterval = null;
        } else {
          _tmpRepeatInterval = _cursor.getInt(_cursorIndexOfRepeatInterval);
        }
        _result.setRepeatInterval(_tmpRepeatInterval);
        final Task.RepeatUnit _tmpRepeatUnit;
        if (_cursor.isNull(_cursorIndexOfRepeatUnit)) {
          _tmpRepeatUnit = null;
        } else {
          _tmpRepeatUnit = __RepeatUnit_stringToEnum(_cursor.getString(_cursorIndexOfRepeatUnit));
        }
        _result.setRepeatUnit(_tmpRepeatUnit);
        final String _tmpRepeatStart;
        if (_cursor.isNull(_cursorIndexOfRepeatStart)) {
          _tmpRepeatStart = null;
        } else {
          _tmpRepeatStart = _cursor.getString(_cursorIndexOfRepeatStart);
        }
        _result.setRepeatStart(_tmpRepeatStart);
        final String _tmpRepeatEnd;
        if (_cursor.isNull(_cursorIndexOfRepeatEnd)) {
          _tmpRepeatEnd = null;
        } else {
          _tmpRepeatEnd = _cursor.getString(_cursorIndexOfRepeatEnd);
        }
        _result.setRepeatEnd(_tmpRepeatEnd);
        final Long _tmpExecuteAt;
        if (_cursor.isNull(_cursorIndexOfExecuteAt)) {
          _tmpExecuteAt = null;
        } else {
          _tmpExecuteAt = _cursor.getLong(_cursorIndexOfExecuteAt);
        }
        _result.setExecuteAt(_tmpExecuteAt);
        final Task.Difficulty _tmpDifficulty;
        if (_cursor.isNull(_cursorIndexOfDifficulty)) {
          _tmpDifficulty = null;
        } else {
          _tmpDifficulty = __Difficulty_stringToEnum(_cursor.getString(_cursorIndexOfDifficulty));
        }
        _result.setDifficulty(_tmpDifficulty);
        final Task.Importance _tmpImportance;
        if (_cursor.isNull(_cursorIndexOfImportance)) {
          _tmpImportance = null;
        } else {
          _tmpImportance = __Importance_stringToEnum(_cursor.getString(_cursorIndexOfImportance));
        }
        _result.setImportance(_tmpImportance);
        final Task.Status _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = __Status_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
        }
        _result.setStatus(_tmpStatus);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<Task> getOneOff() {
    final String _sql = "SELECT * FROM task WHERE recurring = 0 ORDER BY executeAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
      final int _cursorIndexOfRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "recurring");
      final int _cursorIndexOfRepeatInterval = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatInterval");
      final int _cursorIndexOfRepeatUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatUnit");
      final int _cursorIndexOfRepeatStart = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatStart");
      final int _cursorIndexOfRepeatEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatEnd");
      final int _cursorIndexOfExecuteAt = CursorUtil.getColumnIndexOrThrow(_cursor, "executeAt");
      final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
      final int _cursorIndexOfImportance = CursorUtil.getColumnIndexOrThrow(_cursor, "importance");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final List<Task> _result = new ArrayList<Task>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Task _item;
        _item = new Task();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _item.setName(_tmpName);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        _item.setDescription(_tmpDescription);
        final int _tmpCategoryId;
        _tmpCategoryId = _cursor.getInt(_cursorIndexOfCategoryId);
        _item.setCategoryId(_tmpCategoryId);
        final boolean _tmpRecurring;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfRecurring);
        _tmpRecurring = _tmp != 0;
        _item.setRecurring(_tmpRecurring);
        final Integer _tmpRepeatInterval;
        if (_cursor.isNull(_cursorIndexOfRepeatInterval)) {
          _tmpRepeatInterval = null;
        } else {
          _tmpRepeatInterval = _cursor.getInt(_cursorIndexOfRepeatInterval);
        }
        _item.setRepeatInterval(_tmpRepeatInterval);
        final Task.RepeatUnit _tmpRepeatUnit;
        if (_cursor.isNull(_cursorIndexOfRepeatUnit)) {
          _tmpRepeatUnit = null;
        } else {
          _tmpRepeatUnit = __RepeatUnit_stringToEnum(_cursor.getString(_cursorIndexOfRepeatUnit));
        }
        _item.setRepeatUnit(_tmpRepeatUnit);
        final String _tmpRepeatStart;
        if (_cursor.isNull(_cursorIndexOfRepeatStart)) {
          _tmpRepeatStart = null;
        } else {
          _tmpRepeatStart = _cursor.getString(_cursorIndexOfRepeatStart);
        }
        _item.setRepeatStart(_tmpRepeatStart);
        final String _tmpRepeatEnd;
        if (_cursor.isNull(_cursorIndexOfRepeatEnd)) {
          _tmpRepeatEnd = null;
        } else {
          _tmpRepeatEnd = _cursor.getString(_cursorIndexOfRepeatEnd);
        }
        _item.setRepeatEnd(_tmpRepeatEnd);
        final Long _tmpExecuteAt;
        if (_cursor.isNull(_cursorIndexOfExecuteAt)) {
          _tmpExecuteAt = null;
        } else {
          _tmpExecuteAt = _cursor.getLong(_cursorIndexOfExecuteAt);
        }
        _item.setExecuteAt(_tmpExecuteAt);
        final Task.Difficulty _tmpDifficulty;
        if (_cursor.isNull(_cursorIndexOfDifficulty)) {
          _tmpDifficulty = null;
        } else {
          _tmpDifficulty = __Difficulty_stringToEnum(_cursor.getString(_cursorIndexOfDifficulty));
        }
        _item.setDifficulty(_tmpDifficulty);
        final Task.Importance _tmpImportance;
        if (_cursor.isNull(_cursorIndexOfImportance)) {
          _tmpImportance = null;
        } else {
          _tmpImportance = __Importance_stringToEnum(_cursor.getString(_cursorIndexOfImportance));
        }
        _item.setImportance(_tmpImportance);
        final Task.Status _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = __Status_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
        }
        _item.setStatus(_tmpStatus);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<Task> getRecurring() {
    final String _sql = "SELECT * FROM task WHERE recurring = 1 ORDER BY COALESCE(repeatStart, executeAt) ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
      final int _cursorIndexOfRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "recurring");
      final int _cursorIndexOfRepeatInterval = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatInterval");
      final int _cursorIndexOfRepeatUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatUnit");
      final int _cursorIndexOfRepeatStart = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatStart");
      final int _cursorIndexOfRepeatEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatEnd");
      final int _cursorIndexOfExecuteAt = CursorUtil.getColumnIndexOrThrow(_cursor, "executeAt");
      final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
      final int _cursorIndexOfImportance = CursorUtil.getColumnIndexOrThrow(_cursor, "importance");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final List<Task> _result = new ArrayList<Task>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Task _item;
        _item = new Task();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _item.setName(_tmpName);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        _item.setDescription(_tmpDescription);
        final int _tmpCategoryId;
        _tmpCategoryId = _cursor.getInt(_cursorIndexOfCategoryId);
        _item.setCategoryId(_tmpCategoryId);
        final boolean _tmpRecurring;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfRecurring);
        _tmpRecurring = _tmp != 0;
        _item.setRecurring(_tmpRecurring);
        final Integer _tmpRepeatInterval;
        if (_cursor.isNull(_cursorIndexOfRepeatInterval)) {
          _tmpRepeatInterval = null;
        } else {
          _tmpRepeatInterval = _cursor.getInt(_cursorIndexOfRepeatInterval);
        }
        _item.setRepeatInterval(_tmpRepeatInterval);
        final Task.RepeatUnit _tmpRepeatUnit;
        if (_cursor.isNull(_cursorIndexOfRepeatUnit)) {
          _tmpRepeatUnit = null;
        } else {
          _tmpRepeatUnit = __RepeatUnit_stringToEnum(_cursor.getString(_cursorIndexOfRepeatUnit));
        }
        _item.setRepeatUnit(_tmpRepeatUnit);
        final String _tmpRepeatStart;
        if (_cursor.isNull(_cursorIndexOfRepeatStart)) {
          _tmpRepeatStart = null;
        } else {
          _tmpRepeatStart = _cursor.getString(_cursorIndexOfRepeatStart);
        }
        _item.setRepeatStart(_tmpRepeatStart);
        final String _tmpRepeatEnd;
        if (_cursor.isNull(_cursorIndexOfRepeatEnd)) {
          _tmpRepeatEnd = null;
        } else {
          _tmpRepeatEnd = _cursor.getString(_cursorIndexOfRepeatEnd);
        }
        _item.setRepeatEnd(_tmpRepeatEnd);
        final Long _tmpExecuteAt;
        if (_cursor.isNull(_cursorIndexOfExecuteAt)) {
          _tmpExecuteAt = null;
        } else {
          _tmpExecuteAt = _cursor.getLong(_cursorIndexOfExecuteAt);
        }
        _item.setExecuteAt(_tmpExecuteAt);
        final Task.Difficulty _tmpDifficulty;
        if (_cursor.isNull(_cursorIndexOfDifficulty)) {
          _tmpDifficulty = null;
        } else {
          _tmpDifficulty = __Difficulty_stringToEnum(_cursor.getString(_cursorIndexOfDifficulty));
        }
        _item.setDifficulty(_tmpDifficulty);
        final Task.Importance _tmpImportance;
        if (_cursor.isNull(_cursorIndexOfImportance)) {
          _tmpImportance = null;
        } else {
          _tmpImportance = __Importance_stringToEnum(_cursor.getString(_cursorIndexOfImportance));
        }
        _item.setImportance(_tmpImportance);
        final Task.Status _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = __Status_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
        }
        _item.setStatus(_tmpStatus);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __Status_enumToString(@NonNull final Task.Status _value) {
    switch (_value) {
      case ACTIVE: return "ACTIVE";
      case PAUSED: return "PAUSED";
      case DONE: return "DONE";
      case CANCELED: return "CANCELED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __RepeatUnit_enumToString(@NonNull final Task.RepeatUnit _value) {
    switch (_value) {
      case DAY: return "DAY";
      case WEEK: return "WEEK";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __Difficulty_enumToString(@NonNull final Task.Difficulty _value) {
    switch (_value) {
      case VERY_EASY: return "VERY_EASY";
      case EASY: return "EASY";
      case HARD: return "HARD";
      case EXTREME: return "EXTREME";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __Importance_enumToString(@NonNull final Task.Importance _value) {
    switch (_value) {
      case NORMAL: return "NORMAL";
      case IMPORTANT: return "IMPORTANT";
      case VERY_IMPORTANT: return "VERY_IMPORTANT";
      case SPECIAL: return "SPECIAL";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private Task.RepeatUnit __RepeatUnit_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "DAY": return Task.RepeatUnit.DAY;
      case "WEEK": return Task.RepeatUnit.WEEK;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private Task.Difficulty __Difficulty_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "VERY_EASY": return Task.Difficulty.VERY_EASY;
      case "EASY": return Task.Difficulty.EASY;
      case "HARD": return Task.Difficulty.HARD;
      case "EXTREME": return Task.Difficulty.EXTREME;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private Task.Importance __Importance_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "NORMAL": return Task.Importance.NORMAL;
      case "IMPORTANT": return Task.Importance.IMPORTANT;
      case "VERY_IMPORTANT": return Task.Importance.VERY_IMPORTANT;
      case "SPECIAL": return Task.Importance.SPECIAL;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private Task.Status __Status_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "ACTIVE": return Task.Status.ACTIVE;
      case "PAUSED": return Task.Status.PAUSED;
      case "DONE": return Task.Status.DONE;
      case "CANCELED": return Task.Status.CANCELED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
