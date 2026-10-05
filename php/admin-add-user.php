<?php
/**
 * Back to You — Campus Lost & Found System
 * Admin Add User Endpoint
 */

session_start();
header('Content-Type: application/json');
require_once __DIR__ . '/db.php';

// Authorization Check: Must be logged in AND role must be ADMIN
if (!isset($_SESSION['user_id'])) {
    header('HTTP/1.1 401 Unauthorized');
    echo json_encode(['success' => false, 'message' => 'Unauthorized. Please log in as Admin.']);
    exit;
}

if (!isset($_SESSION['role']) || $_SESSION['role'] !== 'ADMIN') {
    header('HTTP/1.1 403 Forbidden');
    echo json_encode(['success' => false, 'message' => 'Forbidden. Admin access required.']);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method.']);
    exit;
}

// Parse JSON body or $_POST
$input = json_decode(file_get_contents('php://input'), true);
if (!$input) {
    $input = $_POST;
}

$name     = isset($input['name']) ? trim($input['name']) : (isset($input['fullname']) ? trim($input['fullname']) : '');
$email    = isset($input['email']) ? trim($input['email']) : '';
$password = isset($input['password']) ? $input['password'] : '';
$role     = isset($input['role']) ? strtoupper(trim($input['role'])) : 'STUDENT';
$status   = isset($input['status']) ? strtoupper(trim($input['status'])) : 'ACTIVE';

// Server-Side Validation
if (empty($name)) {
    echo json_encode(['success' => false, 'message' => 'Full Name is required.']);
    exit;
}

if (empty($email) || !filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(['success' => false, 'message' => 'Please provide a valid email address.']);
    exit;
}

if (empty($password) || strlen($password) < 8) {
    echo json_encode(['success' => false, 'message' => 'Password must be at least 8 characters long.']);
    exit;
}

if (!in_array($role, ['STUDENT', 'ADMIN'], true)) {
    echo json_encode(['success' => false, 'message' => 'Invalid user role selected.']);
    exit;
}

if (!in_array($status, ['ACTIVE', 'INACTIVE'], true)) {
    echo json_encode(['success' => false, 'message' => 'Invalid account status selected.']);
    exit;
}

try {
    // Check duplicate email
    $stmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->execute([$email]);
    if ($stmt->fetch()) {
        echo json_encode(['success' => false, 'message' => 'Email address is already registered.']);
        exit;
    }

    // Hash password
    $passwordHash = password_hash($password, PASSWORD_DEFAULT);

    // Insert user
    $stmt = $pdo->prepare("INSERT INTO users (name, email, password, role, status) VALUES (?, ?, ?, ?, ?)");
    $stmt->execute([$name, $email, $passwordHash, $role, $status]);

    $newId = $pdo->lastInsertId();

    echo json_encode([
        'success' => true,
        'message' => 'User created successfully.',
        'user_id' => (int)$newId
    ]);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error while creating user.']);
}
