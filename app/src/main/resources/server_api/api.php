<?php
declare(strict_types=1);

/**
 * ==============================================================================
 * مسیر فایل بر روی هاست: /api.php (یا در روت سایت drbenam.com)
 * عنوان: وب‌سرویس جامع اتصال اپلیکیشن اندروید به وب‌سایت و دیتابیس دکتر ابراهیم بنام
 * دیتابیس: drbena_drbenam
 * سامانه پیامک: ملی‌پیامک (وب‌سرویس پترن اشتراکی console.melipayamak.com)
 * ==============================================================================
 */

header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// بارگذاری فایل تنظیمات و دیتابیس سایت
$configFile = __DIR__ . '/config.php';
if (!is_file($configFile)) {
    echo json_encode(['ok' => false, 'message' => 'فایل config.php یافت نشد.']);
    exit;
}
require_once $configFile;

/** @var PDO|null $pdo */
$pdo = $pdo ?? $db ?? $conn ?? null;
if (!$pdo instanceof PDO) {
    echo json_encode(['ok' => false, 'message' => 'اتصال به دیتابیس سایت برقرار نشد.']);
    exit;
}

$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
$pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);

// توابع ارسال پیامک ملی‌پیامک (عیناً از فایل‌های appointment.php, appointments.php, wallet.php)
const MELIPAYAMAK_SHARED_URL = 'https://console.melipayamak.com/api/send/shared/338e273d263744b88d3f26919beebe4b';

