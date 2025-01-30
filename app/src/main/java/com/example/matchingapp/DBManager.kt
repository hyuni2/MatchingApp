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
            "CREATE TABLE IF NOT EXISTS UserInfo (" + "id TEXT PRIMARY KEY, " + "password TEXT)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS Profile (" + "id TEXT PRIMARY KEY, " + "name TEXT, " + "isMentor BOOLEAN, " + "major TEXT, " +
                    "FOREIGN KEY(id) REFERENCES UserInfo(id) ON DELETE CASCADE)"
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




    // UserInfo : 회원가입시 아이디와 비밀번호 저장
    fun registerUser(id: String, password: String): Long {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT * FROM UserInfo WHERE id = ?", arrayOf(id))

        if (cursor.count > 0) {
            cursor.close()
            return -1L // 아이디가 이미 존재하면 -1을 반환
        }

        val contentValues = ContentValues().apply {
            put("id", id)
            put("password", password)
        }

        val result = db.insert("UserInfo", null, contentValues)
        cursor.close()

        return result
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
        return db.rawQuery("SELECT * FROM Profile WHERE id = ?", arrayOf(id))
    }

    // userid로 프로필 불러오기
    fun getProfileByUserId(id: String): Cursor {
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




