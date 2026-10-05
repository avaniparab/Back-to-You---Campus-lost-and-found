<?php
/**
 * Back to You — Campus Lost & Found System
 * Admin Get Users Endpoint
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

try {
    $stmt = $pdo->query("SELECT id, name, email, role, created_at FROM users ORDER BY id DESC");
    $users = $stmt->fetchAll();

    echo json_encode([
        'success' => true,
        'count'   => count($users),
        'users'   => $users
    ]);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error while fetching users.']);
}