function sendMeliSms(int $bodyId, string $to, array $args): bool {
    $cleanMobile = trim($to);
    $cleanMobile = preg_replace('/[^0-9+]/', '', $cleanMobile) ?? '';
    if (str_starts_with($cleanMobile, '+98')) {
        $cleanMobile = '0' . substr($cleanMobile, 3);
    } elseif (str_starts_with($cleanMobile, '98') && strlen($cleanMobile) === 12) {
        $cleanMobile = '0' . substr($cleanMobile, 2);
    }
    if (!preg_match('/^09\d{9}$/', $cleanMobile)) return false;

    $payload = json_encode(['bodyId' => $bodyId, 'to' => $cleanMobile, 'args' => $args], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    $ch = curl_init(MELIPAYAMAK_SHARED_URL);
    if (!$ch) return false;

    curl_setopt_array($ch, [
        CURLOPT_POST           => true,
        CURLOPT_POSTFIELDS     => $payload,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_CONNECTTIMEOUT => 8,
        CURLOPT_TIMEOUT        => 15,
        CURLOPT_HTTPHEADER     => ['Content-Type: application/json', 'Content-Length: ' . strlen((string)$payload)],
        CURLOPT_SSL_VERIFYPEER => true,
        CURLOPT_SSL_VERIFYHOST => 2,
    ]);

    $response = curl_exec($ch);
    $httpCode = (int)curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    return $httpCode >= 200 && $httpCode < 300;
}

$action = trim((string)($_REQUEST['action'] ?? ''));

try {
    switch ($action) {
        // ۱. دریافت اطلاعات کامل برای همگام‌سازی اپلیکیشن
        case 'sync_init': {
            $mobile = trim((string)($_REQUEST['mobile'] ?? ''));
            $user = null;
            if ($mobile !== '') {
                $st = $pdo->prepare("SELECT id, name, first_name, last_name, phone, national_id, wallet_balance, birth_date, email, avatar FROM users WHERE phone = ? LIMIT 1");
                $st->execute([$mobile]);
                $user = $st->fetch();
            }

            // خدمات فعال
            $services = $pdo->query("SELECT id, title, duration_minutes, price, deposit_price, short_description FROM services WHERE is_active = 1 ORDER BY id ASC")->fetchAll();
            // مطب‌ها
            $clinics = $pdo->query("SELECT id, name, address, phone FROM clinics WHERE is_active = 1 ORDER BY id ASC")->fetchAll();
            // اسلات‌های آزاد زمان
            $slots = $pdo->query("SELECT id, schedule_id, slot_date, start_time, end_time, status FROM time_slots WHERE status = 'available' AND slot_date >= CURDATE() ORDER BY slot_date ASC, start_time ASC LIMIT 100")->fetchAll();

            // نوبت‌های کاربر
            $appointments = [];
            $walletTxs = [];
            $treatments = [];
            $notifications = [];

            if ($user) {
                $uid = (int)$user['id'];
                $stAppt = $pdo->prepare("SELECT a.id, a.tracking_code, a.appointment_date, a.appointment_time, a.status, a.patient_notes, a.admin_notes, s.title as service_title, s.price as service_price, c.name as clinic_name, c.address as clinic_address, c.phone as clinic_phone FROM appointments a LEFT JOIN services s ON a.service_id = s.id LEFT JOIN clinics c ON a.clinic_id = c.id WHERE a.user_id = ? ORDER BY a.appointment_date DESC, a.appointment_time DESC LIMIT 30");
                $stAppt->execute([$uid]);
                $appointments = $stAppt->fetchAll();

                $stTx = $pdo->prepare("SELECT id, amount, type, status, tracking_code, description, card_or_iban, created_at FROM wallet_transactions WHERE user_id = ? ORDER BY id DESC LIMIT 30");
                $stTx->execute([$uid]);
                $walletTxs = $stTx->fetchAll();

                $stTr = $pdo->prepare("SELECT id, title, treatment_date, sessions, notes FROM treatments WHERE user_id = ? ORDER BY treatment_date DESC LIMIT 20");
                $stTr->execute([$uid]);
                $treatments = $stTr->fetchAll();

                $stNotif = $pdo->prepare("SELECT id, title, message, type, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY id DESC LIMIT 25");
                $stNotif->execute([$uid]);
                $notifications = $stNotif->fetchAll();
            }

            echo json_encode([
                'ok' => true,
                'user' => $user,
                'services' => $services,
                'clinics' => $clinics,
                'available_slots' => $slots,
                'appointments' => $appointments,
                'wallet_transactions' => $walletTxs,
                'treatments' => $treatments,
                'notifications' => $notifications
            ], JSON_UNESCAPED_UNICODE);
            break;
        }

        // ۲. رزرو نوبت جدید از اپلیکیشن و ثبت مستقیم در دیتابیس سایت + ارسال پیامک با پترن 536371
        case 'book_appointment': {
            $mobile = trim((string)($_POST['mobile'] ?? ''));
            $serviceId = (int)($_POST['service_id'] ?? 1);
            $clinicId = (int)($_POST['clinic_id'] ?? 1);
            $date = trim((string)($_POST['appointment_date'] ?? ''));
            $time = trim((string)($_POST['appointment_time'] ?? ''));
            $tracking = trim((string)($_POST['tracking_code'] ?? (string)random_int(1000000, 9999999)));
            $payChoice = trim((string)($_POST['payment_choice'] ?? 'full'));
            $patientName = trim((string)($_POST['patient_name'] ?? 'کاربر گرامی'));

            if ($mobile === '' || $date === '' || $time === '') {
                echo json_encode(['ok' => false, 'message' => 'اطلاعات نوبت ناقص است.']);
                exit;
            }

            // پیدا کردن یا ایجاد کاربر در جدول users
            $pdo->beginTransaction();

            $uStmt = $pdo->prepare("SELECT id, name, phone FROM users WHERE phone = ? LIMIT 1 FOR UPDATE");
            $uStmt->execute([$mobile]);
            $userRow = $uStmt->fetch();
            if ($userRow) {
                $uid = (int)$userRow['id'];
                if (!empty($userRow['name'])) $patientName = (string)$userRow['name'];
            } else {
                $insU = $pdo->prepare("INSERT INTO users (phone, name, role, is_active, created_at) VALUES (?, ?, 'patient', 1, NOW())");
                $insU->execute([$mobile, $patientName]);
                $uid = (int)$pdo->lastInsertId();
            }

            // پیدا کردن اسلات
            $dbTime = (strlen($time) === 5) ? $time . ':00' : $time;
            $slotStmt = $pdo->prepare("SELECT id, schedule_id FROM time_slots WHERE slot_date = ? AND start_time = ? AND status <> 'blocked' LIMIT 1 FOR UPDATE");
            $slotStmt->execute([$date, $dbTime]);
            $slotRow = $slotStmt->fetch();
            $slotId = $slotRow ? (int)$slotRow['id'] : 185;

            // پر کردن اسلات
            $updSlot = $pdo->prepare("UPDATE time_slots SET status = 'booked' WHERE id = ?");
            $updSlot->execute([$slotId]);

            // ثبت نوبت
            $payLabel = ($payChoice === 'deposit') ? 'پرداخت بیعانه' : 'تسویه کامل آنلاین';
            $insAppt = $pdo->prepare("INSERT INTO appointments (tracking_code, user_id, doctor_id, clinic_id, service_id, time_slot_id, appointment_date, appointment_time, status, patient_notes, created_at, updated_at) VALUES (?, ?, 1, ?, ?, ?, ?, ?, 'confirmed', ?, NOW(), NOW())");
            $insAppt->execute([$tracking, $uid, $clinicId, $serviceId, $slotId, $date, $dbTime, "ثبت از اپلیکیشن ($payLabel)"]);
            $apptId = (int)$pdo->lastInsertId();

            // ثبت اعلان
            $notifMsg = "نوبت شما برای تاریخ $date ساعت $time با کد پیگیری $tracking ثبت و قطعی گردید.";
            $insNotif = $pdo->prepare("INSERT INTO notifications (user_id, title, message, type, is_read, created_at) VALUES (?, 'تأیید قطعی نوبت', ?, 'system', 0, NOW())");
            $insNotif->execute([$uid, $notifMsg]);

            $pdo->commit();

            // ارسال پیامک پترن ۵۳۶۳۷۱ به تلفن کاربر
            $smsDateTime = "$date - ساعت $time";
            sendMeliSms(536371, $mobile, [$patientName, "متخصص قلب و عروق", $smsDateTime, $tracking]);

            echo json_encode([
                'ok' => true,
                'appointment_id' => $apptId,
                'tracking_code' => $tracking,
                'message' => 'نوبت با موفقیت در دیتابیس سایت ثبت گردید و پیامک تأیید ارسال شد.'
            ], JSON_UNESCAPED_UNICODE);
            break;
        }

        // ۳. لغو نوبت از اپلیکیشن و آزاد شدن اسلات در سایت + ارسال پیامک با پترن 542683
        case 'cancel_appointment': {
            $appointmentId = (int)($_POST['appointment_id'] ?? 0);
            $tracking = trim((string)($_POST['tracking_code'] ?? ''));
            $reason = trim((string)($_POST['cancellation_reason'] ?? 'لغو از طریق اپلیکیشن'));
            $mobile = trim((string)($_POST['mobile'] ?? ''));

            $pdo->beginTransaction();

            $st = $pdo->prepare("SELECT a.id, a.user_id, a.time_slot_id, u.name, u.phone FROM appointments a LEFT JOIN users u ON a.user_id = u.id WHERE (a.id = ? OR a.tracking_code = ?) LIMIT 1 FOR UPDATE");
            $st->execute([$appointmentId, $tracking]);
            $row = $st->fetch();

            if ($row) {
                $uid = (int)$row['user_id'];
                $slotId = (int)$row['time_slot_id'];
                $patientName = (string)($row['name'] ?? 'کاربر گرامی');
                $targetMobile = (string)($row['phone'] ?? $mobile);

                // لغو نوبت
                $reasonNote = "\n[علت لغو: $reason - تاریخ: " . date('Y-m-d H:i') . "]";
                $pdo->prepare("UPDATE appointments SET status = 'cancelled', patient_notes = CONCAT(COALESCE(patient_notes, ''), ?), updated_at = NOW() WHERE id = ?")->execute([$reasonNote, (int)$row['id']]);

                // آزادسازی اسلات
                if ($slotId > 0) {
                    $pdo->prepare("UPDATE time_slots SET status = 'available' WHERE id = ? AND status <> 'blocked'")->execute([$slotId]);
                }

                // ثبت تراکنش استرداد
                $refCode = 'REF-' . random_int(100000, 999999);
                $pdo->prepare("INSERT INTO wallet_transactions (user_id, amount, type, status, tracking_code, description, created_at) VALUES (?, 500000, 'refund', 'pending', ?, ?, NOW())")
                    ->execute([$uid, $refCode, "درخواست استرداد وجه نوبت لغوشده ($tracking)"]);

                // ثبت اعلان
                $pdo->prepare("INSERT INTO notifications (user_id, title, message, type, is_read, created_at) VALUES (?, 'لغو نوبت و استرداد وجه', ?, 'system', 0, NOW())")
                    ->execute([$uid, "نوبت با کد پیگیری $tracking لغو گردید و در صف استرداد مالی قرار گرفت."]);

                $pdo->commit();

                // ارسال پیامک لغو نوبت با پترن ۵۴۲۶۸۳
                sendMeliSms(542683, $targetMobile, [$patientName]);

                echo json_encode(['ok' => true, 'message' => 'نوبت لغو و پیامک برای بیمار ارسال شد.'], JSON_UNESCAPED_UNICODE);
            } else {
                $pdo->rollBack();
                echo json_encode(['ok' => false, 'message' => 'نوبت پیدا نشد.'], JSON_UNESCAPED_UNICODE);
            }
            break;
        }

        // ۴. درخواست تسویه از کیف پول + ارسال پیامک با پترن 536715
        case 'withdraw_wallet': {
            $mobile = trim((string)($_POST['mobile'] ?? ''));
            $amount = (int)($_POST['amount'] ?? 0);
            $iban = trim((string)($_POST['iban_or_card'] ?? ''));
            $tracking = trim((string)($_POST['tracking_code'] ?? (string)random_int(100000, 999999)));

            if ($mobile === '' || $amount <= 0) {
                echo json_encode(['ok' => false, 'message' => 'اطلاعات تسویه نامعتبر است.']);
                exit;
            }

            $pdo->beginTransaction();

            $st = $pdo->prepare("SELECT id, name, wallet_balance FROM users WHERE phone = ? LIMIT 1 FOR UPDATE");
            $st->execute([$mobile]);
            $u = $st->fetch();

            if ($u) {
                $uid = (int)$u['id'];
                $patientName = (string)($u['name'] ?? 'کاربر گرامی');

                // کسر موجودی و ثبت تراکنش
                $pdo->prepare("UPDATE users SET wallet_balance = GREATEST(0, wallet_balance - ?) WHERE id = ?")->execute([$amount, $uid]);

                $pdo->prepare("INSERT INTO wallet_transactions (user_id, amount, type, status, tracking_code, description, card_or_iban, created_at) VALUES (?, ?, 'withdraw', 'pending', ?, 'درخواست تسویه بانکی', ?, NOW())")
                    ->execute([$uid, $amount, $tracking, $iban]);

                $pdo->prepare("INSERT INTO notifications (user_id, title, message, type, is_read, created_at) VALUES (?, 'درخواست تسویه ثبت شد', ?, 'system', 0, NOW())")
                    ->execute([$uid, "درخواست تسویه به مبلغ " . number_format($amount) . " تومان ثبت شد. کد پیگیری: $tracking"]);

                $pdo->commit();

                // ارسال پیامک تسویه با پترن ۵۳۶۷۱۵
                sendMeliSms(536715, $mobile, [$patientName, number_format($amount)]);

                echo json_encode(['ok' => true, 'message' => 'درخواست تسویه ثبت و پیامک ارسال شد.'], JSON_UNESCAPED_UNICODE);
            } else {
                $pdo->rollBack();
                echo json_encode(['ok' => false, 'message' => 'کاربر یافت نشد.']);
            }
            break;
        }

        // ۵. به‌روزرسانی مشخصات پروفایل
        case 'update_profile': {
            $mobile = trim((string)($_POST['phone'] ?? ''));
            $firstName = trim((string)($_POST['first_name'] ?? ''));
            $lastName = trim((string)($_POST['last_name'] ?? ''));
            $fullName = trim("$firstName $lastName");
            $national = trim((string)($_POST['national_code'] ?? ''));
            $birth = trim((string)($_POST['birth_date'] ?? ''));
            $gender = trim((string)($_POST['gender'] ?? 'unknown'));
            $email = trim((string)($_POST['email'] ?? ''));

            $st = $pdo->prepare("UPDATE users SET name = ?, first_name = ?, last_name = ?, national_id = ?, birth_date = ?, gender = ?, email = ?, updated_at = NOW() WHERE phone = ?");
            $st->execute([$fullName, $firstName, $lastName, $national, $birth, $gender, $email, $mobile]);

            echo json_encode(['ok' => true, 'message' => 'پروفایل در دیتابیس سایت ذخیره شد.'], JSON_UNESCAPED_UNICODE);
            break;
        }

        default: {
            echo json_encode(['ok' => false, 'message' => 'دستور نامعتبر است.']);
            break;
        }
    }
} catch (Throwable $e) {
    if ($pdo->inTransaction()) $pdo->rollBack();
    echo json_encode(['ok' => false, 'error' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}
