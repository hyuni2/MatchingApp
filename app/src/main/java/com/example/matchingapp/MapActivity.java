package com.example.matchingapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import kotlin.Pair;
import okhttp3.*;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
public class MapActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "qv2tml0f23"; // 네이버 클라우드 API Client ID
    private static final String CLIENT_SECRET = "SliLCVpptr1m7oHxAwbGhlrcEhSr0ii7QCArRtK7"; // 네이버 클라우드 API Client Secret
    private static final String BASE_API_URL = "https://naveropenapi.apigw.ntruss.com/map-static/v2/raster";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private ImageView mapImageView;
    private LinearLayout mapPopupLayout; // 지도 팝업 레이아웃
    private TextView locationTextView;
    private Button confirmLocationButton;
    private double selectedLat;
    private double selectedLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapImageView = findViewById(R.id.mapImageView);
        //locationTextView = findViewById(R.id.locationTextView);
        confirmLocationButton = findViewById(R.id.confirmLocationButton);
        Button loadMapButton = findViewById(R.id.loadMapButton);
        EditText addressInput = findViewById(R.id.addressInput);
        Button confirmAddressButton = findViewById(R.id.confirmAddressButton);
        ImageButton closeMapButton = findViewById(R.id.closeMapButton);
        ImageButton closeMainButton = findViewById(R.id.closeMainButton);
        mapPopupLayout = findViewById(R.id.mapPopupLayout);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //위치 요청 설정 (정확도랑 속도높임)
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // 정확도
                .setInterval(5000) // 5초마다 위치업데이트
                .setFastestInterval(2000);
        // 앱 실행하면 지도 숨김
        mapImageView.setVisibility(View.GONE);

        // 현재 로그인한 사용자 ID
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("loggedInUser", null);

        if (userId != null) {
            DBManager dbManager = new DBManager(this, "MatchingAppDB", null, 1);
            UserLocation userLocation = dbManager.getUserLocation(userId);
            if (userLocation != null) {
                selectedLat = userLocation.getLatitude();
                selectedLng = userLocation.getLongitude();

                loadMap();
            } else {
                // 저장된위치 없으면 현재 위치 가져옴
                getCurrentLocation();
            }
        } else {
            // 로그인정보 없을 경우 기본위치 로드
            getCurrentLocation();
        }

        closeMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        closeMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapPopupLayout.setVisibility(View.GONE);
            }
        });

        loadMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MAP", "지도 불러오기 버튼 클릭됨"); //로그 확인
                mapImageView.setVisibility(View.VISIBLE);
                addressInput.setVisibility(View.VISIBLE);
                confirmAddressButton.setVisibility(View.VISIBLE);
                mapPopupLayout.setVisibility(View.VISIBLE); //팝업 레이아웃 보이기
                loadMap();
            }
        });


        // "주소 확인" 버튼 클릭- 입력된 주소를 좌표로 변환
        confirmAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = addressInput.getText().toString().trim();
                if (!address.isEmpty()) {
                    convertAddressToCoordinates(address);
                } else {
                    Toast.makeText(MapActivity.this, "주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //위치 설정 완료 버튼
        confirmLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAddressFromLatLng(selectedLat, selectedLng);
                saveLocation(selectedLat, selectedLng);

                //MyPageFragment로 데이터 전달 (주소 포함)
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedLat", selectedLat);
                resultIntent.putExtra("selectedLng", selectedLng);
                setResult(RESULT_OK, resultIntent);

                finish(); // 팝업 종료
            }
        });

        // 지도 클릭(사용자가 원하는 위치 선택)
        mapImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedLat += 0.001;
                selectedLng += 0.001;
                loadMap();
            }
        });
    }



    // 📌 주소를 위도, 경도로 변환하는 메서드
    private void convertAddressToCoordinates(String address) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Geocoder geocoder = new Geocoder(MapActivity.this, Locale.KOREA);
                    List<Address> addresses = geocoder.getFromLocationName(address, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address location = addresses.get(0);
                        selectedLat = location.getLatitude();
                        selectedLng = location.getLongitude();
// 📌 변환된 좌표를 저장하고 지도 업데이트
                        saveLocation(selectedLat, selectedLng);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadMap(); // 📌 변환된 좌표로 지도 다시 로드
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MapActivity.this, "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    Log.e("MAP", "주소 변환 실패", e);
                }
            }
        }).start();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
