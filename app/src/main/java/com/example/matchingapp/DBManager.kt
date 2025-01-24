import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBManager(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    // 테이블 생성
    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(
            "CREATE TABLE IF NOT EXISTS UserInfo (" + "id TEXT PRIMARY KEY, " + "password TEXT)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS Profile (" + "id TEXT PRIMARY KEY, " + "isMentor BOOLEAN, " + "major TEXT, " +
                    "FOREIGN KEY(id) REFERENCES UserInfo(id) ON DELETE CASCADE)"
        )
    }

    // 데이터베이스 업그레이드 메서드
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS userinfo")
        db.execSQL("DROP TABLE IF EXISTS profile")
        onCreate(db)
    }

    // UserInfo : 회원가입시 아이디와 비밀번호 저장
    fun registerUser(id: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("id", id)
            put("password", password)
        }
        // 아이디가 이미 존재하면 저장하지 않음 (다시입력하라는 팝업 필요)
        return db.insertWithOnConflict("UserInfo", null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    // UserInfo : 로그인시 아이디와 비밀번호 일치 여부 확인
    fun loginUser(id: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM UserInfo WHERE id = ? AND password = ?", arrayOf(id, password))
        val isValid = cursor.count > 0
        cursor.close()
        return isValid
    }

    // UserInfo : 아이디 중복 여부 확인 (중복시 true반환)
    fun checkIdExist(id: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM UserInfo WHERE id = ?", arrayOf(id))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }


    // Profile : 프로필 등록
    fun insertProfileData(id: String, isMentor: Boolean, major: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("id", id)
            put("isMentor", isMentor)
            put("major", major)
        }
        return db.insert("Profile", null, values)
    }


    // Profile : 프로필 조회 (id로 조회)
    fun getProfileByName(id: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM Profile WHERE name = ?", arrayOf(id))
    }


    // Profile : 프로필 수정 (이름 기준)
    fun updateProfile(id: String, isMentor: Boolean, major: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("isMentor", isMentor)
            put("major", major)
        }

        return db.update("Profile", values, "id = ?", arrayOf(id))
    }


    // 회원 탈퇴: id 기준으로 삭제 (탈퇴시 프로필 까지 삭제하도록)
    fun deleteUser(id: String): Boolean {
        val db = this.writableDatabase
        val rowsAffected = db.delete("UserInfo", "id = ?", arrayOf(id))
        return rowsAffected > 0
    }


    // Profile : 모든 프로필 조회, 멘토멘티 찾기 페이지 프로필 로딩용. (임의추가)
    fun getAllProfiles(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM Profile", null)
    }

}
