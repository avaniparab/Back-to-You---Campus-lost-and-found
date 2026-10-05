<?php
/**
 * Back to You — Campus Lost & Found System
 * Student Registration Endpoint
 * Phase 4 Backend
 */

header('Content-Type: application/json');
require_once __DIR__ . '/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method.']);
    exit;
}

// Parse JSON body or $_POST
$input = json_decode(file_get_contents('php://input'), true);
if (!$input) {
    $input = $_POST;
}

$name            = isset($input['fullname']) ? trim($input['fullname']) : (isset($input['name']) ? trim($input['name']) : '');
$email           = isset($input['email']) ? trim($input['email']) : '';
$password        = isset($input['password']) ? $input['password'] : '';
$confirmPassword = isset($input['confirm_password']) ? $input['confirm_password'] : '';

// 1. Server-Side Validation
if (empty($name)) {
    echo json_encode(['success' => false, 'message' => 'Full Name is required.']);
    exit;
}

if (empty($email)) {
    echo json_encode(['success' => false, 'message' => 'Email address is required.']);
    exit;
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(['success' => false, 'message' => 'Invalid email address format.']);
    exit;
}

if (!preg_match('/@viva-technology\.org$/i', $email)) {
    echo json_encode(['success' => false, 'message' => 'Email address must end with @viva-technology.org domain.']);
    exit;
}

if (empty($password)) {
    echo json_encode(['success' => false, 'message' => 'Password is required.']);
    exit;
}

if (strlen($password) < 8) {
    echo json_encode(['success' => false, 'message' => 'Password must be at least 8 characters long.']);
    exit;
}

if (empty($confirmPassword)) {
    echo json_encode(['success' => false, 'message' => 'Please confirm your password.']);
    exit;
}

if ($password !== $confirmPassword) {
    echo json_encode(['success' => false, 'message' => 'Password and Confirm Password do not match.']);
    exit;
}

try {
    // 2. Check if email is already registered
    $stmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->execute([$email]);
    if ($stmt->fetch()) {
        echo json_encode(['success' => false, 'message' => 'Email address is already registered.']);
        exit;
    }

    // 3. Password Hashing (Never store plaintext passwords)
    $passwordHash = password_hash($password, PASSWORD_DEFAULT);

    // 4. Insert new user (Default role: STUDENT)
    $stmt = $pdo->prepare("INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, 'STUDENT')");
    $stmt->execute([$name, $email, $passwordHash]);

    echo json_encode([
        'success' => true,
        'message' => 'Registration successful! You can now log in.'
    ]);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Registration failed due to a database error.']);
}
