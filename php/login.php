<?php
/**
 * Back to You — Campus Lost & Found System
 * Login Endpoint
 * Phase 4 Backend
 */

session_start();
header('Content-Type: application/json');
require_once __DIR__ . '/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method.']);
    exit;
}

// Parse JSON input or $_POST
$input = json_decode(file_get_contents('php://input'), true);
if (!$input) {
    $input = $_POST;
}

$email    = isset($input['email']) ? trim($input['email']) : '';
$password = isset($input['password']) ? $input['password'] : '';

// 1. Server-Side Validation
if (empty($email) || !filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(['success' => false, 'message' => 'Please provide a valid email address.']);
    exit;
}

if (empty($password)) {
    echo json_encode(['success' => false, 'message' => 'Please enter your password.']);
    exit;
}

try {
    // 2. Fetch User Record
    $stmt = $pdo->prepare("SELECT id, name, email, password, role, status FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch();

    // 3. Password Verification (Generic error message for security)
    if (!$user || !password_verify($password, $user['password'])) {
        echo json_encode(['success' => false, 'message' => 'Invalid email or password.']);
        exit;
    }

    // 4. Account Status Verification
    if (isset($user['status']) && strtoupper($user['status']) === 'INACTIVE') {
        echo json_encode(['success' => false, 'message' => 'Your account has been deactivated. Please contact the administrator.']);
        exit;
    }

    // 4. Session Security
    session_regenerate_id(true);

    $_SESSION['user_id'] = (int)$user['id'];
    $_SESSION['name']    = $user['name'];
    $_SESSION['email']   = $user['email'];
    $_SESSION['role']    = $user['role'];

    // 5. Response (Never include password hash in JSON)
    echo json_encode([
        'success' => true,
        'message' => 'Login successful',
        'user'    => [
            'id'    => (int)$user['id'],
            'name'  => $user['name'],
            'email' => $user['email'],
            'role'  => $user['role']
        ]
    ]);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Login failed due to a database error.']);
}