// 📌 권한이 허용되었을 때 현재 위치 가져오기
                getCurrentLocation();
            } else {
// 📌 권한이 거부되었을 때 안내 메시지 추가
                Toast.makeText(this, "위치 권한이 필요합니다. 설정에서 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
            }
        }
    }
    // 📌 현재 위치 가져오기
    private void getCurrentLocation() {
// 📌 위치 권한이 있는지 확인
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
// 📌 권한이 없으면 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        try {
// 📌 가장 최근 캐시된 위치 가져오기
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        selectedLat = location.getLatitude();
                        selectedLng = location.getLongitude();
                        saveLocation(selectedLat, selectedLng);
                        loadMap();
                    } else {
                        Log.e("MAP", "현재 위치를 가져올 수 없음");
// 📌 캐시된 위치가 없으면 새로운 위치 요청
                        LocationRequest locationRequest = LocationRequest.create()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(5000) // 5초마다 업데이트
                                .setFastestInterval(2000);
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("MAP", "위치 권한이 없어 현재 위치를 가져올 수 없음.", e);
        }
    }
    // 📌 지도 이미지 불러오기
    private void loadMap() {
//mapImageView.setVisibility(View.VISIBLE);
        String API_URL = BASE_API_URL +
                "?w=600&h=400&center=" + selectedLng + "," + selectedLat +
                "&level=13&markers=type:d|size:mid|pos:" + selectedLng + " " + selectedLat;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("X-NCP-APIGW-API-KEY-ID", CLIENT_ID)
                .addHeader("X-NCP-APIGW-API-KEY", CLIENT_SECRET)
                .build();
        Log.d("MAP", "API 요청 URL: " + API_URL);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MAP", "API 요청 실패: " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("MAP", "API 응답 실패: " + response.code() + " " + response.message());
                    return;
                }
                final Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("MAP", "지도 이미지 적용 완료");
                        mapImageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }


    // 📌 좌표 -> 주소 변환 후 MyPageFragment로 데이터 전달 + 주소 반환 추가
    private String getAddressFromLatLng(final double lat, final double lng) {
        Geocoder geocoder = new Geocoder(MapActivity.this, Locale.KOREA);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                final String address = addresses.get(0).getAddressLine(0);
                Log.d("MAP", "주소 변환 성공: " + address);

                // ✅ MyPageFragment로 변환된 주소 전달
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedLat", lat);
                resultIntent.putExtra("selectedLng", lng);
                resultIntent.putExtra("selectedAddress", address);
                setResult(RESULT_OK, resultIntent);

                // ✅ UI 업데이트 + 팝업 종료 유지
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish(); // 팝업 종료
                    }
                });

                return address; // ✅ 변환된 주소를 반환하도록 추가
            } else {
                return "주소를 찾을 수 없습니다.";
            }
        } catch (IOException e) {
            Log.e("MAP", "주소 변환 실패", e);
            return "주소 변환 오류";
        }
    }

    // 📌 저장된 위치 불러오기
    private void loadSavedLocation() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        selectedLat = Double.longBitsToDouble(prefs.getLong("saved_lat", Double.doubleToLongBits(0.0)));
        selectedLng = Double.longBitsToDouble(prefs.getLong("saved_lng", Double.doubleToLongBits(0.0)));
    }

    // 📌 위치 저장할 때 DB에도 저장
    private void saveLocation(double lat, double lng) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("saved_lat", Double.doubleToLongBits(lat));
        editor.putLong("saved_lng", Double.doubleToLongBits(lng));
        editor.apply();

        // 📌 현재 로그인한 사용자 ID 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("loggedInUser", null);

        if (userId != null) {
            DBManager dbManager = new DBManager(this, "MatchingAppDB", null, 1);
            dbManager.saveUserLocation(userId, lat, lng);
        }
    }
    // 📌 실시간 위치 업데이트 콜백
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) return;
            for (Location location : locationResult.getLocations()) {
                selectedLat = location.getLatitude();
                selectedLng = location.getLongitude();
                saveLocation(selectedLat, selectedLng);
                loadMap();
                Log.d("LOCATION", "새로운 위치 업데이트: " + selectedLat + ", " + selectedLng);
            }
            fusedLocationClient.removeLocationUpdates(this); // 더 이상 필요 없으면 업데이트 중지
        }
    };
}

