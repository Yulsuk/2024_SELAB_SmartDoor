<?php
// 데이터베이스 연결 설정
include("dbConn.php");

// 쿼리 실행 (entryTime 필드의 데이터 형식이 YYYY.MM.DD - HH:MM:SS 인 것으로 가정)
$sql = "SELECT entryTime FROM raspi_entrytime";
$result = mysqli_query($conn, $sql);

// 결과를 배열로 변환
$data = array();
while ($row = mysqli_fetch_assoc($result)) {
    // entryTime에서 월과 시간을 추출
    $entryTime = $row['entryTime'];
    if (preg_match('/(\d{4})\.(\d{2})\.(\d{2}) - (\d{2}):(\d{2}):(\d{2})/', $entryTime, $matches)) {
        $day = $matches[3]; // 일
        $hour = $matches[4];  // 시간
        // 배열에 추가
        $row['day'] = $day;
        $row['hour'] = $hour;
    }
    $data[] = $row;
}

// JSON으로 변환하여 출력
echo json_encode($data);
?>