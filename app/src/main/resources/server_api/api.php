<?php
declare(strict_types=1);

header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

$configFile = __DIR__ . '/config.php';
if (!is_file($configFile)) {
    echo json_encode(['ok' => false, 'message' => 'فایل config.php یافت نشد.'], JSON_UNESCAPED_UNICODE);
    exit;
}
require_once $configFile;

$pdo = $pdo ?? $db ?? $conn ?? null;
if (!$pdo instanceof PDO) {
    echo json_encode(['ok' => false, 'message' => 'اتصال به دیتابیس سایت برقرار نشد.'], JSON_UNESCAPED_UNICODE);
    exit;
}

$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
$pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);

const MELIPAYAMAK_SHARED_URL = 'https://console.melipayamak.com/api/send/shared/338e273d263744b88d3f26919beebe4b';

function jsonOut(array $data, int $status = 200): never {
    http_response_code($status);
    echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

function cleanMobile(string $mobile): string {
    $mobile = preg_replace('/[^0-9+]/', '', trim($mobile)) ?? '';
    if (str_starts_with($mobile, '+98')) $mobile = '0' . substr($mobile, 3);
    if (str_starts_with($mobile, '98') && strlen($mobile) === 12) $mobile = '0' . substr($mobile, 2);
    return $mobile;
}

function sendMeliSms(int $bodyId, string $to, array $args): bool {
    $to = cleanMobile($to);
    if (!preg_match('/^09\d{9}$/', $to)) return false;

    $payload = json_encode(['bodyId' => $bodyId, 'to' => $to, 'args' => array_values($args)], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    $ch = curl_init(MELIPAYAMAK_SHARED_URL);
    if (!$ch) return false;

    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => $payload,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_CONNECTTIMEOUT => 8,
        CURLOPT_TIMEOUT => 15,
        CURLOPT_HTTPHEADER => ['Content-Type: application/json'],
        CURLOPT_SSL_VERIFYPEER => true,
        CURLOPT_SSL_VERIFYHOST => 2
    ]);

    curl_exec($ch);
    $code = (int)curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    return $code >= 200 && $code < 300;
}

function bearerToken(): string {
    $header = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
    if (preg_match('/Bearer\s+(.+)/i', $header, $m)) return trim($m[1]);
    return trim((string)($_POST['token'] ?? $_GET['token'] ?? ''));
}

function requireUser(PDO $pdo): array {
    $token = bearerToken();
    if ($token === '') jsonOut(['ok' => false, 'message' => 'نشست کاربری معتبر نیست.'], 401);

    $st = $pdo->prepare(
        "SELECT u.*
         FROM login_sessions ls
         INNER JOIN users u ON u.id = ls.user_id
         WHERE ls.session_token_hash = ?
           AND ls.expires_at > NOW()
           AND u.is_active = 1
         LIMIT 1"
    );
    $st->execute([hash('sha256', $token)]);
    $user = $st->fetch();

    if (!$user) jsonOut(['ok' => false, 'message' => 'نشست منقضی شده است. دوباره وارد شوید.'], 401);

    $pdo->prepare("UPDATE login_sessions SET last_activity_at = NOW() WHERE session_token_hash = ?")
        ->execute([hash('sha256', $token)]);

    return $user;
}

function publicUser(array $u): array {
    return [
        'id' => (int)$u['id'],
        'name' => (string)($u['name'] ?? ''),
        'first_name' => (string)($u['first_name'] ?? ''),
        'last_name' => (string)($u['last_name'] ?? ''),
        'phone' => (string)$u['phone'],
        'email' => (string)($u['email'] ?? ''),
        'national_id' => (string)($u['national_id'] ?? ''),
        'birth_date' => (string)($u['birth_date'] ?? ''),
        'gender' => (string)($u['gender'] ?? 'unknown'),
        'avatar' => (string)($u['avatar'] ?? ''),
        'wallet_balance' => (int)round((float)($u['wallet_balance'] ?? 0)),
        'created_at' => (string)($u['created_at'] ?? ''),
        'last_login_at' => (string)($u['last_login_at'] ?? '')
    ];
}

