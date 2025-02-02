package com.example.matchingapp


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

class DBManager(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    // 테이블 생성
    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(
            "CREATE TABLE IF NOT EXISTS UserInfo (" +
                    "id TEXT PRIMARY KEY, " +
                    "password TEXT NOT NULL, "+
                    "salt TEXT NOT NULL)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS Profile (" +
                    "userid TEXT, " +  // Primary Key를 제거하고 Unique로 변경
                    "name TEXT, " +
                    "isMentor INTEGER, " +
                    "major TEXT, " +
                    "intro TEXT, " +
                    "UNIQUE(userid))"
        )


        // 멘토멘티 구인 테이블 // 프로필 테이블 수정 및 참조로 추후 변경
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS MentorMenteeBoard (" +
                    "userId TEXT, " +  // 작성자 ID
                    "name TEXT, " +  // 작성자 이름
                    "major TEXT, " + // 작성자 전공
                    "isMentor BOOLEAN, " + // 작성자 멘토멘티
                    "age INTEGER, " +  // 작성자 나이
                    "studentNum TEXT, " +  // 학번
                    "Content TEXT, " +  // 멘토/멘티 구인글
                    //"createdAt DATETIME DEFAULT CURRENT_TIMESTAMP, " + // 작성 시간(최신 작성시간 기준으로 정렬시 추가)
                    "FOREIGN KEY(userId) REFERENCES UserInfo(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                    "FOREIGN KEY(major) REFERENCES Profile(major) ON DELETE CASCADE ON UPDATE CASCADE, " +
                    "FOREIGN KEY(isMentor) REFERENCES Profile(isMentor) ON DELETE CASCADE ON UPDATE CASCADE)"

        )

        // MatchRequest 테이블 (새로운 테이블)
        //MyMatchHistory : 보낸신청/받은신청 필터링 기능용 new DB
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS MatchRequest (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "senderId TEXT, " + // 신청을 보낸 사용자 ID
                    "receiverId TEXT, " + // 신청을 받은 사용자 ID
                    "status TEXT, " + // 상태: 신청 완료, 매칭 완료, 매칭 실패, 수락, 거절
                    "FOREIGN KEY(senderId) REFERENCES UserInfo(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(receiverId) REFERENCES UserInfo(id) ON DELETE CASCADE)"
        )
    }

    // 데이터베이스 업그레이드 메서드
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS userinfo")
        db.execSQL("DROP TABLE IF EXISTS profile")
        db.execSQL("DROP TABLE IF EXISTS MentorMenteeBoard")
        db.execSQL("DROP TABLE IF EXISTS MatchRequest")
        onCreate(db)
    }

    // 비밀번호 해시화 (SHA-256 + Salt)
    private fun hashPasswordWithSalt(password: String, salt: ByteArray): String {
        val saltedPassword = password.toByteArray() + salt  // Salt를 ByteArray로 붙이기
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(saltedPassword)
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    // Salt 생성 함수
    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16) // 16바이트 Salt
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(salt)
        return salt
    }

    // 비밀번호 검증 (로그인 시 사용)
    fun verifyPassword(storedHash: String, storedSaltBase64: String, enteredPassword: String): Boolean {
        val storedSalt = Base64.decode(storedSaltBase64, Base64.NO_WRAP)
        val enteredHash = hashPasswordWithSalt(enteredPassword, storedSalt)
        return enteredHash == storedHash
    }

    // 회원가입 (UserInfo 테이블에 id, password, salt 저장)
    fun registerUser(id: String, password: String): Long {
        val db = this.writableDatabase

        // 아이디 중복 체크
        val cursor = db.rawQuery("SELECT * FROM UserInfo WHERE id = ?", arrayOf(id))
        if (cursor.count > 0) {
            cursor.close()
            return -1L
        }

        // 새 Salt 생성 & 비밀번호 해시
        val salt = generateSalt()
        val hashedPassword = hashPasswordWithSalt(password, salt)
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)

        // UserInfo 테이블에 삽입 (Salt 따로 저장)
        val contentValues = ContentValues().apply {
            put("id", id)
            put("password", hashedPassword)
            put("salt", saltBase64)  // Salt 별도 저장!
        }

        val result = db.insert("UserInfo", null, contentValues)

        // Profile 테이블에도 같은 id 추가
        val profileValues = ContentValues().apply {
            put("userid", id)
            put("name", "")
            put("isMentor", 0)
            put("major", "")
            put("intro", "")
        }

        val profileResult = db.insert("Profile", null, profileValues)

        cursor.close()
        return if (result == -1L || profileResult == -1L) -1L else result
    }

    // 로그인 (DB에서 Salt 불러와서 검증)
    fun loginUser(id: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT password, salt FROM UserInfo WHERE id = ?", arrayOf(id))

        if (!cursor.moveToFirst()) {
            cursor.close()
            return false
        }

        val storedHash = cursor.getString(0)
        val storedSaltBase64 = cursor.getString(1)

        cursor.close()

        return verifyPassword(storedHash, storedSaltBase64, password)
    }



    // Profile : 프로필 조회 (userid로 조회)
    fun getProfileById(id: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM Profile WHERE userid = ?", arrayOf(id))
    }


    // 닉네임이 이미 존재하는지 확인하는 메소드
    fun checkIfNameExists(name: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM Profile WHERE name = ?"
        val cursor = db.rawQuery(query, arrayOf(name))

        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count > 0
    }


    // Profile : 프로필 수정 (이름 기준)
    fun updateProfile(id: String, newName: String, newisMentor: Int, newMajor: String, newIntro: String): Boolean {
        val db = this.writableDatabase

        // 현재 프로필에서 기존 이름을 조회
        val cursor = db.rawQuery("SELECT name FROM Profile WHERE userid = ?", arrayOf(id))

        // cursor가 비어 있지 않은지 확인
        if (cursor.moveToFirst()) {
            val currentNameIndex = cursor.getColumnIndex("name")
            if (currentNameIndex != -1) {
                val currentName = cursor.getString(currentNameIndex)
                cursor.close()

                // 이름이 변경되었을 때만 닉네임 중복 확인
                val isNameUsed = newName != currentName && checkIfNameExists(newName)

                if (isNameUsed) {
                    return false // 닉네임이 중복되었으면 업데이트 불가
                }

                val values = ContentValues().apply {
                    put("name", newName)    // 닉네임 업데이트
                    put("isMentor", newisMentor) // 멘토/멘티 여부 (1: 멘토, 0: 멘티)
                    put("major", newMajor)  // 전공 업데이트
                    put("intro", newIntro)  // 소개글 업데이트
                }

                // id를 기준으로 프로필 수정
                val rowsAffected = db.update("Profile", values, "userid = ?", arrayOf(id))
                return rowsAffected > 0
            } else {
                cursor.close()
                Log.e("DBManager", "Column 'name' not found in Profile table")
                return false
            }
        } else {
            cursor.close()
            Log.e("DBManager", "No profile found for userid: $id")
            return false
        }
    }


    // 회원 탈퇴: id 기준으로 삭제 (탈퇴시 프로필 까지 삭제하도록)
    fun deleteUser(id: String): Boolean {
        val db = this.writableDatabase

        // 트랜잭션을 사용하여 두 테이블에서 삭제를 한 번에 처리 (원자성 보장)
        db.beginTransaction()
        try {
            // UserInfo 테이블에서 사용자 삭제
            val rowsAffectedUserInfo = db.delete("UserInfo", "id = ?", arrayOf(id))

            // Profile 테이블에서 사용자 프로필 삭제
            val rowsAffectedProfile = db.delete("Profile", "userid = ?", arrayOf(id))

            // 두 테이블에서 모두 삭제가 성공적으로 이루어졌다면 커밋
            if (rowsAffectedUserInfo > 0 && rowsAffectedProfile > 0) {
                db.setTransactionSuccessful()
                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            // 예외가 발생하면 롤백
            return false
        } finally {
            // 트랜잭션 종료
            db.endTransaction()
        }
    }

    // 기존 프로필 정보를 가져오는 메서드
    fun getProfile(userId: String): Profile? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Profile WHERE userId = ?", arrayOf(userId))

        if (cursor != null && cursor.moveToFirst()) {
            // 컬럼 인덱스를 안전하게 가져오기
            val nameColumnIndex = cursor.getColumnIndex("name")
            val isMentorColumnIndex = cursor.getColumnIndex("isMentor")
            val majorColumnIndex = cursor.getColumnIndex("major")
            val introColumnIndex = cursor.getColumnIndex("intro")


            // 컬럼 인덱스가 -1이 아닌지 확인하고 데이터 추출
            if (nameColumnIndex != -1 && majorColumnIndex != -1 && introColumnIndex != -1 && isMentorColumnIndex != -1) {
                val name = cursor.getString(nameColumnIndex) // 이름
                val isMentor = cursor.getInt(isMentorColumnIndex) // isMentor가 null이 아닐 경우 처리
                val major = cursor.getString(majorColumnIndex) ?: "" // major가 null일 경우 빈 문자열 처리
                val intro = cursor.getString(introColumnIndex) ?: "" // intro가 null일 경우 빈 문자열 처리


                cursor.close()
                return Profile(userId, name, isMentor, major, intro) // Profile 객체 반환
            } else {
                // 컬럼 인덱스가 잘못되었을 경우 처리 (디버깅을 위한 로그 추가)
                Log.e("DBManager", "컬럼이 존재하지 않습니다. nameColumnIndex: $nameColumnIndex, majorColumnIndex: $majorColumnIndex, introColumnIndex: $introColumnIndex, isMentorColumnIndex: $isMentorColumnIndex")
            }
        }

        cursor.close()
        return null
    }

    // Profile : 모든 프로필 조회, 멘토멘티 찾기 페이지 프로필 로딩용. (임의추가)
    fun getAllProfiles(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM Profile", null)
    }

    fun getUserIdByName(name: String): String? {
        val db = this.readableDatabase
        var userId: String? = null
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery("SELECT userid FROM Profile WHERE name = ?", arrayOf(name))
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex("userid")
                if (columnIndex != -1) {
                    userId = cursor.getString(columnIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // 예외 발생 시 로그 출력
        } finally {
            cursor?.close() // 커서 닫기
            db.close() // 데이터베이스 닫기
        }

        return userId
    }







    //이하 4개 테이블 전부 신청 히스토리 관련 추가 DB

    // 신청 데이터 추가
    fun insertMatchRequest(senderId: String, receiverId: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("senderId", senderId)
            put("receiverId", receiverId)
            put("status", "신청 완료") // 초기 상태
        }
        val result = db.insert("MatchRequest", null, values)
        db.close()
        return result != -1L // 성공 여부 반환
    }

    // 특정 사용자가 보낸 신청 조회
    fun getSentRequests(userId: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM MatchRequest WHERE senderId = ?",
            arrayOf(userId)
        )
    }

    // 특정 사용자가 받은 신청 조회
    fun getReceivedRequests(userId: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM MatchRequest WHERE receiverId = ?",
            arrayOf(userId)
        )
    }

    // 신청 상태 업데이트
    fun updateRequestStatus(requestId: Int, status: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("status", status)
        }
        return db.update("MatchRequest", values, "id = ?", arrayOf(requestId.toString()))
    }

    // 검색기능
    fun searchProfiles(keyword: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM Profile WHERE name LIKE ? OR major LIKE ? OR intro LIKE ?",
            arrayOf("%$keyword%", "%$keyword%", "%$keyword%")
        )
    }
}




