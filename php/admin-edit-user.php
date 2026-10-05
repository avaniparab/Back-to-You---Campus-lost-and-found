<?php
/**
 * Back to You — Campus Lost & Found System
 * Admin Edit User Endpoint
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

$input = json_decode(file_get_contents('php://input'), true);
if (!$input) {
    $input = $_POST;
}

$targetUserId = isset($input['id']) ? (int)$input['id'] : 0;
$name         = isset($input['name']) ? trim($input['name']) : '';
$email        = isset($input['email']) ? trim($input['email']) : '';
$role         = isset($input['role']) ? strtoupper(trim($input['role'])) : '';
$status       = isset($input['status']) ? strtoupper(trim($input['status'])) : '';
$password     = isset($input['password']) ? trim($input['password']) : '';

if ($targetUserId <= 0) {
    echo json_encode(['success' => false, 'message' => 'Invalid user ID.']);
    exit;
}

if (empty($name)) {
    echo json_encode(['success' => false, 'message' => 'Full Name is required.']);
    exit;
}

if (empty($email) || !filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(['success' => false, 'message' => 'Please provide a valid email address.']);
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

$currentAdminId = (int)$_SESSION['user_id'];

// Protection 1: Self-editing protections
if ($targetUserId === $currentAdminId) {
    if ($status === 'INACTIVE') {
        echo json_encode(['success' => false, 'message' => 'You cannot deactivate your own administrator account.']);
        exit;
    }
    if ($role !== 'ADMIN') {
        echo json_encode(['success' => false, 'message' => 'You cannot remove your own administrator role.']);
        exit;
    }
}

try {
    // Check if target user exists
    $stmt = $pdo->prepare("SELECT id, role, status FROM users WHERE id = ?");
    $stmt->execute([$targetUserId]);
    $targetUser = $stmt->fetch();

    if (!$targetUser) {
        echo json_encode(['success' => false, 'message' => 'User not found.']);
        exit;
    }

    // Protection 2: Prevent demoting or deactivating the last active administrator
    if ($targetUser['role'] === 'ADMIN' && ($role !== 'ADMIN' || $status === 'INACTIVE')) {
        $activeAdminCount = (int)$pdo->query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE'")->fetchColumn();
        if ($activeAdminCount <= 1) {
            echo json_encode(['success' => false, 'message' => 'Cannot modify the role or status of the only active administrator account.']);
            exit;
        }
    }

    // Check duplicate email for OTHER users
    $stmt = $pdo->prepare("SELECT id FROM users WHERE email = ? AND id != ?");
    $stmt->execute([$email, $targetUserId]);
    if ($stmt->fetch()) {
        echo json_encode(['success' => false, 'message' => 'Email address is already in use by another account.']);
        exit;
    }

    // Update with or without new password
    if (!empty($password)) {
        if (strlen($password) < 8) {
            echo json_encode(['success' => false, 'message' => 'New password must be at least 8 characters long.']);
            exit;
        }
        $passwordHash = password_hash($password, PASSWORD_DEFAULT);
        $updateStmt = $pdo->prepare("UPDATE users SET name = ?, email = ?, role = ?, status = ?, password = ? WHERE id = ?");
        $updateStmt->execute([$name, $email, $role, $status, $passwordHash, $targetUserId]);
    } else {
        $updateStmt = $pdo->prepare("UPDATE users SET name = ?, email = ?, role = ?, status = ? WHERE id = ?");
        $updateStmt->execute([$name, $email, $role, $status, $targetUserId]);
    }

    // Update active session name/email if editing self
    if ($targetUserId === $currentAdminId) {
        $_SESSION['name']  = $name;
        $_SESSION['email'] = $email;
    }

    echo json_encode([
        'success' => true,
        'message' => 'User details updated successfully.'
    ]);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error while updating user details.']);
}