function createSession(PDO $pdo, int $userId): string {
    $token = bin2hex(random_bytes(32));
    $pdo->prepare(
        "INSERT INTO login_sessions
         (user_id, session_token_hash, ip_address, user_agent, expires_at, last_activity_at, created_at)
         VALUES (?, ?, ?, ?, DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), NOW())"
    )->execute([
        $userId,
        hash('sha256', $token),
        $_SERVER['REMOTE_ADDR'] ?? null,
        $_SERVER['HTTP_USER_AGENT'] ?? null
    ]);
    return $token;
}

$action = trim((string)($_REQUEST['action'] ?? ''));

try {
    switch ($action) {
        case 'send_otp': {
            $mobile = cleanMobile((string)($_POST['mobile'] ?? ''));
            if (!preg_match('/^09\d{9}$/', $mobile)) jsonOut(['ok' => false, 'message' => 'شماره موبایل معتبر نیست.'], 422);

            $pdo->beginTransaction();

            $st = $pdo->prepare("SELECT id FROM users WHERE phone = ? LIMIT 1 FOR UPDATE");
            $st->execute([$mobile]);
            $user = $st->fetch();

            if (!$user) {
                $pdo->prepare("INSERT INTO users (phone, role, is_active, created_at, updated_at) VALUES (?, 'patient', 1, NOW(), NOW())")
                    ->execute([$mobile]);
                $userId = (int)$pdo->lastInsertId();
            } else {
                $userId = (int)$user['id'];
            }

            $code = (string)random_int(100000, 999999);
            $pdo->prepare("UPDATE otp_codes SET used_at = NOW() WHERE user_id = ? AND used_at IS NULL")->execute([$userId]);

            // otp_codes has no phone column in the site's schema.
            $pdo->prepare(
                "INSERT INTO otp_codes (user_id, code_hash, attempts, expires_at, created_at)
                 VALUES (?, ?, 0, DATE_ADD(NOW(), INTERVAL 2 MINUTE), NOW())"
            )->execute([$userId, password_hash($code, PASSWORD_DEFAULT)]);

            if (!sendMeliSms(536367, $mobile, [$code])) {
                $pdo->rollBack();
                jsonOut(['ok' => false, 'message' => 'ارسال پیامک کد تأیید ناموفق بود.'], 502);
            }

            $pdo->commit();
            jsonOut(['ok' => true, 'message' => 'کد تأیید ارسال شد.']);
        }

        case 'verify_otp': {
            $mobile = cleanMobile((string)($_POST['mobile'] ?? ''));
            $otp = trim((string)($_POST['otp'] ?? ''));

            if (!preg_match('/^09\d{9}$/', $mobile) || !preg_match('/^\d{6}$/', $otp)) {
                jsonOut(['ok' => false, 'message' => 'اطلاعات ورود نامعتبر است.'], 422);
            }

            $st = $pdo->prepare(
                "SELECT u.*, o.id AS otp_id, o.code_hash, o.attempts, o.expires_at
                 FROM users u
                 INNER JOIN otp_codes o ON o.user_id = u.id
                 WHERE u.phone = ? AND o.used_at IS NULL
                 ORDER BY o.id DESC LIMIT 1"
            );
            $st->execute([$mobile]);
            $row = $st->fetch();

            if (!$row || strtotime((string)$row['expires_at']) < time()) jsonOut(['ok' => false, 'message' => 'کد تأیید منقضی شده است.'], 422);
            if ((int)$row['attempts'] >= 5) jsonOut(['ok' => false, 'message' => 'تعداد تلاش‌های ورود بیش از حد مجاز است.'], 429);

            if (!password_verify($otp, (string)$row['code_hash'])) {
                $pdo->prepare("UPDATE otp_codes SET attempts = attempts + 1 WHERE id = ?")->execute([(int)$row['otp_id']]);
                jsonOut(['ok' => false, 'message' => 'کد تأیید صحیح نیست.'], 422);
            }

            $pdo->beginTransaction();
            $pdo->prepare("UPDATE otp_codes SET used_at = NOW() WHERE id = ?")->execute([(int)$row['otp_id']]);
            $pdo->prepare("UPDATE users SET phone_verified_at = NOW(), last_login_at = NOW() WHERE id = ?")->execute([(int)$row['id']]);
            $token = createSession($pdo, (int)$row['id']);
            $pdo->commit();

            $fresh = $pdo->prepare("SELECT * FROM users WHERE id = ? LIMIT 1");
            $fresh->execute([(int)$row['id']]);
            $user = $fresh->fetch() ?: $row;

            jsonOut(['ok' => true, 'message' => 'ورود با موفقیت انجام شد.', 'token' => $token, 'user' => publicUser($user)]);
        }

        case 'login_password': {
            $mobile = cleanMobile((string)($_POST['mobile'] ?? ''));
            $password = (string)($_POST['password'] ?? '');

            $st = $pdo->prepare("SELECT * FROM users WHERE phone = ? AND is_active = 1 LIMIT 1");
            $st->execute([$mobile]);
            $user = $st->fetch();

            if (!$user || empty($user['password_hash']) || !password_verify($password, (string)$user['password_hash'])) {
                jsonOut(['ok' => false, 'message' => 'شماره موبایل یا رمز عبور صحیح نیست.'], 401);
            }

            $token = createSession($pdo, (int)$user['id']);
            $pdo->prepare("UPDATE users SET last_login_at = NOW() WHERE id = ?")->execute([(int)$user['id']);
            jsonOut(['ok' => true, 'message' => 'ورود با موفقیت انجام شد.', 'token' => $token, 'user' => publicUser($user)]);
        }

        case 'logout': {
            $token = bearerToken();
            if ($token !== '') $pdo->prepare("DELETE FROM login_sessions WHERE session_token_hash = ?")->execute([hash('sha256', $token)]);
            jsonOut(['ok' => true]);
        }

        case 'sync_init': {
            $user = requireUser($pdo);
            $uid = (int)$user['id'];

            $services = $pdo->query(
                "SELECT id, title, duration_minutes, price, deposit_price, short_description, description, image_url
                 FROM services WHERE is_active = 1 ORDER BY id ASC"
            )->fetchAll();

            $clinics = $pdo->query(
                "SELECT id, name, address, phone, city, province, postal_code, latitude, longitude, description, image_url, working_hours
                 FROM clinics WHERE is_active = 1 ORDER BY id ASC"
            )->fetchAll();

            $slots = $pdo->query(
                "SELECT ts.id, ts.schedule_id, ts.slot_date, ts.start_time, ts.end_time, ts.status,
                        s.clinic_id, s.doctor_id
                 FROM time_slots ts
                 INNER JOIN schedules s ON s.id = ts.schedule_id
                 WHERE ts.status = 'available'
                   AND ts.slot_date >= CURDATE()
                   AND s.is_active = 1
                 ORDER BY ts.slot_date ASC, ts.start_time ASC"
            )->fetchAll();

            $stAppt = $pdo->prepare(
                "SELECT a.id, a.tracking_code, a.appointment_date, a.appointment_time, a.status,
                        a.patient_notes, a.admin_notes,
                        d.name AS doctor_name, d.specialty AS doctor_specialty,
                        s.title AS service_title, s.price AS service_price,
                        c.name AS clinic_name, c.address AS clinic_address, c.phone AS clinic_phone
                 FROM appointments a
                 LEFT JOIN doctors d ON d.id = a.doctor_id
                 LEFT JOIN services s ON s.id = a.service_id
                 LEFT JOIN clinics c ON c.id = a.clinic_id
                 WHERE a.user_id = ?
                 ORDER BY a.appointment_date DESC, a.appointment_time DESC"
            );
            $stAppt->execute([$uid]);
            $appointments = $stAppt->fetchAll();

            $stTx = $pdo->prepare(
                "SELECT id, amount, type, status, tracking_code, description, card_or_iban, created_at
                 FROM wallet_transactions WHERE user_id = ? ORDER BY id DESC"
            );
            $stTx->execute([$uid]);
            $walletTxs = $stTx->fetchAll();

            $stTr = $pdo->prepare(
                "SELECT t.id, t.title, t.treatment_date, t.sessions, t.notes,
                        s.title AS service_title, c.name AS clinic_name, d.name AS doctor_name
                 FROM treatments t
                 LEFT JOIN services s ON s.id = t.service_id
                 LEFT JOIN appointments a ON a.id = t.appointment_id
                 LEFT JOIN clinics c ON c.id = a.clinic_id
                 LEFT JOIN doctors d ON d.id = a.doctor_id
                 WHERE t.user_id = ?
                 ORDER BY t.treatment_date DESC, t.id DESC"
            );
            $stTr->execute([$uid]);
            $treatments = [];
            foreach ($stTr->fetchAll() as $tr) {
                $notes = json_decode((string)($tr['notes'] ?? ''), true);
                if (!is_array($notes)) $notes = ['body' => (string)($tr['notes'] ?? ''), 'attachments' => []];

                $treatments[] = [
                    'id' => (int)$tr['id'],
                    'title' => (string)($tr['title'] ?? ''),
                    'treatment_date' => (string)$tr['treatment_date'],
                    'service_title' => (string)($tr['service_title'] ?? ''),
                    'clinic_name' => (string)($tr['clinic_name'] ?? ''),
                    'doctor_name' => (string)($tr['doctor_name'] ?? ''),
                    'sessions' => (int)$tr['sessions'],
                    'body' => (string)($notes['body'] ?? ''),
                    'attachments' => is_array($notes['attachments'] ?? null) ? $notes['attachments'] : []
                ];
            }

            $stNotif = $pdo->prepare("SELECT id, title, message, type, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY id DESC");
            $stNotif->execute([$uid]);
            $notifications = $stNotif->fetchAll();

            $stTickets = $pdo->prepare("SELECT id, subject, message, status, created_at, updated_at FROM support_tickets WHERE user_id = ? ORDER BY id DESC");
            $stTickets->execute([$uid]);
            $tickets = $stTickets->fetchAll();

            $stChat = $pdo->prepare("SELECT id, sender_type, message, created_at FROM support_chat_messages WHERE user_id = ? ORDER BY id ASC");
            $stChat->execute([$uid]);
            $chat = $stChat->fetchAll();

            $stProfile = $pdo->prepare("SELECT * FROM users WHERE id = ? LIMIT 1");
            $stProfile->execute([$uid]);
            $freshUser = $stProfile->fetch() ?: $user;

            jsonOut([
                'ok' => true,
                'user' => publicUser($freshUser),
                'services' => $services,
                'clinics' => $clinics,
                'available_slots' => $slots,
                'appointments' => $appointments,
                'wallet_transactions' => $walletTxs,
                'treatments' => $treatments,
                'notifications' => $notifications,
                'support_tickets' => $tickets,
                'chat_messages' => $chat
            ]);
        }

        case 'update_profile': {
            $user = requireUser($pdo);
            $first = trim((string)($_POST['first_name'] ?? ''));
            $last = trim((string)($_POST['last_name'] ?? ''));
            $national = trim((string)($_POST['national_code'] ?? ''));
            $birth = trim((string)($_POST['birth_date'] ?? ''));
            $gender = trim((string)($_POST['gender'] ?? 'unknown'));
            $email = trim((string)($_POST['email'] ?? ''));

            if (!in_array($gender, ['male','female','other','unknown'], true)) $gender = 'unknown';

            $pdo->prepare(
                "UPDATE users SET name = ?, first_name = ?, last_name = ?, national_id = ?, birth_date = ?, gender = ?, email = ?, updated_at = NOW()
                 WHERE id = ?"
            )->execute([
                trim("$first $last"),
                $first,
                $last,
                $national !== '' ? $national : null,
                $birth !== '' ? $birth : null,
                $gender,
                $email !== '' ? $email : null,
                (int)$user['id']
            ]);

            jsonOut(['ok' => true, 'message' => 'پروفایل در دیتابیس سایت ذخیره شد.']);
        }

        case 'book_appointment': {
            $user = requireUser($pdo);
            $uid = (int)$user['id'];
            $serviceId = (int)($_POST['service_id'] ?? 0);
            $clinicId = (int)($_POST['clinic_id'] ?? 0);
            $slotId = (int)($_POST['slot_id'] ?? 0);
            $notes = trim((string)($_POST['patient_notes'] ?? ''));
            $paymentChoice = trim((string)($_POST['payment_choice'] ?? 'full'));

            if ($serviceId <= 0 || $clinicId <= 0 || $slotId <= 0) jsonOut(['ok' => false, 'message' => 'خدمت، مطب یا زمان نوبت نامعتبر است.'], 422);

            $pdo->beginTransaction();

            $st = $pdo->prepare(
                "SELECT ts.id, ts.slot_date, ts.start_time, ts.end_time, s.clinic_id, s.doctor_id
                 FROM time_slots ts
                 INNER JOIN schedules s ON s.id = ts.schedule_id
                 WHERE ts.id = ? AND s.clinic_id = ? AND ts.status = 'available' AND s.is_active = 1
                 FOR UPDATE"
            );
            $st->execute([$slotId, $clinicId]);
            $slot = $st->fetch();

            if (!$slot) {
                $pdo->rollBack();
                jsonOut(['ok' => false, 'message' => 'این زمان دیگر آزاد نیست یا توسط مدیریت غیرفعال شده است.'], 409);
            }

            $st = $pdo->prepare("SELECT 1 FROM service_clinics WHERE service_id = ? AND clinic_id = ? LIMIT 1");
            $st->execute([$serviceId, $clinicId]);
            if (!$st->fetchColumn()) {
                $pdo->rollBack();
                jsonOut(['ok' => false, 'message' => 'این خدمت در این مطب ارائه نمی‌شود.'], 422);
            }

            $st = $pdo->prepare("SELECT id FROM services WHERE id = ? AND is_active = 1 LIMIT 1");
            $st->execute([$serviceId]);
            if (!$st->fetch()) {
                $pdo->rollBack();
                jsonOut(['ok' => false, 'message' => 'خدمت انتخاب‌شده فعال نیست.'], 422);
            }

            // بدون callback/verify درگاه واقعی، هیچ پرداختی در اپ یا API جعل نمی‌شود.
            $tracking = (string)random_int(1000000, 9999999);
            $patientNotes = $notes !== '' ? $notes : "ثبت درخواست از اپلیکیشن؛ روش پرداخت انتخابی: $paymentChoice";

            $pdo->prepare("UPDATE time_slots SET status = 'booked' WHERE id = ? AND status = 'available'")->execute([$slotId]);

            $pdo->prepare(
                "INSERT INTO appointments
                 (tracking_code, user_id, doctor_id, clinic_id, service_id, time_slot_id, appointment_date, appointment_time, status, patient_notes, created_at, updated_at)
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'pending', ?, NOW(), NOW())"
            )->execute([
                $tracking,
                $uid,
                (int)$slot['doctor_id'],
                $clinicId,
                $serviceId,
                $slotId,
                $slot['slot_date'],
                $slot['start_time'],
                $patientNotes
            ]);

            $appointmentId = (int)$pdo->lastInsertId();

            $pdo->prepare(
                "INSERT INTO notifications (user_id, title, message, type, is_read, created_at)
                 VALUES (?, 'درخواست نوبت ثبت شد', ?, 'appointment', 0, NOW())"
            )->execute([
                $uid,
                "درخواست نوبت شما برای تاریخ {$slot['slot_date']} ساعت {$slot['start_time']} با کد پیگیری $tracking ثبت شد و در انتظار تأیید مطب است."
            ]);

            $pdo->commit();

            jsonOut([
                'ok' => true,
                'appointment_id' => $appointmentId,
                'tracking_code' => $tracking,
                'status' => 'pending',
                'message' => 'درخواست نوبت در دیتابیس سایت ثبت شد و تا تأیید مطب در وضعیت انتظار قرار دارد.'
            ]);
        }

        case 'cancel_appointment': {
            $user = requireUser($pdo);
            $uid = (int)$user['id'];
            $appointmentId = (int)($_POST['appointment_id'] ?? 0);
            $reason = trim((string)($_POST['cancellation_reason'] ?? 'لغو توسط بیمار'));

            $pdo->beginTransaction();

            $st = $pdo->prepare(
                "SELECT a.*, u.name AS patient_name, u.phone
                 FROM appointments a
                 INNER JOIN users u ON u.id = a.user_id
                 WHERE a.id = ? AND a.user_id = ?
                 FOR UPDATE"
            );
            $st->execute([$appointmentId, $uid]);
            $a = $st->fetch();

            if (!$a) {
                $pdo->rollBack();
                jsonOut(['ok' => false, 'message' => 'نوبت پیدا نشد یا متعلق به حساب شما نیست.'], 404);
            }

            if (in_array($a['status'], ['cancelled','completed','no_show'], true)) {
                $pdo->rollBack();
                jsonOut(['ok' => false, 'message' => 'این نوبت دیگر قابل لغو نیست.'], 409);
            }

            $note = "\n[علت لغو توسط بیمار: $reason | " . date('Y-m-d H:i:s') . "]";
            $pdo->prepare("UPDATE appointments SET status = 'cancelled', patient_notes = CONCAT(COALESCE(patient_notes,''), ?), updated_at = NOW() WHERE id = ?")
                ->execute([$note, $appointmentId]);

            $pdo->prepare("UPDATE time_slots SET status = 'available' WHERE id = ? AND status <> 'blocked'")
                ->execute([(int)$a['time_slot_id']]);

            $pdo->prepare(
                "INSERT INTO notifications (user_id, title, message, type, is_read, created_at)
                 VALUES (?, 'لغو نوبت', ?, 'appointment', 0, NOW())"
            )->execute([
                $uid,
                "نوبت با کد پیگیری {$a['tracking_code']} لغو شد. درخواست بررسی استرداد وجه توسط مدیریت انجام خواهد شد."
            ]);

            $pdo->commit();

            sendMeliSms(542683, (string)$a['phone'], [(string)($a['patient_name'] ?? 'کاربر گرامی')]);
            jsonOut(['ok' => true, 'message' => 'نوبت لغو و زمان آن دوباره در تقویم سایت آزاد شد.']);
        }

        case 'withdraw_wallet': {
            $user = requireUser($pdo);
            $uid = (int)$user['id'];
            $amount = (int)($_POST['amount'] ?? 0);
            $iban = trim((string)($_POST['iban_or_card'] ?? ''));

            if ($amount <= 0 || $iban === '') jsonOut(['ok' => false, 'message' => 'مبلغ و مقصد تسویه را وارد کنید.'], 422);

            $pdo->beginTransaction();
            $st = $pdo->prepare("SELECT id, name, phone, wallet_balance FROM users WHERE id = ? FOR UPDATE");
            $st->execute([$uid]);
            $u = $st->fetch();

            if (!$u || (float)$u['wallet_balance'] < $amount) {
                $pdo->rollBack();
                jsonOut(['ok' => false, 'message' => 'موجودی کیف پول کافی نیست.'], 409);
            }

            $tracking = 'WDR-' . date('YmdHis') . '-' . strtoupper(bin2hex(random_bytes(3)));

            $pdo->prepare("UPDATE users SET wallet_balance = wallet_balance - ? WHERE id = ?")->execute([$amount, $uid]);

            $pdo->prepare(
                "INSERT INTO wallet_transactions (user_id, amount, type, status, tracking_code, description, card_or_iban, created_at)
                 VALUES (?, ?, 'withdraw', 'pending', ?, 'درخواست تسویه بانکی', ?, NOW())"
            )->execute([$uid, $amount, $tracking, $iban]);

            $pdo->prepare(
                "INSERT INTO notifications (user_id, title, message, type, is_read, created_at)
                 VALUES (?, 'درخواست تسویه ثبت شد', ?, 'wallet', 0, NOW())"
            )->execute([$uid, "درخواست تسویه $amount تومان با کد پیگیری $tracking ثبت شد."]);

            $pdo->commit();

            sendMeliSms(536715, (string)$u['phone'], [(string)($u['name'] ?? 'کاربر گرامی'), number_format($amount)]);
            jsonOut(['ok' => true, 'tracking_code' => $tracking, 'message' => 'درخواست تسویه در دیتابیس سایت ثبت شد.']);
        }

        case 'ticket_create': {
            $user = requireUser($pdo);
            $subject = trim((string)($_POST['subject'] ?? ''));
            $message = trim((string)($_POST['message'] ?? ''));
            if ($subject === '' || $message === '') jsonOut(['ok' => false, 'message' => 'موضوع و متن تیکت الزامی است.'], 422);

            $pdo->prepare("INSERT INTO support_tickets (user_id, subject, message, status, created_at, updated_at) VALUES (?, ?, ?, 'open', NOW(), NOW())")
                ->execute([(int)$user['id'], $subject, $message]);

            jsonOut(['ok' => true, 'ticket_id' => (int)$pdo->lastInsertId(), 'message' => 'تیکت در سیستم پشتیبانی سایت ثبت شد.']);
        }

        case 'chat_send': {
            $user = requireUser($pdo);
            $message = trim((string)($_POST['message'] ?? ''));
            if ($message === '') jsonOut(['ok' => false, 'message' => 'متن پیام خالی است.'], 422);

            $pdo->prepare("INSERT INTO support_chat_messages (user_id, sender_type, message, created_at) VALUES (?, 'patient', ?, NOW())")
                ->execute([(int)$user['id'], $message]);

            jsonOut(['ok' => true, 'message' => 'پیام در چت پشتیبانی سایت ثبت شد.']);
        }

        case 'notification_read': {
            $user = requireUser($pdo);
            $id = (int)($_POST['notification_id'] ?? 0);
            $pdo->prepare("UPDATE notifications SET is_read = 1 WHERE id = ? AND user_id = ?")->execute([$id, (int)$user['id']]);
            jsonOut(['ok' => true]);
        }

        case 'notifications_read_all': {
            $user = requireUser($pdo);
            $pdo->prepare("UPDATE notifications SET is_read = 1 WHERE user_id = ?")->execute([(int)$user['id']]);
            jsonOut(['ok' => true]);
        }

        case 'notification_delete': {
            $user = requireUser($pdo);
            $id = (int)($_POST['notification_id'] ?? 0);
            $pdo->prepare("DELETE FROM notifications WHERE id = ? AND user_id = ?")->execute([$id, (int)$user['id']]);
            jsonOut(['ok' => true]);
        }

        case 'deposit_wallet':
            jsonOut(['ok' => false, 'message' => 'درگاه واقعی شارژ کیف پول هنوز به این API متصل نشده است؛ هیچ موجودی ساختگی ثبت نمی‌شود.'], 501);

        default:
            jsonOut(['ok' => false, 'message' => 'دستور نامعتبر است.'], 400);
    }
} catch (Throwable $e) {
    if ($pdo->inTransaction()) $pdo->rollBack();
    error_log('DrBenam API: ' . $e->getMessage());
    jsonOut(['ok' => false, 'message' => 'خطای داخلی سرور.'], 500);
}
