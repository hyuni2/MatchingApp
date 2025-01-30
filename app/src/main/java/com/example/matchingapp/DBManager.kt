package com.example.matchingapp

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
            "CREATE TABLE IF NOT EXISTS UserInfo (" +
                    "id TEXT PRIMARY KEY, " +
                    "password TEXT)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS Profile (" +
                    "userid TEXT, " +  // Primary Key를 제거하고 Unique로 변경
                    "name TEXT, " +
                    "isMentor INTEGER, " +
                    "major TEXT, " +
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
        onCreate(db)
    }



    // UserInfo : 회원가입시 아이디와 비밀번호 저장 (중복여부까지)
    fun registerUser(id: String, password: String): Long {
        val db = this.writableDatabase

        // UserInfo 테이블에 아이디와 비밀번호 삽입
        val cursor = db.rawQuery("SELECT * FROM UserInfo WHERE id = ?", arrayOf(id))

        if (cursor.count > 0) {
            cursor.close()
            return -1L // 아이디가 이미 존재하면 -1을 반환
        }

        // UserInfo 테이블에 데이터 삽입
        val contentValues = ContentValues().apply {
            put("id", id)
            put("password", password)
        }

        val result = db.insert("UserInfo", null, contentValues)

        // Profile 테이블에도 같은 id 추가
        val profileValues = ContentValues().apply {
            put("userid", id)  // Profile 테이블의 'userid'에 UserInfo의 id 저장
            put("name", "")  // 기본값 설정
            put("isMentor", 0)  // 기본값 설정 (0: 일반 사용자, 1: 멘토)
            put("major", "")  // 기본값 설정
        }

        val profileResult = db.insert("Profile", null, profileValues)

        cursor.close()

        return if (result == -1L || profileResult == -1L) -1L else result
    }



    // UserInfo : 로그인시 아이디와 비밀번호 일치 여부 확인
    fun loginUser(id: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM UserInfo WHERE id = ? AND password = ?", arrayOf(id, password))
        val isValid = cursor.count > 0
        cursor.close()
        return isValid
    }


    // Profile : 프로필 조회 (id로 조회)
    fun getProfileById(id: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM Profile WHERE id = ?", arrayOf(id))
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
    fun updateProfile(id: String, newName: String, newMajor: String): Boolean {
        val db = this.writableDatabase

        // 닉네임 중복 확인
        val isNameUsed = checkIfNameExists(newName)

        if (isNameUsed) {
            return false // 닉네임이 중복되었으면 업데이트 불가
        }

        val values = ContentValues().apply {
            put("name", newName)
            put("major", newMajor)
        }

        // id를 기준으로 프로필 수정
        val rowsAffected = db.update("Profile", values, "id = ?", arrayOf(id))
        return rowsAffected > 0
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
            val rowsAffectedProfile = db.delete("Profile", "id = ?", arrayOf(id))

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


    // Profile : 모든 프로필 조회, 멘토멘티 찾기 페이지 프로필 로딩용. (임의추가)
    fun getAllProfiles(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM Profile", null)
    }

    //이하 4개 테이블 전부 신청 히스토리 관련 추가 DB

    // 신청 데이터 추가
    fun insertMatchRequest(senderId: String, receiverId: String, status: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("senderId", senderId)
            put("receiverId", receiverId)
            put("status", status)
        }
        return db.insert("MatchRequest", null, values)
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
}




